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
package jexer.layout;

import jexer.TWidget;
import jexer.event.TResizeEvent;

/**
 * A LayoutManager provides automatic positioning and sizing of a TWidget's
 * child TWidgets.
 */
public interface LayoutManager {

    /**
     * Process the parent widget's resize event, and resize/reposition child
     * widgets.
     *
     * @param resize resize event
     */
    public void onResize(final TResizeEvent resize);

    /**
     * Add a child widget to manage.
     *
     * @param child the widget to manage
     */
    public void add(final TWidget child);

    /**
     * Remove a child widget from those managed by this LayoutManager.
     *
     * @param child the widget to remove
     */
    public void remove(final TWidget child);

    /**
     * Reset a child widget's original/preferred size.
     *
     * @param child the widget to manage
     */
    public void resetSize(final TWidget child);

}
