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

import java.awt.Rectangle;
import java.util.HashMap;

import jexer.TWidget;
import jexer.event.TResizeEvent;

/**
 * StretchLayoutManager repositions child widgets based on their coordinates
 * when added and the current widget size.
 */
public class StretchLayoutManager implements LayoutManager {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Current width.
     */
    private int width = 0;

    /**
     * Current height.
     */
    private int height = 0;

    /**
     * Original width.
     */
    private int originalWidth = 0;

    /**
     * Original height.
     */
    private int originalHeight = 0;

    /**
     * Map of widget to original dimensions.
     */
    private HashMap<TWidget, Rectangle> children = new HashMap<TWidget, Rectangle>();

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param width the width of the parent widget
     * @param height the height of the parent widget
     */
    public StretchLayoutManager(final int width, final int height) {
        originalWidth = width;
        originalHeight = height;
        this.width = width;
        this.height = height;
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
        Rectangle rect = new Rectangle(child.getX(), child.getY(),
            child.getWidth(), child.getHeight());
        children.put(child, rect);
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
        // For this layout, adding is the same as replacing.
        add(child);
    }

    // ------------------------------------------------------------------------
    // StretchLayoutManager ---------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Resize/reposition child widgets based on difference between current
     * dimensions and the original dimensions.
     */
    private void layoutChildren() {
        double widthRatio = (double) width / originalWidth;
        if (Math.abs(widthRatio) > Double.MAX_VALUE) {
            widthRatio = 1;
        }
        double heightRatio = (double) height / originalHeight;
        if (Math.abs(heightRatio) > Double.MAX_VALUE) {
            heightRatio = 1;
        }
        for (TWidget child: children.keySet()) {
            Rectangle rect = children.get(child);
            child.setDimensions((int) (rect.getX() * widthRatio),
                (int) (rect.getY() * heightRatio),
                (int) (rect.getWidth() * widthRatio),
                (int) (rect.getHeight() * heightRatio));
        }
    }

}
