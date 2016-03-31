package org.connectbot.transport;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.sms.proxy.R;
import com.flurry.android.FlurryAgent;
import com.trilead.ssh2.AuthAgentCallback;
import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.ConnectionInfo;
import com.trilead.ssh2.ConnectionMonitor;
import com.trilead.ssh2.ExtendedServerHostKeyVerifier;
import com.trilead.ssh2.InteractiveCallback;
import com.trilead.ssh2.KnownHosts;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.crypto.PEMDecoder;
import com.trilead.ssh2.signature.DSASHA1Verify;
import com.trilead.ssh2.signature.RSASHA1Verify;

import org.connectbot.bean.HostBean;
import org.connectbot.bean.PortForwardBean;
import org.connectbot.bean.PubkeyBean;
import org.connectbot.service.TerminalManager;
import org.connectbot.util.PubkeyDatabase;
import org.connectbot.util.PubkeyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author zyq 16-3-5
 */
public class SSH extends AbsTransport implements ConnectionMonitor, InteractiveCallback, AuthAgentCallback {

	public SSH() {
		super();
	}


	private static final boolean DEBUG = true;
	private static final String PROTOCOL = "ssh";
	private static final String TAG = "CB.SSH";
	private static final int DEFAULT_PORT = 22;

	private static final String AUTH_PUBLICKEY = "publickey",
			AUTH_PASSWORD = "password",
			AUTH_KEYBOARDINTERACTIVE = "keyboard-interactive";

	private final static int AUTH_TRIES = 20;

	static final Pattern hostmask;

	static {
		hostmask = Pattern.compile("^(.+)@(([0-9a-z.-]+)|(\\[[a-f:0-9]+\\]))(:(\\d+))?$", Pattern.CASE_INSENSITIVE);
	}

	private boolean compression = false;
	private volatile boolean authenticated = false;
	private volatile boolean connected = false;
	private volatile boolean sessionOpen = false;

	private boolean pubkeysExhausted = false;
	private boolean interactiveCanContinue = true;

	private Connection connection;
	private Session session;
	private ConnectionInfo connectionInfo;

	private OutputStream stdin;
	private InputStream stdout;
	private InputStream stderr;

	private static final int conditions = ChannelCondition.STDOUT_DATA
			| ChannelCondition.STDERR_DATA
			| ChannelCondition.CLOSED
			| ChannelCondition.EOF;

	private String useAuthAgent = "no";
	private String agentLockPassphrase;
	private List<PortForwardBean> portForwards = new LinkedList<PortForwardBean>();

	private String message = null;


	public class HostKeyVerifier extends ExtendedServerHostKeyVerifier {
		public boolean verifyServerHostKey(String hostname, int port,
		                                   String serverHostKeyAlgorithm, byte[] serverHostKey) throws IOException {

			// read in all known hosts from hostdb
			KnownHosts hosts = new KnownHosts();
			Boolean result;

			String matchName = String.format(Locale.US, "%s:%d", hostname, port);

			String fingerprint = KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey);


			String algorithmName;
			if ("ssh-rsa".equals(serverHostKeyAlgorithm))
				algorithmName = "RSA";
			else if ("ssh-dss".equals(serverHostKeyAlgorithm))
				algorithmName = "DSA";
			else if (serverHostKeyAlgorithm.startsWith("ecdsa-"))
				algorithmName = "EC";
			else
				algorithmName = serverHostKeyAlgorithm;

			switch (hosts.verifyHostkey(matchName, serverHostKeyAlgorithm, serverHostKey)) {
				case KnownHosts.HOSTKEY_IS_OK:
					if (DEBUG) {
						Log.d(TAG, manager.res.getString(R.string.terminal_sucess, algorithmName, fingerprint));
					}
					return true;

				case KnownHosts.HOSTKEY_IS_NEW:
					// prompt user
					if (DEBUG) {
						Log.d(TAG, manager.res.getString(R.string.host_authenticity_warning, hostname));
						Log.d(TAG, manager.res.getString(R.string.host_fingerprint, algorithmName, fingerprint));
					}

					return true;

				case KnownHosts.HOSTKEY_HAS_CHANGED:
					String header = String.format("@   %s   @",
							manager.res.getString(R.string.host_verification_failure_warning_header));

					char[] atsigns = new char[header.length()];
					Arrays.fill(atsigns, '@');
					String border = new String(atsigns);

					if (DEBUG) {
						Log.d(TAG, border);
						Log.d(TAG, header);
						Log.d(TAG, border);
						Log.d(TAG, manager.res.getString(R.string.host_verification_failure_warning));
						Log.d(TAG, String.format(manager.res.getString(R.string.host_fingerprint),
								algorithmName, fingerprint));
					}
					return true;
				default:
					if (DEBUG) {
						Log.d(TAG, manager.res.getString(R.string.terminal_failed, algorithmName, fingerprint));
					}
					return false;
			}
		}

