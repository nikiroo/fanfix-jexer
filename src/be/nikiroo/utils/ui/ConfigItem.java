package be.nikiroo.utils.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import be.nikiroo.utils.Image;
import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.StringUtils.Alignment;
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
 * @param <E>
 *            the type of {@link Bundle} to edit
 */
public abstract class ConfigItem<E extends Enum<E>> extends JPanel {
	private static final long serialVersionUID = 1L;

	private static int minimumHeight = -1;

	/** A small 16x16 "?" blue in PNG, base64 encoded. */
	private static String img64info = //
	""
			+ "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBI"
			+ "WXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4wURFRg6IrtcdgAAATdJREFUOMvtkj8sQ1EUxr9z/71G"
			+ "m1RDogYxq7WDDYMYTSajSG4n6YRYzSaSLibWbiaDIGwdiLIYDFKDNJEgKu969xi8UNHy7H7LPcN3"
			+ "v/Odcy+hG9oOIeIcBCJS9MAvlZtOMtHxsrFrJHGqe0RVGnHAHpcIbPlng8BS3HmKBJYzabGUzcrJ"
			+ "XK+ckIrqANYR2JEv2nYDEVck0WKGfHzyq82Go+btxoX3XAcAIqTj8wPqOH6mtMeM4bGCLhyfhTMA"
			+ "qlLhKHqujCfaweCAmV0p50dPzsNpEKpK01V/n55HIvTnfDC2odKlfeYadZN/T+AqDACUsnkhqaU1"
			+ "LRIVuX1x7ciuSWQxVIrunONrfq3dI6oh+T94Z8453vEem/HTqT8ZpFJ0qDXtGkPbAGAMeSRngQCA"
			+ "eUvgn195AwlZWyvjtQdhAAAAAElFTkSuQmCC";

	/** A small 16x16 "+" image with colours */
	private static String img64add = //
	""
			+ "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBI"
			+ "WXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4wUeES0QBFvvnAAAAB1pVFh0Q29tbWVudAAAAAAAQ3Jl"
			+ "YXRlZCB3aXRoIEdJTVBkLmUHAAACH0lEQVQ4y42Tz0sVURTHP+fMmC7CQMpH1EjgIimCsEVBEIg/"
			+ "qIbcBAW2Uai1m/oH2rlJXLQpeRJt2gQhTO0iTTKC1I2JBf5gKCJCRPvhPOed22LmvV70Fn7hwr3c"
			+ "+z3ne+73HCFHEClxaASRHgduA91AW369BkwDI3Foy0GkEofmACQnSxyaCyItAkMClMzYdeCAJgVP"
			+ "tJJrPA7tVoUjNZlngXMAiRmXClfoK/Tjq09x7T6LW+8RxOVJ5+LQzgSRojm5WCEDlMrQVbjIQNtN"
			+ "rh0d5FTzaTLBmWKgM4h0Ig4NzWseohYCJUuqx123Sx0MBpF2+MAdyWUnlqX4lf4bIDHjR+rwJJPR"
			+ "qNCgCjDsA10lM/oKIRcO9lByCYklnG/pqQa4euQ6J5tPoKI0yD6ef33Ku40Z80R7CSJNWyZxT+Ki"
			+ "2ytGP911hyZxQaRp1RtPPPYKD4+sGJwPrDUp7Q9Xxnj9fYrUUnaszEAwQHfrZQAerT/g7cYMiuCp"
			+ "z8LmLI0qBqz6wLQn2v5he57FrXkAtlPH2ZZOuskCzG2+4dnnx3iSuSgCKqLAlAIjmXPiVIRsgYjU"
			+ "usrfO0Gq7cA9jUNbBsZrmiQnac1e6n3FeBzakpf39OSBG9IPHAZwzlFoagVg5edHXn57wZed9dpA"
			+ "C3FoYRDpf8M0AQwKwu9yubxjeA7Y72ENqlp3mOqMcwcwDPQCx8gGchV4BYzGoS1V3gL8AVA5C5/0"
			+ "oRFoAAAAAElFTkSuQmCC";

