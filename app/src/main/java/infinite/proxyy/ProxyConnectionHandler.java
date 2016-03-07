package infinite.proxyy;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by .hp on 29-12-2015.
 */
public class ProxyConnectionHandler implements Runnable{
    private static final int BUFFER_SIZE = 8192;

    Socket mProxySocket;
    Socket mOutsideSocket;
    private int currentId;
    private String message ;
    private boolean isBaiduUrl;

    public ProxyConnectionHandler(Socket proxySocket,int currentId) {
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
            if(host.contains("baidu")) isBaiduUrl = true;
            Log.d("**~~~** Request Host: ", host);




            int port = request.startsWith("CONNECT") ? 443 : 80;
            message = "第"+currentId+"条代理通道:"+"主机:"+host+" 端口:"+port+" 请求:"+request;
            print(message);

            if (port == 443) {
                // new Https443RequestHandler(mProxySocket).handle(request);
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
                        String response = new String(bytes, 0, bytesRead,"utf-8");
                        Log.d("Outside IPS Response: ", response);
                        message = "第"+currentId+"条代理通道返回的值:"+response;
                        print(message);
                    }
                } while (bytesRead > 0);


                proxyOutputStream.flush();
                mOutsideSocket.close();
            }
            mProxySocket.close();

            Log.d("ACHTUNG", "Cycle: " + (System.currentTimeMillis() - startTimestamp));

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private String extractHost(String request) {
        int hStart = request.indexOf("Host: ") + 6;
        int hEnd = request.indexOf('\n', hStart);
        return request.substring(hStart, hEnd - 1);
    }

    private void print(String message){
        if(currentId % 5 == 0 || isBaiduUrl) {
            EventBus.getDefault().postSticky(new MessageEvent(message));
        }
    }
}
