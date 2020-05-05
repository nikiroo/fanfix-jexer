/*
 * Jexer - Java Text User Interface
 *
 * The MIT License (MIT)
 *
 * Copyright (C) 2019 David "Niki" ROULET
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
 * @author David ROULET [niki@nikiroo.be]
 * @version 2
 */
package be.nikiroo.jexer;

import static jexer.TKeypress.kbBackTab;
import static jexer.TKeypress.kbDown;
import static jexer.TKeypress.kbEnd;
import static jexer.TKeypress.kbEnter;
import static jexer.TKeypress.kbHome;
import static jexer.TKeypress.kbLeft;
import static jexer.TKeypress.kbPgDn;
import static jexer.TKeypress.kbPgUp;
import static jexer.TKeypress.kbRight;
import static jexer.TKeypress.kbShiftTab;
import static jexer.TKeypress.kbTab;
import static jexer.TKeypress.kbUp;
import jexer.THScroller;
import jexer.TScrollableWidget;
import jexer.TVScroller;
import jexer.TWidget;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;

/**
 * This class represents a browsable {@link TWidget}, that is, a {@link TWidget}
 * where you can use the keyboard or mouse to browse to one line to the next, or
 * from left t right.
 * 
 * @author niki
 */
abstract public class TBrowsableWidget extends TScrollableWidget {
	private int selectedRow;
	private int selectedColumn;
	private int yOffset;

	/**
	 * The number of rows in this {@link TWidget}.
	 * 
	 * @return the number of rows
	 */
	abstract protected int getRowCount();

	/**
	 * The number of columns in this {@link TWidget}.
	 * 
	 * @return the number of columns
	 */
	abstract protected int getColumnCount();

	/**
	 * The virtual width of this {@link TWidget}, that is, the total width it
	 * can take to display all the data.
	 * 
	 * @return the width
	 */
	abstract int getVirtualWidth();

	/**
	 * The virtual height of this {@link TWidget}, that is, the total width it
	 * can take to display all the data.
	 * 
	 * @return the height
	 */
	abstract int getVirtualHeight();

	/**
	 * Basic setup of this class (called by all constructors)
	 */
	private void setup() {
		vScroller = new TVScroller(this, 0, 0, 1);
		hScroller = new THScroller(this, 0, 0, 1);
		fixScrollers();
	}

	/**
	 * Create a new {@link TBrowsableWidget} linked to the given {@link TWidget}
	 * parent.
	 * 
	 * @param parent
	 *            parent widget
	 */
	protected TBrowsableWidget(final TWidget parent) {
		super(parent);
		setup();
	}

	/**
	 * Create a new {@link TBrowsableWidget} linked to the given {@link TWidget}
	 * parent.
	 * 
	 * @param parent
	 *            parent widget
	 * @param x
	 *            column relative to parent
	 * @param y
	 *            row relative to parent
	 * @param width
	 *            width of widget
	 * @param height
	 *            height of widget
	 */
	protected TBrowsableWidget(final TWidget parent, final int x, final int y,
			final int width, final int height) {
		super(parent, x, y, width, height);
		setup();
	}

	/**
	 * Create a new {@link TBrowsableWidget} linked to the given {@link TWidget}
	 * parent.
	 * 
	 * @param parent
	 *            parent widget
	 * @param enabled
	 *            if true assume enabled
	 */
	protected TBrowsableWidget(final TWidget parent, final boolean enabled) {
		super(parent, enabled);
		setup();
	}

	/**
	 * Create a new {@link TBrowsableWidget} linked to the given {@link TWidget}
	 * parent.
	 * 
	 * @param parent
	 *            parent widget
	 * @param enabled
	 *            if true assume enabled
	 * @param x
	 *            column relative to parent
	 * @param y
	 *            row relative to parent
	 * @param width
	 *            width of widget
	 * @param height
	 *            height of widget
	 */
	protected TBrowsableWidget(final TWidget parent, final boolean enabled,
			final int x, final int y, final int width, final int height) {
		super(parent, enabled, x, y, width, height);
		setup();
	}

	/**
	 * The currently selected row (or -1 if no row is selected).
	 * 
	 * @return the selected row
	 */
	public int getSelectedRow() {
		return selectedRow;
	}

	/**
	 * The currently selected row (or -1 if no row is selected).
	 * <p>
	 * You may want to call {@link TBrowsableWidget#reflowData()} when done to
	 * see the changes.
	 * 
	 * @param selectedRow
	 *            the new selected row
	 * 
	 * @throws IndexOutOfBoundsException
	 *             when the index is out of bounds
	 */
	public void setSelectedRow(int selectedRow) {
		if (selectedRow < -1 || selectedRow >= getRowCount()) {
			throw new IndexOutOfBoundsException(String.format(
					"Cannot set row %d on a table with %d rows", selectedRow,
					getRowCount()));
		}

		this.selectedRow = selectedRow;
	}

	/**
	 * The currently selected column (or -1 if no column is selected).
	 * 
	 * @return the new selected column
	 */
	public int getSelectedColumn() {
		return selectedColumn;
	}

	/**
	 * The currently selected column (or -1 if no column is selected).
	 * <p>
	 * You may want to call {@link TBrowsableWidget#reflowData()} when done to
	 * see the changes.
	 * 
	 * @param selectedColumn
	 *            the new selected column
	 * 
	 * @throws IndexOutOfBoundsException
	 *             when the index is out of bounds
	 */
	public void setSelectedColumn(int selectedColumn) {
		if (selectedColumn < -1 || selectedColumn >= getColumnCount()) {
			throw new IndexOutOfBoundsException(String.format(
					"Cannot set column %d on a table with %d columns",
					selectedColumn, getColumnCount()));
		}

		this.selectedColumn = selectedColumn;
	}

