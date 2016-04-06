package org.connectbot.event;

/**
 * @author zyq 16-3-6
 */
public class MessageEvent {

	private String proxyMessage;

	public MessageEvent(String msg){
		this.proxyMessage = msg;
	}

	public String getProxyMessage() {
		return proxyMessage;
	}

	public void setProxyMessage(String proxyMessage) {
		this.proxyMessage = proxyMessage;
	}
}
