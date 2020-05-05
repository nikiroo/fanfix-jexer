package be.nikiroo.utils.test_code;

import java.io.ByteArrayOutputStream;

import be.nikiroo.utils.streams.ReplaceOutputStream;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class ReplaceOutputStreamTest extends TestLauncher {
	public ReplaceOutputStreamTest(String[] args) {
		super("ReplaceOutputStream test", args);

		addTest(new TestCase("Single write, empty bytes replaces") {
			@Override
			public void test() throws Exception {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ReplaceOutputStream out = new ReplaceOutputStream(bout,
						new byte[0], new byte[0]);

				byte[] data = new byte[] { 42, 12, 0, 127 };

				out.write(data);
				out.close();

				checkArrays(this, "FIRST", bout, data);
			}
		});

		addTest(new TestCase("Multiple writes, empty Strings replaces") {
			@Override
			public void test() throws Exception {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ReplaceOutputStream out = new ReplaceOutputStream(bout, "", "");

				byte[] data1 = new byte[] { 42, 12, 0, 127 };
				byte[] data2 = new byte[] { 15, 55 };
				byte[] data3 = new byte[] {};

				byte[] dataAll = new byte[] { 42, 12, 0, 127, 15, 55 };

				out.write(data1);
				out.write(data2);
				out.write(data3);
				out.close();

				checkArrays(this, "FIRST", bout, dataAll);
			}
		});

		addTest(new TestCase("Single write, bytes replaces") {
			@Override
			public void test() throws Exception {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ReplaceOutputStream out = new ReplaceOutputStream(bout,
						new byte[] { 12 }, new byte[] { 55 });

				byte[] data = new byte[] { 42, 12, 0, 127 };

				out.write(data);
				out.close();

				checkArrays(this, "FIRST", bout, new byte[] { 42, 55, 0, 127 });
			}
		});

		addTest(new TestCase("Multiple writes, Strings replaces") {
			@Override
			public void test() throws Exception {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ReplaceOutputStream out = new ReplaceOutputStream(bout, "(-)",
						"(.)");

				byte[] data1 = "un mot ".getBytes("UTF-8");
				byte[] data2 = "(-) of twee ".getBytes("UTF-8");
				byte[] data3 = "(-) makes the difference".getBytes("UTF-8");

				out.write(data1);
				out.write(data2);
				out.write(data3);
				out.close();

				checkArrays(this, "FIRST", bout,
						"un mot (.) of twee (.) makes the difference"
								.getBytes("UTF-8"));
			}
		});

		addTest(new TestCase("Single write, longer bytes replaces") {
			@Override
			public void test() throws Exception {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ReplaceOutputStream out = new ReplaceOutputStream(bout,
						new byte[] { 12 }, new byte[] { 55, 55, 66 });

				byte[] data = new byte[] { 42, 12, 0, 127 };

				out.write(data);
				out.close();

				checkArrays(this, "FIRST", bout, new byte[] { 42, 55, 55, 66,
						0, 127 });
			}
		});

		addTest(new TestCase("Single write, shorter bytes replaces") {
			@Override
			public void test() throws Exception {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ReplaceOutputStream out = new ReplaceOutputStream(bout,
						new byte[] { 12, 0 }, new byte[] { 55 });

				byte[] data = new byte[] { 42, 12, 0, 127 };

				out.write(data);
				out.close();

				checkArrays(this, "FIRST", bout, new byte[] { 42, 55, 127 });
			}
		});

		addTest(new TestCase("Single write, remove bytes replaces") {
			@Override
			public void test() throws Exception {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ReplaceOutputStream out = new ReplaceOutputStream(bout,
						new byte[] { 12 }, new byte[] {});

				byte[] data = new byte[] { 42, 12, 0, 127 };

				out.write(data);
				out.close();

				checkArrays(this, "FIRST", bout, new byte[] { 42, 0, 127 });
			}
		});
	}

	static void checkArrays(TestCase test, String prefix,
			ByteArrayOutputStream bout, byte[] expected) throws Exception {
		byte[] actual = bout.toByteArray();

		if (false) {
			System.out.print("\nExpected data: [ ");
			for (int i = 0; i < expected.length; i++) {
				if (i > 0)
					System.out.print(", ");
				System.out.print(expected[i]);
			}
			System.out.println(" ]");

			System.out.print("Actual data  : [ ");
			for (int i = 0; i < actual.length; i++) {
				if (i > 0)
					System.out.print(", ");
				System.out.print(actual[i]);
			}
			System.out.println(" ]");
		}

		test.assertEquals("The " + prefix
				+ " resulting array has not the correct number of items",
				expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			test.assertEquals(prefix + ": item " + i
					+ " (0-based) is not the same", expected[i], actual[i]);
		}
	}
}
