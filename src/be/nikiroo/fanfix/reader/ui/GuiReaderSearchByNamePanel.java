package be.nikiroo.fanfix.reader.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.searchable.BasicSearchable;
import be.nikiroo.fanfix.searchable.SearchableTag;
import be.nikiroo.fanfix.supported.SupportType;

/**
 * This panel represents a search panel that works for keywords and tags based
 * searches.
 * 
 * @author niki
 */
// JCombobox<E> not 1.6 compatible
@SuppressWarnings({ "unchecked", "rawtypes" })
public class GuiReaderSearchByNamePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private int actionEventId = ActionEvent.ACTION_FIRST;

	private SupportType supportType;
	private BasicSearchable searchable;
	private int page;
	private boolean searchByTags;

	private String keywords;
	private JTabbedPane searchTabs;
	private JTextField keywordsField;
	private JButton submitKeywords;

	private JPanel tagBars;
	private List<JComboBox> combos;
	private JComboBox comboSupportTypes;

	private List<ActionListener> actions = new ArrayList<ActionListener>();
	private List<MetaData> stories = new ArrayList<MetaData>();
	private int storyItem;

	// will throw illegalArgEx if bad support type, NULL allowed
	public GuiReaderSearchByNamePanel(SupportType supportType) {
		setLayout(new BorderLayout());

		// TODO: check if null really is OK for supportType (must be)
		
		setSupportType(supportType);
		page = 1;
		searchByTags = false;

		searchTabs = new JTabbedPane();
		searchTabs.addTab("By name", createByNameSearchPanel());
		searchTabs.addTab("By tags", createByTagSearchPanel());

		add(searchTabs, BorderLayout.CENTER);
	}

	private JPanel createByNameSearchPanel() {
		JPanel byName = new JPanel(new BorderLayout());

		keywordsField = new JTextField();
		byName.add(keywordsField, BorderLayout.CENTER);

		submitKeywords = new JButton("Search");
		byName.add(submitKeywords, BorderLayout.EAST);

		// TODO: ENTER -> search

		submitKeywords.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				search(keywordsField.getText(), 0, null);
			}
		});

		return byName;
	}

	private JPanel createByTagSearchPanel() {
		combos = new ArrayList<JComboBox>();

		JPanel byTag = new JPanel();
		tagBars = new JPanel();
		tagBars.setLayout(new BoxLayout(tagBars, BoxLayout.Y_AXIS));
		byTag.add(tagBars, BorderLayout.NORTH);

		return byTag;
	}

	public SupportType getSupportType() {
		return supportType;
	}

	public void setSupportType(SupportType supportType) {
		BasicSearchable searchable = BasicSearchable.getSearchable(supportType);
		if (searchable == null && supportType != null) {
			throw new java.lang.IllegalArgumentException(
					"Unupported support type: " + supportType);
		}

		// TODO: if <>, reset all
		// if new, set the base tags
		
		this.supportType = supportType;
		this.searchable = searchable;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		// TODO: set against maxPage
		// TODO: update last search?
		this.page = page;
	}

	// actions will be fired in UIthread
	public void addActionListener(ActionListener action) {
		actions.add(action);
	}

	public boolean removeActionListener(ActionListener action) {
		return actions.remove(action);
	}

	public List<MetaData> getStories() {
		return stories;
	}

	// selected item or 0 if none ! one-based !
	public int getStoryItem() {
		return storyItem;
	}

	private void fireAction(final Runnable inUi) {
		GuiReaderSearchFrame.inUi(new Runnable() {
			@Override
			public void run() {
				ActionEvent ae = new ActionEvent(
						GuiReaderSearchByNamePanel.this, actionEventId,
						"stories found");

				actionEventId++;
				if (actionEventId > ActionEvent.ACTION_LAST) {
					actionEventId = ActionEvent.ACTION_FIRST;
				}

				for (ActionListener action : actions) {
					try {
						action.actionPerformed(ae);
					} catch (Exception e) {
						GuiReaderSearchFrame.error(e);
					}
				}
				
				if (inUi != null) {
					inUi.run();
				}
			}
		});
	}

	private void updateSearchBy(final boolean byTag) {
		if (byTag != this.searchByTags) {
			GuiReaderSearchFrame.inUi(new Runnable() {
				@Override
				public void run() {
					if (!byTag) {
						searchTabs.setSelectedIndex(0);
					} else {
						searchTabs.setSelectedIndex(1);
					}
				}
			});
		}
	}

	// cannot be NULL
	private void updateKeywords(final String keywords) {
		if (!keywords.equals(this.keywords)) {
			GuiReaderSearchFrame.inUi(new Runnable() {
				@Override
				public void run() {
					GuiReaderSearchByNamePanel.this.keywords = keywords;
					keywordsField.setText(keywords);
				}
			});
		}
	}

	// update and reset the tagsbar
	// can be NULL, for base tags
	private void updateTags(final SearchableTag tag) {
		final List<SearchableTag> parents = new ArrayList<SearchableTag>();
		SearchableTag parent = (tag == null) ? null : tag;
		while (parent != null) {
			parents.add(parent);
			parent = parent.getParent();
		}

		List<SearchableTag> rootTags = null;
		SearchableTag selectedRootTag = null;
		selectedRootTag = parents.isEmpty() ? null
				: parents.get(parents.size() - 1);

		try {
			rootTags = searchable.getTags();
		} catch (IOException e) {
			GuiReaderSearchFrame.error(e);
		}

		final List<SearchableTag> rootTagsF = rootTags;
		final SearchableTag selectedRootTagF = selectedRootTag;

		GuiReaderSearchFrame.inUi(new Runnable() {
			@Override
			public void run() {
				tagBars.invalidate();
				tagBars.removeAll();

				addTagBar(rootTagsF, selectedRootTagF);

				for (int i = parents.size() - 1; i >= 0; i--) {
					SearchableTag selectedChild = null;
					if (i > 0) {
						selectedChild = parents.get(i - 1);
					}

					SearchableTag parent = parents.get(i);
					addTagBar(parent.getChildren(), selectedChild);
				}

				tagBars.validate();
			}
		});
	}

	// must be quick and no thread change
	private void addTagBar(List<SearchableTag> tags,
			final SearchableTag selected) {
		tags.add(0, null);

		final int comboIndex = combos.size();

		final JComboBox combo = new JComboBox(
				tags.toArray(new SearchableTag[] {}));
		combo.setSelectedItem(selected);

		final ListCellRenderer basic = combo.getRenderer();

		combo.setRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				Object displayValue = value;
				if (value instanceof SearchableTag) {
					displayValue = ((SearchableTag) value).getName();
				} else {
					displayValue = "Select a tag...";
					cellHasFocus = false;
					isSelected = false;
				}

				Component rep = basic.getListCellRendererComponent(list,
						displayValue, index, isSelected, cellHasFocus);

				if (value == null) {
					rep.setForeground(Color.GRAY);
				}

				return rep;
			}
		});

		combo.addActionListener(createComboTagAction(comboIndex));

		combos.add(combo);
		tagBars.add(combo);
	}
	
	private ActionListener createComboTagAction(final int comboIndex) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				List<JComboBox> combos = GuiReaderSearchByNamePanel.this.combos;
				if (combos == null || comboIndex < 0 || comboIndex >= combos.size()) {
					return;
				}
				
				// Tag can be NULL
				final SearchableTag tag = (SearchableTag) combos.get(comboIndex)
						.getSelectedItem();
				
				while (comboIndex + 1 < combos.size()) {
					JComboBox combo = combos.remove(comboIndex + 1);
					tagBars.remove(combo);
				}

				new Thread(new Runnable() {
					@Override
					public void run() {
						final List<SearchableTag> children = getChildrenForTag(tag);
						if (children != null) {
							GuiReaderSearchFrame.inUi(new Runnable() {
								@Override
								public void run() {
									addTagBar(children, tag);
								}
							});
						}
						
						if (tag != null && tag.isLeaf()) {
							try {
								GuiReaderSearchByNamePanel.this.page = 1;
								stories = searchable.search(tag, 1);
							} catch (IOException e) {
								GuiReaderSearchFrame.error(e);
								GuiReaderSearchByNamePanel.this.page = 0;
								stories = new ArrayList<MetaData>();
							}
							
							fireAction(null);
						}
					}
				}).start();
			}
		};
	}

	// sync, add children of tag, NULL = base tags
	// return children of the tag or base tags or NULL
	private List<SearchableTag> getChildrenForTag(final SearchableTag tag) {
		List<SearchableTag> children = new ArrayList<SearchableTag>();
		if (tag == null) {
			try {
				List<SearchableTag> baseTags = searchable.getTags();
				children = baseTags;
			} catch (IOException e) {
				GuiReaderSearchFrame.error(e);
			}
		} else {
			try {
				searchable.fillTag(tag);
			} catch (IOException e) {
				GuiReaderSearchFrame.error(e);
			}

			if (!tag.isLeaf()) {
				children = tag.getChildren();
			} else {
				children = null;
			}
		}

		return children;
	}

	// item 0 = no selection, else = default selection
	// return: maxpage
	public int search(String keywords, int item, Runnable inUi) {
		List<MetaData> stories = new ArrayList<MetaData>();
		int storyItem = 0;

		updateSearchBy(false);
		updateKeywords(keywords);

		int maxPage = -1;
		try {
			maxPage = searchable.searchPages(keywords);
		} catch (IOException e) {
			GuiReaderSearchFrame.error(e);
		}

		if (page > 0) {
			try {
				stories = searchable.search(keywords, page);
			} catch (IOException e) {
				GuiReaderSearchFrame.error(e);
				stories = new ArrayList<MetaData>();
			}

			if (item > 0 && item <= stories.size()) {
				storyItem = item;
			} else if (item > 0) {
				GuiReaderSearchFrame.error(String.format(
						"Story item does not exist: Search [%s], item %d",
						keywords, item));
			}
		}

		this.stories = stories;
		this.storyItem = storyItem;
		fireAction(inUi);

		return maxPage;
	}

	// tag: null = base tags
	// return: max pages
	public int searchTag(SearchableTag tag, int item, Runnable inUi) {
		List<MetaData> stories = new ArrayList<MetaData>();
		int storyItem = 0;

		updateSearchBy(true);
		updateTags(tag);

		int maxPage = 1;
		if (tag != null) {
			try {
				searchable.fillTag(tag);

				if (!tag.isLeaf()) {
					List<SearchableTag> subtags = tag.getChildren();
					if (item > 0 && item <= subtags.size()) {
						SearchableTag subtag = subtags.get(item - 1);
						try {
							tag = subtag;
							searchable.fillTag(tag);
						} catch (IOException e) {
							GuiReaderSearchFrame.error(e);
						}
					} else if (item > 0) {
						GuiReaderSearchFrame.error(String.format(
								"Tag item does not exist: Tag [%s], item %d",
								tag.getFqName(), item));
					}
				}

				maxPage = searchable.searchPages(tag);
				if (page > 0) {
					if (tag.isLeaf()) {
						try {
							stories = searchable.search(tag, page);
							if (item > 0 && item <= stories.size()) {
								storyItem = item;
							} else if (item > 0) {
								GuiReaderSearchFrame.error(String.format(
										"Story item does not exist: Tag [%s], item %d",
										tag.getFqName(), item));
							}
						} catch (IOException e) {
							GuiReaderSearchFrame.error(e);
						}
					}
				}
			} catch (IOException e) {
				GuiReaderSearchFrame.error(e);
				maxPage = 0;
			}
		}

		this.stories = stories;
		this.storyItem = storyItem;
		fireAction(inUi);

		return maxPage;
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
	public void setEnabled(final boolean waiting) {
		GuiReaderSearchFrame.inUi(new Runnable() {
			@Override
			public void run() {
				GuiReaderSearchByNamePanel.super.setEnabled(!waiting);
				keywordsField.setEnabled(!waiting);
				submitKeywords.setEnabled(!waiting);
				// TODO
			}
		});
	}
}
