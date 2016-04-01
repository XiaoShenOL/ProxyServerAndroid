package net.youmi.android.libs.common.network;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

/**
 * Created by jen on 13-5-19.
 */
public class Net_HttpStringRequestExecutor extends Net_HttpExecutor {

	private String mResult;
	private static String mTag = Net_HttpStringRequestExecutor.class.getSimpleName();

	public Net_HttpStringRequestExecutor(Context context, Net_HttpRequester requester) {
		super(context, requester);
	}

	private static String getStringFromContentInputStream(Context context,
	                                                      HttpResponse response, String charset, boolean isLogReqMsg) {
		ByteArrayOutputStream baos = null;
		InputStream inputStream = null;
//        GZIPInputStream gzipInputStream=null;
		try {
			if (response != null) {
				int httpCode = response.getStatusLine().getStatusCode();
				if (isLogReqMsg) {
					Debug_SDK.di(mTag, "Response http code is : %d", httpCode);
				}

				if (httpCode >= 200 && httpCode < 300) {

					HttpEntity entity = response.getEntity();

					try {
						if (isLogReqMsg) {
							Debug_SDK.di(mTag, "Response ContentLength : %d , ContentType : %s",
									entity.getContentLength(),
									entity.getContentType());
						} else {
							if (Debug_SDK.isNetLog) {
								Debug_SDK.de(mTag, "网络加载:length:%d,type:%s",
										entity.getContentLength(), entity.getContentType());
							}
						}

					} catch (Throwable e) {
						if (Debug_SDK.isNetLog) {
							Debug_SDK.de(mTag, e);
						}
					}

					try {
						Header header = entity.getContentEncoding();
						if (header != null) {

							if (header.getValue().toLowerCase(Locale.US).contains("gzip")) {
								inputStream = new GZIPInputStream(entity.getContent());

								if (Debug_SDK.isNetLog) {
									Debug_SDK.de(mTag, "网络加载，已经使用了gzip进行解压");
								}
							}

							if (Debug_SDK.isNetLog) {
								Debug_SDK.de(mTag, "网络加载: ContentEncoding key-> %s ,value -> %s  ",
										header.getName(), header.getValue());
							}
						} else {
							if (Debug_SDK.isNetLog) {
								Debug_SDK.de(mTag, "网络加载: ContentEncoding is null ");
							}
						}

					} catch (Throwable e) {
						if (Debug_SDK.isNetLog) {
							Debug_SDK.de(mTag, e);
						}
					}

					if (inputStream == null) {
						// 表示没有使用gzip
						inputStream = entity.getContent();
					}

					if (inputStream == null) {
						if (isLogReqMsg) {
							Debug_SDK.de(mTag, "Response Content is null");
						}
						return null;
					}

					baos = new ByteArrayOutputStream();

					byte[] buff = new byte[1024];

					int len = 0;

					while ((len = inputStream.read(buff)) > 0) {
						baos.write(buff, 0, len);
					}

					baos.flush();
					byte[] buffer = baos.toByteArray();

					String rspString = new String(buffer, charset);

					if (Debug_SDK.isNetLog) {
						Debug_SDK.de(mTag, "网络请求结果:length:%d,%s", buffer.length, rspString);
					}
					return rspString;
				}
			} else {
				if (isLogReqMsg) {
					Debug_SDK.di(mTag, "Request failed , reponse is null"); // 没有response
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.de(mTag, e);
			}
		} finally {

			try {
				if (baos != null) {
					baos.close();
				}

			} catch (Throwable e2) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.de(mTag, e2);
				}
			}

			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Throwable e2) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.de(mTag, e2);
				}
			}
		}

		return null;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		//设置gzip头启用压缩
		mRequest.addHeader("Accept-Encoding", "gzip");
	}

	@Override
	protected void handleResponse(HttpResponse response) {
		//获取字符串
		boolean isLogReqMsg = mHttpRequest != null && mHttpRequest.isShowReqMsgInLog();
		mResult = getStringFromContentInputStream(mApplicationContext, response, mHttpRequest.getEncodingCharset(),
				isLogReqMsg);
	}

	/**
	 * 获取加载的结果
	 *
	 * @return
	 */
	public String getResult() {
		return mResult;
	}
}
