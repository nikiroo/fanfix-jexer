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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;

import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;
import static jexer.TKeypress.*;

/**
 * TImageWindow shows an image with scrollbars.
 */
public class TImageWindow extends TScrollableWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(TImageWindow.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The number of lines to scroll on mouse wheel up/down.
     */
    private static final int wheelScrollSize = 3;

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Hang onto the TImage so I can resize it with the window.
     */
    private TImage imageField;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor opens a file.
     *
     * @param parent the main application
     * @param file the file to open
     * @throws IOException if a java.io operation throws
     */
    public TImageWindow(final TApplication parent,
        final File file) throws IOException {

        this(parent, file, 0, 0, parent.getScreen().getWidth(),
            parent.getDesktopBottom() - parent.getDesktopTop());
    }

    /**
     * Public constructor opens a file.
     *
     * @param parent the main application
     * @param file the file to open
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of window
     * @param height height of window
     * @throws IOException if a java.io operation throws
     */
    public TImageWindow(final TApplication parent, final File file,
        final int x, final int y, final int width,
        final int height) throws IOException {

        super(parent, file.getName(), x, y, width, height, RESIZABLE);

        BufferedImage image = ImageIO.read(file);

        imageField = addImage(0, 0, getWidth() - 2, getHeight() - 2,
            image, 0, 0);
        setTitle(file.getName());

        setupAfterImage();
    }

    /**
     * Setup other fields after the image is created.
     */
    private void setupAfterImage() {
        if (imageField.getRows() < getHeight() - 2) {
            imageField.setHeight(imageField.getRows());
            setHeight(imageField.getRows() + 2);
        }
        if (imageField.getColumns() < getWidth() - 2) {
            imageField.setWidth(imageField.getColumns());
            setWidth(imageField.getColumns() + 2);
        }

        hScroller = new THScroller(this,
            Math.min(Math.max(0, getWidth() - 17), 17),
            getHeight() - 2,
            getWidth() - Math.min(Math.max(0, getWidth() - 17), 17) - 3);
        vScroller = new TVScroller(this, getWidth() - 2, 0, getHeight() - 2);
        setTopValue(0);
        setBottomValue(imageField.getRows() - imageField.getHeight());
        setLeftValue(0);
        setRightValue(imageField.getColumns() - imageField.getWidth());

        statusBar = newStatusBar(i18n.getString("statusBar"));
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle mouse press events.
     *
     * @param mouse mouse button press event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        // Use TWidget's code to pass the event to the children.
        super.onMouseDown(mouse);

        if (mouse.isMouseWheelUp()) {
            imageField.setTop(imageField.getTop() - wheelScrollSize);
        } else if (mouse.isMouseWheelDown()) {
            imageField.setTop(imageField.getTop() + wheelScrollSize);
        }
        setVerticalValue(imageField.getTop());
    }

    /**
     * Handle mouse release events.
     *
     * @param mouse mouse button release event
     */
    @Override
    public void onMouseUp(final TMouseEvent mouse) {
        // Use TWidget's code to pass the event to the children.
        super.onMouseUp(mouse);

        if (mouse.isMouse1() && mouseOnVerticalScroller(mouse)) {
            // Clicked/dragged on vertical scrollbar
            imageField.setTop(getVerticalValue());
        }
        if (mouse.isMouse1() && mouseOnHorizontalScroller(mouse)) {
            // Clicked/dragged on horizontal scrollbar
            imageField.setLeft(getHorizontalValue());
        }
    }

    /**
     * Method that subclasses can override to handle mouse movements.
     *
     * @param mouse mouse motion event
     */
    @Override
    public void onMouseMotion(final TMouseEvent mouse) {
        // Use TWidget's code to pass the event to the children.
        super.onMouseMotion(mouse);

        if (mouse.isMouse1() && mouseOnVerticalScroller(mouse)) {
            // Clicked/dragged on vertical scrollbar
            imageField.setTop(getVerticalValue());
        }
        if (mouse.isMouse1() && mouseOnHorizontalScroller(mouse)) {
            // Clicked/dragged on horizontal scrollbar
            imageField.setLeft(getHorizontalValue());
        }
    }

    /**
     * Handle window/screen resize events.
     *
     * @param event resize event
     */
    @Override
    public void onResize(final TResizeEvent event) {
        if (event.getType() == TResizeEvent.Type.WIDGET) {
            // Resize the image field
            TResizeEvent imageSize = new TResizeEvent(TResizeEvent.Type.WIDGET,
                event.getWidth() - 2, event.getHeight() - 2);
            imageField.onResize(imageSize);

            // Have TScrollableWindow handle the scrollbars
            super.onResize(event);
            return;
        }

        // Pass to children instead
        for (TWidget widget: getChildren()) {
            widget.onResize(event);
        }
    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        if (keypress.equals(kbUp)) {
            verticalDecrement();
            imageField.setTop(getVerticalValue());
            return;
        }
        if (keypress.equals(kbDown)) {
            verticalIncrement();
            imageField.setTop(getVerticalValue());
            return;
        }
        if (keypress.equals(kbPgUp)) {
            bigVerticalDecrement();
            imageField.setTop(getVerticalValue());
            return;
        }
        if (keypress.equals(kbPgDn)) {
            bigVerticalIncrement();
            imageField.setTop(getVerticalValue());
            return;
        }
        if (keypress.equals(kbRight)) {
            horizontalIncrement();
            imageField.setLeft(getHorizontalValue());
            return;
        }
        if (keypress.equals(kbLeft)) {
            horizontalDecrement();
            imageField.setLeft(getHorizontalValue());
            return;
        }

        // We did not take it, let the TImage instance see it.
        super.onKeypress(keypress);

        setVerticalValue(imageField.getTop());
        setBottomValue(imageField.getRows() - imageField.getHeight());
        setHorizontalValue(imageField.getLeft());
        setRightValue(imageField.getColumns() - imageField.getWidth());
    }

    // ------------------------------------------------------------------------
    // TWindow ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the window.
     */
    @Override
    public void draw() {
        // Draw as normal.
        super.draw();

        // We have to get the scrollbar values after we have let the image
        // try to draw.
        setBottomValue(imageField.getRows() - imageField.getHeight());
        setRightValue(imageField.getColumns() - imageField.getWidth());
    }

}
