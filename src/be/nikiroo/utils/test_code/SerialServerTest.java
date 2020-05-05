package be.nikiroo.utils.test_code;

import java.net.URL;

import be.nikiroo.utils.Version;
import be.nikiroo.utils.serial.server.ConnectActionClientObject;
import be.nikiroo.utils.serial.server.ConnectActionClientString;
import be.nikiroo.utils.serial.server.ConnectActionServerObject;
import be.nikiroo.utils.serial.server.ConnectActionServerString;
import be.nikiroo.utils.serial.server.ServerBridge;
import be.nikiroo.utils.serial.server.ServerObject;
import be.nikiroo.utils.serial.server.ServerString;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class SerialServerTest extends TestLauncher {
	public SerialServerTest(String[] args) {
		super("SerialServer test", args);

		for (String key : new String[] { null,
				"some super secret encryption key" }) {
			for (boolean bridge : new Boolean[] { false, true }) {
				final String skey = (key != null ? "(encrypted)"
						: "(plain text)");
				final String sbridge = (bridge ? " with bridge" : "");

				addSeries(new SerialServerTest(args, key, skey, bridge,
						sbridge, "ServerString"));

				addSeries(new SerialServerTest(args, key, skey, bridge,
						sbridge, new Object() {
							@Override
							public String toString() {
								return "ServerObject";
							}
						}));
			}
		}
	}

	private SerialServerTest(final String[] args, final String key,
			final String skey, final boolean bridge, final String sbridge,
			final String title) {

		super(title + " " + skey + sbridge, args);

		addTest(new TestCase("Simple connection " + skey) {
			@Override
			public void test() throws Exception {
				final String[] rec = new String[1];

				ServerString server = new ServerString(this.getName(), 0, key) {
					@Override
					protected String onRequest(
							ConnectActionServerString action, Version version,
							String data, long id) throws Exception {
						return null;
					}

					@Override
					protected void onError(Exception e) {
					}
				};

				int port = server.getPort();
				assertEquals("A port should have been assigned", true, port > 0);

				server.start();

				ServerBridge br = null;
				if (bridge) {
					br = new ServerBridge(0, key, "", port, key);
					br.setTraceHandler(null);

					port = br.getPort();
					assertEquals(
							"A port should have been assigned to the bridge",
							true, port > 0);

					br.start();
				}

				try {
					try {
						new ConnectActionClientString(null, port, key) {
							@Override
							public void action(Version version)
									throws Exception {
								rec[0] = "ok";
							}
						}.connect();
					} finally {
						server.stop();
					}
				} finally {
					if (br != null) {
						br.stop();
					}
				}

				assertNotNull("The client action was not run", rec[0]);
				assertEquals("ok", rec[0]);
			}
		});

		addTest(new TestCase("Simple exchange " + skey) {
			final String[] sent = new String[1];
			final String[] recd = new String[1];
			final Exception[] err = new Exception[1];

			@Override
			public void test() throws Exception {
				ServerString server = new ServerString(this.getName(), 0, key) {
					@Override
					protected String onRequest(
							ConnectActionServerString action, Version version,
							String data, long id) throws Exception {
						sent[0] = data;
						return "pong";
					}

					@Override
					protected void onError(Exception e) {
						err[0] = e;
					}
				};

				int port = server.getPort();

				server.start();

				ServerBridge br = null;
				if (bridge) {
					br = new ServerBridge(0, key, "", port, key);
					br.setTraceHandler(null);
					port = br.getPort();
					br.start();
				}

				try {
					try {
						new ConnectActionClientString(null, port, key) {
							@Override
							public void action(Version version)
									throws Exception {
								recd[0] = send("ping");
							}
						}.connect();
					} finally {
						server.stop();
					}
				} finally {
					if (br != null) {
						br.stop();
					}
				}

				if (err[0] != null) {
					fail("An exception was thrown: " + err[0].getMessage(),
							err[0]);
				}

				assertEquals("ping", sent[0]);
				assertEquals("pong", recd[0]);
			}
		});

		addTest(new TestCase("Multiple exchanges " + skey) {
			final String[] sent = new String[3];
			final String[] recd = new String[3];
			final Exception[] err = new Exception[1];

			@Override
			public void test() throws Exception {
				ServerString server = new ServerString(this.getName(), 0, key) {
					@Override
					protected String onRequest(
							ConnectActionServerString action, Version version,
							String data, long id) throws Exception {
						sent[0] = data;
						action.send("pong");
						sent[1] = action.rec();
						return "pong2";
					}

					@Override
					protected void onError(Exception e) {
						err[0] = e;
					}
				};

				int port = server.getPort();

				server.start();

				ServerBridge br = null;
				if (bridge) {
					br = new ServerBridge(0, key, "", port, key);
					br.setTraceHandler(null);
					port = br.getPort();
					br.start();
				}

				try {
					try {
						new ConnectActionClientString(null, port, key) {
							@Override
							public void action(Version version)
									throws Exception {
								recd[0] = send("ping");
								recd[1] = send("ping2");
							}
						}.connect();
					} finally {
						server.stop();
					}
				} finally {
					if (br != null) {
						br.stop();
					}
				}

				if (err[0] != null) {
					fail("An exception was thrown: " + err[0].getMessage(),
							err[0]);
				}

				assertEquals("ping", sent[0]);
				assertEquals("pong", recd[0]);
				assertEquals("ping2", sent[1]);
				assertEquals("pong2", recd[1]);
			}
		});

		addTest(new TestCase("Multiple call from client " + skey) {
			final String[] sent = new String[3];
			final String[] recd = new String[3];
			final Exception[] err = new Exception[1];

			@Override
			public void test() throws Exception {
				ServerString server = new ServerString(this.getName(), 0, key) {
					@Override
					protected String onRequest(
							ConnectActionServerString action, Version version,
							String data, long id) throws Exception {
						sent[Integer.parseInt(data)] = data;
						return "" + (Integer.parseInt(data) * 2);
					}

					@Override
					protected void onError(Exception e) {
						err[0] = e;
					}
				};

				int port = server.getPort();

				server.start();

				ServerBridge br = null;
				if (bridge) {
					br = new ServerBridge(0, key, "", port, key);
					br.setTraceHandler(null);
					port = br.getPort();
					br.start();
				}

				try {
					try {
						new ConnectActionClientString(null, port, key) {
							@Override
							public void action(Version version)
									throws Exception {
								for (int i = 0; i < 3; i++) {
									recd[i] = send("" + i);
								}
							}
						}.connect();
					} finally {
						server.stop();
					}
				} finally {
					if (br != null) {
						br.stop();
					}
				}

				if (err[0] != null) {
					fail("An exception was thrown: " + err[0].getMessage(),
							err[0]);
				}

				assertEquals("0", sent[0]);
				assertEquals("0", recd[0]);
				assertEquals("1", sent[1]);
				assertEquals("2", recd[1]);
				assertEquals("2", sent[2]);
				assertEquals("4", recd[2]);
			}
		});
	}

	private SerialServerTest(final String[] args, final String key,
			final String skey, final boolean bridge, final String sbridge,
			final Object title) {

		super(title + " " + skey + sbridge, args);

		addTest(new TestCase("Simple connection " + skey) {
			@Override
			public void test() throws Exception {
				final Object[] rec = new Object[1];

				ServerObject server = new ServerObject(this.getName(), 0, key) {
					@Override
					protected Object onRequest(
							ConnectActionServerObject action, Version version,
							Object data, long id) throws Exception {
						return null;
					}

					@Override
					protected void onError(Exception e) {
					}
				};

				int port = server.getPort();
				assertEquals("A port should have been assigned", true, port > 0);

				server.start();

				ServerBridge br = null;
				if (bridge) {
					br = new ServerBridge(0, key, "", port, key);
					br.setTraceHandler(null);
					port = br.getPort();
					br.start();
				}

				try {
					try {
						new ConnectActionClientObject(null, port, key) {
							@Override
							public void action(Version version)
									throws Exception {
								rec[0] = true;
							}

							@Override
							protected void onError(Exception e) {
							}
						}.connect();
					} finally {
						server.stop();
					}
				} finally {
					if (br != null) {
						br.stop();
					}
				}

				assertNotNull("The client action was not run", rec[0]);
				assertEquals(true, (boolean) ((Boolean) rec[0]));
			}
		});

		addTest(new TestCase("Simple exchange " + skey) {
			final Object[] sent = new Object[1];
			final Object[] recd = new Object[1];
			final Exception[] err = new Exception[1];

			@Override
			public void test() throws Exception {
				ServerObject server = new ServerObject(this.getName(), 0, key) {
					@Override
					protected Object onRequest(
							ConnectActionServerObject action, Version version,
							Object data, long id) throws Exception {
						sent[0] = data;
						return "pong";
					}

					@Override
					protected void onError(Exception e) {
						err[0] = e;
					}
				};

				int port = server.getPort();

				server.start();

				ServerBridge br = null;
				if (bridge) {
					br = new ServerBridge(0, key, "", port, key);
					br.setTraceHandler(null);
					port = br.getPort();
					br.start();
				}

				try {
					try {
						new ConnectActionClientObject(null, port, key) {
							@Override
							public void action(Version version)
									throws Exception {
								recd[0] = send("ping");
							}
						}.connect();
					} finally {
						server.stop();
					}
				} finally {
					if (br != null) {
						br.stop();
					}
				}

				if (err[0] != null) {
					fail("An exception was thrown: " + err[0].getMessage(),
							err[0]);
				}

				assertEquals("ping", sent[0]);
				assertEquals("pong", recd[0]);
			}
		});

		addTest(new TestCase("Multiple exchanges " + skey) {
			final Object[] sent = new Object[3];
			final Object[] recd = new Object[3];
			final Exception[] err = new Exception[1];

			@Override
			public void test() throws Exception {
				ServerObject server = new ServerObject(this.getName(), 0, key) {
					@Override
					protected Object onRequest(
							ConnectActionServerObject action, Version version,
							Object data, long id) throws Exception {
						sent[0] = data;
						action.send("pong");
						sent[1] = action.rec();
						return "pong2";
					}

					@Override
					protected void onError(Exception e) {
						err[0] = e;
					}
				};

				int port = server.getPort();

				server.start();

				ServerBridge br = null;
				if (bridge) {
					br = new ServerBridge(0, key, "", port, key);
					br.setTraceHandler(null);
					port = br.getPort();
					br.start();
				}

				try {
					try {
						new ConnectActionClientObject(null, port, key) {
							@Override
							public void action(Version version)
									throws Exception {
								recd[0] = send("ping");
								recd[1] = send("ping2");
							}
						}.connect();
					} finally {
						server.stop();
					}
				} finally {
					if (br != null) {
						br.stop();
					}
				}

				if (err[0] != null) {
					fail("An exception was thrown: " + err[0].getMessage(),
							err[0]);
				}

				assertEquals("ping", sent[0]);
				assertEquals("pong", recd[0]);
				assertEquals("ping2", sent[1]);
				assertEquals("pong2", recd[1]);
			}
		});

		addTest(new TestCase("Object array of URLs " + skey) {
			final Object[] sent = new Object[1];
			final Object[] recd = new Object[1];
			final Exception[] err = new Exception[1];

			@Override
			public void test() throws Exception {
				ServerObject server = new ServerObject(this.getName(), 0, key) {
					@Override
					protected Object onRequest(
							ConnectActionServerObject action, Version version,
							Object data, long id) throws Exception {
						sent[0] = data;
						return new Object[] { "ACK" };
					}

					@Override
					protected void onError(Exception e) {
						err[0] = e;
					}
				};

				int port = server.getPort();

				server.start();

				ServerBridge br = null;
				if (bridge) {
					br = new ServerBridge(0, key, "", port, key);
					br.setTraceHandler(null);
					port = br.getPort();
					br.start();
				}

				try {
					try {
						new ConnectActionClientObject(null, port, key) {
							@Override
							public void action(Version version)
									throws Exception {
								recd[0] = send(new Object[] {
										"key",
										new URL(
												"https://example.com/from_client"),
										"https://example.com/from_client" });
							}
						}.connect();
					} finally {
						server.stop();
					}
				} finally {
					if (br != null) {
						br.stop();
					}
				}

				if (err[0] != null) {
					fail("An exception was thrown: " + err[0].getMessage(),
							err[0]);
				}

				Object[] sento = (Object[]) (sent[0]);
				Object[] recdo = (Object[]) (recd[0]);

				assertEquals("key", sento[0]);
				assertEquals("https://example.com/from_client",
						((URL) sento[1]).toString());
				assertEquals("https://example.com/from_client", sento[2]);
				assertEquals("ACK", recdo[0]);
			}
		});

		addTest(new TestCase("Multiple call from client " + skey) {
			final Object[] sent = new Object[3];
			final Object[] recd = new Object[3];
			final Exception[] err = new Exception[1];

			@Override
			public void test() throws Exception {
				ServerObject server = new ServerObject(this.getName(), 0, key) {
					@Override
					protected Object onRequest(
							ConnectActionServerObject action, Version version,
							Object data, long id) throws Exception {
						sent[(Integer) data] = data;
						return ((Integer) data) * 2;
					}

					@Override
					protected void onError(Exception e) {
						err[0] = e;
					}
				};

				int port = server.getPort();

				server.start();

				ServerBridge br = null;
				if (bridge) {
					br = new ServerBridge(0, key, "", port, key);
					br.setTraceHandler(null);
					port = br.getPort();
					br.start();
				}

				try {
					try {
						new ConnectActionClientObject(null, port, key) {
							@Override
							public void action(Version version)
									throws Exception {
								for (int i = 0; i < 3; i++) {
									recd[i] = send(i);
								}
							}
						}.connect();
					} finally {
						server.stop();
					}
				} finally {
					if (br != null) {
						br.stop();
					}
				}

				if (err[0] != null) {
					fail("An exception was thrown: " + err[0].getMessage(),
							err[0]);
				}

				assertEquals(0, sent[0]);
				assertEquals(0, recd[0]);
				assertEquals(1, sent[1]);
				assertEquals(2, recd[1]);
				assertEquals(2, sent[2]);
				assertEquals(4, recd[2]);
			}
		});
	}
}
