package net.youmi.android.libs.common.download.model;

/**
 * 下载任务状态
 * 
 * @author zhitaocai
 * 
 */
public class FileDownloadState {
	public final static int STATE_INIT = 100;
	public final static int STATE_DOWNLOAD_START = 101;
	public final static int STATE_DOWNLOADING = 102;
	public final static int STATE_DOWNLOAD_SUCCESS = 103;
	public final static int STATE_DOWNLOAD_FAILED = 104;
	public final static int STATE_ALREADY_EXIST = 105;
	public final static int STATE_OBSERVER_OTHERS_DOWNLOADING = 106;

	/**
	 * 下载停止 created by caizhitao on 2014-7-17
	 */
	public final static int STATE_DOWNLOAD_STOP = 107;
}
