/*
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published
 *    by the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
// Can be found at: https://code.google.com/archive/p/aephyr/source/default/source
// package aephyr.swing;
package be.nikiroo.utils.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

import java.util.*;

public class TreeCellSpanner extends Container implements TreeCellRenderer, ComponentListener {
	
	public TreeCellSpanner(JTree tree, TreeCellRenderer renderer) {
		if (tree == null || renderer == null)
			throw new NullPointerException();
		this.tree = tree;
		this.renderer = renderer;
		treeParent = tree.getParent();
		if (treeParent != null && treeParent instanceof JViewport) {
			treeParent.addComponentListener(this);
		} else {
			treeParent = null;
			tree.addComponentListener(this);
		}
	}
	
	protected final JTree tree;
	
	private TreeCellRenderer renderer;
	
	private Component rendererComponent;
	
	private Container treeParent;
	
	private Map<TreePath,Integer> offsets = new HashMap<TreePath,Integer>();
	
	private TreePath path;
	
	public TreeCellRenderer getRenderer() {
		return renderer;
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		path = tree.getPathForRow(row);
		if (path != null && path.getLastPathComponent() != value)
			path = null;
		rendererComponent = renderer.getTreeCellRendererComponent(
				tree, value, selected, expanded, leaf, row, hasFocus);
		if (getComponentCount() < 1 || getComponent(0) != rendererComponent) {
			removeAll();
			add(rendererComponent);
		}
		return this;
	}
	
	@Override
	public void doLayout() {
		int x = getX();
		if (x < 0)
			return;
		if (path != null) {
			Integer offset = offsets.get(path);
			if (offset == null || offset.intValue() != x) {
				offsets.put(path, x);
				fireTreePathChanged(path);
			}
		}
		rendererComponent.setBounds(getX(), getY(), getWidth(), getHeight());
	}
	
	@Override
	public void paint(Graphics g) {
		if (rendererComponent != null)
			rendererComponent.paint(g);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension s = rendererComponent.getPreferredSize();
		// check if path count is greater than 1 to exclude the root
		if (path != null && path.getPathCount() > 1) {
			Integer offset = offsets.get(path);
			if (offset != null) {
				int width;
				if (tree.getParent() == treeParent) {
					width = treeParent.getWidth();
				} else {
					if (treeParent != null) {
						treeParent.removeComponentListener(this);
						tree.addComponentListener(this);
						treeParent = null;
					}
					if (tree.getParent() instanceof JViewport) {
						treeParent = tree.getParent();
						tree.removeComponentListener(this);
						treeParent.addComponentListener(this);
						width = treeParent.getWidth();
					} else {
						width = tree.getWidth();
					}
				}
				s.width = width - offset;
			}
		}
		return s;
	}

	
	protected void fireTreePathChanged(TreePath path) {
		if (path.getPathCount() > 1) {
			// this cannot be used for the root node or else
			// the entire tree will keep being revalidated ad infinitum
			TreeModel model = tree.getModel();
			Object node = path.getLastPathComponent();
			if (node instanceof TreeNode && (model instanceof DefaultTreeModel
					|| (model instanceof TreeModelTransformer<?> &&
					(model=((TreeModelTransformer<?>)model).getModel()) instanceof DefaultTreeModel))) {
				((DefaultTreeModel)model).nodeChanged((TreeNode)node);
			} else {
				model.valueForPathChanged(path, node.toString());
			}
		} else {
			// root!
			
		}
	}

	
	private int lastWidth;

	@Override
	public void componentHidden(ComponentEvent e) {}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {
		if (e.getComponent().getWidth() != lastWidth) {
			lastWidth = e.getComponent().getWidth();
			for (int row=tree.getRowCount(); --row>=0;) {
				fireTreePathChanged(tree.getPathForRow(row));
			}
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {}
	
}
