package net.youmi.android.libs.common.dns;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class ResolverConfig {

	private String[] servers = null;
	private Name[] searchlist = null;
	private int ndots = -1;

	private static ResolverConfig currentConfig;

	static {
		refresh();
	}

	public ResolverConfig() {
		if (findProperty())
			return;
		if (findSunJVM())
			return;
		if (servers == null || searchlist == null) {
			String OS = System.getProperty("os.name");
			String vendor = System.getProperty("java.vendor");
			if (vendor.indexOf("Android") != -1) {
				findAndroid();
			}
		}
	}

	private void addServer(String server, List list) {
		if (list.contains(server))
			return;
		list.add(server);
	}

	private void addSearch(String search, List list) {
		Name name;
		try {
			name = Name.fromString(search, Name.root);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if (list.contains(name))
			return;
		list.add(name);
	}

	private int parseNdots(String token) {
		token = token.substring(6);
		try {
			int ndots = Integer.parseInt(token);
			if (ndots >= 0) {
				return ndots;
			}
		} catch (NumberFormatException e) {
		}
		return -1;
	}

	private void configureFromLists(List lserver, List lsearch) {
		if (servers == null && lserver.size() > 0)
			servers = (String[]) lserver.toArray(new String[0]);
		if (searchlist == null && lsearch.size() > 0)
			searchlist = (Name[]) lsearch.toArray(new Name[0]);
	}

	private void configureNdots(int lndots) {
		if (ndots < 0 && lndots > 0)
			ndots = lndots;
	}

	private boolean findProperty() {
		String prop;
		List lserver = new ArrayList(0);
		List lsearch = new ArrayList(0);
		StringTokenizer st;

		prop = System.getProperty("dns.server");
		if (prop != null) {
			st = new StringTokenizer(prop, ",");
			while (st.hasMoreTokens())
				addServer(st.nextToken(), lserver);
		}

		prop = System.getProperty("dns.search");
		if (prop != null) {
			st = new StringTokenizer(prop, ",");
			while (st.hasMoreTokens())
				addSearch(st.nextToken(), lsearch);
		}
		configureFromLists(lserver, lsearch);
		return (servers != null && searchlist != null);
	}

	private boolean findSunJVM() {
		List lserver = new ArrayList(0);
		List lserver_tmp;
		List lsearch = new ArrayList(0);
		List lsearch_tmp;

		try {
			Class[] noClasses = new Class[0];
			Object[] noObjects = new Object[0];
			String resConfName = "sun.net.dns.ResolverConfiguration";
			Class resConfClass = Class.forName(resConfName);
			Object resConf;

			// ResolverConfiguration resConf = ResolverConfiguration.open();
			Method open = resConfClass.getDeclaredMethod("open", noClasses);
			resConf = open.invoke(null, noObjects);

			// lserver_tmp = resConf.nameservers();
			Method nameservers = resConfClass.getMethod("nameservers",
					noClasses);
			lserver_tmp = (List) nameservers.invoke(resConf, noObjects);

			// lsearch_tmp = resConf.searchlist();
			Method searchlist = resConfClass.getMethod("searchlist", noClasses);
			lsearch_tmp = (List) searchlist.invoke(resConf, noObjects);
		} catch (Exception e) {
			return false;
		}

		if (lserver_tmp.size() == 0)
			return false;

		if (lserver_tmp.size() > 0) {
			Iterator it = lserver_tmp.iterator();
			while (it.hasNext())
				addServer((String) it.next(), lserver);
		}

		if (lsearch_tmp.size() > 0) {
			Iterator it = lsearch_tmp.iterator();
			while (it.hasNext())
				addSearch((String) it.next(), lsearch);
		}
		configureFromLists(lserver, lsearch);
		return true;
	}

	/**
	 * Looks in /etc/resolv.conf to find servers and a search path. "nameserver"
	 * lines specify servers. "domain" and "search" lines define the search
	 * path.
	 */
	private void findResolvConf(String file) {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return;
		}
		InputStreamReader isr = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(isr);
		List lserver = new ArrayList(0);
		List lsearch = new ArrayList(0);
		int lndots = -1;
		try {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("nameserver")) {
					StringTokenizer st = new StringTokenizer(line);
					st.nextToken(); /* skip nameserver */
					addServer(st.nextToken(), lserver);
				} else if (line.startsWith("domain")) {
					StringTokenizer st = new StringTokenizer(line);
					st.nextToken(); /* skip domain */
					if (!st.hasMoreTokens())
						continue;
					if (lsearch.isEmpty())
						addSearch(st.nextToken(), lsearch);
				} else if (line.startsWith("search")) {
					if (!lsearch.isEmpty())
						lsearch.clear();
					StringTokenizer st = new StringTokenizer(line);
					st.nextToken(); /* skip search */
					while (st.hasMoreTokens())
						addSearch(st.nextToken(), lsearch);
				} else if (line.startsWith("options")) {
					StringTokenizer st = new StringTokenizer(line);
					st.nextToken(); /* skip options */
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (token.startsWith("ndots:")) {
							lndots = parseNdots(token);
						}
					}
				}
			}
			br.close();
		} catch (IOException e) {
		}

		configureFromLists(lserver, lsearch);
		configureNdots(lndots);
	}

	private void findAndroid() {
		// This originally looked for all lines containing .dns; but
		// http://code.google.com/p/android/issues/detail?id=2207#c73
		// indicates that net.dns* should always be the active nameservers, so
		// we use those.
		final String re1 = "^\\d+(\\.\\d+){3}$";
		final String re2 = "^[0-9a-f]+(:[0-9a-f]*)+:[0-9a-f]+$";
		ArrayList lserver = new ArrayList();
		ArrayList lsearch = new ArrayList();
		try {
			Class SystemProperties = Class
					.forName("android.os.SystemProperties");
			Method method = SystemProperties.getMethod("get",
					new Class[] { String.class });
			final String[] netdns = new String[] { "net.dns1", "net.dns2",
					"net.dns3", "net.dns4" };
			for (int i = 0; i < netdns.length; i++) {
				Object[] args = new Object[] { netdns[i] };
				String v = (String) method.invoke(null, args);
				if (v != null && (v.matches(re1) || v.matches(re2))
						&& !lserver.contains(v))
					lserver.add(v);
			}
		} catch (Exception e) {
			// ignore resolutely
		}
		configureFromLists(lserver, lsearch);
	}

	/** Returns all located servers */
	public String[] servers() {
		return servers;
	}

	/** Returns the first located server */
	public String server() {
		if (servers == null)
			return null;
		return servers[0];
	}

	/** Returns all entries in the located search path */
	public Name[] searchPath() {
		return searchlist;
	}

	public int ndots() {
		if (ndots < 0)
			return 1;
		return ndots;
	}

	public static synchronized ResolverConfig getCurrentConfig() {
		return currentConfig;
	}

	public static void refresh() {
		ResolverConfig newConfig = new ResolverConfig();
		synchronized (ResolverConfig.class) {
			currentConfig = newConfig;
		}
	}

}
