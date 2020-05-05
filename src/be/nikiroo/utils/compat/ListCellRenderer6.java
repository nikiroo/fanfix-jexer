package be.nikiroo.utils.compat;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * Compatibility layer so I can at least get rid of the warnings of using
 * {@link JList} without a parameter (and still staying Java 1.6 compatible).
 * <p>
 * This class is merely a {@link ListCellRenderer} that you can parametrise also
 * in Java 1.6.
 * 
 * @author niki
 *
 * @param <E>
 *            the type to use
 */
@SuppressWarnings({ "unchecked", "rawtypes" }) // not compatible Java 1.6
public abstract class ListCellRenderer6<E> implements ListCellRenderer {
	@Override
	@Deprecated
	/**
	 * @deprecated please use@deprecated please use
	 *             {@link ListCellRenderer6#getListCellRendererComponent(JList6, Object, int, boolean, boolean)}
	 *             instead
	 *             {@link ListCellRenderer6#getListCellRendererComponent(JList6, Object, int, boolean, boolean)}
	 *             instead
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		return getListCellRendererComponent((JList6<E>) list, (E) value, index,
				isSelected, cellHasFocus);
	}

	/**
	 * Return a component that has been configured to display the specified
	 * value. That component's <code>paint</code> method is then called to
	 * "render" the cell. If it is necessary to compute the dimensions of a list
	 * because the list cells do not have a fixed size, this method is called to
	 * generate a component on which <code>getPreferredSize</code> can be
	 * invoked.
	 *
	 * @param list
	 *            The JList we're painting.
	 * @param value
	 *            The value returned by list.getModel().getElementAt(index).
	 * @param index
	 *            The cells index.
	 * @param isSelected
	 *            True if the specified cell was selected.
	 * @param cellHasFocus
	 *            True if the specified cell has the focus.
	 * @return A component whose paint() method will render the specified value.
	 *
	 * @see JList
	 * @see ListSelectionModel
	 * @see ListModel
	 */
	public abstract Component getListCellRendererComponent(JList6<E> list,
			E value, int index, boolean isSelected, boolean cellHasFocus);
}
