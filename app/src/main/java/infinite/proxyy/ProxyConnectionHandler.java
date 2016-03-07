package infinite.proxyy;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by .hp on 29-12-2015.
 */
public class ProxyConnectionHandler implements Runnable {
	private static final int BUFFER_SIZE = 32 * 1024;//默认是32KB
	private static final String TAG = "proxyConnectionHandler";

	Socket mProxySocket;
	Socket mOutsideSocket;
	private int currentId;
	private String message;
	private HttpFirstLine mHttpFirstLine;


	public ProxyConnectionHandler(Socket proxySocket, int currentId) {
		mProxySocket = proxySocket;
		this.currentId = currentId;
	}

	@Override
	public void run() {
		try {
			long startTimestamp = System.currentTimeMillis();

			InputStream proxyInputStream = mProxySocket.getInputStream();

			byte[] bytes = new byte[BUFFER_SIZE];
			int bytesRead = proxyInputStream.read(bytes, 0, BUFFER_SIZE);
			String request = new String(bytes);

			Log.d("**~~~** Request: ", request);

			String host = extractHost(request);
			Log.d("**~~~** Request Host: ", host);

			if (host == null) return;
			if (mHttpFirstLine == null) return;

			int port = mHttpFirstLine.Port;
			message = "第" + currentId + "条代理通道:" + "主机:" + host + " 端口:" + port + " 请求:" + request;
			print(message);

			if (port == 443) {
				new Https443RequestHandler(mProxySocket).handle(request);
			} else {
				mOutsideSocket = new Socket(host, port);
				OutputStream outsideOutputStream = mOutsideSocket.getOutputStream();

				outsideOutputStream.write(bytes, 0, bytesRead);
				outsideOutputStream.flush();

				InputStream outsideSocketInputStream = mOutsideSocket.getInputStream();
				OutputStream proxyOutputStream = mProxySocket.getOutputStream();
				byte[] responseArray = new byte[BUFFER_SIZE];

				do {
					bytesRead = outsideSocketInputStream.read(responseArray, 0, BUFFER_SIZE);
					if (bytesRead > 0) {
						proxyOutputStream.write(responseArray, 0, bytesRead);
						String response = new String(bytes, 0, bytesRead, "utf-8");
						//Log.d("Outside IPS Response: ", response);
						//message = "第" + currentId + "条代理通道返回的值:" + response;
						//print(message);
					}
				} while (bytesRead > 0);

				proxyOutputStream.flush();
				mOutsideSocket.close();
			}
			mProxySocket.close();

			Log.d(TAG, "花费多长时间:" + (System.currentTimeMillis() - startTimestamp));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String extractHost(String request) {
		int index = request.indexOf("\r\n");
		if (index > 0) {
			try {
				String firstLine = request.substring(0, index);
				mHttpFirstLine = new HttpFirstLine(firstLine);

				if (mHttpFirstLine.Host == null) {
					int hStart = request.indexOf("Host: ") + 6;
					int hEnd = request.indexOf('\n', hStart);
					return request.substring(hStart, hEnd - 1);
				}
				return mHttpFirstLine.Host;
			} catch (FirstLineFormatErrorException e) {
				Log.e(TAG, e.toString());
			}
		}
		return null;
	}

	private void print(String message) {
		EventBus.getDefault().postSticky(new MessageEvent(message));
	}

	private String inputStreamToString(InputStream in) throws IOException {
		byte[] buf = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int i; (i = in.read(buf)) != -1; ) {
			baos.write(buf, 0, i);
		}
		String data = baos.toString("utf-8");
		return data;
	}

}
