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
package jexer.bits;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * This class represents a single text cell or bit of image on the screen.
 */
public final class Cell extends CellAttributes {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * How this cell needs to be displayed if it is part of a larger glyph.
     */
    public enum Width {
        /**
         * This cell is an entire glyph on its own.
         */
        SINGLE,

        /**
         * This cell is the left half of a wide glyph.
         */
        LEFT,

        /**
         * This cell is the right half of a wide glyph.
         */
        RIGHT,
    }

    /**
     * The special "this cell is unset" (null) value.  This is the Unicode
     * "not a character" value.
     */
    private static final char UNSET_VALUE = (char) 65535;

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The character at this cell.
     */
    private int ch = ' ';

    /**
     * The display width of this cell.
     */
    private Width width = Width.SINGLE;

    /**
     * The image at this cell.
     */
    private BufferedImage image = null;

    /**
     * The image at this cell, inverted.
     */
    private BufferedImage invertedImage = null;

    /**
     * The background color used for the area the image portion might not
     * cover.
     */
    private Color background = Color.BLACK;

    /**
     * hashCode() needs to call image.hashCode(), which can get quite
     * expensive.
     */
    private int imageHashCode = 0;

    /**
     * hashCode() needs to call background.hashCode(), which can get quite
     * expensive.
     */
    private int backgroundHashCode = 0;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor sets default values of the cell to blank.
     *
     * @see #isBlank()
     * @see #reset()
     */
    public Cell() {
        // NOP
    }

    /**
     * Public constructor sets the character.  Attributes are the same as
     * default.
     *
     * @param ch character to set to
     * @see #reset()
     */
    public Cell(final int ch) {
        this.ch = ch;
    }

    /**
     * Public constructor sets the attributes.
     *
     * @param attr attributes to use
     */
    public Cell(final CellAttributes attr) {
        super(attr);
    }

    /**
     * Public constructor sets the character and attributes.
     *
     * @param ch character to set to
     * @param attr attributes to use
     */
    public Cell(final int ch, final CellAttributes attr) {
        super(attr);
        this.ch = ch;
    }

    /**
     * Public constructor creates a duplicate.
     *
     * @param cell the instance to copy
     */
    public Cell(final Cell cell) {
        setTo(cell);
    }

    // ------------------------------------------------------------------------
    // Cell -------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Set the image data for this cell.
     *
     * @param image the image for this cell
     */
    public void setImage(final BufferedImage image) {
        this.image = image;
        imageHashCode = image.hashCode();
        width = Width.SINGLE;
    }

    /**
     * Get the image data for this cell.
     *
     * @return the image for this cell
     */
    public BufferedImage getImage() {
        if (invertedImage != null) {
            return invertedImage;
        }
        return image;
    }

    /**
     * Get the bitmap image background color for this cell.
     *
     * @return the bitmap image background color
     */
    public Color getBackground() {
        return background;
    }

    /**
     * If true, this cell has image data.
     *
     * @return true if this cell is an image rather than a character with
     * attributes
     */
    public boolean isImage() {
        if (image != null) {
            return true;
        }
        return false;
    }

    /**
     * Restore the image in this cell to its normal version, if it has one.
     */
    public void restoreImage() {
        invertedImage = null;
    }

    /**
     * If true, this cell has image data, and that data is inverted.
     *
     * @return true if this cell is an image rather than a character with
     * attributes, and the data is inverted
     */
    public boolean isInvertedImage() {
        if ((image != null) && (invertedImage != null)) {
            return true;
        }
        return false;
    }

