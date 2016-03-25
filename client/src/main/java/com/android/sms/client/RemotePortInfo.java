package com.android.sms.client;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author zyq 16-3-22
 */
public class RemotePortInfo implements Serializable {

	@SerializedName("port")
	private int port;

	@SerializedName("imei")
	private String imei;

	@SerializedName("status")
	private int status;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
