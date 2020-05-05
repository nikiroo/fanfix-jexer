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
import jexer.bits.ColorTheme;

/**
 * A {@link TTable} cell renderer allows you to customize the way a single cell
 * will be displayed on screen.
 * <p>
 * It can be used in a {@link TTable} for the haeders or the separators or in a
 * {@link TTableColumn} for the data.
 * 
 * @author niki
 */
abstract public class TTableCellRenderer {
	private CellRendererMode mode;

	/**
	 * The simple renderer mode.
	 * 
	 * @author niki
	 */
	public enum CellRendererMode {
		/** Normal text mode */
		NORMAL,
		/** Only display a separator */
		SEPARATOR,
		/** Header text mode */
		HEADER,
		/** Both HEADER and SEPARATOR at once */
		HEADER_SEPARATOR;

		/**
		 * This mode represents a separator.
		 * 
		 * @return TRUE for separators
		 */
		public boolean isSeparator() {
			return this == SEPARATOR || this == HEADER_SEPARATOR;
		}

		/**
		 * This mode represents a header.
		 * 
		 * @return TRUE for headers
		 */
		public boolean isHeader() {
			return this == HEADER || this == HEADER_SEPARATOR;
		}
	}

	/**
	 * Create a new renderer of the given mode.
	 * 
	 * @param mode
	 *            the renderer mode, cannot be NULL
	 */
	public TTableCellRenderer(CellRendererMode mode) {
		if (mode == null) {
			throw new IllegalArgumentException(
					"Cannot create a renderer of type NULL");
		}

		this.mode = mode;
	}

	/**
	 * Render the given value.
	 * 
	 * @param table
	 *            the table to write on
	 * @param value
	 *            the value to write
	 * @param rowIndex
	 *            the row index in the table
	 * @param colIndex
	 *            the column index in the table
	 * @param y
	 *            the Y position at which to draw this row
	 */
	abstract public void renderTableCell(TTable table, Object value,
			int rowIndex, int colIndex, int y);

	/**
	 * The mode of this {@link TTableCellRenderer}.
	 * 
	 * @return the mode
	 */
	public CellRendererMode getMode() {
		return mode;
	}

	/**
	 * The cell attributes to use for the given state.
	 * 
	 * @param theme
	 *            the color theme to use
	 * @param isSelected
	 *            TRUE if the cell is selected
	 * @param hasFocus
	 *            TRUE if the cell has focus
	 * 
	 * @return the attributes
	 */
	public CellAttributes getCellAttributes(ColorTheme theme,
			boolean isSelected, boolean hasFocus) {
		return theme.getColor(getColorKey(isSelected, hasFocus));
	}

	/**
	 * Measure the width of the value.
	 * 
	 * @param value
	 *            the value to measure
	 * 
	 * @return its width
	 */
	public int getWidthOf(Object value) {
		if (getMode().isSeparator()) {
			return asText(null, 0, false).length();
		}
		return ("" + value).length();
	}

	/**
	 * The colour to use for the given state, specified as a Jexer colour key.
	 * 
	 * @param isSelected
	 *            TRUE if the cell is selected
	 * @param hasFocus
	 *            TRUE if the cell has focus
	 * 
	 * @return the colour key
	 */
	protected String getColorKey(boolean isSelected, boolean hasFocus) {
		if (mode.isHeader()) {
			return "tlabel";
		}

		String colorKey = "tlist";
		if (isSelected) {
			colorKey += ".selected";
		} else if (!hasFocus) {
			colorKey += ".inactive";
		}

		return colorKey;
	}

	/**
	 * Return the X offset to use to draw a column at the given index.
	 * 
	 * @param table
	 *            the table to draw into
	 * @param colIndex
	 *            the column index
	 * 
	 * @return the offset
	 */
	protected int getXOffset(TTable table, int colIndex) {
		int xOffset = -table.getHorizontalValue();
		for (int i = 0; i <= colIndex; i++) {
			TTableColumn tcol = table.getColumns().get(i);
			xOffset += tcol.getWidth();
			if (i > 0) {
				xOffset += table.getSeparatorRenderer().getWidthOf(null);
			}
		}

		TTableColumn tcol = table.getColumns().get(colIndex);
		if (!getMode().isSeparator()) {
			xOffset -= tcol.getWidth();
		}

		return xOffset;
	}

	/**
	 * Return the text to use (usually the converted-to-text value, except for
	 * the special separator mode).
	 * 
	 * @param value
	 *            the value to get the text of
	 * @param width
	 *            the width we should tale
	 * @param align
	 *            the text to the right
	 * 
	 * @return the {@link String} to display
	 */
	protected String asText(Object value, int width, boolean rightAlign) {
		if (getMode().isSeparator()) {
			// some nice characters for the separator: ┃ │ |
			return " │ ";
		}

		if (width <= 0) {
			return "";
		}

		String format;
		if (!rightAlign) {
			// Left align
			format = "%-" + width + "s";
		} else {
			// right align
			format = "%" + width + "s";
		}

		return String.format(format, value);
	}
}