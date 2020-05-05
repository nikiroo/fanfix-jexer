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
package jexer;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import jexer.bits.CellAttributes;
import jexer.bits.StringUtils;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;
import static jexer.TKeypress.kbDown;
import static jexer.TKeypress.kbEnd;
import static jexer.TKeypress.kbHome;
import static jexer.TKeypress.kbLeft;
import static jexer.TKeypress.kbPgDn;
import static jexer.TKeypress.kbPgUp;
import static jexer.TKeypress.kbRight;
import static jexer.TKeypress.kbUp;

/**
 * TText implements a simple scrollable text area. It reflows automatically on
 * resize.
 */
public class TText extends TScrollableWidget {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Available text justifications.
     */
    public enum Justification {

        /**
         * Not justified at all, use spacing as provided by the client.
         */
        NONE,

        /**
         * Left-justified text.
         */
        LEFT,

        /**
         * Centered text.
         */
        CENTER,

        /**
         * Right-justified text.
         */
        RIGHT,

        /**
         * Fully-justified text.
         */
        FULL,
    }

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * How to justify the text.
     */
    private Justification justification = Justification.LEFT;

    /**
     * Text to display.
     */
    private String text;

    /**
     * Text converted to lines.
     */
    private List<String> lines;

    /**
     * Text color.
     */
    private String colorKey;

    /**
     * Maximum width of a single line.
     */
    private int maxLineWidth;

