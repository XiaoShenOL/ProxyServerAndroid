package net.youmi.android.libs.common.download.basic;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.network.Util_Network_HttpUtil;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * 单纯地对执行下载文件任务,不做逻辑,只返回本次下载的结果
 *
 * @author zhitaocai edit on 2014-5-26
 * @author zhitaocai edit on 2014-7-16
 */
public class BasicFileDownloader {

	/**
	 * 每次读取2KB的流再写入
	 */
	final static int BUFFER_SIZE = 2048;

	/**
	 * 完成下载
	 */
	public final static int STATUS_OK = 0;

	/**
	 * 遇到http错误码，不可重试
	 */
	public final static int STATUS_ERROR_HTTP = -1;

	/**
	 * 错误参数，不可重试
	 */
	public final static int STATUS_ERROR_PARAMS = -2;

	/**
	 * 用户终止下载流程，不可重试(也可以理解为用户暂定了下载任务)
	 */
	public final static int STATUS_STOP = -3;

	/**
	 * 发生了异常，允许重试机制保证重试次数的情况下进行重试
	 */
	public final static int STATUS_EXCEPTION = -4;

	/**
	 * 已经完成的长度。<br/>
	 * 注意，在该值大于0时并且下载任务指定了起始点(即mStart_set大于0)，在RandomAccessFile设置起始点的时候， 必须是mCompleteLength+mStart_set
	 */
	private long mCompleteLength = 0;

	/**
	 * 文件的原始长度，有可能为0。<br/>
	 * 当下载的任务指定的是没有结束点时，可以计算出contentLength。<br/>
	 * 否则是无法计算contentLength的。<br/>
	 * mContentLength= mCompleteLength+currentContentLength
	 */
	private long mContentLength = 0;

	/**
	 * 下载任务是否正在进行中
	 */
	private boolean mIsRunning = true;

	/**
	 * 最终文件url，将接受InputStream后存为文件
	 */
	private String mFileUrl;

	/**
	 * 下载文件File
	 */
	private File mDestFile;

	private Context mContext;

	/**
	 *
	 * @param context
	 * @param fileUrl
	 *            文件下载地址
	 * @param destFile
	 *            保存的本地文件File
	 * @param start
	 *            下载起点
	 */
	public BasicFileDownloader(Context context, String fileUrl, File destFile, long start) {
		mContext = context.getApplicationContext();
		mFileUrl = fileUrl;
		if (start >= 0) {
			mCompleteLength = start;
		}
		mDestFile = destFile;
	}

	public BasicFileDownloader(Context context, String fileUrl, File destFile) {
		this(context, fileUrl, destFile, 0);
	}

