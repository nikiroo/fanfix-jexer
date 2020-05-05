package be.nikiroo.fanfix.output;

import java.io.IOException;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.StringId;
import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.Paragraph.ParagraphType;

class InfoText extends Text {
	// quote chars
	private char openQuote = Instance.getInstance().getTrans().getCharacter(StringId.OPEN_SINGLE_QUOTE);
	private char closeQuote = Instance.getInstance().getTrans().getCharacter(StringId.CLOSE_SINGLE_QUOTE);
	private char openDoubleQuote = Instance.getInstance().getTrans().getCharacter(StringId.OPEN_DOUBLE_QUOTE);
	private char closeDoubleQuote = Instance.getInstance().getTrans().getCharacter(StringId.CLOSE_DOUBLE_QUOTE);

	@Override
	public String getDefaultExtension(boolean readerTarget) {
		return "";
	}

	@Override
	protected void writeChapterHeader(Chapter chap) throws IOException {
		writer.write("\n");

		if (chap.getName() != null && !chap.getName().isEmpty()) {
			writer.write(Instance.getInstance().getTrans().getString(StringId.CHAPTER_NAMED, chap.getNumber(),
					chap.getName()));
		} else {
			writer.write(Instance.getInstance().getTrans().getString(StringId.CHAPTER_UNNAMED, chap.getNumber()));
		}

		writer.write("\n\n");
	}

	@Override
	protected void writeTextLine(ParagraphType type, String line)
			throws IOException {
		switch (type) {
		case NORMAL:
		case QUOTE:
			StringBuilder builder = new StringBuilder();
			for (char car : line.toCharArray()) {
				if (car == '—') {
					builder.append("---");
				} else if (car == '–') {
					builder.append("--");
				} else if (car == openDoubleQuote) {
					builder.append("\"");
				} else if (car == closeDoubleQuote) {
					builder.append("\"");
				} else if (car == openQuote) {
					builder.append("'");
				} else if (car == closeQuote) {
					builder.append("'");
				} else {
					builder.append(car);
				}
			}

			line = builder.toString();
			break;
		default:
			break;
		}

		super.writeTextLine(type, line);
	}
}
