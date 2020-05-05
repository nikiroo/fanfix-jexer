package be.nikiroo.fanfix.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.bundles.StringId;
import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.fanfix.data.Paragraph.ParagraphType;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.StringUtils;

class Epub extends BasicOutput {
	private File tmpDir;
	private BufferedWriter writer;
	private boolean inDialogue = false;
	private boolean inNormal = false;
	private File images;
	private boolean nextParaIsCover = true;

	@Override
	public File process(Story story, File targetDir, String targetName)
			throws IOException {
		String targetNameOrig = targetName;
		targetName += getDefaultExtension(false);

		tmpDir = Instance.getInstance().getTempFiles().createTempDir("fanfic-reader-epub");
		tmpDir.delete();

		if (!tmpDir.mkdir()) {
			throw new IOException(
					"Cannot create a temporary directory: no space left on device?");
		}

		super.process(story, targetDir, targetNameOrig);

		File epub = null;
		try {
			// "Originals"
			File data = new File(tmpDir, "DATA");
			data.mkdir();
			BasicOutput.getOutput(OutputType.TEXT, isWriteInfo(),
					isWriteCover()).process(story, data, targetNameOrig);
			InfoCover.writeInfo(data, targetNameOrig, story.getMeta());
			IOUtils.writeSmallFile(data, "version", "3.0");

			// zip/epub
			epub = new File(targetDir, targetName);
			IOUtils.zip(tmpDir, epub, true);

			OutputStream out = new FileOutputStream(epub);
			try {
				ZipOutputStream zip = new ZipOutputStream(out);
				try {
					// "mimetype" MUST be the first element and not compressed
					zip.setLevel(ZipOutputStream.STORED);
					File mimetype = new File(tmpDir, "mimetype");
					IOUtils.writeSmallFile(tmpDir, "mimetype",
							"application/epub+zip");
					ZipEntry entry = new ZipEntry("mimetype");
					entry.setExtra(new byte[] {});
					zip.putNextEntry(entry);
					FileInputStream in = new FileInputStream(mimetype);
					try {
						IOUtils.write(in, zip);
					} finally {
						in.close();
					}
					IOUtils.deltree(mimetype);
					zip.setLevel(ZipOutputStream.DEFLATED);
					//

					IOUtils.zip(zip, "", tmpDir, true);
				} finally {
					zip.close();
				}
			} finally {
				out.close();
			}
		} finally {
			IOUtils.deltree(tmpDir);
			tmpDir = null;
		}

		return epub;
	}

	@Override
	public String getDefaultExtension(boolean readerTarget) {
		return ".epub";
	}

	@Override
	protected void writeStoryHeader(Story story) throws IOException {
		File ops = new File(tmpDir, "OPS");
		ops.mkdirs();
		File css = new File(ops, "css");
		css.mkdirs();
		images = new File(ops, "images");
		images.mkdirs();
		File metaInf = new File(tmpDir, "META-INF");
		metaInf.mkdirs();

		// META-INF
		String containerContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<container xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\" version=\"1.0\">\n"
				+ "\t<rootfiles>\n"
				+ "\t\t<rootfile full-path=\"OPS/epb.opf\" media-type=\"application/oebps-package+xml\"/>\n"
				+ "\t</rootfiles>\n" + "</container>\n";

		IOUtils.writeSmallFile(metaInf, "container.xml", containerContent);

		// OPS/css
		InputStream inStyle = getClass().getResourceAsStream("epub.style.css");
		if (inStyle == null) {
			throw new IOException("Cannot find style.css resource");
		}
		try {
			IOUtils.write(inStyle, new File(css, "style.css"));
		} finally {
			inStyle.close();
		}

		// OPS/images
		if (story.getMeta() != null && story.getMeta().getCover() != null) {
			File file = new File(images, "cover");
			try {
				Instance.getInstance().getCache().saveAsImage(story.getMeta().getCover(), file, true);
			} catch (Exception e) {
				Instance.getInstance().getTraceHandler().error(e);
			}
		}

		// OPS/* except chapters
		IOUtils.writeSmallFile(ops, "epb.ncx", generateNcx(story));
		IOUtils.writeSmallFile(ops, "epb.opf", generateOpf(story));
		IOUtils.writeSmallFile(ops, "title.xhtml", generateTitleXml(story));

		// Resume
		if (story.getMeta() != null && story.getMeta().getResume() != null) {
			writeChapter(story.getMeta().getResume());
		}
	}

