package com.android.proxy.client;

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
	private static final boolean DEBUG = true;
	private static final String TAG = "proxyConnectionHandler";
	private static final String CRLF = "\r\n";
	private static final int BUFFER_SIZE = 32 * 1024;
	private String[] filters = new String[]{ "sina"};

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
			//byte[] bytes = toByteArray(proxyInputStream);


			//int bytesRead = 1;
			byte[] bytes = new byte[BUFFER_SIZE];
			int bytesRead = proxyInputStream.read(bytes, 0, BUFFER_SIZE);
			String request = new String(bytes);
			String host = extractHost(request);
            Log.d("**~~~** Request Host: ", request);

            if (host == null) return;
            if (mHttpFirstLine == null) return;

			if (DEBUG) {
				int j = 0;
				for (int i = 0; i < filters.length; i++) {
					if (!host.contains(filters[i])) {
						j++;
					}
				}
				if (j == 1) {
					mProxySocket.close();
					return;
				}
			}

            Log.d("sina","长度:"+bytes.length+":"+bytes[10]+"-->"+bytes[100]+"-->"+bytes[bytes.length-1]);

			int port = mHttpFirstLine.Port;
			message = "第" + currentId + "条代理通道:" + "主机:" + host + " 端口:" + port + " 长度:" + bytes
                    .length+ " 请求:" + request ;
			print(message);

			if (port == 443) {
				new Https443RequestHandler(mProxySocket).handle(host);
			} else {
				mOutsideSocket = new Socket(host, port);
				OutputStream outsideOutputStream = mOutsideSocket.getOutputStream();

				outsideOutputStream.write(bytes);
				outsideOutputStream.flush();

				InputStream outsideSocketInputStream = mOutsideSocket.getInputStream();
				OutputStream proxyOutputStream = mProxySocket.getOutputStream();
				byte[] responseArray = new byte[BUFFER_SIZE];

				do {
					bytesRead = outsideSocketInputStream.read(responseArray, 0, BUFFER_SIZE);
					if (bytesRead > 0) {
						proxyOutputStream.write(responseArray, 0, bytesRead);
						String response = new String(bytes, 0, bytesRead);
						Log.d("Outside IPS Response: ", response);
					}
				} while (bytesRead > 0);
				proxyOutputStream.flush();
				mOutsideSocket.close();
			}
			mProxySocket.close();
			Log.d(TAG, "花费时间 " + (System.currentTimeMillis() - startTimestamp) + "ms");
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}

	}

	private String extractHost(String request) {
		int index = request.indexOf(CRLF);
		if (index > 0) {
			try {
				String firstLine = request.substring(0, index);
				Log.d(TAG, "第一行数据:" + firstLine);
				mHttpFirstLine = new HttpFirstLine(firstLine);
				if (mHttpFirstLine.Host == null) {
					int startIndex = request.indexOf("Host: ");
					if (startIndex > 0) {
						int hStart = startIndex + 6;
						int hEnd = request.indexOf('\r', hStart);
						String temp = request.substring(hStart, hEnd);
						Log.d(TAG, "Key Host, Value : " + temp);
						parseHost(mHttpFirstLine, temp);
					} else {
						mHttpFirstLine.Host = null;
					}
				}
				return mHttpFirstLine.Host;
			} catch (FirstLineFormatErrorException e) {
				Log.e(TAG, e.toString());
			}
		}
		return null;
	}

	public void parseHost(HttpFirstLine firstLine, String H) {
		int index = H.indexOf(':');
		if (index > 0) {
			firstLine.Host = H.substring(0, index);
			firstLine.Port = Integer.valueOf(H.substring(index + 1));
		} else {
			firstLine.Host = H;
			firstLine.Port = 80;
		}
	}

	private void print(String message) {
		EventBus.getDefault().postSticky(new MessageEvent(message));

	}

	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		return output.toByteArray();
	}
}
