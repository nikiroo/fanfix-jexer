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
 * @version 1
 */
package be.nikiroo.jexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.TableModel;

import be.nikiroo.jexer.TTableCellRenderer.CellRendererMode;
import jexer.TAction;
import jexer.TWidget;
import jexer.bits.CellAttributes;

/**
 * A table widget to display and browse through tabular data.
 * <p>
 * Currently, you can only select a line (a row) at a time, but the data you
 * present is still tabular. You also access the data in a tabular way (by
 * <tt>(raw,column)</tt>).
 * 
 * @author niki
 */
public class TTable extends TBrowsableWidget {
	// Default renderers use text mode
	static private TTableCellRenderer defaultSeparatorRenderer = new TTableCellRendererText(
			CellRendererMode.SEPARATOR);
	static private TTableCellRenderer defaultHeaderRenderer = new TTableCellRendererText(
			CellRendererMode.HEADER);
	static private TTableCellRenderer defaultHeaderSeparatorRenderer = new TTableCellRendererText(
			CellRendererMode.HEADER_SEPARATOR);

	private boolean showHeader;

	private List<TTableColumn> columns = new ArrayList<TTableColumn>();
	private TableModel model;

	private int selectedColumn;

	private TTableCellRenderer separatorRenderer;
	private TTableCellRenderer headerRenderer;
	private TTableCellRenderer headerSeparatorRenderer;

	/**
	 * The action to perform when the user selects an item (clicks or enter).
	 */
	private TAction enterAction = null;

	/**
	 * The action to perform when the user navigates with keyboard.
	 */
	private TAction moveAction = null;

	/**
	 * Create a new {@link TTable}.
	 * 
	 * @param parent
	 *            the parent widget
	 * @param x
	 *            the X position
	 * @param y
	 *            the Y position
	 * @param width
	 *            the width of the {@link TTable}
	 * @param height
	 *            the height of the {@link TTable}
	 * @param enterAction
	 *            an action to call when a cell is selected
	 * @param moveAction
	 *            an action to call when the currently active cell is changed
	 */
	public TTable(TWidget parent, int x, int y, int width, int height,
			final TAction enterAction, final TAction moveAction) {
		this(parent, x, y, width, height, enterAction, moveAction, null, false);
	}

	/**
	 * Create a new {@link TTable}.
	 * 
	 * @param parent
	 *            the parent widget
	 * @param x
	 *            the X position
	 * @param y
	 *            the Y position
	 * @param width
	 *            the width of the {@link TTable}
	 * @param height
	 *            the height of the {@link TTable}
	 * @param enterAction
	 *            an action to call when a cell is selected
	 * @param moveAction
	 *            an action to call when the currently active cell is changed
	 * @param headers
	 *            the headers of the {@link TTable}
	 * @param showHeaders
	 *            TRUE to show the headers on screen
	 */
	public TTable(TWidget parent, int x, int y, int width, int height,
			final TAction enterAction, final TAction moveAction,
			List<? extends Object> headers, boolean showHeaders) {
		super(parent, x, y, width, height);

		this.model = new TTableModel(new Object[][] {});
		setSelectedRow(-1);
		this.selectedColumn = -1;

		setHeaders(headers, showHeaders);

		this.enterAction = enterAction;
		this.moveAction = moveAction;

		reflowData();
	}

	/**
	 * The data model (containing the actual data) used by this {@link TTable},
	 * as with the usual Swing tables.
	 * 
	 * @return the model
	 */
	public TableModel getModel() {
		return model;
	}

	/**
	 * The data model (containing the actual data) used by this {@link TTable},
	 * as with the usual Swing tables.
	 * <p>
	 * Will reset all the rendering cells.
	 * 
	 * @param model
	 *            the new model
	 */
	public void setModel(TableModel model) {
		this.model = model;
		reflowData();
	}

	/**
	 * The columns used by this {@link TTable} (you need to access them if you
	 * want to change the way they are rendered, for instance, or their size).
	 * 
	 * @return the columns
	 */
	public List<TTableColumn> getColumns() {
		return columns;
	}

	/**
	 * The {@link TTableCellRenderer} used by the separators (one separator
	 * between two data columns).
	 * 
	 * @return the renderer, or the default one if none is set (never NULL)
	 */
	public TTableCellRenderer getSeparatorRenderer() {
		return separatorRenderer != null ? separatorRenderer
				: defaultSeparatorRenderer;
	}

