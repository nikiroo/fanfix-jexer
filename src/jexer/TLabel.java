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
import jexer.bits.MnemonicString;
import jexer.bits.StringUtils;

/**
 * TLabel implements a simple label, with an optional mnemonic hotkey action
 * associated with it.
 */
public class TLabel extends TWidget {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The shortcut and label.
     */
    private MnemonicString mnemonic;

    /**
     * The action to perform when the mnemonic shortcut is pressed.
     */
    private TAction action;

    /**
     * Label color.
     */
    private String colorKey;

    /**
     * If true, use the window's background color.
     */
    private boolean useWindowBackground = true;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor, using the default "tlabel" for colorKey.
     *
     * @param parent parent widget
     * @param text label on the screen
     * @param x column relative to parent
     * @param y row relative to parent
     */
    public TLabel(final TWidget parent, final String text, final int x,
        final int y) {

        this(parent, text, x, y, "tlabel");
    }

    /**
     * Public constructor, using the default "tlabel" for colorKey.
     *
     * @param parent parent widget
     * @param text label on the screen
     * @param x column relative to parent
     * @param y row relative to parent
     * @param action to call when shortcut is pressed
     */
    public TLabel(final TWidget parent, final String text, final int x,
        final int y, final TAction action) {

        this(parent, text, x, y, "tlabel", action);
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param text label on the screen
     * @param x column relative to parent
     * @param y row relative to parent
     * @param colorKey ColorTheme key color to use for foreground text
     */
    public TLabel(final TWidget parent, final String text, final int x,
        final int y, final String colorKey) {

        this(parent, text, x, y, colorKey, true);
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param text label on the screen
     * @param x column relative to parent
     * @param y row relative to parent
     * @param colorKey ColorTheme key color to use for foreground text
     * @param action to call when shortcut is pressed
     */
    public TLabel(final TWidget parent, final String text, final int x,
        final int y, final String colorKey, final TAction action) {

        this(parent, text, x, y, colorKey, true, action);
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param text label on the screen
     * @param x column relative to parent
     * @param y row relative to parent
     * @param colorKey ColorTheme key color to use for foreground text
     * @param useWindowBackground if true, use the window's background color
     */
    public TLabel(final TWidget parent, final String text, final int x,
        final int y, final String colorKey, final boolean useWindowBackground) {

        this(parent, text, x, y, colorKey, useWindowBackground, null);
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param text label on the screen
     * @param x column relative to parent
     * @param y row relative to parent
     * @param colorKey ColorTheme key color to use for foreground text
     * @param useWindowBackground if true, use the window's background color
     * @param action to call when shortcut is pressed
     */
    public TLabel(final TWidget parent, final String text, final int x,
        final int y, final String colorKey, final boolean useWindowBackground,
        final TAction action) {

        // Set parent and window
        super(parent, false, x, y, 0, 1);

        setLabel(text);
        this.colorKey = colorKey;
        this.useWindowBackground = useWindowBackground;
        this.action = action;
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
     * Draw a static label.
     */
    @Override
    public void draw() {
        // Setup my color
        CellAttributes color = new CellAttributes();
        CellAttributes mnemonicColor = new CellAttributes();
        color.setTo(getTheme().getColor(colorKey));
        mnemonicColor.setTo(getTheme().getColor("tlabel.mnemonic"));
        if (useWindowBackground) {
            CellAttributes background = getWindow().getBackground();
            color.setBackColor(background.getBackColor());
            mnemonicColor.setBackColor(background.getBackColor());
        }
        putStringXY(0, 0, mnemonic.getRawLabel(), color);
        if (mnemonic.getScreenShortcutIdx() >= 0) {
            putCharXY(mnemonic.getScreenShortcutIdx(), 0,
                mnemonic.getShortcut(), mnemonicColor);
        }
    }

    // ------------------------------------------------------------------------
    // TLabel -----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get label raw text.
     *
     * @return label text
     */
    public String getLabel() {
        return mnemonic.getRawLabel();
    }

    /**
     * Get the mnemonic string for this label.
     *
     * @return mnemonic string
     */
    public MnemonicString getMnemonic() {
        return mnemonic;
    }

    /**
     * Set label text.
     *
     * @param label new label text
     */
    public void setLabel(final String label) {
        mnemonic = new MnemonicString(label);
        super.setWidth(StringUtils.width(mnemonic.getRawLabel()));
    }

    /**
     * Get the label color.
     *
     * @return the ColorTheme key color to use for foreground text
     */
    public String getColorKey() {
        return colorKey;
    }

    /**
     * Set the label color.
     *
     * @param colorKey ColorTheme key color to use for foreground text
     */
    public void setColorKey(final String colorKey) {
        this.colorKey = colorKey;
    }

    /**
     * Act as though the mnemonic shortcut was pressed.
     */
    public void dispatch() {
        if (action != null) {
            action.DO(this);
        }
    }

}
