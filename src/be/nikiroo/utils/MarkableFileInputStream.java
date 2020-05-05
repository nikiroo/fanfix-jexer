package be.nikiroo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Class was moved to {@link be.nikiroo.utils.streams.MarkableFileInputStream}.
 * 
 * @author niki
 */
@Deprecated
public class MarkableFileInputStream extends
		be.nikiroo.utils.streams.MarkableFileInputStream {
	public MarkableFileInputStream(File file) throws FileNotFoundException {
		super(file);
	}

	public MarkableFileInputStream(FileInputStream fis) {
		super(fis);
	}
}
