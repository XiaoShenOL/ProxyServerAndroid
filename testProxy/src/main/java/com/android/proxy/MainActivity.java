package com.android.proxy;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.network.proxy.ProxyManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

	Button test;
	private TelephonyManager tm;
	private String devicId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});
		IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		this.registerReceiver(ConnectivityReceiver.getInstance(this), filter);
//		Intent intent = new Intent();
//		intent.setAction("andrid.intent.action.SHELL_CORE_SERVICE");
//		intent.setPackage("com.internal.fileexplorer");
//		startService(intent);
		tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		devicId = tm.getDeviceId();
		Toast.makeText(this,"当前deviceId:"+devicId,Toast.LENGTH_LONG).show();
		Toast.makeText(this,"当前deviceId:"+devicId,Toast.LENGTH_LONG).show();
		Toast.makeText(this,"当前deviceId:"+devicId,Toast.LENGTH_LONG).show();
		Toast.makeText(this,"当前deviceId:"+devicId,Toast.LENGTH_LONG).show();


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


	@Override
	public void onClick(View v) {
		String localIp = getCurrentSystemIp(this);
		System.out.println("localIp:"+localIp);
		ProxyManager proxyManager = ProxyManager.getInstance(localIp);
		if(devicId == null){
			devicId = "121212121212121";
		}
		proxyManager.openProxy(devicId);

//		TerminalManager binder = TerminalManager.getInstance();
//
//		String quickConnectString = "ubuntu@52.78.13.149:"+String.valueOf(48888);
//		if(com.android.network.proxy.core.ProxyServiceUtil.isHostValid(quickConnectString, "ssh")){
//			final int endIndex = quickConnectString.indexOf(":");
//			final String host = quickConnectString.substring(0, endIndex);
//			String url = TransportFactory.getUri("ssh", host);
//			URI uri = URI.create(url);
//			com.android.network.proxy.core.ProxyServiceUtil.getInstance().setHostBean(uri);
//			int startIndex = quickConnectString.indexOf(":");
//			String sourcePort = quickConnectString.substring(startIndex + 1);
//			//int sourcePort = new Random().nextInt(8000) + 40000;
//			System.out.println("vps分配到的host本地端口是:" + sourcePort);
//			com.android.network.proxy.core.ProxyServiceUtil.getInstance().setPortFowardBean(String.valueOf(sourcePort), localIp);
//
//			//开始启动服务
//			final HostBean mHostBean = com.android.network.proxy.core.ProxyServiceUtil.getInstance().getHostBean();
//			final PortForwardBean portForward = com.android.network.proxy.core.ProxyServiceUtil.getInstance()
//					.getPortFowardBean();
//			if (mHostBean != null && portForward != null) {
//				String requested = mHostBean.getUri();
//				String requestedNickName = mHostBean.getHostname();
//				TerminalBridge hostBridge = binder
//						.getConnectedBridge(requestedNickName);
//				if (requestedNickName != null && hostBridge == null
//						&& portForward != null) {
//					try {
//						System.out.println(String.format(
//								"We couldnt find an existing bridge with URI=%s (nickname=%s), "
//										+ "so" + " " + "creating one now",
//								requested.toString(), requestedNickName));
//						System.out.println("terminalManager:"+binder);
//						hostBridge = binder.openConnection(uri, portForward);
//					} catch (Throwable e) {
//						System.out
//								.println("Problem while trying to create new requested bridge from URI:"
//										+ e);
//					}
//				}
//
//			}
//		}
	}

	@Override
	protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
		super.onChildTitleChanged(childActivity, title);
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
}
