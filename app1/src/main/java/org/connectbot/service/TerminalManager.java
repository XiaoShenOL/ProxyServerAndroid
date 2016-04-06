package org.connectbot.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.connectbot.bean.HostBean;
import org.connectbot.bean.PortForwardBean;
import org.connectbot.bean.PubkeyBean;
import org.connectbot.transport.TransportFactory;
import org.connectbot.util.PreferenceConstants;
import org.connectbot.util.PubkeyDatabase;
import org.connectbot.util.PubkeyUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import infinite.proxyy.MessageEvent;

/**
 * @author zyq 16-3-5
 */
public class TerminalManager extends Service implements SharedPreferences.OnSharedPreferenceChangeListener,BridgeDisconnectedListener {

	public final static String TAG = "CB.TerminalManager";
	private ArrayList<TerminalBridge> bridges = new ArrayList<TerminalBridge>();
	public Map<HostBean, WeakReference<TerminalBridge>> mHostBridgeMap =
			new HashMap<HostBean, WeakReference<TerminalBridge>>();
	public Map<String, WeakReference<TerminalBridge>> mNicknameBridgeMap =
			new HashMap<String, WeakReference<TerminalBridge>>();

	private final ArrayList<OnHostStatusChangedListener> hostStatusChangedListeners = new ArrayList<>();
	public List<HostBean> disconnected = new LinkedList<HostBean>();
	public Map<String, KeyHolder> loadedKeypairs = new HashMap<String, KeyHolder>();
	public Resources res;

	private Timer pubkeyTimer;
	public PubkeyDatabase pubkeydb;
	private boolean savingKeys;
	protected SharedPreferences prefs;

	final private IBinder binder = new TerminalBinder();

	private Timer idleTimer;
	private final long IDLE_TIMEOUT = 300000; // 5 minutes

	private Vibrator vibrator;
	private volatile boolean wantKeyVibration;
	public static final long VIBRATE_DURATION = 30;

	private ConnectivityReceiver connectivityManager;

	public BridgeDisconnectedListener disconnectListener = null;
	private String message = null;

	public TerminalBridge defaultBridge = null;
	protected List<WeakReference<TerminalBridge>> mPendingReconnect
			= new LinkedList<WeakReference<TerminalBridge>>();


	public class TerminalBinder extends Binder {
		public TerminalManager getService() {
			return TerminalManager.this;
		}
	}


	private void print(String message){
		EventBus.getDefault().postSticky(new MessageEvent(message));
	}
	@Override
	public void onCreate() {
		Log.i(TAG, "Starting service");
		message = "Starting service";
		print(message);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		res = getResources();

		pubkeyTimer = new Timer("pubkeyTimer", true);

		pubkeydb = PubkeyDatabase.get(this);

		List<PubkeyBean> pubkeys = pubkeydb.getAllStartPubkeys();

		for (PubkeyBean pubkey : pubkeys) {
			try {
				PrivateKey privKey = PubkeyUtils.decodePrivate(pubkey.getPrivateKey(), pubkey.getType());
				PublicKey pubKey = PubkeyUtils.decodePublic(pubkey.getPublicKey(), pubkey.getType());
				KeyPair pair = new KeyPair(pubKey, privKey);

				addKey(pubkey, pair);
			} catch (Exception e) {
				message = String.format("Problem adding key '%s' to in-memory cache", pubkey.getNickname())+":"+e;
				print(message);
				Log.d(TAG, String.format("Problem adding key '%s' to in-memory cache", pubkey.getNickname()), e);
			}
		}

		final boolean lockingWifi = prefs.getBoolean(PreferenceConstants.WIFI_LOCK, true);

		connectivityManager = new ConnectivityReceiver(this, lockingWifi);

	}


	public void addKey(PubkeyBean pubkey, KeyPair pair) {
		addKey(pubkey, pair, false);
	}


