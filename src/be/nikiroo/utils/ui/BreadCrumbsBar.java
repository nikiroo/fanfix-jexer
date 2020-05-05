
package be.nikiroo.utils.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class BreadCrumbsBar<T> extends ListenerPanel {
	private class BreadCrumb extends JPanel {
		private JToggleButton button;
		private JToggleButton down;

		public BreadCrumb(final DataNode<T> node) {
			this.setLayout(new BorderLayout());

			if (!node.isRoot()) {
				button = new JToggleButton(node.toString());
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button.setSelected(false);
						if (!node.isRoot()) {
							// TODO: allow clicking on root? option?
							setSelectedNode(node);
						}
					}
				});
				
				this.add(button, BorderLayout.CENTER);
			}

			if (!node.getChildren().isEmpty()) {
				// TODO (see things with icons included in viewer)
				down = new JToggleButton(">");
				final JPopupMenu popup = new JPopupMenu();

				for (final DataNode<T> child : node.getChildren()) {
					popup.add(new AbstractAction(child.toString()) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							setSelectedNode(child);
						}
					});
				}

				down.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ev) {
						if (down.isSelected()) {
							popup.show(down, 0, down.getBounds().height);
						} else {
							popup.setVisible(false);
						}
					}
				});

				popup.addPopupMenuListener(new PopupMenuListener() {
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					}

					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
						down.setSelected(false);
					}

					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {
					}
				});

				this.add(down, BorderLayout.EAST);
			}
		}
	}

	static public final String CHANGE_ACTION = "change";

	private boolean vertical;
	private DataNode<T> node;
	private List<BreadCrumb> crumbs = new ArrayList<BreadCrumb>();

	public BreadCrumbsBar(final DataTree<T> tree) {
		vertical = true; // to force an update
		setVertical(false);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				synchronized (crumbs) {
					for (BreadCrumb crumb : crumbs) {
						setCrumbSize(crumb);
					}
				}
			}
		});

		new SwingWorker<DataNode<T>, Void>() {
			@Override
			protected DataNode<T> doInBackground() throws Exception {
				tree.loadData();
				return tree.getRoot();
			}

			@Override
			protected void done() {
				try {
					node = get();
					addCrumb(node);

					// TODO: option?
					if (node.size() > 0) {
						setSelectedNode(node.getChildren().get(0));
					} else {
						revalidate();
						repaint();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.execute();
	}

	public void setVertical(boolean vertical) {
		if (vertical != this.vertical) {
			synchronized (crumbs) {
				this.vertical = vertical;

				for (BreadCrumb crumb : crumbs) {
					this.remove(crumb);
				}

				if (vertical) {
					this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				} else {
					this.setLayout(new WrapLayout(WrapLayout.LEADING));
				}

				for (BreadCrumb crumb : crumbs) {
					this.add(crumb);
					setCrumbSize(crumb);
				}
			}

			this.revalidate();
			this.repaint();
		}
	}

	public DataNode<T> getSelectedNode() {
		return node;
	}

	public void setSelectedNode(DataNode<T> node) {
		if (this.node == node) {
			return;
		}

		synchronized (crumbs) {
			// clear until common ancestor (can clear all!)
			while (this.node != null && !this.node.isParentOf(node)) {
				this.node = this.node.getParent();
				this.remove(crumbs.remove(crumbs.size() - 1));
			}

			// switch root if needed and possible
			if (this.node == null && node != null) {
				this.node = node.getRoot();
				addCrumb(this.node);
			}

			// re-create until node
			while (node != null && this.node != node) {
				DataNode<T> ancestorOrNode = node;
				for (DataNode<T> child : this.node.getChildren()) {
					if (child.isParentOf(node))
						ancestorOrNode = child;
				}

				this.node = ancestorOrNode;
				addCrumb(this.node);
			}
		}

		this.revalidate();
		this.repaint();

		fireActionPerformed(CHANGE_ACTION);
	}

	private void addCrumb(DataNode<T> node) {
		BreadCrumb crumb = new BreadCrumb(node);
		this.crumbs.add(crumb);
		setCrumbSize(crumb);
		this.add(crumb);
	}

	private void setCrumbSize(BreadCrumb crumb) {
		if (vertical) {
			crumb.setMaximumSize(new Dimension(this.getWidth(),
					crumb.getMinimumSize().height));
		} else {
			crumb.setMaximumSize(null);
		}
	}
}
