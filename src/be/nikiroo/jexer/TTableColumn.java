package be.nikiroo.jexer;

import javax.swing.table.TableModel;

import be.nikiroo.jexer.TTableCellRenderer.CellRendererMode;

public class TTableColumn {
	static private TTableCellRenderer defaultrenderer = new TTableCellRendererText(
			CellRendererMode.NORMAL);

	private TableModel model;
	private int modelIndex;
	private int width;
	private boolean forcedWidth;

	private TTableCellRenderer renderer;

	/** The auto-computed width of the column (the width of the largest value) */
	private int autoWidth;

	private Object headerValue;

	public TTableColumn(int modelIndex) {
		this(modelIndex, null);
	}

	public TTableColumn(int modelIndex, String colName) {
		this(modelIndex, colName, null);
	}

	// set the width and preferred with the the max data size
	public TTableColumn(int modelIndex, Object colValue, TableModel model) {
		this.model = model;
		this.modelIndex = modelIndex;

		reflowData();

		if (colValue != null) {
			setHeaderValue(colValue);
		}
	}

	// never null
	public TTableCellRenderer getRenderer() {
		return renderer != null ? renderer : defaultrenderer;
	}

	public void setCellRenderer(TTableCellRenderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * Recompute whatever data is displayed by this widget.
	 * <p>
	 * Will just update the sizes in this case.
	 */
	public void reflowData() {
		if (model != null) {
			int maxDataSize = 0;
			for (int i = 0; i < model.getRowCount(); i++) {
				maxDataSize = Math.max(
						maxDataSize,
						getRenderer().getWidthOf(
								model.getValueAt(i, modelIndex)));
			}

			autoWidth = maxDataSize;
			if (!forcedWidth) {
				setWidth(maxDataSize);
			}
		} else {
			autoWidth = 0;
			forcedWidth = false;
			width = 0;
		}
	}

	public int getModelIndex() {
		return modelIndex;
	}

	/**
	 * The actual size of the column. This can be auto-computed in some cases.
	 * 
	 * @return the width (never &lt; 0)
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Set the actual size of the column or -1 for auto size.
	 * 
	 * @param width
	 *            the width (or -1 for auto)
	 */
	public void setWidth(int width) {
		forcedWidth = width >= 0;

		if (forcedWidth) {
			this.width = width;
		} else {
			this.width = autoWidth;
		}
	}

	/**
	 * The width was forced by the user (using
	 * {@link TTableColumn#setWidth(int)} with a positive value).
	 * 
	 * @return TRUE if it was
	 */
	public boolean isForcedWidth() {
		return forcedWidth;
	}

	// not an actual forced width, but does change the width return
	void expandWidthTo(int width) {
		this.width = width;
	}

	public Object getHeaderValue() {
		return headerValue;
	}

	public void setHeaderValue(Object headerValue) {
		this.headerValue = headerValue;
	}
}
