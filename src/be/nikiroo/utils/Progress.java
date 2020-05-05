package be.nikiroo.utils;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Progress reporting system, possibly nested.
 * <p>
 * A {@link Progress} can have a name, and that name will be reported through
 * the event system (it will report the first non-null name in the stack from
 * the {@link Progress} from which the event originated to the parent the event
 * is listened on).
 * <p>
 * The {@link Progress} also has a table of keys/values shared amongst all the
 * hierarchy (note that when adding a {@link Progress} to others, its values
 * will be prioritized if some with the same keys were already present in the
 * hierarchy).
 * 
 * @author niki
 */
public class Progress {
	/**
	 * This event listener is designed to report progress events from
	 * {@link Progress}.
	 * 
	 * @author niki
	 */
	public interface ProgressListener extends EventListener {
		/**
		 * A progression event.
		 * 
		 * @param progress
		 *            the {@link Progress} object that generated it, not
		 *            necessarily the same as the one where the listener was
		 *            attached (it could be a child {@link Progress} of this
		 *            {@link Progress}).
		 * @param name
		 *            the first non-null name of the {@link Progress} step that
		 *            generated this event
		 */
		public void progress(Progress progress, String name);
	}

	private Map<Object, Object> map = new HashMap<Object, Object>();
	private Progress parent = null;
	private Object lock = new Object();
	private String name;
	private Map<Progress, Double> children;
	private List<ProgressListener> listeners;
	private int min;
	private int max;
	private double relativeLocalProgress;
	private double relativeProgress; // children included

	/**
	 * Create a new default unnamed {@link Progress}, from 0 to 100.
	 */
	public Progress() {
		this(null);
	}

	/**
	 * Create a new default {@link Progress}, from 0 to 100.
	 * 
	 * @param name
	 *            the name of this {@link Progress} step
	 */
	public Progress(String name) {
		this(name, 0, 100);
	}

	/**
	 * Create a new unnamed {@link Progress}, from min to max.
	 * 
	 * @param min
	 *            the minimum progress value (and starting value) -- must be
	 *            non-negative
	 * @param max
	 *            the maximum progress value
	 */
	public Progress(int min, int max) {
		this(null, min, max);
	}

	/**
	 * Create a new {@link Progress}, from min to max.
	 * 
	 * @param name
	 *            the name of this {@link Progress} step
	 * @param min
	 *            the minimum progress value (and starting value) -- must be
	 *            non-negative
	 * @param max
	 *            the maximum progress value
	 */
	public Progress(String name, int min, int max) {
		this.name = name;
		this.children = new HashMap<Progress, Double>();
		this.listeners = new ArrayList<Progress.ProgressListener>();
		setMinMax(min, max);
		setProgress(min);
	}

	/**
	 * The name of this {@link Progress} step.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The name of this {@link Progress} step.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
		changed(this, name);
	}

	/**
	 * The minimum progress value.
	 * 
	 * @return the min
	 */
	public int getMin() {
		return min;
	}

	/**
	 * The minimum progress value.
	 * 
	 * @param min
	 *            the min to set
	 * 
	 * 
	 * @throws RuntimeException
	 *             if min &lt; 0 or if min &gt; max
	 */
	public void setMin(int min) {
		if (min < 0) {
			throw new RuntimeException("negative values not supported");
		}

		synchronized (lock) {
			if (min > max) {
				throw new RuntimeException(
						"The minimum progress value must be <= the maximum progress value");
			}

			this.min = min;
		}
	}

	/**
	 * The maximum progress value.
	 * 
	 * @return the max
	 */
	public int getMax() {
		return max;
	}

	/**
	 * The maximum progress value (must be >= the minimum progress value).
	 * 
	 * @param max
	 *            the max to set
	 * 
	 * 
	 * @throws RuntimeException
	 *             if max &lt; min
	 */
	public void setMax(int max) {
		synchronized (lock) {
			if (max < min) {
				throw new Error(
						"The maximum progress value must be >= the minimum progress value");
			}

			this.max = max;
		}
	}