	/**
	 * The {@link TTableCellRenderer} used by the separators (one separator
	 * between two data columns).
	 * 
	 * @param separatorRenderer
	 *            the new renderer, or NULL to use the default renderer
	 */
	public void setSeparatorRenderer(TTableCellRenderer separatorRenderer) {
		this.separatorRenderer = separatorRenderer;
	}

	/**
	 * The {@link TTableCellRenderer} used by the headers (if
	 * {@link TTable#isShowHeader()} is enabled, the first line represents the
	 * headers with the column names).
	 * 
	 * @return the renderer, or the default one if none is set (never NULL)
	 */
	public TTableCellRenderer getHeaderRenderer() {
		return headerRenderer != null ? headerRenderer : defaultHeaderRenderer;
	}

	/**
	 * The {@link TTableCellRenderer} used by the headers (if
	 * {@link TTable#isShowHeader()} is enabled, the first line represents the
	 * headers with the column names).
	 * 
	 * @param headerRenderer
	 *            the new renderer, or NULL to use the default renderer
	 */
	public void setHeaderRenderer(TTableCellRenderer headerRenderer) {
		this.headerRenderer = headerRenderer;
	}

	/**
	 * The {@link TTableCellRenderer} to use on separators in header lines (see
	 * the related methods to understand what each of them is).
	 * 
	 * @return the renderer, or the default one if none is set (never NULL)
	 */
	public TTableCellRenderer getHeaderSeparatorRenderer() {
		return headerSeparatorRenderer != null ? headerSeparatorRenderer
				: defaultHeaderSeparatorRenderer;
	}

	/**
	 * The {@link TTableCellRenderer} to use on separators in header lines (see
	 * the related methods to understand what each of them is).
	 * 
	 * @param headerSeparatorRenderer
	 *            the new renderer, or NULL to use the default renderer
	 */
	public void setHeaderSeparatorRenderer(
			TTableCellRenderer headerSeparatorRenderer) {
		this.headerSeparatorRenderer = headerSeparatorRenderer;
	}

	/**
	 * Show the header row on this {@link TTable}.
	 * 
	 * @return TRUE if we show them
	 */
	public boolean isShowHeader() {
		return showHeader;
	}

