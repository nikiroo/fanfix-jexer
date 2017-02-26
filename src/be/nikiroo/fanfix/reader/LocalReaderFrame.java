package be.nikiroo.fanfix.reader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.Library;
import be.nikiroo.fanfix.bundles.UiConfig;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.fanfix.output.BasicOutput.OutputType;
import be.nikiroo.fanfix.reader.LocalReaderBook.BookActionListener;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.ui.ProgressBar;
import be.nikiroo.utils.ui.WrapLayout;

/**
 * A {@link Frame} that will show a {@link LocalReaderBook} item for each
 * {@link Story} in the main cache ({@link Instance#getCache()}), and offer a
 * way to copy them to the {@link LocalReader} cache ({@link LocalReader#lib}),
 * read them, delete them...
 * 
 * @author niki
 */
class LocalReaderFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private LocalReader reader;
	private List<MetaData> stories;
	private List<LocalReaderBook> books;
	private JPanel bookPane;
	private String type;
	private Color color;
	private ProgressBar pgBar;
	private JMenuBar bar;
	private LocalReaderBook selectedBook;

	/**
	 * Create a new {@link LocalReaderFrame}.
	 * 
	 * @param reader
	 *            the associated {@link LocalReader} to forward some commands
	 *            and access its {@link Library}
	 * @param type
	 *            the type of {@link Story} to load, or NULL for all types
	 */
	public LocalReaderFrame(LocalReader reader, String type) {
		super("Fanfix Library");

		this.reader = reader;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setLayout(new BorderLayout());

		books = new ArrayList<LocalReaderBook>();
		bookPane = new JPanel(new WrapLayout(WrapLayout.LEADING, 5, 5));

		color = Instance.getUiConfig().getColor(UiConfig.BACKGROUND_COLOR);

		if (color != null) {
			setBackground(color);
			bookPane.setBackground(color);
		}

		JScrollPane scroll = new JScrollPane(bookPane);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(scroll, BorderLayout.CENTER);

		pgBar = new ProgressBar();
		add(pgBar, BorderLayout.SOUTH);

		refreshBooks(type);
		setJMenuBar(createMenu());

		setVisible(true);
	}

	/**
	 * Refresh the list of {@link LocalReaderBook}s from disk.
	 * 
	 * @param type
	 *            the type of {@link Story} to load, or NULL for all types
	 */
	private void refreshBooks(String type) {
		this.type = type;
		stories = Instance.getLibrary().getList(type);
		books.clear();
		bookPane.invalidate();
		bookPane.removeAll();
		for (MetaData meta : stories) {
			LocalReaderBook book = new LocalReaderBook(meta,
					reader.isCached(meta.getLuid()));
			if (color != null) {
				book.setBackground(color);
			}

			books.add(book);

			book.addActionListener(new BookActionListener() {
				public void select(LocalReaderBook book) {
					selectedBook = book;
					for (LocalReaderBook abook : books) {
						abook.setSelected(abook == book);
					}
				}

				public void popupRequested(LocalReaderBook book, MouseEvent e) {
					JPopupMenu popup = new JPopupMenu();
					popup.add(createMenuItemOpenBook());
					popup.addSeparator();
					popup.add(createMenuItemExport());
					popup.add(createMenuItemClearCache());
					popup.add(createMenuItemRedownload());
					popup.addSeparator();
					popup.add(createMenuItemDelete());
					popup.show(e.getComponent(), e.getX(), e.getY());
				}

				public void action(final LocalReaderBook book) {
					openBook(book);
				}
			});

			bookPane.add(book);
		}

		bookPane.validate();
		bookPane.repaint();
	}

	/**
	 * Create the main menu bar.
	 * 
	 * @return the bar
	 */
	private JMenuBar createMenu() {
		bar = new JMenuBar();

		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);

		JMenuItem imprt = new JMenuItem("Import URL...", KeyEvent.VK_U);
		imprt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imprt(true);
			}
		});
		JMenuItem imprtF = new JMenuItem("Import File...", KeyEvent.VK_F);
		imprtF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imprt(false);
			}
		});
		JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LocalReaderFrame.this.dispatchEvent(new WindowEvent(
						LocalReaderFrame.this, WindowEvent.WINDOW_CLOSING));
			}
		});

		file.add(createMenuItemOpenBook());
		file.add(createMenuItemExport());
		file.addSeparator();
		file.add(imprt);
		file.add(imprtF);
		file.addSeparator();
		file.add(exit);

		bar.add(file);

		JMenu edit = new JMenu("Edit");
		edit.setMnemonic(KeyEvent.VK_E);

		edit.add(createMenuItemClearCache());
		edit.add(createMenuItemRedownload());
		edit.addSeparator();
		edit.add(createMenuItemDelete());

		bar.add(edit);

		JMenu view = new JMenu("View");
		view.setMnemonic(KeyEvent.VK_V);

		List<String> tt = Instance.getLibrary().getTypes();
		tt.add(0, null);
		for (final String type : tt) {
			JMenuItem item = new JMenuItem(type == null ? "All books" : type);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					refreshBooks(type);
				}
			});
			view.add(item);

			if (type == null) {
				view.addSeparator();
			}
		}

		bar.add(view);

		return bar;
	}

	/**
	 * Create the export menu item.
	 * 
	 * @return the item
	 */
	private JMenuItem createMenuItemExport() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);

		final Map<FileFilter, OutputType> filters = new HashMap<FileFilter, OutputType>();
		for (OutputType type : OutputType.values()) {
			String ext = type.getDefaultExtension(false);
			String desc = type.getDesc(false);
			if (ext == null || ext.isEmpty()) {
				filters.put(createAllFilter(desc), type);
			} else {
				filters.put(new FileNameExtensionFilter(desc, ext), type);
			}
		}

		// First the "ALL" filters, then, the extension filters
		for (Entry<FileFilter, OutputType> entry : filters.entrySet()) {
			if (!(entry.getKey() instanceof FileNameExtensionFilter)) {
				fc.addChoosableFileFilter(entry.getKey());
			}
		}
		for (Entry<FileFilter, OutputType> entry : filters.entrySet()) {
			if (entry.getKey() instanceof FileNameExtensionFilter) {
				fc.addChoosableFileFilter(entry.getKey());
			}
		}
		//

		JMenuItem export = new JMenuItem("Save as...", KeyEvent.VK_S);
		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedBook != null) {
					fc.showDialog(LocalReaderFrame.this, "Save");
					final OutputType type = filters.get(fc.getFileFilter());
					final String path = fc.getSelectedFile().getAbsolutePath()
							+ type.getDefaultExtension(false);
					final Progress pg = new Progress();
					outOfUi(pg, new Runnable() {
						public void run() {
							try {
								Instance.getLibrary().export(
										selectedBook.getMeta().getLuid(), type,
										path, pg);
							} catch (IOException e) {
								Instance.syserr(e);
							}
						}
					});
				}
			}
		});

		return export;
	}

	/**
	 * Create a {@link FileFilter} that accepts all files and return the given
	 * description.
	 * 
	 * @param desc
	 *            the description
	 * 
	 * @return the filter
	 */
	private FileFilter createAllFilter(final String desc) {
		return new FileFilter() {
			@Override
			public String getDescription() {
				return desc;
			}

			@Override
			public boolean accept(File f) {
				return true;
			}
		};
	}

	/**
	 * Create the refresh (delete cache) menu item.
	 * 
	 * @return the item
	 */
	private JMenuItem createMenuItemClearCache() {
		JMenuItem refresh = new JMenuItem("Clear cache", KeyEvent.VK_C);
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedBook != null) {
					outOfUi(null, new Runnable() {
						public void run() {
							reader.refresh(selectedBook.getMeta().getLuid());
							selectedBook.setCached(false);
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									selectedBook.repaint();
								}
							});
						}
					});
				}
			}
		});

		return refresh;
	}

	/**
	 * Create the redownload (then delete original) menu item.
	 * 
	 * @return the item
	 */
	private JMenuItem createMenuItemRedownload() {
		JMenuItem refresh = new JMenuItem("Redownload", KeyEvent.VK_R);
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedBook != null) {
					imprt(selectedBook.getMeta().getUrl(), new Runnable() {
						public void run() {
							reader.delete(selectedBook.getMeta().getLuid());
							selectedBook = null;
						}
					});
				}
			}
		});

		return refresh;
	}

	/**
	 * Create the delete menu item.
	 * 
	 * @return the item
	 */
	private JMenuItem createMenuItemDelete() {
		JMenuItem delete = new JMenuItem("Delete", KeyEvent.VK_D);
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedBook != null) {
					outOfUi(null, new Runnable() {
						public void run() {
							reader.delete(selectedBook.getMeta().getLuid());
							selectedBook = null;
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									refreshBooks(type);
								}
							});
						}
					});
				}
			}
		});

		return delete;
	}

	/**
	 * Create the open menu item.
	 * 
	 * @return the item
	 */
	private JMenuItem createMenuItemOpenBook() {
		JMenuItem open = new JMenuItem("Open", KeyEvent.VK_O);
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedBook != null) {
					openBook(selectedBook);
				}
			}
		});

		return open;
	}

	/**
	 * Open a {@link LocalReaderBook} item.
	 * 
	 * @param book
	 *            the {@link LocalReaderBook} to open
	 */
	private void openBook(final LocalReaderBook book) {
		final Progress pg = new Progress();
		outOfUi(pg, new Runnable() {
			public void run() {
				try {
					reader.open(book.getMeta().getLuid(), pg);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							book.setCached(true);
						}
					});
				} catch (IOException e) {
					// TODO: error message?
					Instance.syserr(e);
				}
			}
		});
	}

	/**
	 * Process the given action out of the Swing UI thread and link the given
	 * {@link ProgressBar} to the action.
	 * <p>
	 * The code will make sure that the {@link ProgressBar} (if not NULL) is set
	 * to done when the action is done.
	 * 
	 * @param pg
	 *            the {@link ProgressBar} or NULL
	 * @param run
	 *            the action to run
	 */
	private void outOfUi(final Progress pg, final Runnable run) {
		pgBar.setProgress(pg);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setEnabled(false);
				pgBar.addActioListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						pgBar.setProgress(null);
						setEnabled(true);
					}
				});
			}
		});

		new Thread(new Runnable() {
			public void run() {
				run.run();
				if (pg == null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							setEnabled(true);
						}
					});
				} else if (!pg.isDone()) {
					pg.setProgress(pg.getMax());
				}
			}
		}).start();
	}

	/**
	 * Import a {@link Story} into the main {@link Library}.
	 * <p>
	 * Should be called inside the UI thread.
	 * 
	 * @param askUrl
	 *            TRUE for an {@link URL}, false for a {@link File}
	 */
	private void imprt(boolean askUrl) {
		JFileChooser fc = new JFileChooser();

		String url;
		if (askUrl) {
			url = JOptionPane.showInputDialog(LocalReaderFrame.this,
					"url of the story to import?", "Importing from URL",
					JOptionPane.QUESTION_MESSAGE);
		} else if (fc.showOpenDialog(this) != JFileChooser.CANCEL_OPTION) {
			url = fc.getSelectedFile().getAbsolutePath();
		} else {
			url = null;
		}

		if (url != null && !url.isEmpty()) {
			imprt(url, null);
		}
	}

	/**
	 * Actually import the {@link Story} into the main {@link Library}.
	 * <p>
	 * Should be called inside the UI thread.
	 * 
	 * @param url
	 *            the {@link Story} to import by {@link URL}
	 * @param onSuccess
	 *            Action to execute on success
	 */
	private void imprt(final String url, final Runnable onSuccess) {
		final Progress pg = new Progress("Importing " + url);
		outOfUi(pg, new Runnable() {
			public void run() {
				Exception ex = null;
				try {
					Instance.getLibrary().imprt(BasicReader.getUrl(url), pg);
				} catch (IOException e) {
					ex = e;
				}

				final Exception e = ex;

				final boolean ok = (e == null);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (!ok) {
							JOptionPane.showMessageDialog(
									LocalReaderFrame.this, "Cannot import: "
											+ url, e.getMessage(),
									JOptionPane.ERROR_MESSAGE);

							setEnabled(true);
						} else {
							refreshBooks(type);
							if (onSuccess != null) {
								onSuccess.run();
								refreshBooks(type);
							}
						}
					}
				});
			}
		});
	}

	/**
	 * Enables or disables this component, depending on the value of the
	 * parameter <code>b</code>. An enabled component can respond to user input
	 * and generate events. Components are enabled initially by default.
	 * <p>
	 * Disabling this component will also affect its children.
	 * 
	 * @param b
	 *            If <code>true</code>, this component is enabled; otherwise
	 *            this component is disabled
	 */
	@Override
	public void setEnabled(boolean b) {
		for (LocalReaderBook book : books) {
			book.setEnabled(b);
			book.repaint();
		}

		bar.setEnabled(b);
		bookPane.setEnabled(b);
		bookPane.repaint();

		super.setEnabled(b);
		repaint();
	}
}
