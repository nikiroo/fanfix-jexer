package be.nikiroo.utils.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import be.nikiroo.utils.Progress;

/**
 * A graphical control to show the progress of a {@link Progress}.
 * <p>
 * This control is <b>NOT</b> thread-safe.
 * 
 * @author niki
 */
public class ProgressBar extends JPanel {
	private static final long serialVersionUID = 1L;

	private Map<Progress, JProgressBar> bars;
	private List<ActionListener> actionListeners;
	private List<ActionListener> updateListeners;
	private Progress pg;
	private Object lock = new Object();

	public ProgressBar() {
		bars = new HashMap<Progress, JProgressBar>();
		actionListeners = new ArrayList<ActionListener>();
		updateListeners = new ArrayList<ActionListener>();
	}
	
	public ProgressBar(Progress pg) {
		this();
		setProgress(pg);
	}

	public void setProgress(final Progress pg) {
		this.pg = pg;

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (pg != null) {
					final JProgressBar bar = new JProgressBar();
					bar.setStringPainted(true);

					bars.clear();
					bars.put(pg, bar);

					bar.setMinimum(pg.getMin());
					bar.setMaximum(pg.getMax());
					bar.setValue(pg.getProgress());
					bar.setString(pg.getName());

					pg.addProgressListener(new Progress.ProgressListener() {
						@Override
						public void progress(Progress progress, String name) {
							final Progress.ProgressListener l = this;
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									Map<Progress, JProgressBar> newBars = new HashMap<Progress, JProgressBar>();
									newBars.put(pg, bar);

									bar.setMinimum(pg.getMin());
									bar.setMaximum(pg.getMax());
									bar.setValue(pg.getProgress());
									bar.setString(pg.getName());

									synchronized (lock) {
									for (Progress pgChild : getChildrenAsOrderedList(pg)) {
										JProgressBar barChild = bars
												.get(pgChild);
										if (barChild == null) {
											barChild = new JProgressBar();
											barChild.setStringPainted(true);
										}

										newBars.put(pgChild, barChild);

										barChild.setMinimum(pgChild.getMin());
										barChild.setMaximum(pgChild.getMax());
										barChild.setValue(pgChild.getProgress());
										barChild.setString(pgChild.getName());
									}
									
									if (ProgressBar.this.pg == null) {
										bars.clear();
									} else {
										bars = newBars;
									}
									}
									
									if (ProgressBar.this.pg != null) {
										if (pg.isDone()) {
											pg.removeProgressListener(l);
											for (ActionListener listener : actionListeners) {
												listener.actionPerformed(new ActionEvent(
														ProgressBar.this, 0,
														"done"));
											}
										}

										update();
									}
								}
							});
						}
					});
				}

				update();
			}
		});
	}

	public void addActionListener(ActionListener l) {
		actionListeners.add(l);
	}

	public void clearActionListeners() {
		actionListeners.clear();
	}

	public void addUpdateListener(ActionListener l) {
		updateListeners.add(l);
	}

	public void clearUpdateListeners() {
		updateListeners.clear();
	}

	public int getProgress() {
		if (pg == null) {
			return 0;
		}

		return pg.getProgress();
	}

	// only named ones
	private List<Progress> getChildrenAsOrderedList(Progress pg) {
		List<Progress> children = new ArrayList<Progress>();
		
		synchronized (lock) {
			for (Progress child : pg.getChildren()) {
			if (child.getName() != null && !child.getName().isEmpty()) {
				children.add(child);
			}
			children.addAll(getChildrenAsOrderedList(child));
		}
		}
		
		return children;
	}

	private void update() {
		synchronized (lock) {
			invalidate();
			removeAll();

			if (pg != null) {
				setLayout(new GridLayout(bars.size(), 1));
				add(bars.get(pg), 0);
				for (Progress child : getChildrenAsOrderedList(pg)) {
					JProgressBar jbar = bars.get(child);
					if (jbar != null) {
						add(jbar);
					}
				}
			}

			validate();
			repaint();
		}

		for (ActionListener listener : updateListeners) {
			listener.actionPerformed(new ActionEvent(this, 0, "update"));
		}
	}
}
