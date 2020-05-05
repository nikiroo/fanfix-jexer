package be.nikiroo.utils.test_code;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.StringUtils.Alignment;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class StringUtilsTest extends TestLauncher {
	public StringUtilsTest(String[] args) {
		super("StringUtils test", args);

		addTest(new TestCase("Time serialisation") {
			@Override
			public void test() throws Exception {
				for (long fullTime : new Long[] { 0l, 123456l, 123456000l,
						new Date().getTime() }) {
					// precise to the second, no more
					long time = (fullTime / 1000) * 1000;

					String displayTime = StringUtils.fromTime(time);
					assertNotNull("The stringified time for " + time
							+ " should not be null", displayTime);
					assertEquals("The stringified time for " + time
							+ " should not be empty", false, displayTime.trim()
							.isEmpty());

					assertEquals("The time " + time
							+ " should be loop-convertable", time,
							StringUtils.toTime(displayTime));

					assertEquals("The time " + displayTime
							+ " should be loop-convertable", displayTime,
							StringUtils.fromTime(StringUtils
									.toTime(displayTime)));
				}
			}
		});

		addTest(new TestCase("MD5") {
			@Override
			public void test() throws Exception {
				String mess = "The String we got is not what 'md5sum' said it should heve been";
				assertEquals(mess, "34ded48fcff4221d644be9a37e1cb1d9",
						StringUtils.getMd5Hash("fanfan la tulipe"));
				assertEquals(mess, "7691b0cb74ed0f94b4d8cd858abe1165",
						StringUtils.getMd5Hash("je te do-o-o-o-o-o-nne"));
			}
		});

		addTest(new TestCase("Padding") {
			@Override
			public void test() throws Exception {
				for (String data : new String[] { "fanfan", "la tulipe",
						"1234567890", "12345678901234567890", "1", "" }) {
					String result = StringUtils.padString(data, -1);
					assertEquals("A size of -1 is expected to produce a noop",
							true, data.equals(result));
					for (int size : new Integer[] { 0, 1, 5, 10, 40 }) {
						result = StringUtils.padString(data, size);
						assertEquals(
								"Padding a String at a certain size should give a String of the given size",
								size, result.length());
						assertEquals(
								"Padding a String should not change the content",
								true, data.trim().startsWith(result.trim()));

						result = StringUtils.padString(data, size, false, null);
						assertEquals(
								"Padding a String without cutting should not shorten the String",
								true, data.length() <= result.length());
						assertEquals(
								"Padding a String without cutting should keep the whole content",
								true, data.trim().equals(result.trim()));

						result = StringUtils.padString(data, size, false,
								Alignment.RIGHT);
						if (size > data.length()) {
							assertEquals(
									"Padding a String to the end should work as expected",
									true, result.endsWith(data));
						}

						result = StringUtils.padString(data, size, false,
								Alignment.JUSTIFY);
						if (size > data.length()) {
							String unspacedData = data.trim();
							String unspacedResult = result.trim();
							for (int i = 0; i < size; i++) {
								unspacedData = unspacedData.replace("  ", " ");
								unspacedResult = unspacedResult.replace("  ",
										" ");
							}

							assertEquals(
									"Justified text trimmed with all spaces collapsed "
											+ "sould be identical to original text "
											+ "trimmed with all spaces collapsed",
									unspacedData, unspacedResult);
						}

						result = StringUtils.padString(data, size, false,
								Alignment.CENTER);
						if (size > data.length()) {
							int before = 0;
							for (int i = 0; i < result.length()
									&& result.charAt(i) == ' '; i++) {
								before++;
							}

							int after = 0;
							for (int i = result.length() - 1; i >= 0
									&& result.charAt(i) == ' '; i--) {
								after++;
							}

							if (result.trim().isEmpty()) {
								after = before / 2;
								if (before > (2 * after)) {
									before = after + 1;
								} else {
									before = after;
								}
							}

							assertEquals(
									"Padding a String on center should work as expected",
									result.length(), before + data.length()
											+ after);
							assertEquals(
									"Padding a String on center should not uncenter the content",
									true, Math.abs(before - after) <= 1);
						}
					}
				}
			}
		});

		addTest(new TestCase("Justifying") {
			@Override
			public void test() throws Exception {
				Map<String, Map<Integer, Entry<Alignment, List<String>>>> source = new HashMap<String, Map<Integer, Entry<Alignment, List<String>>>>();
				addValue(source, Alignment.LEFT, "testy", -1, "testy");
				addValue(source, Alignment.RIGHT, "testy", -1, "testy");
				addValue(source, Alignment.CENTER, "testy", -1, "testy");
				addValue(source, Alignment.JUSTIFY, "testy", -1, "testy");
				addValue(source, Alignment.LEFT, "testy", 5, "testy");
				addValue(source, Alignment.LEFT, "testy", 3, "te-", "sty");
				addValue(source, Alignment.LEFT,
						"Un petit texte qui se mettra sur plusieurs lignes",
						10, "Un petit", "texte qui", "se mettra", "sur",
						"plusieurs", "lignes");
				addValue(source, Alignment.LEFT,
						"Un petit texte qui se mettra sur plusieurs lignes", 7,
						"Un", "petit", "texte", "qui se", "mettra", "sur",
						"plusie-", "urs", "lignes");
				addValue(source, Alignment.RIGHT,
						"Un petit texte qui se mettra sur plusieurs lignes", 7,
						"     Un", "  petit", "  texte", " qui se", " mettra",
						"    sur", "plusie-", "    urs", " lignes");
				addValue(source, Alignment.CENTER,
						"Un petit texte qui se mettra sur plusieurs lignes", 7,
						"  Un   ", " petit ", " texte ", "qui se ", "mettra ",
						"  sur  ", "plusie-", "  urs  ", "lignes ");
				addValue(source, Alignment.JUSTIFY,
						"Un petit texte qui se mettra sur plusieurs lignes", 7,
						"Un pet-", "it tex-", "te  qui", "se met-", "tra sur",
						"plusie-", "urs li-", "gnes");
				addValue(source, Alignment.JUSTIFY,
						"Un petit texte qui se mettra sur plusieurs lignes",
						14, "Un       petit", "texte  qui  se",
						"mettra     sur", "plusieurs lig-", "nes");
				addValue(source, Alignment.JUSTIFY, "le dash-test", 9,
						"le  dash-", "test");

				for (String data : source.keySet()) {
					for (int size : source.get(data).keySet()) {
						Alignment align = source.get(data).get(size).getKey();
						List<String> values = source.get(data).get(size)
								.getValue();

						List<String> result = StringUtils.justifyText(data,
								size, align);

						// System.out.println("[" + data + " (" + size + ")" +
						// "] -> [");
						// for (int i = 0; i < result.size(); i++) {
						// String resultLine = result.get(i);
						// System.out.println(i + ": " + resultLine);
						// }
						// System.out.println("]");

						assertEquals(values, result);
					}
				}
			}
		});

		addTest(new TestCase("unhtml") {
			@Override
			public void test() throws Exception {
				Map<String, String> data = new HashMap<String, String>();
				data.put("aa", "aa");
				data.put("test with spaces ", "test with spaces ");
				data.put("<a href='truc://target/'>link</a>", "link");
				data.put("<html>Digimon</html>", "Digimon");
				data.put("", "");
				data.put(" ", " ");

				for (Entry<String, String> entry : data.entrySet()) {
					String result = StringUtils.unhtml(entry.getKey());
					assertEquals("Result is not what we expected",
							entry.getValue(), result);
				}
			}
		});

		addTest(new TestCase("zip64") {
			@Override
			public void test() throws Exception {
				String orig = "test";
				String zipped = StringUtils.zip64(orig);
				String unzipped = StringUtils.unzip64s(zipped);
				assertEquals(orig, unzipped);
			}
		});

		addTest(new TestCase("format/toNumber simple") {
			@Override
			public void test() throws Exception {
				assertEquals(263l, StringUtils.toNumber("263"));
				assertEquals(21200l, StringUtils.toNumber("21200"));
				assertEquals(0l, StringUtils.toNumber("0"));
				assertEquals("263", StringUtils.formatNumber(263l));
				assertEquals("21 k", StringUtils.formatNumber(21000l));
				assertEquals("0", StringUtils.formatNumber(0l));
			}
		});

		addTest(new TestCase("format/toNumber not 000") {
			@Override
			public void test() throws Exception {
				assertEquals(263200l, StringUtils.toNumber("263.2 k"));
				assertEquals(42000l, StringUtils.toNumber("42.0 k"));
				assertEquals(12000000l, StringUtils.toNumber("12 M"));
				assertEquals(2000000000l, StringUtils.toNumber("2 G"));
				assertEquals("263 k", StringUtils.formatNumber(263012l));
				assertEquals("42 k", StringUtils.formatNumber(42012l));
				assertEquals("12 M", StringUtils.formatNumber(12012121l));
				assertEquals("7 G", StringUtils.formatNumber(7364635928l));
			}
		});

		addTest(new TestCase("format/toNumber decimals") {
			@Override
			public void test() throws Exception {
				assertEquals(263200l, StringUtils.toNumber("263.2 k"));
				assertEquals(1200l, StringUtils.toNumber("1.2 k"));
				assertEquals(42700000l, StringUtils.toNumber("42.7 M"));
				assertEquals(1220l, StringUtils.toNumber("1.22 k"));
				assertEquals(1432l, StringUtils.toNumber("1.432 k"));
				assertEquals(6938l, StringUtils.toNumber("6.938 k"));
				assertEquals("1.3 k", StringUtils.formatNumber(1300l, 1));
				assertEquals("263.2020 k", StringUtils.formatNumber(263202l, 4));
				assertEquals("1.26 k", StringUtils.formatNumber(1267l, 2));
				assertEquals("42.7 M", StringUtils.formatNumber(42712121l, 1));
				assertEquals("5.09 G", StringUtils.formatNumber(5094837485l, 2));
			}
		});
	}

	static private void addValue(
			Map<String, Map<Integer, Entry<Alignment, List<String>>>> source,
			final Alignment align, String input, int size,
			final String... result) {
		if (!source.containsKey(input)) {
			source.put(input,
					new HashMap<Integer, Entry<Alignment, List<String>>>());
		}

		source.get(input).put(size, new Entry<Alignment, List<String>>() {
			@Override
			public Alignment getKey() {
				return align;
			}

			@Override
			public List<String> getValue() {
				return Arrays.asList(result);
			}

			@Override
			public List<String> setValue(List<String> value) {
				return null;
			}
		});
	}
}
