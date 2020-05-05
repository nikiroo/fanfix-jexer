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

/**
 * A TAction represents a simple action to perform in response to the user.
 *
 * @see TButton
 */
public abstract class TAction {

    /**
     * The widget that called this action's DO() method.  Note that this
     * field could be null, for example if executed as a timer action.
     */
    public TWidget source;

    /**
     * An optional bit of data associated with this action.
     */
    public Object data;

    /**
     * Call DO() with source widget set.
     *
     * @param source the source widget
     */
    public final void DO(final TWidget source) {
        this.source = source;
        DO();
    }

    /**
     * Call DO() with source widget and data set.
     *
     * @param source the source widget
     * @param data the data
     */
    public final void DO(final TWidget source, final Object data) {
        this.source = source;
        this.data = data;
        DO();
    }

    /**
     * Various classes will call DO() when they are clicked/selected.
     */
    public abstract void DO();
}