	/**
	 * Set both the minimum and maximum progress values.
	 * 
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * 
	 * @throws RuntimeException
	 *             if min &lt; 0 or if min &gt; max
	 */
	public void setMinMax(int min, int max) {
		if (min < 0) {
			throw new RuntimeException("negative values not supported");
		}

		if (min > max) {
			throw new RuntimeException(
					"The minimum progress value must be <= the maximum progress value");
		}

		synchronized (lock) {
			this.min = min;
			this.max = max;
		}
	}

	/**
	 * Get the total progress value (including the optional children
	 * {@link Progress}) on a {@link Progress#getMin()} to
	 * {@link Progress#getMax()} scale.
	 * 
	 * @return the progress the value
	 */
	public int getProgress() {
		return (int) Math.round(relativeProgress * (max - min));
	}

	/**
	 * Set the local progress value (not including the optional children
	 * {@link Progress}), on a {@link Progress#getMin()} to
	 * {@link Progress#getMax()} scale.
	 * 
	 * @param progress
	 *            the progress to set
	 */
	public void setProgress(int progress) {
		synchronized (lock) {
			double childrenProgress = relativeProgress - relativeLocalProgress;

			relativeLocalProgress = ((double) progress) / (max - min);

			setRelativeProgress(this, name, relativeLocalProgress
					+ childrenProgress);
		}
	}

	/**
	 * Get the total progress value (including the optional children
	 * {@link Progress}) on a 0.0 to 1.0 scale.
	 * 
	 * @return the progress
	 */
	public double getRelativeProgress() {
		return relativeProgress;
	}

	/**
	 * Set the total progress value (including the optional children
	 * {@link Progress}), on a 0 to 1 scale.
	 * 
	 * @param pg
	 *            the {@link Progress} to report as the progression emitter
	 * @param name
	 *            the current name (if it is NULL, the first non-null name in
	 *            the hierarchy will overwrite it) of the {@link Progress} who
	 *            emitted this change
	 * @param relativeProgress
	 *            the progress to set
	 */
	private void setRelativeProgress(Progress pg, String name,
			double relativeProgress) {
		synchronized (lock) {
			relativeProgress = Math.max(0, relativeProgress);
			relativeProgress = Math.min(1, relativeProgress);
			this.relativeProgress = relativeProgress;

			changed(pg, name);
		}
	}

	/**
	 * Get the total progress value (including the optional children
	 * {@link Progress}) on a 0 to 1 scale.
	 * 
	 * @return the progress the value
	 */
	private int getLocalProgress() {
		return (int) Math.round(relativeLocalProgress * (max - min));
	}

	/**
	 * Add some value to the current progression of this {@link Progress}.
	 * 
	 * @param step
	 *            the amount to add
	 */
	public void add(int step) {
		synchronized (lock) {
			setProgress(getLocalProgress() + step);
		}
	}

	/**
	 * Check if the action corresponding to this {@link Progress} is done (i.e.,
	 * if its progress value == its max value).
	 * 
	 * @return TRUE if it is
	 */
	public boolean isDone() {
		return getProgress() == max;
	}

	/**
	 * Mark the {@link Progress} as done by setting its value to max.
	 */
	public void done() {
		synchronized (lock) {
			double childrenProgress = relativeProgress - relativeLocalProgress;
			relativeLocalProgress = 1 - childrenProgress;
			setRelativeProgress(this, name, 1d);
		}
	}

	/**
	 * Return the list of direct children of this {@link Progress}.
	 * 
	 * @return the children (Who will think of the children??)
	 */
	public List<Progress> getChildren() {
		synchronized (lock) {
			return new ArrayList<Progress>(children.keySet());
		}
	}

