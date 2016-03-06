package org.connectbot.service;

import android.util.Log;

import org.connectbot.bean.HostBean;
import org.connectbot.bean.PortForwardBean;
import org.connectbot.transport.AbsTransport;
import org.connectbot.transport.TransportFactory;

import java.io.IOException;
import java.util.List;

import infinite.proxyy.R;

/**
 * @author zyq 16-3-5
 */
public class TerminalBridge {
	public final static String TAG = "CB.TerminalBridge";

	protected final TerminalManager manager;

	protected BridgeDisconnectedListener disconnectListener = null;

	public HostBean host;
	private final String emulation;

	/* package */ AbsTransport transport;
	private boolean disconnected = false;
	private boolean awaitingClose = false;
	private PortForwardBean mForwardBean;


	/**
	 * Create new terminal bridge with following parameters. We will immediately
	 * launch thread to start SSH connection and handle any hostkey verification
	 * and password authentication.
	 */
	public TerminalBridge(final TerminalManager manager, final HostBean host,PortForwardBean portForwardBean) throws IOException {
		this.manager = manager;
		this.host = host;
		emulation = "xterm-256color";
		this.mForwardBean = portForwardBean;
	}

	/**
	 * Spawn thread to open connection and start login process.
	 */
	protected void startConnection() {
		transport = TransportFactory.getTransport(host.getProtocol());
		transport.setBridge(this);
		transport.setManager(manager);
		transport.setHost(host);

		// TODO make this more abstract so we don't litter on AbsTransport
		transport.setCompression(host.getCompression());
		transport.setUseAuthAgent(host.getUseAuthAgent());
		transport.setEmulation(emulation);

		if (transport.canForwardPorts()) {
//todo         由于端口是随机,所以这里不做存储,打算在service启动之前就获取转发的信息
//			for (PortForwardBean portForward : manager.hostdb.getPortForwardsForHost(host))
//				transport.addPortForward(portForward);
			transport.addPortForward(mForwardBean);
		}

		Log.d(TAG, manager.res.getString(R.string.terminal_connecting, host.getHostname(), host.getPort(), host
				.getProtocol()));
		Thread connectionThread = new Thread(new Runnable() {
			public void run() {
				transport.connect();
			}
		});
		connectionThread.setName("Connection");
		connectionThread.setDaemon(true);
		connectionThread.start();
	}

	/**
	 * Internal method to request actual PTY terminal once we've finished
	 * authentication. If called before authenticated, it will just fail.
	 */
	public void onConnected() {
		//todo 暂时不知在这可以干什么
		disconnected = false;

	}

	/**
	 * @return whether a session is open or not
	 */
	public boolean isSessionOpen() {
		if (transport != null)
			return transport.isSessionOpen();
		return false;
	}

	public void setOnDisconnectedListener(BridgeDisconnectedListener disconnectListener) {
		this.disconnectListener = disconnectListener;
	}

	/**
	 * Force disconnection of this terminal bridge.
	 */
	public void dispatchDisconnect(boolean immediate) {
		// We don't need to do this multiple times.
		synchronized (this) {
			if (disconnected && !immediate)
				return;

			disconnected = true;
		}

		// Cancel any pending prompts.

		// disconnection request hangs if we havent really connected to a host yet
		// temporary fix is to just spawn disconnection into a thread
		Thread disconnectThread = new Thread(new Runnable() {
			public void run() {
				if (transport != null && transport.isConnected())
					transport.close();
			}
		});
		disconnectThread.setName("Disconnect");
		disconnectThread.start();

		//todo 这里要关闭连接时候,是立即关闭,还是先保持连接,暂时直接关闭.
		triggerDisconnectListener();
	}

	/**
	 * Tells the TerminalManager that we can be destroyed now.
	 */
	private void triggerDisconnectListener() {
		if (disconnectListener != null) {
			disconnectListener.onDisconnected(TerminalBridge.this);
		}
	}

	/**
	 * @return whether underlying transport can forward ports
	 */
	public boolean canFowardPorts() {
		return transport.canForwardPorts();
	}

	/**
	 * Adds the {@link PortForwardBean} to the list.
	 * @param portForward the port forward bean to add
	 * @return true on successful addition
	 */
	public boolean addPortForward(PortForwardBean portForward) {
		return transport.addPortForward(portForward);
	}

	/**
	 * Removes the {@link PortForwardBean} from the list.
	 * @param portForward the port forward bean to remove
	 * @return true on successful removal
	 */
	public boolean removePortForward(PortForwardBean portForward) {
		return transport.removePortForward(portForward);
	}

	/**
	 * @return the list of port forwards
	 */
	public List<PortForwardBean> getPortForwards() {
		return transport.getPortForwards();
	}

	/**
	 * Enables a port forward member. After calling this method, the port forward should
	 * be operational.
	 * @param portForward member of our current port forwards list to enable
	 * @return true on successful port forward setup
	 */
	public boolean enablePortForward(PortForwardBean portForward) {
		if (!transport.isConnected()) {
			Log.i(TAG, "Attempt to enable port forward while not connected");
			return false;
		}

		return transport.enablePortForward(portForward);
	}

	/**
	 * Disables a port forward member. After calling this method, the port forward should
	 * be non-functioning.
	 * @param portForward member of our current port forwards list to enable
	 * @return true on successful port forward tear-down
	 */
	public boolean disablePortForward(PortForwardBean portForward) {
		if (!transport.isConnected()) {
			Log.i(TAG, "Attempt to disable port forward while not connected");
			return false;
		}

		return transport.disablePortForward(portForward);
	}

	/**
	 * @return whether the TerminalBridge should close
	 */
	public boolean isAwaitingClose() {
		return awaitingClose;
	}

	/**
	 * @return whether this connection had started and subsequently disconnected
	 */
	public boolean isDisconnected() {
		return disconnected;
	}

	/**
	 * @return
	 */
	public boolean isUsingNetwork() {
		return transport.usesNetwork();
	}
}
