package net.youmi.android.libs.common.download.listener;

import net.youmi.android.libs.common.download.model.FileDownloadTask;

/**
 * 用于检查文件是否可用
 * 
 * @author jen
 * 
 */
public interface FileAvailableChecker {

	/**
	 * 如果文件可用，返回true，否则返回false
	 * 
	 * @return
	 */
	boolean checkFileAvailable(FileDownloadTask task);

	/**
	 * 是否需要联网检查文件长度，通过文件长度的比对，可以提高精准度
	 * 
	 * @return
	 */
	boolean isNeedToCheckContentLengthByNetwork(FileDownloadTask task);

}
