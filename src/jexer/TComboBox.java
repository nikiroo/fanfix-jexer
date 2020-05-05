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

import java.util.ArrayList;
import java.util.List;

import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;
import jexer.event.TResizeEvent.Type;
import static jexer.TKeypress.*;

/**
 * TComboBox implements a combobox containing a drop-down list and edit
 * field.  Alt-Down can be used to show the drop-down.
 */
public class TComboBox extends TWidget {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The list of items in the drop-down.
     */
    private TList list;

    /**
     * The edit field containing the value to return.
     */
    private TField field;

    /**
     * The action to perform when the user selects an item (clicks or enter).
     */
    private TAction updateAction = null;

    /**
     * If true, the field cannot be updated to a value not on the list.
     */
    private boolean limitToListValue = true;
    
    /**
     * The height of the list of values when it is shown, or -1 to use the 
     * number of values in the list as the height.
     */
    private int valuesHeight = -1;
    
    /**
     * The values shown by the drop-down list.
     */
    private List<String> values = new ArrayList<String>();
    
    /**
     * When looking for a link between the displayed text and the list 
     * of values, do a case sensitive search.
     */
    private boolean caseSensitive = true;

    /**
     * The maximum height of the values drop-down when it is visible.
     */
    private int maxValuesHeight = 3;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width visible combobox width, including the down-arrow
     * @param values the possible values for the box, shown in the drop-down
     * @param valuesIndex the initial index in values, or -1 for no default
     * value
     * @param valuesHeight the height of the values drop-down when it is
     * visible, or -1 to use the number of values as the height of the list
     * @param updateAction action to call when a new value is selected from
     * the list or enter is pressed in the edit field
     */
    public TComboBox(final TWidget parent, final int x, final int y,
        final int width, final List<String> values, final int valuesIndex,
        final int valuesHeight, final TAction updateAction) {

        // Set parent and window
        super(parent, x, y, width, 1);

        assert (values != null);

        this.updateAction = updateAction;
        this.values = values;
        this.valuesHeight = valuesHeight;

        field = new TField(this, 0, 0, Math.max(0, width - 3), false, "",
            updateAction, null);
        if (valuesIndex >= 0) {
            field.setText(values.get(valuesIndex));
        }

        setHeight(1);
        if (limitToListValue) {
            field.setEnabled(false);
        } else {
            activate(field);
        }
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Returns true if the mouse is currently on the down arrow.
     *
     * @param mouse mouse event
     * @return true if the mouse is currently on the down arrow
     */
    private boolean mouseOnArrow(final TMouseEvent mouse) {
        if ((mouse.getY() == 0)
            && (mouse.getX() >= getWidth() - 3)
            && (mouse.getX() <= getWidth() - 1)
        ) {
            return true;
        }
        return false;
    }

    /**
     * Handle mouse down clicks.
     *
     * @param mouse mouse button down event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        if ((mouseOnArrow(mouse)) && (mouse.isMouse1())) {
            // Make the list visible or not.
            if (list != null) {
                hideDropdown();
            } else {
                displayDropdown();
            }
        }

        // Pass to parent for the things we don't care about.
        super.onMouseDown(mouse);
    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        if (keypress.equals(kbEsc)) {
            if (list != null) {
                hideDropdown();
                return;
            }
        }

        if (keypress.equals(kbAltDown)) {
            displayDropdown();
            return;
        }

        if (keypress.equals(kbTab)
            || (keypress.equals(kbShiftTab))
            || (keypress.equals(kbBackTab))
        ) {
            if (list != null) {
                hideDropdown();
                return;
            }
        }

        // Pass to parent for the things we don't care about.
        super.onKeypress(keypress);
    }

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Override TWidget's width: we need to set child widget widths.
     *
     * @param width new widget width
     */
    @Override
    public void setWidth(final int width) {
        if (field != null) {
            field.setWidth(width - 3);
        }
        if (list != null) {
            list.setWidth(width);
        }
        super.setWidth(width);
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
     * Draw the combobox down arrow.
     */
    @Override
    public void draw() {
        CellAttributes comboBoxColor;

        if (!isAbsoluteActive()) {
            // We lost focus, turn off the list.
            hideDropdown();
        }

        if (isAbsoluteActive()) {
            comboBoxColor = getTheme().getColor("tcombobox.active");
        } else {
            comboBoxColor = getTheme().getColor("tcombobox.inactive");
        }

        putCharXY(getWidth() - 3, 0, GraphicsChars.DOWNARROWLEFT,
            comboBoxColor);
        putCharXY(getWidth() - 2, 0, GraphicsChars.DOWNARROW,
            comboBoxColor);
        putCharXY(getWidth() - 1, 0, GraphicsChars.DOWNARROWRIGHT,
            comboBoxColor);
    }

    // ------------------------------------------------------------------------
    // TComboBox --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Hide the drop-down list.
     */
    public void hideList() {
        list.setEnabled(false);
        list.setVisible(false);
        super.setHeight(1);
        if (limitToListValue == false) {
            activate(field);
        }
    }

    /**
     * Show the drop-down list.
     */
    public void showList() {
        list.setEnabled(true);
        list.setVisible(true);
        super.setHeight(list.getHeight() + 1);
        activate(list);
    }

    /**
     * Get combobox text value.
     *
     * @return text in the edit field
     */
    public String getText() {
        return field.getText();
    }

    /**
     * Set combobox text value.
     *
     * @param text the new text in the edit field
     */
    public void setText(final String text) {
        setText(text, true);
    }

    /**
     * Set combobox text value.
     *
     * @param text the new text in the edit field
     * @param caseSensitive if true, perform a case-sensitive search for the
     * list item
     */
    public void setText(final String text, final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    	field.setText(text);
        if (list != null) {
        	displayDropdown();
        }
    }

    /**
     * Set combobox text to one of the list values.
     *
     * @param index the index in the list
     */
    public void setIndex(final int index) {
        list.setSelectedIndex(index);
        field.setText(list.getSelected());
    }

    /**
     * Get a copy of the list of strings to display.
     *
     * @return the list of strings
     */
    public final List<String> getList() {
        return list.getList();
    }

    /**
     * Set the new list of strings to display.
     *
     * @param list new list of strings
     */
    public final void setList(final List<String> list) {
        this.list.setList(list);
        this.list.setHeight(Math.max(3, Math.min(list.size() + 1,
                    maxValuesHeight)));
        field.setText("");
    }
    
    /**
     * Make sure the widget displays all its elements correctly according to
     * the current size and content.
     */
    public void reflowData() {
    	// TODO: why setW/setH/reflow not enough for the scrollbars?
    	TList list = this.list;
    	if (list != null) {
    		int valuesHeight = this.valuesHeight;
    		if (valuesHeight < 0) {
    			valuesHeight = values == null ? 0 : values.size() + 1;
    		}
    		
    		list.onResize(new TResizeEvent(Type.WIDGET, getWidth(), 
    				valuesHeight));
    		setHeight(valuesHeight + 1);
    	}
    	
    	field.onResize(new TResizeEvent(Type.WIDGET, getWidth(), 
    			field.getHeight()));
    }
    
    @Override
    public void onResize(TResizeEvent resize) {
    	super.onResize(resize);
    	reflowData();
    }

    /**
     * Display the drop-down menu represented by {@link TComboBox#list}.
     */
    private void displayDropdown() {
    	if (this.list != null) {
    		hideDropdown();
    	}
    	
    	int valuesHeight = this.valuesHeight;
    	if (valuesHeight < 0) {
    		valuesHeight = values == null ? 0 : values.size() + 1;
    	}
    	
    	TList list = new TList(this, values, 0, 1, getWidth(), valuesHeight,
    			new TAction() {
					@Override
					public void DO() {
						TList list = TComboBox.this.list;
						if (list == null) {
							return;
						}
						
						field.setText(list.getSelected());
						hideDropdown();
						
						if (updateAction != null) {
							updateAction.DO();
						}
					}
				}
    	);
    	
    	int i = -1;
    	if (values != null) {
    		String current = field.getText();
    		for (i = 0 ; i < values.size() ; i++) {
    			String value = values.get(i);
    			if ((caseSensitive && current.equals(value)) 
    					|| (!caseSensitive && current.equalsIgnoreCase(value))) {
    				break;
    			}
    		}
    		
    		if (i >= values.size()) {
    			i = -1;
    		}
    	}
    	list.setSelectedIndex(i);
    	
    	list.setEnabled(true);
    	list.setVisible(true);
    	
    	this.list = list;
    	
    	reflowData();
    	activate(list);
    }
    
    /**
     * Hide the drop-down menu represented by {@link TComboBox#list}.
     */
    private void hideDropdown() {
    	TList list = this.list;
    	
    	if (list != null) {
    		list.setEnabled(false);
    		list.setVisible(false);
    		removeChild(list);
    		
    		setHeight(1);
    		if (limitToListValue == false) {
                activate(field);
            }
    		
    		this.list = null;
    	}
    }
}
