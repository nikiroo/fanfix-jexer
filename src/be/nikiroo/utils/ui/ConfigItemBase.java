package be.nikiroo.utils.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nikiroo.utils.resources.Bundle;
import be.nikiroo.utils.resources.MetaInfo;

/**
 * A graphical item that reflect a configuration option from the given
 * {@link Bundle}.
 * <p>
 * This graphical item can be edited, and the result will be saved back into the
 * linked {@link MetaInfo}; you still have to save the {@link MetaInfo} should
 * you wish to, of course.
 * 
 * @author niki
 * 
 * @param <T>
 *            the graphical base type to use (i.e., T or TWidget)
 * @param <E>
 *            the type of {@link Bundle} to edit
 */
public abstract class ConfigItemBase<T, E extends Enum<E>> {
	/** The original value before current changes. */
	private Object orig;
	private List<Object> origs = new ArrayList<Object>();
	private List<Integer> dirtyBits;

	/** The fields (one for non-array, a list for arrays). */
	private T field;
	private List<T> fields = new ArrayList<T>();

	/** The fields to panel map to get the actual item added to 'main'. */
	private Map<Integer, T> itemFields = new HashMap<Integer, T>();

	/** The {@link MetaInfo} linked to the field. */
	private MetaInfo<E> info;

	/** The {@link MetaInfo} linked to the field. */
	public MetaInfo<E> getInfo() {
		return info;
	}

	/**
	 * The number of fields, for arrays.
	 * 
	 * @return
	 */
	public int getFieldsSize() {
		return fields.size();
	}

	/**
	 * The number of fields to panel map to get the actual item added to 'main'.
	 */
	public int getItemFieldsSize() {
		return itemFields.size();
	}

	/**
	 * Add a new item in an array-value {@link MetaInfo}.
	 * 
	 * @param item
	 *            the index of the new item
	 * @param panel
	 *            a linked T, if we want to link it into the itemFields (can be
	 *            NULL) -- that way, we can get it back later on
	 *            {@link ConfigItemBase#removeItem(int)}
	 * 
	 * @return the newly created graphical field
	 */
	public T addItem(final int item, T panel) {
		if (panel != null) {
			itemFields.put(item, panel);
		}
		return createField(item);
	}

	/**
	 * The counter-part to {@link ConfigItemBase#addItem(int, Object)}, to
	 * remove a specific item of an array-values {@link MetaInfo}; all the
	 * remaining items will be shifted as required (so, always the last
	 * graphical object will be removed).
	 * 
	 * @param item
	 *            the index of the item to remove
	 * 
	 * @return the linked graphical T to remove if any (always the latest
	 *         graphical object if any)
	 */
	protected T removeItem(int item) {
		int last = itemFields.size() - 1;

		for (int i = item; i <= last; i++) {
			Object value = null;
			if (i < last) {
				value = getFromField(i + 1);
			}
			setToField(value, i);
			setToInfo(value, i);
			setDirtyItem(i);
		}

		return itemFields.remove(last);
	}

	/**
	 * Prepare a new {@link ConfigItemBase} instance, linked to the given
	 * {@link MetaInfo}.
	 * 
	 * @param info
	 *            the info
	 * @param autoDirtyHandling
	 *            TRUE to automatically manage the setDirty/Save operations,
	 *            FALSE if you want to do it yourself via
	 *            {@link ConfigItemBase#setDirtyItem(int)}
	 */
	protected ConfigItemBase(MetaInfo<E> info, boolean autoDirtyHandling) {
		this.info = info;
		if (!autoDirtyHandling) {
			dirtyBits = new ArrayList<Integer>();
		}
	}

	/**
	 * Create an empty graphical component to be used later by
	 * {@link ConfigItemBase#createField(int)}.
	 * <p>
	 * Note that {@link ConfigItemBase#reload(int)} will be called after it was
	 * created by {@link ConfigItemBase#createField(int)}.
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * 
	 * @return the graphical component
	 */
	abstract protected T createEmptyField(int item);

	/**
	 * Get the information from the {@link MetaInfo} in the subclass preferred
	 * format.
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * 
	 * @return the information in the subclass preferred format
	 */
	abstract protected Object getFromInfo(int item);

	/**
	 * Set the value to the {@link MetaInfo}.
	 * 
	 * @param value
	 *            the value in the subclass preferred format
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 */
	abstract protected void setToInfo(Object value, int item);

	/**
	 * The value present in the given item's related field in the subclass
	 * preferred format.
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * 
	 * @return the value present in the given item's related field in the
	 *         subclass preferred format
	 */
	abstract protected Object getFromField(int item);

	/**
	 * Set the value (in the subclass preferred format) into the field.
	 * 
	 * @param value
	 *            the value in the subclass preferred format
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 */
	abstract protected void setToField(Object value, int item);

