package be.nikiroo.utils.ui;

import java.awt.event.ActionListener;

/**
 * The default {@link ActionListener} add/remove/fire methods.
 * 
 * @author niki
 */
public interface ListenerItem {
	/**
	 * Check that this {@link ListenerItem} currently has
	 * {@link ActionListener}s that listen on it.
	 * 
	 * @return TRUE if it has
	 */
	public boolean hasListeners();

	/**
	 * Check how many events are currently waiting for an
	 * {@link ActionListener}.
	 * 
	 * @return the number of waiting events (can be 0)
	 */
	public int getWaitingEventCount();

	/**
	 * Adds the specified action listener to receive action events from this
	 * {@link ListenerItem}.
	 *
	 * @param listener
	 *            the action listener to be added
	 */
	public void addActionListener(ActionListener listener);

	/**
	 * Removes the specified action listener so that it no longer receives
	 * action events from this {@link ListenerItem}.
	 *
	 * @param listener
	 *            the action listener to be removed
	 */
	public void removeActionListener(ActionListener listener);

	/**
	 * Notify the listeners of an action.
	 * 
	 * @param listenerCommand
	 *            A string that may specify a command (possibly one of several)
	 *            associated with the event
	 */
	public void fireActionPerformed(String listenerCommand);
}
