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
package jexer.tterminal;

import jexer.bits.Cell;
import jexer.bits.CellAttributes;

/**
 * This represents a single line of the display buffer.
 */
public class DisplayLine {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Maximum line length.
     */
    private static final int MAX_LINE_LENGTH = 256;

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The characters/attributes of the line.
     */
    private Cell [] chars;

    /**
     * Double-width line flag.
     */
    private boolean doubleWidth = false;

    /**
     * Double height line flag.  Valid values are:
     *
     * <p><pre>
     *   0 = single height
     *   1 = top half double height
     *   2 = bottom half double height
     * </pre>
     */
    private int doubleHeight = 0;

    /**
     * DECSCNM - reverse video.  We copy the flag to the line so that
     * reverse-mode scrollback lines still show inverted colors correctly.
     */
    private boolean reverseColor = false;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor makes a duplicate (deep copy).
     *
     * @param line the line to duplicate
     */
    public DisplayLine(final DisplayLine line) {
        chars = new Cell[MAX_LINE_LENGTH];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = new Cell(line.chars[i]);
        }
        doubleWidth = line.doubleWidth;
        doubleHeight = line.doubleHeight;
        reverseColor = line.reverseColor;
    }

    /**
     * Public constructor sets everything to drawing attributes.
     *
     * @param attr current drawing attributes
     */
    public DisplayLine(final CellAttributes attr) {
        chars = new Cell[MAX_LINE_LENGTH];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = new Cell(attr);
        }
    }

    // ------------------------------------------------------------------------
    // DisplayLine ------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the Cell at a specific column.
     *
     * @param idx the character index
     * @return the Cell
     */
    public Cell charAt(final int idx) {
        return chars[idx];
    }

    /**
     * Get the length of this line.
     *
     * @return line length
     */
    public int length() {
        return chars.length;
    }

    /**
     * Get double width flag.
     *
     * @return double width
     */
    public boolean isDoubleWidth() {
        return doubleWidth;
    }

    /**
     * Set double width flag.
     *
     * @param doubleWidth new value for double width flag
     */
    public void setDoubleWidth(final boolean doubleWidth) {
        this.doubleWidth = doubleWidth;
    }

    /**
     * Get double height flag.
     *
     * @return double height
     */
    public int getDoubleHeight() {
        return doubleHeight;
    }

    /**
     * Set double height flag.
     *
     * @param doubleHeight new value for double height flag
     */
    public void setDoubleHeight(final int doubleHeight) {
        this.doubleHeight = doubleHeight;
    }

    /**
     * Get reverse video flag.
     *
     * @return reverse video
     */
    public boolean isReverseColor() {
        return reverseColor;
    }

    /**
     * Set double-height flag.
     *
     * @param reverseColor new value for reverse video flag
     */
    public void setReverseColor(final boolean reverseColor) {
        this.reverseColor = reverseColor;
    }

    /**
     * Insert a character at the specified position.
     *
     * @param idx the character index
     * @param newCell the new Cell
     */
    public void insert(final int idx, final Cell newCell) {
        System.arraycopy(chars, idx, chars, idx + 1, chars.length - idx - 1);
        chars[idx] = new Cell(newCell);
    }

    /**
     * Replace character at the specified position.
     *
     * @param idx the character index
     * @param newCell the new Cell
     */
    public void replace(final int idx, final Cell newCell) {
        chars[idx].setTo(newCell);
    }

    /**
     * Set the Cell at the specified position to the blank (reset).
     *
     * @param idx the character index
     */
    public void setBlank(final int idx) {
        chars[idx].reset();
    }

    /**
     * Set the character (just the char, not the attributes) at the specified
     * position to ch.
     *
     * @param idx the character index
     * @param ch the new char
     */
    public void setChar(final int idx, final int ch) {
        chars[idx].setChar(ch);
    }

    /**
     * Set the attributes (just the attributes, not the char) at the
     * specified position to attr.
     *
     * @param idx the character index
     * @param attr the new attributes
     */
    public void setAttr(final int idx, final CellAttributes attr) {
        chars[idx].setAttr(attr);
    }

    /**
     * Delete character at the specified position, filling in the new
     * character on the right with newCell.
     *
     * @param idx the character index
     * @param newCell the new Cell
     */
    public void delete(final int idx, final Cell newCell) {
        System.arraycopy(chars, idx + 1, chars, idx, chars.length - idx - 1);
        chars[chars.length - 1] = new Cell(newCell);
    }

    /**
     * Determine if line contains image data.
     *
     * @return true if the line has image data
     */
    public boolean isImage() {
        for (int i = 0; i < chars.length; i++) {
            if (chars[i].isImage()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clear image data from line.
     */
    public void clearImages() {
        for (int i = 0; i < chars.length; i++) {
            if (chars[i].isImage()) {
                chars[i].reset();
            }
        }
    }

}
