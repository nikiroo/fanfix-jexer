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

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;

import jexer.bits.Cell;
import jexer.bits.StringUtils;

/**
 * GlyphMakerFont creates glyphs as bitmaps from a font.
 */
class GlyphMakerFont {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * If true, enable debug messages.
     */
    private static boolean DEBUG = false;

    /**
     * If true, we were successful at getting the font dimensions.
     */
    private boolean gotFontDimensions = false;

    /**
     * The currently selected font.
     */
    private Font font = null;

    /**
     * Width of a character cell in pixels.
     */
    private int textWidth = 1;

    /**
     * Height of a character cell in pixels.
     */
    private int textHeight = 1;

    /**
     * Width of a character cell in pixels, as reported by font.
     */
    private int fontTextWidth = 1;

    /**
     * Height of a character cell in pixels, as reported by font.
     */
    private int fontTextHeight = 1;

    /**
     * Descent of a character cell in pixels.
     */
    private int maxDescent = 0;

    /**
     * System-dependent Y adjustment for text in the character cell.
     */
    private int textAdjustY = 0;

    /**
     * System-dependent X adjustment for text in the character cell.
     */
    private int textAdjustX = 0;

    /**
     * System-dependent height adjustment for text in the character cell.
     */
    private int textAdjustHeight = 0;

    /**
     * System-dependent width adjustment for text in the character cell.
     */
    private int textAdjustWidth = 0;

    /**
     * A cache of previously-rendered glyphs for blinking text, when it is
     * not visible.
     */
    private HashMap<Cell, BufferedImage> glyphCacheBlink;

