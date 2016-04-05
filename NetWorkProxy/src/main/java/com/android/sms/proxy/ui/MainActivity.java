package com.android.sms.proxy.ui;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.android.sms.proxy.R;
import com.android.sms.proxy.entity.MessageEvent;
import com.android.sms.proxy.entity.NativeParams;
import com.android.sms.proxy.entity.PhoneInfo;
import com.android.sms.proxy.service.AlarmControl;
import com.android.sms.proxy.service.ApkUpdateUtil;
import com.android.sms.proxy.service.IProxyControl;
import com.android.sms.proxy.service.ProxyServiceUtil;
import com.android.sms.proxy.service.Receiver_SMS;
import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.assist.bolts.Task;

import net.luna.common.download.interfaces.ApkDownloadListener;
import net.luna.common.download.model.AppModel;
import net.luna.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.download.ext.OplayDownloadManager;
import net.youmi.android.libs.common.download.ext.SimpleAppInfo;

import org.connectbot.bean.HostBean;
import org.connectbot.bean.PortForwardBean;
import org.connectbot.service.BridgeDisconnectedListener;
import org.connectbot.service.TerminalBridge;
import org.connectbot.service.TerminalManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author zyq 16-3-10
 */
public class MainActivity extends AppCompatActivity implements Receiver_SMS.OnReceiveSMSListener,
		BridgeDisconnectedListener, ServiceConnection, ApkDownloadListener, OplayDownloadManager
				.OnDownloadStatusChangeListener, OplayDownloadManager.OnProgressUpdateListener, net.youmi.android.libs
				.common.download.listener.ApkDownloadListener {

	private static final boolean DEBUG = false;
	private static final String TAG = "main";
	public static final String NETWORK_CACHE_DIR = "volley";
	private TextView message;
	private String printMessage;
	private StringBuilder oldMsg = new StringBuilder();
	private TerminalManager binder;
	private TerminalBridge hostBridge;
	private HostBean mHostBean;
	private PortForwardBean portForward;
	private long mBindServiceStartTime = 0;
	private IProxyControl mProxyControl;
	private boolean isProxyServiceRunning = false;

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "TerminalManager service 建立起来了");
			binder = ((TerminalManager.TerminalBinder) service).getService();
			mHostBean = ProxyServiceUtil.getInstance(MainActivity.this).getHostBean();
			portForward = ProxyServiceUtil.getInstance(MainActivity.this).getPortFowardBean();

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


	private void print(String printMessage) {
		EventBus.getDefault().postSticky(new MessageEvent(printMessage));
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		message = (TextView) findViewById(R.id.message);
		message.setMovementMethod(ScrollingMovementMethod.getInstance());
		//EventBus.getDefault().register(this);
		//Receiver_SMS.setReceiveListener(this);
//		AlarmControl.getInstance(this).initAlarm(15, 52, 0, 0);
//		Log.d(TAG, "imsi:" + PhoneInfo.getInstance(this).getPhoneIMSI());
		String phoneNumber = PhoneInfo.getInstance(this).getNativePhoneNumber1();
//		Log.d(TAG, "手机号码:" + phoneNumber);
//		String imei = PhoneInfo.getInstance(this).getIMEI();
//		Log.d(TAG, "imei:" + imei);

		TextView mTvGetPhone = (TextView)findViewById(R.id.trygetnumber);
		if (!TextUtils.isEmpty(phoneNumber)) {
			mTvGetPhone.setText("phoneNumber：" + phoneNumber);
		} else {
			mTvGetPhone.setText("cannot find phone number");
		}

		EventBus.getDefault().register(this);
		AlarmControl.getInstance(this).initAlarm(1, 1, 1, 1);
		FlurryAgent.onStartSession(this);
		OplayDownloadManager.getInstance(this).registerListener(this);
		OplayDownloadManager.getInstance(this).addDownloadStatusListener(this);
		OplayDownloadManager.getInstance(this).addProgressUpdateListener(this);
		Task.callInBackground(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				ApkUpdateUtil.getInstance(getApplication()).updateApk();
				return null;
			}
		});

	}


	@Override
	public void onReceiveSMS(String sms) {
		Log.d(TAG, "短信内容为:" + sms);
		//sendRegisterCode(sms);
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}


