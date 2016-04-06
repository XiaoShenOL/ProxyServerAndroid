package com.android.sms.proxy.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.sms.proxy.core.ProxyServer;


public class ProxyService extends Service {
	private static final boolean DEBUG = true;
	public static final String TAG = "ProxyService";

	@Override
	public IBinder onBind(Intent binder) {
		return new IProxyControl.Stub() {
			@Override
			public boolean start() throws RemoteException {
				return doStart();
			}

			@Override
			public boolean stop() throws RemoteException {
				return doStop();
			}

			@Override
			public boolean isRunning() throws RemoteException {
				return ProxyServer.getInstance().isRunning();
			}

			@Override
			public int getPort() throws RemoteException {
				return ProxyServer.getInstance().getPort();
			}

		};
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public boolean doStart() {
		ProxyServer proxyServer = ProxyServer.getInstance();
		if (proxyServer.isRunning()) {
			if (DEBUG) {
				Log.d(TAG, "已经开始了，无需再启动！！！！！！！！！！！！");
			}
			return false;
		}

		return proxyServer.start();
	}

	public boolean doStop() {
		ProxyServer proxyServer = ProxyServer.getInstance();
		if (!proxyServer.isRunning()) {
			if(DEBUG){
				Log.d(TAG,"已经停止，无需再停止！！！！！！！！！！！！");
			}
			return false;
		}

		return proxyServer.stop();
	}

}
