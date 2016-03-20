package com.android.sms.proxy.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.ServiceCompat;
import android.util.Log;

import com.android.sms.proxy.entity.BindServiceEvent;
import com.android.sms.proxy.entity.MessageEvent;
import com.oplay.nohelper.utils.Util_Service;

import org.connectbot.bean.HostBean;
import org.connectbot.bean.PortForwardBean;
import org.connectbot.event.WaitForSocketEvent;
import org.connectbot.service.BridgeDisconnectedListener;
import org.connectbot.service.TerminalBridge;
import org.connectbot.service.TerminalManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author zyq 16-3-9
 */
public class HeartBeatService extends Service implements BridgeDisconnectedListener {

	private static final boolean DEBUG = true;
	private static final String TAG = "heartBeatService";
	private ScheduledExecutorService mExecutorService;
	private ScheduledFuture mScheduledFuture;
	private static final long MESSAGE_INIT_DELAY = 2;//Message 推送延迟
	public static long MESSAGE_DELAY = 8;//Message 轮询消息
	private HeartBeatRunnable mHeartBeatRunnable = null;
	private TerminalManager binder;
	private TerminalBridge hostBridge;
	private String printMessage;
	private IProxyControl mProxyControl;
	private boolean isProxyServiceRunning;
	private static HeartBeatService instance;