	/**
	 * An offset on the Y position of the table, i.e., the number of rows to
	 * skip so the control can draw that many rows always on top.
	 * 
	 * @return the offset
	 */
	public int getYOffset() {
		return yOffset;
	}

	/**
	 * An offset on the Y position of the table, i.e., the number of rows that
	 * should always stay on top.
	 * 
	 * @param yOffset
	 *            the new offset
	 */
	public void setYOffset(int yOffset) {
		this.yOffset = yOffset;
	}

	@SuppressWarnings("unused")
	public void dispatchMove(int fromRow, int toRow) {
		reflowData();
	}

	@SuppressWarnings("unused")
	public void dispatchEnter(int selectedRow) {
		reflowData();
	}

	@Override
	public void onMouseDown(final TMouseEvent mouse) {
		if (mouse.isMouseWheelUp()) {
			vScroller.decrement();
			return;
		}
		if (mouse.isMouseWheelDown()) {
			vScroller.increment();
			return;
		}

		if ((mouse.getX() < getWidth() - 1) && (mouse.getY() < getHeight() - 1)) {
			if (vScroller.getValue() + mouse.getY() < getRowCount()) {
				selectedRow = vScroller.getValue() + mouse.getY()
						- getYOffset();
			}
			dispatchEnter(selectedRow);
			return;
		}

		// Pass to children
		super.onMouseDown(mouse);
	}

	@Override
	public void onKeypress(final TKeypressEvent keypress) {
		int maxX = getRowCount();
		int prevSelectedRow = selectedRow;

		int firstLineIndex = vScroller.getValue() - getYOffset() + 2;
		int lastLineIndex = firstLineIndex - hScroller.getHeight()
				+ getHeight() - 2 - 2;

		if (keypress.equals(kbLeft)) {
			hScroller.decrement();
		} else if (keypress.equals(kbRight)) {
			hScroller.increment();
		} else if (keypress.equals(kbUp)) {
			if (maxX > 0 && selectedRow < maxX) {
				if (selectedRow > 0) {
					if (selectedRow <= firstLineIndex) {
						vScroller.decrement();
					}
					selectedRow--;
				} else {
					selectedRow = 0;
				}

				dispatchMove(prevSelectedRow, selectedRow);
			}
		} else if (keypress.equals(kbDown)) {
			if (maxX > 0) {
				if (selectedRow >= 0) {
					if (selectedRow < maxX - 1) {
						selectedRow++;
						if (selectedRow >= lastLineIndex) {
							vScroller.increment();
						}
					}
				} else {
					selectedRow = 0;
				}

				dispatchMove(prevSelectedRow, selectedRow);
			}
		} else if (keypress.equals(kbPgUp)) {
			if (selectedRow >= 0) {
				vScroller.bigDecrement();
				selectedRow -= getHeight() - 1;
				if (selectedRow < 0) {
					selectedRow = 0;
				}

				dispatchMove(prevSelectedRow, selectedRow);
			}
		} else if (keypress.equals(kbPgDn)) {
			if (selectedRow >= 0) {
				vScroller.bigIncrement();
				selectedRow += getHeight() - 1;
				if (selectedRow > getRowCount() - 1) {
					selectedRow = getRowCount() - 1;
				}

				dispatchMove(prevSelectedRow, selectedRow);
			}
		} else if (keypress.equals(kbHome)) {
			if (getRowCount() > 0) {
				vScroller.toTop();
				selectedRow = 0;
				dispatchMove(prevSelectedRow, selectedRow);
			}
		} else if (keypress.equals(kbEnd)) {
			if (getRowCount() > 0) {
				vScroller.toBottom();
				selectedRow = getRowCount() - 1;
				dispatchMove(prevSelectedRow, selectedRow);
			}
		} else if (keypress.equals(kbTab)) {
			getParent().switchWidget(true);
		} else if (keypress.equals(kbShiftTab) || keypress.equals(kbBackTab)) {
			getParent().switchWidget(false);
		} else if (keypress.equals(kbEnter)) {
			if (selectedRow >= 0) {
				dispatchEnter(selectedRow);
			}
		} else {
			// Pass other keys (tab etc.) on
			super.onKeypress(keypress);
		}
	}

	@Override
	public void onResize(TResizeEvent event) {
		super.onResize(event);
		reflowData();
	}

	@Override
	public void reflowData() {
		super.reflowData();
		fixScrollers();
	}

	private void fixScrollers() {
		int width = getWidth() - 1; // vertical prio
		int height = getHeight();

		// TODO: why did we do that before?
		if (false) {
			width -= 2;
			height = -1;
		}

		int x = Math.max(0, width);
		int y = Math.max(0, height - 1);

		vScroller.setX(x);
		vScroller.setHeight(height);
		hScroller.setY(y);
		hScroller.setWidth(width);

		// TODO why did we use to add 2?
		// + 2 (for the border of the window)

		// virtual_size
		// + the other scroll bar size
		vScroller.setTopValue(0);
		vScroller.setBottomValue(Math.max(0, getVirtualHeight() - getHeight()
				+ hScroller.getHeight()));
		hScroller.setLeftValue(0);
		hScroller.setRightValue(Math.max(0, getVirtualWidth() - getWidth()
				+ vScroller.getWidth()));
	}
}