	/**
	 * Show the header row on this {@link TTable}.
	 * 
	 * @param showHeader
	 *            TRUE to show them
	 */
	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
		setYOffset(showHeader ? 2 : 0);
		reflowData();
	}

	/**
	 * Change the headers of the table.
	 * <p>
	 * Note that this method is a convenience method that will create columns of
	 * the corresponding names and set them. As such, the previous columns if
	 * any will be replaced.
	 * 
	 * @param headers
	 *            the new headers
	 */
	public void setHeaders(List<? extends Object> headers) {
		setHeaders(headers, showHeader);
	}

	/**
	 * Change the headers of the table.
	 * <p>
	 * Note that this method is a convenience method that will create columns of
	 * the corresponding names and set them in the same order. As such, the
	 * previous columns if any will be replaced.
	 * 
	 * @param headers
	 *            the new headers
	 * @param showHeader
	 *            TRUE to show them on screen
	 */
	public void setHeaders(List<? extends Object> headers, boolean showHeader) {
		if (headers == null) {
			headers = new ArrayList<Object>();
		}

		int i = 0;
		this.columns = new ArrayList<TTableColumn>();
		for (Object header : headers) {
			this.columns.add(new TTableColumn(i++, header, getModel()));
		}

		setShowHeader(showHeader);
	}

	/**
	 * Set the data and create a new {@link TTableModel} for them.
	 * 
	 * @param data
	 *            the data to set into this table, as an array of rows, that is,
	 *            an array of arrays of values
	 */

	public void setRowData(Object[][] data) {
		setRowData(TTableModel.convert(data));
	}

	/**
	 * Set the data and create a new {@link TTableModel} for them.
	 * 
	 * @param data
	 *            the data to set into this table, as a collection of rows, that
	 *            is, a collection of collections of values
	 */
	public void setRowData(
			final Collection<? extends Collection<? extends Object>> data) {
		setModel(new TTableModel(data));
	}

	/**
	 * The currently selected cell.
	 * 
	 * @return the cell
	 */
	public Object getSelectedCell() {
		int selectedRow = getSelectedRow();
		if (selectedRow >= 0 && selectedColumn >= 0) {
			return model.getValueAt(selectedRow, selectedColumn);
		}

		return null;
	}

	@Override
	public int getRowCount() {
		if (model == null) {
			return 0;
		}
		return model.getRowCount();
	}

	@Override
	public int getColumnCount() {
		if (model == null) {
			return 0;
		}
		return model.getColumnCount();
	}

	@Override
	public void dispatchEnter(int selectedRow) {
		super.dispatchEnter(selectedRow);
		if (enterAction != null) {
			enterAction.DO();
		}
	}

	@Override
	public void dispatchMove(int fromRow, int toRow) {
		super.dispatchMove(fromRow, toRow);
		if (moveAction != null) {
			moveAction.DO();
		}
	}

	/**
	 * Clear the content of the {@link TTable}.
	 * <p>
	 * It will not affect the headers.
	 * <p>
	 * You may want to call {@link TTable#reflowData()} when done to see the
	 * changes.
	 */
	public void clear() {
		setSelectedRow(-1);
		selectedColumn = -1;
		setModel(new TTableModel(new Object[][] {}));
	}

	@Override
	public void reflowData() {
		super.reflowData();

		int lastAutoColumn = -1;
		int rowWidth = 0;

		int i = 0;
		for (TTableColumn tcol : columns) {
			tcol.reflowData();

			if (!tcol.isForcedWidth()) {
				lastAutoColumn = i;
			}

			rowWidth += tcol.getWidth();

			i++;
		}

		if (!columns.isEmpty()) {
			rowWidth += (i - 1) * getSeparatorRenderer().getWidthOf(null);

			int extraWidth = getWidth() - rowWidth;
			if (extraWidth > 0) {
				if (lastAutoColumn < 0) {
					lastAutoColumn = columns.size() - 1;
				}
				TTableColumn tcol = columns.get(lastAutoColumn);
				tcol.expandWidthTo(tcol.getWidth() + extraWidth);
				rowWidth += extraWidth;
			}
		}
	}

	@Override
	public void draw() {
		int begin = vScroller.getValue();
		int y = this.showHeader ? 2 : 0;

		if (showHeader) {
			CellAttributes colorHeaders = getHeaderRenderer()
					.getCellAttributes(getTheme(), false, isAbsoluteActive());
			drawRow(-1, 0);
			String formatString = "%-" + Integer.toString(getWidth()) + "s";
			String data = String.format(formatString, "");
			getScreen().putStringXY(0, 1, data, colorHeaders);
		}

		// draw the actual rows until no more,
		// then pad the rest with blank rows
		for (int i = begin; i < getRowCount(); i++) {
			drawRow(i, y);
			y++;

			// -2: window borders
			if (y >= getHeight() - 2 - getHorizontalScroller().getHeight()) {
				break;
			}
		}

		CellAttributes emptyRowColor = getSeparatorRenderer()
				.getCellAttributes(getTheme(), false, isAbsoluteActive());
		for (int i = getRowCount(); i < getHeight(); i++) {
			getScreen().hLineXY(0, y, getWidth() - 1, ' ', emptyRowColor);
			y++;
		}
	}

	@Override
	protected int getVirtualWidth() {
		int width = 0;

		if (getColumns() != null) {
			for (TTableColumn tcol : getColumns()) {
				width += tcol.getWidth();
			}

			if (getColumnCount() > 0) {
				width += (getColumnCount() - 1)
						* getSeparatorRenderer().getWidthOf(null);
			}
		}

		return width;
	}

	@Override
	protected int getVirtualHeight() {
		// TODO: allow changing the height of one row
		return (showHeader ? 2 : 0) + (getRowCount() * 1);
	}

	/**
	 * Draw the given row (it <b>MUST</b> exist) at the specified index and
	 * offset.
	 * 
	 * @param rowIndex
	 *            the index of the row to draw or -1 for the headers
	 * @param y
	 *            the Y position
	 */
	private void drawRow(int rowIndex, int y) {
		for (int i = 0; i < getColumnCount(); i++) {
			TTableColumn tcol = columns.get(i);
			Object value;
			if (rowIndex < 0) {
				value = tcol.getHeaderValue();
			} else {
				value = model.getValueAt(rowIndex, tcol.getModelIndex());
			}

			if (i > 0) {
				TTableCellRenderer sep = rowIndex < 0 ? getHeaderSeparatorRenderer()
						: getSeparatorRenderer();
				sep.renderTableCell(this, null, rowIndex, i - 1, y);
			}

			if (rowIndex < 0) {
				getHeaderRenderer()
						.renderTableCell(this, value, rowIndex, i, y);
			} else {
				tcol.getRenderer().renderTableCell(this, value, rowIndex, i, y);
			}
		}
	}
}
