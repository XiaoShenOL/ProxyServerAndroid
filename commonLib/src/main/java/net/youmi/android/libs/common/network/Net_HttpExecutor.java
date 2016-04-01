package net.youmi.android.libs.common.network;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Charsets;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.entity.ByteArrayEntity;

import java.util.List;

/**
 * 网络请求基础抽象类，需要子类继承实现相关方法:
 * <p>
 * {@link code onPreExecute()} 实现请求之前的勾子 <br/>
 * {@link code handleResponse(HttpResponse)} 处理请求结果
 * </p>
 * 可以参考{@link code Net_HttpStringRequestExecutor} 的实现
 * 
 * @author jen created on 2013-5-19
 * @author zhitaocai edit on 2014-5-14
 */
public abstract class Net_HttpExecutor {

	protected Context mApplicationContext;
	protected HttpUriRequest mRequest;
	protected HttpClient mHttpClient;
	protected Net_HttpMonitor mMonitor;
	protected Net_HttpRequester mHttpRequest;
	protected long mStartTime_ms = 0L;

	/**
	 * 请求起始时间（ms）
	 * 
	 * @return
	 */
	public long getStartTime() {
		return mStartTime_ms;
	}

	public HttpUriRequest getRequest() {
		return mRequest;
	}

	public void setRequest(HttpUriRequest request) {
		mRequest = request;
	}

	public void setHttpRequester(Net_HttpRequester httpRequest) {
		mHttpRequest = httpRequest;
	}

	public HttpClient getHttpClient() {
		return mHttpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		mHttpClient = httpClient;
	}

	public void setUserAgent(String userAgent) {
		Util_Network_HttpUtil.setUserAgent(userAgent);
	}

	public Net_HttpMonitor getMonitor() {
		return mMonitor;
	}

	public void setMonitor(Net_HttpMonitor monitor) {
		this.mMonitor = monitor;
	}

	public Net_HttpExecutor(Context context, Net_HttpRequester requester) throws NullPointerException {
		if (context == null || requester == null) {
			throw new NullPointerException();
		}
		mApplicationContext = context.getApplicationContext();
		mHttpRequest = requester;
	}

	/**
	 * 执行网络请求
	 */
	public synchronized void execute() {

		long startTime_ms = System.currentTimeMillis();
		mStartTime_ms = startTime_ms;

		try {
			HttpResponse rsp = executeHttp();
			try {
				monitorResponse(rsp, startTime_ms);
			} catch (Throwable e) {
			}
			handleResponse(rsp); // 处理返回结果
			return;
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, this, e);
			}
		} finally {

			// 清除请求
			try {
				if (mRequest != null) {
					mRequest.abort();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}

			}
			mRequest = null;

			// 关闭连接
			try {
				if (mHttpClient != null) {
					mHttpClient.getConnectionManager().shutdown();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}
			mHttpClient = null;

			// 统计请求时间
			try {
				if (mMonitor != null) {
					long endTime_ms = System.currentTimeMillis();
					long costTime_ms = endTime_ms - startTime_ms;
					mMonitor.setCostTime_ms(costTime_ms);
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}
		}
	}

	/**
	 * 监听并对请求结果进行记录相关数据
	 * 
	 * @param response
	 * @param startTime_ms
	 */
	private void monitorResponse(HttpResponse response, long startTime_ms) {

		if (response == null) {
			return;
		}

		Net_HttpMonitor monitor = mMonitor;
		if (monitor == null) {
			return;
		}

		long endTime_ms = System.currentTimeMillis();
		long costTime_ms = endTime_ms - startTime_ms;
		monitor.setResponseTime_ms(costTime_ms);

		int httpCode = response.getStatusLine().getStatusCode();
		String reasonPhrase = response.getStatusLine().getReasonPhrase();
		monitor.setHttpCode(httpCode);
		monitor.setHttpReasonPhrase(reasonPhrase);

		if (monitor.isIsNeedToCollectHeaders()) {
			monitor.setRequestHeader(mRequest.getAllHeaders());
			Header[] headers = response.getAllHeaders();
			monitor.setResponseHeads(headers);
		}

	}

	/**
	 * 执行http请求
	 * 
	 * @return
	 */
	private HttpResponse executeHttp() {

		try {
			if (mHttpClient == null) {
				mHttpClient = Util_Network_HttpUtil.createHttpClient(mApplicationContext);
			}
			// request
			if (mRequest == null) {
				if (mHttpRequest.getPostDataNameValuePair() != null && mHttpRequest.getPostDataNameValuePair().size() > 0) {
					// Post NameValuePair
					HttpPost post = new HttpPost(mHttpRequest.getRequestUrl());
					List<NameValuePair> postData = mHttpRequest.getPostDataNameValuePair();
					String charset = mHttpRequest.getEncodingCharset() != null ? mHttpRequest.getEncodingCharset()
							: Global_Charsets.UTF_8;
					HttpEntity httpEntity = new UrlEncodedFormEntity(postData, charset);
					post.setEntity(httpEntity);
					mRequest = post;
				}

				else if (mHttpRequest.getPostDataByteArray() != null && mHttpRequest.getPostDataByteArray().length > 0) {
					// Posts byte[]
					HttpPost post = new HttpPost(mHttpRequest.getRequestUrl());
					post.setEntity(new ByteArrayEntity(mHttpRequest.getPostDataByteArray()));
					mRequest = post;

				} else {
					// Get
					mRequest = new HttpGet(mHttpRequest.getRequestUrl());
				}
			}

			try {
				// request headers
				List<Header> reqHeaders = mHttpRequest.getRequestHeaders();
				if (reqHeaders != null && reqHeaders.size() > 0) {
					for (int i = 0; i < reqHeaders.size(); i++) {
						mRequest.addHeader(reqHeaders.get(i));
					}
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}

			try {
				// pre execute
				onPreExecute();// 在连接之前，勾子
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}

			HttpResponse response = mHttpClient.execute(mRequest);
			return response;
		} catch (ConnectionPoolTimeoutException e) {

			// 从ConnectionManager管理的连接池中取出连接超时
			monitorException(Net_Client_Exception.ConnectionPoolTimeoutException);

		} catch (ConnectTimeoutException e) {

			// 网络与服务器建立连接超时
			monitorException(Net_Client_Exception.ConnectTimeoutException);

		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, this, e);
			}
		}
		return null;
	}

	/**
	 * 监听http请求exception
	 * 
	 * @param exceptionCode
	 *            {@link Net_Client_Exception}
	 */
	protected void monitorException(int exceptionCode) {
		try {
			if (mMonitor != null) {
				mMonitor.setClientException(exceptionCode);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, this, e);
			}
		}
	}

	/**
	 * 勾子:在连接之前
	 * <p>
	 * 子类重写
	 * </p>
	 */
	protected void onPreExecute() {
	}

	/**
	 * 处理请求结果
	 * 
	 * @param response
	 */
	protected abstract void handleResponse(HttpResponse response);

}
