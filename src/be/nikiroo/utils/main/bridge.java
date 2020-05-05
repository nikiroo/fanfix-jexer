package be.nikiroo.utils.main;

import be.nikiroo.utils.TraceHandler;
import be.nikiroo.utils.serial.server.ServerBridge;

/**
 * Serialiser bridge (starts a {@link ServerBridge} and can thus intercept
 * communication between a client and a server).
 * 
 * @author niki
 */
public class bridge {
	/**
	 * The optional options that can be passed to the program.
	 * 
	 * @author niki
	 */
	private enum Option {
		/**
		 * The encryption key for the input data (optional, but can also be
		 * empty <b>which is different</b> (it will then use an empty encryption
		 * key)).
		 */
		KEY,
		/**
		 * The encryption key for the output data (optional, but can also be
		 * empty <b>which is different</b> (it will then use an empty encryption
		 * key)).
		 */
		FORWARD_KEY,
		/** The trace level (1, 2, 3.. default is 1). */
		TRACE_LEVEL,
		/**
		 * The maximum length after which to truncate data to display (the whole
		 * data will still be sent).
		 */
		MAX_DISPLAY_SIZE,
		/** The help message. */
		HELP,
	}

	static private String getSyntax() {
		return "Syntax: (--options) (--) [NAME] [PORT] [FORWARD_HOST] [FORWARD_PORT]\n"//
				+ "\tNAME         : the bridge name for display/debug purposes\n"//
				+ "\tPORT         : the port to listen on\n"//
				+ "\tFORWARD_HOST : the host to connect to\n"//
				+ "\tFORWARD_PORT : the port to connect to\n"//
				+ "\n" //
				+ "\tOptions: \n" //
				+ "\t--                 : no more options in the rest of the parameters\n" //
				+ "\t--help             : this help message\n" //
				+ "\t--key              : the INCOMING encryption key\n" //
				+ "\t--forward-key      : the OUTGOING encryption key\n" //
				+ "\t--trace-level      : the trace level (1, 2, 3... default is 1)\n" //
				+ "\t--max-display-size : the maximum size after which to \n"//
				+ "\t        truncate the messages to display (the full message will still be sent)\n" //
		;
	}

	/**
	 * Start a bridge between 2 servers.
	 * 
	 * @param args
	 *            the parameters, which can be seen by passing "--help" or just
	 *            calling the program without parameters
	 */
	public static void main(String[] args) {
		final TraceHandler tracer = new TraceHandler(true, false, 0);
		try {
			if (args.length == 0) {
				tracer.error(getSyntax());
				System.exit(0);
			}

			String key = null;
			String fkey = null;
			int traceLevel = 1;
			int maxPrintSize = 0;

			int i = 0;
			while (args[i].startsWith("--")) {
				String arg = args[i];
				i++;

				if (arg.equals("--")) {
					break;
				}

				arg = arg.substring(2).toUpperCase().replace("-", "_");
				try {
					Option opt = Enum.valueOf(Option.class, arg);
					switch (opt) {
					case HELP:
						tracer.trace(getSyntax());
						System.exit(0);
						break;
					case FORWARD_KEY:
						fkey = args[i++];
						break;
					case KEY:
						key = args[i++];
						break;
					case MAX_DISPLAY_SIZE:
						maxPrintSize = Integer.parseInt(args[i++]);
						break;
					case TRACE_LEVEL:
						traceLevel = Integer.parseInt(args[i++]);
						break;
					}
				} catch (Exception e) {
					tracer.error(getSyntax());
					System.exit(1);
				}
			}

			if ((args.length - i) != 4) {
				tracer.error(getSyntax());
				System.exit(2);
			}

			String name = args[i++];
			int port = Integer.parseInt(args[i++]);
			String fhost = args[i++];
			int fport = Integer.parseInt(args[i++]);

			ServerBridge bridge = new ServerBridge(name, port, key, fhost,
					fport, fkey);
			bridge.setTraceHandler(new TraceHandler(true, true, traceLevel,
					maxPrintSize));
			bridge.run();
		} catch (Exception e) {
			tracer.error(e);
			System.exit(42);
		}
	}
}
