package be.nikiroo.fanfix.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.fanfix.library.BasicLibrary;
import be.nikiroo.fanfix.library.LocalLibrary;
import be.nikiroo.fanfix.output.BasicOutput.OutputType;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class LibraryTest extends TestLauncher {
	private BasicLibrary lib;
	private File tmp;

	public LibraryTest(String[] args) {
		super("Library", args);

		final String luid1 = "001"; // A
		final String luid2 = "002"; // B
		final String luid3 = "003"; // B then A, then B
		final String name1 = "My story 1";
		final String name2 = "My story 2";
		final String name3 = "My story 3";
		final String name3ex = "My story 3 [edited]";
		final String source1 = "Source A";
		final String source2 = "Source B";
		final String author1 = "Unknown author";
		final String author2 = "Another other otter author";

		final String errMess = "The resulting stories in the list are not what we expected";

		addSeries(new TestLauncher("Local", args) {
			{
				addTest(new TestCase("getList") {
					@Override
					public void test() throws Exception {
						List<MetaData> metas = lib.getList().getMetas();
						assertEquals(errMess, Arrays.asList(),
								titlesAsList(metas));
					}
				});

				addTest(new TestCase("save") {
					@Override
					public void test() throws Exception {
						lib.save(story(luid1, name1, source1, author1), luid1,
								null);

						List<MetaData> metas = lib.getList().getMetas();
						assertEquals(errMess, Arrays.asList(name1),
								titlesAsList(metas));
					}
				});

				addTest(new TestCase("save more") {
					@Override
					public void test() throws Exception {
						List<MetaData> metas = null;

						lib.save(story(luid2, name2, source2, author1), luid2,
								null);

						metas = lib.getList().getMetas();
						assertEquals(errMess, Arrays.asList(name1, name2),
								titlesAsList(metas));

						lib.save(story(luid3, name3, source2, author1), luid3,
								null);

						metas = lib.getList().getMetas();
						assertEquals(errMess,
								Arrays.asList(name1, name2, name3),
								titlesAsList(metas));
					}
				});

				addTest(new TestCase("save override luid (change author)") {
					@Override
					public void test() throws Exception {
						// same luid as a previous one
						lib.save(story(luid3, name3ex, source2, author2),
								luid3, null);

						List<MetaData> metas = lib.getList().getMetas();
						assertEquals(errMess,
								Arrays.asList(name1, name2, name3ex),
								titlesAsList(metas));
					}
				});

				addTest(new TestCase("getList with results") {
					@Override
					public void test() throws Exception {
						List<MetaData> metas = lib.getList().getMetas();
						assertEquals(3, metas.size());
					}
				});

				addTest(new TestCase("getList by source") {
					@Override
					public void test() throws Exception {
						List<MetaData> metas = null;

						metas = lib.getList().filter(source1, null, null);
						assertEquals(1, metas.size());

						metas = lib.getList().filter(source2, null, null);
						assertEquals(2, metas.size());

						metas = lib.getList().filter((String)null, null, null);
						assertEquals(3, metas.size());
					}
				});

				addTest(new TestCase("getList by author") {
					@Override
					public void test() throws Exception {
						List<MetaData> metas = null;

						metas = lib.getList().filter(null, author1, null);
						assertEquals(2, metas.size());

						metas = lib.getList().filter(null, author2, null);
						assertEquals(1, metas.size());

						metas = lib.getList().filter((String)null, null, null);
						assertEquals(3, metas.size());
					}
				});

				addTest(new TestCase("changeType") {
					@Override
					public void test() throws Exception {
						List<MetaData> metas = null;

						lib.changeSource(luid3, source1, null);

						metas = lib.getList().filter(source1, null, null);
						assertEquals(2, metas.size());

						metas = lib.getList().filter(source2, null, null);
						assertEquals(1, metas.size());

						metas = lib.getList().filter((String)null, null, null);
						assertEquals(3, metas.size());
					}
				});

				addTest(new TestCase("save override luid (change source)") {
					@Override
					public void test() throws Exception {
						List<MetaData> metas = null;

						// same luid as a previous one
						lib.save(story(luid3, "My story 3", source2, author2),
								luid3, null);

						metas = lib.getList().filter(source1, null, null);
						assertEquals(1, metas.size());

						metas = lib.getList().filter(source2, null, null);
						assertEquals(2, metas.size());

						metas = lib.getList().filter((String)null, null, null);
						assertEquals(3, metas.size());
					}
				});
			}
		});
	}

	@Override
	protected void start() throws Exception {
		tmp = File.createTempFile(".test-fanfix", ".library");
		tmp.delete();
		tmp.mkdir();

		lib = new LocalLibrary(tmp, OutputType.INFO_TEXT, OutputType.CBZ);
	}

	@Override
	protected void stop() throws Exception {
		IOUtils.deltree(tmp);
	}

	/**
	 * Return the (sorted) list of titles present in this list of
	 * {@link MetaData}s.
	 * 
	 * @param metas
	 *            the meta
	 * 
	 * @return the sorted list
	 */
	private List<String> titlesAsList(List<MetaData> metas) {
		List<String> list = new ArrayList<String>();
		for (MetaData meta : metas) {
			list.add(meta.getTitle());
		}

		Collections.sort(list);
		return list;
	}

	private Story story(String luid, String title, String source, String author) {
		Story story = new Story();

		MetaData meta = new MetaData();
		meta.setLuid(luid);
		meta.setTitle(title);
		meta.setSource(source);
		meta.setAuthor(author);
		story.setMeta(meta);

		Chapter resume = chapter(0, "Resume");
		meta.setResume(resume);

		List<Chapter> chapters = new ArrayList<Chapter>();
		chapters.add(chapter(1, "Chap 1"));
		chapters.add(chapter(2, "Chap 2"));
		story.setChapters(chapters);

		long words = 0;
		for (Chapter chap : story.getChapters()) {
			words += chap.getWords();
		}
		meta.setWords(words);

		return story;
	}

	private Chapter chapter(int number, String name) {
		Chapter chapter = new Chapter(number, name);

		List<Paragraph> paragraphs = new ArrayList<Paragraph>();
		paragraphs.add(new Paragraph(Paragraph.ParagraphType.NORMAL,
				"some words in this paragraph please thank you", 8));

		chapter.setParagraphs(paragraphs);

		long words = 0;
		for (Paragraph para : chapter.getParagraphs()) {
			words += para.getWords();
		}
		chapter.setWords(words);

		return chapter;
	}
}