	public static HeartBeatService getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "service onCreate()");
		EventBus.getDefault().register(this);
		//Subprocess.create(getApplicationContext(), WatchDog.class);
		instance = this;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		scheduledWithFixedDelay();
		return ServiceCompat.START_STICKY;
	}

	public void scheduledWithFixedDelay() {
		if (mScheduledFuture == null || mScheduledFuture.isCancelled()) {
			if (DEBUG) {
				Log.d(TAG, "开始发心跳包");
			}
			EventBus.getDefault().postSticky(new MessageEvent("开始发心跳包"));
		}
		if (mExecutorService == null) {
			mExecutorService = Executors.newScheduledThreadPool(1);
		}
		if (mHeartBeatRunnable == null) {
			mHeartBeatRunnable = new HeartBeatRunnable(this);
		}
		if (mScheduledFuture == null || mScheduledFuture.isCancelled()) {
			mScheduledFuture = mExecutorService.scheduleAtFixedRate(mHeartBeatRunnable, MESSAGE_INIT_DELAY,
					MESSAGE_DELAY,
					TimeUnit.SECONDS);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			Log.d(TAG, "heartBeatService destroy()");
			if (mHeartBeatRunnable != null) {
				mHeartBeatRunnable.isSSHConnected = false;
			}
			if (mProxyControl != null) {
				unbindService(proxyConnection);
			}
			if (binder != null) {
				unbindService(connection);
				Log.d(TAG, "heartBeatService结束自己，terminalManager也要结束自己");
				binder.stopSelf();
			}
			if (mScheduledFuture != null) {
				cancelScheduledTasks();
				EventBus.getDefault().postSticky(new MessageEvent("service 退出"));
			}
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	public void cancelScheduledTasks() {
		if (mScheduledFuture != null) {
			if (!mScheduledFuture.isCancelled()) {
				mScheduledFuture.cancel(true);
			}
		}
	}

	public void restScheduledTasks() {
		if (mHeartBeatRunnable == null) mHeartBeatRunnable = new HeartBeatRunnable(this);
		if (mExecutorService == null) mExecutorService = Executors.newScheduledThreadPool(1);
		mScheduledFuture = mExecutorService.scheduleAtFixedRate(mHeartBeatRunnable, MESSAGE_INIT_DELAY, 20, TimeUnit
				.SECONDS);
	}


	public void startTerminalService() {
		try {
			Intent serviceIntent = new Intent(this, TerminalManager.class);
			bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}

	/**
	 * 确保service一定要在运行状态
	 */
	private void sureServiceIsRunning(String serviceName) {
	}

	class CheckServiceRunnable implements Runnable {

		private String serviceName;

		public CheckServiceRunnable(String svcName) {
			serviceName = svcName;
		}

		@Override
		public void run() {
			boolean isServiceRunning = Util_Service.isServiceRunning(HeartBeatService.this, serviceName);
			Log.d(TAG, "当前service是：" + serviceName + " 是否在运行中：" + isServiceRunning);
			if (!isServiceRunning) {
				Log.d(TAG,"重新启动service");
				startTerminalService();
				sureServiceIsRunning(TerminalManager.class.getCanonicalName());
			}
		}
	}

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "TerminalManager service 建立起来了");
			binder = ((TerminalManager.TerminalBinder) service).getService();
			final HostBean mHostBean = ProxyServiceUtil.getInstance(HeartBeatService.this).getHostBean();
			final PortForwardBean portForward = ProxyServiceUtil.getInstance(HeartBeatService.this)
					.getPortFowardBean();

			Log.d(TAG, "TerminalManager建立起来的hostBean:" + mHostBean + " portForwardBean:" + portForward);
			if (mHostBean != null && portForward != null) {

				Uri requested = mHostBean.getUri();
				final String requestedNickName = (requested != null) ? requested.getFragment() : null;

				Log.d(TAG, "requestedNickName:" + requested.getFragment());

				hostBridge = binder.getConnectedBridge(requestedNickName);
				if (requestedNickName != null && hostBridge == null && portForward != null) {
					try {
						Log.d(TAG, String.format("We couldnt find an existing bridge with URI=%s (nickname=%s), so " +
								"creating one now", requested.toString(), requestedNickName));
						printMessage = "重新启动一个代理请求:" + requestedNickName;
						print(printMessage);
						hostBridge = binder.openConnection(requested, portForward);
					} catch (Exception e) {
						printMessage = "Problem while trying to create new requested bridge from URI:" + e;
						print(printMessage);
						Log.e(TAG, "Problem while trying to create new requested bridge from URI", e);
					}
				}

			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			binder = null;
		}
	};


	private void print(String message) {
		EventBus.getDefault().postSticky(new MessageEvent(message));
	}

	@Subscribe
	public void onEvent(BindServiceEvent event) {
		if (event != null) {
			if (binder == null) {
				Log.d(TAG, "开始ssh 服务");
				startTerminalService();
			}
		}
	}

	@Subscribe
	public void onEvent(WaitForSocketEvent event) {
		if (event != null) {
			if (mProxyControl == null) {
				Log.d(TAG, "开始代理服务");
				startProxyService();
			}
		}
	}

	private void startProxyService() {
		try {
			HeartBeatRunnable.isSSHConnected = true;
			Intent serviceIntent = new Intent(this, ProxyService.class);
			bindService(serviceIntent, proxyConnection, Context.BIND_AUTO_CREATE);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}

	private ServiceConnection proxyConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mProxyControl = (IProxyControl) service;
			Log.d(TAG, "proxyService开始监听" + ProxyServiceUtil.getDestPort() + "端口");
			Log.d(TAG, "proxyService 开始链接" + mProxyControl);
			try {
				if (!isProxyServiceRunning && mProxyControl != null) {
					isProxyServiceRunning = mProxyControl.start();
					if (isProxyServiceRunning) {
						printMessage = "开始监听端口:" + ProxyServiceUtil.getDestPort();
						print(printMessage);
					}
				}
			} catch (RemoteException e) {
				Log.e(TAG, e.fillInStackTrace().toString());
				printMessage = e.fillInStackTrace().toString();
				print(printMessage);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mProxyControl = null;
		}
	};


	@Override
	public void onDisconnected(TerminalBridge bridge) {
		try {
			HeartBeatRunnable.isSSHConnected = false;
			HeartBeatRunnable.mCurrentCount = 0;
			Log.d(TAG, "接到ssh要关闭的信息了");
			if (mProxyControl != null) {
				mProxyControl.stop();
				unbindService(proxyConnection);
			}
			cancelScheduledTasks();
		} catch (RemoteException e) {
			if (DEBUG) {
				Log.e(TAG, "heartBeatService.onDisconnected()函数异常:" + e.fillInStackTrace().toString());
			}
		}
	}

	public TerminalManager getBinder() {
		return binder;
	}

	public IProxyControl getmProxyControl() {
		return mProxyControl;
	}
}
