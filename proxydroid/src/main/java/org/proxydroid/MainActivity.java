//package org.proxydroid;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.os.StrictMode;
//import android.text.method.ScrollingMovementMethod;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import com.android.proxy.client.GlobalProxyUtil;
//
//
//public class MainActivity extends Activity implements View.OnClickListener {
//
//	private TextView mTvShow;
//	private StringBuilder oldMsg;
//	private EditText mEdtPort;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		findViewById(R.id.connect).setOnClickListener(this);
//		mTvShow = (TextView) findViewById(R.id.tv_socket_message);
//		mEdtPort = (EditText) findViewById(R.id.port);
//		oldMsg = new StringBuilder();
//		if (android.os.Build.VERSION.SDK_INT > 9) {
//			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//			StrictMode.setThreadPolicy(policy);
//		}
//		mTvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
//		GlobalProxyUtil.getInstance(getApplicationContext());
//	}
//
//
//	@Override
//	public void onClick(View v) {
//		try {
//			String portStr = mEdtPort.getText().toString();
//			int port = Integer.valueOf(portStr);
//			String host = "103.27.79.138";
//			GlobalProxyUtil.getInstance(getApplicationContext()).startProxy(host, port);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//
//
//
//
//}
