package com.android.proxy.client;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by .hp on 29-12-2015.
 */
public class MyProxyServer {
    private String message = null;
    private int currentId =0;

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
            message = "开始监听:"+port+"端口";
            print(message);
            Log.d("Started on: ", ""+port);
        } catch (IOException e) {
            System.err.println("Could not listen on port!");
            System.exit(-1);
        }

        while (listening) {
           // new ProxyThread(serverSocket.accept()).start();
            try {
                Socket proxySocket = serverSocket.accept();
                currentId ++;
                ProxyConnectionHandler proxyConnectionHandler = new ProxyConnectionHandler(proxySocket,currentId);
                new Thread(proxyConnectionHandler).start();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        serverSocket.close();
    }

    private void print(String message){
        EventBus.getDefault().postSticky(new MessageEvent(message));
    }
}
