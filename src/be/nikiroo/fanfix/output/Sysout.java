package be.nikiroo.fanfix.output;

import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.fanfix.data.Story;

class Sysout extends BasicOutput {
	@Override
	protected void writeStoryHeader(Story story) {
		System.out.println(story);
	}

	@Override
	protected void writeChapterHeader(Chapter chap) {
		System.out.println(chap);
	}

	@Override
	protected void writeParagraphHeader(Paragraph para) {
		System.out.println(para);
	}
}
