package be.nikiroo.utils.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import be.nikiroo.utils.resources.MetaInfo;

class ConfigItemPassword<E extends Enum<E>> extends ConfigItem<E> {
	private static final long serialVersionUID = 1L;
	/** A small 16x16 pass-protecet icon in PNG, base64 encoded. */
	private static String img64passProtected = //
	""
			+ "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAAAnNCSVQICFXsRgQAAAD5SURBVCjP"
			+ "ndG9LoNxGIbxHxJTG9U0IsJAdCSNqZEa9BR87BaHYfW5ESYkmjQh4giwIU00MWFwAPWRSmpgaf6G"
			+ "6ts36eZ+xuu+lvuhlTGjOFHAsXldWVDRa82WhE9pZFxrtmBeUY87+yqCH3UzMh4E1VYhp2ZVVfi7"
			+ "C0PuBc9G2v6KoOlIQUoyhovyLb+uZla/TbsRHnOgJkfSi4YpbDiXjuwJDS+SlASLYC9mw5KgxJlg"
			+ "CWJ4OyqckvKkIWswwmXrmPbl0QBkHcbsHRv6Fbz6MNnesWMnpMw51vRmphuXo7FujHf+cCt4NGza"
			+ "lbp3l5b1xR/1rWrYf/MLWpplWwswQpMAAAAASUVORK5CYII=";

	/** A small 16x16 pass-unprotecet icon in PNG, base64 encoded. */
	private static String img64passUnprotected = //
	""
			+ "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAA"
			+ "CxMAAAsTAQCanBgAAAAHdElNRQfjBR8MIilwhCwdAAABK0lEQVQoz5XQv0uUAQCH8c/7qod4nect"
			+ "gop3BIKDFBIiRyiKtATmcEiBDW7+Ae5ODt5gW0SLigouKTg6SJvkjw4Co8mcNeWgc+o839dBBXPz"
			+ "+Y7PM33r3NCpWcWKM1lfHapJq0B4G/TbEDoyZlyHQxuGtdw6eSMC33yyJxa79MW+wIj8TdDrxJSS"
			+ "+N5KppQNEchrkrMosmzRT0/0eGdSaFrob6DXloSqgu9mNWlUNqPPpmYNJkg5UvEMResystYVpbwW"
			+ "qWpjVWwcfNQqLS1rAXwQOw4N4SWoqZeUVFMGuzgg65/IqIw5a3LarZnDcxd+ScMrkcikhB8+m1eU"
			+ "MODUua67q967EttR0KHFoCVX/nhxp1N4o/rfUTueekC332KRM9veqnuoAwQyHs81DiddylUvrecA"
			+ "AAAASUVORK5CYII=";

	private Map<JComponent, JPasswordField> fields = new HashMap<JComponent, JPasswordField>();

	/**
	 * Create a new {@link ConfigItemPassword} for the given {@link MetaInfo}.
	 * 
	 * @param info
	 *            the {@link MetaInfo}
	 */
	public ConfigItemPassword(MetaInfo<E> info) {
		super(info, true);
	}

	@Override
	protected Object getFromField(int item) {
		JPasswordField field = fields.get(getField(item));
		if (field != null) {
			return new String(field.getPassword());
		}

		return null;
	}

	@Override
	protected Object getFromInfo(int item) {
		return getInfo().getString(item, false);
	}

	@Override
	protected void setToField(Object value, int item) {
		JPasswordField field = fields.get(getField(item));
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
		JPanel pane = new JPanel(new BorderLayout());
		final JPasswordField field = new JPasswordField();
		field.setEchoChar('*');

		final JButton show = new JButton();
		final Boolean[] visible = new Boolean[] { false };
		setImage(show, img64passProtected, "/");
		show.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visible[0] = !visible[0];
				if (visible[0]) {
					field.setEchoChar((char) 0);
					setImage(show, img64passUnprotected, "o");
				} else {
					field.setEchoChar('*');
					setImage(show, img64passProtected, "/");
				}
			}
		});

		pane.add(field, BorderLayout.CENTER);
		pane.add(show, BorderLayout.EAST);

		fields.put(pane, field);
		return pane;
	}
}
