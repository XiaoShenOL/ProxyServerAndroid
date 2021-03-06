package infinite.proxyy;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{

    private TextView mTvShow;
    private StringBuilder oldMsg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.connect).setOnClickListener(this);
        mTvShow =(TextView)findViewById(R.id.tv_socket_message);
        oldMsg = new StringBuilder();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        EventBus.getDefault().register(this);
    }



    @Override
    public void onClick(View v) {

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new MyTestServer().init();
                        Log.d("~~~:", "proxy server initiated.");
                    }catch (Exception e){

                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onEvent(final  MessageEvent event){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(event != null ){
                    Log.d("testEvent","接收到信息"+ oldMsg.length());
                    if(oldMsg.length() == 0) {
                        oldMsg.append(event.getSocketMessage());
                    }else {
                        oldMsg.append("\r\n");
                        oldMsg.append(event.getSocketMessage());
                    }

                    Log.d("testEvent",oldMsg.toString());
                    mTvShow.setText(oldMsg.toString());
                }
            }
        });

    }

}