    /**
     * A cache of previously-rendered glyphs for non-blinking, or
     * blinking-and-visible, text.
     */
    private HashMap<Cell, BufferedImage> glyphCache;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param filename the resource filename of the font to use
     * @param fontSize the size of font to use
     */
    public GlyphMakerFont(final String filename, final int fontSize) {

        if (filename.length() == 0) {
            // Fallback font
            font = new Font(Font.MONOSPACED, Font.PLAIN, fontSize - 2);
            return;
        }

        Font fontRoot = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream in = loader.getResourceAsStream(filename);
            fontRoot = Font.createFont(Font.TRUETYPE_FONT, in);
            font = fontRoot.deriveFont(Font.PLAIN, fontSize - 2);
        } catch (FontFormatException e) {
            // Ideally we would report an error here, either via System.err
            // or TExceptionDialog.  However, I do not want GlyphMaker to
            // know about available backends, so we quietly fallback to
            // whatever is available as MONO.
            font = new Font(Font.MONOSPACED, Font.PLAIN, fontSize - 2);
        } catch (IOException e) {
            // See comment above.
            font = new Font(Font.MONOSPACED, Font.PLAIN, fontSize - 2);
        }
    }

    // ------------------------------------------------------------------------
    // GlyphMakerFont ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get a glyph image.
     *
     * @param cell the character to draw
     * @param cellWidth the width of the text cell to draw into
     * @param cellHeight the height of the text cell to draw into
     * @return the glyph as an image
     */
    public BufferedImage getImage(final Cell cell, final int cellWidth,
        final int cellHeight) {

        return getImage(cell, cellWidth, cellHeight, true);
    }

    /**
     * Get a glyph image.
     *
     * @param cell the character to draw
     * @param cellWidth the width of the text cell to draw into
     * @param cellHeight the height of the text cell to draw into
     * @param blinkVisible if true, the cell is visible if it is blinking
     * @return the glyph as an image
     */
    public BufferedImage getImage(final Cell cell, final int cellWidth,
        final int cellHeight, final boolean blinkVisible) {

        if (gotFontDimensions == false) {
            // Lazy-load the text width/height and adjustments.
            getFontDimensions();
        }

        if (DEBUG && !font.canDisplay(cell.getChar())) {
            System.err.println("font " + font + " has no glyph for " +
                String.format("0x%x", cell.getChar()));
        }

        BufferedImage image = null;
        if (cell.isBlink() && !blinkVisible) {
            image = glyphCacheBlink.get(cell);
        } else {
            image = glyphCache.get(cell);
        }
        if (image != null) {
            return image;
        }

        // Generate glyph and draw it.
        image = new BufferedImage(cellWidth, cellHeight,
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr2 = image.createGraphics();
        gr2.setFont(font);

        Cell cellColor = new Cell(cell);

        // Check for reverse
        if (cell.isReverse()) {
            cellColor.setForeColor(cell.getBackColor());
            cellColor.setBackColor(cell.getForeColor());
        }

        // Draw the background rectangle, then the foreground character.
        gr2.setColor(SwingTerminal.attrToBackgroundColor(cellColor));
        gr2.fillRect(0, 0, cellWidth, cellHeight);

        // Handle blink and underline
        if (!cell.isBlink()
            || (cell.isBlink() && blinkVisible)
        ) {
            gr2.setColor(SwingTerminal.attrToForegroundColor(cellColor));
            char [] chars = Character.toChars(cell.getChar());
            gr2.drawChars(chars, 0, chars.length, textAdjustX,
                cellHeight - maxDescent + textAdjustY);

            if (cell.isUnderline()) {
                gr2.fillRect(0, cellHeight - 2, cellWidth, 2);
            }
        }
        gr2.dispose();

        // We need a new key that will not be mutated by invertCell().
        Cell key = new Cell(cell);
        if (cell.isBlink() && !blinkVisible) {
            glyphCacheBlink.put(key, image);
        } else {
            glyphCache.put(key, image);
        }

        /*
        System.err.println("cellWidth " + cellWidth +
            " cellHeight " + cellHeight + " image " + image);
         */

        return image;
    }

    /**
     * Figure out my font dimensions.
     */
    private void getFontDimensions() {
        glyphCacheBlink = new HashMap<Cell, BufferedImage>();
        glyphCache = new HashMap<Cell, BufferedImage>();

        BufferedImage image = new BufferedImage(font.getSize() * 2,
            font.getSize() * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = image.createGraphics();
        gr.setFont(font);
        FontMetrics fm = gr.getFontMetrics();
        maxDescent = fm.getMaxDescent();
        Rectangle2D bounds = fm.getMaxCharBounds(gr);
        int leading = fm.getLeading();
        fontTextWidth = (int)Math.round(bounds.getWidth());
        // fontTextHeight = (int)Math.round(bounds.getHeight()) - maxDescent;

        // This produces the same number, but works better for ugly
        // monospace.
        fontTextHeight = fm.getMaxAscent() + maxDescent - leading;
        gr.dispose();

        textHeight = fontTextHeight + textAdjustHeight;
        textWidth = fontTextWidth + textAdjustWidth;
        /*
        System.err.println("font " + font);
        System.err.println("fontTextWidth " + fontTextWidth);
        System.err.println("fontTextHeight " + fontTextHeight);
        System.err.println("textWidth " + textWidth);
        System.err.println("textHeight " + textHeight);
         */

        gotFontDimensions = true;
    }

    /**
     * Checks if this maker's Font has a glyph for the specified character.
     *
     * @param codePoint the character (Unicode code point) for which a glyph
     * is needed.
     * @return true if this Font has a glyph for the character; false
     * otherwise.
     */
    public boolean canDisplay(final int codePoint) {
        return font.canDisplay(codePoint);
    }
}

/**
 * GlyphMaker presents unified interface to all of its supported fonts to
 * clients.
 */
public class GlyphMaker {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The mono font resource filename (terminus).
     */
    private static final String MONO = "terminus-ttf-4.39/TerminusTTF-Bold-4.39.ttf";

    /**
     * The CJK font resource filename.
     */
    private static final String cjkFontFilename = "NotoSansMonoCJKtc-Regular.otf";

    /**
     * The emoji font resource filename.
     */
    private static final String emojiFontFilename = "OpenSansEmoji.ttf";

    /**
     * The fallback font resource filename.
     */
    private static final String fallbackFontFilename = "";

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * If true, enable debug messages.
     */
    private static boolean DEBUG = false;

    /**
     * Cache of font bundles by size.
     */
    private static HashMap<Integer, GlyphMaker> makers = new HashMap<Integer, GlyphMaker>();

    /**
     * The instance that has the mono (default) font.
     */
    private GlyphMakerFont makerMono;

    /**
     * The instance that has the CJK font.
     */
    private GlyphMakerFont makerCjk;

    /**
     * The instance that has the emoji font.
     */
    private GlyphMakerFont makerEmoji;

    /**
     * The instance that has the fallback font.
     */
    private GlyphMakerFont makerFallback;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Create an instance with references to the necessary fonts.
     *
     * @param fontSize the size of these fonts in pixels
     */
    private GlyphMaker(final int fontSize) {
        makerMono = new GlyphMakerFont(MONO, fontSize);

        String fontFilename = null;
        fontFilename = System.getProperty("jexer.cjkFont.filename",
            cjkFontFilename);
        makerCjk = new GlyphMakerFont(fontFilename, fontSize);
        fontFilename = System.getProperty("jexer.emojiFont.filename",
            emojiFontFilename);
        makerEmoji = new GlyphMakerFont(fontFilename, fontSize);
        fontFilename = System.getProperty("jexer.fallbackFont.filename",
            fallbackFontFilename);
        makerFallback = new GlyphMakerFont(fontFilename, fontSize);
    }

    // ------------------------------------------------------------------------
    // GlyphMaker -------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Obtain the GlyphMaker instance for a particular font size.
     *
     * @param fontSize the size of these fonts in pixels
     * @return the instance
     */
    public static GlyphMaker getInstance(final int fontSize) {
        synchronized (GlyphMaker.class) {
            GlyphMaker maker = makers.get(fontSize);
            if (maker == null) {
                maker = new GlyphMaker(fontSize);
                makers.put(fontSize, maker);
            }
            return maker;
        }
    }

    /**
     * Get a glyph image.
     *
     * @param cell the character to draw
     * @param cellWidth the width of the text cell to draw into
     * @param cellHeight the height of the text cell to draw into
     * @return the glyph as an image
     */
    public BufferedImage getImage(final Cell cell, final int cellWidth,
        final int cellHeight) {

        return getImage(cell, cellWidth, cellHeight, true);
    }

    /**
     * Get a glyph image.
     *
     * @param cell the character to draw
     * @param cellWidth the width of the text cell to draw into
     * @param cellHeight the height of the text cell to draw into
     * @param blinkVisible if true, the cell is visible if it is blinking
     * @return the glyph as an image
     */
    public BufferedImage getImage(final Cell cell, final int cellWidth,
        final int cellHeight, final boolean blinkVisible) {

        int ch = cell.getChar();
        if (StringUtils.isCjk(ch)) {
            if (makerCjk.canDisplay(ch)) {
                return makerCjk.getImage(cell, cellWidth, cellHeight,
                    blinkVisible);
            }
        }
        if (StringUtils.isEmoji(ch)) {
            if (makerEmoji.canDisplay(ch)) {
                // System.err.println("emoji: " + String.format("0x%x", ch));
                return makerEmoji.getImage(cell, cellWidth, cellHeight,
                    blinkVisible);
            }
        }

        // When all else fails, use the default.
        if (makerMono.canDisplay(ch)) {
            return makerMono.getImage(cell, cellWidth, cellHeight,
                blinkVisible);
        }

        return makerFallback.getImage(cell, cellWidth, cellHeight,
            blinkVisible);
    }

}
