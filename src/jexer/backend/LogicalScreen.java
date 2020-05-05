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

import java.awt.image.BufferedImage;

import jexer.backend.GlyphMaker;
import jexer.bits.Cell;
import jexer.bits.CellAttributes;
import jexer.bits.Clipboard;
import jexer.bits.GraphicsChars;
import jexer.bits.StringUtils;

/**
 * A logical screen composed of a 2D array of Cells.
 */
public class LogicalScreen implements Screen {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Width of the visible window.
     */
    protected int width;

    /**
     * Height of the visible window.
     */
    protected int height;

    /**
     * Drawing offset for x.
     */
    private int offsetX;

    /**
     * Drawing offset for y.
     */
    private int offsetY;

    /**
     * Ignore anything drawn right of clipRight.
     */
    private int clipRight;

    /**
     * Ignore anything drawn below clipBottom.
     */
    private int clipBottom;

    /**
     * Ignore anything drawn left of clipLeft.
     */
    private int clipLeft;

    /**
     * Ignore anything drawn above clipTop.
     */
    private int clipTop;

    /**
     * The physical screen last sent out on flush().
     */
    protected Cell [][] physical;

    /**
     * The logical screen being rendered to.
     */
    protected Cell [][] logical;

    /**
     * Set if the user explicitly wants to redraw everything starting with a
     * ECMATerminal.clearAll().
     */
    protected boolean reallyCleared;

    /**
     * If true, the cursor is visible and should be placed onscreen at
     * (cursorX, cursorY) during a call to flushPhysical().
     */
    protected boolean cursorVisible;

    /**
     * Cursor X position if visible.
     */
    protected int cursorX;

    /**
     * Cursor Y position if visible.
     */
    protected int cursorY;

    /**
     * The last used height of a character cell in pixels, only used for
     * full-width chars.
     */
    private int lastTextHeight = -1;

    /**
     * The glyph drawer for full-width chars.
     */
    private GlyphMaker glyphMaker = null;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.  Sets everything to not-bold, white-on-black.
     */
    protected LogicalScreen() {
        offsetX  = 0;
        offsetY  = 0;
        width    = 80;
        height   = 24;
        logical  = null;
        physical = null;
        reallocate(width, height);
    }

    // ------------------------------------------------------------------------
    // Screen -----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the width of a character cell in pixels.
     *
     * @return the width in pixels of a character cell
     */
    public int getTextWidth() {
        // Default width is 16 pixels.
        return 16;
    }

    /**
     * Get the height of a character cell in pixels.
     *
     * @return the height in pixels of a character cell
     */
    public int getTextHeight() {
        // Default height is 20 pixels.
        return 20;
    }

