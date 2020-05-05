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

import jexer.bits.CellAttributes;

/**
 * A simple {@link TTableCellRenderer} that display the values within a
 * {@link TLabel}.
 * <p>
 * It supports a few different modes, see
 * {@link TTableOldSimpleTextCellRenderer.CellRendererMode}.
 * 
 * @author niki
 */
public class TTableCellRendererText extends TTableCellRenderer {
	private boolean rightAlign;

	/**
	 * Create a new renderer for normal text mode.
	 */
	public TTableCellRendererText() {
		this(CellRendererMode.NORMAL);
	}

	/**
	 * Create a new renderer of the given mode.
	 * 
	 * @param mode
	 *            the renderer mode
	 */
	public TTableCellRendererText(CellRendererMode mode) {
		this(mode, false);
	}

	/**
	 * Create a new renderer of the given mode.
	 * 
	 * @param mode
	 *            the renderer mode, cannot be NULL
	 */
	public TTableCellRendererText(CellRendererMode mode,
			boolean rightAlign) {
		super(mode);

		this.rightAlign = rightAlign;
	}

	@Override
	public void renderTableCell(TTable table, Object value, int rowIndex,
			int colIndex, int y) {

		int xOffset = getXOffset(table, colIndex);
		TTableColumn tcol = table.getColumns().get(colIndex);
		String data = asText(value, tcol.getWidth(), rightAlign);

		if (!data.isEmpty()) {
			boolean isSelected = table.getSelectedRow() == rowIndex;
			boolean hasFocus = table.isAbsoluteActive();
			CellAttributes color = getCellAttributes(table.getWindow()
					.getApplication().getTheme(), isSelected, hasFocus);
			table.getScreen().putStringXY(xOffset, y, data, color);
		}
	}
}
