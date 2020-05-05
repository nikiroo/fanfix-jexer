package be.nikiroo.fanfix_jexer.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jexer.TAction;
import jexer.TButton;
import jexer.TLabel;
import jexer.TText;
import jexer.TWindow;
import jexer.event.TCommandEvent;
import jexer.event.TResizeEvent;
import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.fanfix.data.Paragraph.ParagraphType;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.fanfix.reader.BasicReader;
import be.nikiroo.jexer.TSizeConstraint;
import be.nikiroo.jexer.TTable;

/**
 * This window will contain the {@link Story} in a readable format, with a
 * chapter browser.
 * 
 * @author niki
 */
class TuiReaderStoryWindow extends TWindow {
	private Story story;
	private TLabel titleField;
	private TText textField;
	private TTable table;
	private int chapter = -99; // invalid value
	private List<TButton> navigationButtons;
	private TLabel currentChapter;
	private List<TSizeConstraint> sizeConstraints = new ArrayList<TSizeConstraint>();

	// chapter: -1 for "none" (0 is desc)
	public TuiReaderStoryWindow(TuiReaderApplication app, Story story,
			int chapter) {
		super(app, desc(story.getMeta()), 0, 0, 60, 18, CENTERED | RESIZABLE);

		this.story = story;

		app.setStatusBar(this, desc(story.getMeta()));

		// last = use window background
		titleField = new TLabel(this, "    Title", 0, 1, "tlabel", false);
		textField = new TText(this, "", 0, 0, 1, 1);
		table = new TTable(this, 0, 0, 1, 1, null, null, Arrays.asList("Key",
				"Value"), true);

		titleField.setEnabled(false);

		navigationButtons = new ArrayList<TButton>(5);

		navigationButtons.add(addButton("<<", 0, 0, new TAction() {
			@Override
			public void DO() {
				setChapter(-1);
			}
		}));
		navigationButtons.add(addButton("< ", 4, 0, new TAction() {
			@Override
			public void DO() {
				setChapter(TuiReaderStoryWindow.this.chapter - 1);
			}
		}));
		navigationButtons.add(addButton("> ", 7, 0, new TAction() {
			@Override
			public void DO() {
				setChapter(TuiReaderStoryWindow.this.chapter + 1);
			}
		}));
		navigationButtons.add(addButton(">>", 10, 0, new TAction() {
			@Override
			public void DO() {
				setChapter(getStory().getChapters().size());
			}
		}));

		navigationButtons.get(0).setEnabled(false);
		navigationButtons.get(1).setEnabled(false);

		currentChapter = addLabel("", 0, 0);

		TSizeConstraint.setSize(sizeConstraints, textField, 1, 3, -1, -1);
		TSizeConstraint.setSize(sizeConstraints, table, 0, 3, 0, -1);
		TSizeConstraint.setSize(sizeConstraints, currentChapter, 14, -3, -1,
				null);

		for (TButton navigationButton : navigationButtons) {
			navigationButton.setShadowColor(null);
			// navigationButton.setEmptyBorders(false);
			TSizeConstraint.setSize(sizeConstraints, navigationButton, null,
					-3, null, null);
		}

		onResize(null);

		setChapter(chapter);
	}

	@Override
	public void onResize(TResizeEvent resize) {
		if (resize != null) {
			super.onResize(resize);
		}

		// TODO: find out why TText and TTable does not behave the same way
		// (offset of 2 for height and width)

		TSizeConstraint.resize(sizeConstraints);

		// Improve the disposition of the scrollbars
		textField.getVerticalScroller().setX(textField.getWidth());
		textField.getVerticalScroller().setHeight(textField.getHeight());
		textField.getHorizontalScroller().setX(-1);
		textField.getHorizontalScroller().setWidth(textField.getWidth() + 1);

		setCurrentChapterText();
	}

	/**
	 * Display the current chapter in the window, or the {@link Story} info
	 * page.
	 * 
	 * @param chapter
	 *            the chapter (including "0" which is the description) or "-1"
	 *            to display the info page instead
	 */
	private void setChapter(int chapter) {
		if (chapter > getStory().getChapters().size()) {
			chapter = getStory().getChapters().size();
		}

		if (chapter != this.chapter) {
			this.chapter = chapter;

			int max = getStory().getChapters().size();
			navigationButtons.get(0).setEnabled(chapter > -1);
			navigationButtons.get(1).setEnabled(chapter > -1);
			navigationButtons.get(2).setEnabled(chapter < max);
			navigationButtons.get(3).setEnabled(chapter < max);

			if (chapter < 0) {
				displayInfoPage();
			} else {
				displayChapterPage();
			}
		}

		setCurrentChapterText();
	}