    /**
     * Set drawing offset for x.
     *
     * @param offsetX new drawing offset
     */
    public final void setOffsetX(final int offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * Set drawing offset for y.
     *
     * @param offsetY new drawing offset
     */
    public final void setOffsetY(final int offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * Get right drawing clipping boundary.
     *
     * @return drawing boundary
     */
    public final int getClipRight() {
        return clipRight;
    }

    /**
     * Set right drawing clipping boundary.
     *
     * @param clipRight new boundary
     */
    public final void setClipRight(final int clipRight) {
        this.clipRight = clipRight;
    }

    /**
     * Get bottom drawing clipping boundary.
     *
     * @return drawing boundary
     */
    public final int getClipBottom() {
        return clipBottom;
    }

    /**
     * Set bottom drawing clipping boundary.
     *
     * @param clipBottom new boundary
     */
    public final void setClipBottom(final int clipBottom) {
        this.clipBottom = clipBottom;
    }

    /**
     * Get left drawing clipping boundary.
     *
     * @return drawing boundary
     */
    public final int getClipLeft() {
        return clipLeft;
    }

    /**
     * Set left drawing clipping boundary.
     *
     * @param clipLeft new boundary
     */
    public final void setClipLeft(final int clipLeft) {
        this.clipLeft = clipLeft;
    }

    /**
     * Get top drawing clipping boundary.
     *
     * @return drawing boundary
     */
    public final int getClipTop() {
        return clipTop;
    }

    /**
     * Set top drawing clipping boundary.
     *
     * @param clipTop new boundary
     */
    public final void setClipTop(final int clipTop) {
        this.clipTop = clipTop;
    }

    /**
     * Get dirty flag.
     *
     * @return if true, the logical screen is not in sync with the physical
     * screen
     */
    public final boolean isDirty() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!logical[x][y].equals(physical[x][y])) {
                    return true;
                }
                if (logical[x][y].isBlink()) {
                    // Blinking screens are always dirty.  There is
                    // opportunity for a Netscape blink tag joke here...
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the attributes at one location.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @return attributes at (x, y)
     */
    public final CellAttributes getAttrXY(final int x, final int y) {
        CellAttributes attr = new CellAttributes();
        if ((x >= 0) && (x < width) && (y >= 0) && (y < height)) {
            attr.setTo(logical[x][y]);
        }
        return attr;
    }

    /**
     * Get the cell at one location.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @return the character + attributes
     */
    public Cell getCharXY(final int x, final int y) {
        Cell cell = new Cell();
        if ((x >= 0) && (x < width) && (y >= 0) && (y < height)) {
            cell.setTo(logical[x][y]);
        }
        return cell;
    }

    /**
     * Set the attributes at one location.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    public final void putAttrXY(final int x, final int y,
        final CellAttributes attr) {

        putAttrXY(x, y, attr, true);
    }

    /**
     * Set the attributes at one location.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param attr attributes to use (bold, foreColor, backColor)
     * @param clip if true, honor clipping/offset
     */
    public final void putAttrXY(final int x, final int y,
        final CellAttributes attr, final boolean clip) {

        int X = x;
        int Y = y;

        if (clip) {
            if ((x < clipLeft)
                || (x >= clipRight)
                || (y < clipTop)
                || (y >= clipBottom)
            ) {
                return;
            }
            X += offsetX;
            Y += offsetY;
        }

        if ((X >= 0) && (X < width) && (Y >= 0) && (Y < height)) {
            logical[X][Y].setTo(attr);

            // If this happens to be the cursor position, make the position
            // dirty.
            if ((cursorX == X) && (cursorY == Y)) {
                physical[cursorX][cursorY].unset();
                unsetImageRow(cursorY);
            }
        }
    }

    /**
     * Fill the entire screen with one character with attributes.
     *
     * @param ch character to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    public final void putAll(final int ch, final CellAttributes attr) {

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                putCharXY(x, y, ch, attr);
            }
        }
    }

    /**
     * Render one character with attributes.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param ch character + attributes to draw
     */
    public final void putCharXY(final int x, final int y, final Cell ch) {
        if ((x < clipLeft)
            || (x >= clipRight)
            || (y < clipTop)
            || (y >= clipBottom)
        ) {
            return;
        }

        if ((StringUtils.width(ch.getChar()) == 2) && (!ch.isImage())) {
            putFullwidthCharXY(x, y, ch);
            return;
        }

        int X = x + offsetX;
        int Y = y + offsetY;

        // System.err.printf("putCharXY: %d, %d, %c\n", X, Y, ch);

        if ((X >= 0) && (X < width) && (Y >= 0) && (Y < height)) {

            // Do not put control characters on the display
            if (!ch.isImage()) {
                assert (ch.getChar() >= 0x20);
                assert (ch.getChar() != 0x7F);
            }
            logical[X][Y].setTo(ch);

            // If this happens to be the cursor position, make the position
            // dirty.
            if ((cursorX == X) && (cursorY == Y)) {
                physical[cursorX][cursorY].unset();
                unsetImageRow(cursorY);
            }
        }
    }

    /**
     * Render one character with attributes.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param ch character to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    public final void putCharXY(final int x, final int y, final int ch,
        final CellAttributes attr) {

        if ((x < clipLeft)
            || (x >= clipRight)
            || (y < clipTop)
            || (y >= clipBottom)
        ) {
            return;
        }

        if (StringUtils.width(ch) == 2) {
            putFullwidthCharXY(x, y, ch, attr);
            return;
        }

        int X = x + offsetX;
        int Y = y + offsetY;

        // System.err.printf("putCharXY: %d, %d, %c\n", X, Y, ch);

        if ((X >= 0) && (X < width) && (Y >= 0) && (Y < height)) {

            // Do not put control characters on the display
            assert (ch >= 0x20);
            assert (ch != 0x7F);

            logical[X][Y].setTo(attr);
            logical[X][Y].setChar(ch);

            // If this happens to be the cursor position, make the position
            // dirty.
            if ((cursorX == X) && (cursorY == Y)) {
                physical[cursorX][cursorY].unset();
                unsetImageRow(cursorY);
            }
        }
    }

    /**
     * Render one character without changing the underlying attributes.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param ch character to draw
     */
    public final void putCharXY(final int x, final int y, final int ch) {
        if ((x < clipLeft)
            || (x >= clipRight)
            || (y < clipTop)
            || (y >= clipBottom)
        ) {
            return;
        }

        if (StringUtils.width(ch) == 2) {
            putFullwidthCharXY(x, y, ch);
            return;
        }

        int X = x + offsetX;
        int Y = y + offsetY;

        // System.err.printf("putCharXY: %d, %d, %c\n", X, Y, ch);

        if ((X >= 0) && (X < width) && (Y >= 0) && (Y < height)) {
            logical[X][Y].setChar(ch);

            // If this happens to be the cursor position, make the position
            // dirty.
            if ((cursorX == X) && (cursorY == Y)) {
                physical[cursorX][cursorY].unset();
                unsetImageRow(cursorY);
            }
        }
    }

    /**
     * Render a string.  Does not wrap if the string exceeds the line.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param str string to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    public final void putStringXY(final int x, final int y, final String str,
        final CellAttributes attr) {

        int i = x;
        for (int j = 0; j < str.length();) {
            int ch = str.codePointAt(j);
            j += Character.charCount(ch);
            putCharXY(i, y, ch, attr);
            i += StringUtils.width(ch);
            if (i == width) {
                break;
            }
        }
    }

    /**
     * Render a string without changing the underlying attribute.  Does not
     * wrap if the string exceeds the line.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param str string to draw
     */
    public final void putStringXY(final int x, final int y, final String str) {

        int i = x;
        for (int j = 0; j < str.length();) {
            int ch = str.codePointAt(j);
            j += Character.charCount(ch);
            putCharXY(i, y, ch);
            i += StringUtils.width(ch);
            if (i == width) {
                break;
            }
        }
    }

    /**
     * Draw a vertical line from (x, y) to (x, y + n).
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param n number of characters to draw
     * @param ch character to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    public final void vLineXY(final int x, final int y, final int n,
        final int ch, final CellAttributes attr) {

        for (int i = y; i < y + n; i++) {
            putCharXY(x, i, ch, attr);
        }
    }

    /**
     * Draw a horizontal line from (x, y) to (x + n, y).
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param n number of characters to draw
     * @param ch character to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    public final void hLineXY(final int x, final int y, final int n,
        final int ch, final CellAttributes attr) {

        for (int i = x; i < x + n; i++) {
            putCharXY(i, y, ch, attr);
        }
    }

    /**
     * Change the width.  Everything on-screen will be destroyed and must be
     * redrawn.
     *
     * @param width new screen width
     */
    public final synchronized void setWidth(final int width) {
        reallocate(width, this.height);
    }

    /**
     * Change the height.  Everything on-screen will be destroyed and must be
     * redrawn.
     *
     * @param height new screen height
     */
    public final synchronized void setHeight(final int height) {
        reallocate(this.width, height);
    }

    /**
     * Change the width and height.  Everything on-screen will be destroyed
     * and must be redrawn.
     *
     * @param width new screen width
     * @param height new screen height
     */
    public final void setDimensions(final int width, final int height) {
        reallocate(width, height);
        resizeToScreen();
    }

    /**
     * Resize the physical screen to match the logical screen dimensions.
     */
    public void resizeToScreen() {
        // Subclasses are expected to override this.
    }

    /**
     * Get the height.
     *
     * @return current screen height
     */
    public final synchronized int getHeight() {
        return this.height;
    }

    /**
     * Get the width.
     *
     * @return current screen width
     */
    public final synchronized int getWidth() {
        return this.width;
    }

    /**
     * Reset screen to not-bold, white-on-black.  Also flushes the offset and
     * clip variables.
     */
    public final synchronized void reset() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                logical[col][row].reset();
            }
        }
        resetClipping();
    }

    /**
     * Flush the offset and clip variables.
     */
    public final void resetClipping() {
        offsetX    = 0;
        offsetY    = 0;
        clipLeft   = 0;
        clipTop    = 0;
        clipRight  = width;
        clipBottom = height;
    }

    /**
     * Clear the logical screen.
     */
    public final void clear() {
        reset();
    }

    /**
     * Draw a box with a border and empty background.
     *
     * @param left left column of box.  0 is the left-most column.
     * @param top top row of the box.  0 is the top-most row.
     * @param right right column of box
     * @param bottom bottom row of the box
     * @param border attributes to use for the border
     * @param background attributes to use for the background
     */
    public final void drawBox(final int left, final int top,
        final int right, final int bottom,
        final CellAttributes border, final CellAttributes background) {

        drawBox(left, top, right, bottom, border, background, 1, false);
    }

    /**
     * Draw a box with a border and empty background.
     *
     * @param left left column of box.  0 is the left-most column.
     * @param top top row of the box.  0 is the top-most row.
     * @param right right column of box
     * @param bottom bottom row of the box
     * @param border attributes to use for the border
     * @param background attributes to use for the background
     * @param borderType if 1, draw a single-line border; if 2, draw a
     * double-line border; if 3, draw double-line top/bottom edges and
     * single-line left/right edges (like Qmodem)
     * @param shadow if true, draw a "shadow" on the box
     */
    public final void drawBox(final int left, final int top,
        final int right, final int bottom,
        final CellAttributes border, final CellAttributes background,
        final int borderType, final boolean shadow) {

        int boxWidth = right - left;
        int boxHeight = bottom - top;

        char cTopLeft;
        char cTopRight;
        char cBottomLeft;
        char cBottomRight;
        char cHSide;
        char cVSide;

        switch (borderType) {
        case 1:
            cTopLeft = GraphicsChars.ULCORNER;
            cTopRight = GraphicsChars.URCORNER;
            cBottomLeft = GraphicsChars.LLCORNER;
            cBottomRight = GraphicsChars.LRCORNER;
            cHSide = GraphicsChars.SINGLE_BAR;
            cVSide = GraphicsChars.WINDOW_SIDE;
            break;

        case 2:
            cTopLeft = GraphicsChars.WINDOW_LEFT_TOP_DOUBLE;
            cTopRight = GraphicsChars.WINDOW_RIGHT_TOP_DOUBLE;
            cBottomLeft = GraphicsChars.WINDOW_LEFT_BOTTOM_DOUBLE;
            cBottomRight = GraphicsChars.WINDOW_RIGHT_BOTTOM_DOUBLE;
            cHSide = GraphicsChars.DOUBLE_BAR;
            cVSide = GraphicsChars.WINDOW_SIDE_DOUBLE;
            break;

        case 3:
            cTopLeft = GraphicsChars.WINDOW_LEFT_TOP;
            cTopRight = GraphicsChars.WINDOW_RIGHT_TOP;
            cBottomLeft = GraphicsChars.WINDOW_LEFT_BOTTOM;
            cBottomRight = GraphicsChars.WINDOW_RIGHT_BOTTOM;
            cHSide = GraphicsChars.WINDOW_TOP;
            cVSide = GraphicsChars.WINDOW_SIDE;
            break;
        default:
            throw new IllegalArgumentException("Invalid border type: "
                + borderType);
        }

        // Place the corner characters
        putCharXY(left, top, cTopLeft, border);
        putCharXY(left + boxWidth - 1, top, cTopRight, border);
        putCharXY(left, top + boxHeight - 1, cBottomLeft, border);
        putCharXY(left + boxWidth - 1, top + boxHeight - 1, cBottomRight,
            border);

        // Draw the box lines
        hLineXY(left + 1, top, boxWidth - 2, cHSide, border);
        vLineXY(left, top + 1, boxHeight - 2, cVSide, border);
        hLineXY(left + 1, top + boxHeight - 1, boxWidth - 2, cHSide, border);
        vLineXY(left + boxWidth - 1, top + 1, boxHeight - 2, cVSide, border);

        // Fill in the interior background
        for (int i = 1; i < boxHeight - 1; i++) {
            hLineXY(1 + left, i + top, boxWidth - 2, ' ', background);
        }

        if (shadow) {
            // Draw a shadow
            drawBoxShadow(left, top, right, bottom);
        }
    }

    /**
     * Draw a box shadow.
     *
     * @param left left column of box.  0 is the left-most column.
     * @param top top row of the box.  0 is the top-most row.
     * @param right right column of box
     * @param bottom bottom row of the box
     */
    public final void drawBoxShadow(final int left, final int top,
        final int right, final int bottom) {

        int boxTop = top;
        int boxLeft = left;
        int boxWidth = right - left;
        int boxHeight = bottom - top;
        CellAttributes shadowAttr = new CellAttributes();

        // Shadows do not honor clipping but they DO honor offset.
        int oldClipRight = clipRight;
        int oldClipBottom = clipBottom;
        // When offsetX or offsetY go negative, we need to increase the clip
        // bounds.
        clipRight = width - offsetX;
        clipBottom = height - offsetY;

        for (int i = 0; i < boxHeight; i++) {
            Cell cell = getCharXY(offsetX + boxLeft + boxWidth,
                offsetY + boxTop + 1 + i);
            if (cell.getWidth() == Cell.Width.SINGLE) {
                putAttrXY(boxLeft + boxWidth, boxTop + 1 + i, shadowAttr);
            } else {
                putCharXY(boxLeft + boxWidth, boxTop + 1 + i, ' ', shadowAttr);
            }
            cell = getCharXY(offsetX + boxLeft + boxWidth + 1,
                offsetY + boxTop + 1 + i);
            if (cell.getWidth() == Cell.Width.SINGLE) {
                putAttrXY(boxLeft + boxWidth + 1, boxTop + 1 + i, shadowAttr);
            } else {
                putCharXY(boxLeft + boxWidth + 1, boxTop + 1 + i, ' ',
                    shadowAttr);
            }
        }
        for (int i = 0; i < boxWidth; i++) {
            Cell cell = getCharXY(offsetX + boxLeft + 2 + i,
                offsetY + boxTop + boxHeight);
            if (cell.getWidth() == Cell.Width.SINGLE) {
                putAttrXY(boxLeft + 2 + i, boxTop + boxHeight, shadowAttr);
            } else {
                putCharXY(boxLeft + 2 + i, boxTop + boxHeight, ' ', shadowAttr);
            }
        }
        clipRight = oldClipRight;
        clipBottom = oldClipBottom;
    }

    /**
     * Default implementation does nothing.
     */
    public void flushPhysical() {}

    /**
     * Put the cursor at (x,y).
     *
     * @param visible if true, the cursor should be visible
     * @param x column coordinate to put the cursor on
     * @param y row coordinate to put the cursor on
     */
    public void putCursor(final boolean visible, final int x, final int y) {
        if ((cursorY >= 0)
            && (cursorX >= 0)
            && (cursorY <= height - 1)
            && (cursorX <= width - 1)
        ) {
            // Make the current cursor position dirty
            physical[cursorX][cursorY].unset();
            unsetImageRow(cursorY);
        }

        cursorVisible = visible;
        cursorX = x;
        cursorY = y;
    }

    /**
     * Hide the cursor.
     */
    public final void hideCursor() {
        cursorVisible = false;
    }

    /**
     * Get the cursor visibility.
     *
     * @return true if the cursor is visible
     */
    public boolean isCursorVisible() {
        return cursorVisible;
    }

    /**
     * Get the cursor X position.
     *
     * @return the cursor x column position
     */
    public int getCursorX() {
        return cursorX;
    }

    /**
     * Get the cursor Y position.
     *
     * @return the cursor y row position
     */
    public int getCursorY() {
        return cursorY;
    }

    /**
     * Set the window title.  Default implementation does nothing.
     *
     * @param title the new title
     */
    public void setTitle(final String title) {}

    // ------------------------------------------------------------------------
    // LogicalScreen ----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Reallocate screen buffers.
     *
     * @param width new width
     * @param height new height
     */
    private synchronized void reallocate(final int width, final int height) {
        if (logical != null) {
            for (int row = 0; row < this.height; row++) {
                for (int col = 0; col < this.width; col++) {
                    logical[col][row] = null;
                }
            }
            logical = null;
        }
        logical = new Cell[width][height];
        if (physical != null) {
            for (int row = 0; row < this.height; row++) {
                for (int col = 0; col < this.width; col++) {
                    physical[col][row] = null;
                }
            }
            physical = null;
        }
        physical = new Cell[width][height];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                physical[col][row] = new Cell();
                logical[col][row] = new Cell();
            }
        }

        this.width = width;
        this.height = height;

        clipLeft = 0;
        clipTop = 0;
        clipRight = width;
        clipBottom = height;

        reallyCleared = true;
    }

    /**
     * Clear the physical screen.
     */
    public final void clearPhysical() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                physical[col][row].unset();
            }
        }
    }

    /**
     * Unset every image cell on one row of the physical screen, forcing
     * images on that row to be redrawn.
     *
     * @param y row coordinate.  0 is the top-most row.
     */
    public final void unsetImageRow(final int y) {
        if ((y < 0) || (y >= height)) {
            return;
        }
        for (int x = 0; x < width; x++) {
            if (logical[x][y].isImage()) {
                physical[x][y].unset();
            }
        }
    }

    /**
     * Render one fullwidth cell.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param cell the cell to draw
     */
    public final void putFullwidthCharXY(final int x, final int y,
        final Cell cell) {

        int cellWidth = getTextWidth();
        int cellHeight = getTextHeight();

        if (lastTextHeight != cellHeight) {
            glyphMaker = GlyphMaker.getInstance(cellHeight);
            lastTextHeight = cellHeight;
        }
        BufferedImage image = glyphMaker.getImage(cell, cellWidth * 2,
            cellHeight);
        BufferedImage leftImage = image.getSubimage(0, 0, cellWidth,
            cellHeight);
        BufferedImage rightImage = image.getSubimage(cellWidth, 0, cellWidth,
            cellHeight);

        Cell left = new Cell(cell);
        left.setImage(leftImage);
        left.setWidth(Cell.Width.LEFT);
        putCharXY(x, y, left);

        Cell right = new Cell(cell);
        right.setImage(rightImage);
        right.setWidth(Cell.Width.RIGHT);
        putCharXY(x + 1, y, right);
    }

    /**
     * Render one fullwidth character with attributes.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param ch character to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    public final void putFullwidthCharXY(final int x, final int y,
        final int ch, final CellAttributes attr) {

        Cell cell = new Cell(ch, attr);
        putFullwidthCharXY(x, y, cell);
    }

    /**
     * Render one fullwidth character with attributes.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param ch character to draw
     */
    public final void putFullwidthCharXY(final int x, final int y,
        final int ch) {

        Cell cell = new Cell(ch);
        cell.setAttr(getAttrXY(x, y));
        putFullwidthCharXY(x, y, cell);
    }

    /**
     * Invert the cell color at a position, including both halves of a
     * double-width cell.
     *
     * @param x column position
     * @param y row position
     */
    public void invertCell(final int x, final int y) {
        invertCell(x, y, false);
    }

    /**
     * Invert the cell color at a position.
     *
     * @param x column position
     * @param y row position
     * @param onlyThisCell if true, only invert this cell, otherwise invert
     * both halves of a double-width cell if necessary
     */
    public void invertCell(final int x, final int y,
        final boolean onlyThisCell) {

        Cell cell = getCharXY(x, y);
        if (cell.isImage()) {
            cell.invertImage();
        }
        if (cell.getForeColorRGB() < 0) {
            cell.setForeColor(cell.getForeColor().invert());
        } else {
            cell.setForeColorRGB(cell.getForeColorRGB() ^ 0x00ffffff);
        }
        if (cell.getBackColorRGB() < 0) {
            cell.setBackColor(cell.getBackColor().invert());
        } else {
            cell.setBackColorRGB(cell.getBackColorRGB() ^ 0x00ffffff);
        }
        putCharXY(x, y, cell);
        if ((onlyThisCell == true) || (cell.getWidth() == Cell.Width.SINGLE)) {
            return;
        }

        // This cell is one half of a fullwidth glyph.  Invert the other
        // half.
        if (cell.getWidth() == Cell.Width.LEFT) {
            if (x < width - 1) {
                Cell rightHalf = getCharXY(x + 1, y);
                if (rightHalf.getWidth() == Cell.Width.RIGHT) {
                    invertCell(x + 1, y, true);
                    return;
                }
            }
        }
        if (cell.getWidth() == Cell.Width.RIGHT) {
            if (x > 0) {
                Cell leftHalf = getCharXY(x - 1, y);
                if (leftHalf.getWidth() == Cell.Width.LEFT) {
                    invertCell(x - 1, y, true);
                }
            }
        }
    }

    /**
     * Set a selection area on the screen.
     *
     * @param x0 the starting X position of the selection
     * @param y0 the starting Y position of the selection
     * @param x1 the ending X position of the selection
     * @param y1 the ending Y position of the selection
     * @param rectangle if true, this is a rectangle select
     */
    public void setSelection(final int x0, final int y0,
        final int x1, final int y1, final boolean rectangle) {

        int startX = x0;
        int startY = y0;
        int endX = x1;
        int endY = y1;

        if (((x1 < x0) && (y1 == y0))
            || (y1 < y0)
        ) {
            // The user dragged from bottom-to-top and/or right-to-left.
            // Reverse the coordinates for the inverted section.
            startX = x1;
            startY = y1;
            endX = x0;
            endY = y0;
        }
        if (rectangle) {
            for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    invertCell(x, y);
                }
            }
        } else {
            if (endY > startY) {
                for (int x = startX; x < width; x++) {
                    invertCell(x, startY);
                }
                for (int y = startY + 1; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        invertCell(x, y);
                    }
                }
                for (int x = 0; x <= endX; x++) {
                    invertCell(x, endY);
                }
            } else {
                assert (startY == endY);
                for (int x = startX; x <= endX; x++) {
                    invertCell(x, startY);
                }
            }
        }
    }

    /**
     * Copy the screen selection area to the clipboard.
     *
     * @param clipboard the clipboard to use
     * @param x0 the starting X position of the selection
     * @param y0 the starting Y position of the selection
     * @param x1 the ending X position of the selection
     * @param y1 the ending Y position of the selection
     * @param rectangle if true, this is a rectangle select
     */
    public void copySelection(final Clipboard clipboard,
        final int x0, final int y0, final int x1, final int y1,
        final boolean rectangle) {

        StringBuilder sb = new StringBuilder();

        int startX = x0;
        int startY = y0;
        int endX = x1;
        int endY = y1;

        if (((x1 < x0) && (y1 == y0))
            || (y1 < y0)
        ) {
            // The user dragged from bottom-to-top and/or right-to-left.
            // Reverse the coordinates for the inverted section.
            startX = x1;
            startY = y1;
            endX = x0;
            endY = y0;
        }
        if (rectangle) {
            for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    sb.append(Character.toChars(getCharXY(x, y).getChar()));
                }
                sb.append("\n");
            }
        } else {
            if (endY > startY) {
                for (int x = startX; x < width; x++) {
                    sb.append(Character.toChars(getCharXY(x, startY).getChar()));
                }
                sb.append("\n");
                for (int y = startY + 1; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        sb.append(Character.toChars(getCharXY(x, y).getChar()));
                    }
                    sb.append("\n");
                }
                for (int x = 0; x <= endX; x++) {
                    sb.append(Character.toChars(getCharXY(x, endY).getChar()));
                }
            } else {
                assert (startY == endY);
                for (int x = startX; x <= endX; x++) {
                    sb.append(Character.toChars(getCharXY(x, startY).getChar()));
                }
            }
        }
        clipboard.copyText(sb.toString());
    }

}
