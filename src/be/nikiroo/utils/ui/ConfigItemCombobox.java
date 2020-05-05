package be.nikiroo.utils.ui;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import be.nikiroo.utils.resources.MetaInfo;

class ConfigItemCombobox<E extends Enum<E>> extends ConfigItem<E> {
	private static final long serialVersionUID = 1L;

	private boolean editable;
	private String[] allowedValues;

	/**
	 * Create a new {@link ConfigItemCombobox} for the given {@link MetaInfo}.
	 * 
	 * @param info
	 *            the {@link MetaInfo}
	 * @param editable
	 *            allows the user to type in another value not in the list
	 */
	public ConfigItemCombobox(MetaInfo<E> info, boolean editable) {
		super(info, true);
		this.editable = editable;
		this.allowedValues = info.getAllowedValues();
	}

	@Override
	protected Object getFromField(int item) {
		// rawtypes for Java 1.6 (and 1.7 ?) support
		@SuppressWarnings("rawtypes")
		JComboBox field = (JComboBox) getField(item);
		if (field != null) {
			return field.getSelectedItem();
		}

		return null;
	}

	@Override
	protected Object getFromInfo(int item) {
		return getInfo().getString(item, false);
	}

	@Override
	protected void setToField(Object value, int item) {
		// rawtypes for Java 1.6 (and 1.7 ?) support
		@SuppressWarnings("rawtypes")
		JComboBox field = (JComboBox) getField(item);
		if (field != null) {
			field.setSelectedItem(value);
		}
	}

	@Override
	protected void setToInfo(Object value, int item) {
		getInfo().setString((String) value, item);
	}

	// rawtypes for Java 1.6 (and 1.7 ?) support
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected JComponent createEmptyField(int item) {
		JComboBox field = new JComboBox(allowedValues);
		field.setEditable(editable);
		return field;
	}
}
