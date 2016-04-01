package net.youmi.android.libs.common.dns;

import net.youmi.android.libs.common.dns.Message.Record;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.EventListener;

public class SimpleResolver {

	public static final int DEFAULT_PORT = 53;

	private InetSocketAddress address;
	private InetSocketAddress localAddress;
	private long timeoutValue = 10 * 1000;

	private static final short DEFAULT_UDPSIZE = 512;

	private static String defaultResolver = "localhost";
	private static int uniqueID = 0;

	public SimpleResolver(String hostname) throws UnknownHostException {
		if (hostname == null) {
			hostname = ResolverConfig.getCurrentConfig().server();
			if (hostname == null)
				hostname = defaultResolver;
		}
		InetAddress addr;
		if (hostname.equals("0"))
			addr = InetAddress.getLocalHost();
		else
			addr = InetAddress.getByName(hostname);
		address = new InetSocketAddress(addr, DEFAULT_PORT);
	}

	public SimpleResolver() throws UnknownHostException {
		this(null);
	}

	private Message parseMessage(byte[] b) throws IOException {
		return (new Message(b));
	}

	private int maxUDPSize(Message query) {
		return DEFAULT_UDPSIZE;
	}

	public Message send(Message query) throws IOException {
		System.err.println("Sending to "
				+ address.getAddress().getHostAddress() + ":"
				+ address.getPort());
		byte[] out = query.toWire();
		int udpSize = maxUDPSize(query);
		long endTime = System.currentTimeMillis() + timeoutValue;
		try {
			do {
				byte[] in;
				in = UDPClient.sendrecv(localAddress, address, out, udpSize,
						endTime);
				if (in.length < Message.HEADER_LENGTH) {
					throw new IOException("invalid DNS header - " + "too short");
				}
				Message response = parseMessage(in);
				// Log.e("ip", InetAddress.getByAddress(response.getAddr())
				// .getHostAddress());
				return response;
			} while (true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return query;
	}

	public Object sendAsync(final Message query, final ResolverListener listener) {
		final Object id;
		synchronized (this) {
			id = new Integer(uniqueID++);
		}
		Record question = query.getQuestion();
		String qname;
		if (question != null)
			qname = question.name.toString();
		else
			qname = "(none)";
		String name = this.getClass() + ": " + qname;
		Thread thread = new ResolveThread(this, query, id, listener);
		thread.setName(name);
		thread.setDaemon(true);
		thread.start();
		return id;
	}

	public interface ResolverListener extends EventListener {

		void receiveMessage(Object id, Message m);

		void handleException(Object id, Exception e);
	}

	class ResolveThread extends Thread {

		private Message query;
		private Object id;
		private ResolverListener listener;
		private SimpleResolver res;

		/** Creates a new ResolveThread */
		public ResolveThread(SimpleResolver res, Message query, Object id,
				ResolverListener listener) {
			this.res = res;
			this.query = query;
			this.id = id;
			this.listener = listener;
		}

		public void run() {
			try {
				Message response = res.send(query);
				listener.receiveMessage(id, response);
			} catch (Exception e) {
				listener.handleException(id, e);
			}
		}
	}

}
