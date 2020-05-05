package be.nikiroo.utils.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.SwingWorker;

/**
 * This class helps you delay some graphical actions and execute the most recent
 * ones when under contention.
 * <p>
 * How does it work?
 * <ul>
 * <li>it takes an ID and an associated {@link SwingWorker} and will call
 * {@link SwingWorker#execute()} after a small delay (see
 * {@link DelayWorker#DelayWorker(int)})</li>
 * <li>if a second call to {@link DelayWorker#delay(String, SwingWorker)} comes
 * with the same ID before the first one is done, it will be put on a waiting
 * queue</li>
 * <li>if a third call still with the same ID comes, its associated worker will
 * <b>replace</b> the one in the queue (only one worker per ID in the queue,
 * always the latest one)</li>
 * <li>when the first worker is done, it will check the waiting queue and
 * execute that latest worker if any</li>
 * </ul>
 * 
 * @author niki
 *
 */
@SuppressWarnings("rawtypes")
public class DelayWorker {
	private Map<String, SwingWorker> lazyEnCours;
	private Object lazyEnCoursLock;

	private TreeSet<String> wip;

	private Object waiter;

	private boolean cont;
	private boolean paused;
	private Thread loop;

	/**
	 * Create a new {@link DelayWorker} with the given delay (in milliseconds)
	 * before each drain of the queue.
	 * 
	 * @param delayMs
	 *            the delay in milliseconds (can be 0, cannot be negative)
	 */
	public DelayWorker(final int delayMs) {
		if (delayMs < 0) {
			throw new IllegalArgumentException(
					"A waiting delay cannot be negative");
		}

		lazyEnCours = new HashMap<String, SwingWorker>();
		lazyEnCoursLock = new Object();
		wip = new TreeSet<String>();
		waiter = new Object();
		cont = true;
		paused = false;

		loop = new Thread(new Runnable() {
			@Override
			public void run() {
				while (cont) {
					try {
						Thread.sleep(delayMs);
					} catch (InterruptedException e) {
					}

					Map<String, SwingWorker> workers = new HashMap<String, SwingWorker>();
					synchronized (lazyEnCoursLock) {
						for (String key : new ArrayList<String>(
								lazyEnCours.keySet())) {
							if (!wip.contains(key)) {
								workers.put(key, lazyEnCours.remove(key));
							}
						}
					}

					for (final String key : workers.keySet()) {
						SwingWorker worker = workers.get(key);

						synchronized (lazyEnCoursLock) {
							wip.add(key);
						}

						worker.addPropertyChangeListener(
								new PropertyChangeListener() {
									@Override
									public void propertyChange(
											PropertyChangeEvent evt) {
										synchronized (lazyEnCoursLock) {
											wip.remove(key);
										}
										wakeup();
									}
								});

						// Start it, at last
						worker.execute();
					}

					synchronized (waiter) {
						do {
							try {
								if (cont)
									waiter.wait();
							} catch (InterruptedException e) {
							}
						} while (cont && paused);
					}
				}
			}
		});

		loop.setDaemon(true);
		loop.setName("Loop for DelayWorker");
	}

	/**
	 * Start the internal loop that will drain the processing queue. <b>MUST
	 * NOT</b> be started twice (but see {@link DelayWorker#pause()} and
	 * {@link DelayWorker#resume()} instead).
	 */
	public void start() {
		loop.start();
	}

	/**
	 * Pause the system until {@link DelayWorker#resume()} is called -- note
	 * that it will still continue on the processes currently scheduled to run,
	 * but will pause after that.
	 * <p>
	 * Can be called even if already paused, will just do nothing in that
	 * context.
	 */
	public void pause() {
		paused = true;
	}

	/**
	 * Check if the {@link DelayWorker} is currently paused.
	 * 
	 * @return TRUE if it is
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * Resume the system after a pause.
	 * <p>
	 * Can be called even if already running, will just do nothing in that
	 * context.
	 */
	public void resume() {
		synchronized (waiter) {
			paused = false;
			wakeup();
		}
	}

	/**
	 * Stop the system.
	 * <p>
	 * Note: this is final, you <b>MUST NOT</b> call {@link DelayWorker#start()}
	 * a second time (but see {@link DelayWorker#pause()} and
	 * {@link DelayWorker#resume()} instead).
	 */
	public void stop() {
		synchronized (waiter) {
			cont = false;
			wakeup();
		}
	}

	/**
	 * Clear all the processes that were put on the queue but not yet scheduled
	 * to be executed -- note that it will still continue on the processes
	 * currently scheduled to run.
	 */
	public void clear() {
		synchronized (lazyEnCoursLock) {
			lazyEnCours.clear();
			wip.clear();
		}
	}

	/**
	 * Put a new process in the delay queue.
	 * 
	 * @param id
	 *            the ID of this process (if you want to skip workers when they
	 *            are superseded by a new one, you need to use the same ID key)
	 * @param worker
	 *            the process to delay
	 */
	public void delay(String id, SwingWorker worker) {
		synchronized (lazyEnCoursLock) {
			lazyEnCours.put(id, worker);
		}

		wakeup();
	}

	/**
	 * Wake up the loop thread.
	 */
	private void wakeup() {
		synchronized (waiter) {
			waiter.notifyAll();
		}
	}
}