		@Override
		public List<String> getKnownKeyAlgorithmsForHost(String host, int port) {
			return null;
		}

		@Override
		public void removeServerHostKey(String host, int port, String algorithm, byte[] hostKey) {
		}

		@Override
		public void addServerHostKey(String host, int port, String algorithm, byte[] hostKey) {
		}
	}

	private void authenticate() {
		try {
			if (connection.authenticateWithNone(host.getUsername())) {
				finishConnection();
				return;
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.d(TAG, "Host does not support 'none' authentication.");
			}
			FlurryAgent.onError(TAG, "", e);
		}
		if (DEBUG) {
			Log.d(TAG, manager.res.getString(R.string.terminal_auth));
		}
		try {
			long pubkeyId = host.getPubkeyId();//使用指定的密匙,哈哈

			if (DEBUG) {
				Log.d(TAG, "pubkeyId:" + pubkeyId + " userName: " + host.getUsername());
			}
			boolean isAvailable = connection.isAuthMethodAvailable(host.getUsername(), AUTH_PUBLICKEY);
			if (DEBUG) {
				Log.d(TAG, "是否有效：" + isAvailable);
			}
			if (!pubkeysExhausted &&
					pubkeyId != HostBean.PUBKEYID_NEVER &&
					connection.isAuthMethodAvailable(host.getUsername(), AUTH_PUBLICKEY)) {


				if (pubkeyId == HostBean.PUBKEYID_ANY) {
					// try each of the in-memory keys
					//尝试使用内存已有的公匙
				} else {
					//尝试用指定的公匙
					if (DEBUG) {
						Log.d(TAG, manager.res.getString(R.string.terminal_auth_pubkey_specific));
					}
					PubkeyBean pubkey = manager.pubkeydb.findPubkeyById(pubkeyId);
					if (pubkey == null) {
						if (DEBUG) {
							Log.d(TAG, manager.res.getString(R.string.terminal_auth_pubkey_invalid));
						}
					} else if (tryPublicKey(pubkey)) {
						finishConnection();
					}
				}
				pubkeysExhausted = true;
			} else {
			}
		} catch (IllegalStateException e) {
			if (DEBUG) {
				Log.e(TAG, "Connection went away while we were trying to authenticate", e);
			}
			FlurryAgent.onError(TAG, "", e.fillInStackTrace());
			return;
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, "Problem during handleAuthentication()", e);
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}


	/**
	 * Attempt connection with given {@code pubkey}.
	 *
	 * @return {@code true} for successful authentication
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 */
	private boolean tryPublicKey(PubkeyBean pubkey) throws NoSuchAlgorithmException, InvalidKeySpecException,
			IOException {
		KeyPair pair = null;

		if (manager.isKeyLoaded(pubkey.getNickname())) {
			// load this key from memory if its already there
			if (DEBUG) {
				Log.d(TAG, String.format("Found unlocked key '%s' already in-memory", pubkey.getNickname()));
			}
			pair = manager.getKey(pubkey.getNickname());
		} else {
			// otherwise load key from database and prompt for password as needed
			String password = null;
			if (pubkey.isEncrypted()) {//这里不设置密码
				return false;
			}

			if (PubkeyDatabase.KEY_TYPE_IMPORTED.equals(pubkey.getType())) {
				// load specific key using pem format
				pair = PEMDecoder.decode(new String(pubkey.getPrivateKey()).toCharArray(), password);
			} else {
				// load using internal generated format
				PrivateKey privKey;
				try {
					privKey = PubkeyUtils.decodePrivate(pubkey.getPrivateKey(),
							pubkey.getType(), password);
				} catch (Exception e) {
					if (DEBUG) {
						Log.e(TAG, message, e);
					}
					FlurryAgent.onError(TAG, "", e.toString());
					return false;
				}

				PublicKey pubKey = PubkeyUtils.decodePublic(pubkey.getPublicKey(), pubkey.getType());

				// convert key to trilead format
				pair = new KeyPair(pubKey, privKey);
			}
			if (DEBUG) {
				Log.d(TAG, String.format("Unlocked key '%s'", pubkey.getNickname()));
			}
			// save this key in memory
			manager.addKey(pubkey, pair);
		}

		return tryPublicKey(host.getUsername(), pubkey.getNickname(), pair);
	}

	private boolean tryPublicKey(String username, String keyNickname, KeyPair pair) throws IOException {
		//bridge.outputLine(String.format("Attempting 'publickey' with key '%s' [%s]...", keyNickname, trileadKey
		// .toString()));
		boolean success = connection.authenticateWithPublicKey(username, pair);
		if (!success) {
		}
		return success;
	}

	/**
	 * Internal method to request actual PTY terminal once we've finished
	 * authentication. If called before authenticated, it will just fail.
	 */
	private void finishConnection() {
		authenticated = true;

		for (PortForwardBean portForward : portForwards) {
			try {
				enablePortForward(portForward);
				if (DEBUG) {
					Log.d(TAG, manager.res.getString(R.string.terminal_enable_portfoward, portForward.getDescription
							()));

				}
			} catch (Throwable e) {
				if (DEBUG) {
					Log.e(TAG, "Error setting up port forward during connect", e);
				}
				FlurryAgent.onError(TAG, "", e);
			}
		}

		if (!host.getWantSession()) {
			if (DEBUG) {
				Log.d(TAG, manager.res.getString(R.string.terminal_no_session));
			}
			bridge.onConnected();
			return;
		}
	}

	@Override
	public Map<String, byte[]> retrieveIdentities() {
		Map<String, byte[]> pubKeys = new HashMap<String, byte[]>(manager.loadedKeypairs.size());

		for (Map.Entry<String, TerminalManager.KeyHolder> entry : manager.loadedKeypairs.entrySet()) {
			KeyPair pair = entry.getValue().pair;

			try {
				PrivateKey privKey = pair.getPrivate();
				if (privKey instanceof RSAPrivateKey) {
					RSAPublicKey pubkey = (RSAPublicKey) pair.getPublic();
					pubKeys.put(entry.getKey(), RSASHA1Verify.encodeSSHRSAPublicKey(pubkey));
				} else if (privKey instanceof DSAPrivateKey) {
					DSAPublicKey pubkey = (DSAPublicKey) pair.getPublic();
					pubKeys.put(entry.getKey(), DSASHA1Verify.encodeSSHDSAPublicKey(pubkey));
				} else
					continue;
			} catch (IOException e) {
				continue;
			}
		}

		return pubKeys;
	}

	@Override
	public boolean addIdentity(KeyPair pair, String comment, boolean confirmUse, int lifetime) {
		PubkeyBean pubkey = new PubkeyBean();
		pubkey.setNickname(comment);
		pubkey.setConfirmUse(confirmUse);
		pubkey.setLifetime(lifetime);
		manager.addKey(pubkey, pair);
		return true;
	}

	@Override
	public boolean removeIdentity(byte[] publicKey) {
		return manager.removeKey(publicKey);
	}

	@Override
	public boolean removeAllIdentities() {
		manager.loadedKeypairs.clear();
		return true;
	}

	@Override
	public KeyPair getKeyPair(byte[] publicKey) {
		String nickname = manager.getKeyNickname(publicKey);

		if (nickname == null)
			return null;

		return null;
	}

	@Override
	public boolean isAgentLocked() {
		return agentLockPassphrase != null;
	}

	@Override
	public boolean setAgentLock(String lockPassphrase) {
		if (agentLockPassphrase != null)
			return false;

		agentLockPassphrase = lockPassphrase;
		return true;
	}

	@Override
	public boolean requestAgentUnlock(String unlockPassphrase) {
		if (agentLockPassphrase == null)
			return false;

		if (agentLockPassphrase.equals(unlockPassphrase))
			agentLockPassphrase = null;

		return agentLockPassphrase == null;
	}

	@Override
	public void connectionLost(Throwable reason) {
		if(DEBUG){
			Log.d(TAG,"ssh connectionLost !!!!!!!!!");
		}
		onDisconnect();
	}

	private void onDisconnect() {

		bridge.dispatchDisconnect(false);
	}

	@Override
	public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt, boolean[] echo)
			throws Exception {
		interactiveCanContinue = true;
		String[] responses = new String[numPrompts];
		return responses;
	}

	@Override
	public void connect() {
		connection = new Connection(host.getHostname(), host.getPort());
		connection.addConnectionMonitor(this);

		try {
			connection.setCompression(compression);
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, "Could not enable compression!", e);
			}
			FlurryAgent.onError(TAG, "", e);
		}

		try {
			/* Uncomment when debugging SSH protocol:
			DebugLogger logger = new DebugLogger() {
				public void log(int level, String className, String message) {
					Log.d("SSH", message);
				}
			};
			Logger.enabled = true;
			Logger.logger = logger;
			*/

			connectionInfo = connection.connect(new HostKeyVerifier());
			connected = true;
			//客户端到服务器端的算法,还有服务器端到客户端的算法
			if (connectionInfo.clientToServerCryptoAlgorithm
					.equals(connectionInfo.serverToClientCryptoAlgorithm)
					&& connectionInfo.clientToServerMACAlgorithm
					.equals(connectionInfo.serverToClientMACAlgorithm)) {
			}
		} catch (IOException e) {
			if (DEBUG) {
				Log.e(TAG, "Problem in SSH connection thread during authentication", e);
			}
			FlurryAgent.onError(TAG, "", e.fillInStackTrace());
			// Display the reason in the text.
			Throwable t = e.getCause();
			do {
				t = t.getCause();
			} while (t != null);
			close();
			onDisconnect();
			return;
		}

		try {
			// enter a loop to keep trying until authentication
			int tries = 0;
			while (connected && !connection.isAuthenticationComplete() && tries++ < AUTH_TRIES) {
				authenticate();
				// sleep to make sure we dont kill system
				Thread.sleep(1000);
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, "Problem in SSH connection thread during authentication", e);
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		int bytesRead = 0;

		if (session == null)
			return 0;

		int newConditions = session.waitForCondition(conditions, 0);

		if ((newConditions & ChannelCondition.STDOUT_DATA) != 0) {
			bytesRead = stdout.read(buffer, offset, length);
		}

		if ((newConditions & ChannelCondition.STDERR_DATA) != 0) {
			byte discard[] = new byte[256];
			while (stderr.available() > 0) {
				stderr.read(discard);
			}
		}

		if ((newConditions & ChannelCondition.EOF) != 0) {
			if(DEBUG){
				Log.d(TAG,"ssh read error!!!!!!!!!!!!");
			}
			close();
			onDisconnect();
			throw new IOException("Remote end closed connection");
		}

		return bytesRead;
	}

	@Override
	public Map<String, String> getOptions() {
		Map<String, String> options = new HashMap<String, String>();

		options.put("compression", Boolean.toString(compression));

		return options;
	}

	public static String getProtocolName() {
		return PROTOCOL;
	}


	@Override
	public void setOptions(Map<String, String> options) {
		if (options.containsKey("compression"))
			compression = Boolean.parseBoolean(options.get("compression"));
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		if (stdin != null)
			stdin.write(buffer);
	}

	@Override
	public void write(int c) throws IOException {
		if (stdin != null)
			stdin.write(c);
	}

	@Override
	public void flush() throws IOException {
		if (stdin != null)
			stdin.flush();
	}

	@Override
	public void close() {
		connected = false;

		if (session != null) {
			session.close();
			session = null;
		}

		if (connection != null) {
			connection.close();
			connection = null;
		}
	}

	@Override
	public void setDimensions(int columns, int rows, int width, int height) {

	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean isSessionOpen() {
		return sessionOpen;
	}

	@Override
	public int getDefaultPort() {
		return DEFAULT_PORT;
	}

	@Override
	public String getDefaultNickname(String username, String hostname, int port) {
		if (port == DEFAULT_PORT) {
			return String.format(Locale.US, "%s@%s", username, hostname);
		} else {
			return String.format(Locale.US, "%s@%s:%d", username, hostname, port);
		}
	}

	@Override
	public void getSelectionArgs(Uri uri, Map<String, String> selection) {

	}

	@Override
	public HostBean createHost(Uri uri) {
		HostBean host = new HostBean();

		host.setProtocol(PROTOCOL);

		host.setHostname(uri.getHost());

		int port = uri.getPort();
		if (port < 0)
			port = DEFAULT_PORT;
		host.setPort(port);

		host.setUsername(uri.getUserInfo());

		String nickname = uri.getFragment();
		if (nickname == null || nickname.length() == 0) {
			host.setNickname(getDefaultNickname(host.getUsername(),
					host.getHostname(), host.getPort()));
		} else {
			host.setNickname(uri.getFragment());
		}
		return host;
	}

	@Override
	public boolean usesNetwork() {
		return true;
	}

	public static Uri getUri(String input) {
		Matcher matcher = hostmask.matcher(input);

		if (!matcher.matches())
			return null;

		StringBuilder sb = new StringBuilder();

		sb.append(PROTOCOL)
				.append("://")
				.append(Uri.encode(matcher.group(1)))
				.append('@')
				.append(Uri.encode(matcher.group(2)));

		String portString = matcher.group(6);
		int port = DEFAULT_PORT;
		if (portString != null) {
			try {
				port = Integer.parseInt(portString);
				if (port < 1 || port > 65535) {
					port = DEFAULT_PORT;
				}
			} catch (NumberFormatException nfe) {
				// Keep the default port
			}
		}

		if (port != DEFAULT_PORT) {
			sb.append(':')
					.append(port);
		}

		sb.append("/#")
				.append(Uri.encode(input));

		Uri uri = Uri.parse(sb.toString());

		return uri;
	}

	@Override
	public boolean canForwardPorts() {
		return true;
	}

	@Override
	public List<PortForwardBean> getPortForwards() {
		return portForwards;
	}

	@Override
	public boolean addPortForward(PortForwardBean portForward) {
		return portForwards.add(portForward);
	}

	@Override
	public boolean removePortForward(PortForwardBean portForward) {
		// Make sure we don't have a phantom forwarder.
		disablePortForward(portForward);
		return portForwards.remove(portForward);
	}

	@Override
	public boolean enablePortForward(PortForwardBean portForward) {
		if (!portForwards.contains(portForward)) {
			if (DEBUG) {
				Log.e(TAG, "Attempt to enable port forward not in list");
			}
			return false;
		}

		if (!authenticated)
			return false;

		if (PortForwardBean.PORTFORWARD_REMOTE.equals(portForward.getType())) {
			try {
				connection.requestRemotePortForwarding("", portForward.getSourcePort(), portForward.getDestAddr(),
						portForward.getDestPort());
			} catch (Throwable e) {
				if (DEBUG) {
					Log.e(TAG, "Could not create remote port forward", e);
				}
				FlurryAgent.onError(TAG, "", e);
				return false;
			}

			portForward.setEnabled(true);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean disablePortForward(PortForwardBean portForward) {
		if (!portForwards.contains(portForward)) {
			if (DEBUG) {
				Log.e(TAG, "Attempt to disable port forward not in list");
			}
			return false;
		}

		if (!authenticated)
			return false;

		if (PortForwardBean.PORTFORWARD_REMOTE.equals(portForward.getType())) {
			portForward.setEnabled(false);

			try {
				connection.cancelRemotePortForwarding(portForward.getSourcePort());
			} catch (IOException e) {
				if (DEBUG) {
					Log.e(TAG, "Could not stop remote port forwarding, setting enabled to false", e);
				}
				return false;
			}

			return true;
		} else {
			// Unsupported type
			if (DEBUG) {
				Log.e(TAG, String.format("attempt to forward unknown type %s", portForward.getType()));
			}
			return false;
		}
	}


	public static String getFormatHint(Context context) {
		return String.format("%s@%s:%s",
				context.getString(R.string.format_username),
				context.getString(R.string.format_hostname),
				context.getString(R.string.format_port));
	}

	@Override
	public void setUseAuthAgent(String useAuthAgent) {
		this.useAuthAgent = useAuthAgent;
	}

}
