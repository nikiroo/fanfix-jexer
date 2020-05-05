package be.nikiroo.utils.ui;

import javax.swing.JComponent;
import javax.swing.JTextField;

import be.nikiroo.utils.resources.MetaInfo;

class ConfigItemString<E extends Enum<E>> extends ConfigItem<E> {
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new {@link ConfigItemString} for the given {@link MetaInfo}.
	 * 
	 * @param info
	 *            the {@link MetaInfo}
	 */
	public ConfigItemString(MetaInfo<E> info) {
		super(info, true);
	}

	@Override
	protected Object getFromField(int item) {
		JTextField field = (JTextField) getField(item);
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
		JTextField field = (JTextField) getField(item);
		if (field != null) {
			field.setText(value == null ? "" : value.toString());
		}
	}

	@Override
	protected void setToInfo(Object value, int item) {
		getInfo().setString((String) value, item);
	}

	@Override
	protected JComponent createEmptyField(int item) {
		return new JTextField();
	}
}