	/** A small 32x32 "-" image with colours */
	private static String img64remove = //
	""
			+ "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBI"
			+ "WXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4wUeESw5X/JGsQAAAB1pVFh0Q29tbWVudAAAAAAAQ3Jl"
			+ "YXRlZCB3aXRoIEdJTVBkLmUHAAACKUlEQVQ4y5WTO2iTYRSG3+//v/+SJrG5SSABh1JQBHFJNUNR"
			+ "YodCLoMoTkK0YKhQtBmsl01wKVZRBwcrgosg3SwFW9Cippe0VmlpB6uYqYIaNSZtbv/lOKRx0iR9"
			+ "4YOzvOc8vOd8wLbG4nYGAKP9tshKr3Pq0zFXORt0UzbopvUeZ2ml1/niUcIWAYBzwwqr+xgAjCSt"
			+ "wpXjWzx105Ha+1XsMgT8U6IJfPAacyfO50OXJi3VwbtbxMbidtZ3tiClbzi/eAuCmxgai4AfNvNn"
			+ "KJn3X5xWKgwA0lHHYud3MdDUXMcmIOMx0oGJXJCN9tuiJ98p4//DbtTk2cFKhB/OSBcMgQHVMkir"
			+ "AqwJBhGYrIIkCQc2eJK3aewI9Crko2FIh0K1Jo0mcwmV6XFUlmfRXhK7eXuRKaRVIYdiUGKnW8Kn"
			+ "0ia0t6/hKHJVqCcLzncQgLhtIvBfbWbZZahq+cl96AuvQLre2Mw59NUlkCwjZ6USL0uYgSj26B/X"
			+ "oK+vtkYgMAhMRF4x5oWlPdod0UQtfUFo7YEBBKz59BEGAAtRx1xHVgzu5AYyHmMmMJHrZolhhU3t"
			+ "05XJe7s2PJuCq9k1MgKyNjOXiBf8kWW5JDy4XKHBl2ql6+pvX8ZjzDOqrcWsFQAAE/T3H3z2GG/6"
			+ "zhT8sfdKeehWkUQAeJ7WcH23xTz1uPBwf1hclA3mBZjPojFOIOSsVPpmN1OznfpA+Gn+2kCHqg/d"
			+ "LhIA/AFU5d0V6gTjtQAAAABJRU5ErkJggg==";

	/** The original value before current changes. */
	private Object orig;
	private List<Object> origs = new ArrayList<Object>();
	private List<Integer> dirtyBits;

	/** The fields (one for non-array, a list for arrays). */
	private JComponent field;
	private List<JComponent> fields = new ArrayList<JComponent>();

	/** The fields to panel map to get the actual item added to 'main'. */
	private Map<Integer, JComponent> itemFields = new HashMap<Integer, JComponent>();

	/** The main panel with all the fields in it. */
	private JPanel main;