//	@Override
//	protected void onDestroy() {
//
//		super.onDestroy();
////		ComponentName componentToEnable = new ComponentName("com.android.sms.proxy", "com.android.sms.proxy.ui" +
////				".MainActivity");
////		getPackageManager().setComponentEnabledSetting(componentToEnable, PackageManager
////						.COMPONENT_ENABLED_STATE_ENABLED,
////				PackageManager.DONT_KILL_APP);
////		if (binder != null) {
////			unbindService(connection);
////		}
////		if (mProxyControl != null) {
////			unbindService(this);
////		}
//	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Subscribe
	public void onEvent(final MessageEvent event) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (event != null) {

					if (oldMsg.length() == 0) {
						oldMsg.append(event.getSocketMessage());
					} else {
						oldMsg.append("\r\n");
						oldMsg.append(event.getSocketMessage());
					}

					message.setText(oldMsg.toString());
				}
			}
		});
	}

	@Override
	public void onDisconnected(TerminalBridge bridge) {

	}

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


	@Override
	protected void onDestroy() {
		super.onDestroy();
		FlurryAgent.onEndSession(this);
		OplayDownloadManager.getInstance(this).removeListener(this);
		OplayDownloadManager.getInstance(this).removeDownloadStatusListener(this);
		OplayDownloadManager.getInstance(this).removeProgressUpdateListener(this);
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

		final String downloadSuccess = "\nDownload success\n";
		EventBus.getDefault().post(new MessageEvent(downloadSuccess));
	}

	@Override
	public void onApkDownloadSuccess(AppModel model) {
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(true));
		FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);

		final String downloadSuccess = "\nDownload success(isExist)\n";
		EventBus.getDefault().post(new MessageEvent(downloadSuccess));
	}

	@Override
	public void onApkDownloadFailed(FileDownloadTask task) {
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(false));
		FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);

		final String downloadFail = "\nDownload Fail\n";
		EventBus.getDefault().post(new MessageEvent(downloadFail));
	}

	@Override
	public void onApkDownloadStop(FileDownloadTask task) {

	}

	@Override
	public void onApkDownloadProgressUpdate(FileDownloadTask task, long contentLength, long completeLength, int
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
	}


	@Override
	public void onProgressUpdate(String url, int percent, long speedBytesPerS) {
		if (DEBUG) {
			Log.d(TAG, "onProgressUpdate!!!!!!!!!!!");
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
		if(DEBUG){
			Log.d(TAG,"apkDownloadSuccess!!!!!!!!!");
		}
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(true));
		FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
		final String downloadSuccess = "\nDownload success(isExist)\n";
		EventBus.getDefault().post(new MessageEvent(downloadSuccess));
	}

	@Override
	public void onApkDownloadFailed(net.youmi.android.libs.common.download.model.FileDownloadTask task) {
		if(DEBUG){
			Log.d(TAG,"apkDownloadFail!!!!!!!!!!!!!");
		}
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(false));
		FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
		final String downloadSuccess = "\nDownload success(isExist)\n";
		EventBus.getDefault().post(new MessageEvent(downloadSuccess));
	}

	@Override
	public void onApkDownloadStop(net.youmi.android.libs.common.download.model.FileDownloadTask task) {
		if(DEBUG){
			Log.d(TAG,"apkDownloadStop!!!!!!!!!!!!!");
		}
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_DOWNLOAD_SUCCESS, String.valueOf(false));
		FlurryAgent.logEvent(NativeParams.EVENT_START_DOWNLOAD, map);
		final String downloadSuccess = "\nDownload success(isExist)\n";
		EventBus.getDefault().post(new MessageEvent(downloadSuccess));
	}

	@Override
	public void onApkDownloadProgressUpdate(net.youmi.android.libs.common.download.model.FileDownloadTask task, long
			contentLength, long completeLength, int percent, long speedBytesPerS) {

	}

	@Override
	public void onApkInstallSuccess(int rawUrlHashCode) {

	}
}
