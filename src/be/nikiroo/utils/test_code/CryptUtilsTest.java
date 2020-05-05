package be.nikiroo.utils.test_code;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import be.nikiroo.utils.CryptUtils;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class CryptUtilsTest extends TestLauncher {
	private String key;
	private CryptUtils crypt;

	public CryptUtilsTest(String[] args) {
		super("CryptUtils test", args);

		String longKey = "some long string with more than 128 bits (=32 bytes) of data";

		addSeries(new CryptUtilsTest(args, "Manual input wuth NULL key", null,
				1));
		addSeries(new CryptUtilsTest(args, "Streams with NULL key", null, true));

		addSeries(new CryptUtilsTest(args, "Manual input with emptykey", "", 1));
		addSeries(new CryptUtilsTest(args, "Streams with empty key", "", true));

		addSeries(new CryptUtilsTest(args, "Manual input with long key",
				longKey, 1));
		addSeries(new CryptUtilsTest(args, "Streams with long key", longKey,
				true));
	}

	@Override
	protected void addTest(final TestCase test) {
		super.addTest(new TestCase(test.getName()) {
			@Override
			public void test() throws Exception {
				test.test();
			}

			@Override
			public void setUp() throws Exception {
				crypt = new CryptUtils(key);
				test.setUp();
			}

			@Override
			public void tearDown() throws Exception {
				test.tearDown();
				crypt = null;
			}
		});
	}

	private CryptUtilsTest(String[] args, String title, String key,
			@SuppressWarnings("unused") int dummy) {
		super(title, args);
		this.key = key;

		final String longData = "Le premier jour, Le Grand Barbu dans le cloud fit la lumière, et il vit que c'était bien. Ou quelque chose comme ça. Je préfère la Science-Fiction en général, je trouve ça plus sain :/";

		addTest(new TestCase("Short") {
			@Override
			public void test() throws Exception {
				String orig = "data";
				byte[] encrypted = crypt.encrypt(orig);
				String decrypted = crypt.decrypts(encrypted);

				assertEquals(orig, decrypted);
			}
		});

		addTest(new TestCase("Short, base64") {
			@Override
			public void test() throws Exception {
				String orig = "data";
				String encrypted = crypt.encrypt64(orig);
				String decrypted = crypt.decrypt64s(encrypted);

				assertEquals(orig, decrypted);
			}
		});

		addTest(new TestCase("Empty") {
			@Override
			public void test() throws Exception {
				String orig = "";
				byte[] encrypted = crypt.encrypt(orig);
				String decrypted = crypt.decrypts(encrypted);

				assertEquals(orig, decrypted);
			}
		});

		addTest(new TestCase("Empty, base64") {
			@Override
			public void test() throws Exception {
				String orig = "";
				String encrypted = crypt.encrypt64(orig);
				String decrypted = crypt.decrypt64s(encrypted);

				assertEquals(orig, decrypted);
			}
		});

		addTest(new TestCase("Long") {
			@Override
			public void test() throws Exception {
				String orig = longData;
				byte[] encrypted = crypt.encrypt(orig);
				String decrypted = crypt.decrypts(encrypted);

				assertEquals(orig, decrypted);
			}
		});

		addTest(new TestCase("Long, base64") {
			@Override
			public void test() throws Exception {
				String orig = longData;
				String encrypted = crypt.encrypt64(orig);
				String decrypted = crypt.decrypt64s(encrypted);

				assertEquals(orig, decrypted);
			}
		});
	}

	private CryptUtilsTest(String[] args, String title, String key,
			@SuppressWarnings("unused") boolean dummy) {
		super(title, args);
		this.key = key;

		addTest(new TestCase("Simple test") {
			@Override
			public void test() throws Exception {
				InputStream in = new ByteArrayInputStream(new byte[] { 42, 127,
						12 });
				crypt.encrypt(in);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				IOUtils.write(in, out);
				byte[] result = out.toByteArray();

				assertEquals(
						"We wrote 3 bytes, we expected 3 bytes back but got: "
								+ result.length, result.length, result.length);

				assertEquals(42, result[0]);
				assertEquals(127, result[1]);
				assertEquals(12, result[2]);
			}
		});
	}
}
