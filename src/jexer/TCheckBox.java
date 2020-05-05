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

import static jexer.TKeypress.kbEnter;
import static jexer.TKeypress.kbEsc;
import static jexer.TKeypress.kbSpace;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.bits.MnemonicString;
import jexer.bits.StringUtils;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;

/**
 * TCheckBox implements an on/off checkbox.
 */
public class TCheckBox extends TWidget {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * CheckBox state, true means checked.
     */
    private boolean checked = false;

    /**
     * The shortcut and checkbox label.
     */
    private MnemonicString mnemonic;

    /**
     * If true, use the window's background color.
     */
    private boolean useWindowBackground = false;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param label label to display next to (right of) the checkbox
     * @param checked initial check state
     */
    public TCheckBox(final TWidget parent, final int x, final int y,
        final String label, final boolean checked) {

        // Set parent and window
        super(parent, x, y, StringUtils.width(label) + 4, 1);

        mnemonic = new MnemonicString(label);
        this.checked = checked;

        setCursorVisible(true);
        setCursorX(1);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Returns true if the mouse is currently on the checkbox.
     *
     * @param mouse mouse event
     * @return true if the mouse is currently on the checkbox
     */
    private boolean mouseOnCheckBox(final TMouseEvent mouse) {
        if ((mouse.getY() == 0)
            && (mouse.getX() >= 0)
            && (mouse.getX() <= 2)
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
        if ((mouseOnCheckBox(mouse)) && (mouse.isMouse1())) {
            // Switch state
            checked = !checked;
        }
    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        if (keypress.equals(kbSpace)
            || keypress.equals(kbEnter)
        ) {
            checked = !checked;
            return;
        }

        if (keypress.equals(kbEsc)) {
            checked = false;
            return;
        }

        // Pass to parent for the things we don't care about.
        super.onKeypress(keypress);
    }

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw a checkbox with label.
     */
    @Override
    public void draw() {
        CellAttributes checkboxColor;
        CellAttributes mnemonicColor;

        if (isAbsoluteActive()) {
            checkboxColor = getTheme().getColor("tcheckbox.active");
            mnemonicColor = getTheme().getColor("tcheckbox.mnemonic.highlighted");
        } else {
            checkboxColor = getTheme().getColor("tcheckbox.inactive");
            mnemonicColor = getTheme().getColor("tcheckbox.mnemonic");
        }
        if (useWindowBackground) {
            CellAttributes background = getWindow().getBackground();
            checkboxColor.setBackColor(background.getBackColor());
        }

        putCharXY(0, 0, '[', checkboxColor);
        if (checked) {
            putCharXY(1, 0, GraphicsChars.CHECK, checkboxColor);
        } else {
            putCharXY(1, 0, ' ', checkboxColor);
        }
        putCharXY(2, 0, ']', checkboxColor);
        putStringXY(4, 0, mnemonic.getRawLabel(), checkboxColor);
        if (mnemonic.getScreenShortcutIdx() >= 0) {
            putCharXY(4 + mnemonic.getScreenShortcutIdx(), 0,
                mnemonic.getShortcut(), mnemonicColor);
        }
    }

    // ------------------------------------------------------------------------
    // TCheckBox --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get checked value.
     *
     * @return if true, this is checked
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * Set checked value.
     *
     * @param checked new checked value.
     */
    public void setChecked(final boolean checked) {
        this.checked = checked;
    }

    /**
     * Get the mnemonic string for this checkbox.
     *
     * @return mnemonic string
     */
    public MnemonicString getMnemonic() {
        return mnemonic;
    }

}
