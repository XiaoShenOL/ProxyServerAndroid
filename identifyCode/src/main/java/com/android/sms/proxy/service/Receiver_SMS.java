package com.android.sms.proxy.service;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 短信拦截
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


	private OnReceiveSMSListener mOnReceiveSMSListener;

	public Receiver_SMS(OnReceiveSMSListener listener) {
		mOnReceiveSMSListener = listener;
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
			if (SMS_ACTION.equals(intent.getAction())) {
				Bundle args = intent.getExtras();
				String msgContent = "";
				if (DEBUG) {
					Log.d(TAG, "收到短信:" + args);
				}
				if (args != null) {
					Object[] pdus = (Object[]) args.get(SMS_SERVICE);
					SmsMessage messages[] = new SmsMessage[pdus.length];

					for (int i = 0; i < messages.length; i++) {
						messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
						msgContent = messages[i].getMessageBody();

						final String verifyCode = getVerificationCode(msgContent);
						if (verifyCode != null && mOnReceiveSMSListener != null) {
							mOnReceiveSMSListener.onReceiveSMS(verifyCode);
						}
						deleteSMS(context, msgContent);
					}
				}
			}
		}catch (Exception e){
			if(DEBUG){
				Log.e(TAG,e.fillInStackTrace().toString());
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

	public interface OnReceiveSMSListener {
		void onReceiveSMS(String sms);
	}


	public void deleteSMS(Context context,String smsContent){
		try{
			Uri uri = Uri.parse(SMS_RECEIVE_CONTENT);
			ContentResolver contentResolver = context.getContentResolver();
			if(contentResolver != null){
				Cursor isRead = contentResolver.query(uri,null,"read="+0,null,null);
				Log.d(TAG,"找到类似的短信多少条:"+isRead.getCount());
				while(isRead.moveToNext()){
					String body = isRead.getString(isRead.getColumnIndex(SMS_BODY)).trim();
					if(body.equals(smsContent)){
						int id = isRead.getInt(isRead.getColumnIndex(SMS_ID));
						Log.d(TAG,"找到该短信:"+smsContent+" 短信标识为:"+id + "准备删除!");
						int count = context.getContentResolver().delete(Uri.parse(SMS_CONTENT),"_id="+id,null);
						Log.d(TAG,(count == 1) ? "删除成功":"删除失败");
					}
				}
			}
		}catch (Exception e){
			Log.e(TAG,e.fillInStackTrace().toString());
		}
	}

}