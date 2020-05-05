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
 * TRadioGroup is a collection of TRadioButtons with a box and label.
 */
public class TRadioGroup extends TWidget {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Label for this radio button group.
     */
    private String label;

    /**
     * Only one of my children can be selected.
     */
    private TRadioButton selectedButton = null;

    /**
     * If true, one of the children MUST be selected.  Note package private
     * access.
     */
    boolean requiresSelection = false;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of group
     * @param label label to display on the group box
     */
    public TRadioGroup(final TWidget parent, final int x, final int y,
        final int width, final String label) {

        // Set parent and window
        super(parent, x, y, width, 2);

        this.label = label;
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param label label to display on the group box
     */
    public TRadioGroup(final TWidget parent, final int x, final int y,
        final String label) {

        // Set parent and window
        super(parent, x, y, StringUtils.width(label) + 4, 2);

        this.label = label;
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
     * Draw a radio button with label.
     */
    @Override
    public void draw() {
        CellAttributes radioGroupColor;

        if (isAbsoluteActive()) {
            radioGroupColor = getTheme().getColor("tradiogroup.active");
        } else {
            radioGroupColor = getTheme().getColor("tradiogroup.inactive");
        }

        drawBox(0, 0, getWidth(), getHeight(), radioGroupColor, radioGroupColor,
            3, false);

        hLineXY(1, 0, StringUtils.width(label) + 2, ' ', radioGroupColor);
        putStringXY(2, 0, label, radioGroupColor);
    }

    // ------------------------------------------------------------------------
    // TRadioGroup ------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the radio button ID that was selected.
     *
     * @return ID of the selected button, or 0 if no button is selected
     */
    public int getSelected() {
        if (selectedButton == null) {
            return 0;
        }
        return selectedButton.getId();
    }

    /**
     * Set the new selected radio button.  1-based.
     *
     * @param id ID of the selected button, or 0 to unselect
     */
    public void setSelected(final int id) {
        if ((id < 0) || (id > getChildren().size())) {
            return;
        }

        for (TWidget widget: getChildren()) {
            ((TRadioButton) widget).selected = false;
        }
        if (id == 0) {
            selectedButton = null;
            return;
        }
        assert ((id > 0) && (id <= getChildren().size()));
        TRadioButton button = (TRadioButton) (getChildren().get(id - 1));
        button.selected = true;
        selectedButton = button;
    }

    /**
     * Get the radio button that was selected.
     *
     * @return the selected button, or null if no button is selected
     */
    public TRadioButton getSelectedButton() {
        return selectedButton;
    }

    /**
     * Convenience function to add a radio button to this group.
     *
     * @param label label to display next to (right of) the radiobutton
     * @param selected if true, this will be the selected radiobutton
     * @return the new radio button
     */
    public TRadioButton addRadioButton(final String label,
        final boolean selected) {

        TRadioButton button = addRadioButton(label);
        setSelected(button.id);
        return button;
    }

    /**
     * Convenience function to add a radio button to this group.
     *
     * @param label label to display next to (right of) the radiobutton
     * @return the new radio button
     */
    public TRadioButton addRadioButton(final String label) {
        return new TRadioButton(this, 0, 0, label, 0);
    }

    /**
     * Package private method for RadioButton to add itself to a RadioGroup
     * container.
     *
     * @param button the button to add
     */
    void addRadioButton(final TRadioButton button) {
        super.setHeight(getChildren().size() + 2);
        button.setX(1);
        button.setY(getChildren().size());
        button.id = getChildren().size();
        String label = button.getMnemonic().getRawLabel();

        if (StringUtils.width(label) + 4 > getWidth()) {
            super.setWidth(StringUtils.width(label) + 7);
        }

        if (getParent().getLayoutManager() != null) {
            getParent().getLayoutManager().resetSize(this);
        }

        // Default to the first item on the list.
        activate(getChildren().get(0));
    }

    /**
     * Get the requires selection flag.
     *
     * @return true if this radiogroup requires that one of the buttons be
     * selected
     */
    public boolean getRequiresSelection() {
        return requiresSelection;
    }

    /**
     * Set the requires selection flag.
     *
     * @param requiresSelection if true, then this radiogroup requires that
     * one of the buttons be selected
     */
    public void setRequiresSelection(final boolean requiresSelection) {
        this.requiresSelection = requiresSelection;
        if (requiresSelection) {
            if ((getChildren().size() > 0) && (selectedButton == null)) {
                setSelected(1);
            }
        }
    }

}
