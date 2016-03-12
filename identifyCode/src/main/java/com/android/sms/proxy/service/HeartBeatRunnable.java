package com.android.sms.proxy.service;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.sms.proxy.entity.HeartBeatInfo;
import com.android.sms.proxy.entity.HeartBeatJson;
import com.android.sms.proxy.entity.MessageEvent;
import com.android.sms.proxy.entity.NativeParams;
import com.android.sms.proxy.entity.PhoneInfo;
import com.android.sms.proxy.loader.Loader_Base_ForCommon;
import com.oplay.nohelper.volley.RequestEntity;
import com.oplay.nohelper.volley.Response;
import com.oplay.nohelper.volley.VolleyError;

import org.connectbot.bean.HostBean;
import org.connectbot.transport.TransportFactory;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyq 16-3-10
 */
public class HeartBeatRunnable implements Runnable {

	private static final boolean DEBUG = true;
	private static final String TAG = "heartBeatRunnable";
	public static final boolean isSSHConnected = false;
	public static final String url = "http://172.16.5.29:8000/heartbeat/";
	public static String phoneNumber;
	public static String imei;
	private Loader_Base_ForCommon<HeartBeatJson> mLoader;
	private Context mContext;


	public HeartBeatRunnable(Context context) {
		this.mContext = context;
		mLoader = Loader_Base_ForCommon.getInstance();

	}

	@Override
	public void run() {
		try {
			if (phoneNumber == null) phoneNumber = PhoneInfo.getInstance(mContext).getNativePhoneNumber();
			if (imei == null) imei = PhoneInfo.getInstance(mContext).getIMEI();
			if (TextUtils.isEmpty(phoneNumber)) return;
			Log.d(TAG, "发起网络请求");
			Map<String, String> map = new HashMap<>();
			map.put(NativeParams.TYPE_PHONE_NUMBER, phoneNumber);
			map.put(NativeParams.TYPE_PHONE_IMEI, imei);
			map.put(NativeParams.TYPE_SSH_CONNECT, String.valueOf(isSSHConnected));
			RequestEntity<HeartBeatJson> entity = new RequestEntity<HeartBeatJson>(url, HeartBeatJson.class, map);
			mLoader.onRequestLoadNetworkTask(entity, true, new Response.Listener() {
				@Override
				public void onResponse(Object response) {
					if (response instanceof HeartBeatJson) {
						handleResponse((HeartBeatJson) response);
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {

					Log.d(TAG, "网路请求错误:" + error.toString());
				}
			});
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, e.fillInStackTrace().toString());
			}
		}
	}


	private void handleResponse(HeartBeatJson result) {
		int code = result.getCode();
		if (code == NativeParams.SUCCESS) {
			HeartBeatInfo info = result.getData();
			if (info != null) {
				int type = info.getStatusType();
				Log.d(TAG, "返回是:" + type);
				EventBus.getDefault().postSticky(new MessageEvent("返回类型是:" + type));
				switch (type) {
					case HeartBeatInfo.TYPE_IDLE:
						break;
					case HeartBeatInfo.TYPE_START_SSH:
						String host = info.getPort();
						handleStartSSH(host);
						break;
					case HeartBeatInfo.TYPE_WAITING_SSH:
						break;
					case HeartBeatInfo.TYPE_BUILD_SSH_SUCCESS:
						break;
					case HeartBeatInfo.TYPE_CLOSE_SSH:
						break;
				}
			}
		}
	}


	//开始启动SSH
	private void handleStartSSH(String quickConnectString) {
		if (ProxyServiceUtil.isHostValid(quickConnectString, "ssh")) {
			Uri uri = TransportFactory.getUri("ssh", quickConnectString);
			HostBean mHost = new HostBean();
			HostBean host = TransportFactory.getTransport("ssh").createHost(uri);
			mHost.setProtocol(host.getProtocol());
			mHost.setUsername(host.getUsername());
			mHost.setHostname(host.getHostname());
			mHost.setNickname(host.getNickname());
			mHost.setPort(host.getPort());
		}
	}


}
