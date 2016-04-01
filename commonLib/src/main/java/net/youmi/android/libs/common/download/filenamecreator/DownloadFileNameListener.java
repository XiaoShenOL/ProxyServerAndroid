package net.youmi.android.libs.common.download.filenamecreator;

import net.youmi.android.libs.common.download.model.FileDownloadTask;

/**
 * 管理下载文件名
 * 
 * @author jen
 */
public interface DownloadFileNameListener {

	/**
	 * 通过解析url和contentDisposition来获取文件在服务器上的原始名字
	 * 
	 * @param task
	 * @param contentDisposition
	 * @return
	 */
	String getSourceFileName(FileDownloadTask task, String contentDisposition);

	/**
	 * 通过解析url和contentDisposition来获取文件最终保存到本地的名字
	 * 
	 * @param task
	 * @param contentDisposition
	 * @return
	 */
	String getStoreFileName(FileDownloadTask task, String contentDisposition);


}
