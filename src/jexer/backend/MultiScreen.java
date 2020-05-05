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

import java.util.ArrayList;
import java.util.List;

import jexer.bits.Cell;
import jexer.bits.CellAttributes;
import jexer.bits.Clipboard;

/**
 * MultiScreen mirrors its I/O to several screens.
 */
public class MultiScreen implements Screen {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The list of screens to use.
     */
    private List<Screen> screens = new ArrayList<Screen>();

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor requires one screen.
     *
     * @param screen the screen to add
     */
    public MultiScreen(final Screen screen) {
        screens.add(screen);
    }

    // ------------------------------------------------------------------------
    // Screen -----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Set drawing offset for x.
     *
     * @param offsetX new drawing offset
     */
    public void setOffsetX(final int offsetX) {
        for (Screen screen: screens) {
            screen.setOffsetX(offsetX);
        }
    }

    /**
     * Set drawing offset for y.
     *
     * @param offsetY new drawing offset
     */
    public void setOffsetY(final int offsetY) {
        for (Screen screen: screens) {
            screen.setOffsetY(offsetY);
        }
    }

    /**
     * Get right drawing clipping boundary.
     *
     * @return drawing boundary
     */
    public int getClipRight() {
        if (screens.size() > 0) {
            return screens.get(0).getClipRight();
        }
        return 0;
    }

    /**
     * Set right drawing clipping boundary.
     *
     * @param clipRight new boundary
     */
    public void setClipRight(final int clipRight) {
        for (Screen screen: screens) {
            screen.setClipRight(clipRight);
        }
    }

    /**
     * Get bottom drawing clipping boundary.
     *
     * @return drawing boundary
     */
    public int getClipBottom() {
        if (screens.size() > 0) {
            return screens.get(0).getClipBottom();
        }
        return 0;
    }

    /**
     * Set bottom drawing clipping boundary.
     *
     * @param clipBottom new boundary
     */
    public void setClipBottom(final int clipBottom) {
        for (Screen screen: screens) {
            screen.setClipBottom(clipBottom);
        }
    }

    /**
     * Get left drawing clipping boundary.
     *
     * @return drawing boundary
     */
    public int getClipLeft() {
        if (screens.size() > 0) {
            return screens.get(0).getClipLeft();
        }
        return 0;
    }

    /**
     * Set left drawing clipping boundary.
     *
     * @param clipLeft new boundary
     */
    public void setClipLeft(final int clipLeft) {
        for (Screen screen: screens) {
            screen.setClipLeft(clipLeft);
        }
    }

    /**
     * Get top drawing clipping boundary.
     *
     * @return drawing boundary
     */
    public int getClipTop() {
        if (screens.size() > 0) {
            return screens.get(0).getClipTop();
        }
        return 0;
    }

    /**
     * Set top drawing clipping boundary.
     *
     * @param clipTop new boundary
     */
    public void setClipTop(final int clipTop) {
        for (Screen screen: screens) {
            screen.setClipTop(clipTop);
        }
    }