    /**
     * Invert the image in this cell, if it has one.
     */
    public void invertImage() {
        if (image == null) {
            return;
        }
        if (invertedImage == null) {
            invertedImage = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_INT_ARGB);

            int [] rgbArray = image.getRGB(0, 0,
                image.getWidth(), image.getHeight(), null, 0, image.getWidth());

            for (int i = 0; i < rgbArray.length; i++) {
                // Set the colors to fully inverted.
                if (rgbArray[i] != 0x00FFFFFF) {
                    rgbArray[i] ^= 0x00FFFFFF;
                }
                // Also set alpha to non-transparent.
                rgbArray[i] |= 0xFF000000;
            }
            invertedImage.setRGB(0, 0, image.getWidth(), image.getHeight(),
                rgbArray, 0, image.getWidth());
        }
    }

    /**
     * Getter for cell character.
     *
     * @return cell character
     */
    public int getChar() {
        return ch;
    }

    /**
     * Setter for cell character.
     *
     * @param ch new cell character
     */
    public void setChar(final int ch) {
        this.ch = ch;
    }

    /**
     * Getter for cell width.
     *
     * @return Width.SINGLE, Width.LEFT, or Width.RIGHT
     */
    public Width getWidth() {
        return width;
    }

    /**
     * Setter for cell width.
     *
     * @param width new cell width, one of Width.SINGLE, Width.LEFT, or
     * Width.RIGHT
     */
    public void setWidth(final Width width) {
        this.width = width;
    }

    /**
     * Reset this cell to a blank.
     */
    @Override
    public void reset() {
        super.reset();
        ch = ' ';
        width = Width.SINGLE;
        image = null;
        imageHashCode = 0;
        invertedImage = null;
        background = Color.BLACK;
        backgroundHashCode = 0;
    }

    /**
     * UNset this cell.  It will not be equal to any other cell until it has
     * been assigned attributes and a character.
     */
    public void unset() {
        super.reset();
        ch = UNSET_VALUE;
        width = Width.SINGLE;
        image = null;
        imageHashCode = 0;
        invertedImage = null;
        background = Color.BLACK;
        backgroundHashCode = 0;
    }

    /**
     * Check to see if this cell has default attributes: white foreground,
     * black background, no bold/blink/reverse/underline/protect, and a
     * character value of ' ' (space).
     *
     * @return true if this cell has default attributes.
     */
    public boolean isBlank() {
        if ((ch == UNSET_VALUE) || (image != null)) {
            return false;
        }
        if ((getForeColor().equals(Color.WHITE))
            && (getBackColor().equals(Color.BLACK))
            && !isBold()
            && !isBlink()
            && !isReverse()
            && !isUnderline()
            && !isProtect()
            && !isRGB()
            && !isImage()
            && (width == Width.SINGLE)
            && (ch == ' ')
        ) {
            return true;
        }

        return false;
    }

    /**
     * Comparison check.  All fields must match to return true.
     *
     * @param rhs another Cell instance
     * @return true if all fields are equal
     */
    @Override
    public boolean equals(final Object rhs) {
        if (!(rhs instanceof Cell)) {
            return false;
        }

        Cell that = (Cell) rhs;

        // Unsetted cells can never be equal.
        if ((ch == UNSET_VALUE) || (that.ch == UNSET_VALUE)) {
            return false;
        }

        // If this or rhs has an image and the other doesn't, these are not
        // equal.
        if ((image != null) && (that.image == null)) {
            return false;
        }
        if ((image == null) && (that.image != null)) {
            return false;
        }
        // If this and rhs have images, both must match.
        if ((image != null) && (that.image != null)) {
            if ((invertedImage == null) && (that.invertedImage != null)) {
                return false;
            }
            if ((invertedImage != null) && (that.invertedImage == null)) {
                return false;
            }
            // Either both objects have their image inverted, or neither do.
            // Now if the images are identical the cells are the same
            // visually.
            if (image.equals(that.image)
                && (background.equals(that.background))
            ) {
                return true;
            } else {
                return false;
            }
        }

        // Normal case: character and attributes must match.
        if ((ch == that.ch) && (width == that.width)) {
            return super.equals(rhs);
        }
        return false;
    }

    /**
     * Hashcode uses all fields in equals().
     *
     * @return the hash
     */
    @Override
    public int hashCode() {
        int A = 13;
        int B = 23;
        int hash = A;
        hash = (B * hash) + super.hashCode();
        hash = (B * hash) + ch;
        hash = (B * hash) + width.hashCode();
        if (image != null) {
            /*
            hash = (B * hash) + image.hashCode();
            hash = (B * hash) + background.hashCode();
             */
            hash = (B * hash) + imageHashCode;
            hash = (B * hash) + backgroundHashCode;
        }
        if (invertedImage != null) {
            hash = (B * hash) + invertedImage.hashCode();
        }
        return hash;
    }

    /**
     * Set my field values to that's field.
     *
     * @param rhs an instance of either Cell or CellAttributes
     */
    @Override
    public void setTo(final Object rhs) {
        // Let this throw a ClassCastException
        CellAttributes thatAttr = (CellAttributes) rhs;
        this.image = null;
        this.imageHashCode = 0;
        this.backgroundHashCode = 0;
        this.width = Width.SINGLE;
        super.setTo(thatAttr);

        if (rhs instanceof Cell) {
            Cell that = (Cell) rhs;
            this.ch = that.ch;
            this.width = that.width;
            this.image = that.image;
            this.invertedImage = that.invertedImage;
            this.background = that.background;
            this.imageHashCode = that.imageHashCode;
            this.backgroundHashCode = that.backgroundHashCode;
        }
    }

    /**
     * Set my field attr values to that's field.
     *
     * @param that a CellAttributes instance
     */
    public void setAttr(final CellAttributes that) {
        image = null;
        super.setTo(that);
    }

    /**
     * Make human-readable description of this Cell.
     *
     * @return displayable String
     */
    @Override
    public String toString() {
        return String.format("fore: %s back: %s bold: %s blink: %s ch %c",
            getForeColor(), getBackColor(), isBold(), isBlink(), ch);
    }
}
