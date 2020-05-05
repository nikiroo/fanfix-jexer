/*
 * Jexer - Java Text User Interface
 *
 * The MIT License (MIT)
 *
 * Copyright (C) 2019 Kevin Lamonte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * @author Kevin Lamonte [kevin.lamonte@gmail.com]
 * @version 1
 */
package jexer.backend;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;

import jexer.bits.Cell;
import jexer.bits.CellAttributes;
import jexer.bits.Color;
import jexer.bits.StringUtils;
import jexer.event.TCommandEvent;
import jexer.event.TInputEvent;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * This class reads keystrokes and mouse events and emits output to ANSI
 * X3.64 / ECMA-48 type terminals e.g. xterm, linux, vt100, ansi.sys, etc.
 */
public class ECMA48Terminal extends LogicalScreen
                            implements TerminalReader, Runnable {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * States in the input parser.
     */
    private enum ParseState {
        GROUND,
        ESCAPE,
        ESCAPE_INTERMEDIATE,
        CSI_ENTRY,
        CSI_PARAM,
        MOUSE,
        MOUSE_SGR,
    }

    /**
     * Available Jexer images support.
     */
    private enum JexerImageOption {
        DISABLED,
        JPG,
        PNG,
        RGB,
    }

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Emit debugging to stderr.
     */
    private boolean debugToStderr = false;

    /**
     * If true, emit T.416-style RGB colors for normal system colors.  This
     * is a) expensive in bandwidth, and b) potentially terrible looking for
     * non-xterms.
     */
    private static boolean doRgbColor = false;

    /**
     * The session information.
     */
    private SessionInfo sessionInfo;

    /**
     * The event queue, filled up by a thread reading on input.
     */
    private List<TInputEvent> eventQueue;

    /**
     * If true, we want the reader thread to exit gracefully.
     */
    private boolean stopReaderThread;

    /**
     * The reader thread.
     */
    private Thread readerThread;

    /**
     * Parameters being collected.  E.g. if the string is \033[1;3m, then
     * params[0] will be 1 and params[1] will be 3.
     */
    private List<String> params;

    /**
     * Current parsing state.
     */
    private ParseState state;

    /**
     * The time we entered ESCAPE.  If we get a bare escape without a code
     * following it, this is used to return that bare escape.
     */
    private long escapeTime;

    /**
     * The time we last checked the window size.  We try not to spawn stty
     * more than once per second.
     */
    private long windowSizeTime;

    /**
     * true if mouse1 was down.  Used to report mouse1 on the release event.
     */
    private boolean mouse1;

    /**
     * true if mouse2 was down.  Used to report mouse2 on the release event.
     */
    private boolean mouse2;

    /**
     * true if mouse3 was down.  Used to report mouse3 on the release event.
     */
    private boolean mouse3;

    /**
     * Cache the cursor visibility value so we only emit the sequence when we
     * need to.
     */
    private boolean cursorOn = true;

    /**
     * Cache the last window size to figure out if a TResizeEvent needs to be
     * generated.
     */
    private TResizeEvent windowResize = null;

    /**
     * If true, emit wide-char (CJK/Emoji) characters as sixel images.
     */
    private boolean wideCharImages = true;

    /**
     * Window width in pixels.  Used for sixel support.
     */
    private int widthPixels = 640;

    /**
     * Window height in pixels.  Used for sixel support.
     */
    private int heightPixels = 400;

    /**
     * If true, emit image data via sixel.
     */
    private boolean sixel = true;

    /**
     * If true, use a single shared palette for sixel.
     */
    private boolean sixelSharedPalette = true;

    /**
     * The sixel palette handler.
     */
    private SixelPalette palette = null;

    /**
     * The sixel post-rendered string cache.
     */
    private ImageCache sixelCache = null;

    /**
     * Number of colors in the sixel palette.  Xterm 335 defines the max as
     * 1024.  Valid values are: 2 (black and white), 256, 512, 1024, and
     * 2048.
     */
    private int sixelPaletteSize = 1024;

    /**
     * If true, emit image data via iTerm2 image protocol.
     */
    private boolean iterm2Images = false;

    /**
     * The iTerm2 post-rendered string cache.
     */
    private ImageCache iterm2Cache = null;

    /**
     * If not DISABLED, emit image data via Jexer image protocol if the
     * terminal supports it.
     */
    private JexerImageOption jexerImageOption = JexerImageOption.JPG;

    /**
     * The Jexer post-rendered string cache.
     */
    private ImageCache jexerCache = null;

    /**
     * If true, then we changed System.in and need to change it back.
     */
    private boolean setRawMode = false;

    /**
     * If true, '?' was seen in terminal response.
     */
    private boolean decPrivateModeFlag = false;

    /**
     * The terminal's input.  If an InputStream is not specified in the
     * constructor, then this InputStreamReader will be bound to System.in
     * with UTF-8 encoding.
     */
    private Reader input;

    /**
     * The terminal's raw InputStream.  If an InputStream is not specified in
     * the constructor, then this InputReader will be bound to System.in.
     * This is used by run() to see if bytes are available() before calling
     * (Reader)input.read().
     */
    private InputStream inputStream;

    /**
     * The terminal's output.  If an OutputStream is not specified in the
     * constructor, then this PrintWriter will be bound to System.out with
     * UTF-8 encoding.
     */
    private PrintWriter output;

    /**
     * The listening object that run() wakes up on new input.
     */
    private Object listener;

    // Colors to map DOS colors to AWT colors.
    private static java.awt.Color MYBLACK;
    private static java.awt.Color MYRED;
    private static java.awt.Color MYGREEN;
    private static java.awt.Color MYYELLOW;
    private static java.awt.Color MYBLUE;
    private static java.awt.Color MYMAGENTA;
    private static java.awt.Color MYCYAN;
    private static java.awt.Color MYWHITE;
    private static java.awt.Color MYBOLD_BLACK;
    private static java.awt.Color MYBOLD_RED;
    private static java.awt.Color MYBOLD_GREEN;
    private static java.awt.Color MYBOLD_YELLOW;
    private static java.awt.Color MYBOLD_BLUE;
    private static java.awt.Color MYBOLD_MAGENTA;
    private static java.awt.Color MYBOLD_CYAN;
    private static java.awt.Color MYBOLD_WHITE;

    /**
     * SixelPalette is used to manage the conversion of images between 24-bit
     * RGB color and a palette of sixelPaletteSize colors.
     */
    private class SixelPalette {

        /**
         * Color palette for sixel output, sorted low to high.
         */
        private List<Integer> rgbColors = new ArrayList<Integer>();

        /**
         * Map of color palette index for sixel output, from the order it was
         * generated by makePalette() to rgbColors.
         */
        private int [] rgbSortedIndex = new int[sixelPaletteSize];

        /**
         * The color palette, organized by hue, saturation, and luminance.
         * This is used for a fast color match.
         */
        private ArrayList<ArrayList<ArrayList<ColorIdx>>> hslColors;

        /**
         * Number of bits for hue.
         */
        private int hueBits = -1;

        /**
         * Number of bits for saturation.
         */
        private int satBits = -1;

        /**
         * Number of bits for luminance.
         */
        private int lumBits = -1;

        /**
         * Step size for hue bins.
         */
        private int hueStep = -1;

        /**
         * Step size for saturation bins.
         */
        private int satStep = -1;

        /**
         * Cached RGB to HSL result.
         */
        private int hsl[] = new int[3];

        /**
         * ColorIdx records a RGB color and its palette index.
         */
        private class ColorIdx {
            /**
             * The 24-bit RGB color.
             */
            public int color;

            /**
             * The palette index for this color.
             */
            public int index;

            /**
             * Public constructor.
             *
             * @param color the 24-bit RGB color
             * @param index the palette index for this color
             */
            public ColorIdx(final int color, final int index) {
                this.color = color;
                this.index = index;
            }
        }

        /**
         * Public constructor.
         */
        public SixelPalette() {
            makePalette();
        }

        /**
         * Find the nearest match for a color in the palette.
         *
         * @param color the RGB color
         * @return the index in rgbColors that is closest to color
         */
        public int matchColor(final int color) {

            assert (color >= 0);

            /*
             * matchColor() is a critical performance bottleneck.  To make it
             * decent, we do the following:
             *
             *   1. Find the nearest two hues that bracket this color.
             *
             *   2. Find the nearest two saturations that bracket this color.
             *
             *   3. Iterate within these four bands of luminance values,
             *      returning the closest color by Euclidean distance.
             *
             * This strategy reduces the search space by about 97%.
             */
            int red   = (color >>> 16) & 0xFF;
            int green = (color >>>  8) & 0xFF;
            int blue  =  color         & 0xFF;

            if (sixelPaletteSize == 2) {
                if (((red * red) + (green * green) + (blue * blue)) < 35568) {
                    // Black
                    return 0;
                }
                // White
                return 1;
            }


            rgbToHsl(red, green, blue, hsl);
            int hue = hsl[0];
            int sat = hsl[1];
            int lum = hsl[2];
            // System.err.printf("%d %d %d\n", hue, sat, lum);

            double diff = Double.MAX_VALUE;
            int idx = -1;

            int hue1 = hue / (360/hueStep);
            int hue2 = hue1 + 1;
            if (hue1 >= hslColors.size() - 1) {
                // Bracket pure red from above.
                hue1 = hslColors.size() - 1;
                hue2 = 0;
            } else if (hue1 == 0) {
                // Bracket pure red from below.
                hue2 = hslColors.size() - 1;
            }

            for (int hI = hue1; hI != -1;) {
                ArrayList<ArrayList<ColorIdx>> sats = hslColors.get(hI);
                if (hI == hue1) {
                    hI = hue2;
                } else if (hI == hue2) {
                    hI = -1;
                }

                int sMin = (sat / satStep) - 1;
                int sMax = sMin + 1;
                if (sMin < 0) {
                    sMin = 0;
                    sMax = 1;
                } else if (sMin == sats.size() - 1) {
                    sMax = sMin;
                    sMin--;
                }
                assert (sMin >= 0);
                assert (sMax - sMin == 1);

                // int sMin = 0;
                // int sMax = sats.size() - 1;

                for (int sI = sMin; sI <= sMax; sI++) {
                    ArrayList<ColorIdx> lums = sats.get(sI);

                    // True 3D colorspace match for the remaining values
                    for (ColorIdx c: lums) {
                        int rgbColor = c.color;
                        double newDiff = 0;
                        int red2   = (rgbColor >>> 16) & 0xFF;
                        int green2 = (rgbColor >>>  8) & 0xFF;
                        int blue2  =  rgbColor         & 0xFF;
                        newDiff += Math.pow(red2 - red, 2);
                        newDiff += Math.pow(green2 - green, 2);
                        newDiff += Math.pow(blue2 - blue, 2);
                        if (newDiff < diff) {
                            idx = rgbSortedIndex[c.index];
                            diff = newDiff;
                        }
                    }
                }
            }

            if (((red * red) + (green * green) + (blue * blue)) < diff) {
                // Black is a closer match.
                idx = 0;
            } else if ((((255 - red) * (255 - red)) +
                    ((255 - green) * (255 - green)) +
                    ((255 - blue) * (255 - blue))) < diff) {

                // White is a closer match.
                idx = sixelPaletteSize - 1;
            }
            assert (idx != -1);
            return idx;
        }

        /**
         * Clamp an int value to [0, 255].
         *
         * @param x the int value
         * @return an int between 0 and 255.
         */
        private int clamp(final int x) {
            if (x < 0) {
                return 0;
            }
            if (x > 255) {
                return 255;
            }
            return x;
        }

        /**
         * Dither an image to a sixelPaletteSize palette.  The dithered
         * image cells will contain indexes into the palette.
         *
         * @param image the image to dither
         * @return the dithered image.  Every pixel is an index into the
         * palette.
         */
        public BufferedImage ditherImage(final BufferedImage image) {

            BufferedImage ditheredImage = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_INT_ARGB);

            int [] rgbArray = image.getRGB(0, 0, image.getWidth(),
                image.getHeight(), null, 0, image.getWidth());
            ditheredImage.setRGB(0, 0, image.getWidth(), image.getHeight(),
                rgbArray, 0, image.getWidth());

            for (int imageY = 0; imageY < image.getHeight(); imageY++) {
                for (int imageX = 0; imageX < image.getWidth(); imageX++) {
                    int oldPixel = ditheredImage.getRGB(imageX,
                        imageY) & 0xFFFFFF;
                    int colorIdx = matchColor(oldPixel);
                    assert (colorIdx >= 0);
                    assert (colorIdx < sixelPaletteSize);
                    int newPixel = rgbColors.get(colorIdx);
                    ditheredImage.setRGB(imageX, imageY, colorIdx);

                    int oldRed   = (oldPixel >>> 16) & 0xFF;
                    int oldGreen = (oldPixel >>>  8) & 0xFF;
                    int oldBlue  =  oldPixel         & 0xFF;

                    int newRed   = (newPixel >>> 16) & 0xFF;
                    int newGreen = (newPixel >>>  8) & 0xFF;
                    int newBlue  =  newPixel         & 0xFF;

                    int redError   = (oldRed - newRed) / 16;
                    int greenError = (oldGreen - newGreen) / 16;
                    int blueError  = (oldBlue - newBlue) / 16;

                    int red, green, blue;
                    if (imageX < image.getWidth() - 1) {
                        int pXpY  = ditheredImage.getRGB(imageX + 1, imageY);
                        red   = ((pXpY >>> 16) & 0xFF) + (7 * redError);
                        green = ((pXpY >>>  8) & 0xFF) + (7 * greenError);
                        blue  = ( pXpY         & 0xFF) + (7 * blueError);
                        red = clamp(red);
                        green = clamp(green);
                        blue = clamp(blue);
                        pXpY = ((red & 0xFF) << 16);
                        pXpY |= ((green & 0xFF) << 8) | (blue & 0xFF);
                        ditheredImage.setRGB(imageX + 1, imageY, pXpY);

                        if (imageY < image.getHeight() - 1) {
                            int pXpYp = ditheredImage.getRGB(imageX + 1,
                                imageY + 1);
                            red   = ((pXpYp >>> 16) & 0xFF) + redError;
                            green = ((pXpYp >>>  8) & 0xFF) + greenError;
                            blue  = ( pXpYp         & 0xFF) + blueError;
                            red = clamp(red);
                            green = clamp(green);
                            blue = clamp(blue);
                            pXpYp = ((red & 0xFF) << 16);
                            pXpYp |= ((green & 0xFF) << 8) | (blue & 0xFF);
                            ditheredImage.setRGB(imageX + 1, imageY + 1, pXpYp);
                        }
                    } else if (imageY < image.getHeight() - 1) {
                        int pXmYp = ditheredImage.getRGB(imageX - 1,
                            imageY + 1);
                        int pXYp  = ditheredImage.getRGB(imageX,
                            imageY + 1);

                        red   = ((pXmYp >>> 16) & 0xFF) + (3 * redError);
                        green = ((pXmYp >>>  8) & 0xFF) + (3 * greenError);
                        blue  = ( pXmYp         & 0xFF) + (3 * blueError);
                        red = clamp(red);
                        green = clamp(green);
                        blue = clamp(blue);
                        pXmYp = ((red & 0xFF) << 16);
                        pXmYp |= ((green & 0xFF) << 8) | (blue & 0xFF);
                        ditheredImage.setRGB(imageX - 1, imageY + 1, pXmYp);

                        red   = ((pXYp >>> 16) & 0xFF) + (5 * redError);
                        green = ((pXYp >>>  8) & 0xFF) + (5 * greenError);
                        blue  = ( pXYp         & 0xFF) + (5 * blueError);
                        red = clamp(red);
                        green = clamp(green);
                        blue = clamp(blue);
                        pXYp = ((red & 0xFF) << 16);
                        pXYp |= ((green & 0xFF) << 8) | (blue & 0xFF);
                        ditheredImage.setRGB(imageX,     imageY + 1, pXYp);
                    }
                } // for (int imageY = 0; imageY < image.getHeight(); imageY++)
            } // for (int imageX = 0; imageX < image.getWidth(); imageX++)

            return ditheredImage;
        }

        /**
         * Convert an RGB color to HSL.
         *
         * @param red red color, between 0 and 255
         * @param green green color, between 0 and 255
         * @param blue blue color, between 0 and 255
         * @param hsl the hsl color as [hue, saturation, luminance]
         */
        private void rgbToHsl(final int red, final int green,
            final int blue, final int [] hsl) {

            assert ((red >= 0) && (red <= 255));
            assert ((green >= 0) && (green <= 255));
            assert ((blue >= 0) && (blue <= 255));

            double R = red / 255.0;
            double G = green / 255.0;
            double B = blue / 255.0;
            boolean Rmax = false;
            boolean Gmax = false;
            boolean Bmax = false;
            double min = (R < G ? R : G);
            min = (min < B ? min : B);
            double max = 0;
            if ((R >= G) && (R >= B)) {
                max = R;
                Rmax = true;
            } else if ((G >= R) && (G >= B)) {
                max = G;
                Gmax = true;
            } else if ((B >= G) && (B >= R)) {
                max = B;
                Bmax = true;
            }

            double L = (min + max) / 2.0;
            double H = 0.0;
            double S = 0.0;
            if (min != max) {
                if (L < 0.5) {
                    S = (max - min) / (max + min);
                } else {
                    S = (max - min) / (2.0 - max - min);
                }
            }
            if (Rmax) {
                assert (Gmax == false);
                assert (Bmax == false);
                H = (G - B) / (max - min);
            } else if (Gmax) {
                assert (Rmax == false);
                assert (Bmax == false);
                H = 2.0 + (B - R) / (max - min);
            } else if (Bmax) {
                assert (Rmax == false);
                assert (Gmax == false);
                H = 4.0 + (R - G) / (max - min);
            }
            if (H < 0.0) {
                H += 6.0;
            }
            hsl[0] = (int) (H * 60.0);
            hsl[1] = (int) (S * 100.0);
            hsl[2] = (int) (L * 100.0);

            assert ((hsl[0] >= 0) && (hsl[0] <= 360));
            assert ((hsl[1] >= 0) && (hsl[1] <= 100));
            assert ((hsl[2] >= 0) && (hsl[2] <= 100));
        }

        /**
         * Convert a HSL color to RGB.
         *
         * @param hue hue, between 0 and 359
         * @param sat saturation, between 0 and 100
         * @param lum luminance, between 0 and 100
         * @return the rgb color as 0x00RRGGBB
         */
        private int hslToRgb(final int hue, final int sat, final int lum) {
            assert ((hue >= 0) && (hue <= 360));
            assert ((sat >= 0) && (sat <= 100));
            assert ((lum >= 0) && (lum <= 100));

            double S = sat / 100.0;
            double L = lum / 100.0;
            double C = (1.0 - Math.abs((2.0 * L) - 1.0)) * S;
            double Hp = hue / 60.0;
            double X = C * (1.0 - Math.abs((Hp % 2) - 1.0));
            double Rp = 0.0;
            double Gp = 0.0;
            double Bp = 0.0;
            if (Hp <= 1.0) {
                Rp = C;
                Gp = X;
            } else if (Hp <= 2.0) {
                Rp = X;
                Gp = C;
            } else if (Hp <= 3.0) {
                Gp = C;
                Bp = X;
            } else if (Hp <= 4.0) {
                Gp = X;
                Bp = C;
            } else if (Hp <= 5.0) {
                Rp = X;
                Bp = C;
            } else if (Hp <= 6.0) {
                Rp = C;
                Bp = X;
            }
            double m = L - (C / 2.0);
            int red   = ((int) ((Rp + m) * 255.0)) << 16;
            int green = ((int) ((Gp + m) * 255.0)) << 8;
            int blue  =  (int) ((Bp + m) * 255.0);

            return (red | green | blue);
        }

        /**
         * Create the sixel palette.
         */
        private void makePalette() {
            // Generate the sixel palette.  Because we have no idea at this
            // layer which image(s) will be shown, we have to use a common
            // palette with sixelPaletteSize colors for everything, and
            // map the BufferedImage colors to their nearest neighbor in RGB
            // space.

            if (sixelPaletteSize == 2) {
                rgbColors.add(0);
                rgbColors.add(0xFFFFFF);
                rgbSortedIndex[0] = 0;
                rgbSortedIndex[1] = 1;
                return;
            }

            // We build a palette using the Hue-Saturation-Luminence model,
            // with 5+ bits for Hue, 2+ bits for Saturation, and 1+ bit for
            // Luminance.  We convert these colors to 24-bit RGB, sort them
            // ascending, and steal the first index for pure black and the
            // last for pure white.  The 8-bit final palette favors bright
            // colors, somewhere between pastel and classic television
            // technicolor.  9- and 10-bit palettes are more uniform.

            // Default at 256 colors.
            hueBits = 5;
            satBits = 2;
            lumBits = 1;

            assert (sixelPaletteSize >= 256);
            assert ((sixelPaletteSize == 256)
                || (sixelPaletteSize == 512)
                || (sixelPaletteSize == 1024)
                || (sixelPaletteSize == 2048));

            switch (sixelPaletteSize) {
            case 512:
                hueBits = 5;
                satBits = 2;
                lumBits = 2;
                break;
            case 1024:
                hueBits = 5;
                satBits = 2;
                lumBits = 3;
                break;
            case 2048:
                hueBits = 5;
                satBits = 3;
                lumBits = 3;
                break;
            }
            hueStep = (int) (Math.pow(2, hueBits));
            satStep = (int) (100 / Math.pow(2, satBits));
            // 1 bit for luminance: 40 and 70.
            int lumBegin = 40;
            int lumStep = 30;
            switch (lumBits) {
            case 2:
                // 2 bits: 20, 40, 60, 80
                lumBegin = 20;
                lumStep = 20;
                break;
            case 3:
                // 3 bits: 8, 20, 32, 44, 56, 68, 80, 92
                lumBegin = 8;
                lumStep = 12;
                break;
            }

            // System.err.printf("<html><body>\n");
            // Hue is evenly spaced around the wheel.
            hslColors = new ArrayList<ArrayList<ArrayList<ColorIdx>>>();

            final boolean DEBUG = false;
            ArrayList<Integer> rawRgbList = new ArrayList<Integer>();

            for (int hue = 0; hue < (360 - (360 % hueStep));
                 hue += (360/hueStep)) {

                ArrayList<ArrayList<ColorIdx>> satList = null;
                satList = new ArrayList<ArrayList<ColorIdx>>();
                hslColors.add(satList);

                // Saturation is linearly spaced between pastel and pure.
                for (int sat = satStep; sat <= 100; sat += satStep) {

                    ArrayList<ColorIdx> lumList = new ArrayList<ColorIdx>();
                    satList.add(lumList);

                    // Luminance brackets the pure color, but leaning toward
                    // lighter.
                    for (int lum = lumBegin; lum < 100; lum += lumStep) {
                        /*
                        System.err.printf("<font style = \"color:");
                        System.err.printf("hsl(%d, %d%%, %d%%)",
                            hue, sat, lum);
                        System.err.printf(";\">=</font>\n");
                        */
                        int rgbColor = hslToRgb(hue, sat, lum);
                        rgbColors.add(rgbColor);
                        ColorIdx colorIdx = new ColorIdx(rgbColor,
                            rgbColors.size() - 1);
                        lumList.add(colorIdx);

                        rawRgbList.add(rgbColor);
                        if (DEBUG) {
                            int red   = (rgbColor >>> 16) & 0xFF;
                            int green = (rgbColor >>>  8) & 0xFF;
                            int blue  =  rgbColor         & 0xFF;
                            int [] backToHsl = new int[3];
                            rgbToHsl(red, green, blue, backToHsl);
                            System.err.printf("%d [%d] %d [%d] %d [%d]\n",
                                hue, backToHsl[0], sat, backToHsl[1],
                                lum, backToHsl[2]);
                        }
                    }
                }
            }
            // System.err.printf("\n</body></html>\n");

            assert (rgbColors.size() == sixelPaletteSize);

            /*
             * We need to sort rgbColors, so that toSixel() can know where
             * BLACK and WHITE are in it.  But we also need to be able to
             * find the sorted values using the old unsorted indexes.  So we
             * will sort it, put all the indexes into a HashMap, and then
             * build rgbSortedIndex[].
             */
            Collections.sort(rgbColors);
            HashMap<Integer, Integer> rgbColorIndices = null;
            rgbColorIndices = new HashMap<Integer, Integer>();
            for (int i = 0; i < sixelPaletteSize; i++) {
                rgbColorIndices.put(rgbColors.get(i), i);
            }
            for (int i = 0; i < sixelPaletteSize; i++) {
                int rawColor = rawRgbList.get(i);
                rgbSortedIndex[i] = rgbColorIndices.get(rawColor);
            }
            if (DEBUG) {
                for (int i = 0; i < sixelPaletteSize; i++) {
                    assert (rawRgbList != null);
                    int idx = rgbSortedIndex[i];
                    int rgbColor = rgbColors.get(idx);
                    if ((idx != 0) && (idx != sixelPaletteSize - 1)) {
                        /*
                        System.err.printf("%d %06x --> %d %06x\n",
                            i, rawRgbList.get(i), idx, rgbColors.get(idx));
                        */
                        assert (rgbColor == rawRgbList.get(i));
                    }
                }
            }

            // Set the dimmest color as true black, and the brightest as true
            // white.
            rgbColors.set(0, 0);
            rgbColors.set(sixelPaletteSize - 1, 0xFFFFFF);

            /*
            System.err.printf("<html><body>\n");
            for (Integer rgb: rgbColors) {
                System.err.printf("<font style = \"color:");
                System.err.printf("#%06x", rgb);
                System.err.printf(";\">=</font>\n");
            }
            System.err.printf("\n</body></html>\n");
            */

        }

        /**
         * Emit the sixel palette.
         *
         * @param sb the StringBuilder to append to
         * @param used array of booleans set to true for each color actually
         * used in this cell, or null to emit the entire palette
         * @return the string to emit to an ANSI / ECMA-style terminal
         */
        public String emitPalette(final StringBuilder sb,
            final boolean [] used) {

            for (int i = 0; i < sixelPaletteSize; i++) {
                if (((used != null) && (used[i] == true)) || (used == null)) {
                    int rgbColor = rgbColors.get(i);
                    sb.append(String.format("#%d;2;%d;%d;%d", i,
                            ((rgbColor >>> 16) & 0xFF) * 100 / 255,
                            ((rgbColor >>>  8) & 0xFF) * 100 / 255,
                            ( rgbColor         & 0xFF) * 100 / 255));
                }
            }
            return sb.toString();
        }
    }

    /**
     * ImageCache is a least-recently-used cache that hangs on to the
     * post-rendered sixel or iTerm2 string for a particular set of cells.
     */
    private class ImageCache {

        /**
         * Maximum size of the cache.
         */
        private int maxSize = 100;

        /**
         * The entries stored in the cache.
         */
        private HashMap<String, CacheEntry> cache = null;

        /**
         * CacheEntry is one entry in the cache.
         */
        private class CacheEntry {
            /**
             * The cache key.
             */
            public String key;

            /**
             * The cache data.
             */
            public String data;

            /**
             * The last time this entry was used.
             */
            public long millis = 0;

            /**
             * Public constructor.
             *
             * @param key the cache entry key
             * @param data the cache entry data
             */
            public CacheEntry(final String key, final String data) {
                this.key = key;
                this.data = data;
                this.millis = System.currentTimeMillis();
            }
        }

        /**
         * Public constructor.
         *
         * @param maxSize the maximum size of the cache
         */
        public ImageCache(final int maxSize) {
            this.maxSize = maxSize;
            cache = new HashMap<String, CacheEntry>();
        }

        /**
         * Make a unique key for a list of cells.
         *
         * @param cells the cells
         * @return the key
         */
        private String makeKey(final ArrayList<Cell> cells) {
            StringBuilder sb = new StringBuilder();
            for (Cell cell: cells) {
                sb.append(cell.hashCode());
            }
            return sb.toString();
        }

        /**
         * Get an entry from the cache.
         *
         * @param cells the list of cells that are the cache key
         * @return the sixel string representing these cells, or null if this
         * list of cells is not in the cache
         */
        public String get(final ArrayList<Cell> cells) {
            CacheEntry entry = cache.get(makeKey(cells));
            if (entry == null) {
                return null;
            }
            entry.millis = System.currentTimeMillis();
            return entry.data;
        }

        /**
         * Put an entry into the cache.
         *
         * @param cells the list of cells that are the cache key
         * @param data the sixel string representing these cells
         */
        public void put(final ArrayList<Cell> cells, final String data) {
            String key = makeKey(cells);

            // System.err.println("put() " + key + " size " + cache.size());

            assert (!cache.containsKey(key));

            assert (cache.size() <= maxSize);
            if (cache.size() == maxSize) {
                // Cache is at limit, evict oldest entry.
                long oldestTime = Long.MAX_VALUE;
                String keyToRemove = null;
                for (CacheEntry entry: cache.values()) {
                    if ((entry.millis < oldestTime) || (keyToRemove == null)) {
                        keyToRemove = entry.key;
                        oldestTime = entry.millis;
                    }
                }
                /*
                System.err.println("put() remove key = " + keyToRemove +
                    " size " + cache.size());
                 */
                assert (keyToRemove != null);
                cache.remove(keyToRemove);
                /*
                System.err.println("put() removed, size " + cache.size());
                 */
            }
            assert (cache.size() <= maxSize);
            CacheEntry entry = new CacheEntry(key, data);
            assert (key.equals(entry.key));
            cache.put(key, entry);
            /*
            System.err.println("put() added key " + key + " " +
                " size " + cache.size());
             */
        }

    }

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Constructor sets up state for getEvent().  If either windowWidth or
     * windowHeight are less than 1, the terminal is not resized.
     *
     * @param listener the object this backend needs to wake up when new
     * input comes in
     * @param input an InputStream connected to the remote user, or null for
     * System.in.  If System.in is used, then on non-Windows systems it will
     * be put in raw mode; closeTerminal() will (blindly!) put System.in in
     * cooked mode.  input is always converted to a Reader with UTF-8
     * encoding.
     * @param output an OutputStream connected to the remote user, or null
     * for System.out.  output is always converted to a Writer with UTF-8
     * encoding.
     * @param windowWidth the number of text columns to start with
     * @param windowHeight the number of text rows to start with
     * @throws UnsupportedEncodingException if an exception is thrown when
     * creating the InputStreamReader
     */
    public ECMA48Terminal(final Object listener, final InputStream input,
        final OutputStream output, final int windowWidth,
        final int windowHeight) throws UnsupportedEncodingException {

        this(listener, input, output);

        // Send dtterm/xterm sequences, which will probably not work because
        // allowWindowOps is defaulted to false.
        if ((windowWidth > 0) && (windowHeight > 0)) {
            String resizeString = String.format("\033[8;%d;%dt", windowHeight,
                windowWidth);
            this.output.write(resizeString);
            this.output.flush();
        }
    }

    /**
     * Constructor sets up state for getEvent().
     *
     * @param listener the object this backend needs to wake up when new
     * input comes in
     * @param input an InputStream connected to the remote user, or null for
     * System.in.  If System.in is used, then on non-Windows systems it will
     * be put in raw mode; closeTerminal() will (blindly!) put System.in in
     * cooked mode.  input is always converted to a Reader with UTF-8
     * encoding.
     * @param output an OutputStream connected to the remote user, or null
     * for System.out.  output is always converted to a Writer with UTF-8
     * encoding.
     * @throws UnsupportedEncodingException if an exception is thrown when
     * creating the InputStreamReader
     */
    public ECMA48Terminal(final Object listener, final InputStream input,
        final OutputStream output) throws UnsupportedEncodingException {

        resetParser();
        mouse1           = false;
        mouse2           = false;
        mouse3           = false;
        stopReaderThread = false;
        this.listener    = listener;

        if (input == null) {
            // inputStream = System.in;
            inputStream = new FileInputStream(FileDescriptor.in);
            sttyRaw();
            setRawMode = true;
        } else {
            inputStream = input;
        }
        this.input = new InputStreamReader(inputStream, "UTF-8");

        if (input instanceof SessionInfo) {
            // This is a TelnetInputStream that exposes window size and
            // environment variables from the telnet layer.
            sessionInfo = (SessionInfo) input;
        }
        if (sessionInfo == null) {
            if (input == null) {
                // Reading right off the tty
                sessionInfo = new TTYSessionInfo();
            } else {
                sessionInfo = new TSessionInfo();
            }
        }

        if (output == null) {
            this.output = new PrintWriter(new OutputStreamWriter(System.out,
                    "UTF-8"));
        } else {
            this.output = new PrintWriter(new OutputStreamWriter(output,
                    "UTF-8"));
        }

        // Request Device Attributes
        this.output.printf("\033[c");

        // Request xterm report window/cell dimensions in pixels
        this.output.printf("%s", xtermReportPixelDimensions());

        // Enable mouse reporting and metaSendsEscape
        this.output.printf("%s%s", mouse(true), xtermMetaSendsEscape(true));

        // Request xterm use the sixel settings we want
        this.output.printf("%s", xtermSetSixelSettings());

        this.output.flush();

        // Query the screen size
        sessionInfo.queryWindowSize();
        setDimensions(sessionInfo.getWindowWidth(),
            sessionInfo.getWindowHeight());

        // Hang onto the window size
        windowResize = new TResizeEvent(TResizeEvent.Type.SCREEN,
            sessionInfo.getWindowWidth(), sessionInfo.getWindowHeight());

        reloadOptions();

        // Spin up the input reader
        eventQueue = new ArrayList<TInputEvent>();
        readerThread = new Thread(this);
        readerThread.start();

        // Clear the screen
        this.output.write(clearAll());
        this.output.flush();
    }

    /**
     * Constructor sets up state for getEvent().
     *
     * @param listener the object this backend needs to wake up when new
     * input comes in
     * @param input the InputStream underlying 'reader'.  Its available()
     * method is used to determine if reader.read() will block or not.
     * @param reader a Reader connected to the remote user.
     * @param writer a PrintWriter connected to the remote user.
     * @param setRawMode if true, set System.in into raw mode with stty.
     * This should in general not be used.  It is here solely for Demo3,
     * which uses System.in.
     * @throws IllegalArgumentException if input, reader, or writer are null.
     */
    public ECMA48Terminal(final Object listener, final InputStream input,
        final Reader reader, final PrintWriter writer,
        final boolean setRawMode) {

        if (input == null) {
            throw new IllegalArgumentException("InputStream must be specified");
        }
        if (reader == null) {
            throw new IllegalArgumentException("Reader must be specified");
        }
        if (writer == null) {
            throw new IllegalArgumentException("Writer must be specified");
        }
        resetParser();
        mouse1           = false;
        mouse2           = false;
        mouse3           = false;
        stopReaderThread = false;
        this.listener    = listener;

        inputStream = input;
        this.input = reader;

        if (setRawMode == true) {
            sttyRaw();
        }
        this.setRawMode = setRawMode;

        if (input instanceof SessionInfo) {
            // This is a TelnetInputStream that exposes window size and
            // environment variables from the telnet layer.
            sessionInfo = (SessionInfo) input;
        }
        if (sessionInfo == null) {
            if (setRawMode == true) {
                // Reading right off the tty
                sessionInfo = new TTYSessionInfo();
            } else {
                sessionInfo = new TSessionInfo();
            }
        }

        this.output = writer;

        // Request Device Attributes
        this.output.printf("\033[c");

        // Request xterm report window/cell dimensions in pixels
        this.output.printf("%s", xtermReportPixelDimensions());

        // Enable mouse reporting and metaSendsEscape
        this.output.printf("%s%s", mouse(true), xtermMetaSendsEscape(true));

        // Request xterm use the sixel settings we want
        this.output.printf("%s", xtermSetSixelSettings());

        this.output.flush();

        // Query the screen size
        sessionInfo.queryWindowSize();
        setDimensions(sessionInfo.getWindowWidth(),
            sessionInfo.getWindowHeight());

        // Hang onto the window size
        windowResize = new TResizeEvent(TResizeEvent.Type.SCREEN,
            sessionInfo.getWindowWidth(), sessionInfo.getWindowHeight());

        reloadOptions();

        // Spin up the input reader
        eventQueue = new ArrayList<TInputEvent>();
        readerThread = new Thread(this);
        readerThread.start();

        // Clear the screen
        this.output.write(clearAll());
        this.output.flush();
    }

    /**
     * Constructor sets up state for getEvent().
     *
     * @param listener the object this backend needs to wake up when new
     * input comes in
     * @param input the InputStream underlying 'reader'.  Its available()
     * method is used to determine if reader.read() will block or not.
     * @param reader a Reader connected to the remote user.
     * @param writer a PrintWriter connected to the remote user.
     * @throws IllegalArgumentException if input, reader, or writer are null.
     */
    public ECMA48Terminal(final Object listener, final InputStream input,
        final Reader reader, final PrintWriter writer) {

        this(listener, input, reader, writer, false);
    }

    // ------------------------------------------------------------------------
    // LogicalScreen ----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Set the window title.
     *
     * @param title the new title
     */
    @Override
    public void setTitle(final String title) {
        output.write(getSetTitleString(title));
        flush();
    }

    /**
     * Push the logical screen to the physical device.
     */
    @Override
    public void flushPhysical() {
        StringBuilder sb = new StringBuilder();
        if ((cursorVisible)
            && (cursorY >= 0)
            && (cursorX >= 0)
            && (cursorY <= height - 1)
            && (cursorX <= width - 1)
        ) {
            flushString(sb);
            sb.append(cursor(true));
            sb.append(gotoXY(cursorX, cursorY));
        } else {
            sb.append(cursor(false));
            flushString(sb);
        }
        output.write(sb.toString());
        flush();
    }

    /**
     * Resize the physical screen to match the logical screen dimensions.
     */
    @Override
    public void resizeToScreen() {
        // Send dtterm/xterm sequences, which will probably not work because
        // allowWindowOps is defaulted to false.
        String resizeString = String.format("\033[8;%d;%dt", getHeight(),
            getWidth());
        this.output.write(resizeString);
        this.output.flush();
    }

    // ------------------------------------------------------------------------
    // TerminalReader ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Check if there are events in the queue.
     *
     * @return if true, getEvents() has something to return to the backend
     */
    public boolean hasEvents() {
        synchronized (eventQueue) {
            return (eventQueue.size() > 0);
        }
    }

    /**
     * Return any events in the IO queue.
     *
     * @param queue list to append new events to
     */
    public void getEvents(final List<TInputEvent> queue) {
        synchronized (eventQueue) {
            if (eventQueue.size() > 0) {
                synchronized (queue) {
                    queue.addAll(eventQueue);
                }
                eventQueue.clear();
            }
        }
    }

    /**
     * Restore terminal to normal state.
     */
    public void closeTerminal() {

        // System.err.println("=== closeTerminal() ==="); System.err.flush();

        // Tell the reader thread to stop looking at input
        stopReaderThread = true;
        try {
            readerThread.join();
        } catch (InterruptedException e) {
            if (debugToStderr) {
                e.printStackTrace();
            }
        }

        // Disable mouse reporting and show cursor.  Defensive null check
        // here in case closeTerminal() is called twice.
        if (output != null) {
            output.printf("%s%s%s%s", mouse(false), cursor(true),
                defaultColor(), xtermResetSixelSettings());
            output.flush();
        }

        if (setRawMode) {
            sttyCooked();
            setRawMode = false;
            // We don't close System.in/out
        } else {
            // Shut down the streams, this should wake up the reader thread
            // and make it exit.
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // SQUASH
                }
                input = null;
            }
            if (output != null) {
                output.close();
                output = null;
            }
        }
    }

    /**
     * Set listener to a different Object.
     *
     * @param listener the new listening object that run() wakes up on new
     * input
     */
    public void setListener(final Object listener) {
        this.listener = listener;
    }

    /**
     * Reload options from System properties.
     */
    public void reloadOptions() {
        // Permit RGB colors only if externally requested.
        if (System.getProperty("jexer.ECMA48.rgbColor",
                "false").equals("true")
        ) {
            doRgbColor = true;
        } else {
            doRgbColor = false;
        }

        // Default to using images for full-width characters.
        if (System.getProperty("jexer.ECMA48.wideCharImages",
                "true").equals("true")) {
            wideCharImages = true;
        } else {
            wideCharImages = false;
        }

        // Pull the system properties for sixel output.
        if (System.getProperty("jexer.ECMA48.sixel", "true").equals("true")) {
            sixel = true;
        } else {
            sixel = false;
        }

        // Palette size
        int paletteSize = 1024;
        try {
            paletteSize = Integer.parseInt(System.getProperty(
                "jexer.ECMA48.sixelPaletteSize", "1024"));
            switch (paletteSize) {
            case 2:
            case 256:
            case 512:
            case 1024:
            case 2048:
                sixelPaletteSize = paletteSize;
                break;
            default:
                // Ignore value
                break;
            }
        } catch (NumberFormatException e) {
            // SQUASH
        }

        // Shared palette
        if (System.getProperty("jexer.ECMA48.sixelSharedPalette",
                "true").equals("false")) {
            sixelSharedPalette = false;
        } else {
            sixelSharedPalette = true;
        }

        // Default to not supporting iTerm2 images.
        if (System.getProperty("jexer.ECMA48.iTerm2Images",
                "false").equals("true")) {
            iterm2Images = true;
        } else {
            iterm2Images = false;
        }

        // Default to using JPG Jexer images if terminal supports it.
        String jexerImageStr = System.getProperty("jexer.ECMA48.jexerImages",
            "jpg").toLowerCase();
        if (jexerImageStr.equals("false")) {
            jexerImageOption = JexerImageOption.DISABLED;
        } else if (jexerImageStr.equals("jpg")) {
            jexerImageOption = JexerImageOption.JPG;
        } else if (jexerImageStr.equals("png")) {
            jexerImageOption = JexerImageOption.PNG;
        } else if (jexerImageStr.equals("rgb")) {
            jexerImageOption = JexerImageOption.RGB;
        }

        // Set custom colors
        setCustomSystemColors();
    }

    // ------------------------------------------------------------------------
    // Runnable ---------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Read function runs on a separate thread.
     */
    public void run() {
        boolean done = false;
        // available() will often return > 1, so we need to read in chunks to
        // stay caught up.
        char [] readBuffer = new char[128];
        List<TInputEvent> events = new ArrayList<TInputEvent>();

        while (!done && !stopReaderThread) {
            try {
                // We assume that if inputStream has bytes available, then
                // input won't block on read().
                int n = inputStream.available();

                /*
                System.err.printf("inputStream.available(): %d\n", n);
                System.err.flush();
                */

                if (n > 0) {
                    if (readBuffer.length < n) {
                        // The buffer wasn't big enough, make it huger
                        readBuffer = new char[readBuffer.length * 2];
                    }

                    // System.err.printf("BEFORE read()\n"); System.err.flush();

                    int rc = input.read(readBuffer, 0, readBuffer.length);

                    /*
                    System.err.printf("AFTER read() %d\n", rc);
                    System.err.flush();
                    */

                    if (rc == -1) {
                        // This is EOF
                        done = true;
                    } else {
                        for (int i = 0; i < rc; i++) {
                            int ch = readBuffer[i];
                            processChar(events, (char)ch);
                        }
                        getIdleEvents(events);
                        if (events.size() > 0) {
                            // Add to the queue for the backend thread to
                            // be able to obtain.
                            synchronized (eventQueue) {
                                eventQueue.addAll(events);
                            }
                            if (listener != null) {
                                synchronized (listener) {
                                    listener.notifyAll();
                                }
                            }
                            events.clear();
                        }
                    }
                } else {
                    getIdleEvents(events);
                    if (events.size() > 0) {
                        synchronized (eventQueue) {
                            eventQueue.addAll(events);
                        }
                        if (listener != null) {
                            synchronized (listener) {
                                listener.notifyAll();
                            }
                        }
                        events.clear();
                    }

                    if (output.checkError()) {
                        // This is EOF.
                        done = true;
                    }

                    // Wait 20 millis for more data
                    Thread.sleep(20);
                }
                // System.err.println("end while loop"); System.err.flush();
            } catch (InterruptedException e) {
                // SQUASH
            } catch (IOException e) {
                e.printStackTrace();
                done = true;
            }
        } // while ((done == false) && (stopReaderThread == false))

        // Pass an event up to TApplication to tell it this Backend is done.
        synchronized (eventQueue) {
            eventQueue.add(new TCommandEvent(cmBackendDisconnect));
        }
        if (listener != null) {
            synchronized (listener) {
                listener.notifyAll();
            }
        }

        // System.err.println("*** run() exiting..."); System.err.flush();
    }

    // ------------------------------------------------------------------------
    // ECMA48Terminal ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the width of a character cell in pixels.
     *
     * @return the width in pixels of a character cell
     */
    public int getTextWidth() {
        if (sessionInfo.getWindowWidth() > 0) {
            return (widthPixels / sessionInfo.getWindowWidth());
        }
        return 16;
    }

    /**
     * Get the height of a character cell in pixels.
     *
     * @return the height in pixels of a character cell
     */
    public int getTextHeight() {
        if (sessionInfo.getWindowHeight() > 0) {
            return (heightPixels / sessionInfo.getWindowHeight());
        }
        return 20;
    }

    /**
     * Getter for sessionInfo.
     *
     * @return the SessionInfo
     */
    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    /**
     * Get the output writer.
     *
     * @return the Writer
     */
    public PrintWriter getOutput() {
        return output;
    }

    /**
     * Call 'stty' to set cooked mode.
     *
     * <p>Actually executes '/bin/sh -c stty sane cooked &lt; /dev/tty'
     */
    private void sttyCooked() {
        doStty(false);
    }

    /**
     * Call 'stty' to set raw mode.
     *
     * <p>Actually executes '/bin/sh -c stty -ignbrk -brkint -parmrk -istrip
     * -inlcr -igncr -icrnl -ixon -opost -echo -echonl -icanon -isig -iexten
     * -parenb cs8 min 1 &lt; /dev/tty'
     */
    private void sttyRaw() {
        doStty(true);
    }

    /**
     * Call 'stty' to set raw or cooked mode.
     *
     * @param mode if true, set raw mode, otherwise set cooked mode
     */
    private void doStty(final boolean mode) {
        String [] cmdRaw = {
            "/bin/sh", "-c", "stty -ignbrk -brkint -parmrk -istrip -inlcr -igncr -icrnl -ixon -opost -echo -echonl -icanon -isig -iexten -parenb cs8 min 1 < /dev/tty"
        };
        String [] cmdCooked = {
            "/bin/sh", "-c", "stty sane cooked < /dev/tty"
        };
        try {
            Process process;
            if (mode) {
                process = Runtime.getRuntime().exec(cmdRaw);
            } else {
                process = Runtime.getRuntime().exec(cmdCooked);
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line = in.readLine();
            if ((line != null) && (line.length() > 0)) {
                System.err.println("WEIRD?! Normal output from stty: " + line);
            }
            while (true) {
                BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
                line = err.readLine();
                if ((line != null) && (line.length() > 0)) {
                    System.err.println("Error output from stty: " + line);
                }
                try {
                    process.waitFor();
                    break;
                } catch (InterruptedException e) {
                    if (debugToStderr) {
                        e.printStackTrace();
                    }
                }
            }
            int rc = process.exitValue();
            if (rc != 0) {
                System.err.println("stty returned error code: " + rc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Flush output.
     */
    public void flush() {
        output.flush();
    }

    /**
     * Perform a somewhat-optimal rendering of a line.
     *
     * @param y row coordinate.  0 is the top-most row.
     * @param sb StringBuilder to write escape sequences to
     * @param lastAttr cell attributes from the last call to flushLine
     */
    private void flushLine(final int y, final StringBuilder sb,
        CellAttributes lastAttr) {

        int lastX = -1;
        int textEnd = 0;
        for (int x = 0; x < width; x++) {
            Cell lCell = logical[x][y];
            if (!lCell.isBlank()) {
                textEnd = x;
            }
        }
        // Push textEnd to first column beyond the text area
        textEnd++;

        // DEBUG
        // reallyCleared = true;

        boolean hasImage = false;

        for (int x = 0; x < width; x++) {
            Cell lCell = logical[x][y];
            Cell pCell = physical[x][y];

            if (!lCell.equals(pCell) || reallyCleared) {

                if (debugToStderr) {
                    System.err.printf("\n--\n");
                    System.err.printf(" Y: %d X: %d\n", y, x);
                    System.err.printf("   lCell: %s\n", lCell);
                    System.err.printf("   pCell: %s\n", pCell);
                    System.err.printf("    ====    \n");
                }

                if (lastAttr == null) {
                    lastAttr = new CellAttributes();
                    sb.append(normal());
                }

                // Place the cell
                if ((lastX != (x - 1)) || (lastX == -1)) {
                    // Advancing at least one cell, or the first gotoXY
                    sb.append(gotoXY(x, y));
                }

                assert (lastAttr != null);

                if ((x == textEnd) && (textEnd < width - 1)) {
                    assert (lCell.isBlank());

                    for (int i = x; i < width; i++) {
                        assert (logical[i][y].isBlank());
                        // Physical is always updated
                        physical[i][y].reset();
                    }

                    // Clear remaining line
                    sb.append(clearRemainingLine());
                    lastAttr.reset();
                    return;
                }

                // Image cell: bypass the rest of the loop, it is not
                // rendered here.
                if ((wideCharImages && lCell.isImage())
                    || (!wideCharImages
                        && lCell.isImage()
                        && (lCell.getWidth() == Cell.Width.SINGLE))
                ) {
                    hasImage = true;

                    // Save the last rendered cell
                    lastX = x;

                    // Physical is always updated
                    physical[x][y].setTo(lCell);
                    continue;
                }

                assert ((wideCharImages && !lCell.isImage())
                    || (!wideCharImages
                        && (!lCell.isImage()
                            || (lCell.isImage()
                                && (lCell.getWidth() != Cell.Width.SINGLE)))));

                if (!wideCharImages && (lCell.getWidth() == Cell.Width.RIGHT)) {
                    continue;
                }

                if (hasImage) {
                    hasImage = false;
                    sb.append(gotoXY(x, y));
                }

                // Now emit only the modified attributes
                if ((lCell.getForeColor() != lastAttr.getForeColor())
                    && (lCell.getBackColor() != lastAttr.getBackColor())
                    && (!lCell.isRGB())
                    && (lCell.isBold() == lastAttr.isBold())
                    && (lCell.isReverse() == lastAttr.isReverse())
                    && (lCell.isUnderline() == lastAttr.isUnderline())
                    && (lCell.isBlink() == lastAttr.isBlink())
                ) {
                    // Both colors changed, attributes the same
                    sb.append(color(lCell.isBold(),
                            lCell.getForeColor(), lCell.getBackColor()));

                    if (debugToStderr) {
                        System.err.printf("1 Change only fore/back colors\n");
                    }

                } else if (lCell.isRGB()
                    && (lCell.getForeColorRGB() != lastAttr.getForeColorRGB())
                    && (lCell.getBackColorRGB() != lastAttr.getBackColorRGB())
                    && (lCell.isBold() == lastAttr.isBold())
                    && (lCell.isReverse() == lastAttr.isReverse())
                    && (lCell.isUnderline() == lastAttr.isUnderline())
                    && (lCell.isBlink() == lastAttr.isBlink())
                ) {
                    // Both colors changed, attributes the same
                    sb.append(colorRGB(lCell.getForeColorRGB(),
                            lCell.getBackColorRGB()));

                    if (debugToStderr) {
                        System.err.printf("1 Change only fore/back colors (RGB)\n");
                    }
                } else if ((lCell.getForeColor() != lastAttr.getForeColor())
                    && (lCell.getBackColor() != lastAttr.getBackColor())
                    && (!lCell.isRGB())
                    && (lCell.isBold() != lastAttr.isBold())
                    && (lCell.isReverse() != lastAttr.isReverse())
                    && (lCell.isUnderline() != lastAttr.isUnderline())
                    && (lCell.isBlink() != lastAttr.isBlink())
                ) {
                    // Everything is different
                    sb.append(color(lCell.getForeColor(),
                            lCell.getBackColor(),
                            lCell.isBold(), lCell.isReverse(),
                            lCell.isBlink(),
                            lCell.isUnderline()));

                    if (debugToStderr) {
                        System.err.printf("2 Set all attributes\n");
                    }
                } else if ((lCell.getForeColor() != lastAttr.getForeColor())
                    && (lCell.getBackColor() == lastAttr.getBackColor())
                    && (!lCell.isRGB())
                    && (lCell.isBold() == lastAttr.isBold())
                    && (lCell.isReverse() == lastAttr.isReverse())
                    && (lCell.isUnderline() == lastAttr.isUnderline())
                    && (lCell.isBlink() == lastAttr.isBlink())
                ) {

                    // Attributes same, foreColor different
                    sb.append(color(lCell.isBold(),
                            lCell.getForeColor(), true));

                    if (debugToStderr) {
                        System.err.printf("3 Change foreColor\n");
                    }
                } else if (lCell.isRGB()
                    && (lCell.getForeColorRGB() != lastAttr.getForeColorRGB())
                    && (lCell.getBackColorRGB() == lastAttr.getBackColorRGB())
                    && (lCell.getForeColorRGB() >= 0)
                    && (lCell.getBackColorRGB() >= 0)
                    && (lCell.isBold() == lastAttr.isBold())
                    && (lCell.isReverse() == lastAttr.isReverse())
                    && (lCell.isUnderline() == lastAttr.isUnderline())
                    && (lCell.isBlink() == lastAttr.isBlink())
                ) {
                    // Attributes same, foreColor different
                    sb.append(colorRGB(lCell.getForeColorRGB(), true));

                    if (debugToStderr) {
                        System.err.printf("3 Change foreColor (RGB)\n");
                    }
                } else if ((lCell.getForeColor() == lastAttr.getForeColor())
                    && (lCell.getBackColor() != lastAttr.getBackColor())
                    && (!lCell.isRGB())
                    && (lCell.isBold() == lastAttr.isBold())
                    && (lCell.isReverse() == lastAttr.isReverse())
                    && (lCell.isUnderline() == lastAttr.isUnderline())
                    && (lCell.isBlink() == lastAttr.isBlink())
                ) {
                    // Attributes same, backColor different
                    sb.append(color(lCell.isBold(),
                            lCell.getBackColor(), false));

                    if (debugToStderr) {
                        System.err.printf("4 Change backColor\n");
                    }
                } else if (lCell.isRGB()
                    && (lCell.getForeColorRGB() == lastAttr.getForeColorRGB())
                    && (lCell.getBackColorRGB() != lastAttr.getBackColorRGB())
                    && (lCell.isBold() == lastAttr.isBold())
                    && (lCell.isReverse() == lastAttr.isReverse())
                    && (lCell.isUnderline() == lastAttr.isUnderline())
                    && (lCell.isBlink() == lastAttr.isBlink())
                ) {
                    // Attributes same, foreColor different
                    sb.append(colorRGB(lCell.getBackColorRGB(), false));

                    if (debugToStderr) {
                        System.err.printf("4 Change backColor (RGB)\n");
                    }
                } else if ((lCell.getForeColor() == lastAttr.getForeColor())
                    && (lCell.getBackColor() == lastAttr.getBackColor())
                    && (lCell.getForeColorRGB() == lastAttr.getForeColorRGB())
                    && (lCell.getBackColorRGB() == lastAttr.getBackColorRGB())
                    && (lCell.isBold() == lastAttr.isBold())
                    && (lCell.isReverse() == lastAttr.isReverse())
                    && (lCell.isUnderline() == lastAttr.isUnderline())
                    && (lCell.isBlink() == lastAttr.isBlink())
                ) {

                    // All attributes the same, just print the char
                    // NOP

                    if (debugToStderr) {
                        System.err.printf("5 Only emit character\n");
                    }
                } else {
                    // Just reset everything again
                    if (!lCell.isRGB()) {
                        sb.append(color(lCell.getForeColor(),
                                lCell.getBackColor(),
                                lCell.isBold(),
                                lCell.isReverse(),
                                lCell.isBlink(),
                                lCell.isUnderline()));

                        if (debugToStderr) {
                            System.err.printf("6 Change all attributes\n");
                        }
                    } else {
                        sb.append(colorRGB(lCell.getForeColorRGB(),
                                lCell.getBackColorRGB(),
                                lCell.isBold(),
                                lCell.isReverse(),
                                lCell.isBlink(),
                                lCell.isUnderline()));
                        if (debugToStderr) {
                            System.err.printf("6 Change all attributes (RGB)\n");
                        }
                    }

                }
                // Emit the character
                if (wideCharImages
                    // Don't emit the right-half of full-width chars.
                    || (!wideCharImages
                        && (lCell.getWidth() != Cell.Width.RIGHT))
                ) {
                    sb.append(Character.toChars(lCell.getChar()));
                }

                // Save the last rendered cell
                lastX = x;
                lastAttr.setTo(lCell);

                // Physical is always updated
                physical[x][y].setTo(lCell);

            } // if (!lCell.equals(pCell) || (reallyCleared == true))

        } // for (int x = 0; x < width; x++)
    }

    /**
     * Render the screen to a string that can be emitted to something that
     * knows how to process ECMA-48/ANSI X3.64 escape sequences.
     *
     * @param sb StringBuilder to write escape sequences to
     * @return escape sequences string that provides the updates to the
     * physical screen
     */
    private String flushString(final StringBuilder sb) {
        CellAttributes attr = null;

        if (reallyCleared) {
            attr = new CellAttributes();
            sb.append(clearAll());
        }

        /*
         * For images support, draw all of the image output first, and then
         * draw everything else afterwards.  This works OK, but performance
         * is still a drag on larger pictures.
         */
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // If physical had non-image data that is now image data, the
                // entire row must be redrawn.
                Cell lCell = logical[x][y];
                Cell pCell = physical[x][y];
                if (lCell.isImage() && !pCell.isImage()) {
                    unsetImageRow(y);
                    break;
                }
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell lCell = logical[x][y];
                Cell pCell = physical[x][y];

                if (!lCell.isImage()
                    || (!wideCharImages
                        && (lCell.getWidth() != Cell.Width.SINGLE))
                ) {
                    continue;
                }

                int left = x;
                int right = x;
                while ((right < width)
                    && (logical[right][y].isImage())
                    && (!logical[right][y].equals(physical[right][y])
                        || reallyCleared)
                ) {
                    right++;
                }
                ArrayList<Cell> cellsToDraw = new ArrayList<Cell>();
                for (int i = 0; i < (right - x); i++) {
                    assert (logical[x + i][y].isImage());
                    cellsToDraw.add(logical[x + i][y]);

                    // Physical is always updated.
                    physical[x + i][y].setTo(lCell);
                }
                if (cellsToDraw.size() > 0) {
                    if (iterm2Images) {
                        sb.append(toIterm2Image(x, y, cellsToDraw));
                    } else if (jexerImageOption != JexerImageOption.DISABLED) {
                        sb.append(toJexerImage(x, y, cellsToDraw));
                    } else {
                        sb.append(toSixel(x, y, cellsToDraw));
                    }
                }

                x = right;
            }
        }

        // Draw the text part now.
        for (int y = 0; y < height; y++) {
            flushLine(y, sb, attr);
        }

        reallyCleared = false;

        String result = sb.toString();
        if (debugToStderr) {
            System.err.printf("flushString(): %s\n", result);
        }
        return result;
    }

    /**
     * Reset keyboard/mouse input parser.
     */
    private void resetParser() {
        state = ParseState.GROUND;
        params = new ArrayList<String>();
        params.clear();
        params.add("");
        decPrivateModeFlag = false;
    }

    /**
     * Produce a control character or one of the special ones (ENTER, TAB,
     * etc.).
     *
     * @param ch Unicode code point
     * @param alt if true, set alt on the TKeypress
     * @return one TKeypress event, either a control character (e.g. isKey ==
     * false, ch == 'A', ctrl == true), or a special key (e.g. isKey == true,
     * fnKey == ESC)
     */
    private TKeypressEvent controlChar(final char ch, final boolean alt) {
        // System.err.printf("controlChar: %02x\n", ch);

        switch (ch) {
        case 0x0D:
            // Carriage return --> ENTER
            return new TKeypressEvent(kbEnter, alt, false, false);
        case 0x0A:
            // Linefeed --> ENTER
            return new TKeypressEvent(kbEnter, alt, false, false);
        case 0x1B:
            // ESC
            return new TKeypressEvent(kbEsc, alt, false, false);
        case '\t':
            // TAB
            return new TKeypressEvent(kbTab, alt, false, false);
        default:
            // Make all other control characters come back as the alphabetic
            // character with the ctrl field set.  So SOH would be 'A' +
            // ctrl.
            return new TKeypressEvent(false, 0, (char)(ch + 0x40),
                alt, true, false);
        }
    }

    /**
     * Produce special key from CSI Pn ; Pm ; ... ~
     *
     * @return one KEYPRESS event representing a special key
     */
    private TInputEvent csiFnKey() {
        int key = 0;
        if (params.size() > 0) {
            key = Integer.parseInt(params.get(0));
        }
        boolean alt = false;
        boolean ctrl = false;
        boolean shift = false;
        if (params.size() > 1) {
            shift = csiIsShift(params.get(1));
            alt = csiIsAlt(params.get(1));
            ctrl = csiIsCtrl(params.get(1));
        }

        switch (key) {
        case 1:
            return new TKeypressEvent(kbHome, alt, ctrl, shift);
        case 2:
            return new TKeypressEvent(kbIns, alt, ctrl, shift);
        case 3:
            return new TKeypressEvent(kbDel, alt, ctrl, shift);
        case 4:
            return new TKeypressEvent(kbEnd, alt, ctrl, shift);
        case 5:
            return new TKeypressEvent(kbPgUp, alt, ctrl, shift);
        case 6:
            return new TKeypressEvent(kbPgDn, alt, ctrl, shift);
        case 15:
            return new TKeypressEvent(kbF5, alt, ctrl, shift);
        case 17:
            return new TKeypressEvent(kbF6, alt, ctrl, shift);
        case 18:
            return new TKeypressEvent(kbF7, alt, ctrl, shift);
        case 19:
            return new TKeypressEvent(kbF8, alt, ctrl, shift);
        case 20:
            return new TKeypressEvent(kbF9, alt, ctrl, shift);
        case 21:
            return new TKeypressEvent(kbF10, alt, ctrl, shift);
        case 23:
            return new TKeypressEvent(kbF11, alt, ctrl, shift);
        case 24:
            return new TKeypressEvent(kbF12, alt, ctrl, shift);
        default:
            // Unknown
            return null;
        }
    }

    /**
     * Produce mouse events based on "Any event tracking" and UTF-8
     * coordinates.  See
     * http://invisible-island.net/xterm/ctlseqs/ctlseqs.html#Mouse%20Tracking
     *
     * @return a MOUSE_MOTION, MOUSE_UP, or MOUSE_DOWN event
     */
    private TInputEvent parseMouse() {
        int buttons = params.get(0).charAt(0) - 32;
        int x = params.get(0).charAt(1) - 32 - 1;
        int y = params.get(0).charAt(2) - 32 - 1;

        // Clamp X and Y to the physical screen coordinates.
        if (x >= windowResize.getWidth()) {
            x = windowResize.getWidth() - 1;
        }
        if (y >= windowResize.getHeight()) {
            y = windowResize.getHeight() - 1;
        }

        TMouseEvent.Type eventType = TMouseEvent.Type.MOUSE_DOWN;
        boolean eventMouse1 = false;
        boolean eventMouse2 = false;
        boolean eventMouse3 = false;
        boolean eventMouseWheelUp = false;
        boolean eventMouseWheelDown = false;
        boolean eventAlt = false;
        boolean eventCtrl = false;
        boolean eventShift = false;

        // System.err.printf("buttons: %04x\r\n", buttons);

        switch (buttons & 0xE3) {
        case 0:
            eventMouse1 = true;
            mouse1 = true;
            break;
        case 1:
            eventMouse2 = true;
            mouse2 = true;
            break;
        case 2:
            eventMouse3 = true;
            mouse3 = true;
            break;
        case 3:
            // Release or Move
            if (!mouse1 && !mouse2 && !mouse3) {
                eventType = TMouseEvent.Type.MOUSE_MOTION;
            } else {
                eventType = TMouseEvent.Type.MOUSE_UP;
            }
            if (mouse1) {
                mouse1 = false;
                eventMouse1 = true;
            }
            if (mouse2) {
                mouse2 = false;
                eventMouse2 = true;
            }
            if (mouse3) {
                mouse3 = false;
                eventMouse3 = true;
            }
            break;

        case 32:
            // Dragging with mouse1 down
            eventMouse1 = true;
            mouse1 = true;
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;

        case 33:
            // Dragging with mouse2 down
            eventMouse2 = true;
            mouse2 = true;
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;

        case 34:
            // Dragging with mouse3 down
            eventMouse3 = true;
            mouse3 = true;
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;

        case 96:
            // Dragging with mouse2 down after wheelUp
            eventMouse2 = true;
            mouse2 = true;
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;

        case 97:
            // Dragging with mouse2 down after wheelDown
            eventMouse2 = true;
            mouse2 = true;
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;

        case 64:
            eventMouseWheelUp = true;
            break;

        case 65:
            eventMouseWheelDown = true;
            break;

        default:
            // Unknown, just make it motion
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;
        }

        if ((buttons & 0x04) != 0) {
            eventShift = true;
        }
        if ((buttons & 0x08) != 0) {
            eventAlt = true;
        }
        if ((buttons & 0x10) != 0) {
            eventCtrl = true;
        }

        return new TMouseEvent(eventType, x, y, x, y,
            eventMouse1, eventMouse2, eventMouse3,
            eventMouseWheelUp, eventMouseWheelDown,
            eventAlt, eventCtrl, eventShift);
    }

    /**
     * Produce mouse events based on "Any event tracking" and SGR
     * coordinates.  See
     * http://invisible-island.net/xterm/ctlseqs/ctlseqs.html#Mouse%20Tracking
     *
     * @param release if true, this was a release ('m')
     * @return a MOUSE_MOTION, MOUSE_UP, or MOUSE_DOWN event
     */
    private TInputEvent parseMouseSGR(final boolean release) {
        // SGR extended coordinates - mode 1006
        if (params.size() < 3) {
            // Invalid position, bail out.
            return null;
        }
        int buttons = Integer.parseInt(params.get(0));
        int x = Integer.parseInt(params.get(1)) - 1;
        int y = Integer.parseInt(params.get(2)) - 1;

        // Clamp X and Y to the physical screen coordinates.
        if (x >= windowResize.getWidth()) {
            x = windowResize.getWidth() - 1;
        }
        if (y >= windowResize.getHeight()) {
            y = windowResize.getHeight() - 1;
        }

        TMouseEvent.Type eventType = TMouseEvent.Type.MOUSE_DOWN;
        boolean eventMouse1 = false;
        boolean eventMouse2 = false;
        boolean eventMouse3 = false;
        boolean eventMouseWheelUp = false;
        boolean eventMouseWheelDown = false;
        boolean eventAlt = false;
        boolean eventCtrl = false;
        boolean eventShift = false;

        if (release) {
            eventType = TMouseEvent.Type.MOUSE_UP;
        }

        switch (buttons & 0xE3) {
        case 0:
            eventMouse1 = true;
            break;
        case 1:
            eventMouse2 = true;
            break;
        case 2:
            eventMouse3 = true;
            break;
        case 35:
            // Motion only, no buttons down
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;

        case 32:
            // Dragging with mouse1 down
            eventMouse1 = true;
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;

        case 33:
            // Dragging with mouse2 down
            eventMouse2 = true;
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;

        case 34:
            // Dragging with mouse3 down
            eventMouse3 = true;
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;

        case 96:
            // Dragging with mouse2 down after wheelUp
            eventMouse2 = true;
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;

        case 97:
            // Dragging with mouse2 down after wheelDown
            eventMouse2 = true;
            eventType = TMouseEvent.Type.MOUSE_MOTION;
            break;

        case 64:
            eventMouseWheelUp = true;
            break;

        case 65:
            eventMouseWheelDown = true;
            break;

        default:
            // Unknown, bail out
            return null;
        }

        if ((buttons & 0x04) != 0) {
            eventShift = true;
        }
        if ((buttons & 0x08) != 0) {
            eventAlt = true;
        }
        if ((buttons & 0x10) != 0) {
            eventCtrl = true;
        }

        return new TMouseEvent(eventType, x, y, x, y,
            eventMouse1, eventMouse2, eventMouse3,
            eventMouseWheelUp, eventMouseWheelDown,
            eventAlt, eventCtrl, eventShift);
    }

    /**
     * Return any events in the IO queue due to timeout.
     *
     * @param queue list to append new events to
     */
    private void getIdleEvents(final List<TInputEvent> queue) {
        long nowTime = System.currentTimeMillis();

        // Check for new window size
        long windowSizeDelay = nowTime - windowSizeTime;
        if (windowSizeDelay > 1000) {
            int oldTextWidth = getTextWidth();
            int oldTextHeight = getTextHeight();

            sessionInfo.queryWindowSize();
            int newWidth = sessionInfo.getWindowWidth();
            int newHeight = sessionInfo.getWindowHeight();

            if ((newWidth != windowResize.getWidth())
                || (newHeight != windowResize.getHeight())
            ) {

                // Request xterm report window dimensions in pixels again.
                // Between now and then, ensure that the reported text cell
                // size is the same by setting widthPixels and heightPixels
                // to match the new dimensions.
                widthPixels = oldTextWidth * newWidth;
                heightPixels = oldTextHeight * newHeight;

                if (debugToStderr) {
                    System.err.println("Screen size changed, old size " +
                        windowResize);
                    System.err.println("                     new size " +
                        newWidth + " x " + newHeight);
                    System.err.println("                   old pixels " +
                        oldTextWidth + " x " + oldTextHeight);
                    System.err.println("                   new pixels " +
                        getTextWidth() + " x " + getTextHeight());
                }

                this.output.printf("%s", xtermReportPixelDimensions());
                this.output.flush();

                TResizeEvent event = new TResizeEvent(TResizeEvent.Type.SCREEN,
                    newWidth, newHeight);
                windowResize = new TResizeEvent(TResizeEvent.Type.SCREEN,
                    newWidth, newHeight);
                queue.add(event);
            }
            windowSizeTime = nowTime;
        }

        // ESCDELAY type timeout
        if (state == ParseState.ESCAPE) {
            long escDelay = nowTime - escapeTime;
            if (escDelay > 100) {
                // After 0.1 seconds, assume a true escape character
                queue.add(controlChar((char)0x1B, false));
                resetParser();
            }
        }
    }

    /**
     * Returns true if the CSI parameter for a keyboard command means that
     * shift was down.
     */
    private boolean csiIsShift(final String x) {
        if ((x.equals("2"))
            || (x.equals("4"))
            || (x.equals("6"))
            || (x.equals("8"))
        ) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the CSI parameter for a keyboard command means that
     * alt was down.
     */
    private boolean csiIsAlt(final String x) {
        if ((x.equals("3"))
            || (x.equals("4"))
            || (x.equals("7"))
            || (x.equals("8"))
        ) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the CSI parameter for a keyboard command means that
     * ctrl was down.
     */
    private boolean csiIsCtrl(final String x) {
        if ((x.equals("5"))
            || (x.equals("6"))
            || (x.equals("7"))
            || (x.equals("8"))
        ) {
            return true;
        }
        return false;
    }

    /**
     * Parses the next character of input to see if an InputEvent is
     * fully here.
     *
     * @param events list to append new events to
     * @param ch Unicode code point
     */
    private void processChar(final List<TInputEvent> events, final char ch) {

        // ESCDELAY type timeout
        long nowTime = System.currentTimeMillis();
        if (state == ParseState.ESCAPE) {
            long escDelay = nowTime - escapeTime;
            if (escDelay > 250) {
                // After 0.25 seconds, assume a true escape character
                events.add(controlChar((char)0x1B, false));
                resetParser();
            }
        }

        // TKeypress fields
        boolean ctrl = false;
        boolean alt = false;
        boolean shift = false;

        // System.err.printf("state: %s ch %c\r\n", state, ch);

        switch (state) {
        case GROUND:

            if (ch == 0x1B) {
                state = ParseState.ESCAPE;
                escapeTime = nowTime;
                return;
            }

            if (ch <= 0x1F) {
                // Control character
                events.add(controlChar(ch, false));
                resetParser();
                return;
            }

            if (ch >= 0x20) {
                // Normal character
                events.add(new TKeypressEvent(false, 0, ch,
                        false, false, false));
                resetParser();
                return;
            }

            break;

        case ESCAPE:
            if (ch <= 0x1F) {
                // ALT-Control character
                events.add(controlChar(ch, true));
                resetParser();
                return;
            }

            if (ch == 'O') {
                // This will be one of the function keys
                state = ParseState.ESCAPE_INTERMEDIATE;
                return;
            }

            // '[' goes to CSI_ENTRY
            if (ch == '[') {
                state = ParseState.CSI_ENTRY;
                return;
            }

            // Everything else is assumed to be Alt-keystroke
            if ((ch >= 'A') && (ch <= 'Z')) {
                shift = true;
            }
            alt = true;
            events.add(new TKeypressEvent(false, 0, ch, alt, ctrl, shift));
            resetParser();
            return;

        case ESCAPE_INTERMEDIATE:
            if ((ch >= 'P') && (ch <= 'S')) {
                // Function key
                switch (ch) {
                case 'P':
                    events.add(new TKeypressEvent(kbF1));
                    break;
                case 'Q':
                    events.add(new TKeypressEvent(kbF2));
                    break;
                case 'R':
                    events.add(new TKeypressEvent(kbF3));
                    break;
                case 'S':
                    events.add(new TKeypressEvent(kbF4));
                    break;
                default:
                    break;
                }
                resetParser();
                return;
            }

            // Unknown keystroke, ignore
            resetParser();
            return;

        case CSI_ENTRY:
            // Numbers - parameter values
            if ((ch >= '0') && (ch <= '9')) {
                params.set(params.size() - 1,
                    params.get(params.size() - 1) + ch);
                state = ParseState.CSI_PARAM;
                return;
            }
            // Parameter separator
            if (ch == ';') {
                params.add("");
                return;
            }

            if ((ch >= 0x30) && (ch <= 0x7E)) {
                switch (ch) {
                case 'A':
                    // Up
                    events.add(new TKeypressEvent(kbUp, alt, ctrl, shift));
                    resetParser();
                    return;
                case 'B':
                    // Down
                    events.add(new TKeypressEvent(kbDown, alt, ctrl, shift));
                    resetParser();
                    return;
                case 'C':
                    // Right
                    events.add(new TKeypressEvent(kbRight, alt, ctrl, shift));
                    resetParser();
                    return;
                case 'D':
                    // Left
                    events.add(new TKeypressEvent(kbLeft, alt, ctrl, shift));
                    resetParser();
                    return;
                case 'H':
                    // Home
                    events.add(new TKeypressEvent(kbHome));
                    resetParser();
                    return;
                case 'F':
                    // End
                    events.add(new TKeypressEvent(kbEnd));
                    resetParser();
                    return;
                case 'Z':
                    // CBT - Cursor backward X tab stops (default 1)
                    events.add(new TKeypressEvent(kbBackTab));
                    resetParser();
                    return;
                case 'M':
                    // Mouse position
                    state = ParseState.MOUSE;
                    return;
                case '<':
                    // Mouse position, SGR (1006) coordinates
                    state = ParseState.MOUSE_SGR;
                    return;
                case '?':
                    // DEC private mode flag
                    decPrivateModeFlag = true;
                    return;
                default:
                    break;
                }
            }

            // Unknown keystroke, ignore
            resetParser();
            return;

        case MOUSE_SGR:
            // Numbers - parameter values
            if ((ch >= '0') && (ch <= '9')) {
                params.set(params.size() - 1,
                    params.get(params.size() - 1) + ch);
                return;
            }
            // Parameter separator
            if (ch == ';') {
                params.add("");
                return;
            }

            switch (ch) {
            case 'M':
                // Generate a mouse press event
                TInputEvent event = parseMouseSGR(false);
                if (event != null) {
                    events.add(event);
                }
                resetParser();
                return;
            case 'm':
                // Generate a mouse release event
                event = parseMouseSGR(true);
                if (event != null) {
                    events.add(event);
                }
                resetParser();
                return;
            default:
                break;
            }

            // Unknown keystroke, ignore
            resetParser();
            return;

        case CSI_PARAM:
            // Numbers - parameter values
            if ((ch >= '0') && (ch <= '9')) {
                params.set(params.size() - 1,
                    params.get(params.size() - 1) + ch);
                state = ParseState.CSI_PARAM;
                return;
            }
            // Parameter separator
            if (ch == ';') {
                params.add("");
                return;
            }

            if (ch == '~') {
                events.add(csiFnKey());
                resetParser();
                return;
            }

            if ((ch >= 0x30) && (ch <= 0x7E)) {
                switch (ch) {
                case 'A':
                    // Up
                    if (params.size() > 1) {
                        shift = csiIsShift(params.get(1));
                        alt = csiIsAlt(params.get(1));
                        ctrl = csiIsCtrl(params.get(1));
                    }
                    events.add(new TKeypressEvent(kbUp, alt, ctrl, shift));
                    resetParser();
                    return;
                case 'B':
                    // Down
                    if (params.size() > 1) {
                        shift = csiIsShift(params.get(1));
                        alt = csiIsAlt(params.get(1));
                        ctrl = csiIsCtrl(params.get(1));
                    }
                    events.add(new TKeypressEvent(kbDown, alt, ctrl, shift));
                    resetParser();
                    return;
                case 'C':
                    // Right
                    if (params.size() > 1) {
                        shift = csiIsShift(params.get(1));
                        alt = csiIsAlt(params.get(1));
                        ctrl = csiIsCtrl(params.get(1));
                    }
                    events.add(new TKeypressEvent(kbRight, alt, ctrl, shift));
                    resetParser();
                    return;
                case 'D':
                    // Left
                    if (params.size() > 1) {
                        shift = csiIsShift(params.get(1));
                        alt = csiIsAlt(params.get(1));
                        ctrl = csiIsCtrl(params.get(1));
                    }
                    events.add(new TKeypressEvent(kbLeft, alt, ctrl, shift));
                    resetParser();
                    return;
                case 'H':
                    // Home
                    if (params.size() > 1) {
                        shift = csiIsShift(params.get(1));
                        alt = csiIsAlt(params.get(1));
                        ctrl = csiIsCtrl(params.get(1));
                    }
                    events.add(new TKeypressEvent(kbHome, alt, ctrl, shift));
                    resetParser();
                    return;
                case 'F':
                    // End
                    if (params.size() > 1) {
                        shift = csiIsShift(params.get(1));
                        alt = csiIsAlt(params.get(1));
                        ctrl = csiIsCtrl(params.get(1));
                    }
                    events.add(new TKeypressEvent(kbEnd, alt, ctrl, shift));
                    resetParser();
                    return;
                case 'c':
                    // Device Attributes
                    if (decPrivateModeFlag == false) {
                        break;
                    }
                    boolean reportsJexerImages = false;
                    boolean reportsIterm2Images = false;
                    for (String x: params) {
                        if (x.equals("4")) {
                            // Terminal reports sixel support
                            if (debugToStderr) {
                                System.err.println("Device Attributes: sixel");
                            }
                        }
                        if (x.equals("444")) {
                            // Terminal reports Jexer images support
                            if (debugToStderr) {
                                System.err.println("Device Attributes: Jexer images");
                            }
                            reportsJexerImages = true;
                        }
                        if (x.equals("1337")) {
                            // Terminal reports iTerm2 images support
                            if (debugToStderr) {
                                System.err.println("Device Attributes: iTerm2 images");
                            }
                            reportsIterm2Images = true;
                        }
                    }
                    if (reportsJexerImages == false) {
                        // Terminal does not support Jexer images, disable
                        // them.
                        jexerImageOption = JexerImageOption.DISABLED;
                    }
                    if (reportsIterm2Images == false) {
                        // Terminal does not support iTerm2 images, disable
                        // them.
                        iterm2Images = false;
                    }
                    resetParser();
                    return;
                case 't':
                    // windowOps
                    if ((params.size() > 2) && (params.get(0).equals("4"))) {
                        if (debugToStderr) {
                            System.err.printf("windowOp pixels: " +
                                "height %s width %s\n",
                                params.get(1), params.get(2));
                        }
                        try {
                            widthPixels = Integer.parseInt(params.get(2));
                            heightPixels = Integer.parseInt(params.get(1));
                        } catch (NumberFormatException e) {
                            if (debugToStderr) {
                                e.printStackTrace();
                            }
                        }
                        if (widthPixels <= 0) {
                            widthPixels = 640;
                        }
                        if (heightPixels <= 0) {
                            heightPixels = 400;
                        }
                    }
                    if ((params.size() > 2) && (params.get(0).equals("6"))) {
                        if (debugToStderr) {
                            System.err.printf("windowOp text cell pixels: " +
                                "height %s width %s\n",
                                params.get(1), params.get(2));
                        }
                        try {
                            widthPixels = width * Integer.parseInt(params.get(2));
                            heightPixels = height * Integer.parseInt(params.get(1));
                        } catch (NumberFormatException e) {
                            if (debugToStderr) {
                                e.printStackTrace();
                            }
                        }
                        if (widthPixels <= 0) {
                            widthPixels = 640;
                        }
                        if (heightPixels <= 0) {
                            heightPixels = 400;
                        }
                    }
                    resetParser();
                    return;
                default:
                    break;
                }
            }

            // Unknown keystroke, ignore
            resetParser();
            return;

        case MOUSE:
            params.set(0, params.get(params.size() - 1) + ch);
            if (params.get(0).length() == 3) {
                // We have enough to generate a mouse event
                events.add(parseMouse());
                resetParser();
            }
            return;

        default:
            break;
        }

        // This "should" be impossible to reach
        return;
    }

    /**
     * Request (u)xterm to use the sixel settings we need:
     *
     *   - enable sixel scrolling
     *
     *   - disable private color registers (so that we can use one common
     *     palette) if sixelSharedPalette is set
     *
     * @return the string to emit to xterm
     */
    private String xtermSetSixelSettings() {
        if (sixelSharedPalette == true) {
            return "\033[?80h\033[?1070l";
        } else {
            return "\033[?80h\033[?1070h";
        }
    }

    /**
     * Restore (u)xterm its default sixel settings:
     *
     *   - enable sixel scrolling
     *
     *   - enable private color registers
     *
     * @return the string to emit to xterm
     */
    private String xtermResetSixelSettings() {
        return "\033[?80h\033[?1070h";
    }

    /**
     * Request (u)xterm to report the current window and cell size dimensions
     * in pixels.
     *
     * @return the string to emit to xterm
     */
    private String xtermReportPixelDimensions() {
        // We will ask for both window and text cell dimensions, and
        // hopefully one of them will work.
        return "\033[14t\033[16t";
    }

    /**
     * Tell (u)xterm that we want alt- keystrokes to send escape + character
     * rather than set the 8th bit.  Anyone who wants UTF8 should want this
     * enabled.
     *
     * @param on if true, enable metaSendsEscape
     * @return the string to emit to xterm
     */
    private String xtermMetaSendsEscape(final boolean on) {
        if (on) {
            return "\033[?1036h\033[?1034l";
        }
        return "\033[?1036l";
    }

    /**
     * Create an xterm OSC sequence to change the window title.
     *
     * @param title the new title
     * @return the string to emit to xterm
     */
    private String getSetTitleString(final String title) {
        return "\033]2;" + title + "\007";
    }

    // ------------------------------------------------------------------------
    // Sixel output support ---------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the number of colors in the sixel palette.
     *
     * @return the palette size
     */
    public int getSixelPaletteSize() {
        return sixelPaletteSize;
    }

    /**
     * Set the number of colors in the sixel palette.
     *
     * @param paletteSize the new palette size
     */
    public void setSixelPaletteSize(final int paletteSize) {
        if (paletteSize == sixelPaletteSize) {
            return;
        }

        switch (paletteSize) {
        case 2:
        case 256:
        case 512:
        case 1024:
        case 2048:
            break;
        default:
            throw new IllegalArgumentException("Unsupported sixel palette " +
                " size: " + paletteSize);
        }

        // Don't step on the screen refresh thread.
        synchronized (this) {
            sixelPaletteSize = paletteSize;
            palette = null;
            sixelCache = null;
            clearPhysical();
        }
    }

    /**
     * Start a sixel string for display one row's worth of bitmap data.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @return the string to emit to an ANSI / ECMA-style terminal
     */
    private String startSixel(final int x, final int y) {
        StringBuilder sb = new StringBuilder();

        assert (sixel == true);

        // Place the cursor
        sb.append(gotoXY(x, y));

        // DCS
        sb.append("\033Pq");

        if (palette == null) {
            palette = new SixelPalette();
            if (sixelSharedPalette == true) {
                palette.emitPalette(sb, null);
            }
        }

        return sb.toString();
    }

    /**
     * End a sixel string for display one row's worth of bitmap data.
     *
     * @return the string to emit to an ANSI / ECMA-style terminal
     */
    private String endSixel() {
        assert (sixel == true);

        // ST
        return ("\033\\");
    }

    /**
     * Create a sixel string representing a row of several cells containing
     * bitmap data.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param cells the cells containing the bitmap data
     * @return the string to emit to an ANSI / ECMA-style terminal
     */
    private String toSixel(final int x, final int y,
        final ArrayList<Cell> cells) {

        StringBuilder sb = new StringBuilder();

        assert (cells != null);
        assert (cells.size() > 0);
        assert (cells.get(0).getImage() != null);

        if (sixel == false) {
            sb.append(normal());
            sb.append(gotoXY(x, y));
            for (int i = 0; i < cells.size(); i++) {
                sb.append(' ');
            }
            return sb.toString();
        }

        if (y == height - 1) {
            // We are on the bottom row.  If scrolling mode is enabled
            // (default), then VT320/xterm will scroll the entire screen if
            // we draw any pixels here.  Do not draw the image, bail out
            // instead.
            sb.append(normal());
            sb.append(gotoXY(x, y));
            for (int j = 0; j < cells.size(); j++) {
                sb.append(' ');
            }
            return sb.toString();
        }

        if (sixelCache == null) {
            sixelCache = new ImageCache(height * 10);
        }

        // Save and get rows to/from the cache that do NOT have inverted
        // cells.
        boolean saveInCache = true;
        for (Cell cell: cells) {
            if (cell.isInvertedImage()) {
                saveInCache = false;
            }
        }
        if (saveInCache) {
            String cachedResult = sixelCache.get(cells);
            if (cachedResult != null) {
                // System.err.println("CACHE HIT");
                sb.append(startSixel(x, y));
                sb.append(cachedResult);
                sb.append(endSixel());
                return sb.toString();
            }
            // System.err.println("CACHE MISS");
        }

        BufferedImage image = cellsToImage(cells);
        int fullHeight = image.getHeight();

        // Dither the image.  It is ok to lose the original here.
        if (palette == null) {
            palette = new SixelPalette();
            if (sixelSharedPalette == true) {
                palette.emitPalette(sb, null);
            }
        }
        image = palette.ditherImage(image);

        // Collect the raster information
        int rasterHeight = 0;
        int rasterWidth = image.getWidth();

        if (sixelSharedPalette == false) {
            // Emit the palette, but only for the colors actually used by
            // these cells.
            boolean [] usedColors = new boolean[sixelPaletteSize];
            for (int imageX = 0; imageX < image.getWidth(); imageX++) {
                for (int imageY = 0; imageY < image.getHeight(); imageY++) {
                    usedColors[image.getRGB(imageX, imageY)] = true;
                }
            }
            palette.emitPalette(sb, usedColors);
        }

        // Render the entire row of cells.
        for (int currentRow = 0; currentRow < fullHeight; currentRow += 6) {
            int [][] sixels = new int[image.getWidth()][6];

            // See which colors are actually used in this band of sixels.
            for (int imageX = 0; imageX < image.getWidth(); imageX++) {
                for (int imageY = 0;
                     (imageY < 6) && (imageY + currentRow < fullHeight);
                     imageY++) {

                    int colorIdx = image.getRGB(imageX, imageY + currentRow);
                    assert (colorIdx >= 0);
                    assert (colorIdx < sixelPaletteSize);

                    sixels[imageX][imageY] = colorIdx;
                }
            }

            for (int i = 0; i < sixelPaletteSize; i++) {
                boolean isUsed = false;
                for (int imageX = 0; imageX < image.getWidth(); imageX++) {
                    for (int j = 0; j < 6; j++) {
                        if (sixels[imageX][j] == i) {
                            isUsed = true;
                        }
                    }
                }
                if (isUsed == false) {
                    continue;
                }

                // Set to the beginning of scan line for the next set of
                // colored pixels, and select the color.
                sb.append(String.format("$#%d", i));

                int oldData = -1;
                int oldDataCount = 0;
                for (int imageX = 0; imageX < image.getWidth(); imageX++) {

                    // Add up all the pixels that match this color.
                    int data = 0;
                    for (int j = 0;
                         (j < 6) && (currentRow + j < fullHeight);
                         j++) {

                        if (sixels[imageX][j] == i) {
                            switch (j) {
                            case 0:
                                data += 1;
                                break;
                            case 1:
                                data += 2;
                                break;
                            case 2:
                                data += 4;
                                break;
                            case 3:
                                data += 8;
                                break;
                            case 4:
                                data += 16;
                                break;
                            case 5:
                                data += 32;
                                break;
                            }
                            if ((currentRow + j + 1) > rasterHeight) {
                                rasterHeight = currentRow + j + 1;
                            }
                        }
                    }
                    assert (data >= 0);
                    assert (data < 64);
                    data += 63;

                    if (data == oldData) {
                        oldDataCount++;
                    } else {
                        if (oldDataCount == 1) {
                            sb.append((char) oldData);
                        } else if (oldDataCount > 1) {
                            sb.append(String.format("!%d", oldDataCount));
                            sb.append((char) oldData);
                        }
                        oldDataCount = 1;
                        oldData = data;
                    }

                } // for (int imageX = 0; imageX < image.getWidth(); imageX++)

                // Emit the last sequence.
                if (oldDataCount == 1) {
                    sb.append((char) oldData);
                } else if (oldDataCount > 1) {
                    sb.append(String.format("!%d", oldDataCount));
                    sb.append((char) oldData);
                }

            } // for (int i = 0; i < sixelPaletteSize; i++)

            // Advance to the next scan line.
            sb.append("-");

        } // for (int currentRow = 0; currentRow < imageHeight; currentRow += 6)

        // Kill the very last "-", because it is unnecessary.
        sb.deleteCharAt(sb.length() - 1);

        // Add the raster information
        sb.insert(0, String.format("\"1;1;%d;%d", rasterWidth, rasterHeight));

        if (saveInCache) {
            // This row is OK to save into the cache.
            sixelCache.put(cells, sb.toString());
        }

        return (startSixel(x, y) + sb.toString() + endSixel());
    }

    /**
     * Get the sixel support flag.
     *
     * @return true if this terminal is emitting sixel
     */
    public boolean hasSixel() {
        return sixel;
    }

    /**
     * Convert a horizontal range of cell's image data into a single
     * contigous image, rescaled and anti-aliased to match the current text
     * cell size.
     *
     * @param cells the cells containing image data
     * @return the image resized to the current text cell size
     */
    private BufferedImage cellsToImage(final List<Cell> cells) {
        int imageWidth = cells.get(0).getImage().getWidth();
        int imageHeight = cells.get(0).getImage().getHeight();

        // Piece cells.get(x).getImage() pieces together into one larger
        // image for final rendering.
        int totalWidth = 0;
        int fullWidth = cells.size() * imageWidth;
        int fullHeight = imageHeight;
        for (int i = 0; i < cells.size(); i++) {
            totalWidth += cells.get(i).getImage().getWidth();
        }

        BufferedImage image = new BufferedImage(fullWidth,
            fullHeight, BufferedImage.TYPE_INT_ARGB);

        int [] rgbArray;
        for (int i = 0; i < cells.size() - 1; i++) {
            int tileWidth = imageWidth;
            int tileHeight = imageHeight;

            if (false && cells.get(i).isInvertedImage()) {
                // I used to put an all-white cell over the cursor, don't do
                // that anymore.
                rgbArray = new int[imageWidth * imageHeight];
                for (int j = 0; j < rgbArray.length; j++) {
                    rgbArray[j] = 0xFFFFFF;
                }
            } else {
                try {
                    rgbArray = cells.get(i).getImage().getRGB(0, 0,
                        tileWidth, tileHeight, null, 0, tileWidth);
                } catch (Exception e) {
                    throw new RuntimeException("image " + imageWidth + "x" +
                        imageHeight +
                        "tile " + tileWidth + "x" +
                        tileHeight +
                        " cells.get(i).getImage() " +
                        cells.get(i).getImage() +
                        " i " + i +
                        " fullWidth " + fullWidth +
                        " fullHeight " + fullHeight, e);
                }
            }

            /*
            System.err.printf("calling image.setRGB(): %d %d %d %d %d\n",
                i * imageWidth, 0, imageWidth, imageHeight,
                0, imageWidth);
            System.err.printf("   fullWidth %d fullHeight %d cells.size() %d textWidth %d\n",
                fullWidth, fullHeight, cells.size(), getTextWidth());
             */

            image.setRGB(i * imageWidth, 0, tileWidth, tileHeight,
                rgbArray, 0, tileWidth);
            if (tileHeight < fullHeight) {
                int backgroundColor = cells.get(i).getBackground().getRGB();
                for (int imageX = 0; imageX < image.getWidth(); imageX++) {
                    for (int imageY = imageHeight; imageY < fullHeight;
                         imageY++) {

                        image.setRGB(imageX, imageY, backgroundColor);
                    }
                }
            }
        }
        totalWidth -= ((cells.size() - 1) * imageWidth);
        if (false && cells.get(cells.size() - 1).isInvertedImage()) {
            // I used to put an all-white cell over the cursor, don't do that
            // anymore.
            rgbArray = new int[totalWidth * imageHeight];
            for (int j = 0; j < rgbArray.length; j++) {
                rgbArray[j] = 0xFFFFFF;
            }
        } else {
            try {
                rgbArray = cells.get(cells.size() - 1).getImage().getRGB(0, 0,
                    totalWidth, imageHeight, null, 0, totalWidth);
            } catch (Exception e) {
                throw new RuntimeException("image " + imageWidth + "x" +
                    imageHeight + " cells.get(cells.size() - 1).getImage() " +
                    cells.get(cells.size() - 1).getImage(), e);
            }
        }
        image.setRGB((cells.size() - 1) * imageWidth, 0, totalWidth,
            imageHeight, rgbArray, 0, totalWidth);

        if (totalWidth < imageWidth) {
            int backgroundColor = cells.get(cells.size() - 1).getBackground().getRGB();

            for (int imageX = image.getWidth() - totalWidth;
                 imageX < image.getWidth(); imageX++) {

                for (int imageY = 0; imageY < fullHeight; imageY++) {
                    image.setRGB(imageX, imageY, backgroundColor);
                }
            }
        }

        if ((image.getWidth() != cells.size() * getTextWidth())
            || (image.getHeight() != getTextHeight())
        ) {
            // Rescale the image to fit the text cells it is going into.
            BufferedImage newImage;
            newImage = new BufferedImage(cells.size() * getTextWidth(),
                getTextHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics gr = newImage.getGraphics();
            if (gr instanceof Graphics2D) {
                ((Graphics2D) gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                ((Graphics2D) gr).setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            }
            gr.drawImage(image, 0, 0, newImage.getWidth(),
                newImage.getHeight(), null, null);
            gr.dispose();
            image = newImage;
        }

        return image;
    }

    // ------------------------------------------------------------------------
    // End sixel output support -----------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // iTerm2 image output support --------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Create an iTerm2 images string representing a row of several cells
     * containing bitmap data.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param cells the cells containing the bitmap data
     * @return the string to emit to an ANSI / ECMA-style terminal
     */
    private String toIterm2Image(final int x, final int y,
        final ArrayList<Cell> cells) {

        StringBuilder sb = new StringBuilder();

        assert (cells != null);
        assert (cells.size() > 0);
        assert (cells.get(0).getImage() != null);

        if (iterm2Images == false) {
            sb.append(normal());
            sb.append(gotoXY(x, y));
            for (int i = 0; i < cells.size(); i++) {
                sb.append(' ');
            }
            return sb.toString();
        }

        if (iterm2Cache == null) {
            iterm2Cache = new ImageCache(height * 10);
        }

        // Save and get rows to/from the cache that do NOT have inverted
        // cells.
        boolean saveInCache = true;
        for (Cell cell: cells) {
            if (cell.isInvertedImage()) {
                saveInCache = false;
            }
        }
        if (saveInCache) {
            String cachedResult = iterm2Cache.get(cells);
            if (cachedResult != null) {
                // System.err.println("CACHE HIT");
                sb.append(gotoXY(x, y));
                sb.append(cachedResult);
                return sb.toString();
            }
            // System.err.println("CACHE MISS");
        }

        BufferedImage image = cellsToImage(cells);
        int fullHeight = image.getHeight();

        /*
         * From https://iterm2.com/documentation-images.html:
         *
         * Protocol
         *
         * iTerm2 extends the xterm protocol with a set of proprietary escape
         * sequences. In general, the pattern is:
         *
         * ESC ] 1337 ; key = value ^G
         *
         * Whitespace is shown here for ease of reading: in practice, no
         * spaces should be used.
         *
         * For file transfer and inline images, the code is:
         *
         * ESC ] 1337 ; File = [optional arguments] : base-64 encoded file contents ^G
         *
         * The optional arguments are formatted as key=value with a semicolon
         * between each key-value pair. They are described below:
         *
         * Key		Description of value
         * name         base-64 encoded filename. Defaults to "Unnamed file".
         * size         File size in bytes. Optional; this is only used by the
         *              progress indicator.
         * width        Width to render. See notes below.
         * height       Height to render. See notes below.
         * preserveAspectRatio If set to 0, then the image's inherent aspect
         *                     ratio will not be respected; otherwise, it
         *                     will fill the specified width and height as
         *                     much as possible without stretching. Defaults
         *                     to 1.
         * inline If set to 1, the file will be displayed inline. Otherwise,
         *        it will be downloaded with no visual representation in the
         *        terminal session. Defaults to 0.
         *
         * The width and height are given as a number followed by a unit, or
         * the word "auto".
         *
         * N: N character cells.
         * Npx: N pixels.
         * N%: N percent of the session's width or height.
         * auto: The image's inherent size will be used to determine an
         *       appropriate dimension.
         *
         */

        // File contents can be several image formats.  We will use PNG.
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream(1024);
        try {
            if (!ImageIO.write(image.getSubimage(0, 0, image.getWidth(),
                        Math.min(image.getHeight(), fullHeight)),
                    "PNG", pngOutputStream)
            ) {
                // We failed to render image, bail out.
                return "";
            }
        } catch (IOException e) {
            // We failed to render image, bail out.
            return "";
        }

        sb.append("\033]1337;File=");
        /*
        sb.append(String.format("width=$d;height=1;preserveAspectRatio=1;",
                cells.size()));
         */
        /*
        sb.append(String.format("width=$dpx;height=%dpx;preserveAspectRatio=1;",
                image.getWidth(), Math.min(image.getHeight(),
                    getTextHeight())));
         */
        sb.append("inline=1:");
        sb.append(StringUtils.toBase64(pngOutputStream.toByteArray()));
        sb.append("\007");

        if (saveInCache) {
            // This row is OK to save into the cache.
            iterm2Cache.put(cells, sb.toString());
        }

        return (gotoXY(x, y) + sb.toString());
    }

    /**
     * Get the iTerm2 images support flag.
     *
     * @return true if this terminal is emitting iTerm2 images
     */
    public boolean hasIterm2Images() {
        return iterm2Images;
    }

    // ------------------------------------------------------------------------
    // End iTerm2 image output support ----------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Jexer image output support ---------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Create a Jexer images string representing a row of several cells
     * containing bitmap data.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param cells the cells containing the bitmap data
     * @return the string to emit to an ANSI / ECMA-style terminal
     */
    private String toJexerImage(final int x, final int y,
        final ArrayList<Cell> cells) {

        StringBuilder sb = new StringBuilder();

        assert (cells != null);
        assert (cells.size() > 0);
        assert (cells.get(0).getImage() != null);

        if (jexerImageOption == JexerImageOption.DISABLED) {
            sb.append(normal());
            sb.append(gotoXY(x, y));
            for (int i = 0; i < cells.size(); i++) {
                sb.append(' ');
            }
            return sb.toString();
        }

        if (jexerCache == null) {
            jexerCache = new ImageCache(height * 10);
        }

        // Save and get rows to/from the cache that do NOT have inverted
        // cells.
        boolean saveInCache = true;
        for (Cell cell: cells) {
            if (cell.isInvertedImage()) {
                saveInCache = false;
            }
        }
        if (saveInCache) {
            String cachedResult = jexerCache.get(cells);
            if (cachedResult != null) {
                // System.err.println("CACHE HIT");
                sb.append(gotoXY(x, y));
                sb.append(cachedResult);
                return sb.toString();
            }
            // System.err.println("CACHE MISS");
        }

        BufferedImage image = cellsToImage(cells);
        int fullHeight = image.getHeight();

        if (jexerImageOption == JexerImageOption.PNG) {
            // Encode as PNG
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream(1024);
            try {
                if (!ImageIO.write(image.getSubimage(0, 0, image.getWidth(),
                            Math.min(image.getHeight(), fullHeight)),
                        "PNG", pngOutputStream)
                ) {
                    // We failed to render image, bail out.
                    return "";
                }
            } catch (IOException e) {
                // We failed to render image, bail out.
                return "";
            }

            sb.append("\033]444;1;0;");
            sb.append(StringUtils.toBase64(pngOutputStream.toByteArray()));
            sb.append("\007");

        } else if (jexerImageOption == JexerImageOption.JPG) {

            // Encode as JPG
            ByteArrayOutputStream jpgOutputStream = new ByteArrayOutputStream(1024);

            // Convert from ARGB to RGB, otherwise the JPG encode will fail.
            BufferedImage jpgImage = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_INT_RGB);
            int [] pixels = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels,
                0, image.getWidth());
            jpgImage.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels,
                0, image.getWidth());

            try {
                if (!ImageIO.write(jpgImage.getSubimage(0, 0,
                            jpgImage.getWidth(),
                            Math.min(jpgImage.getHeight(), fullHeight)),
                        "JPG", jpgOutputStream)
                ) {
                    // We failed to render image, bail out.
                    return "";
                }
            } catch (IOException e) {
                // We failed to render image, bail out.
                return "";
            }

            sb.append("\033]444;2;0;");
            sb.append(StringUtils.toBase64(jpgOutputStream.toByteArray()));
            sb.append("\007");

        } else if (jexerImageOption == JexerImageOption.RGB) {

            // RGB
            sb.append(String.format("\033]444;0;%d;%d;0;", image.getWidth(),
                    Math.min(image.getHeight(), fullHeight)));

            byte [] bytes = new byte[image.getWidth() * image.getHeight() * 3];
            int stride = image.getWidth();
            for (int px = 0; px < stride; px++) {
                for (int py = 0; py < image.getHeight(); py++) {
                    int rgb = image.getRGB(px, py);
                    bytes[(py * stride * 3) + (px * 3)]     = (byte) ((rgb >>> 16) & 0xFF);
                    bytes[(py * stride * 3) + (px * 3) + 1] = (byte) ((rgb >>>  8) & 0xFF);
                    bytes[(py * stride * 3) + (px * 3) + 2] = (byte) ( rgb         & 0xFF);
                }
            }
            sb.append(StringUtils.toBase64(bytes));
            sb.append("\007");
        }

        if (saveInCache) {
            // This row is OK to save into the cache.
            jexerCache.put(cells, sb.toString());
        }

        return (gotoXY(x, y) + sb.toString());
    }

    /**
     * Get the Jexer images support flag.
     *
     * @return true if this terminal is emitting Jexer images
     */
    public boolean hasJexerImages() {
        return (jexerImageOption != JexerImageOption.DISABLED);
    }

    // ------------------------------------------------------------------------
    // End Jexer image output support -----------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Setup system colors to match DOS color palette.
     */
    private void setDOSColors() {
        MYBLACK         = new java.awt.Color(0x00, 0x00, 0x00);
        MYRED           = new java.awt.Color(0xa8, 0x00, 0x00);
        MYGREEN         = new java.awt.Color(0x00, 0xa8, 0x00);
        MYYELLOW        = new java.awt.Color(0xa8, 0x54, 0x00);
        MYBLUE          = new java.awt.Color(0x00, 0x00, 0xa8);
        MYMAGENTA       = new java.awt.Color(0xa8, 0x00, 0xa8);
        MYCYAN          = new java.awt.Color(0x00, 0xa8, 0xa8);
        MYWHITE         = new java.awt.Color(0xa8, 0xa8, 0xa8);
        MYBOLD_BLACK    = new java.awt.Color(0x54, 0x54, 0x54);
        MYBOLD_RED      = new java.awt.Color(0xfc, 0x54, 0x54);
        MYBOLD_GREEN    = new java.awt.Color(0x54, 0xfc, 0x54);
        MYBOLD_YELLOW   = new java.awt.Color(0xfc, 0xfc, 0x54);
        MYBOLD_BLUE     = new java.awt.Color(0x54, 0x54, 0xfc);
        MYBOLD_MAGENTA  = new java.awt.Color(0xfc, 0x54, 0xfc);
        MYBOLD_CYAN     = new java.awt.Color(0x54, 0xfc, 0xfc);
        MYBOLD_WHITE    = new java.awt.Color(0xfc, 0xfc, 0xfc);
    }

    /**
     * Setup ECMA48 colors to match those provided in system properties.
     */
    private void setCustomSystemColors() {
        setDOSColors();

        MYBLACK   = getCustomColor("jexer.ECMA48.color0", MYBLACK);
        MYRED     = getCustomColor("jexer.ECMA48.color1", MYRED);
        MYGREEN   = getCustomColor("jexer.ECMA48.color2", MYGREEN);
        MYYELLOW  = getCustomColor("jexer.ECMA48.color3", MYYELLOW);
        MYBLUE    = getCustomColor("jexer.ECMA48.color4", MYBLUE);
        MYMAGENTA = getCustomColor("jexer.ECMA48.color5", MYMAGENTA);
        MYCYAN    = getCustomColor("jexer.ECMA48.color6", MYCYAN);
        MYWHITE   = getCustomColor("jexer.ECMA48.color7", MYWHITE);
        MYBOLD_BLACK   = getCustomColor("jexer.ECMA48.color8", MYBOLD_BLACK);
        MYBOLD_RED     = getCustomColor("jexer.ECMA48.color9", MYBOLD_RED);
        MYBOLD_GREEN   = getCustomColor("jexer.ECMA48.color10", MYBOLD_GREEN);
        MYBOLD_YELLOW  = getCustomColor("jexer.ECMA48.color11", MYBOLD_YELLOW);
        MYBOLD_BLUE    = getCustomColor("jexer.ECMA48.color12", MYBOLD_BLUE);
        MYBOLD_MAGENTA = getCustomColor("jexer.ECMA48.color13", MYBOLD_MAGENTA);
        MYBOLD_CYAN    = getCustomColor("jexer.ECMA48.color14", MYBOLD_CYAN);
        MYBOLD_WHITE   = getCustomColor("jexer.ECMA48.color15", MYBOLD_WHITE);
    }

    /**
     * Setup one system color to match the RGB value provided in system
     * properties.
     *
     * @param key the system property key
     * @param defaultColor the default color to return if key is not set, or
     * incorrect
     * @return a color from the RGB string, or defaultColor
     */
    private java.awt.Color getCustomColor(final String key,
        final java.awt.Color defaultColor) {

        String rgb = System.getProperty(key);
        if (rgb == null) {
            return defaultColor;
        }
        if (rgb.startsWith("#")) {
            rgb = rgb.substring(1);
        }
        int rgbInt = 0;
        try {
            rgbInt = Integer.parseInt(rgb, 16);
        } catch (NumberFormatException e) {
            return defaultColor;
        }
        java.awt.Color color = new java.awt.Color((rgbInt & 0xFF0000) >>> 16,
            (rgbInt & 0x00FF00) >>> 8,
            (rgbInt & 0x0000FF));

        return color;
    }

    /**
     * Create a T.416 RGB parameter sequence for a custom system color.
     *
     * @param color one of the MYBLACK, MYBOLD_BLUE, etc. colors
     * @return the color portion of the string to emit to an ANSI /
     * ECMA-style terminal
     */
    private String systemColorRGB(final java.awt.Color color) {
        return String.format("%d;%d;%d", color.getRed(), color.getGreen(),
            color.getBlue());
    }

    /**
     * Create a SGR parameter sequence for a single color change.
     *
     * @param bold if true, set bold
     * @param color one of the Color.WHITE, Color.BLUE, etc. constants
     * @param foreground if true, this is a foreground color
     * @return the string to emit to an ANSI / ECMA-style terminal,
     * e.g. "\033[42m"
     */
    private String color(final boolean bold, final Color color,
        final boolean foreground) {
        return color(color, foreground, true) +
                rgbColor(bold, color, foreground);
    }

    /**
     * Create a T.416 RGB parameter sequence for a single color change.
     *
     * @param colorRGB a 24-bit RGB value for foreground color
     * @param foreground if true, this is a foreground color
     * @return the string to emit to an ANSI / ECMA-style terminal,
     * e.g. "\033[42m"
     */
    private String colorRGB(final int colorRGB, final boolean foreground) {

        int colorRed     = (colorRGB >>> 16) & 0xFF;
        int colorGreen   = (colorRGB >>>  8) & 0xFF;
        int colorBlue    =  colorRGB         & 0xFF;

        StringBuilder sb = new StringBuilder();
        if (foreground) {
            sb.append("\033[38;2;");
        } else {
            sb.append("\033[48;2;");
        }
        sb.append(String.format("%d;%d;%dm", colorRed, colorGreen, colorBlue));
        return sb.toString();
    }

    /**
     * Create a T.416 RGB parameter sequence for both foreground and
     * background color change.
     *
     * @param foreColorRGB a 24-bit RGB value for foreground color
     * @param backColorRGB a 24-bit RGB value for foreground color
     * @return the string to emit to an ANSI / ECMA-style terminal,
     * e.g. "\033[42m"
     */
    private String colorRGB(final int foreColorRGB, final int backColorRGB) {
        int foreColorRed     = (foreColorRGB >>> 16) & 0xFF;
        int foreColorGreen   = (foreColorRGB >>>  8) & 0xFF;
        int foreColorBlue    =  foreColorRGB         & 0xFF;
        int backColorRed     = (backColorRGB >>> 16) & 0xFF;
        int backColorGreen   = (backColorRGB >>>  8) & 0xFF;
        int backColorBlue    =  backColorRGB         & 0xFF;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\033[38;2;%d;%d;%dm",
                foreColorRed, foreColorGreen, foreColorBlue));
        sb.append(String.format("\033[48;2;%d;%d;%dm",
                backColorRed, backColorGreen, backColorBlue));
        return sb.toString();
    }

    /**
     * Create a T.416 RGB parameter sequence for a single color change.
     *
     * @param bold if true, set bold
     * @param color one of the Color.WHITE, Color.BLUE, etc. constants
     * @param foreground if true, this is a foreground color
     * @return the string to emit to an xterm terminal with RGB support,
     * e.g. "\033[38;2;RR;GG;BBm"
     */
    private String rgbColor(final boolean bold, final Color color,
        final boolean foreground) {
        if (doRgbColor == false) {
            return "";
        }
        StringBuilder sb = new StringBuilder("\033[");
        if (bold) {
            // Bold implies foreground only
            sb.append("38;2;");
            if (color.equals(Color.BLACK)) {
                sb.append(systemColorRGB(MYBOLD_BLACK));
            } else if (color.equals(Color.RED)) {
                sb.append(systemColorRGB(MYBOLD_RED));
            } else if (color.equals(Color.GREEN)) {
                sb.append(systemColorRGB(MYBOLD_GREEN));
            } else if (color.equals(Color.YELLOW)) {
                sb.append(systemColorRGB(MYBOLD_YELLOW));
            } else if (color.equals(Color.BLUE)) {
                sb.append(systemColorRGB(MYBOLD_BLUE));
            } else if (color.equals(Color.MAGENTA)) {
                sb.append(systemColorRGB(MYBOLD_MAGENTA));
            } else if (color.equals(Color.CYAN)) {
                sb.append(systemColorRGB(MYBOLD_CYAN));
            } else if (color.equals(Color.WHITE)) {
                sb.append(systemColorRGB(MYBOLD_WHITE));
            }
        } else {
            if (foreground) {
                sb.append("38;2;");
            } else {
                sb.append("48;2;");
            }
            if (color.equals(Color.BLACK)) {
                sb.append(systemColorRGB(MYBLACK));
            } else if (color.equals(Color.RED)) {
                sb.append(systemColorRGB(MYRED));
            } else if (color.equals(Color.GREEN)) {
                sb.append(systemColorRGB(MYGREEN));
            } else if (color.equals(Color.YELLOW)) {
                sb.append(systemColorRGB(MYYELLOW));
            } else if (color.equals(Color.BLUE)) {
                sb.append(systemColorRGB(MYBLUE));
            } else if (color.equals(Color.MAGENTA)) {
                sb.append(systemColorRGB(MYMAGENTA));
            } else if (color.equals(Color.CYAN)) {
                sb.append(systemColorRGB(MYCYAN));
            } else if (color.equals(Color.WHITE)) {
                sb.append(systemColorRGB(MYWHITE));
            }
        }
        sb.append("m");
        return sb.toString();
    }

    /**
     * Create a T.416 RGB parameter sequence for both foreground and
     * background color change.
     *
     * @param bold if true, set bold
     * @param foreColor one of the Color.WHITE, Color.BLUE, etc. constants
     * @param backColor one of the Color.WHITE, Color.BLUE, etc. constants
     * @return the string to emit to an xterm terminal with RGB support,
     * e.g. "\033[38;2;RR;GG;BB;48;2;RR;GG;BBm"
     */
    private String rgbColor(final boolean bold, final Color foreColor,
        final Color backColor) {
        if (doRgbColor == false) {
            return "";
        }

        return rgbColor(bold, foreColor, true) +
                rgbColor(false, backColor, false);
    }

    /**
     * Create a SGR parameter sequence for a single color change.
     *
     * @param color one of the Color.WHITE, Color.BLUE, etc. constants
     * @param foreground if true, this is a foreground color
     * @param header if true, make the full header, otherwise just emit the
     * color parameter e.g. "42;"
     * @return the string to emit to an ANSI / ECMA-style terminal,
     * e.g. "\033[42m"
     */
    private String color(final Color color, final boolean foreground,
        final boolean header) {

        int ecmaColor = color.getValue();

        // Convert Color.* values to SGR numerics
        if (foreground) {
            ecmaColor += 30;
        } else {
            ecmaColor += 40;
        }

        if (header) {
            return String.format("\033[%dm", ecmaColor);
        } else {
            return String.format("%d;", ecmaColor);
        }
    }

    /**
     * Create a SGR parameter sequence for both foreground and background
     * color change.
     *
     * @param bold if true, set bold
     * @param foreColor one of the Color.WHITE, Color.BLUE, etc. constants
     * @param backColor one of the Color.WHITE, Color.BLUE, etc. constants
     * @return the string to emit to an ANSI / ECMA-style terminal,
     * e.g. "\033[31;42m"
     */
    private String color(final boolean bold, final Color foreColor,
        final Color backColor) {
        return color(foreColor, backColor, true) +
                rgbColor(bold, foreColor, backColor);
    }

    /**
     * Create a SGR parameter sequence for both foreground and
     * background color change.
     *
     * @param foreColor one of the Color.WHITE, Color.BLUE, etc. constants
     * @param backColor one of the Color.WHITE, Color.BLUE, etc. constants
     * @param header if true, make the full header, otherwise just emit the
     * color parameter e.g. "31;42;"
     * @return the string to emit to an ANSI / ECMA-style terminal,
     * e.g. "\033[31;42m"
     */
    private String color(final Color foreColor, final Color backColor,
        final boolean header) {

        int ecmaForeColor = foreColor.getValue();
        int ecmaBackColor = backColor.getValue();

        // Convert Color.* values to SGR numerics
        ecmaBackColor += 40;
        ecmaForeColor += 30;

        if (header) {
            return String.format("\033[%d;%dm", ecmaForeColor, ecmaBackColor);
        } else {
            return String.format("%d;%d;", ecmaForeColor, ecmaBackColor);
        }
    }

    /**
     * Create a SGR parameter sequence for foreground, background, and
     * several attributes.  This sequence first resets all attributes to
     * default, then sets attributes as per the parameters.
     *
     * @param foreColor one of the Color.WHITE, Color.BLUE, etc. constants
     * @param backColor one of the Color.WHITE, Color.BLUE, etc. constants
     * @param bold if true, set bold
     * @param reverse if true, set reverse
     * @param blink if true, set blink
     * @param underline if true, set underline
     * @return the string to emit to an ANSI / ECMA-style terminal,
     * e.g. "\033[0;1;31;42m"
     */
    private String color(final Color foreColor, final Color backColor,
        final boolean bold, final boolean reverse, final boolean blink,
        final boolean underline) {

        int ecmaForeColor = foreColor.getValue();
        int ecmaBackColor = backColor.getValue();

        // Convert Color.* values to SGR numerics
        ecmaBackColor += 40;
        ecmaForeColor += 30;

        StringBuilder sb = new StringBuilder();
        if        (  bold &&  reverse &&  blink && !underline ) {
            sb.append("\033[0;1;7;5;");
        } else if (  bold &&  reverse && !blink && !underline ) {
            sb.append("\033[0;1;7;");
        } else if ( !bold &&  reverse &&  blink && !underline ) {
            sb.append("\033[0;7;5;");
        } else if (  bold && !reverse &&  blink && !underline ) {
            sb.append("\033[0;1;5;");
        } else if (  bold && !reverse && !blink && !underline ) {
            sb.append("\033[0;1;");
        } else if ( !bold &&  reverse && !blink && !underline ) {
            sb.append("\033[0;7;");
        } else if ( !bold && !reverse &&  blink && !underline) {
            sb.append("\033[0;5;");
        } else if (  bold &&  reverse &&  blink &&  underline ) {
            sb.append("\033[0;1;7;5;4;");
        } else if (  bold &&  reverse && !blink &&  underline ) {
            sb.append("\033[0;1;7;4;");
        } else if ( !bold &&  reverse &&  blink &&  underline ) {
            sb.append("\033[0;7;5;4;");
        } else if (  bold && !reverse &&  blink &&  underline ) {
            sb.append("\033[0;1;5;4;");
        } else if (  bold && !reverse && !blink &&  underline ) {
            sb.append("\033[0;1;4;");
        } else if ( !bold &&  reverse && !blink &&  underline ) {
            sb.append("\033[0;7;4;");
        } else if ( !bold && !reverse &&  blink &&  underline) {
            sb.append("\033[0;5;4;");
        } else if ( !bold && !reverse && !blink &&  underline) {
            sb.append("\033[0;4;");
        } else {
            assert (!bold && !reverse && !blink && !underline);
            sb.append("\033[0;");
        }
        sb.append(String.format("%d;%dm", ecmaForeColor, ecmaBackColor));
        sb.append(rgbColor(bold, foreColor, backColor));
        return sb.toString();
    }

    /**
     * Create a SGR parameter sequence for foreground, background, and
     * several attributes.  This sequence first resets all attributes to
     * default, then sets attributes as per the parameters.
     *
     * @param foreColorRGB a 24-bit RGB value for foreground color
     * @param backColorRGB a 24-bit RGB value for foreground color
     * @param bold if true, set bold
     * @param reverse if true, set reverse
     * @param blink if true, set blink
     * @param underline if true, set underline
     * @return the string to emit to an ANSI / ECMA-style terminal,
     * e.g. "\033[0;1;31;42m"
     */
    private String colorRGB(final int foreColorRGB, final int backColorRGB,
        final boolean bold, final boolean reverse, final boolean blink,
        final boolean underline) {

        int foreColorRed     = (foreColorRGB >>> 16) & 0xFF;
        int foreColorGreen   = (foreColorRGB >>>  8) & 0xFF;
        int foreColorBlue    =  foreColorRGB         & 0xFF;
        int backColorRed     = (backColorRGB >>> 16) & 0xFF;
        int backColorGreen   = (backColorRGB >>>  8) & 0xFF;
        int backColorBlue    =  backColorRGB         & 0xFF;

        StringBuilder sb = new StringBuilder();
        if        (  bold &&  reverse &&  blink && !underline ) {
            sb.append("\033[0;1;7;5;");
        } else if (  bold &&  reverse && !blink && !underline ) {
            sb.append("\033[0;1;7;");
        } else if ( !bold &&  reverse &&  blink && !underline ) {
            sb.append("\033[0;7;5;");
        } else if (  bold && !reverse &&  blink && !underline ) {
            sb.append("\033[0;1;5;");
        } else if (  bold && !reverse && !blink && !underline ) {
            sb.append("\033[0;1;");
        } else if ( !bold &&  reverse && !blink && !underline ) {
            sb.append("\033[0;7;");
        } else if ( !bold && !reverse &&  blink && !underline) {
            sb.append("\033[0;5;");
        } else if (  bold &&  reverse &&  blink &&  underline ) {
            sb.append("\033[0;1;7;5;4;");
        } else if (  bold &&  reverse && !blink &&  underline ) {
            sb.append("\033[0;1;7;4;");
        } else if ( !bold &&  reverse &&  blink &&  underline ) {
            sb.append("\033[0;7;5;4;");
        } else if (  bold && !reverse &&  blink &&  underline ) {
            sb.append("\033[0;1;5;4;");
        } else if (  bold && !reverse && !blink &&  underline ) {
            sb.append("\033[0;1;4;");
        } else if ( !bold &&  reverse && !blink &&  underline ) {
            sb.append("\033[0;7;4;");
        } else if ( !bold && !reverse &&  blink &&  underline) {
            sb.append("\033[0;5;4;");
        } else if ( !bold && !reverse && !blink &&  underline) {
            sb.append("\033[0;4;");
        } else {
            assert (!bold && !reverse && !blink && !underline);
            sb.append("\033[0;");
        }

        sb.append("m\033[38;2;");
        sb.append(String.format("%d;%d;%d", foreColorRed, foreColorGreen,
                foreColorBlue));
        sb.append("m\033[48;2;");
        sb.append(String.format("%d;%d;%d", backColorRed, backColorGreen,
                backColorBlue));
        sb.append("m");
        return sb.toString();
    }

    /**
     * Create a SGR parameter sequence to reset to VT100 defaults.
     *
     * @return the string to emit to an ANSI / ECMA-style terminal,
     * e.g. "\033[0m"
     */
    private String normal() {
        return normal(true) + rgbColor(false, Color.WHITE, Color.BLACK);
    }

    /**
     * Create a SGR parameter sequence to reset to ECMA-48 default
     * foreground/background.
     *
     * @return the string to emit to an ANSI / ECMA-style terminal,
     * e.g. "\033[0m"
     */
    private String defaultColor() {
        /*
         * VT100 normal.
         * Normal (neither bold nor faint).
         * Not italicized.
         * Not underlined.
         * Steady (not blinking).
         * Positive (not inverse).
         * Visible (not hidden).
         * Not crossed-out.
         * Default foreground color.
         * Default background color.
         */
        return "\033[0;22;23;24;25;27;28;29;39;49m";
    }

    /**
     * Create a SGR parameter sequence to reset to defaults.
     *
     * @param header if true, make the full header, otherwise just emit the
     * bare parameter e.g. "0;"
     * @return the string to emit to an ANSI / ECMA-style terminal,
     * e.g. "\033[0m"
     */
    private String normal(final boolean header) {
        if (header) {
            return "\033[0;37;40m";
        }
        return "0;37;40";
    }

    /**
     * Create a SGR parameter sequence for enabling the visible cursor.
     *
     * @param on if true, turn on cursor
     * @return the string to emit to an ANSI / ECMA-style terminal
     */
    private String cursor(final boolean on) {
        if (on && !cursorOn) {
            cursorOn = true;
            return "\033[?25h";
        }
        if (!on && cursorOn) {
            cursorOn = false;
            return "\033[?25l";
        }
        return "";
    }

    /**
     * Clear the entire screen.  Because some terminals use back-color-erase,
     * set the color to white-on-black beforehand.
     *
     * @return the string to emit to an ANSI / ECMA-style terminal
     */
    private String clearAll() {
        return "\033[0;37;40m\033[2J";
    }

    /**
     * Clear the line from the cursor (inclusive) to the end of the screen.
     * Because some terminals use back-color-erase, set the color to
     * white-on-black beforehand.
     *
     * @return the string to emit to an ANSI / ECMA-style terminal
     */
    private String clearRemainingLine() {
        return "\033[0;37;40m\033[K";
    }

    /**
     * Move the cursor to (x, y).
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @return the string to emit to an ANSI / ECMA-style terminal
     */
    private String gotoXY(final int x, final int y) {
        return String.format("\033[%d;%dH", y + 1, x + 1);
    }

    /**
     * Tell (u)xterm that we want to receive mouse events based on "Any event
     * tracking", UTF-8 coordinates, and then SGR coordinates.  Ideally we
     * will end up with SGR coordinates with UTF-8 coordinates as a fallback.
     * See
     * http://invisible-island.net/xterm/ctlseqs/ctlseqs.html#Mouse%20Tracking
     *
     * Note that this also sets the alternate/primary screen buffer.
     *
     * Finally, also emit a Privacy Message sequence that Jexer recognizes to
     * mean "hide the mouse pointer."  We have to use our own sequence to do
     * this because there is no standard in xterm for unilaterally hiding the
     * pointer all the time (regardless of typing).
     *
     * @param on If true, enable mouse report and use the alternate screen
     * buffer.  If false disable mouse reporting and use the primary screen
     * buffer.
     * @return the string to emit to xterm
     */
    private String mouse(final boolean on) {
        if (on) {
            return "\033[?1002;1003;1005;1006h\033[?1049h\033^hideMousePointer\033\\";
        }
        return "\033[?1002;1003;1006;1005l\033[?1049l\033^showMousePointer\033\\";
    }

}