    /**
     * Get dirty flag.
     *
     * @return if true, the logical screen is not in sync with the physical
     * screen
     */
    public boolean isDirty() {
        for (Screen screen: screens) {
            if (screen.isDirty()) {
                return true;
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
    public CellAttributes getAttrXY(final int x, final int y) {
        if (screens.size() > 0) {
            return screens.get(0).getAttrXY(x, y);
        }
        return new CellAttributes();
    }

    /**
     * Get the cell at one location.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @return the character + attributes
     */
    public Cell getCharXY(final int x, final int y) {
        if (screens.size() > 0) {
            return screens.get(0).getCharXY(x, y);
        }
        return new Cell();
    }

    /**
     * Set the attributes at one location.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    public void putAttrXY(final int x, final int y,
        final CellAttributes attr) {

        for (Screen screen: screens) {
            screen.putAttrXY(x, y, attr);
        }
    }

    /**
     * Set the attributes at one location.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param attr attributes to use (bold, foreColor, backColor)
     * @param clip if true, honor clipping/offset
     */
    public void putAttrXY(final int x, final int y,
        final CellAttributes attr, final boolean clip) {

        for (Screen screen: screens) {
            screen.putAttrXY(x, y, attr, clip);
        }
    }

    /**
     * Fill the entire screen with one character with attributes.
     *
     * @param ch character to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    public void putAll(final int ch, final CellAttributes attr) {
        for (Screen screen: screens) {
            screen.putAll(ch, attr);
        }
    }

    /**
     * Render one character with attributes.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param ch character + attributes to draw
     */
    public void putCharXY(final int x, final int y, final Cell ch) {
        for (Screen screen: screens) {
            screen.putCharXY(x, y, ch);
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
    public void putCharXY(final int x, final int y, final int ch,
        final CellAttributes attr) {

        for (Screen screen: screens) {
            screen.putCharXY(x, y, ch, attr);
        }
    }

    /**
     * Render one character without changing the underlying attributes.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param ch character to draw
     */
    public void putCharXY(final int x, final int y, final int ch) {
        for (Screen screen: screens) {
            screen.putCharXY(x, y, ch);
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
    public void putStringXY(final int x, final int y, final String str,
        final CellAttributes attr) {

        for (Screen screen: screens) {
            screen.putStringXY(x, y, str, attr);
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
    public void putStringXY(final int x, final int y, final String str) {
        for (Screen screen: screens) {
            screen.putStringXY(x, y, str);
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
    public void vLineXY(final int x, final int y, final int n,
        final int ch, final CellAttributes attr) {

        for (Screen screen: screens) {
            screen.vLineXY(x, y, n, ch, attr);
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
    public void hLineXY(final int x, final int y, final int n,
        final int ch, final CellAttributes attr) {

        for (Screen screen: screens) {
            screen.hLineXY(x, y, n, ch, attr);
        }
    }

    /**
     * Change the width.  Everything on-screen will be destroyed and must be
     * redrawn.
     *
     * @param width new screen width
     */
    public void setWidth(final int width) {
        for (Screen screen: screens) {
            screen.setWidth(width);
        }
    }

    /**
     * Change the height.  Everything on-screen will be destroyed and must be
     * redrawn.
     *
     * @param height new screen height
     */
    public void setHeight(final int height) {
        for (Screen screen: screens) {
            screen.setHeight(height);
        }
    }

    /**
     * Change the width and height.  Everything on-screen will be destroyed
     * and must be redrawn.
     *
     * @param width new screen width
     * @param height new screen height
     */
    public void setDimensions(final int width, final int height) {
        for (Screen screen: screens) {
            // Do not blindly call setDimension() on every screen.  Instead
            // call it only on those screens that do not already have the
            // requested dimension.  With this very small check, we have the
            // ability for ANY screen in the MultiBackend to resize ALL of
            // the screens.
            if ((screen.getWidth() != width)
                || (screen.getHeight() != height)
            ) {
                screen.setDimensions(width, height);
            } else {
                // The screen that didn't change is probably the one that
                // prompted the resize.  Force it to repaint.
                screen.clearPhysical();
            }
        }
    }

    /**
     * Get the height.
     *
     * @return current screen height
     */
    public int getHeight() {
        // Return the smallest height of the screens.
        int height = 25;
        if (screens.size() > 0) {
            height = screens.get(0).getHeight();
        }
        for (Screen screen: screens) {
            if (screen.getHeight() < height) {
                height = screen.getHeight();
            }
        }
        return height;
    }

    /**
     * Get the width.
     *
     * @return current screen width
     */
    public int getWidth() {
        // Return the smallest width of the screens.
        int width = 80;
        if (screens.size() > 0) {
            width = screens.get(0).getWidth();
        }
        for (Screen screen: screens) {
            if (screen.getWidth() < width) {
                width = screen.getWidth();
            }
        }
        return width;
    }

    /**
     * Reset screen to not-bold, white-on-black.  Also flushes the offset and
     * clip variables.
     */
    public void reset() {
        for (Screen screen: screens) {
            screen.reset();
        }
    }

    /**
     * Flush the offset and clip variables.
     */
    public void resetClipping() {
        for (Screen screen: screens) {
            screen.resetClipping();
        }
    }

    /**
     * Clear the logical screen.
     */
    public void clear() {
        for (Screen screen: screens) {
            screen.clear();
        }
    }

    /**
     * Draw a box with a border and empty background.
     *
     * @param left left column of box.  0 is the left-most row.
     * @param top top row of the box.  0 is the top-most row.
     * @param right right column of box
     * @param bottom bottom row of the box
     * @param border attributes to use for the border
     * @param background attributes to use for the background
     */
    public void drawBox(final int left, final int top,
        final int right, final int bottom,
        final CellAttributes border, final CellAttributes background) {

        for (Screen screen: screens) {
            screen.drawBox(left, top, right, bottom, border, background);
        }
    }

    /**
     * Draw a box with a border and empty background.
     *
     * @param left left column of box.  0 is the left-most row.
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
    public void drawBox(final int left, final int top,
        final int right, final int bottom,
        final CellAttributes border, final CellAttributes background,
        final int borderType, final boolean shadow) {

        for (Screen screen: screens) {
            screen.drawBox(left, top, right, bottom, border, background,
                borderType, shadow);
        }
    }

    /**
     * Draw a box shadow.
     *
     * @param left left column of box.  0 is the left-most row.
     * @param top top row of the box.  0 is the top-most row.
     * @param right right column of box
     * @param bottom bottom row of the box
     */
    public void drawBoxShadow(final int left, final int top,
        final int right, final int bottom) {

        for (Screen screen: screens) {
            screen.drawBoxShadow(left, top, right, bottom);
        }
    }

    /**
     * Clear the physical screen.
     */
    public void clearPhysical() {
        for (Screen screen: screens) {
            screen.clearPhysical();
        }
    }

    /**
     * Unset every image cell on one row of the physical screen, forcing
     * images on that row to be redrawn.
     *
     * @param y row coordinate.  0 is the top-most row.
     */
    public final void unsetImageRow(final int y) {
        for (Screen screen: screens) {
            screen.unsetImageRow(y);
        }
    }

    /**
     * Classes must provide an implementation to push the logical screen to
     * the physical device.
     */
    public void flushPhysical() {
        for (Screen screen: screens) {
            screen.flushPhysical();
        }
    }

    /**
     * Put the cursor at (x,y).
     *
     * @param visible if true, the cursor should be visible
     * @param x column coordinate to put the cursor on
     * @param y row coordinate to put the cursor on
     */
    public void putCursor(final boolean visible, final int x, final int y) {
        for (Screen screen: screens) {
            screen.putCursor(visible, x, y);
        }
    }

    /**
     * Hide the cursor.
     */
    public void hideCursor() {
        for (Screen screen: screens) {
            screen.hideCursor();
        }
    }

    /**
     * Get the cursor visibility.
     *
     * @return true if the cursor is visible
     */
    public boolean isCursorVisible() {
        if (screens.size() > 0) {
            return screens.get(0).isCursorVisible();
        }
        return true;
    }

    /**
     * Get the cursor X position.
     *
     * @return the cursor x column position
     */
    public int getCursorX() {
        if (screens.size() > 0) {
            return screens.get(0).getCursorX();
        }
        return 0;
    }

    /**
     * Get the cursor Y position.
     *
     * @return the cursor y row position
     */
    public int getCursorY() {
        if (screens.size() > 0) {
            return screens.get(0).getCursorY();
        }
        return 0;
    }

    /**
     * Set the window title.
     *
     * @param title the new title
     */
    public void setTitle(final String title) {
        for (Screen screen: screens) {
            screen.setTitle(title);
        }
    }

    // ------------------------------------------------------------------------
    // MultiScreen ------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Add a screen to the list.
     *
     * @param screen the screen to add
     */
    public void addScreen(final Screen screen) {
        screens.add(screen);
    }

    /**
     * Remove a screen from the list.
     *
     * @param screen the screen to remove
     */
    public void removeScreen(final Screen screen) {
        if (screens.size() > 1) {
            screens.remove(screen);
        }
    }

    /**
     * Get the width of a character cell in pixels.
     *
     * @return the width in pixels of a character cell
     */
    public int getTextWidth() {
        int textWidth = 16;
        for (Screen screen: screens) {
            int newTextWidth = screen.getTextWidth();
            if (newTextWidth < textWidth) {
                textWidth = newTextWidth;
            }
        }
        return textWidth;
    }

    /**
     * Get the height of a character cell in pixels.
     *
     * @return the height in pixels of a character cell
     */
    public int getTextHeight() {
        int textHeight = 20;
        for (Screen screen: screens) {
            int newTextHeight = screen.getTextHeight();
            if (newTextHeight < textHeight) {
                textHeight = newTextHeight;
            }
        }
        return textHeight;
    }

    /**
     * Invert the cell color at a position, including both halves of a
     * double-width cell.
     *
     * @param x column position
     * @param y row position
     */
    public void invertCell(final int x, final int y) {
        for (Screen screen: screens) {
            screen.invertCell(x, y);
        }
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

        for (Screen screen: screens) {
            screen.invertCell(x, y, onlyThisCell);
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

        for (Screen screen: screens) {
            screen.setSelection(x0, y0, x1, y1, rectangle);
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

        // Only copy from the first screen.
        if (screens.size() > 0) {
            screens.get(0).copySelection(clipboard, x0, y0, x1, y1, rectangle);
        }
    }

}