	/** The {@link MetaInfo} linked to the field. */
	protected MetaInfo<E> info;

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
		if (info.isArray()) {
			this.setLayout(new BorderLayout());
			add(label(nhgap), BorderLayout.WEST);

			main = new JPanel();

			main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
			int size = info.getListSize(false);
			for (int i = 0; i < size; i++) {
				addItem(i);
			}
			main.revalidate();
			main.repaint();

			final JButton add = new JButton();
			setImage(add, img64add, "+");

			add.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addItem(fields.size());
					main.revalidate();
					main.repaint();
				}
			});

			JPanel tmp = new JPanel(new BorderLayout());
			tmp.add(add, BorderLayout.WEST);

			JPanel mainPlus = new JPanel(new BorderLayout());
			mainPlus.add(main, BorderLayout.CENTER);
			mainPlus.add(tmp, BorderLayout.SOUTH);

			add(mainPlus, BorderLayout.CENTER);
		} else {
			this.setLayout(new BorderLayout());
			add(label(nhgap), BorderLayout.WEST);

			JComponent field = createField(-1);
			add(field, BorderLayout.CENTER);
		}
	}

	private void addItem(final int item) {
		JPanel minusPanel = new JPanel(new BorderLayout());
		itemFields.put(item, minusPanel);

		JComponent field = createField(item);

		final JButton remove = new JButton();
		setImage(remove, img64remove, "-");

		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeItem(item);
			}
		});

		minusPanel.add(field, BorderLayout.CENTER);
		minusPanel.add(remove, BorderLayout.EAST);

		main.add(minusPanel);
		main.revalidate();
		main.repaint();
	}

	private void removeItem(int item) {
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

		main.remove(itemFields.remove(last));
		main.revalidate();
		main.repaint();
	}

	/**
	 * Prepare a new {@link ConfigItem} instance, linked to the given
	 * {@link MetaInfo}.
	 * 
	 * @param info
	 *            the info
	 * @param autoDirtyHandling
	 *            TRUE to automatically manage the setDirty/Save operations,
	 *            FALSE if you want to do it yourself via
	 *            {@link ConfigItem#setDirtyItem(int)}
	 */
	protected ConfigItem(MetaInfo<E> info, boolean autoDirtyHandling) {
		this.info = info;
		if (!autoDirtyHandling) {
			dirtyBits = new ArrayList<Integer>();
		}
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
	abstract protected JComponent createEmptyField(int item);

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
	 * {@link ConfigItem#createEmptyField(int)}).
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * @param field
	 *            the graphical component
	 */
	private void setField(int item, JComponent field) {
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
	 * {@link ConfigItem#createEmptyField(int)}.
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 * 
	 * @return the graphical component
	 */
	protected JComponent getField(int item) {
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
	 * {@link ConfigItem#ConfigItem(MetaInfo, boolean)}).
	 * 
	 * @param item
	 *            the item number to get for an array of values, or -1 to get
	 *            the whole value (has no effect if {@link MetaInfo#isArray()}
	 *            is FALSE)
	 */
	protected void setDirtyItem(int item) {
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
	protected boolean hasValueChanged(Object value, int item) {
		// We consider "" and NULL to be equals
		Object orig = getOrig(item);
		if (orig == null) {
			orig = "";
		}
		return !orig.equals(value == null ? "" : value);
	}

	/**
	 * Reload the values to what they currently are in the {@link MetaInfo}.
	 */
	private void reload() {
		if (info.isArray()) {
			while (!itemFields.isEmpty()) {
				main.remove(itemFields.remove(itemFields.size() - 1));
			}
			main.revalidate();
			main.repaint();
			for (int item = 0; item < info.getListSize(false); item++) {
				reload(item);
			}
		} else {
			reload(-1);
		}
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
			addItem(item);
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
	protected JComponent createField(final int item) {
		JComponent field = createEmptyField(item);
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

		int height = Math
				.max(getMinimumHeight(), field.getMinimumSize().height);
		field.setPreferredSize(new Dimension(200, height));

		return field;
	}

	/**
	 * Create a label which width is constrained in lock steps.
	 * 
	 * @param nhgap
	 *            negative horisontal gap in pixel to use for the label, i.e.,
	 *            the step lock sized labels will start smaller by that amount
	 *            (the use case would be to align controls that start at a
	 *            different horisontal position)
	 * 
	 * @return the label
	 */
	protected JComponent label(int nhgap) {
		final JLabel label = new JLabel(info.getName());

		Dimension ps = label.getPreferredSize();
		if (ps == null) {
			ps = label.getSize();
		}

		ps.height = Math.max(ps.height, getMinimumHeight());

		int w = ps.width;
		int step = 150;
		for (int i = 2 * step - nhgap; i < 10 * step; i += step) {
			if (w < i) {
				w = i;
				break;
			}
		}

		final Runnable showInfo = new Runnable() {
			@Override
			public void run() {
				StringBuilder builder = new StringBuilder();
				String text = (info.getDescription().replace("\\n", "\n"))
						.trim();
				for (String line : StringUtils.justifyText(text, 80,
						Alignment.LEFT)) {
					if (builder.length() > 0) {
						builder.append("\n");
					}
					builder.append(line);
				}
				text = builder.toString();
				JOptionPane.showMessageDialog(ConfigItem.this, text,
						info.getName(), JOptionPane.INFORMATION_MESSAGE);
			}
		};

		JLabel help = new JLabel("");
		help.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setImage(help, img64info, "?");

		help.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				showInfo.run();
			}
		});

		JPanel pane2 = new JPanel(new BorderLayout());
		pane2.add(help, BorderLayout.WEST);
		pane2.add(new JLabel(" "), BorderLayout.CENTER);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(label, BorderLayout.WEST);
		contentPane.add(pane2, BorderLayout.CENTER);

		ps.width = w + 30; // 30 for the (?) sign
		contentPane.setSize(ps);
		contentPane.setPreferredSize(ps);

		JPanel pane = new JPanel(new BorderLayout());
		pane.add(contentPane, BorderLayout.NORTH);

		return pane;
	}

	/**
	 * Create a new {@link ConfigItem} for the given {@link MetaInfo}.
	 * 
	 * @param <E>
	 *            the type of {@link Bundle} to edit
	 * 
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
	static public <E extends Enum<E>> ConfigItem<E> createItem(
			MetaInfo<E> info, int nhgap) {

		ConfigItem<E> configItem;
		switch (info.getFormat()) {
		case BOOLEAN:
			configItem = new ConfigItemBoolean<E>(info);
			break;
		case COLOR:
			configItem = new ConfigItemColor<E>(info);
			break;
		case FILE:
			configItem = new ConfigItemBrowse<E>(info, false);
			break;
		case DIRECTORY:
			configItem = new ConfigItemBrowse<E>(info, true);
			break;
		case COMBO_LIST:
			configItem = new ConfigItemCombobox<E>(info, true);
			break;
		case FIXED_LIST:
			configItem = new ConfigItemCombobox<E>(info, false);
			break;
		case INT:
			configItem = new ConfigItemInteger<E>(info);
			break;
		case PASSWORD:
			configItem = new ConfigItemPassword<E>(info);
			break;
		case LOCALE:
			configItem = new ConfigItemLocale<E>(info);
			break;
		case STRING:
		default:
			configItem = new ConfigItemString<E>(info);
			break;
		}

		configItem.init(nhgap);
		return configItem;
	}

	/**
	 * Set an image to the given {@link JButton}, with a fallback text if it
	 * fails.
	 * 
	 * @param button
	 *            the button to set
	 * @param image64
	 *            the image in BASE64 (should be PNG or similar)
	 * @param fallbackText
	 *            text to use in case the image cannot be created
	 */
	static protected void setImage(JLabel button, String image64,
			String fallbackText) {
		try {
			Image img = new Image(image64);
			try {
				BufferedImage bImg = ImageUtilsAwt.fromImage(img);
				button.setIcon(new ImageIcon(bImg));
			} finally {
				img.close();
			}
		} catch (IOException e) {
			// This is an hard-coded image, should not happen
			button.setText(fallbackText);
		}
	}

	/**
	 * Set an image to the given {@link JButton}, with a fallback text if it
	 * fails.
	 * 
	 * @param button
	 *            the button to set
	 * @param image64
	 *            the image in BASE64 (should be PNG or similar)
	 * @param fallbackText
	 *            text to use in case the image cannot be created
	 */
	static protected void setImage(JButton button, String image64,
			String fallbackText) {
		try {
			Image img = new Image(image64);
			try {
				BufferedImage bImg = ImageUtilsAwt.fromImage(img);
				button.setIcon(new ImageIcon(bImg));
			} finally {
				img.close();
			}
		} catch (IOException e) {
			// This is an hard-coded image, should not happen
			button.setText(fallbackText);
		}
	}

	static private int getMinimumHeight() {
		if (minimumHeight < 0) {
			minimumHeight = new JTextField("Test").getMinimumSize().height;
		}

		return minimumHeight;
	}
}
