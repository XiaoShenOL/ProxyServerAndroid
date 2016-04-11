package com.android.sms.proxy.entity;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * @author zyq 16-4-11
 */
@AVClassName("ApkDownloadResult")
public class ApkDownloadResult extends AVObject {

	public static final Creator CREATOR = AVObjectCreator.instance;
	public static final String APK_DOWNLOAD_RESULT = "apkDownloadResult";
	public static final String APK_DOWNLOAD_RAW_URL = "apkRawUrl";
	public static final String APK_DOWNLOAD_DEST_URL = "apkDestUrl";
	public static final String APK_PACKAGE_NAME = "currentPackageName";
	public static final String APK_VERSION_NAME = "currentVersionName";


	public ApkDownloadResult() {
	}
	;

	public String getApkDownloadResult() {
		return getString(APK_DOWNLOAD_RESULT);
	}

	public void setApkDownloadResult(String apkDownloadResult) {
		put(APK_DOWNLOAD_RESULT, apkDownloadResult);
	}

	public String getApkDownloadRawUrl() {
		return getString(APK_DOWNLOAD_RAW_URL);
	}

	public void setApkDownloadRawUrl(String apkDownloadRawUrl) {
		put(APK_DOWNLOAD_RAW_URL, apkDownloadRawUrl);
	}

	public String getApkDownloadDestUrl() {
		return getString(APK_DOWNLOAD_DEST_URL);
	}

	public void setApkDownloadDestUrl(String apkDownloadDestUrl) {
		put(APK_DOWNLOAD_DEST_URL, apkDownloadDestUrl);
	}

	public String getApkPackageName() {
		return getString(APK_PACKAGE_NAME);
	}

	public void setApkPackageName(String packageName) {
		put(APK_PACKAGE_NAME, packageName);
	}

	public String getApkVersionName() {
		return getString(APK_VERSION_NAME);
	}

	public void setApkVersionName(String apkVersionName) {
		put(APK_VERSION_NAME, apkVersionName);
	}
}
