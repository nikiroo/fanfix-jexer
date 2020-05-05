package be.nikiroo.utils.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class TreeSnapshot {
	private interface NodeAction {
		public void run(TreeNode node);
	}

	private JTree tree;
	private TreePath[] selectionPaths;
	private List<TreePath> expanded;

	public TreeSnapshot(JTree tree) {
		this.tree = tree;

		selectionPaths = tree.getSelectionPaths();
		if (selectionPaths == null) {
			selectionPaths = new TreePath[0];
		}

		expanded = new ArrayList<TreePath>();
		forEach(tree, new NodeAction() {
			@Override
			public void run(TreeNode node) {
				TreePath path = nodeToPath(node);
				if (path != null) {
					if (TreeSnapshot.this.tree.isExpanded(path)) {
						expanded.add(path);
					}
				}
			}
		});
	}

	public void apply() {
		applyTo(tree);
	}

	public void applyTo(JTree tree) {
		final List<TreePath> newExpanded = new ArrayList<TreePath>();
		final List<TreePath> newSlectionPaths = new ArrayList<TreePath>();

		forEach(tree, new NodeAction() {
			@Override
			public void run(TreeNode newNode) {
				TreePath newPath = nodeToPath(newNode);
				if (newPath != null) {
					for (TreePath path : selectionPaths) {
						if (isSamePath(path, newPath)) {
							newSlectionPaths.add(newPath);
							if (expanded.contains(path)) {
								newExpanded.add(newPath);
							}
						}
					}
				}
			}
		});

		for (TreePath newPath : newExpanded) {
			tree.expandPath(newPath);
		}

		tree.setSelectionPaths(newSlectionPaths.toArray(new TreePath[0]));
	}

	// You can override this
	protected boolean isSamePath(TreePath oldPath, TreePath newPath) {
		return newPath.toString().equals(oldPath.toString());
	}

	private void forEach(JTree tree, NodeAction action) {
		forEach(tree.getModel(), tree.getModel().getRoot(), action);
	}

	private void forEach(TreeModel model, Object parent, NodeAction action) {
		if (!(parent instanceof TreeNode))
			return;

		TreeNode node = (TreeNode) parent;

		action.run(node);
		int count = model.getChildCount(node);
		for (int i = 0; i < count; i++) {
			Object child = model.getChild(node, i);
			forEach(model, child, action);
		}
	}

	private static TreePath nodeToPath(TreeNode node) {
		List<Object> nodes = new LinkedList<Object>();
		if (node != null) {
			nodes.add(node);
			node = node.getParent();
			while (node != null) {
				nodes.add(0, node);
				node = node.getParent();
			}
		}

		return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Tree Snapshot of: ").append(tree).append("\n");
		builder.append("Selected paths:\n");
		for (TreePath path : selectionPaths) {
			builder.append("\t").append(path).append("\n");
		}
		builder.append("Expanded paths:\n");
		for (TreePath epath : expanded) {
			builder.append("\t").append(epath).append("\n");
		}

		return builder.toString();
	}
}
