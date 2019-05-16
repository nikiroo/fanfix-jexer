package be.nikiroo.utils.ui;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import be.nikiroo.utils.resources.Bundle;
import be.nikiroo.utils.resources.Meta.Format;
import be.nikiroo.utils.resources.MetaInfo;

/**
 * A graphical item that reflect a configuration option from the given
 * {@link Bundle}.
 * 
 * @author niki
 * 
 * @param <E>
 *            the type of {@link Bundle} to edit
 */
public class ConfigItem<E extends Enum<E>> extends JPanel {
	private static final long serialVersionUID = 1L;

	public ConfigItem(final MetaInfo<E> info) {
		this.setLayout(new BorderLayout());
		// this.setBorder(new EmptyBorder(2, 10, 2, 10));

		if (info.getFormat() == Format.BOOLEAN) {
			final JCheckBox field = new JCheckBox();
			field.setToolTipText(info.getDescription());
			Boolean state = info.getBoolean();
			if (state == null) {
				info.getDefaultBoolean();
			}

			// Should not happen!
			if (state == null) {
				System.err
						.println("No default value given for BOOLEAN parameter \""
								+ info.getName()
								+ "\", we consider it is FALSE");
				state = false;
			}

			field.setSelected(state);

			info.addReloadedListener(new Runnable() {
				@Override
				public void run() {
					Boolean state = info.getBoolean();
					if (state == null) {
						info.getDefaultBoolean();
					}
					if (state == null) {
						state = false;
					}

					field.setSelected(state);
				}
			});
			info.addSaveListener(new Runnable() {
				@Override
				public void run() {
					info.setBoolean(field.isSelected());
				}
			});

			this.add(new JLabel(info.getName() + ": "), BorderLayout.WEST);
			this.add(field, BorderLayout.CENTER);
		} else {
			final JTextField field = new JTextField();
			field.setToolTipText(info.getDescription());
			field.setText(info.getString());

			info.addReloadedListener(new Runnable() {
				@Override
				public void run() {
					field.setText(info.getString());
				}
			});
			info.addSaveListener(new Runnable() {
				@Override
				public void run() {
					info.setString(field.getText());
				}
			});

			this.add(new JLabel(info.getName() + ": "), BorderLayout.WEST);
			this.add(field, BorderLayout.CENTER);
		}
	}
}
