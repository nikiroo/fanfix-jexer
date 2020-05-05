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
import jexer.bits.Color;
import jexer.bits.GraphicsChars;
import jexer.bits.MnemonicString;
import jexer.bits.StringUtils;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;
import static jexer.TKeypress.kbEnter;
import static jexer.TKeypress.kbSpace;

/**
 * TButton implements a simple button.  To make the button do something, pass
 * a TAction class to its constructor.
 *
 * @see TAction#DO()
 */
public class TButton extends TWidget {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The shortcut and button text.
     */
    private MnemonicString mnemonic;

    /**
     * Remember mouse state.
     */
    private TMouseEvent mouse;

    /**
     * True when the button is being pressed and held down.
     */
    private boolean inButtonPress = false;

    /**
     * The action to perform when the button is clicked.
     */
    private TAction action;

    /**
     * The background color used for the button "shadow", or null for "no
     * shadow".
     */
    private CellAttributes shadowColor;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Private constructor.
     *
     * @param parent parent widget
     * @param text label on the button
     * @param x column relative to parent
     * @param y row relative to parent
     */
    private TButton(final TWidget parent, final String text,
        final int x, final int y) {

        // Set parent and window
        super(parent);

        mnemonic = new MnemonicString(text);

        setX(x);
        setY(y);
        super.setHeight(2);
        super.setWidth(StringUtils.width(mnemonic.getRawLabel()) + 3);

        shadowColor = new CellAttributes();
        shadowColor.setTo(getWindow().getBackground());
        shadowColor.setForeColor(Color.BLACK);
        shadowColor.setBold(false);

        // Since we set dimensions after TWidget's constructor, we need to
        // update the layout manager.
        if (getParent().getLayoutManager() != null) {
            getParent().getLayoutManager().remove(this);
            getParent().getLayoutManager().add(this);
        }
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param text label on the button
     * @param x column relative to parent
     * @param y row relative to parent
     * @param action to call when button is pressed
     */
    public TButton(final TWidget parent, final String text,
        final int x, final int y, final TAction action) {

        this(parent, text, x, y);
        this.action = action;
    }
    
    /**
     * The action to call when the button is pressed.
     **/
    public TAction getAction() {
		return action;
	}
    
    /**
     * The action to call when the button is pressed.
     **/
    public void setAction(TAction action) {
		this.action = action;
	}

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Returns true if the mouse is currently on the button.
     *
     * @return if true the mouse is currently on the button
     */
    private boolean mouseOnButton() {
        int rightEdge = getWidth() - 1;
        if (inButtonPress) {
            rightEdge++;
        }
        if ((mouse != null)
            && (mouse.getY() == 0)
            && (mouse.getX() >= 0)
            && (mouse.getX() < rightEdge)
        ) {
            return true;
        }
        return false;
    }

    /**
     * Handle mouse button presses.
     *
     * @param mouse mouse button event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        this.mouse = mouse;

        if ((mouseOnButton()) && (mouse.isMouse1())) {
            // Begin button press
            inButtonPress = true;
        }
    }

    /**
     * Handle mouse button releases.
     *
     * @param mouse mouse button release event
     */
    @Override
    public void onMouseUp(final TMouseEvent mouse) {
        this.mouse = mouse;

        if (inButtonPress && mouse.isMouse1()) {
            // Dispatch the event
            dispatch();
        }

    }

    /**
     * Handle mouse movements.
     *
     * @param mouse mouse motion event
     */
    @Override
    public void onMouseMotion(final TMouseEvent mouse) {
        this.mouse = mouse;

        if (!mouseOnButton()) {
            inButtonPress = false;
        }
    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        if (keypress.equals(kbEnter)
            || keypress.equals(kbSpace)
        ) {
            // Dispatch
            dispatch();
            return;
        }

        // Pass to parent for the things we don't care about.
        super.onKeypress(keypress);
    }

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Override TWidget's width: we can only set width at construction time.
     *
     * @param width new widget width (ignored)
     */
    @Override
    public void setWidth(final int width) {
        // Do nothing
    }

    /**
     * Override TWidget's height: we can only set height at construction
     * time.
     *
     * @param height new widget height (ignored)
     */
    @Override
    public void setHeight(final int height) {
        // Do nothing
    }

    /**
     * Draw a button with a shadow.
     */
    @Override
    public void draw() {
        CellAttributes buttonColor;
        CellAttributes menuMnemonicColor;

        if (!isEnabled()) {
            buttonColor = getTheme().getColor("tbutton.disabled");
            menuMnemonicColor = getTheme().getColor("tbutton.disabled");
        } else if (isAbsoluteActive()) {
            buttonColor = getTheme().getColor("tbutton.active");
            menuMnemonicColor = getTheme().getColor("tbutton.mnemonic.highlighted");
        } else {
            buttonColor = getTheme().getColor("tbutton.inactive");
            menuMnemonicColor = getTheme().getColor("tbutton.mnemonic");
        }

        if (inButtonPress) {
            putCharXY(1, 0, ' ', buttonColor);
            putStringXY(2, 0, mnemonic.getRawLabel(), buttonColor);
            putCharXY(getWidth() - 1, 0, ' ', buttonColor);
        } else {
            putCharXY(0, 0, ' ', buttonColor);
            putStringXY(1, 0, mnemonic.getRawLabel(), buttonColor);
            putCharXY(getWidth() - 2, 0, ' ', buttonColor);

            if (shadowColor != null) {
                putCharXY(getWidth() - 1, 0,
                    GraphicsChars.CP437[0xDC], shadowColor);
                hLineXY(1, 1, getWidth() - 1,
                    GraphicsChars.CP437[0xDF], shadowColor);
            }
        }
        if (mnemonic.getScreenShortcutIdx() >= 0) {
            if (inButtonPress) {
                putCharXY(2 + mnemonic.getScreenShortcutIdx(), 0,
                    mnemonic.getShortcut(), menuMnemonicColor);
            } else {
                putCharXY(1 + mnemonic.getScreenShortcutIdx(), 0,
                    mnemonic.getShortcut(), menuMnemonicColor);
            }
        }
    }

    // ------------------------------------------------------------------------
    // TButton ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the mnemonic string for this button.
     *
     * @return mnemonic string
     */
    public MnemonicString getMnemonic() {
        return mnemonic;
    }

    /**
     * Act as though the button was pressed.  This is useful for other UI
     * elements to get the same action as if the user clicked the button.
     */
    public void dispatch() {
        if (action != null) {
            action.DO(this);
            inButtonPress = false;
        }
    }

    /**
     * Set the background color used for the button "shadow".  If null, no
     * shadow will be drawn.
     *
     * @param color the new background color, or null for no shadow
     */
    public void setShadowColor(final CellAttributes color) {
        if (color != null) {
            shadowColor = new CellAttributes();
            shadowColor.setTo(color);
            shadowColor.setForeColor(Color.BLACK);
            shadowColor.setBold(false);
        } else {
            shadowColor = null;
        }
    }

}
