package com.oplay.nohelper.volley;//package com.oplay.nohelper.volley;
//
//import android.text.TextUtils;
//
//import com.oplay.nohelper.volley.toolbox.HttpHeaderParser;
//
//import net.youmi.android.libs.common.debug.Debug_SDK;
//
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.FileBody;
//import org.apache.http.entity.mime.content.StringBody;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.nio.charset.Charset;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public abstract class MultipartRequest<T> extends Request<T> {
//
//	private MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//
//	private final Response.Listener<T> mListener;
//	private static final String TAG = "multipartRequest";
//	private final RequestEntity<T> mRequestEntity;
//	private static final boolean DEBUG = true;
//
//	private List<File> mFileParts;
//	private String mFilePartName;
//	private Map<String, String> mParams;
//
//	/**
//	 * 单个文件
//	 *
//	 * @param url
//	 * @param errorListener
//	 * @param listener
//	 * @param filePartName
//	 * @param file
//	 * @param params
//	 */
//	public MultipartRequest(RequestEntity<T> requestEntity, Response.ErrorListener errorListener,
//	                        Response.Listener<T> listener, String filePartName, File file) {
//		super(Method.POST, requestEntity.getUrl(), errorListener, false);
//		this.mRequestEntity = requestEntity;
//		mFileParts = new ArrayList<File>();
//		if (file != null) {
//			mFileParts.add(file);
//		}
//		mFilePartName = filePartName;
//		mListener = listener;
//		mParams = requestEntity.getPostParams();
//		buildMultipartEntity();
//	}
//
//	/**
//	 * 多个文件，对应一个key
//	 *
//	 * @param url
//	 * @param errorListener
//	 * @param listener
//	 * @param filePartName
//	 * @param files
//	 * @param params
//	 */
//	public MultipartRequest(RequestEntity<T> requestEntity, Response.ErrorListener errorListener,
//	                        Response.Listener<T> listener, String filePartName,
//	                        List<File> files) {
//		super(Method.POST, requestEntity.getUrl(), errorListener, false);
//		this.mRequestEntity = requestEntity;
//		mFilePartName = filePartName;
//		mListener = listener;
//		mFileParts = files;
//		mParams = requestEntity.getPostParams();
//		buildMultipartEntity();
//	}
//
//	private void buildMultipartEntity() {
//
//		if (mFileParts != null && mFileParts.size() > 0) {
//			for (File file : mFileParts) {
//				entity.addPart(mFilePartName, new FileBody(file));
//			}
//			long l = entity.getContentLength();
//			if (DEBUG) {
//				Debug_SDK.dd(TAG, "文件多少个:%s,长度:%s", mFileParts.size(), l);
//			}
//		}
//
//		try {
//			if (mParams != null && mParams.size() > 0) {
//				for (Map.Entry<String, String> entry : mParams.entrySet()) {
//					if (!TextUtils.isEmpty(entry.getValue())) {
//						entity.addPart(
//								entry.getKey(),
//								new StringBody(entry.getValue(), Charset
//										.forName("UTF-8")));
//					}
//				}
//			}
//		} catch (UnsupportedEncodingException e) {
//			VolleyLog.e("UnsupportedEncodingException");
//		}
//	}
//
//	@Override
//	public String getBodyContentType() {
//		return entity.getContentType().getValue();
//	}
//
//	@Override
//	public byte[] getBody() throws AuthFailureError {
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		try {
//			entity.writeTo(bos);
//		} catch (IOException e) {
//			VolleyLog.e("IOException writing to ByteArrayOutputStream");
//		}
//		return bos.toByteArray();
//	}
//
//	@Override
//	protected Response<T> parseNetworkResponse(NetworkResponse response) {
//		if (DEBUG) {
//			Debug_SDK.dd(TAG, "%s", "parseNetworkResponse");
//		}
//		if (VolleyLog.DEBUG) {
//			if (response.headers != null) {
//				for (Map.Entry<String, String> entry : response.headers
//						.entrySet()) {
//					VolleyLog.d(entry.getKey() + "=" + entry.getValue());
//				}
//			}
//		}
//
//		String parsed;
//		try {
//			parsed = new String(response.data,
//					HttpHeaderParser.parseCharset(response.headers));
//		} catch (UnsupportedEncodingException e) {
//			parsed = new String(response.data);
//		}
//		return Response.success(fromJson(parsed, mRequestEntity.getClassOfT()),
//				HttpHeaderParser.parseCacheHeaders(response));
//	}
//
//
//	/*
//	 * (non-Javadoc)
//	 *
//	 * @see com.android.volley.Request#getHeaders()
//	 */
//	@Override
//	public Map<String, String> getHeaders() throws AuthFailureError {
//		VolleyLog.d("getHeaders");
//		Map<String, String> headers = super.getHeaders();
//
//		if (headers == null || headers.equals(Collections.emptyMap())) {
//			headers = new HashMap<String, String>();
//		}
//
//
//		return headers;
//	}
//
//	@Override
//	protected void deliverResponse(T response) {
//		mListener.onResponse(response);
//	}
//
//	protected abstract T fromJson(String json, Class<T> classOfT);
//}
