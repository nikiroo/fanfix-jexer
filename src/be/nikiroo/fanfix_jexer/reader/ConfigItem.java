package be.nikiroo.fanfix_jexer.reader;

import java.util.List;

import jexer.TAction;
import jexer.TButton;
import jexer.TLabel;
import jexer.TPanel;
import jexer.TWidget;
import be.nikiroo.utils.resources.Bundle;
import be.nikiroo.utils.resources.MetaInfo;
import be.nikiroo.utils.ui.ConfigItemBase;

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
 * @param <E>
 *            the type of {@link Bundle} to edit
 */
public abstract class ConfigItem<E extends Enum<E>> extends TWidget {
	/** The code base */
	private final ConfigItemBase<TWidget, E> base;

	/**
	 * Prepare a new {@link ConfigItem} instance, linked to the given
	 * {@link MetaInfo}.
	 * 
	 * @param parent
	 *            the parent widget
	 * @param info
	 *            the info
	 * @param autoDirtyHandling
	 *            TRUE to automatically manage the setDirty/Save operations,
	 *            FALSE if you want to do it yourself via
	 *            {@link ConfigItem#setDirtyItem(int)}
	 */
	protected ConfigItem(TWidget parent, MetaInfo<E> info,
			boolean autoDirtyHandling) {
		super(parent);

		base = new ConfigItemBase<TWidget, E>(info, autoDirtyHandling) {
			@Override
			protected TWidget createEmptyField(int item) {
				return ConfigItem.this.createEmptyField(item);
			}

			@Override
			protected Object getFromInfo(int item) {
				return ConfigItem.this.getFromInfo(item);
			}

			@Override
			protected void setToInfo(Object value, int item) {
				ConfigItem.this.setToInfo(value, item);
			}

			@Override
			protected Object getFromField(int item) {
				return ConfigItem.this.getFromField(item);
			}

			@Override
			protected void setToField(Object value, int item) {
				ConfigItem.this.setToField(value, item);
			}

			@Override
			public TWidget createField(int item) {
				TWidget field = super.createField(item);

				// TODO: size?

				return field;
			}

			@Override
			public List<TWidget> reload() {
				List<TWidget> removed = base.reload();
				if (!removed.isEmpty()) {
					for (TWidget c : removed) {
						removeChild(c);
					}
				}

				return removed;
			}
		};
	}

	/**
	 * Create a new {@link ConfigItem} for the given {@link MetaInfo}.
	 * 
	 * @param nhgap
	 *            negative horisontal gap in pixel to use for the label, i.e.,
	 *            the step lock sized labels will start smaller by that amount
	 *            (the use case would be to align controls that start at a
	 *            different horisontal position)
	 */
	public void init(int nhgap) {
		if (getInfo().isArray()) {
			// TODO: width
			int size = getInfo().getListSize(false);
			final TPanel pane = new TPanel(this, 0, 0, 20, size + 2);
			final TWidget label = label(0, 0, nhgap);
			label.setParent(pane, false);
			setHeight(pane.getHeight());

			for (int i = 0; i < size; i++) {
				// TODO: minusPanel
				TWidget field = base.addItem(i, null);
				field.setParent(pane, false);
				field.setX(label.getWidth() + 1);
				field.setY(i);
			}

			// x, y
			final TButton add = new TButton(pane, "+", label.getWidth() + 1,
					size + 1, null);
			TAction action = new TAction() {
				@Override
				public void DO() {
					TWidget field = base.addItem(base.getFieldsSize(), null);
					field.setParent(pane, false);
					field.setX(label.getWidth() + 1);
					field.setY(add.getY());
					add.setY(add.getY() + 1);
				}
			};
			add.setAction(action);
		} else {
			final TWidget label = label(0, 0, nhgap);

			TWidget field = base.createField(-1);
			field.setX(label.getWidth() + 1);
			field.setWidth(10); // TODO

			// TODO
			setWidth(30);
			setHeight(1);
		}
	}

	/** The {@link MetaInfo} linked to the field. */
	public MetaInfo<E> getInfo() {
		return base.getInfo();
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
	protected TWidget getField(int item) {
		return base.getField(item);
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
	protected void setDirtyItem(int item) {
		base.setDirtyItem(item);
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
	protected boolean hasValueChanged(Object value, int item) {
		return base.hasValueChanged(value, item);
	}

	/**
	 * Create an empty graphical component to be used later by
	 * {@link ConfigItem#createField(int)}.
	 * <p>
	 * Note that {@link ConfigItem#reload(int)} will be called after it was
	 * created by {@link ConfigItem#createField(int)}.
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * 
	 * @return the graphical component
	 */
	abstract protected TWidget createEmptyField(int item);

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
	 * Create a label which width is constrained in lock steps.
	 * 
	 * @param x
	 *            the X position of the label
	 * @param y
	 *            the Y position of the label
	 * @param nhgap
	 *            negative horisontal gap in pixel to use for the label, i.e.,
	 *            the step lock sized labels will start smaller by that amount
	 *            (the use case would be to align controls that start at a
	 *            different horisontal position)
	 * 
	 * @return the label
	 */
	protected TWidget label(int x, int y, int nhgap) {
		// TODO: see Swing version for lock-step sizes
		// TODO: see Swing version for help info-buttons

		String lbl = getInfo().getName();
		return new TLabel(this, lbl, x, y);
	}

	/**
	 * Create a new {@link ConfigItem} for the given {@link MetaInfo}.
	 * 
	 * @param <E>
	 *            the type of {@link Bundle} to edit
	 * 
	 * @param x
	 *            the X position of the item
	 * @param y
	 *            the Y position of the item
	 * @param parent
	 *            the parent widget to use for this one
	 * @param info
	 *            the {@link MetaInfo}
	 * @param nhgap
	 *            negative horisontal gap in pixel to use for the label, i.e.,
	 *            the step lock sized labels will start smaller by that amount
	 *            (the use case would be to align controls that start at a
	 *            different horisontal position)
	 * 
	 * @return the new {@link ConfigItem}
	 */
	static public <E extends Enum<E>> ConfigItem<E> createItem(TWidget parent,
			int x, int y, MetaInfo<E> info, int nhgap) {

		ConfigItem<E> configItem;
		switch (info.getFormat()) {
		// TODO
		// case BOOLEAN:
		// configItem = new ConfigItemBoolean<E>(info);
		// break;
		// case COLOR:
		// configItem = new ConfigItemColor<E>(info);
		// break;
		// case FILE:
		// configItem = new ConfigItemBrowse<E>(info, false);
		// break;
		// case DIRECTORY:
		// configItem = new ConfigItemBrowse<E>(info, true);
		// break;
		// case COMBO_LIST:
		// configItem = new ConfigItemCombobox<E>(info, true);
		// break;
		// case FIXED_LIST:
		// configItem = new ConfigItemCombobox<E>(info, false);
		// break;
		// case INT:
		// configItem = new ConfigItemInteger<E>(info);
		// break;
		// case PASSWORD:
		// configItem = new ConfigItemPassword<E>(info);
		// break;
		// case LOCALE:
		// configItem = new ConfigItemLocale<E>(info);
		// break;
		// case STRING:
		default:
			configItem = new ConfigItemString<E>(parent, info);
			break;
		}

		configItem.init(nhgap);
		configItem.setX(x);
		configItem.setY(y);

		return configItem;
	}
}
