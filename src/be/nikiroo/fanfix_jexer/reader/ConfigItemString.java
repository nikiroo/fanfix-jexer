package be.nikiroo.fanfix_jexer.reader;

import jexer.TField;
import jexer.TWidget;
import be.nikiroo.utils.resources.MetaInfo;

class ConfigItemString<E extends Enum<E>> extends ConfigItem<E> {
	/**
	 * Create a new {@link ConfigItemString} for the given {@link MetaInfo}.
	 * 
	 * @param info
	 *            the {@link MetaInfo}
	 */
	public ConfigItemString(TWidget parent, MetaInfo<E> info) {
		super(parent, info, true);
	}

	@Override
	protected Object getFromField(int item) {
		TField field = (TField) getField(item);
		if (field != null) {
			return field.getText();
		}

		return null;
	}

	@Override
	protected Object getFromInfo(int item) {
		return getInfo().getString(item, false);
	}

	@Override
	protected void setToField(Object value, int item) {
		TField field = (TField) getField(item);
		if (field != null) {
			field.setText(value == null ? "" : value.toString());
		}
	}

	@Override
	protected void setToInfo(Object value, int item) {
		getInfo().setString((String) value, item);
	}

	@Override
	protected TWidget createEmptyField(int item) {
		return new TField(this, 0, 0, 1, false);
	}
}
