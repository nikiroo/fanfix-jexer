package be.nikiroo.utils.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

public class DataNode<T> {
	private DataNode<T> parent;
	private List<? extends DataNode<T>> children;
	private T userData;

	public DataNode(List<? extends DataNode<T>> children, T userData) {
		if (children == null) {
			children = new ArrayList<DataNode<T>>();
		}

		this.children = children;
		this.userData = userData;

		for (DataNode<T> child : children) {
			child.parent = this;
		}
	}

	public DataNode<T> getRoot() {
		DataNode<T> root = this;
		while (root.parent != null) {
			root = root.parent;
		}

		return root;
	}

	public DataNode<T> getParent() {
		return parent;
	}

	public List<? extends DataNode<T>> getChildren() {
		return children;
	}

	public int size() {
		return children.size();
	}

	public boolean isRoot() {
		return this == getRoot();
	}

	public boolean isSiblingOf(DataNode<T> node) {
		if (this == node) {
			return true;
		}

		return node != null && parent != null && parent.children.contains(node);
	}

	public boolean isParentOf(DataNode<T> node) {
		if (node == null || node.parent == null)
			return false;

		if (this == node.parent)
			return true;

		return isParentOf(node.parent);
	}

	public boolean isChildOf(DataNode<T> node) {
		if (node == null || node.size() == 0)
			return false;

		return node.isParentOf(this);
	}

	public T getUserData() {
		return userData;
	}

	/**
	 * The total number of nodes present in this {@link DataNode} (including
	 * itself and descendants).
	 * 
	 * @return the number
	 */
	public int count() {
		int s = 1;
		for (DataNode<T> child : children) {
			s += child.count();
		}

		return s;
	}

	@Override
	public String toString() {
		if (userData == null) {
			return "";
		}

		return userData.toString();
	}
}