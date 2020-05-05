package be.nikiroo.utils.test_code;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.streams.ReplaceInputStream;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class ReplaceInputStreamTest extends TestLauncher {
	public ReplaceInputStreamTest(String[] args) {
		super("ReplaceInputStream test", args);

		addTest(new TestCase("Empty replace") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 127 };
				ReplaceInputStream in = new ReplaceInputStream(
						new ByteArrayInputStream(data), new byte[0],
						new byte[0]);

				checkArrays(this, "FIRST", in, data);
			}
		});

		addTest(new TestCase("Simple replace") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 127 };
				ReplaceInputStream in = new ReplaceInputStream(
						new ByteArrayInputStream(data), new byte[] { 0 },
						new byte[] { 10 });

				checkArrays(this, "FIRST", in, new byte[] { 42, 12, 10, 127 });
			}
		});

		addTest(new TestCase("3/4 replace") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 127 };
				ReplaceInputStream in = new ReplaceInputStream(
						new ByteArrayInputStream(data),
						new byte[] { 12, 0, 127 }, new byte[] { 10, 10, 10 });

				checkArrays(this, "FIRST", in, new byte[] { 42, 10, 10, 10 });
			}
		});

		addTest(new TestCase("Lnger replace") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 127 };
				ReplaceInputStream in = new ReplaceInputStream(
						new ByteArrayInputStream(data), new byte[] { 0 },
						new byte[] { 10, 10, 10 });

				checkArrays(this, "FIRST", in, new byte[] { 42, 12, 10, 10, 10,
						127 });
			}
		});

		addTest(new TestCase("Shorter replace") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 127 };
				ReplaceInputStream in = new ReplaceInputStream(
						new ByteArrayInputStream(data),
						new byte[] { 42, 12, 0 }, new byte[] { 10 });

				checkArrays(this, "FIRST", in, new byte[] { 10, 127 });
			}
		});

		addTest(new TestCase("String replace") {
			@Override
			public void test() throws Exception {
				byte[] data = "I like red".getBytes("UTF-8");
				ReplaceInputStream in = new ReplaceInputStream(
						new ByteArrayInputStream(data),
						"red".getBytes("UTF-8"), "blue".getBytes("UTF-8"));

				checkArrays(this, "FIRST", in, "I like blue".getBytes("UTF-8"));

				data = "I like blue".getBytes("UTF-8");
				in = new ReplaceInputStream(new ByteArrayInputStream(data),
						"blue".getBytes("UTF-8"), "red".getBytes("UTF-8"));

				checkArrays(this, "FIRST", in, "I like red".getBytes("UTF-8"));
			}
		});
	}

	static void checkArrays(TestCase test, String prefix, InputStream in,
			byte[] expected) throws Exception {
		byte[] actual = IOUtils.toByteArray(in);
		test.assertEquals("The " + prefix
				+ " resulting array has not the correct number of items",
				expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			test.assertEquals("Item " + i + " (0-based) is not the same",
					expected[i], actual[i]);
		}
	}
}
