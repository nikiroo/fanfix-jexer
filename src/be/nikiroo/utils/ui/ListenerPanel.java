package be.nikiroo.utils.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JPanel;

/**
 * A {@link JPanel} with the default {@link ActionListener} add/remove/fire
 * methods.
 * <p>
 * Note that it will queue all events until at least one listener comes (or
 * comes back!); this first (or at least currently unique) listener will drain
 * the queue.
 * 
 * @author niki
 */
public class ListenerPanel extends JPanel implements ListenerItem {
	private static final long serialVersionUID = 1L;

	/** Waiting queue until at least one listener is here to get the events. */
	private final Queue<ActionEvent> waitingQueue;

	/**
	 * Create a new {@link ListenerPanel}.
	 */
	public ListenerPanel() {
		waitingQueue = new LinkedList<ActionEvent>();
	}

	@Override
	public synchronized boolean hasListeners() {
		return listenerList.getListenerList().length > 1;
	}

	@Override
	public synchronized int getWaitingEventCount() {
		return waitingQueue.size();
	}

	@Override
	public synchronized void addActionListener(ActionListener listener) {
		if (!hasListeners()) {
			while (!waitingQueue.isEmpty()) {
				listener.actionPerformed(waitingQueue.remove());
			}
		}

		listenerList.add(ActionListener.class, listener);
	}

	@Override
	public synchronized void removeActionListener(ActionListener listener) {
		listenerList.remove(ActionListener.class, listener);
	}

	@Override
	public synchronized void fireActionPerformed(String listenerCommand) {
		ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
				listenerCommand);

		ActionListener[] listeners = getListeners(ActionListener.class);
		if (listeners.length > 0) {
			for (ActionListener action : listeners) {
				action.actionPerformed(e);
			}
		} else {
			waitingQueue.add(e);
		}
	}
}
