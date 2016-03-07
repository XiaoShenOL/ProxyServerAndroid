package infinite.proxyy;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by .hp on 31-12-2015.
 */
public class Https443RequestHandler  {

    private static final int BUFFER_SIZE = 8192;
    private static final String CRLF = "\r\n";

    Socket mProxySocket;
    Socket mOutsideSocket;
    private String mHttpsFirstLine;

    public Https443RequestHandler(Socket proxySocket) {
        this.mProxySocket = proxySocket;
    }

    public void handle(String host) throws Exception {
        int port = 443;
        mOutsideSocket = new Socket();
        mOutsideSocket.setKeepAlive(true);
        mOutsideSocket.connect(new InetSocketAddress(host, port));

        OutputStream proxyOutputStream = mProxySocket.getOutputStream();
        proxyOutputStream.write(("HTTP/1.1 200 Connection established" + CRLF + CRLF).getBytes());
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
