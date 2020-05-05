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

/**
 * The attributes used by a Cell: color, bold, blink, etc.
 */
public class CellAttributes {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Bold attribute.
     */
    private static final int BOLD       = 0x01;

    /**
     * Blink attribute.
     */
    private static final int BLINK      = 0x02;

    /**
     * Reverse attribute.
     */
    private static final int REVERSE    = 0x04;

    /**
     * Underline attribute.
     */
    private static final int UNDERLINE  = 0x08;

    /**
     * Protected attribute.
     */
    private static final int PROTECT    = 0x10;

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Boolean flags.
     */
    private int flags = 0;

    /**
     * Foreground color.  Color.WHITE, Color.RED, etc.
     */
    private Color foreColor = Color.WHITE;

    /**
     * Background color.  Color.WHITE, Color.RED, etc.
     */
    private Color backColor = Color.BLACK;

    /**
     * Foreground color as 24-bit RGB value.  Negative value means not set.
     */
    private int foreColorRGB = -1;

    /**
     * Background color as 24-bit RGB value.  Negative value means not set.
     */
    private int backColorRGB = -1;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor sets default values of the cell to white-on-black,
     * no bold/blink/reverse/underline/protect.
     *
     * @see #reset()
     */
    public CellAttributes() {
        // NOP
    }

    /**
     * Public constructor makes a copy from another instance.
     *
     * @param that another CellAttributes instance
     * @see #reset()
     */
    public CellAttributes(final CellAttributes that) {
        setTo(that);
    }

    // ------------------------------------------------------------------------
    // CellAttributes ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Getter for bold.
     *
     * @return bold value
     */
    public final boolean isBold() {
        return ((flags & BOLD) == 0 ? false : true);
    }

    /**
     * Setter for bold.
     *
     * @param bold new bold value
     */
    public final void setBold(final boolean bold) {
        if (bold) {
            flags |= BOLD;
        } else {
            flags &= ~BOLD;
        }
    }

    /**
     * Getter for blink.
     *
     * @return blink value
     */
    public final boolean isBlink() {
        return ((flags & BLINK) == 0 ? false : true);
    }

    /**
     * Setter for blink.
     *
     * @param blink new blink value
     */
    public final void setBlink(final boolean blink) {
        if (blink) {
            flags |= BLINK;
        } else {
            flags &= ~BLINK;
        }
    }

    /**
     * Getter for reverse.
     *
     * @return reverse value
     */
    public final boolean isReverse() {
        return ((flags & REVERSE) == 0 ? false : true);
    }

    /**
     * Setter for reverse.
     *
     * @param reverse new reverse value
     */
    public final void setReverse(final boolean reverse) {
        if (reverse) {
            flags |= REVERSE;
        } else {
            flags &= ~REVERSE;
        }
    }

    /**
     * Getter for underline.
     *
     * @return underline value
     */
    public final boolean isUnderline() {
        return ((flags & UNDERLINE) == 0 ? false : true);
    }

    /**
     * Setter for underline.
     *
     * @param underline new underline value
     */
    public final void setUnderline(final boolean underline) {
        if (underline) {
            flags |= UNDERLINE;
        } else {
            flags &= ~UNDERLINE;
        }
    }

    /**
     * Getter for protect.
     *
     * @return protect value
     */
    public final boolean isProtect() {
        return ((flags & PROTECT) == 0 ? false : true);
    }

    /**
     * Setter for protect.
     *
     * @param protect new protect value
     */
    public final void setProtect(final boolean protect) {
        if (protect) {
            flags |= PROTECT;
        } else {
            flags &= ~PROTECT;
        }
    }

    /**
     * Getter for foreColor.
     *
     * @return foreColor value
     */
    public final Color getForeColor() {
        return foreColor;
    }

    /**
     * Setter for foreColor.
     *
     * @param foreColor new foreColor value
     */
    public final void setForeColor(final Color foreColor) {
        this.foreColor = foreColor;
    }

    /**
     * Getter for backColor.
     *
     * @return backColor value
     */
    public final Color getBackColor() {
        return backColor;
    }

    /**
     * Setter for backColor.
     *
     * @param backColor new backColor value
     */
    public final void setBackColor(final Color backColor) {
        this.backColor = backColor;
    }

    /**
     * Getter for foreColor RGB.
     *
     * @return foreColor value.  Negative means unset.
     */
    public final int getForeColorRGB() {
        return foreColorRGB;
    }

    /**
     * Setter for foreColor RGB.
     *
     * @param foreColorRGB new foreColor RGB value
     */
    public final void setForeColorRGB(final int foreColorRGB) {
        this.foreColorRGB = foreColorRGB;
    }

    /**
     * Getter for backColor RGB.
     *
     * @return backColor value.  Negative means unset.
     */
    public final int getBackColorRGB() {
        return backColorRGB;
    }

    /**
     * Setter for backColor RGB.
     *
     * @param backColorRGB new backColor RGB value
     */
    public final void setBackColorRGB(final int backColorRGB) {
        this.backColorRGB = backColorRGB;
    }

    /**
     * See if this cell uses RGB or ANSI colors.
     *
     * @return true if this cell has a RGB color
     */
    public final boolean isRGB() {
        return (foreColorRGB >= 0) || (backColorRGB >= 0);
    }

    /**
     * Set to default: white foreground on black background, no
     * bold/underline/blink/rever/protect.
     */
    public void reset() {
        flags           = 0;
        foreColor       = Color.WHITE;
        backColor       = Color.BLACK;
        foreColorRGB    = -1;
        backColorRGB    = -1;
    }

    /**
     * Comparison check.  All fields must match to return true.
     *
     * @param rhs another CellAttributes instance
     * @return true if all fields are equal
     */
    @Override
    public boolean equals(final Object rhs) {
        if (!(rhs instanceof CellAttributes)) {
            return false;
        }

        CellAttributes that = (CellAttributes) rhs;
        return ((flags == that.flags)
            && (foreColor == that.foreColor)
            && (backColor == that.backColor)
            && (foreColorRGB == that.foreColorRGB)
            && (backColorRGB == that.backColorRGB));
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
        hash = (B * hash) + flags;
        hash = (B * hash) + foreColor.hashCode();
        hash = (B * hash) + backColor.hashCode();
        hash = (B * hash) + foreColorRGB;
        hash = (B * hash) + backColorRGB;
        return hash;
    }

    /**
     * Set my field values to that's field.
     *
     * @param rhs another CellAttributes instance
     */
    public void setTo(final Object rhs) {
        CellAttributes that = (CellAttributes) rhs;

        this.flags              = that.flags;
        this.foreColor          = that.foreColor;
        this.backColor          = that.backColor;
        this.foreColorRGB       = that.foreColorRGB;
        this.backColorRGB       = that.backColorRGB;
    }

    /**
     * Make human-readable description of this CellAttributes.
     *
     * @return displayable String
     */
    @Override
    public String toString() {
        if ((foreColorRGB >= 0) || (backColorRGB >= 0)) {
            return String.format("RGB: #%06x on #%06x",
                (foreColorRGB & 0xFFFFFF),
                (backColorRGB & 0xFFFFFF));
        }
        return String.format("%s%s%s on %s", (isBold() ? "bold " : ""),
            (isBlink() ? "blink " : ""), foreColor, backColor);
    }

}
