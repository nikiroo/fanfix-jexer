package be.nikiroo.utils.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import be.nikiroo.utils.resources.MetaInfo;

class ConfigItemBrowse<E extends Enum<E>> extends ConfigItem<E> {
	private static final long serialVersionUID = 1L;

	private boolean dir;
	private Map<JComponent, JTextField> fields = new HashMap<JComponent, JTextField>();

	/**
	 * Create a new {@link ConfigItemBrowse} for the given {@link MetaInfo}.
	 * 
	 * @param info
	 *            the {@link MetaInfo}
	 * @param dir
	 *            TRUE for directory browsing, FALSE for file browsing
	 */
	public ConfigItemBrowse(MetaInfo<E> info, boolean dir) {
		super(info, false);
		this.dir = dir;
	}

	@Override
	protected Object getFromField(int item) {
		JTextField field = fields.get(getField(item));
		if (field != null) {
			return new File(field.getText());
		}

		return null;
	}

	@Override
	protected Object getFromInfo(int item) {
		String path = getInfo().getString(item, false);
		if (path != null && !path.isEmpty()) {
			return new File(path);
		}

		return null;
	}

	@Override
	protected void setToField(Object value, int item) {
		JTextField field = fields.get(getField(item));
		if (field != null) {
			field.setText(value == null ? "" : ((File) value).getPath());
		}
	}

	@Override
	protected void setToInfo(Object value, int item) {
		getInfo().setString(((File) value).getPath(), item);
	}

	@Override
	protected JComponent createEmptyField(final int item) {
		final JPanel pane = new JPanel(new BorderLayout());
		final JTextField field = new JTextField();
		field.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				File file = null;
				if (!field.getText().isEmpty()) {
					file = new File(field.getText());
				}

				if (hasValueChanged(file, item)) {
					setDirtyItem(item);
				}
			}
		});

		final JButton browseButton = new JButton("...");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory((File) getFromInfo(item));
				chooser.setFileSelectionMode(dir ? JFileChooser.DIRECTORIES_ONLY
						: JFileChooser.FILES_ONLY);
				if (chooser.showOpenDialog(ConfigItemBrowse.this) == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					if (file != null) {
						setToField(file, item);
						if (hasValueChanged(file, item)) {
							setDirtyItem(item);
						}
					}
				}
			}
		});

		pane.add(browseButton, BorderLayout.WEST);
		pane.add(field, BorderLayout.CENTER);

		fields.put(pane, field);
		return pane;
	}
}
