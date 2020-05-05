package be.nikiroo.utils;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Simple proxy helper to select a default internet proxy.
 * 
 * @author niki
 */
public class Proxy {
	/**
	 * Use the proxy described by this string:
	 * <ul>
	 * <li><tt>((user(:pass)@)proxy:port)</tt></li>
	 * <li>System proxy is noted <tt>:</tt></li>
	 * </ul>
	 * Some examples:
	 * <ul>
	 * <li><tt></tt> → do not use any proxy</li>
	 * <li><tt>:</tt> → use the system proxy</li>
	 * <li><tt>user@prox.com</tt> → use the proxy "prox.com" with default port
	 * and user "user"</li>
	 * <li><tt>prox.com:8080</tt> → use the proxy "prox.com" on port 8080</li>
	 * <li><tt>user:pass@prox.com:8080</tt> → use "prox.com" on port 8080
	 * authenticated as "user" with password "pass"</li>
	 * <li><tt>user:pass@:</tt> → use the system proxy authenticated as user
	 * "user" with password "pass"</li>
	 * </ul>
	 * 
	 * @param proxy
	 *            the proxy
	 */
	static public void use(String proxy) {
		if (proxy != null && !proxy.isEmpty()) {
			String user = null;
			String password = null;
			int port = 8080;

			if (proxy.contains("@")) {
				int pos = proxy.indexOf("@");
				user = proxy.substring(0, pos);
				proxy = proxy.substring(pos + 1);
				if (user.contains(":")) {
					pos = user.indexOf(":");
					password = user.substring(pos + 1);
					user = user.substring(0, pos);
				}
			}

			if (proxy.equals(":")) {
				proxy = null;
			} else if (proxy.contains(":")) {
				int pos = proxy.indexOf(":");
				try {
					port = Integer.parseInt(proxy.substring(0, pos));
					proxy = proxy.substring(pos + 1);
				} catch (Exception e) {
				}
			}

			if (proxy == null) {
				Proxy.useSystemProxy(user, password);
			} else {
				Proxy.useProxy(proxy, port, user, password);
			}
		}
	}

	/**
	 * Use the system proxy.
	 */
	static public void useSystemProxy() {
		useSystemProxy(null, null);
	}

	/**
	 * Use the system proxy with the given login/password, for authenticated
	 * proxies.
	 * 
	 * @param user
	 *            the user name or login
	 * @param password
	 *            the password
	 */
	static public void useSystemProxy(String user, String password) {
		System.setProperty("java.net.useSystemProxies", "true");
		auth(user, password);
	}

	/**
	 * Use the give proxy.
	 * 
	 * @param host
	 *            the proxy host name or IP address
	 * @param port
	 *            the port to use
	 */
	static public void useProxy(String host, int port) {
		useProxy(host, port, null, null);
	}

	/**
	 * Use the given proxy with the given login/password, for authenticated
	 * proxies.
	 * 
	 * @param user
	 *            the user name or login
	 * @param password
	 *            the password
	 * @param host
	 *            the proxy host name or IP address
	 * @param port
	 *            the port to use
	 * @param user
	 *            the user name or login
	 * @param password
	 *            the password
	 */
	static public void useProxy(String host, int port, String user,
			String password) {
		System.setProperty("http.proxyHost", host);
		System.setProperty("http.proxyPort", Integer.toString(port));
		auth(user, password);
	}

	/**
	 * Select the default authenticator for proxy requests.
	 * 
	 * @param user
	 *            the user name or login
	 * @param password
	 *            the password
	 */
	static private void auth(final String user, final String password) {
		if (user != null && password != null) {
			Authenticator proxy = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					if (getRequestorType() == RequestorType.PROXY) {
						return new PasswordAuthentication(user,
								password.toCharArray());
					}
					return null;
				}
			};
			Authenticator.setDefault(proxy);
		}
	}
}
