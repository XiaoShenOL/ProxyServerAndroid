package net.youmi.android.libs.common.download.filenamecreator;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.network.Util_Network_Http_ContentUtil;

public abstract class BaseFileNameCreator implements DownloadFileNameListener {

	/**
	 * 先从contentDisposition中尝试获取文件名，不行在从http url获取
	 */
	@Override
	public String getSourceFileName(FileDownloadTask task, String contentDisposition) {
		String fileName = null;
		try {
			// 从contentDisposition中获取文件名字(有时候这里为空)
			fileName = Util_Network_Http_ContentUtil.getFileNameFromContentDisposition(contentDisposition);
			if (!Basic_StringUtil.isNullOrEmpty(fileName)) {
				// !!很重要的设置
				// task.setSourceFileName(fileName);// 在这里设置文件的原始名字--content
				return fileName;
			}

		} catch (Throwable e) {
			fileName = null;
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		try {
			// 从httpurl中获取文件名字
			fileName = Util_Network_Http_ContentUtil.getFileNameFromHttpUrl(task.getDestUrl());
			if (!Basic_StringUtil.isNullOrEmpty(fileName)) {
				// !!很重要的设置
				// task.setSourceFileName(fileName);// 在这里设置文件的原始名字--content
				return fileName;
			}

		} catch (Throwable e) {
			fileName = null;
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return fileName;
	}

}
