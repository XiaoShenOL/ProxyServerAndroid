package com.android.sms.client;

/**
 * @author zyq 16-3-31
 */
public class MessageEvent {

	private String message;

	public MessageEvent(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
