package com.oplay.nohelper.loader;


import com.oplay.nohelper.assist.RequestManager;
import com.oplay.nohelper.assist.bolts.Capture;
import com.oplay.nohelper.assist.bolts.Task;
import com.oplay.nohelper.assist.debug.Debug_SDK;
import com.oplay.nohelper.assist.debug.Util_System_Runtime;
import com.oplay.nohelper.entity.JsonBase;
import com.oplay.nohelper.volley.RequestEntity;
import com.oplay.nohelper.volley.Response;
import com.oplay.nohelper.volley.VolleyError;
import com.oplay.nohelper.volley.toolbox.JsonRequest;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 配合bolts使用
 * ProtocolJson 主要是为了辨别json基类,而 T 是区分
 *
 * @author zyq 15-10-20
 */
public class Callable_Loader_Base_ForCommon<T extends JsonBase> implements
		Callable<Task<T>>, Response.Listener<T>, Response.ErrorListener {

	protected boolean DEBUG = false;
	private final static String TAG = "Callable";

	//用于等待当前是否完成.
	private final Capture<Boolean> waitingToResponse = new Capture<>(false);
	private final Object responseLock = new Object();
	protected AtomicBoolean mShouldUpdateFromNet = new AtomicBoolean(true);
	protected RequestEntity<T> mEntity;
	protected String mTag;

	protected Response.ErrorListener mErrorListener;
	protected Response.Listener mListener;
	protected JsonRequest mJsonRequest;
	protected VolleyError mError;
	protected T data;
	protected T dataTmp;


	public Callable_Loader_Base_ForCommon(RequestEntity<T> entity, String tag, boolean isDebug) {
		this(entity, true, tag, null, null, isDebug);
	}

	public Callable_Loader_Base_ForCommon(RequestEntity<T> entity, String tag, Response.ErrorListener errorListener,
	                                      boolean isDebug) {
		this(entity, true, tag, null, errorListener, isDebug);
	}

	public Callable_Loader_Base_ForCommon(RequestEntity<T> entity,
	                                      boolean isReset, String tag, boolean isDebug) {
		this(entity, isReset, tag, null, null, isDebug);
	}

	public Callable_Loader_Base_ForCommon(RequestEntity<T> entity,
	                                      boolean isReset, String tag, Response.ErrorListener errorListener, boolean
			                                      isDebug) {
		this(entity, isReset, tag, null, errorListener, isDebug);
	}

	public Callable_Loader_Base_ForCommon(RequestEntity<T> entity,
	                                      boolean isReset, String tag, Response.Listener listener,
	                                      Response.ErrorListener errorListener, boolean isDebug) {
		this.mEntity = entity;
		this.mShouldUpdateFromNet.set(isReset);
		this.mListener = listener;
		this.mErrorListener = errorListener;
		this.mTag = tag;
		this.DEBUG = isDebug;
	}


	public void setRequestEntity(RequestEntity<T> entity) {
		this.mEntity = entity;
	}

	/**
	 * 如果我们想要cancel某个request,我们只要在外部通过requestManager.cancel(),
	 * todo 添加volley 对request.cancel()的回调处理
	 * 而对于这个task 想要cancel,直接通过CancellationTokenSource
	 * 所以我们应该要忽略本身task的返回值,直接执行接下来的任务.
	 */
	@Override
	public Task<T> call() throws Exception {
		if (DEBUG) {
			Thread.currentThread().sleep(1000);
			if (DEBUG) Debug_SDK.dd(TAG, "%s", "暂停2秒钟");
			data = initDebugData();
			if (data == null) {
				data = dataTmp;
			}
			Util_System_Runtime.getInstance().runInUiThread(new Runnable() {
				@Override
				public void run() {
					onResponse(data);
				}
			});
		} else {
			onRequestLoadNetworkTask(mEntity, mShouldUpdateFromNet.get(), this, this, mTag);
		}
		//由于volley对取消后request没有反馈,所以这里需要解决request被cancel的问题.
		//后面考虑把volley对于cancel的请求给个回调
		synchronized (responseLock) {//获取lock
			waitingToResponse.set(true);
			responseLock.wait();//该线程释放了lock
		}
		if (DEBUG) Debug_SDK.dd(TAG, "%s", data);
		if (getError() != null) {
			if (DEBUG) Debug_SDK.dd(TAG, "%s", "出错了还没有报错");
			return Task.forError(getError());
		}
		//这里data 可能是json基类,可能为null(ps:可能task取消,调用reset(),可能request.cancel())
		if (DEBUG) Debug_SDK.dd(TAG, "结果是%s", data);
		return Task.forResult(data);
	}

	/**
	 * 当我们想要重新初始化时候,我们需要重置状态
	 */
	public void reset() {
		data = null;
		mError = null;
		if (mJsonRequest != null) {
			mJsonRequest.cancel();
		}
		synchronized (responseLock) {
			if (waitingToResponse.get()) {
				responseLock.notify();
			}
		}
		waitingToResponse.set(false);
		mShouldUpdateFromNet.set(true);
	}

	public VolleyError getError() {
		return mError;
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
	public void onRequestLoadNetworkTask(final RequestEntity<T> entity,
	                                     final boolean isReset, final Response.Listener listener,
	                                     final Response.ErrorListener errorListener,
	                                     final String tag) {
		mError = null;
		final RequestEntity requestEntity = entity;
		if (requestEntity == null) return;
		mJsonRequest = new JsonRequest<T>(entity, listener, errorListener) {
			@Override
			protected T fromJson(String json, Class<T> classOfT) {
				return HttpDataLoader.fromJson(json, classOfT);
			}
		};
		mJsonRequest.setReset(isReset);//表示是否刷新该数据
		if (requestEntity.isDelCache()) {//默认是为false,本身已经有LRU机制,所以没必要.
			RequestManager.getInstance().getRequestQueue().getVolleyConfiguration().diskCache.remove(mJsonRequest
					.getCacheKey());
			RequestManager.getInstance().getRequestQueue().getVolleyConfiguration().memoryCache.remove
					(mJsonRequest.getCacheKey());
		}
		RequestManager.getInstance().addRequest(mJsonRequest, tag);
	}

	/**
	 * 这里其实可以mListener,是为了解决listener多层嵌套,这样最外层同样接受到该消息,而且是为了主要解决UI上的更新
	 * 我们一般可以直接用task.success之类来解决相关问题.
	 *
	 * @param response
	 */
	@Override
	public void onResponse(T response) {
		if (DEBUG) {
			Debug_SDK.dd(TAG, "状态:%s ,结果:%s", "即将释放锁", response);
		}
		data = response;
		if (mListener != null) {
			mListener.onResponse(response);
		}
		synchronized (responseLock) {//获取该锁
			if (waitingToResponse.get()) {
				responseLock.notify();//唤醒了,但另外线程还是不能获取锁,直到结束整个synchronized()
				waitingToResponse.set(false);
			}
		}
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		if (DEBUG) {
			Debug_SDK.dd(TAG, "状态:%s ,结果:%s", "即将释放锁", error.toString());
		}
		mError = error;
		if (mErrorListener != null) {
			mErrorListener.onErrorResponse(error);
		}
		synchronized (responseLock) {
			if (waitingToResponse.get()) {
				responseLock.notify();
				waitingToResponse.set(false);
			}
		}
	}

	public T initDebugData() {
		return null;
	}

	public void setDebugData(T debugData) {
		this.dataTmp = debugData;
	}
}
