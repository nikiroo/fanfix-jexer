package be.nikiroo.utils.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;

public class ListSnapshot {
	private JList list;
	private List<Object> elements = new ArrayList<Object>();

	public ListSnapshot(JList list) {
		this.list = list;

		for (int index : list.getSelectedIndices()) {
			elements.add(list.getModel().getElementAt(index));
		}
	}

	public void apply() {
		applyTo(list);
	}

	public void applyTo(JList list) {
		List<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < list.getModel().getSize(); i++) {
			Object newObject = list.getModel().getElementAt(i);
			for (Object oldObject : elements) {
				if (isSameElement(oldObject, newObject)) {
					indices.add(i);
					break;
				}
			}
		}

		int a[] = new int[indices.size()];
		for (int i = 0; i < indices.size(); i++) {
			a[i] = indices.get(i);
		}
		list.setSelectedIndices(a);
	}

	// You can override this
	protected boolean isSameElement(Object oldElement, Object newElement) {
		if (oldElement == null || newElement == null)
			return oldElement == null && newElement == null;

		return oldElement.toString().equals(newElement.toString());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("List Snapshot of: ").append(list).append("\n");
		builder.append("Selected elements:\n");
		for (Object element : elements) {
			builder.append("\t").append(element).append("\n");
		}

		return builder.toString();
	}
}