	@Override
	protected void writeChapterHeader(Chapter chap) throws IOException {
		String filename = String.format("%s%03d%s", "chapter-",
				chap.getNumber(), ".xhtml");
		writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(tmpDir + File.separator + "OPS",
						filename)), "UTF-8"));
		inDialogue = false;
		inNormal = false;
		try {
			String title = "Chapter " + chap.getNumber();
			String nameOrNum = Integer.toString(chap.getNumber());
			if (chap.getName() != null && !chap.getName().isEmpty()) {
				title += ": " + chap.getName();
				nameOrNum = chap.getName();
			}

			writer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			writer.append("\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
			writer.append("\n<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			writer.write("\n<head>");
			writer.write("\n	<title>" + StringUtils.xmlEscape(title)
					+ "</title>");
			writer.write("\n	<link rel='stylesheet' href='css/style.css' type='text/css'/>");
			writer.write("\n</head>");
			writer.write("\n<body>");
			writer.write("\n	<h2>");
			writer.write("\n		<span class='chap'>Chapter <span class='chapnumber'>"
					+ chap.getNumber() + "</span>:</span> ");
			writer.write("\n		<span class='chaptitle'>"
					+ StringUtils.xmlEscape(nameOrNum) + "</span>");
			writer.write("\n	</h2>");
			writer.write("\n	");
			writer.write("\n	<div class='chapter_content'>\n");
		} catch (Exception e) {
			writer.close();
			throw new IOException(e);
		}
	}

	@Override
	protected void writeChapterFooter(Chapter chap) throws IOException {
		try {
			if (inDialogue) {
				writer.write("		</div>\n");
				inDialogue = false;
			}
			if (inNormal) {
				writer.write("		</div>\n");
				inNormal = false;
			}
			writer.write("	</div>\n</body>\n</html>\n");
		} finally {
			writer.close();
			writer = null;
		}
	}

	@Override
	protected void writeParagraphHeader(Paragraph para) throws IOException {
		if (para.getType() == ParagraphType.QUOTE && !inDialogue) {
			writer.write("		<div class='dialogues'>\n");
			inDialogue = true;
		} else if (para.getType() != ParagraphType.QUOTE && inDialogue) {
			writer.write("		</div>\n");
			inDialogue = false;
		}

		if (para.getType() == ParagraphType.NORMAL && !inNormal) {
			writer.write("		<div class='normals'>\n");
			inNormal = true;
		} else if (para.getType() != ParagraphType.NORMAL && inNormal) {
			writer.write("		</div>\n");
			inNormal = false;
		}

		switch (para.getType()) {
		case BLANK:
			writer.write("		<div class='blank'></div>");
			break;
		case BREAK:
			writer.write("		<hr class='break'/>");
			break;
		case NORMAL:
			writer.write("		<span class='normal'>");
			break;
		case QUOTE:
			writer.write("			<div class='dialogue'>&mdash; ");
			break;
		case IMAGE:
			File file = new File(images, getCurrentImageBestName(false));
			Instance.getInstance().getCache().saveAsImage(para.getContentImage(), file, nextParaIsCover);
			writer.write("			<img alt='page image' class='page-image' src='images/"
					+ getCurrentImageBestName(false) + "'/>");
			break;
		}

		nextParaIsCover = false;
	}

	@Override
	protected void writeParagraphFooter(Paragraph para) throws IOException {
		switch (para.getType()) {
		case NORMAL:
			writer.write("</span>\n");
			break;
		case QUOTE:
			writer.write("</div>\n");
			break;
		default:
			writer.write("\n");
			break;
		}
	}

	@Override
	protected void writeTextLine(ParagraphType type, String line)
			throws IOException {
		switch (type) {
		case QUOTE:
		case NORMAL:
			writer.write(decorateText(StringUtils.xmlEscape(line)));
			break;
		default:
			break;
		}
	}

	@Override
	protected String enbold(String word) {
		return "<strong>" + word + "</strong>";
	}

	@Override
	protected String italize(String word) {
		return "<emph>" + word + "</emph>";
	}

	private String generateNcx(Story story) {
		StringBuilder builder = new StringBuilder();

		String title = "";
		String uuid = "";
		String author = "";
		if (story.getMeta() != null) {
			MetaData meta = story.getMeta();
			uuid = meta.getUuid();
			author = meta.getAuthor();
			title = meta.getTitle();
		}

		builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		builder.append("\n<!DOCTYPE ncx");
		builder.append("\nPUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">");
		builder.append("\n<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">");
		builder.append("\n	<head>");
		builder.append("\n		<!--The following four metadata items are required for all");
		builder.append("\n		    NCX documents, including those conforming to the relaxed");
		builder.append("\n		    constraints of OPS 2.0-->");
		builder.append("\n		<meta name=\"dtb:uid\" content=\""
				+ StringUtils.xmlEscapeQuote(uuid) + "\"/>");
		builder.append("\n		<meta name=\"dtb:depth\" content=\"1\"/>");
		builder.append("\n		<meta name=\"dtb:totalPageCount\" content=\"0\"/>");
		builder.append("\n		<meta name=\"dtb:maxPageNumber\" content=\"0\"/>");
		builder.append("\n		<meta name=\"epub-creator\" content=\""
				+ StringUtils.xmlEscapeQuote(EPUB_CREATOR) + "\"/>");
		builder.append("\n	</head>");
		builder.append("\n	<docTitle>");
		builder.append("\n		<text>" + StringUtils.xmlEscape(title) + "</text>");
		builder.append("\n	</docTitle>");
		builder.append("\n	<docAuthor>");

		builder.append("\n		<text>" + StringUtils.xmlEscape(author) + "</text>");
		builder.append("\n	</docAuthor>");
		builder.append("\n	<navMap>");
		builder.append("\n		<navPoint id=\"navpoint-1\" playOrder=\"1\">");
		builder.append("\n			<navLabel>");
		builder.append("\n				<text>Title Page</text>");
		builder.append("\n			</navLabel>");
		builder.append("\n			<content src=\"title.xhtml\"/>");
		builder.append("\n		</navPoint>");

		int navPoint = 2; // 1 is above

		if (story.getMeta() != null & story.getMeta().getResume() != null) {
			Chapter chap = story.getMeta().getResume();
			generateNcx(chap, builder, navPoint++);
		}

		for (Chapter chap : story) {
			generateNcx(chap, builder, navPoint++);
		}

		builder.append("\n	</navMap>");
		builder.append("\n</ncx>\n");

		return builder.toString();
	}

	private void generateNcx(Chapter chap, StringBuilder builder, int navPoint) {
		String name;
		if (chap.getName() != null && !chap.getName().isEmpty()) {
			name = Instance.getInstance().getTrans().getString(StringId.CHAPTER_NAMED, chap.getNumber(),
					chap.getName());
		} else {
			name = Instance.getInstance().getTrans().getString(StringId.CHAPTER_UNNAMED, chap.getNumber());
		}

		String nnn = String.format("%03d", (navPoint - 2));

		builder.append("\n		<navPoint id=\"navpoint-" + navPoint
				+ "\" playOrder=\"" + navPoint + "\">");
		builder.append("\n			<navLabel>");
		builder.append("\n				<text>" + name + "</text>");
		builder.append("\n			</navLabel>");
		builder.append("\n			<content src=\"chapter-" + nnn + ".xhtml\"/>");
		builder.append("\n		</navPoint>\n");
	}

	private String generateOpf(Story story) {
		StringBuilder builder = new StringBuilder();

		String title = "";
		String uuid = "";
		String author = "";
		String date = "";
		String publisher = "";
		String subject = "";
		String source = "";
		String lang = "";
		if (story.getMeta() != null) {
			MetaData meta = story.getMeta();
			title = meta.getTitle();
			uuid = meta.getUuid();
			author = meta.getAuthor();
			date = meta.getDate();
			publisher = meta.getPublisher();
			subject = meta.getSubject();
			source = meta.getSource();
			lang = meta.getLang();
		}

		builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		builder.append("\n<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"BookId\" version=\"2.0\">");
		builder.append("\n   <metadata xmlns:opf=\"http://www.idpf.org/2007/opf\"");
		builder.append("\n             xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
		builder.append("\n      <dc:title>" + StringUtils.xmlEscape(title)
				+ "</dc:title>");
		builder.append("\n      <dc:creator opf:role=\"aut\" opf:file-as=\""
				+ StringUtils.xmlEscapeQuote(author) + "\">"
				+ StringUtils.xmlEscape(author) + "</dc:creator>");
		builder.append("\n      <dc:date opf:event=\"original-publication\">"
				+ StringUtils.xmlEscape(date) + "</dc:date>");
		builder.append("\n      <dc:publisher>"
				+ StringUtils.xmlEscape(publisher) + "</dc:publisher>");
		builder.append("\n      <dc:date opf:event=\"epub-publication\"></dc:date>");
		builder.append("\n      <dc:subject>" + StringUtils.xmlEscape(subject)
				+ "</dc:subject>");
		builder.append("\n      <dc:source>" + StringUtils.xmlEscape(source)
				+ "</dc:source>");
		builder.append("\n      <dc:rights>Not for commercial use.</dc:rights>");
		builder.append("\n      <dc:identifier id=\"BookId\" opf:scheme=\"URI\">"
				+ StringUtils.xmlEscape(uuid) + "</dc:identifier>");
		builder.append("\n      <dc:language>" + StringUtils.xmlEscape(lang)
				+ "</dc:language>");
		builder.append("\n   </metadata>");
		builder.append("\n   <manifest>");
		builder.append("\n      <!-- Content Documents -->");
		builder.append("\n      <item id=\"titlepage\" href=\"title.xhtml\" media-type=\"application/xhtml+xml\"/>");
		for (int i = 0; i <= story.getChapters().size(); i++) {
			String name = String.format("%s%03d", "chapter-", i);
			builder.append("\n      <item id=\""
					+ StringUtils.xmlEscapeQuote(name) + "\" href=\""
					+ StringUtils.xmlEscapeQuote(name)
					+ ".xhtml\" media-type=\"application/xhtml+xml\"/>");
		}

		builder.append("\n      <!-- CSS Style Sheets -->");
		builder.append("\n      <item id=\"style-css\" href=\"css/style.css\" media-type=\"text/css\"/>");

		builder.append("\n      <!-- Images -->");

		if (story.getMeta() != null && story.getMeta().getCover() != null) {
			String format = Instance.getInstance().getConfig()
					.getString(Config.FILE_FORMAT_IMAGE_FORMAT_COVER)
					.toLowerCase();
			builder.append("\n      <item id=\"cover\" href=\"images/cover."
					+ format + "\" media-type=\"image/png\"/>");
		}

		builder.append("\n      <!-- NCX -->");
		builder.append("\n      <item id=\"ncx\" href=\"epb.ncx\" media-type=\"application/x-dtbncx+xml\"/>");
		builder.append("\n   </manifest>");
		builder.append("\n   <spine toc=\"ncx\">");
		builder.append("\n      <itemref idref=\"titlepage\" linear=\"yes\"/>");
		for (int i = 0; i <= story.getChapters().size(); i++) {
			String name = String.format("%s%03d", "chapter-", i);
			builder.append("\n      <itemref idref=\""
					+ StringUtils.xmlEscapeQuote(name) + "\" linear=\"yes\"/>");
		}
		builder.append("\n   </spine>");
		builder.append("\n</package>\n");

		return builder.toString();
	}

	private String generateTitleXml(Story story) {
		StringBuilder builder = new StringBuilder();

		String title = "";
		String tags = "";
		String author = "";
		if (story.getMeta() != null) {
			MetaData meta = story.getMeta();
			title = meta.getTitle();
			if (meta.getTags() != null) {
				for (String tag : meta.getTags()) {
					if (!tags.isEmpty()) {
						tags += ", ";
					}
					tags += tag;
				}

				if (!tags.isEmpty()) {
					tags = "(" + tags + ")";
				}
			}
			author = meta.getAuthor();
		}

		String format = Instance.getInstance().getConfig()
				.getString(Config.FILE_FORMAT_IMAGE_FORMAT_COVER).toLowerCase();

		builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		builder.append("\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
		builder.append("\n<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		builder.append("\n<head>");
		builder.append("\n	<title>" + StringUtils.xmlEscape(title) + "</title>");
		builder.append("\n	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\"/>");
		builder.append("\n</head>");
		builder.append("\n<body>");
		builder.append("\n	<div class=\"titlepage\">");
		builder.append("\n		<h1>" + StringUtils.xmlEscape(title) + "</h1>");
		builder.append("\n			<div class=\"type\">"
				+ StringUtils.xmlEscape(tags) + "</div>");
		builder.append("\n		<div class=\"cover\">");
		builder.append("\n			<img alt=\"cover image\" src=\"images/cover."
				+ format + "\"></img>");
		builder.append("\n		</div>");
		builder.append("\n		<div class=\"author\">"
				+ StringUtils.xmlEscape(author) + "</div>");
		builder.append("\n	</div>");
		builder.append("\n</body>");
		builder.append("\n</html>\n");

		return builder.toString();
	}
}