	/**
	 * 下载文件
	 *
	 * @return
	 */
	public int downloadToFile() {

		RandomAccessFile accessFile = null;
		HttpGet get = null;
		DefaultHttpClient client = null;
		InputStream inputStream = null;

		try {

			if (mFileUrl == null) {
				return STATUS_ERROR_PARAMS;
			}
			if (mDestFile == null) {
				return STATUS_ERROR_PARAMS;
			}

			// 标记为开始
			mIsRunning = true;

			// 是否使用断点续传

			client = Util_Network_HttpUtil.createHttpClient(mContext);

			get = new HttpGet(mFileUrl);


			long start = mCompleteLength;// 两个都已经保证至少为0

			// 如果起始点大于0，即指定了下载起始点，需要配置块下载——起始至文件结束
			if (start > 0) {
				get.setHeader("RANGE", String.format("bytes=%d-", start));
			}

			//这里我们禁用了302
			HttpParams params=client.getParams();
			params.setParameter(ClientPNames.HANDLE_REDIRECTS,false);

			// 注：如果起始点为0,而结束点没有指定，则说明是从文件头部开始下载整个文件，因此不指定RANGE
			HttpResponse response = client.execute(get);

			int code = response.getStatusLine().getStatusCode();

			// http状态码错误， 结束
			if (code < 200 || code >= 300) {
				return STATUS_ERROR_HTTP;
			}

			long currentDestLength = response.getEntity().getContentLength();
			// 这里特别解释一下：
			// response.getEntity().getContentLength()拿到的是本次http请求的文件部分长度。
			// 而可能本次http请求之前就已经下载了其他一部分数据(mCompleteLength)
			// 因此mTaskDestLength是两者相加的。

			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this,
						"本次下载信息：\n * httpCode：%d\n * 目标文件：%s\n * 文件当前长度：%d\n * 起始下载位置：%d\n * 需要下载的长度：%d", code,
						mDestFile.getAbsolutePath(), mDestFile.length(), start, currentDestLength);
			}
			// --处理使用RANGE但code为200的情况
			if (code == HttpStatus.SC_OK && start > 0) {
				// 断点下载之前，需要再判断一下文件长度，是否和start一致，确保文件没有被篡改
				if (mDestFile.length() != start) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "断点下载链接成功：\n文件当前长度 != 本次下载的起点\n下载文件有误，准备删除文件%s",
								mDestFile.getAbsolutePath());
					}
					if (mDestFile.delete()) {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "删除成功");
						}
					} else {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "删除失败");
						}
					}
					// 接下来会继续下载
				}
			}

			try {
				// 如果是从0开始下载，并且文件存在，这时候如果文件已经存在的长度比服务器返回的文件总长度还大，并且没有指定结尾
				// 则说明旧文件已经被写入内容，即使重新从0写入，最终得到的文件也不可能与原文件一致，
				// 因此需要删除文件
				if (start == 0 && mDestFile.exists() && mDestFile.isFile() && (mDestFile.length() > currentDestLength)) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "下载文件又被篡改的嫌疑，准备删除文件%s", mDestFile.getAbsolutePath());
					}
					if (mDestFile.delete()) {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "删除成功");
						}
					} else {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "删除失败");
						}
					}
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}

			// 设置文件的写入起点
			accessFile = new RandomAccessFile(mDestFile, "rw");
			accessFile.seek(start);

			// 文件最终的长度= 当前文件长度 + 本次下载长度
			mContentLength = mCompleteLength + currentDestLength;

			inputStream = response.getEntity().getContent();
			byte[] buff = new byte[BUFFER_SIZE];
			int len = 0;

			while (mIsRunning && ((len = inputStream.read(buff)) > 0)) {
				accessFile.write(buff, 0, len);
				mCompleteLength += len;
			}

			if (mContentLength == mCompleteLength) {
				// 这样保证是没问题的
				return STATUS_OK;
			}

			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, "下载被停止！\n * 下载文件：%s\n * 下载url：%s\n最后完成的进度百分比：%d",
						mDestFile.getAbsolutePath(), mFileUrl, getPercent());
			}

			// 可能被主动停止或者是网络原因而被终止了
			return STATUS_STOP;

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		} finally {
			mIsRunning = false;// 这里必须标记为停止
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
			try {
				if (accessFile != null) {
					accessFile.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}

			try {
				if (get != null) {
					get.abort();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}

			try {
				if (client != null) {
					client.getConnectionManager().shutdown();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}


		}
		return STATUS_EXCEPTION;
	}

	/**
	 * 获取已经完成的长度
	 *
	 * @return
	 */
	public long getCompleteLength() {
		return mCompleteLength;
	}

	/**
	 * 获取下载进度的百分比
	 *
	 * @return
	 */
	public int getPercent() {
		long contentLength = mContentLength;
		if (contentLength > 0) {
			long completeLength = mCompleteLength;
			return (int) ((completeLength * 100) / contentLength);
		}
		return 0;
	}

	/**
	 * 获取本次 加载的总长度
	 *
	 * @return
	 */
	public long getContentLength() {
		return mContentLength;
	}

	/**
	 * 停止下载过程
	 */
	public void stop() {
		mIsRunning = false;
	}

	/**
	 * 是否正在下载中
	 *
	 * @return
	 */
	public boolean isRunning() {
		return mIsRunning;
	}

}
