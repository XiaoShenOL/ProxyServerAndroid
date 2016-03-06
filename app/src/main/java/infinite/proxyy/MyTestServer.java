package infinite.proxyy;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by apple on 16/3/5.
 */
public class MyTestServer {

    private int connectCount = 0;
    private static  int BUFFER_SIZE = 8192;
    private static String TAG = "testRunnable";
    public void init() throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;

        int port = 1213;	//default
        // try {
        //   port = Integer.parseInt(args[0]);
        //} catch (Exception e) {
        //ignore me
//        }

        try {
            serverSocket = new ServerSocket(port);
            EventBus.getDefault().post(new MessageEvent("当前监听"+port+"端口"));
            Log.d(TAG, "当前监听哪个端口" + port);
        } catch (IOException e) {
            System.err.println("Could not listen on port!");
            System.exit(-1);
        }

        while (listening) {
            // new ProxyThread(serverSocket.accept()).start();
            try {
                Socket proxySocket = serverSocket.accept();
                connectCount++;
                new Thread(new TestRunnable(proxySocket,connectCount)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        serverSocket.close();
    }

    class TestRunnable implements  Runnable{

        private Socket mTestClientSocket;
        private int currentId;
        private String message = null;
        public TestRunnable(Socket testClientSocket,int currentId){
            this.mTestClientSocket = testClientSocket;
            this.currentId = currentId;
        }

        @Override
        public void run() {
            try {

                message = "当前是 "+currentId+" 建立连接";
                EventBus.getDefault().post(new MessageEvent(message));
                Log.d(TAG,"已经建立了链接");
                InputStream proxyInputStream = mTestClientSocket.getInputStream();

                byte[] bytes = new byte[BUFFER_SIZE];
                int bytesRead = proxyInputStream.read(bytes, 0, BUFFER_SIZE);
                String request = new String(bytes);

                Log.d(TAG,"接收到socket:"+request);
                message = "当前是 "+currentId+" 接收到socket,内容是"+request;
                EventBus.getDefault().post(new MessageEvent(message));


                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(mTestClientSocket.getOutputStream()));
                bufferedWriter.write("接收到了");
                bufferedWriter.flush();
                Log.d(TAG, "回复socket");

                message ="当前是 "+currentId+" 回复serversocket";
                EventBus.getDefault().post(new MessageEvent(message));
                mTestClientSocket.close();
            }catch (IOException e){

            }


        }
    }
}
