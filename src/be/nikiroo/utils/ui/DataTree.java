package be.nikiroo.utils.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public abstract class DataTree<E> {
	protected DataNode<E> data;

	public DataNode<E> loadData() throws IOException {
		return this.data = extractData();
	}

	public DataNode<E> getRoot() {
		return getRoot(null);
	}

	public DataNode<E> getRoot(String filter) {
		return filterNode(data, filter);
	}

	protected abstract DataNode<E> extractData() throws IOException;

	// filter cannot be null nor empty
	protected abstract boolean checkFilter(String filter, E userData);

	protected boolean checkFilter(DataNode<E> node, String filter) {
		if (filter == null || filter.isEmpty()) {
			return true;
		}

		if (checkFilter(filter, node.getUserData()))
			return true;

		for (DataNode<E> child : node.getChildren()) {
			if (checkFilter(child, filter))
				return true;
		}

		return false;
	}

	protected void sort(List<String> values) {
		Collections.sort(values, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return ("" + o1).compareToIgnoreCase("" + o2);
			}
		});
	}

	// note: we always send TAHT node, but filter children
	private DataNode<E> filterNode(DataNode<E> source, String filter) {
		List<DataNode<E>> children = new ArrayList<DataNode<E>>();
		for (DataNode<E> child : source.getChildren()) {
			if (checkFilter(child, filter)) {
				children.add(filterNode(child, filter));
			}
		}

		return new DataNode<E>(children, source.getUserData());
	}
}
