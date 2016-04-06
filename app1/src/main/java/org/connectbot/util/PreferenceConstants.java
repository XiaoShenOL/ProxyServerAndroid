package org.connectbot.util;

import android.os.Build;

/**
 * @author zyq 16-3-6
 */
public class PreferenceConstants {

	public static final int SDK_INT = Integer.parseInt(Build.VERSION.SDK);
	public static final boolean PRE_ECLAIR = SDK_INT < 5;
	public static final boolean PRE_FROYO = SDK_INT < 8;
	public static final boolean PRE_HONEYCOMB = SDK_INT < 11;
    public static final String CONNECTION_PERSIST = "connPersist";
	public static final String BELL_NOTIFICATION = "bellNotification";
	//公匙是否预加载内存中
	public static final String MEMKEYS = "memkeys";
	public static final String WIFI_LOCK = "wifilock";

}
