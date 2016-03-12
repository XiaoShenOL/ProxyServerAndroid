package com.android.sms.proxy.service;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

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

	private static final boolean DEBUG = true;
	private static final String TAG = "proxyCheckUtil";
	private volatile static ProxyServiceUtil instance;
	private Context mContext;
	private PortForwardBean mForwardBean;


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
	}

	private void addPortForwartBean(Context context){
		mForwardBean = new PortForwardBean(
				mHost != null ? mHost.getId() : -1,
				nickNameEdit.getText().toString(),
				PortForwardBean.PORTFORWARD_REMOTE,
				sourcePort,
				destination);
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
