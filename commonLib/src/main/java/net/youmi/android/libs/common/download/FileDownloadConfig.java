package net.youmi.android.libs.common.download;

/**
 * 文件下载配置项
 * 
 * @author jen
 * 
 */
public class FileDownloadConfig {

	/**
	 * 缓存文件被锁超期时间<br/>
	 * 当进行下载前会对缓存文件的最后修改时间进行判断，如果超过16秒，说明没有其他访问者，则可以对其进入下载写入操作。
	 */
	public final static long TEMP_FILE_LOCK_TIME_OUT_MS = 16000;

	/**
	 * 观察下载任务进度的线程循环时间间隔，即每隔1.5s更新一次下载进度
	 */
	public final static long INTERVAL_PROGRESS_NOTIFY = 1500;

	/**
	 * 缓存文件的后缀名
	 */
	public final static String TEMP_FILE_SUFFIX = ".ymtf";

	/**
	 * 网络不可用的情况下，下一次重试的等待时间，等10s重试
	 */
	public final static int NETWORK_RETRY_DELAY_MS = 10000;

	/**
	 * 网络可用情况下，可以进行最大请求的次数
	 */
	public final static int MAX_RETRY_TIMES_NETWORK_AVAILABLE = 8;

	/**
	 * 网络不可用情况下，可以进行最大请求的次数
	 * 
	 * @author zhitaocai edit on 2014-5-26 修改网络不行的情况下，重试上限默认值为10，原来为60，太长了
	 */
	public final static int MAX_RETRY_TIMES_NETWORK_UNAVAILABLE = 10;
	
	/**
	 * 产品类型
	 * <ol>
	 * <li>app : 则会关闭多进程那个下载前的检测。</li>
	 * <li>sdk : 则会开启多进程那个下载前的检测。</li>
	 * </ol>
	 */
	public final static int PRODUCT_TYPE = 1;
}
