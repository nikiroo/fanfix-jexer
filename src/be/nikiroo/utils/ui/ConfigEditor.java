package be.nikiroo.utils.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.resources.Bundle;
import be.nikiroo.utils.resources.MetaInfo;

/**
 * A configuration panel for a {@link Bundle}.
 * <p>
 * All the items in the given {@link Bundle} will be displayed in editable
 * controls, with options to Save, Reset and/or Reset to the application default
 * values.
 * 
 * @author niki
 * 
 * @param <E>
 *            the type of {@link Bundle} to edit
 */
public class ConfigEditor<E extends Enum<E>> extends JPanel {
	private static final long serialVersionUID = 1L;
	private List<MetaInfo<E>> items;

	/**
	 * Create a new {@link ConfigEditor} for this {@link Bundle}.
	 * 
	 * @param type
	 *            a class instance of the item type to work on
	 * @param bundle
	 *            the {@link Bundle} to sort through
	 * @param title
	 *            the title to display before the options
	 */
	public ConfigEditor(Class<E> type, final Bundle<E> bundle, String title) {
		this.setLayout(new BorderLayout());

		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.PAGE_AXIS));
		main.setBorder(new EmptyBorder(5, 5, 5, 5));

		main.add(new JLabel(title));

		items = new ArrayList<MetaInfo<E>>();
		List<MetaInfo<E>> groupedItems = MetaInfo.getItems(type, bundle);
		for (MetaInfo<E> item : groupedItems) {
			// will init this.items
			addItem(main, item, 0);
		}

		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.PAGE_AXIS));
		buttons.setBorder(new EmptyBorder(5, 5, 5, 5));

		buttons.add(createButton("Reset", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (MetaInfo<E> item : items) {
					item.reload();
				}
			}
		}));

		buttons.add(createButton("Default", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object snap = bundle.takeSnapshot();
				bundle.reload(true);
				for (MetaInfo<E> item : items) {
					item.reload();
				}
				bundle.reload(false);
				bundle.restoreSnapshot(snap);
			}
		}));

		buttons.add(createButton("Save", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (MetaInfo<E> item : items) {
					item.save(true);
				}

				try {
					bundle.updateFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}));

		JScrollPane scroll = new JScrollPane(main);
		scroll.getVerticalScrollBar().setUnitIncrement(16);

		this.add(scroll, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);
	}

	private void addItem(JPanel main, MetaInfo<E> item, int nhgap) {
		if (item.isGroup()) {
			JPanel bpane = new JPanel(new BorderLayout());
			bpane.setBorder(new TitledBorder(item.getName()));
			JPanel pane = new JPanel();
			pane.setBorder(new EmptyBorder(5, 5, 5, 5));
			pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

			String info = item.getDescription();
			info = StringUtils.justifyTexts(info, 100);
			if (!info.isEmpty()) {
				info = info + "\n";
				JTextArea text = new JTextArea(info);
				text.setWrapStyleWord(true);
				text.setOpaque(false);
				text.setForeground(new Color(100, 100, 180));
				text.setEditable(false);
				pane.add(text);
			}

			for (MetaInfo<E> subitem : item) {
				addItem(pane, subitem, nhgap + 11);
			}
			bpane.add(pane, BorderLayout.CENTER);
			main.add(bpane);
		} else {
			items.add(item);
			main.add(ConfigItem.createItem(item, nhgap));
		}
	}

	/**
	 * Add an action button for this action.
	 * 
	 * @param title
	 *            the action title
	 * @param listener
	 *            the action
	 */
	private JComponent createButton(String title, ActionListener listener) {
		JButton button = new JButton(title);
		button.addActionListener(listener);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(2, 10, 2, 10));
		panel.add(button, BorderLayout.CENTER);

		return panel;
	}
}
