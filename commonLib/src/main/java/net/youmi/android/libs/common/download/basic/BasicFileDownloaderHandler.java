package net.youmi.android.libs.common.download.basic;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.FileDownloadConfig;
import net.youmi.android.libs.common.network.Util_Network_Status;

import java.io.File;

/**
 * 封装文件下载并且对文件下载handler做逻辑处理，如设置下载重试次数,网络问题重试机制,文件先下载到缓存文件等等
 * <p>
 * 调用到文件下载类{@link BasicFileDownloader}进行文件下载
 * 
 * @author zhitaocai edit on 2014-5-27
 * @author zhitaocai edit on 2014-7-16
 * 
 */
public class BasicFileDownloaderHandler implements Runnable {

	/**
	 * 网络不可用的情况下，下一次重试的等待时间
	 */
	private final static int NETWORK_RETRY_DELAY_MS = FileDownloadConfig.NETWORK_RETRY_DELAY_MS;

	/**
	 * 网络可用情况下可以进行最大请求的次数
	 */
	private int mMaxRetryTimes = FileDownloadConfig.MAX_RETRY_TIMES_NETWORK_AVAILABLE;

	/**
	 * 网络不可用情况下可以进行最大请求的次数
	 */
	private int mMaxRetryTimes_Network_Unavailable = FileDownloadConfig.MAX_RETRY_TIMES_NETWORK_UNAVAILABLE;

	/**
	 * 目标存储文件
	 */
	private File mDestFile;

	/**
	 * 缓存文件，先下载在这里，ok才复制到目标存储文件 mDestFile
	 */
	private File mTempFile;

	/**
	 * 是否正在运行
	 */
	private boolean mIsRunning = false;

	/**
	 * 下载结果状态：（默认为0，标识暂时没有下载结果，即可能还在运行中）
	 * <p>
	 * 1、下载成功<br>
	 * 2、下载被停止（一般是调用stopdownload之后到这里）<br>
	 * 3、下载失败（一般是网络问题，参数错误，httperrorcode或者文件读取不了等引起）
	 */
	private int mDownloadResultStatus = 0;
	/**
	 * 文件基本下载类
	 */
	private BasicFileDownloader mDownloader;

	/**
	 * 计数器-网络可用情况下已经进行过请求的总次数
	 */
	private int mRunCounter = 0;

	/**
	 * 计数器-网络不可用情况下可以进行请求的次数
	 */
	private int mRunCounter_Network_Unavailable = 0;

	private Context mApplicationContext;

	/**
	 * 
	 * @param context
	 * @param destUrl
	 *            下载地址url
	 * @param startByte
	 *            下载起始点
	 * @param tempFile
	 *            必须保证与sotreFile同一目录
	 * @param storeFile
	 *            必须保证与tempFile同一目录
	 * @param maxRetryTimes_NetWork_Available
	 *            网络可用情况下，可以进行重新尝试下载上限次数，默认为8
	 * @param maxRetryTimes_NetWork_Unavailable
	 *            网络不可用情况下，可以进行重新尝试下载上限次数，默认为10
	 */
	public BasicFileDownloaderHandler(Context context, String destUrl, long startByte, File tempFile, File storeFile,
			int maxRetryTimes_NetWork_Available, int maxRetryTimes_NetWork_Unavailable) {
		mApplicationContext = context.getApplicationContext();
		mDestFile = storeFile;
		mTempFile = tempFile;
		mDownloader = new BasicFileDownloader(mApplicationContext, destUrl, mTempFile, startByte);
		if (maxRetryTimes_NetWork_Available > 0) {
			mMaxRetryTimes = maxRetryTimes_NetWork_Available;
		}
		if (maxRetryTimes_NetWork_Unavailable > 0) {
			mMaxRetryTimes_Network_Unavailable = maxRetryTimes_NetWork_Unavailable;
		}
		// 注意，这里务必切换isRuuing的状态，否则如果在run()里面切换的话，有可能导致下载被判定为失败，因为run()的执行可能会比外围监听线程的执行慢。
		mIsRunning = true;
		mDownloadResultStatus = 0; // 标识现在还没有下载结果

	}

	public BasicFileDownloaderHandler(Context context, String destUrl, long startByte, File tempFile, File storeFile) {
		this(context, destUrl, startByte, tempFile, storeFile, -1, -1);
	}

	public BasicFileDownloaderHandler(Context context, String destUrl, long startByte, File tempFile, File storeFile,
			int maxRetryTimes_NetWork_Available) {
		this(context, destUrl, startByte, tempFile, storeFile, maxRetryTimes_NetWork_Available, -1);
	}

