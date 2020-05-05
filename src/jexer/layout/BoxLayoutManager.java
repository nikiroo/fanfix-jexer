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

import java.util.ArrayList;

import jexer.TWidget;
import jexer.event.TResizeEvent;

/**
 * BoxLayoutManager repositions child widgets based on the order they are
 * added to the parent widget and desired orientation.
 */
public class BoxLayoutManager implements LayoutManager {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * If true, orient vertically.  If false, orient horizontally.
     */
    private boolean vertical = true;

    /**
     * Current width.
     */
    private int width = 0;

    /**
     * Current height.
     */
    private int height = 0;

    /**
     * Widgets being managed.
     */
    private ArrayList<TWidget> children = new ArrayList<TWidget>();

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param width the width of the parent widget
     * @param height the height of the parent widget
     * @param vertical if true, arrange widgets vertically
     */
    public BoxLayoutManager(final int width, final int height,
        final boolean vertical) {

        this.width = width;
        this.height = height;
        this.vertical = vertical;
    }

    // ------------------------------------------------------------------------
    // LayoutManager ----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Process the parent widget's resize event, and resize/reposition child
     * widgets.
     *
     * @param resize resize event
     */
    public void onResize(final TResizeEvent resize) {
        if (resize.getType() == TResizeEvent.Type.WIDGET) {
            width = resize.getWidth();
            height = resize.getHeight();
            layoutChildren();
        }
    }

    /**
     * Add a child widget to manage.
     *
     * @param child the widget to manage
     */
    public void add(final TWidget child) {
        children.add(child);
        layoutChildren();
    }

    /**
     * Remove a child widget from those managed by this LayoutManager.
     *
     * @param child the widget to remove
     */
    public void remove(final TWidget child) {
        children.remove(child);
        layoutChildren();
    }

    /**
     * Reset a child widget's original/preferred size.
     *
     * @param child the widget to manage
     */
    public void resetSize(final TWidget child) {
        // NOP
    }

    // ------------------------------------------------------------------------
    // BoxLayoutManager -------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Resize/reposition child widgets based on horizontal/vertical
     * arrangement.
     */
    private void layoutChildren() {
        if (children.size() == 0) {
            return;
        }
        if (vertical) {
            int widgetHeight = Math.max(1, height / children.size());
            int leftoverHeight = height % children.size();
            for (int i = 0; i < children.size() - 1; i++) {
                TWidget child = children.get(i);
                child.setDimensions(child.getX(), i * widgetHeight,
                    width, widgetHeight);
            }
            TWidget child = children.get(children.size() - 1);
            child.setDimensions(child.getX(),
                (children.size() - 1) * widgetHeight, width,
                widgetHeight + leftoverHeight);
        } else {
            int widgetWidth = Math.max(1, width / children.size());
            int leftoverWidth = width % children.size();
            for (int i = 0; i < children.size() - 1; i++) {
                TWidget child = children.get(i);
                child.setDimensions(i * widgetWidth, child.getY(),
                    widgetWidth, height);
            }
            TWidget child = children.get(children.size() - 1);
            child.setDimensions((children.size() - 1) * widgetWidth,
                child.getY(), widgetWidth + leftoverWidth, height);
        }
    }

}
