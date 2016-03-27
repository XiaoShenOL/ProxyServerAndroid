package com.android.sms.proxy.service;

import com.android.sms.proxy.entity.HeartBeatInfo;
import com.android.sms.proxy.entity.HeartBeatJson;
import com.android.sms.proxy.entity.NativeParams;
import com.oplay.nohelper.loader.Callable_Loader_Base_ForCommon;
import com.oplay.nohelper.volley.RequestEntity;
import com.oplay.nohelper.volley.Response;
import com.oplay.nohelper.volley.VolleyError;

/**
 * @author zyq 16-3-9
 */
public class HeartBeatCallable extends Callable_Loader_Base_ForCommon<HeartBeatJson> {

	private static Response.Listener<HeartBeatJson>  mSuccessListener = new Response.Listener<HeartBeatJson>() {
		@Override
		public void onResponse(HeartBeatJson response) {
          if(response != null){
	          int code = response.getCode();
	          if(code == NativeParams.SUCCESS){
		          HeartBeatInfo info = response.getData();
		          if(info != null){
			          int type = info.getStatusType();
			          switch (type){
				          case HeartBeatInfo.TYPE_IDLE:
					          break;
				          case HeartBeatInfo.TYPE_START_SSH:
					          break;
				          case HeartBeatInfo.TYPE_WAITING_SSH:
					          break;
				          case HeartBeatInfo.TYPE_BUILD_SSH_SUCCESS:
					          break;
				          case HeartBeatInfo.TYPE_CLOSE_SSH:
					          break;
			          }
		          }
	          }
          }
		}
	};


	private static Response.ErrorListener mErrorListener = new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {

		}
	};



	public HeartBeatCallable(RequestEntity<HeartBeatJson> entity,boolean isReset,String tag){
		super(entity,isReset,tag,true);
	}

	@Override
	public HeartBeatJson initDebugData() {
		HeartBeatJson heartBeatJson = new HeartBeatJson();
        heartBeatJson.setCode(NativeParams.SUCCESS);
		HeartBeatInfo info = new HeartBeatInfo();
		info.setStatusType(0);
		return heartBeatJson;
	}


}