	/**
	 * 如果没有结束，则一直尝试下载直至到达指定重试次数
	 */
	@Override
	public void run() {
		try {
			while (mIsRunning) {
				try {
					// 网络不可用，计数
					if (!Util_Network_Status.isNetworkAvailable(mApplicationContext)) {

						++mRunCounter_Network_Unavailable;
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK.te(Debug_SDK.mDownloadTag, this, "当前网络不可用，等待10秒后进行第[%d]次重试，总共可以进行的重试次数为[%d]",
									mRunCounter_Network_Unavailable, mMaxRetryTimes_Network_Unavailable);
						}
						try {
							Thread.sleep(NETWORK_RETRY_DELAY_MS);
						} catch (Throwable e) {
							if (Debug_SDK.isDownloadLog) {
								Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
							}
						}
					}

					// 10秒后再次检查网络
					if (!Util_Network_Status.isNetworkAvailable(mApplicationContext)) {

						// 如果网络还是不行，则判断是否是否已经达到重试上限
						if (mRunCounter_Network_Unavailable >= mMaxRetryTimes_Network_Unavailable) {
							if (Debug_SDK.isDownloadLog) {
								Debug_SDK.te(Debug_SDK.mDownloadTag, this,
										"当前网络不可用，已经进行了[%d]次重试，总共可以进行的重试次数为[%d]，结束下载", mRunCounter_Network_Unavailable,
										mMaxRetryTimes_Network_Unavailable);
							}
							// 由于网络不成功导致的重试，达到最大限定次数后取消，同时标记为下载失败
							mIsRunning = false;
							mDownloadResultStatus = 3;
							break;
						}
						// 如果还没有达最大次数就进行下一次循环
						else {
							continue;
						}
					}
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}

				// 当前网络可用，则进行文件下载
				++mRunCounter;
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "=============尝试第[%d]次下载", mRunCounter);
				}
				// 如果下载成功则不会进行下次循环
				download();
				// 下载之后(不管成功与否)判断是否已经到达最大重试次数以确定是否需要进行下一次下载，如果已经达到最大次数就标记为下载失败
				if (mRunCounter >= mMaxRetryTimes) {
					mIsRunning = false;
					mDownloadResultStatus = 3;
					break;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

	}

	/**
	 * 进行下载
	 */
	private void download() {

		try {
			switch (mDownloader.downloadToFile()) {

			// 下载成功
			case BasicFileDownloader.STATUS_OK:
				// 1、重命名暂存文件到目标文件
				// 2、结束下载循环while
				if (mTempFile.renameTo(mDestFile)) {
					mIsRunning = false;
					mDownloadResultStatus = 1;
				}
				break;

			case BasicFileDownloader.STATUS_ERROR_HTTP:// http码错误(4**或5**)
			case BasicFileDownloader.STATUS_ERROR_PARAMS:// 参数错误
				mIsRunning = false;// 直接不运行
				mDownloadResultStatus = 3;
				break;

			// 下载停止了[被动或者主动]
			case BasicFileDownloader.STATUS_STOP:
				mIsRunning = false;
				mDownloadResultStatus = 2;
				break;

			// 重试
			case BasicFileDownloader.STATUS_EXCEPTION:
				break;
			default:
				break;

			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 是否正在运行
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return mIsRunning;
	}

	/**
	 * 是否已经成功下载
	 * 
	 * @return
	 */
	public boolean isSuccess() {
		return mDownloadResultStatus == 1;
	}

	/**
	 * 是否已经被停止了（主动）
	 * 
	 * @return
	 */
	public boolean isStop() {
		return mDownloadResultStatus == 2;
	}

	/**
	 * 是否下载失败（一般由网络问题等引起）
	 * 
	 * @return
	 */
	public boolean isFailed() {
		return mDownloadResultStatus == 3;
	}

	/**
	 * 获取总长度
	 * 
	 * @return
	 */
	public long getContentLength() {
		return mDownloader.getContentLength();
	}

	/**
	 * 获取已经完成的长度
	 * 
	 * @return
	 */
	public long getCompleteLength() {
		return mDownloader.getCompleteLength();
	}

	/**
	 * 获取下载进度的百分比
	 * 
	 * @return
	 */
	public int getPercent() {
		return mDownloader.getPercent();
	}

	/**
	 * 终止下载
	 */
	public void stopDownload() {
		mIsRunning = false;
		mDownloadResultStatus = 2;
		mDownloader.stop();
	}
}
