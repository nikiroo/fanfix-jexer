package be.nikiroo.fanfix.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.StringId;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.fanfix.data.Paragraph.ParagraphType;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.fanfix.supported.BasicSupport;
import be.nikiroo.fanfix.supported.BasicSupportHelper;
import be.nikiroo.fanfix.supported.BasicSupportImages;
import be.nikiroo.fanfix.supported.BasicSupportPara;
import be.nikiroo.fanfix.supported.SupportType;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class BasicSupportUtilitiesTest extends TestLauncher {
	// quote chars
	private char openQuote = Instance.getInstance().getTrans().getCharacter(StringId.OPEN_SINGLE_QUOTE);
	private char closeQuote = Instance.getInstance().getTrans().getCharacter(StringId.CLOSE_SINGLE_QUOTE);
	private char openDoubleQuote = Instance.getInstance().getTrans().getCharacter(StringId.OPEN_DOUBLE_QUOTE);
	private char closeDoubleQuote = Instance.getInstance().getTrans().getCharacter(StringId.CLOSE_DOUBLE_QUOTE);
	
	public BasicSupportUtilitiesTest(String[] args) {
		super("BasicSupportUtilities", args);
		
		addSeries(new TestLauncher("General", args) {
			{
				addTest(new TestCase("BasicSupport.makeParagraphs()") {
					@Override
					public void test() throws Exception {
						BasicSupportParaPublic bsPara = new BasicSupportParaPublic() {
							@Override
							public void fixBlanksBreaks(List<Paragraph> paras) {
							}

							@Override
							public List<Paragraph> requotify(Paragraph para, boolean html) {
								List<Paragraph> paras = new ArrayList<Paragraph>(
										1);
								paras.add(para);
								return paras;
							}
						};

						List<Paragraph> paras = null;

						paras = bsPara.makeParagraphs(null, null, "", true, null);
						assertEquals(
								"An empty content should not generate paragraphs",
								0, paras.size());

						paras = bsPara.makeParagraphs(null, null,
								"Line 1</p><p>Line 2</p><p>Line 3</p>", true, null);
						assertEquals(5, paras.size());
						assertEquals("Line 1", paras.get(0).getContent());
						assertEquals(ParagraphType.BLANK, paras.get(1)
								.getType());
						assertEquals("Line 2", paras.get(2).getContent());
						assertEquals(ParagraphType.BLANK, paras.get(3)
								.getType());
						assertEquals("Line 3", paras.get(4).getContent());

						paras = bsPara.makeParagraphs(null, null,
								"<p>Line1</p><p>Line2</p><p>Line3</p>", true, null);
						assertEquals(6, paras.size());
					}
				});

				addTest(new TestCase("BasicSupport.removeDoubleBlanks()") {
					@Override
					public void test() throws Exception {
						BasicSupportParaPublic support = new BasicSupportParaPublic();

						List<Paragraph> paras = null;

						paras = support
								.makeParagraphs(
										null,
										null,
										"<p>Line1</p><p>Line2</p><p>Line3<br/><br><p></p>",
										true,
										null);
						assertEquals(5, paras.size());

						paras = support
								.makeParagraphs(
										null,
										null,
										"<p>Line1</p><p>Line2</p><p>Line3<br/><br><p></p>* * *",
										true,
										null);
						assertEquals(5, paras.size());

						paras = support.makeParagraphs(null, null, "1<p>* * *<p>2",
								true, null);
						assertEquals(3, paras.size());
						assertEquals(ParagraphType.BREAK, paras.get(1)
								.getType());

						paras = support.makeParagraphs(null, null,
								"1<p><br/><p>* * *<p>2", true, null);
						assertEquals(3, paras.size());
						assertEquals(ParagraphType.BREAK, paras.get(1)
								.getType());

						paras = support.makeParagraphs(null, null,
								"1<p>* * *<br/><p><br><p>2", true, null);
						assertEquals(3, paras.size());
						assertEquals(ParagraphType.BREAK, paras.get(1)
								.getType());

						paras = support.makeParagraphs(null, null,
								"1<p><br/><br>* * *<br/><p><br><p>2", true, null);
						assertEquals(3, paras.size());
						assertEquals(ParagraphType.BREAK, paras.get(1)
								.getType());
					}
				});

				addTest(new TestCase("BasicSupport.processPara() quotes") {
					@Override
					public void test() throws Exception {
						BasicSupportParaPublic support = new BasicSupportParaPublic();

						Paragraph para;

						// sanity check
						para = support.processPara("", true);
						assertEquals(ParagraphType.BLANK, para.getType());
						//

						para = support.processPara("\"Yes, my Lord!\"", true);
						assertEquals(ParagraphType.QUOTE, para.getType());
						assertEquals(openDoubleQuote + "Yes, my Lord!"
								+ closeDoubleQuote, para.getContent());

						para = support.processPara("«Yes, my Lord!»", true);
						assertEquals(ParagraphType.QUOTE, para.getType());
						assertEquals(openDoubleQuote + "Yes, my Lord!"
								+ closeDoubleQuote, para.getContent());

						para = support.processPara("'Yes, my Lord!'", true);
						assertEquals(ParagraphType.QUOTE, para.getType());
						assertEquals(openQuote + "Yes, my Lord!" + closeQuote,
								para.getContent());

						para = support.processPara("‹Yes, my Lord!›", true);
						assertEquals(ParagraphType.QUOTE, para.getType());
						assertEquals(openQuote + "Yes, my Lord!" + closeQuote,
								para.getContent());
					}
				});

				addTest(new TestCase(
						"BasicSupport.processPara() double-simple quotes") {
					@Override
					public void setUp() throws Exception {
						super.setUp();

					}

					@Override
					public void tearDown() throws Exception {

						super.tearDown();
					}

					@Override
					public void test() throws Exception {
						BasicSupportParaPublic support = new BasicSupportParaPublic();

						Paragraph para;

						para = support.processPara("''Yes, my Lord!''", true);
						assertEquals(ParagraphType.QUOTE, para.getType());
						assertEquals(openDoubleQuote + "Yes, my Lord!"
								+ closeDoubleQuote, para.getContent());

						para = support.processPara("‹‹Yes, my Lord!››", true);
						assertEquals(ParagraphType.QUOTE, para.getType());
						assertEquals(openDoubleQuote + "Yes, my Lord!"
								+ closeDoubleQuote, para.getContent());
					}
				});

				addTest(new TestCase("BasicSupport.processPara() apostrophe") {
					@Override
					public void test() throws Exception {
						BasicSupportParaPublic support = new BasicSupportParaPublic();

						Paragraph para;

						String text = "Nous étions en été, mais cela aurait être l'hiver quand nous n'étions encore qu'à Aubeuge";
						para = support.processPara(text, true);
						assertEquals(ParagraphType.NORMAL, para.getType());
						assertEquals(text, para.getContent());
					}
				});

				addTest(new TestCase("BasicSupport.processPara() words count") {
					@Override
					public void test() throws Exception {
						BasicSupportParaPublic support = new BasicSupportParaPublic();

						Paragraph para;

						para = support.processPara("«Yes, my Lord!»", true);
						assertEquals(3, para.getWords());

						para = support.processPara("One, twee, trois.", true);
						assertEquals(3, para.getWords());
					}
				});

				addTest(new TestCase("BasicSupport.requotify() words count") {
					@Override
					public void test() throws Exception {
						BasicSupportParaPublic support = new BasicSupportParaPublic();

						char openDoubleQuote = Instance.getInstance().getTrans()
								.getCharacter(StringId.OPEN_DOUBLE_QUOTE);
						char closeDoubleQuote = Instance.getInstance().getTrans()
								.getCharacter(StringId.CLOSE_DOUBLE_QUOTE);

						String content = null;
						Paragraph para = null;
						List<Paragraph> paras = null;
						long words = 0;

						content = "One, twee, trois.";
						para = new Paragraph(ParagraphType.NORMAL, content,
								content.split(" ").length);
						paras = support.requotify(para, false);
						words = 0;
						for (Paragraph p : paras) {
							words += p.getWords();
						}
						assertEquals("Bad words count in a single paragraph",
								para.getWords(), words);

						content = "Such WoW! So Web2.0! With Colours!";
						para = new Paragraph(ParagraphType.NORMAL, content,
								content.split(" ").length);
						paras = support.requotify(para, false);
						words = 0;
						for (Paragraph p : paras) {
							words += p.getWords();
						}
						assertEquals("Bad words count in a single paragraph",
								para.getWords(), words);

						content = openDoubleQuote + "Such a good idea!"
								+ closeDoubleQuote
								+ ", she said. This ought to be a new para.";
						para = new Paragraph(ParagraphType.QUOTE, content,
								content.split(" ").length);
						paras = support.requotify(para, false);
						words = 0;
						for (Paragraph p : paras) {
							words += p.getWords();
						}
						assertEquals(
								"Bad words count in a requotified paragraph",
								para.getWords(), words);
					}
				});
			}
		});

		addSeries(new TestLauncher("Text", args) {
			{
				addTest(new TestCase("Chapter detection simple") {
					private File tmp;

					@Override
					public void setUp() throws Exception {
						tmp = File.createTempFile("fanfix-text-file_", ".test");
						IOUtils.writeSmallFile(tmp.getParentFile(),
								tmp.getName(), "TITLE"
										+ "\n"//
										+ "By nona"
										+ "\n" //
										+ "\n" //
										+ "Chapter 0: Resumé" + "\n" + "\n"
										+ "'sume." + "\n" + "\n"
										+ "Chapter 1: chap1" + "\n" + "\n"
										+ "Fanfan." + "\n" + "\n"
										+ "Chapter 2: Chap2" + "\n" + "\n" //
										+ "Tulipe." + "\n");
					}

					@Override
					public void tearDown() throws Exception {
						tmp.delete();
					}

					@Override
					public void test() throws Exception {
						BasicSupport support = BasicSupport.getSupport(
								SupportType.TEXT, tmp.toURI().toURL());

						Story story = support.process(null);

						assertEquals(2, story.getChapters().size());
						assertEquals(1, story.getChapters().get(1)
								.getParagraphs().size());
						assertEquals("Tulipe.", story.getChapters().get(1)
								.getParagraphs().get(0).getContent());
					}
				});

				addTest(new TestCase("Chapter detection with String 'Chapter'") {
					private File tmp;

					@Override
					public void setUp() throws Exception {
						tmp = File.createTempFile("fanfix-text-file_", ".test");
						IOUtils.writeSmallFile(tmp.getParentFile(),
								tmp.getName(), "TITLE"
										+ "\n"//
										+ "By nona"
										+ "\n" //
										+ "\n" //
										+ "Chapter 0: Resumé" + "\n" + "\n"
										+ "'sume." + "\n" + "\n"
										+ "Chapter 1: chap1" + "\n" + "\n"
										+ "Chapter fout-la-merde" + "\n"
										+ "\n"//
										+ "Fanfan." + "\n" + "\n"
										+ "Chapter 2: Chap2" + "\n" + "\n" //
										+ "Tulipe." + "\n");
					}

					@Override
					public void tearDown() throws Exception {
						tmp.delete();
					}

					@Override
					public void test() throws Exception {
						BasicSupport support = BasicSupport.getSupport(
								SupportType.TEXT, tmp.toURI().toURL());

						Story story = support.process(null);

						assertEquals(2, story.getChapters().size());
						assertEquals(1, story.getChapters().get(1)
								.getParagraphs().size());
						assertEquals("Tulipe.", story.getChapters().get(1)
								.getParagraphs().get(0).getContent());
					}
				});
			}
		});
	}
	
	class BasicSupportParaPublic extends BasicSupportPara {
		public BasicSupportParaPublic() {
			super(new BasicSupportHelper(), new BasicSupportImages());
		}
		
		@Override
		// and make it public!
		public Paragraph makeParagraph(BasicSupport support, URL source,
				String line, boolean html) {
			return super.makeParagraph(support, source, line, html);
		}
		
		@Override
		// and make it public!
		public List<Paragraph> makeParagraphs(BasicSupport support,
				URL source, String content, boolean html, Progress pg)
				throws IOException {
			return super.makeParagraphs(support, source, content, html, pg);
		}
		
		@Override
		// and make it public!
		public Paragraph processPara(String line, boolean html) {
			return super.processPara(line, html);
		}
		
		@Override
		// and make it public!
		public List<Paragraph> requotify(Paragraph para, boolean html) {
			return super.requotify(para, html);
		}
	}
}