	/**
	 * Create a new field for the given graphical component at the given index
	 * (note that the component is usually created by
	 * {@link ConfigItemBase#createEmptyField(int)}).
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * @param field
	 *            the graphical component
	 */
	private void setField(int item, T field) {
		if (item < 0) {
			this.field = field;
			return;
		}

		for (int i = fields.size(); i <= item; i++) {
			fields.add(null);
		}

		fields.set(item, field);
	}

	/**
	 * Retrieve the associated graphical component that was created with
	 * {@link ConfigItemBase#createEmptyField(int)}.
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * 
	 * @return the graphical component
	 */
	public T getField(int item) {
		if (item < 0) {
			return field;
		}

		if (item < fields.size()) {
			return fields.get(item);
		}

		return null;
	}

	/**
	 * The original value (before any changes to the {@link MetaInfo}) for this
	 * item.
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * 
	 * @return the original value
	 */
	private Object getOrig(int item) {
		if (item < 0) {
			return orig;
		}

		if (item < origs.size()) {
			return origs.get(item);
		}

		return null;
	}

	/**
	 * The original value (before any changes to the {@link MetaInfo}) for this
	 * item.
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * @param value
	 *            the new original value
	 */
	private void setOrig(Object value, int item) {
		if (item < 0) {
			orig = value;
		} else {
			while (item >= origs.size()) {
				origs.add(null);
			}

			origs.set(item, value);
		}
	}

	/**
	 * Manually specify that the given item is "dirty" and thus should be saved
	 * when asked.
	 * <p>
	 * Has no effect if the class is using automatic dirty handling (see
	 * {@link ConfigItemBase#ConfigItem(MetaInfo, boolean)}).
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 */
	public void setDirtyItem(int item) {
		if (dirtyBits != null) {
			dirtyBits.add(item);
		}
	}

	/**
	 * Check if the value changed since the last load/save into the linked
	 * {@link MetaInfo}.
	 * <p>
	 * Note that we consider NULL and an Empty {@link String} to be equals.
	 * 
	 * @param value
	 *            the value to test
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * 
	 * @return TRUE if it has
	 */
	public boolean hasValueChanged(Object value, int item) {
		// We consider "" and NULL to be equals
		Object orig = getOrig(item);
		if (orig == null) {
			orig = "";
		}
		return !orig.equals(value == null ? "" : value);
	}

	/**
	 * Reload the values to what they currently are in the {@link MetaInfo}.
	 * 
	 * @return for arrays, the list of graphical T objects we don't need any
	 *         more (never NULL, but can be empty)
	 */
	public List<T> reload() {
		List<T> removed = new ArrayList<T>();
		if (info.isArray()) {
			while (!itemFields.isEmpty()) {
				removed.add(itemFields.remove(itemFields.size() - 1));
			}
			for (int item = 0; item < info.getListSize(false); item++) {
				reload(item);
			}
		} else {
			reload(-1);
		}

		return removed;
	}

	/**
	 * Reload the values to what they currently are in the {@link MetaInfo}.
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 */
	private void reload(int item) {
		if (item >= 0 && !itemFields.containsKey(item)) {
			addItem(item, null);
		}

		Object value = getFromInfo(item);
		setToField(value, item);
		setOrig(value == null ? "" : value, item);
	}

	/**
	 * If the item has been modified, set the {@link MetaInfo} to dirty then
	 * modify it to, reflect the changes so it can be saved later.
	 * <p>
	 * This method does <b>not</b> call {@link MetaInfo#save(boolean)}.
	 */
	private void save() {
		if (info.isArray()) {
			boolean dirty = itemFields.size() != info.getListSize(false);
			for (int item = 0; item < itemFields.size(); item++) {
				if (getDirtyBit(item)) {
					dirty = true;
				}
			}

			if (dirty) {
				info.setDirty();
				info.setString(null, -1);

				for (int item = 0; item < itemFields.size(); item++) {
					Object value = null;
					if (getField(item) != null) {
						value = getFromField(item);
						if ("".equals(value)) {
							value = null;
						}
					}

					setToInfo(value, item);
					setOrig(value, item);
				}
			}
		} else {
			if (getDirtyBit(-1)) {
				Object value = getFromField(-1);

				info.setDirty();
				setToInfo(value, -1);
				setOrig(value, -1);
			}
		}
	}

	/**
	 * Check if the item is dirty, and clear the dirty bit if set.
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * 
	 * @return TRUE if it was dirty, FALSE if not
	 */
	private boolean getDirtyBit(int item) {
		if (dirtyBits != null) {
			return dirtyBits.remove((Integer) item);
		}

		Object value = null;
		if (getField(item) != null) {
			value = getFromField(item);
		}

		return hasValueChanged(value, item);
	}

	/**
	 * Create a new field for the given item.
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * 
	 * @return the newly created field
	 */
	public T createField(final int item) {
		T field = createEmptyField(item);
		setField(item, field);
		reload(item);

		info.addReloadedListener(new Runnable() {
			@Override
			public void run() {
				reload();
			}
		});
		info.addSaveListener(new Runnable() {
			@Override
			public void run() {
				save();
			}
		});

		return field;
	}
}
