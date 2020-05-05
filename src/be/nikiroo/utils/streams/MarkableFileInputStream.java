package be.nikiroo.utils.streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * This is a markable (and thus reset-able) stream that you can create from a
 * FileInputStream.
 * 
 * @author niki
 */
public class MarkableFileInputStream extends FilterInputStream {
	private FileChannel channel;
	private long mark = 0;

	/**
	 * Create a new {@link MarkableFileInputStream} from this file.
	 * 
	 * @param file
	 *            the {@link File} to wrap
	 * 
	 * @throws FileNotFoundException
	 *             if the {@link File} cannot be found
	 */
	public MarkableFileInputStream(File file) throws FileNotFoundException {
		this(new FileInputStream(file));
	}

	/**
	 * Create a new {@link MarkableFileInputStream} from this stream.
	 * 
	 * @param in
	 *            the original {@link FileInputStream} to wrap
	 */
	public MarkableFileInputStream(FileInputStream in) {
		super(in);
		channel = in.getChannel();
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public synchronized void mark(int readlimit) {
		try {
			mark = channel.position();
		} catch (IOException ex) {
			ex.printStackTrace();
			mark = -1;
		}
	}

	@Override
	public synchronized void reset() throws IOException {
		if (mark < 0) {
			throw new IOException("mark position not valid");
		}
		channel.position(mark);
	}
}