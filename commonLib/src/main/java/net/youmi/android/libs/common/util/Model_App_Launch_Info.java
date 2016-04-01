package net.youmi.android.libs.common.util;

public class Model_App_Launch_Info {

	private String mAppName;

	private String mMainActivityName;

	private int mIconResourceId;

	public Model_App_Launch_Info(String appName, int iconResourceId, String mainActivityName) {
		mAppName = appName;
		mIconResourceId = iconResourceId;
		mMainActivityName = mainActivityName;
	}

	public String getAppName() {
		return mAppName;
	}

	public String getMainActivityName() {
		return mMainActivityName;
	}

	public int getIconResourceId() {
		return mIconResourceId;
	}

}
