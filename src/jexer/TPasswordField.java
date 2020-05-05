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

import jexer.bits.CellAttributes;
import jexer.bits.StringUtils;

/**
 * TPasswordField implements an editable text field that displays
 * stars/asterisks when it is not active.
 */
public class TPasswordField extends TField {

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width visible text width
     * @param fixed if true, the text cannot exceed the display width
     */
    public TPasswordField(final TWidget parent, final int x, final int y,
        final int width, final boolean fixed) {

        this(parent, x, y, width, fixed, "", null, null);
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width visible text width
     * @param fixed if true, the text cannot exceed the display width
     * @param text initial text, default is empty string
     */
    public TPasswordField(final TWidget parent, final int x, final int y,
        final int width, final boolean fixed, final String text) {

        this(parent, x, y, width, fixed, text, null, null);
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width visible text width
     * @param fixed if true, the text cannot exceed the display width
     * @param text initial text, default is empty string
     * @param enterAction function to call when enter key is pressed
     * @param updateAction function to call when the text is updated
     */
    public TPasswordField(final TWidget parent, final int x, final int y,
        final int width, final boolean fixed, final String text,
        final TAction enterAction, final TAction updateAction) {

        // Set parent and window
        super(parent, x, y, width, fixed, text, enterAction, updateAction);
    }

    // ------------------------------------------------------------------------
    // TField -----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the text field.
     */
    @Override
    public void draw() {
        CellAttributes fieldColor;

        boolean showStars = false;
        if (isAbsoluteActive()) {
            fieldColor = getTheme().getColor("tfield.active");
        } else {
            fieldColor = getTheme().getColor("tfield.inactive");
            showStars = true;
        }

        int end = windowStart + getWidth();
        if (end > StringUtils.width(text)) {
            end = StringUtils.width(text);
        }

        hLineXY(0, 0, getWidth(), backgroundChar, fieldColor);
        if (showStars) {
            hLineXY(0, 0, getWidth() - 2, '*', fieldColor);
        } else {
            putStringXY(0, 0, text.substring(screenToTextPosition(windowStart),
                    screenToTextPosition(end)), fieldColor);
        }

        // Fix the cursor, it will be rendered by TApplication.drawAll().
        updateCursor();
    }

}
