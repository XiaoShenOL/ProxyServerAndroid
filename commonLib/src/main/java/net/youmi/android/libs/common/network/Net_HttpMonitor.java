package net.youmi.android.libs.common.network;

import org.apache.http.Header;

/**
 * 用于收集http请求结果的相关数据
 * 
 * @author jen created on 2013-5-19
 * @author zhitaocai edit on 2014-5-14
 * @category 数据模型
 */
public class Net_HttpMonitor {

	/**
	 * Http码
	 */
	private int mHttpCode;

	/**
	 * 请求Http头
	 */
	Header[] requsetHeaders;

	/**
	 * 客户端异常码
	 */
	private int mClientException = Net_Client_Exception.NoException;

	/**
	 * 请求总花费的时间
	 */
	private long mCostTime_ms;

	/**
	 * http返回的ReasonPhrase
	 */
	private String mHttpReasonPhrase;

	/**
	 * 响应时间
	 */
	private long mResponseTime_ms = -1;

	/**
	 * 是否超时
	 */
	private boolean mIsTimeOut = false;

	/**
	 * 是否需要收集报头
	 * 
	 * @return
	 */
	private boolean mIsNeedToCollectHeaders = false;

	private Header[] responseHeads;

	public int getHttpCode() {
		return mHttpCode;
	}

	public void setHttpCode(int httpCode) {
		mHttpCode = httpCode;
	}

	public Header[] getRequestHeader() {
		return requsetHeaders;
	}

	public void setRequestHeader(Header[] headers) {
		this.requsetHeaders = headers;
	}

	public int getClientException() {
		return mClientException;
	}

	public void setClientException(int clientException) {
		mClientException = clientException;
	}

	public String getHttpReasonPhrase() {
		return mHttpReasonPhrase;
	}

	public void setHttpReasonPhrase(String httpReasonPhrase) {
		mHttpReasonPhrase = httpReasonPhrase;
	}

	public long getCostTime_ms() {
		return mCostTime_ms;
	}

	public void setCostTime_ms(long costTime_ms) {
		mCostTime_ms = costTime_ms;
	}

	public long getResponseTime_ms() {
		return mResponseTime_ms;
	}

	/**
	 * 设置请求返回时间 (ms)
	 * 
	 * @param responseTime_ms
	 */
	public void setResponseTime_ms(long responseTime_ms) {
		mResponseTime_ms = responseTime_ms;
	}

	public boolean isIsTimeOut() {
		return mIsTimeOut;
	}

	public void setIsTimeOut(boolean isTimeOut) {
		mIsTimeOut = isTimeOut;
	}

	public boolean isIsNeedToCollectHeaders() {
		return mIsNeedToCollectHeaders;
	}

	public void setIsNeedToCollectHeaders(boolean isNeedToCollectHeaders) {
		mIsNeedToCollectHeaders = isNeedToCollectHeaders;
	}

	public Header[] getResponseHeads() {
		return responseHeads;
	}

	public void setResponseHeads(Header[] httpHeads) {
		this.responseHeads = httpHeads;
	}

}
