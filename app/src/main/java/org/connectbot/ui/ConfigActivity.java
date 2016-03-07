package org.connectbot.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.connectbot.bean.HostBean;
import org.connectbot.bean.PortForwardBean;
import org.connectbot.bean.PubkeyBean;
import org.connectbot.event.WaitForSocketEvent;
import org.connectbot.service.BridgeDisconnectedListener;
import org.connectbot.service.TerminalBridge;
import org.connectbot.service.TerminalManager;
import org.connectbot.transport.TransportFactory;
import org.connectbot.util.PubkeyDatabase;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import infinite.proxyy.MessageEvent;
import infinite.proxyy.MyProxyServer;
import infinite.proxyy.MyTestServer;
import infinite.proxyy.R;

/**
 * 配置activity,包括host
 * @author zyq 16-3-6
 */
public class ConfigActivity extends AppCompatActivity implements View.OnClickListener,BridgeDisconnectedListener {

	private static final String TAG = "configActivity";
	private TextInputLayout mQuickConnectContainer;
	private EditText mQuickConnectField;
	private EditText destEdit;
	private EditText nickNameEdit;
	private EditText sourcePortEdit;
	private TextView mPromptMessage;
    private Button mBtnBeginProxy;
	private HostBean mHost;
	private TerminalBridge hostBridge = null;
	protected TerminalManager bound = null;
	protected PortForwardBean portForward = null;

	private String message = null;
	private StringBuilder sb = new StringBuilder();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHost = new HostBean();

        setContentView(R.layout.activity_config);

		EventBus.getDefault().register(this);
		mQuickConnectContainer = (TextInputLayout)findViewById(R.id.quickconnect_field_container);
		mQuickConnectContainer.requestFocus();
		mQuickConnectField = (EditText)findViewById(R.id.quickconnect_field);
		mPromptMessage = (TextView)findViewById(R.id.tv_prompt);
		mPromptMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
		mBtnBeginProxy = (Button)findViewById(R.id.tv_begin_proxy);
		mBtnBeginProxy.setOnClickListener(this);

		destEdit = (EditText)findViewById(R.id.portforward_destination);
		nickNameEdit = (EditText)findViewById(R.id.nickname);
		sourcePortEdit = (EditText)findViewById(R.id.portforward_source);

		mQuickConnectField.setText(mHost.toString());
		mQuickConnectField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyQuickConnectString(s.toString(), mHost.getProtocol());
            }
        });



		//约定好用同一个key,经测过,可以同时用.
		PubkeyBean pubkey = new PubkeyBean();
		PubkeyDatabase pubkeydb = PubkeyDatabase.get(ConfigActivity.this);
		List<PubkeyBean> list = pubkeydb.getPubkeysByNick(pubkey.getNickname());
		if(list.size() == 0) {
			Log.d(TAG,"成功存入数据库");
			message = "成功存入数据库";
			print(message);
			pubkeydb.savePubkey(pubkey);
		}else{
			message = "之前已存,不需添加";
			print(message);
			Log.d(TAG,"之前已存,不需添加");
		}

        mQuickConnectField.setText("root@103.27.79.138");
        nickNameEdit.setText("test5");
        destEdit.setText("192.168.13.100:1213");
        sourcePortEdit.setText("12141");
	}

	private void print(String message){
		EventBus.getDefault().postSticky(new MessageEvent(message));
	}
	private void applyQuickConnectString(String quickConnectString,String protocol){
		if (quickConnectString == null || protocol == null)
			return;
		Uri uri = TransportFactory.getUri(protocol, quickConnectString);
		if (uri == null) {
			// If the URI was invalid, null out the associated fields.
			Toast.makeText(this,getString(R.string.message_error_or_empty),Toast.LENGTH_LONG).show();
			return;
		}

		HostBean host = TransportFactory.getTransport(protocol).createHost(uri);
		mHost.setProtocol(host.getProtocol());
		mHost.setUsername(host.getUsername());
		mHost.setHostname(host.getHostname());
		mHost.setNickname(host.getNickname());
		mHost.setPort(host.getPort());
	}


	@Override
	public void onClick(View v) {

		String sourcePort = sourcePortEdit.getText().toString();
		if(sourcePort.length() == 0){
			Toast.makeText(this,getString(R.string.message_error_or_empty),Toast.LENGTH_LONG).show();
			return;
		}
		String destination = destEdit.getText().toString();
		if (destination.length() == 0) {
			Toast.makeText(this,getString(R.string.message_error_or_empty),Toast.LENGTH_LONG).show();
			return;
		}

		if(TextUtils.isEmpty(mHost.getHostname())||(TextUtils.isEmpty(mHost.getUsername()))){
			Toast.makeText(this,getString(R.string.message_error_or_empty),Toast.LENGTH_LONG).show();
			return;
		}
		 portForward = new PortForwardBean(
				mHost != null ? mHost.getId() : -1,
				nickNameEdit.getText().toString(),
				PortForwardBean.PORTFORWARD_REMOTE,
				sourcePort,
				destination);

		if(portForward != null && !TextUtils.isEmpty(portForward.getDescription())) {
			//开始绑定跟启动这个service,
			message = getString(R.string.proxy_config_enough);
			print(message);
			bindService(new Intent(this, TerminalManager.class), connection, Context.BIND_AUTO_CREATE);
		}
	}

	private ServiceConnection connection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			bound = ((TerminalManager.TerminalBinder)service).getService();

			bound.disconnectListener = ConfigActivity.this;

			Uri requested = mHost.getUri();
			final String requestedNickName = (requested != null)? requested.getFragment():null;

			hostBridge = bound.getConnectedBridge(requestedNickName);
			if(requestedNickName != null &&  hostBridge == null && portForward != null){
				try{
					Log.d(TAG, String.format("We couldnt find an existing bridge with URI=%s (nickname=%s), so " +
							"creating one now", requested.toString(), requestedNickName));
					message = "重新启动一个代理请求:"+requestedNickName;
					print(message);
					hostBridge = bound.openConnection(requested,portForward);
				}catch (Exception e){
					message = "Problem while trying to create new requested bridge from URI:"+e;
					print(message);
					Log.e(TAG, "Problem while trying to create new requested bridge from URI", e);
				}
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = null;
		}
	};

	@Override
	public void onDisconnected(TerminalBridge bridge) {
		if(bridge != null && bridge.isAwaitingClose()){
			finish();
		}
	}

	@Override
	public void onStop() {
		super.onStop();

        if(bound!=null) {
            unbindService(connection);
        }
	}

	@Subscribe
	public void onEvent(final MessageEvent event){

		runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (event != null) {
                    if (sb.length() == 0) {
                        sb.append(event.getSocketMessage());
                    } else {
                        sb.append("\r\n");
                        sb.append(event.getSocketMessage());
                    }
                    mPromptMessage.setText(sb);
                }
            }
        });

	}

    @Subscribe
    public void onEvent(final WaitForSocketEvent event){

        if(event != null) {
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new MyProxyServer().init();
                            Log.d("~~~:", "proxy server initiated.");
                        } catch (Exception e) {

                        }
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
