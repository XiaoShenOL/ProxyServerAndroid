package com.android.sms.proxy.entity;

public class MessageEvent {

	private String socketMessage;

	public MessageEvent(String message) {
		this.socketMessage = message;

	}

	public String getSocketMessage() {
		return socketMessage;
	}

	public void setSocketMessage(String socketMessage) {
		this.socketMessage = socketMessage;
	}

}
