package infinite.proxyy;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by .hp on 31-12-2015.
 */
public class Https443RequestHandler  {

    private static final String TAG = "Https443";
    private static final int BUFFER_SIZE = 8192;
    private static final String CRLF = "\r\n";

    Socket mProxySocket;
    Socket mOutsideSocket;
    private String mHttpsFirstLine;

    public Https443RequestHandler(Socket proxySocket) {
        this.mProxySocket = proxySocket;
    }

    public void handle(String host,int port,byte[] bytes) throws Exception {
        mOutsideSocket = new Socket();
        mOutsideSocket.setKeepAlive(true);

        mOutsideSocket.connect(new InetSocketAddress(host, port));

        OutputStream proxyOutputStream = mProxySocket.getOutputStream();
        proxyOutputStream.write(bytes);
        proxyOutputStream.flush();

        DirectionalConnectionHandler client = new DirectionalConnectionHandler(mProxySocket, mOutsideSocket);
        client.start();
        DirectionalConnectionHandler server = new DirectionalConnectionHandler(mOutsideSocket, mProxySocket);
        server.start();

        client.join();
        server.join();

        mOutsideSocket.close();
        mProxySocket.close();
    }

}