    /**
     * Number of lines between each paragraph.
     */
    private int lineSpacing = 1;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param text text on the screen
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     */
    public TText(final TWidget parent, final String text, final int x,
            final int y, final int width, final int height) {

        this(parent, text, x, y, width, height, "ttext");
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param text text on the screen
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param colorKey ColorTheme key color to use for foreground
     * text. Default is "ttext".
     */
    public TText(final TWidget parent, final String text, final int x,
            final int y, final int width, final int height,
            final String colorKey) {

        // Set parent and window
        super(parent, x, y, width, height);

        this.text = text;
        this.colorKey = colorKey;

        lines = new ArrayList<String>();

        vScroller = new TVScroller(this, getWidth() - 1, 0,
            Math.max(1, getHeight() - 1));
        hScroller = new THScroller(this, 0, getHeight() - 1,
            Math.max(1, getWidth() - 1));
        reflowData();
    }

    // ------------------------------------------------------------------------
    // TScrollableWidget ------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Override TWidget's width: we need to set child widget widths.
     *
     * @param width new widget width
     */
    @Override
    public void setWidth(final int width) {
        super.setWidth(width);
        if (hScroller != null) {
            hScroller.setWidth(getWidth() - 1);
        }
        if (vScroller != null) {
            vScroller.setX(getWidth() - 1);
        }
    }

    /**
     * Override TWidget's height: we need to set child widget heights.
     * time.
     *
     * @param height new widget height
     */
    @Override
    public void setHeight(final int height) {
        super.setHeight(height);
        if (hScroller != null) {
            hScroller.setY(getHeight() - 1);
        }
        if (vScroller != null) {
            vScroller.setHeight(getHeight() - 1);
        }
    }

    /**
     * Draw the text box.
     */
    @Override
    public void draw() {
        // Setup my color
        CellAttributes color = getTheme().getColor(colorKey);

        int begin = vScroller.getValue();
        int topY = 0;
        for (int i = begin; i < lines.size(); i++) {
            String line = lines.get(i);
            if (hScroller.getValue() < StringUtils.width(line)) {
                line = line.substring(hScroller.getValue());
            } else {
                line = "";
            }
            if (getWidth() > 3) {
                String formatString = "%-" + Integer.toString(getWidth() - 1) + "s";
                putStringXY(0, topY, String.format(formatString, line), color);
            }
            topY++;

            if (topY >= (getHeight() - 1)) {
                break;
            }
        }

        // Pad the rest with blank lines
        for (int i = topY; i < (getHeight() - 1); i++) {
            hLineXY(0, i, getWidth() - 1, ' ', color);
        }

    }

    /**
     * Handle mouse press events.
     *
     * @param mouse mouse button press event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        if (mouse.isMouseWheelUp()) {
            vScroller.decrement();
            return;
        }
        if (mouse.isMouseWheelDown()) {
            vScroller.increment();
            return;
        }

        // Pass to children
        super.onMouseDown(mouse);
    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        if (keypress.equals(kbLeft)) {
            hScroller.decrement();
        } else if (keypress.equals(kbRight)) {
            hScroller.increment();
        } else if (keypress.equals(kbUp)) {
            vScroller.decrement();
        } else if (keypress.equals(kbDown)) {
            vScroller.increment();
        } else if (keypress.equals(kbPgUp)) {
            vScroller.bigDecrement();
        } else if (keypress.equals(kbPgDn)) {
            vScroller.bigIncrement();
        } else if (keypress.equals(kbHome)) {
            vScroller.toTop();
        } else if (keypress.equals(kbEnd)) {
            vScroller.toBottom();
        } else {
            // Pass other keys (tab etc.) on
            super.onKeypress(keypress);
        }
    }

    /**
     * Resize text and scrollbars for a new width/height.
     */
    @Override
    public void reflowData() {
        // Reset the lines
        lines.clear();

        // Break up text into paragraphs
        String[] paragraphs = text.split("\n\n");
        for (String p : paragraphs) {
            switch (justification) {
            case NONE:
                lines.addAll(Arrays.asList(p.split("\n")));
                break;
            case LEFT:
                lines.addAll(jexer.bits.StringUtils.left(p,
                        getWidth() - 1));
                break;
            case CENTER:
                lines.addAll(jexer.bits.StringUtils.center(p,
                        getWidth() - 1));
                break;
            case RIGHT:
                lines.addAll(jexer.bits.StringUtils.right(p,
                        getWidth() - 1));
                break;
            case FULL:
                lines.addAll(jexer.bits.StringUtils.full(p,
                        getWidth() - 1));
                break;
            }

            for (int i = 0; i < lineSpacing; i++) {
                lines.add("");
            }
        }
        computeBounds();
    }

    // ------------------------------------------------------------------------
    // TText ------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Set the text.
     *
     * @param text new text to display
     */
    public void setText(final String text) {
        this.text = text;
        reflowData();
    }

    /**
     * Get the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Convenience method used by TWindowLoggerOutput.
     *
     * @param line new line to add
     */
    public void addLine(final String line) {
        if (StringUtils.width(text) == 0) {
            text = line;
        } else {
            text += "\n\n";
            text += line;
        }
        reflowData();
    }

    /**
     * Recompute the bounds for the scrollbars.
     */
    private void computeBounds() {
        maxLineWidth = 0;
        for (String line : lines) {
            if (StringUtils.width(line) > maxLineWidth) {
                maxLineWidth = StringUtils.width(line);
            }
        }

        vScroller.setTopValue(0);
        vScroller.setBottomValue((lines.size() - getHeight()) + 1);
        if (vScroller.getBottomValue() < 0) {
            vScroller.setBottomValue(0);
        }
        if (vScroller.getValue() > vScroller.getBottomValue()) {
            vScroller.setValue(vScroller.getBottomValue());
        }

        hScroller.setLeftValue(0);
        hScroller.setRightValue((maxLineWidth - getWidth()) + 1);
        if (hScroller.getRightValue() < 0) {
            hScroller.setRightValue(0);
        }
        if (hScroller.getValue() > hScroller.getRightValue()) {
            hScroller.setValue(hScroller.getRightValue());
        }
    }

    /**
     * Set justification.
     *
     * @param justification NONE, LEFT, CENTER, RIGHT, or FULL
     */
    public void setJustification(final Justification justification) {
        this.justification = justification;
        reflowData();
    }

    /**
     * Left-justify the text.
     */
    public void leftJustify() {
        justification = Justification.LEFT;
        reflowData();
    }

    /**
     * Center-justify the text.
     */
    public void centerJustify() {
        justification = Justification.CENTER;
        reflowData();
    }

    /**
     * Right-justify the text.
     */
    public void rightJustify() {
        justification = Justification.RIGHT;
        reflowData();
    }

    /**
     * Fully-justify the text.
     */
    public void fullJustify() {
        justification = Justification.FULL;
        reflowData();
    }

    /**
     * Un-justify the text.
     */
    public void unJustify() {
        justification = Justification.NONE;
        reflowData();
    }

}
