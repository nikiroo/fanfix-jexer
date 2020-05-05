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
package jexer.event;

/**
 * This class encapsulates a screen or window resize event.
 */
public class TResizeEvent extends TInputEvent {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Resize events can be generated for either a total screen resize or a
     * widget/window resize.
     */
    public enum Type {
        /**
         * The entire screen size changed.
         */
        SCREEN,

        /**
         * A widget was resized.
         */
        WIDGET
    }

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The type of resize.
     */
    private Type type;

    /**
     * New width.
     */
    private int width;

    /**
     * New height.
     */
    private int height;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public contructor.
     *
     * @param type the Type of resize, Screen or Widget
     * @param width the new width
     * @param height the new height
     */
    public TResizeEvent(final Type type, final int width, final int height) {
        this.type   = type;
        this.width  = width;
        this.height = height;
    }

    // ------------------------------------------------------------------------
    // TResizeEvent -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get resize type.
     *
     * @return SCREEN or WIDGET
     */
    public Type getType() {
        return type;
    }

    /**
     * Get the new width.
     *
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the new height.
     *
     * @return height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Make human-readable description of this TResizeEvent.
     *
     * @return displayable String
     */
    @Override
    public String toString() {
        return String.format("Resize: %s width = %d height = %d",
            type, width, height);
    }

}
