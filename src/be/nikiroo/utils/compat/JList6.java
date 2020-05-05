package be.nikiroo.utils.compat;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

/**
 * Compatibility layer so I can at least get rid of the warnings of using
 * {@link JList} without a parameter (and still staying Java 1.6 compatible).
 * <p>
 * This class is merely a {@link JList} that you can parametrise also in Java
 * 1.6.
 * 
 * @author niki
 *
 * @param <E>
 *            the type to use
 */
@SuppressWarnings({ "unchecked", "rawtypes" }) // not compatible Java 1.6
public class JList6<E> extends JList {
	private static final long serialVersionUID = 1L;

	@Override
	@Deprecated
	/**
	 * @deprecated please use {@link JList6#setCellRenderer(ListCellRenderer6)}
	 *             instead
	 */
	public void setCellRenderer(ListCellRenderer cellRenderer) {
		super.setCellRenderer(cellRenderer);
	}

	/**
	 * Sets the delegate that is used to paint each cell in the list. The job of
	 * a cell renderer is discussed in detail in the <a href="#renderer">class
	 * level documentation</a>.
	 * <p>
	 * If the {@code prototypeCellValue} property is {@code non-null}, setting
	 * the cell renderer also causes the {@code fixedCellWidth} and
	 * {@code fixedCellHeight} properties to be re-calculated. Only one
	 * <code>PropertyChangeEvent</code> is generated however - for the
	 * <code>cellRenderer</code> property.
	 * <p>
	 * The default value of this property is provided by the {@code ListUI}
	 * delegate, i.e. by the look and feel implementation.
	 * <p>
	 * This is a JavaBeans bound property.
	 *
	 * @param cellRenderer
	 *            the <code>ListCellRenderer</code> that paints list cells
	 * @see #getCellRenderer
	 * @beaninfo bound: true attribute: visualUpdate true description: The
	 *           component used to draw the cells.
	 */
	public void setCellRenderer(ListCellRenderer6<E> cellRenderer) {
		super.setCellRenderer(cellRenderer);
	}

	@Override
	@Deprecated
	public void setModel(ListModel model) {
		super.setModel(model);
	}

	/**
	 * Sets the model that represents the contents or "value" of the list,
	 * notifies property change listeners, and then clears the list's selection.
	 * <p>
	 * This is a JavaBeans bound property.
	 *
	 * @param model
	 *            the <code>ListModel</code> that provides the list of items for
	 *            display
	 * @exception IllegalArgumentException
	 *                if <code>model</code> is <code>null</code>
	 * @see #getModel
	 * @see #clearSelection
	 * @beaninfo bound: true attribute: visualUpdate true description: The
	 *           object that contains the data to be drawn by this JList.
	 */
	public void setModel(ListModel6<E> model) {
		super.setModel(model);
	}
}
