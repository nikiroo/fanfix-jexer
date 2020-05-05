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
import jexer.bits.GraphicsChars;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;
import static jexer.TKeypress.*;

/**
 * TSpinner implements a simple up/down spinner.
 */
public class TSpinner extends TWidget {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The action to perform when the user clicks on the up arrow.
     */
    private TAction upAction = null;

    /**
     * The action to perform when the user clicks on the down arrow.
     */
    private TAction downAction = null;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param upAction action to call when the up arrow is clicked or pressed
     * @param downAction action to call when the down arrow is clicked or
     * pressed
     */
    public TSpinner(final TWidget parent, final int x, final int y,
        final TAction upAction, final TAction downAction) {

        // Set parent and window
        super(parent, x, y, 2, 1);

        this.upAction = upAction;
        this.downAction = downAction;
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Returns true if the mouse is currently on the up arrow.
     *
     * @param mouse mouse event
     * @return true if the mouse is currently on the up arrow
     */
    private boolean mouseOnUpArrow(final TMouseEvent mouse) {
        if ((mouse.getY() == 0)
            && (mouse.getX() == getWidth() - 2)
        ) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the mouse is currently on the down arrow.
     *
     * @param mouse mouse event
     * @return true if the mouse is currently on the down arrow
     */
    private boolean mouseOnDownArrow(final TMouseEvent mouse) {
        if ((mouse.getY() == 0)
            && (mouse.getX() == getWidth() - 1)
        ) {
            return true;
        }
        return false;
    }

    /**
     * Handle mouse checkbox presses.
     *
     * @param mouse mouse button down event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        if ((mouseOnUpArrow(mouse)) && (mouse.isMouse1())) {
            up();
        } else if ((mouseOnDownArrow(mouse)) && (mouse.isMouse1())) {
            down();
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
            up();
            return;
        }
        if (keypress.equals(kbDown)) {
            down();
            return;
        }

        // Pass to parent for the things we don't care about.
        super.onKeypress(keypress);
    }

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the spinner arrows.
     */
    @Override
    public void draw() {
        CellAttributes spinnerColor;

        if (isAbsoluteActive()) {
            spinnerColor = getTheme().getColor("tspinner.active");
        } else {
            spinnerColor = getTheme().getColor("tspinner.inactive");
        }

        putCharXY(getWidth() - 2, 0, GraphicsChars.UPARROW, spinnerColor);
        putCharXY(getWidth() - 1, 0, GraphicsChars.DOWNARROW, spinnerColor);
    }

    // ------------------------------------------------------------------------
    // TSpinner ---------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Perform the "up" action.
     */
    private void up() {
        if (upAction != null) {
            upAction.DO(this);
        }
    }

    /**
     * Perform the "down" action.
     */
    private void down() {
        if (downAction != null) {
            downAction.DO(this);
        }
    }

}
