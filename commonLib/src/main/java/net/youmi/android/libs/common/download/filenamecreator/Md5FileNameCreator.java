package net.youmi.android.libs.common.download.filenamecreator;

import net.youmi.android.libs.common.download.model.FileDownloadTask;

public class Md5FileNameCreator extends BaseFileNameCreator {

	private static Md5FileNameCreator mInstance;

	public synchronized static Md5FileNameCreator getInstance() {
		if (mInstance == null) {
			mInstance = new Md5FileNameCreator();
		}
		return mInstance;
	}

	@Override
	public String getStoreFileName(FileDownloadTask task, String contentDisposition) {
		// 由于FileDownloadTask的identity就是url的md5值，所以直接使用其作为md5文件名。
		return task.getIdentity();
	}

}