	/**
	 * Notify the listeners that this {@link Progress} changed value.
	 * 
	 * @param pg
	 *            the emmiter, that is, the (sub-){link Progress} that just
	 *            reported some change, not always the same as <tt>this</tt>
	 * @param name
	 *            the current name (if it is NULL, the first non-null name in
	 *            the hierarchy will overwrite it) of the {@link Progress} who
	 *            emitted this change
	 */
	private void changed(Progress pg, String name) {
		if (pg == null) {
			pg = this;
		}

		if (name == null) {
			name = this.name;
		}

		synchronized (lock) {
			for (ProgressListener l : listeners) {
				l.progress(pg, name);
			}
		}
	}

	/**
	 * Add a {@link ProgressListener} that will trigger on progress changes.
	 * <p>
	 * Note: the {@link Progress} that will be reported will be the active
	 * progress, not necessarily the same as the current one (it could be a
	 * child {@link Progress} of this {@link Progress}).
	 * 
	 * @param l
	 *            the listener
	 */
	public void addProgressListener(ProgressListener l) {
		synchronized (lock) {
			this.listeners.add(l);
		}
	}

	/**
	 * Remove a {@link ProgressListener} that would trigger on progress changes.
	 * 
	 * @param l
	 *            the listener
	 * 
	 * @return TRUE if it was found (and removed)
	 */
	public boolean removeProgressListener(ProgressListener l) {
		synchronized (lock) {
			return this.listeners.remove(l);
		}
	}

	/**
	 * Add a child {@link Progress} of the given weight.
	 * 
	 * @param progress
	 *            the child {@link Progress} to add
	 * @param weight
	 *            the weight (on a {@link Progress#getMin()} to
	 *            {@link Progress#getMax()} scale) of this child
	 *            {@link Progress} in relation to its parent
	 * 
	 * @throws RuntimeException
	 *             if weight exceed {@link Progress#getMax()} or if progress
	 *             already has a parent
	 */
	public void addProgress(Progress progress, double weight) {
		if (weight < min || weight > max) {
			throw new RuntimeException(String.format(
					"Progress object %s cannot have a weight of %f, "
							+ "it is outside of its parent (%s) range (%d)",
					progress.name, weight, name, max));
		}

		if (progress.parent != null) {
			throw new RuntimeException(String.format(
					"Progress object %s cannot be added to %s, "
							+ "as it already has a parent (%s)", progress.name,
					name, progress.parent.name));
		}

		ProgressListener progressListener = new ProgressListener() {
			@Override
			public void progress(Progress pg, String name) {
				synchronized (lock) {
					double total = relativeLocalProgress;
					for (Entry<Progress, Double> entry : children.entrySet()) {
						total += (entry.getValue() / (max - min))
								* entry.getKey().getRelativeProgress();
					}

					setRelativeProgress(pg, name, total);
				}
			}
		};

		synchronized (lock) {
			// Should not happen but just in case
			if (this.map != progress.map) {
				this.map.putAll(progress.map);
			}
			progress.map = this.map;
			progress.parent = this;
			this.children.put(progress, weight);
			progress.addProgressListener(progressListener);
		}
	}

	/**
	 * Set the given value for the given key on this {@link Progress} and it's
	 * children.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void put(Object key, Object value) {
		map.put(key, value);
	}

	/**
	 * Return the value associated with this key as a {@link String} if any,
	 * NULL if not.
	 * <p>
	 * If the value is not NULL but not a {@link String}, it will be converted
	 * via {@link Object#toString()}.
	 * 
	 * @param key
	 *            the key to check
	 * 
	 * @return the value or NULL
	 */
	public String getString(Object key) {
		Object value = map.get(key);
		if (value == null) {
			return null;
		}

		return value.toString();
	}

	/**
	 * Return the value associated with this key if any, NULL if not.
	 * 
	 * @param key
	 *            the key to check
	 * 
	 * @return the value or NULL
	 */
	public Object get(Object key) {
		return map.get(key);
	}
}
