package com.android.proxy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.network.proxy.ProxyConnector;
import com.android.network.proxy.ProxyManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Random;

/**
 * @author zyq 16-3-5
 */
public class ConnectivityReceiver extends BroadcastReceiver implements ProxyConnector{

	private static final boolean DEBUG = false;
	private static final String TAG = "CB.ConnectivityManager";

	private boolean mIsConnected = false;

	public static ConnectivityReceiver instance;

	public static ConnectivityReceiver getInstance(Context context){
		if(instance == null){
			synchronized (ConnectivityReceiver.class){
				if(instance == null){
					instance = new ConnectivityReceiver(context);
				}
			}
		}
		return  instance;
	}

	public ConnectivityReceiver(Context context) {
		String ip = getCurrentSystemIp(context);
		String imei =getImei(context);
		System.out.println("ip:"+ip);
		System.out.println("imei:"+imei);

		ProxyManager proxyManager = ProxyManager.getInstance(ip);
		proxyManager.registerProxyConnector(this);
		//这里要设置imei
		proxyManager.setImei(imei);
		final ConnectivityManager cm =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			mIsConnected = (info.getState() == NetworkInfo.State.CONNECTED);
			NetworkInfo networkInfo = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			if(networkInfo != null && networkInfo.isConnected()){
				switch (networkInfo.getType()){
					case ConnectivityManager.TYPE_WIFI:
						mIsConnected = true;
						break;
					case ConnectivityManager.TYPE_MOBILE:
                        mIsConnected = false;
						break;
					default:
						break;
				}
			}
			if(mIsConnected){
				if(DEBUG) {
					System.out.println("网络连上了,开始进行代理工作");
				}
				proxyManager.openProxy(imei);
			}
		}
	}


	/**
	 * 获取imei,这里把imei存进数据库,保证一台设备对应一个imei
	 */
	public static String getImei(Context context) {
			SharedPreferences sp = context.getSharedPreferences("proxy", Activity.MODE_PRIVATE);
            String imei = sp.getString("imei", null);
            if(!TextUtils.isEmpty(imei)) return imei;
			TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(
					Context.TELEPHONY_SERVICE);
			imei = mTelephonyMgr.getDeviceId();
			if (imei == null) {
				Random random = new Random();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < 10; i++) {
					int next = random.nextInt(10);
					sb.append(String.valueOf(next));
				}
				imei = sb.toString();
			}
			SharedPreferences.Editor et = sp.edit();
			et.putString("imei",imei);
			et.commit();
			return imei;
	}

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			if (DEBUG) {
				Log.w(TAG, "onReceived() called: " + intent);
			}
			return;
		}
		boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

		boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
		if(DEBUG) {
			Log.d(TAG, "onReceived() called; noConnectivity? " + noConnectivity + "; isFailover? " + isFailover);
		}
		String ip = getCurrentSystemIp(context);
		ProxyManager proxyManager = ProxyManager.getInstance(ip);
		if (noConnectivity && !isFailover && mIsConnected) {
			mIsConnected = false;
			System.out.println("网络连接断开");
			System.out.println("proxy connectLost()");
			proxyManager.onConnectivityLost();
		} else if (!mIsConnected) {
			NetworkInfo info = (NetworkInfo) intent.getExtras()
					.get(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (mIsConnected = (info.getState() == NetworkInfo.State.CONNECTED)) {
				NetworkInfo networkInfo = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
				if(networkInfo != null && networkInfo.isConnected()){
					switch (networkInfo.getType()){
						case ConnectivityManager.TYPE_WIFI:
							System.out.println("网络连接到wifi");
							System.out.println("proxy restored()");
							/**
							 * 这里要重新更新ip
							 */
							String ip1 = getCurrentSystemIp(context);
							proxyManager.updateLocalIp(ip1);
							proxyManager.onConnectivityRestored();
							break;
						default:
							break;
					}
				}
			}
		} else if(mIsConnected){
			NetworkInfo info = (NetworkInfo) intent.getExtras()
					.get(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (mIsConnected = (info.getState() == NetworkInfo.State.CONNECTED)) {
				NetworkInfo networkInfo = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
				if(networkInfo != null && networkInfo.isConnected()){
					switch (networkInfo.getType()){
						case ConnectivityManager.TYPE_MOBILE:
							System.out.println("网络连接到移动网络");
							mIsConnected = false;
							System.out.println("proxy connectLost()");
							proxyManager.onConnectivityLost();
							break;
					}
				}
			}
		}
	}


	public static String getCurrentSystemIp(Context context){

		String ipAddress = null;
		try{
			NetworkInfo networkInfo = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			if(networkInfo != null && networkInfo.isConnected()){
				switch (networkInfo.getType()){
					case ConnectivityManager.TYPE_WIFI:
						ipAddress = getWifiIpAddress(context);
						break;
					case ConnectivityManager.TYPE_MOBILE:
						ipAddress = getLocalIpAddress();
						break;
					default:
						break;
				}
			}

		}catch (Throwable e){
		}
		return  ipAddress;
	}

	//获取本机WIFI
	public static  String getWifiIpAddress(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		// 获取32位整型IP地址
		int ipAddress = wifiInfo.getIpAddress();

		//返回整型地址转换成“*.*.*.*”地址
		return String.format("%d.%d.%d.%d",
				(ipAddress & 0xff), (ipAddress >> 8 & 0xff),
				(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& inetAddress instanceof Inet4Address) {
						// if (!inetAddress.isLoopbackAddress() && inetAddress
						// instanceof Inet6Address) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * ssh开始连接
	 */
	@Override
	public void onSSHStartConnect() {
        Log.d("proxy","onSSHStartConnect");
	}

	/**
	 * ssh 建立成功
	 */
	@Override
	public void onSSHConnect() {
         Log.d("proxy","onSSHConnect");
	}

	/**
	 * proxy 建立成功
	 */
	@Override
	public void onProxyConnect() {
		Log.d("proxy","onProxyConnect");
	}

	/**
	 * ssh 连接断开
	 */
	@Override
	public void onSSHDisconnect() {
		Log.d("proxy","onSSHDisconnect");
	}
}
