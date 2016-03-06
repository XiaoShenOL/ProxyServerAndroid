package infinite.proxyy;

/**
 * @author zyq 16-3-6
 */
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
