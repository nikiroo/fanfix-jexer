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
import java.util.Arrays;
import java.util.Collection;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * The model of a {@link TTable}. It contains the data of the table and allows
 * you access to it.
 * <p>
 * Note that you don't need to send it the representation of the data, but the
 * data itself; {@link TTableCellRenderer} is the class responsible of
 * representing that data (you can change the headers renderer on a
 * {@link TTable} and the cells renderer on each of its {@link TTableColumn}).
 * <p>
 * It works in a similar way to the Java Swing version of it.
 * 
 * @author niki
 */
public class TTableModel implements TableModel {
	private TableModel model;

	/**
	 * Create a new {@link TTableModel} with the given data inside.
	 * 
	 * @param data
	 *            the data
	 */
	public TTableModel(Object[][] data) {
		this(convert(data));
	}

	/**
	 * Create a new {@link TTableModel} with the given data inside.
	 * 
	 * @param data
	 *            the data
	 */
	public TTableModel(
			final Collection<? extends Collection<? extends Object>> data) {

		int maxItemsPerRow = 0;
		for (Collection<? extends Object> rowOfData : data) {
			maxItemsPerRow = Math.max(maxItemsPerRow, rowOfData.size());
		}

		int i = 0;
		final Object[][] odata = new Object[data.size()][maxItemsPerRow];
		for (Collection<? extends Object> rowOfData : data) {
			odata[i] = new String[maxItemsPerRow];
			int j = 0;
			for (Object pieceOfData : rowOfData) {
				odata[i][j] = pieceOfData;
				j++;
			}
			i++;
		}

		final int maxItemsPerRowFinal = maxItemsPerRow;
		this.model = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return odata[rowIndex][columnIndex];
			}

			@Override
			public int getRowCount() {
				return odata.length;
			}

			@Override
			public int getColumnCount() {
				return maxItemsPerRowFinal;
			}
		};
	}

	@Override
	public int getRowCount() {
		return model.getRowCount();
	}

	@Override
	public int getColumnCount() {
		return model.getColumnCount();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return model.getColumnName(columnIndex);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return model.getColumnClass(columnIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return model.isCellEditable(rowIndex, columnIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return model.getValueAt(rowIndex, columnIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		model.setValueAt(aValue, rowIndex, columnIndex);
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		model.addTableModelListener(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		model.removeTableModelListener(l);
	}

	/**
	 * Helper method to convert an array to a collection.
	 * 
	 * @param <T>
	 * 
	 * @param data
	 *            the data
	 * 
	 * @return the data in another format
	 */
	static <T> Collection<Collection<T>> convert(T[][] data) {
		Collection<Collection<T>> dataCollection = new ArrayList<Collection<T>>(
				data.length);
		for (T pieceOfData[] : data) {
			dataCollection.add(Arrays.asList(pieceOfData));
		}

		return dataCollection;
	}
}