	public void addKey(PubkeyBean pubkey,KeyPair pair,boolean force){
        if(!savingKeys && !force){
	        return;
        }
		removeKey(pubkey.getNickname());
		byte[] sshPubKey = PubkeyUtils.extractOpenSSHPublic(pair);

		KeyHolder keyHolder = new KeyHolder();
		keyHolder.bean = pubkey;
		keyHolder.pair = pair;
		keyHolder.openSSHPubkey = sshPubKey;

		loadedKeypairs.put(pubkey.getNickname(), keyHolder);

		if (pubkey.getLifetime() > 0) {
			final String nickname = pubkey.getNickname();
			pubkeyTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					Log.d(TAG, "Unloading from memory key: " + nickname);
					removeKey(nickname);
				}
			}, pubkey.getLifetime() * 1000);
		}

		Log.d(TAG, String.format("Added key '%s' to in-memory cache", pubkey.getNickname()));
		message = String.format("Added key '%s' to in-memory cache", pubkey.getNickname());
		print(message);
	}

	public boolean isKeyLoaded(String nickname) {
		return loadedKeypairs.containsKey(nickname);
	}
	public boolean removeKey(String nickname) {
		Log.d(TAG, String.format("Removed key '%s' to in-memory cache", nickname));
		return loadedKeypairs.remove(nickname) != null;
	}

	public boolean removeKey(byte[] publicKey) {
		String nickname = null;
		for (Map.Entry<String, KeyHolder> entry : loadedKeypairs.entrySet()) {
			if (Arrays.equals(entry.getValue().openSSHPubkey, publicKey)) {
				nickname = entry.getKey();
				break;
			}
		}

		if (nickname != null) {
			Log.d(TAG, String.format("Removed key '%s' to in-memory cache", nickname));
			return removeKey(nickname);
		} else
			return false;
	}

	public KeyPair getKey(String nickname) {
		if (loadedKeypairs.containsKey(nickname)) {
			KeyHolder keyHolder = loadedKeypairs.get(nickname);
			return keyHolder.pair;
		} else
			return null;
	}


	public KeyPair getKey(byte[] publicKey) {
		for (KeyHolder keyHolder : loadedKeypairs.values()) {
			if (Arrays.equals(keyHolder.openSSHPubkey, publicKey))
				return keyHolder.pair;
		}
		return null;
	}

	public String getKeyNickname(byte[] publicKey) {
		for (Map.Entry<String, KeyHolder> entry : loadedKeypairs.entrySet()) {
			if (Arrays.equals(entry.getValue().openSSHPubkey, publicKey))
				return entry.getKey();
		}
		return null;
	}

	private void stopWithDelay() {
		// TODO add in a way to check whether keys loaded are encrypted and only
		// set timer when we have an encrypted key loaded

		if (loadedKeypairs.size() > 0) {
			synchronized (this) {
				if (idleTimer == null)
					idleTimer = new Timer("idleTimer", true);

				idleTimer.schedule(new IdleTask(), IDLE_TIMEOUT);
			}
		} else {
			Log.d(TAG, "Stopping service immediately");
			stopSelf();
		}
	}

	public ArrayList<TerminalBridge> getBridges() {
		return bridges;
	}

	private class IdleTask extends TimerTask {
		@Override
		public void run() {
			Log.d(TAG, String.format("Stopping service after timeout of ~%d seconds", IDLE_TIMEOUT / 1000));
			TerminalManager.this.stopNow();
		}
	}

	protected void stopNow() {
		if (bridges.size() == 0) {
			stopSelf();
		}
	}


	/**
	 * Called when connectivity to the network is lost and it doesn't appear
	 * we'll be getting a different connection any time soon.
	 */
	public void onConnectivityLost() {
		final Thread t = new Thread() {
			@Override
			public void run() {
				disconnectAll(false, true);
			}
		};
		t.setName("Disconnector");
		t.start();
	}

	/**
	 * Called when connectivity to the network is restored.
	 */
	public void onConnectivityRestored() {
		final Thread t = new Thread() {
			@Override
			public void run() {
				reconnectPending();
			}
		};
		t.setName("Reconnector");
		t.start();
	}

	/**
	 * Reconnect all bridges that were pending a reconnect when connectivity
	 * was lost.
	 */
	private void reconnectPending() {
		synchronized (mPendingReconnect) {
			for (WeakReference<TerminalBridge> ref : mPendingReconnect) {
				TerminalBridge bridge = ref.get();
				if (bridge == null) {
					continue;
				}
				bridge.startConnection();
			}
			mPendingReconnect.clear();
		}
	}

	private void updateSavingKeys() {
		savingKeys = prefs.getBoolean(PreferenceConstants.MEMKEYS, true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		 * We want this service to continue running until it is explicitly
		 * stopped, so return sticky.
		 */
		return START_STICKY;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Log.i(TAG, "Someone rebound to TerminalManager with " + bridges.size() + " bridges active");
		keepServiceAlive();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "Someone unbound from TerminalManager with " + bridges.size() + " bridges active");
		if(bridges.size() == 0){
			stopWithDelay();
		}
		return super.onUnbind(intent);
	}

	/**
	 * Send system notification to user for a certain host. When user selects
	 * the notification, it will bring them directly to the ConsoleActivity
	 * displaying the host.
	 *
	 * @param host
	 */
	public void sendActivityNotification(HostBean host) {
		if (!prefs.getBoolean(PreferenceConstants.BELL_NOTIFICATION, false))
			return;

		ConnectionNotifier.getInstance().showActivityNotification(this, host);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "Someone bound to TerminalManager with active");
		keepServiceAlive();
		return binder;
	}

	/**
	 * Make sure we stay running to maintain the bridges. Later {@link #stopNow} should be called to stop the service.
	 */
	private void keepServiceAlive() {
		stopIdleTimer();
		startService(new Intent(this, TerminalManager.class));
	}

	private synchronized void stopIdleTimer() {
		if (idleTimer != null) {
			idleTimer.cancel();
			idleTimer = null;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (PreferenceConstants.MEMKEYS.equals(key)) {
			updateSavingKeys();
		}
	}
	@Override
	public void onDisconnected(TerminalBridge bridge) {
		boolean shouldHideRunningNotification = false;
		message = "Bridge Disconnected. Removing it.";
		print(message);
		Log.d(TAG, "Bridge Disconnected. Removing it.");
		synchronized (bridges) {
			// remove this bridge from our list
			bridges.remove(bridge);

			mHostBridgeMap.remove(bridge.host);
			mNicknameBridgeMap.remove(bridge.host.getNickname());

			if (bridge.isUsingNetwork()) {
				connectivityManager.decRef();
			}

			if (bridges.size() == 0 &&
					mPendingReconnect.size() == 0) {
				shouldHideRunningNotification = true;
			}

			// pass notification back up to gui
			if (disconnectListener != null)
				disconnectListener.onDisconnected(bridge);
		}

		synchronized (disconnected) {
			disconnected.add(bridge.host);
		}

		notifyHostStatusChanged();

		if (shouldHideRunningNotification) {
			ConnectionNotifier.getInstance().hideRunningNotification(this);
		}
	}

	public void disconnectAll(final boolean immediate,final boolean excludeLocal){
		TerminalBridge[] tmpBridges = null;

		synchronized (bridges) {
			if (bridges.size() > 0) {
				tmpBridges = bridges.toArray(new TerminalBridge[bridges.size()]);
			}
		}

		if (tmpBridges != null) {
			// disconnect and dispose of any existing bridges
			for (int i = 0; i < tmpBridges.length; i++) {
				if (excludeLocal && !tmpBridges[i].isUsingNetwork())
					continue;
				tmpBridges[i].dispatchDisconnect(immediate);
			}
		}
	}

	/**
	 * Open a new connection by reading parameters from the given URI. Follows
	 * format specified by an individual transport.
	 */
	public TerminalBridge openConnection(Uri uri,PortForwardBean forwardBean) throws Exception {

		HostBean host = TransportFactory.getTransport(uri.getScheme()).createHost(uri);

		return openConnection(host,forwardBean);
	}

	private TerminalBridge openConnection(HostBean host,PortForwardBean forwardBean)throws IOException,IllegalArgumentException{
		// throw exception if terminal already open
		if (getConnectedBridge(host) != null) {
			throw new IllegalArgumentException("Connection already open for that nickname");
		}

		TerminalBridge bridge = new TerminalBridge(this,host,forwardBean);
		bridge.setOnDisconnectedListener(this);
		bridge.startConnection();

		synchronized (bridges){
			bridges.add(bridge);
			WeakReference<TerminalBridge> wr = new WeakReference<TerminalBridge>(bridge);
			mHostBridgeMap.put(bridge.host,wr);
			mNicknameBridgeMap.put(bridge.host.getNickname(),wr);
		}

		synchronized (disconnected){
			disconnected.remove(bridge.host);
		}

		if(bridge.isUsingNetwork()){
			connectivityManager.incRef();
		}
		if (prefs.getBoolean(PreferenceConstants.CONNECTION_PERSIST, true)) {
			//ConnectionNotifier.getInstance().showRunningNotification(this);
		}

		notifyHostStatusChanged();

		return bridge;
	}

	/**
	 * Find a connected {@link TerminalBridge} with the given HostBean.
	 *
	 * @param host the HostBean to search for
	 * @return TerminalBridge that uses the HostBean
	 */
	public TerminalBridge getConnectedBridge(HostBean host) {
		WeakReference<TerminalBridge> wr = mHostBridgeMap.get(host);
		if (wr != null) {
			return wr.get();
		} else {
			return null;
		}
	}

	/**
	 * Find a connected {@link TerminalBridge} using its nickname.
	 *
	 * @param nickname
	 * @return TerminalBridge that matches nickname
	 */
	public TerminalBridge getConnectedBridge(final String nickname) {
		if (nickname == null) {
			return null;
		}
		WeakReference<TerminalBridge> wr = mNicknameBridgeMap.get(nickname);
		if (wr != null) {
			return wr.get();
		} else {
			return null;
		}
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Destroying service");
		disconnectAll(true, false);

		pubkeydb = null;

		synchronized (this){
			if(idleTimer != null){
				idleTimer.cancel();
			}
			if(pubkeyTimer != null){
				pubkeyTimer.cancel();
			}
		}

		connectivityManager.cleanup();

		//表示当前是否要显示到通知栏上.
		ConnectionNotifier.getInstance().hideRunningNotification(this);
	}

	public static class KeyHolder {
		public PubkeyBean bean;
		public KeyPair pair;
		public byte[] openSSHPubkey;
	}

	private void notifyHostStatusChanged() {
		for (OnHostStatusChangedListener listener : hostStatusChangedListeners) {
			listener.onHostStatusChanged();
		}
	}

	/**
	 * Register a {@code listener} that wants to know when a host's status materially changes.
	 * @see #hostStatusChangedListeners
	 */
	public void registerOnHostStatusChangedListener(OnHostStatusChangedListener listener) {
		if (!hostStatusChangedListeners.contains(listener)) {
			hostStatusChangedListeners.add(listener);
		}
	}

	/**
	 * Unregister a {@code listener} that wants to know when a host's status materially changes.
	 * @see #hostStatusChangedListeners
	 */
	public void unregisterOnHostStatusChangedListener(OnHostStatusChangedListener listener) {
		hostStatusChangedListeners.remove(listener);
	}



}
