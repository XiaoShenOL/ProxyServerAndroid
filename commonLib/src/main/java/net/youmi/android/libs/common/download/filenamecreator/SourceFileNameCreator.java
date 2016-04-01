package net.youmi.android.libs.common.download.filenamecreator;

import net.youmi.android.libs.common.download.model.FileDownloadTask;

public class SourceFileNameCreator extends BaseFileNameCreator{

	private static SourceFileNameCreator mInstance;

	public synchronized static SourceFileNameCreator getInstance() {
		if (mInstance == null) {
			mInstance = new SourceFileNameCreator();
		}
		return mInstance;
	}

	@Override
	public String getStoreFileName(FileDownloadTask task, String contentDisposition) {
		 return getSourceFileName(task, contentDisposition);
	}

}
