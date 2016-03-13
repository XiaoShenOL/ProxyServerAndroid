package com.android.sms.proxy.service;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import com.oplay.nohelper.utils.Util_GetSystemIP;

import org.connectbot.bean.HostBean;
import org.connectbot.bean.PortForwardBean;
import org.connectbot.bean.PubkeyBean;
import org.connectbot.transport.TransportFactory;
import org.connectbot.util.PubkeyDatabase;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * @author zyq 16-3-11
 *         检查是否满足要求
 */
public class ProxyServiceUtil {

	private static final boolean DEBUG = true;
	private static final String TAG = "proxyCheckUtil";
	private volatile static ProxyServiceUtil instance;
	private Context mContext;
	private PortForwardBean mForwardBean;
    private static int destPort;
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
			Log.d(TAG, "成功存入数据库");
			pubkeydb.savePubkey(pubkey);
		} else {
			Log.d(TAG, "之前已存,不需添加");
		}
        Log.d(TAG,"提前注入密匙:"+pubkey.getDescription());
	}


    public HostBean getHostBean() {
        return mHost;
    }

    public void setHostBean(Uri uri){
        mHost = new HostBean();
        HostBean host = TransportFactory.getTransport("ssh").createHost(uri);
        mHost.setProtocol(host.getProtocol());
        mHost.setUsername(host.getUsername());
        mHost.setHostname(host.getHostname());
        mHost.setNickname(host.getNickname());
        mHost.setPort(host.getPort());//默认是22端口,代理端口

        Log.d(TAG,"根据接口返回的数据生成一个hostbean:"+mHost.toString());
    }

	public  void setPortFowardBean(Context context,String sourcePort){
         destPort = 8964;
//        try{
//            ServerSocket socket = new ServerSocket(0);
//            destPort = socket.getLocalPort();
//        }catch (IOException e){
//            Log.d(TAG,"setPortForwardBean()函数异常："+e.fillInStackTrace().toString());
//        }
        Log.d(TAG, "最后选中的本地监听端口是:" + destPort);

        String destHost = Util_GetSystemIP.getCurrentSystemIp(context);
        Log.d(TAG,"当前ip地址是:"+destHost);
        StringBuilder sb = new StringBuilder();
        sb.append(destHost)
                .append(":")
                .append(String.valueOf(destPort));
        Log.d(TAG, "目标地址：" + sb.toString());
		mForwardBean = new PortForwardBean(
				1,
				"proxy",
				PortForwardBean.PORTFORWARD_REMOTE,
                String.valueOf(sourcePort),
				sb.toString());

        Log.d(TAG,"设置端口转发:"+mForwardBean.getDescription());
	}

    public static  int getDestPort() {
        return destPort;
    }


    public PortForwardBean getPortFowardBean(){
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
