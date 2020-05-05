package be.nikiroo.fanfix.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.fanfix.data.Paragraph.ParagraphType;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.StringUtils;

class Html extends BasicOutput {
	private File dir;
	protected BufferedWriter writer;
	private boolean inDialogue = false;
	private boolean inNormal = false;

	@Override
	public File process(Story story, File targetDir, String targetName)
			throws IOException {
		String targetNameOrig = targetName;

		File target = new File(targetDir, targetName);
		target.mkdir();
		dir = target;

		target = new File(targetDir, targetName + getDefaultExtension(true));

		writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(target), "UTF-8"));
		try {
			super.process(story, targetDir, targetNameOrig);
		} finally {
			writer.close();
			writer = null;
		}

		// write a copy of the originals inside
		InfoCover.writeInfo(dir, targetName, story.getMeta());
		InfoCover.writeCover(dir, targetName, story.getMeta());
		BasicOutput.getOutput(OutputType.TEXT, isWriteInfo(), isWriteCover())
				.process(story, dir, targetNameOrig);

		if (story.getMeta().getCover() != null) {
			Instance.getInstance().getCache().saveAsImage(story.getMeta().getCover(), new File(dir, "cover"), true);
		}

		return target;
	}

	@Override
	public String getDefaultExtension(boolean readerTarget) {
		if (readerTarget) {
			return File.separator + "index.html";
		}

		return "";
	}

	@Override
	protected void writeStoryHeader(Story story) throws IOException {
		String title = "";
		String tags = "";
		String author = "";
		Chapter resume = null;
		if (story.getMeta() != null) {
			MetaData meta = story.getMeta();
			title = meta.getTitle();
			resume = meta.getResume();
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

		InputStream inStyle = getClass().getResourceAsStream("html.style.css");
		if (inStyle == null) {
			throw new IOException("Cannot find style.css resource");
		}
		try {
			IOUtils.write(inStyle, new File(dir, "style.css"));
		} finally {
			inStyle.close();
		}

		writer.write("<!DOCTYPE html>");
		writer.write("\n<html>");
		writer.write("\n<head>");
		writer.write("\n	<meta http-equiv='content-type' content='text/html; charset=utf-8'>");
		writer.write("\n	<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
		writer.write("\n	<link rel='stylesheet' type='text/css' href='style.css'>");
		writer.write("\n	<title>" + StringUtils.xmlEscape(title) + "</title>");
		writer.write("\n</head>");
		writer.write("\n<body>\n");

		writer.write("\n	<div class=\"titlepage\">");
		writer.write("\n		<h1>" + StringUtils.xmlEscape(title) + "</h1>");
		writer.write("\n			<div class=\"type\">" + StringUtils.xmlEscape(tags)
				+ "</div>");
		writer.write("\n		<div class=\"cover\">");
		writer.write("\n			<img src=\"cover." + format + "\"></img>");
		writer.write("\n		</div>");
		writer.write("\n		<div class=\"author\">"
				+ StringUtils.xmlEscape(author) + "</div>");
		writer.write("\n	</div>");

		writer.write("\n	<hr/><br/>");

		if (resume != null) {
			for (Paragraph para : resume) {
				writeParagraph(para);
			}
			if (inDialogue) {
				writer.write("		</div>\n");
				inDialogue = false;
			}
			if (inNormal) {
				writer.write("		</div>\n");
				inNormal = false;
			}
		}

		writer.write("\n	<br/>");
	}

	@Override
	protected void writeStoryFooter(Story story) throws IOException {
		writer.write("</body>\n");
	}

	@Override
	protected void writeChapterHeader(Chapter chap) throws IOException {
		String nameOrNumber;
		if (chap.getName() != null && !chap.getName().isEmpty()) {
			nameOrNumber = chap.getName();
		} else {
			nameOrNumber = Integer.toString(chap.getNumber());
		}

		writer.write("\n	<h2>");
		writer.write("\n		<span class='chap'>Chapter <span class='chapnumber'>"
				+ chap.getNumber() + "</span>:</span> ");
		writer.write("\n		<span class='chaptitle'>"
				+ StringUtils.xmlEscape(nameOrNumber) + "</span>");
		writer.write("\n	</h2>");
		writer.write("\n	");
		writer.write("\n	<div class='chapter_content'>\n");

		inDialogue = false;
		inNormal = false;
	}

	@Override
	protected void writeChapterFooter(Chapter chap) throws IOException {
		if (inDialogue) {
			writer.write("		</div>\n");
			inDialogue = false;
		}
		if (inNormal) {
			writer.write("		</div>\n");
			inNormal = false;
		}

		writer.write("\n	</div>");
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
			// TODO check if images work OK
			writer.write("<a href='"
					+ StringUtils.xmlEscapeQuote(para.getContent()) + "'>"
					+ StringUtils.xmlEscape(para.getContent()) + "</a>");
			break;
		}
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
}
