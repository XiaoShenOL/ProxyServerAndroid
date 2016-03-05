package infinite.proxyy;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by .hp on 29-12-2015.
 */
public class MyProxyServer {
    public void init() throws IOException{
        ServerSocket serverSocket = null;
        boolean listening = true;

        int port = 8090;	//default
       // try {
         //   port = Integer.parseInt(args[0]);
        //} catch (Exception e) {
            //ignore me
//        }

        try {
            serverSocket = new ServerSocket(port);
            Log.d("Started on: ", ""+port);
        } catch (IOException e) {
            System.err.println("Could not listen on port!");
            System.exit(-1);
        }

        while (listening) {
           // new ProxyThread(serverSocket.accept()).start();
            try {
                Socket proxySocket = serverSocket.accept();
                ProxyConnectionHandler proxyConnectionHandler = new ProxyConnectionHandler(proxySocket);
                new Thread(proxyConnectionHandler).start();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        serverSocket.close();
    }
}
