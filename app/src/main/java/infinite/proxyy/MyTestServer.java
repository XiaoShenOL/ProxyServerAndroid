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
            EventBus.getDefault().post(new MessageEvent("当前监听哪个端口" + port));
            serverSocket = new ServerSocket(port);


        } catch (IOException e) {
            EventBus.getDefault().post(new MessageEvent(e.toString()));
            System.err.println("Could not listen on port!");
            System.exit(-1);
        }

        while (listening) {
            // new ProxyThread(serverSocket.accept()).start();
            try {
                EventBus.getDefault().post(new MessageEvent("进入等待状态"));
                Socket proxySocket = serverSocket.accept();
                new Thread(new TestRunnable(proxySocket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        serverSocket.close();
    }

    class TestRunnable implements  Runnable{

        private Socket mTestClientSocket;
        public TestRunnable(Socket testClientSocket){
            this.mTestClientSocket = testClientSocket;
        }

        @Override
        public void run() {
            try {

                EventBus.getDefault().post(new MessageEvent("恭喜已经建立链接"));
                Log.d(TAG,"已经建立了链接");
                InputStream proxyInputStream = mTestClientSocket.getInputStream();

                byte[] bytes = new byte[BUFFER_SIZE];
                int bytesRead = proxyInputStream.read(bytes, 0, BUFFER_SIZE);
                String request = new String(bytes);

                Log.d(TAG,"接收到socket:"+request);

                EventBus.getDefault().post(new MessageEvent("接收到的socket" + request));
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(mTestClientSocket.getOutputStream()));
                bufferedWriter.write("接收到了");
                bufferedWriter.flush();
                Log.d(TAG, "回复socket");
                EventBus.getDefault().post(new MessageEvent("回复serverSocket"));
                mTestClientSocket.close();
            }catch (IOException e){

            }
        }
    }
}
