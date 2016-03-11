package com.android.sms.proxy.loader;

/**
 * @author zyq 16-3-10
 */

import com.android.sms.proxy.entity.JsonBase;
import com.oplay.nohelper.assist.RequestManager;
import com.oplay.nohelper.volley.RequestEntity;
import com.oplay.nohelper.volley.Response;
import com.oplay.nohelper.volley.toolbox.JsonRequest;

/**
 * volley封装出来的common类
 *
 * @author zyq 15-6-6
 * @since VERSION
 */
public class Loader_Base_ForCommon<ProtocolJson extends JsonBase> {

	private volatile static Loader_Base_ForCommon instance;

	public static Loader_Base_ForCommon getInstance() {
		if (instance == null) {
			synchronized (Loader_Base_ForCommon.class) {
				if (instance == null) {
					instance = new Loader_Base_ForCommon();
				}
			}
		}
		return instance;
	}

	/**
	 * 对于常用请求，一般不用缓存
	 *
	 * @param entity
	 * @param isReset
	 * @param listener
	 * @param errorListener
	 */
	@SuppressWarnings("unchecked")
	public void onRequestLoadNetworkTask(final RequestEntity<ProtocolJson> entity,
	                                     final boolean isReset, final Response.Listener listener,
	                                     final Response.ErrorListener errorListener) {
		final RequestEntity requestEntity = entity;
		if (requestEntity == null) return;
		if (requestEntity != null) {
			requestEntity.setShouldCache(false);
		}
		JsonRequest jsonRequest = new JsonRequest<ProtocolJson>(entity, listener, errorListener) {
			@Override
			protected ProtocolJson fromJson(String json, Class<ProtocolJson> classOfT) {
				return HttpDataLoader.fromJson(json, classOfT);
			}
		};
		jsonRequest.setReset(isReset);
		RequestManager.getInstance().addRequest(jsonRequest, null);
	}
}
