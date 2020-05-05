package be.nikiroo.utils.ui;

import java.awt.Component;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;

import be.nikiroo.utils.resources.MetaInfo;

class ConfigItemLocale<E extends Enum<E>> extends ConfigItemCombobox<E> {
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new {@link ConfigItemLocale} for the given {@link MetaInfo}.
	 * 
	 * @param info
	 *            the {@link MetaInfo}
	 */
	public ConfigItemLocale(MetaInfo<E> info) {
		super(info, true);
	}

	// rawtypes for Java 1.6 (and 1.7 ?) support
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected JComponent createEmptyField(int item) {
		JComboBox field = (JComboBox) super.createEmptyField(item);
		field.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				String svalue = value == null ? "" : value.toString();
				String[] tab = svalue.split("-");
				Locale locale = null;
				if (tab.length == 1) {
					locale = new Locale(tab[0]);
				} else if (tab.length == 2) {
					locale = new Locale(tab[0], tab[1]);
				} else if (tab.length == 3) {
					locale = new Locale(tab[0], tab[1], tab[2]);
				}

				String displayValue = svalue;
				if (locale != null) {
					displayValue = locale.getDisplayName();
				}

				return super.getListCellRendererComponent(list, displayValue,
						index, isSelected, cellHasFocus);
			}
		});

		return field;
	}
}
