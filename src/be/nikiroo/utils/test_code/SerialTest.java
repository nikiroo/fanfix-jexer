package be.nikiroo.utils.test_code;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.net.URL;
import java.util.Arrays;

import be.nikiroo.utils.serial.Exporter;
import be.nikiroo.utils.serial.Importer;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class SerialTest extends TestLauncher {
	/**
	 * Required for Import/Export of objects.
	 */
	public SerialTest() {
		this(null);
	}

	private void encodeRecodeTest(TestCase test, Object data) throws Exception {
		byte[] encoded = toBytes(data, true);
		Object redata = fromBytes(toBytes(data, false));
		byte[] reencoded = toBytes(redata, true);

		// We suppose text mode
		if (encoded.length < 256 && reencoded.length < 256) {
			test.assertEquals("Different data after encode/decode/encode",
					new String(encoded, "UTF-8"),
					new String(reencoded, "UTF-8"));
		} else {
			test.assertEquals("Different data after encode/decode/encode",
					true, Arrays.equals(encoded, reencoded));
		}
	}

	// try to remove pointer addresses
	private byte[] toBytes(Object data, boolean clearRefs)
			throws NotSerializableException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new Exporter(out).append(data);
		out.flush();

		if (clearRefs) {
			String tmp = new String(out.toByteArray(), "UTF-8");
			tmp = tmp.replaceAll("@[0-9]*", "@REF");
			return tmp.getBytes("UTF-8");
		}

		return out.toByteArray();
	}

	private Object fromBytes(byte[] data) throws NoSuchFieldException,
			NoSuchMethodException, ClassNotFoundException,
			NullPointerException, IOException {

		InputStream in = new ByteArrayInputStream(data);
		try {
			return new Importer().read(in).getValue();
		} finally {
			in.close();
		}
	}

	public SerialTest(String[] args) {
		super("Serial test", args);

		addTest(new TestCase("Simple class Import/Export") {
			@Override
			public void test() throws Exception {
				Data data = new Data(42);
				encodeRecodeTest(this, data);
			}
		});

		addTest(new TestCase() {
			@SuppressWarnings("unused")
			private TestCase me = setName("Anonymous inner class");

			@Override
			public void test() throws Exception {
				Data data = new Data() {
					@SuppressWarnings("unused")
					int value = 42;
				};
				encodeRecodeTest(this, data);
			}
		});
		addTest(new TestCase() {
			@SuppressWarnings("unused")
			private TestCase me = setName("Array of anonymous inner classes");

			@Override
			public void test() throws Exception {
				Data[] data = new Data[] { new Data() {
					@SuppressWarnings("unused")
					int value = 42;
				} };

				byte[] encoded = toBytes(data, false);
				Object redata = fromBytes(encoded);

				// Comparing the 2 arrays won't be useful, because the @REFs
				// will be ZIP-encoded; so we parse and re-encode each object

				byte[] encoded1 = toBytes(data[0], true);
				byte[] reencoded1 = toBytes(((Object[]) redata)[0], true);

				assertEquals("Different data after encode/decode/encode", true,
						Arrays.equals(encoded1, reencoded1));
			}
		});
		addTest(new TestCase("URL Import/Export") {
			@Override
			public void test() throws Exception {
				URL data = new URL("https://fanfan.be/");
				encodeRecodeTest(this, data);
			}
		});
		addTest(new TestCase("URL-String Import/Export") {
			@Override
			public void test() throws Exception {
				String data = new URL("https://fanfan.be/").toString();
				encodeRecodeTest(this, data);
			}
		});
		addTest(new TestCase("URL/URL-String arrays Import/Export") {
			@Override
			public void test() throws Exception {
				final String url = "https://fanfan.be/";
				Object[] data = new Object[] { new URL(url), url };

				byte[] encoded = toBytes(data, false);
				Object redata = fromBytes(encoded);

				// Comparing the 2 arrays won't be useful, because the @REFs
				// will be ZIP-encoded; so we parse and re-encode each object

				byte[] encoded1 = toBytes(data[0], true);
				byte[] reencoded1 = toBytes(((Object[]) redata)[0], true);
				byte[] encoded2 = toBytes(data[1], true);
				byte[] reencoded2 = toBytes(((Object[]) redata)[1], true);

				assertEquals("Different data 1 after encode/decode/encode",
						true, Arrays.equals(encoded1, reencoded1));
				assertEquals("Different data 2 after encode/decode/encode",
						true, Arrays.equals(encoded2, reencoded2));
			}
		});
		addTest(new TestCase("Import/Export with nested objects") {
			@Override
			public void test() throws Exception {
				Data data = new DataObject(new Data(21));
				encodeRecodeTest(this, data);
			}
		});
		addTest(new TestCase("Import/Export String in object") {
			@Override
			public void test() throws Exception {
				Data data = new DataString("fanfan");
				encodeRecodeTest(this, data);
				data = new DataString("http://example.com/query.html");
				encodeRecodeTest(this, data);
				data = new DataString("Test|Ché|http://|\"\\\"Pouch\\");
				encodeRecodeTest(this, data);
				data = new DataString("Test|Ché\\n|\nhttp://|\"\\\"Pouch\\");
				encodeRecodeTest(this, data);
			}
		});
		addTest(new TestCase("Import/Export with nested objects forming a loop") {
			@Override
			public void test() throws Exception {
				DataLoop data = new DataLoop("looping");
				data.next = new DataLoop("level 2");
				data.next.next = data;
				encodeRecodeTest(this, data);
			}
		});
		addTest(new TestCase("Array in Object Import/Export") {
			@Override
			public void test() throws Exception {
				Object data = new DataArray();// new String[] { "un", "deux" };
				encodeRecodeTest(this, data);
			}
		});
		addTest(new TestCase("Array Import/Export") {
			@Override
			public void test() throws Exception {
				Object data = new String[] { "un", "deux" };
				encodeRecodeTest(this, data);
			}
		});
		addTest(new TestCase("Enum Import/Export") {
			@Override
			public void test() throws Exception {
				Object data = EnumToSend.FANFAN;
				encodeRecodeTest(this, data);
			}
		});
	}

	class DataArray {
		public String[] data = new String[] { "un", "deux" };
	}

	class Data {
		private int value;

		private Data() {
		}

		public Data(int value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Data) {
				Data other = (Data) obj;
				return other.value == this.value;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return new Integer(value).hashCode();
		}
	}

	@SuppressWarnings("unused")
	class DataObject extends Data {
		private Data data;

		@SuppressWarnings("synthetic-access")
		private DataObject() {
		}

		@SuppressWarnings("synthetic-access")
		public DataObject(Data data) {
			this.data = data;
		}
	}

	@SuppressWarnings("unused")
	class DataString extends Data {
		private String data;

		@SuppressWarnings("synthetic-access")
		private DataString() {
		}

		@SuppressWarnings("synthetic-access")
		public DataString(String data) {
			this.data = data;
		}
	}

	@SuppressWarnings("unused")
	class DataLoop extends Data {
		public DataLoop next;
		private String value;

		@SuppressWarnings("synthetic-access")
		private DataLoop() {
		}

		@SuppressWarnings("synthetic-access")
		public DataLoop(String value) {
			this.value = value;
		}
	}

	enum EnumToSend {
		FANFAN, TULIPE,
	}
}
