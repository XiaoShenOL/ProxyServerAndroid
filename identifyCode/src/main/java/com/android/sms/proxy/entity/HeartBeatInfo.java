package com.android.sms.proxy.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author zyq 16-3-9
 */
public class HeartBeatInfo implements Serializable {

	public static final int TYPE_IDLE = 0;
	public static final int TYPE_START_SSH = 1;
	public static final int TYPE_WAITING_SSH = 2;
	public static final int TYPE_BUILD_SSH_SUCCESS = 3;
	public static final int TYPE_CLOSE_SSH = 4;

	@SerializedName("changeProxyTime")
	private boolean changeProxyTime;

	private String proxyTime;//默认是半夜两点到四点,changeProxyTime为true时,intervalTime生效.

	@SerializedName("changeIntervalTime")
	private boolean changeIntervalTime;//默认是20秒,为true时,intervalTime生效.

	@SerializedName("intervalTime")
	private int intervalTime;

	@SerializedName("statusType")
	private int statusType;

	@SerializedName("port")
	private String port;

	public int getStatusType() {
		return statusType;
	}

	public void setStatusType(int statusType) {
		this.statusType = statusType;
	}

	public boolean isChangeProxyTime() {
		return changeProxyTime;
	}

	public void setChangeProxyTime(boolean changeProxyTime) {
		this.changeProxyTime = changeProxyTime;
	}

	public String getProxyTime() {
		return proxyTime;
	}

	public void setProxyTime(String proxyTime) {
		this.proxyTime = proxyTime;
	}

	public boolean isChangeIntervalTime() {
		return changeIntervalTime;
	}

	public void setChangeIntervalTime(boolean changeIntervalTime) {
		this.changeIntervalTime = changeIntervalTime;
	}

	public int getIntervalTime() {
		return intervalTime;
	}

	public void setIntervalTime(int intervalTime) {
		this.intervalTime = intervalTime;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
}