	/**
	 * Append the info page about the current {@link Story}.
	 * 
	 * @param builder
	 *            the builder to append to
	 */
	private void displayInfoPage() {
		textField.setVisible(false);
		table.setVisible(true);
		textField.setEnabled(false);
		table.setEnabled(true);

		MetaData meta = getStory().getMeta();

		setCurrentTitle(meta.getTitle());

		Map<String, String> metaDesc = BasicReader.getMetaDesc(meta);
		String[][] metaDescObj = new String[metaDesc.size()][2];
		int i = 0;
		for (String key : metaDesc.keySet()) {
			metaDescObj[i][0] = " " + key;
			metaDescObj[i][1] = metaDesc.get(key);
			i++;
		}

		table.setRowData(metaDescObj);
		table.setHeaders(Arrays.asList("key", "value"), false);
		table.toTop();
	}

	/**
	 * Append the current chapter.
	 * 
	 * @param builder
	 *            the builder to append to
	 */
	private void displayChapterPage() {
		table.setVisible(false);
		textField.setVisible(true);
		table.setEnabled(false);
		textField.setEnabled(true);

		StringBuilder builder = new StringBuilder();

		Chapter chap = null;
		if (chapter == 0) {
			chap = getStory().getMeta().getResume();
		} else if (chapter > 0) {
			chap = getStory().getChapters().get(chapter - 1);
		}

		// TODO: i18n
		String chapName = chap == null ? "[No RESUME]" : chap.getName();
		setCurrentTitle(String.format("Chapter %d: %s", chapter, chapName));

		if (chap != null) {
			for (Paragraph para : chap) {
				if (para.getType() == ParagraphType.BREAK) {
					builder.append("\n");
				}
				builder.append(para.getContent()).append("\n");
				if (para.getType() == ParagraphType.BREAK) {
					builder.append("\n");
				}
			}
		}

		setText(builder.toString());
	}

	private Story getStory() {
		return story;
	}

	/**
	 * Display the given text on the window.
	 * 
	 * @param text
	 *            the text to display
	 */
	private void setText(String text) {
		textField.setText(text);
		textField.reflowData();
		textField.toTop();
	}

	/**
	 * Set the current chapter area to the correct value.
	 */
	private void setCurrentChapterText() {
		String name;
		if (chapter < 0) {
			name = " " + getStory().getMeta().getTitle();
		} else if (chapter == 0) {
			Chapter resume = getStory().getMeta().getResume();
			if (resume != null) {
				name = String.format(" %s", resume.getName());
			} else {
				// TODO: i18n
				name = "[No RESUME]";
			}
		} else {
			int max = getStory().getChapters().size();
			Chapter chap = getStory().getChapters().get(chapter - 1);
			name = String.format(" %d/%d: %s", chapter, max, chap.getName());
		}

		int width = getWidth() - currentChapter.getX();
		name = String.format("%-" + width + "s", name);
		if (name.length() > width) {
			name = name.substring(0, width);
		}

		currentChapter.setLabel(name);
	}

	/**
	 * Set the current title in-window.
	 * 
	 * @param title
	 *            the new title
	 */
	private void setCurrentTitle(String title) {
		String pad = "";
		if (title.length() < getWidth()) {
			int padSize = (getWidth() - title.length()) / 2;
			pad = String.format("%" + padSize + "s", "");
		}

		title = pad + title + pad;
		titleField.setWidth(title.length());
		titleField.setLabel(title);
	}

	private static String desc(MetaData meta) {
		return String.format("%s: %s", meta.getLuid(), meta.getTitle());
	}

	@Override
	public void onCommand(TCommandEvent command) {
		if (command.getCmd().equals(TuiReaderApplication.CMD_EXIT)) {
			TuiReaderApplication.close(this);
		} else {
			// Handle our own event if needed here
			super.onCommand(command);
		}
	}
}
