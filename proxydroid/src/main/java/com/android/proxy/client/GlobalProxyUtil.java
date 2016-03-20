package com.android.proxy.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import org.proxydroid.ProxyDroidService;
import org.proxydroid.ProxyedApp;
import org.proxydroid.db.DNSResponse;
import org.proxydroid.db.DatabaseHelper;
import org.proxydroid.utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author zyq 16-3-16
 */
public class GlobalProxyUtil {

	private static String TAG = "globalProxyUtil";
	public volatile static GlobalProxyUtil instance;
	public Context mContext;
	private ExecutorService exec;
	private boolean isRoot;
	private String packageName;
	private final String SP_FILE_IS_SAVE = "isFileSave";
	public final static String PREFS_KEY_PROXYED = "Proxyed";

	public static GlobalProxyUtil getInstance(Context context) {
		if (instance == null) {
			synchronized (GlobalProxyUtil.class) {
				if (instance == null) {
					instance = new GlobalProxyUtil(context);
				}
			}
		}
		return instance;
	}

	public GlobalProxyUtil(Context context) {
		mContext = context;
		exec = Executors.newCachedThreadPool();
		packageName = mContext.getPackageName();
		Utils.DEFAULT_IPTABLES = "/data/data/" + packageName + "/iptables";
		ProxyDroidService.BASE = "/data/data" + packageName + "/";
		isRoot = Utils.isRoot();
		if (!isRoot) {
			Log.d(TAG, "当前没有root");
		}
	}

	public void init() {
		new Thread(new ProxyInitRunnable(mContext)).start();
	}

	public void addProxyPackage(Context context, String packageName) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String tordAppString = prefs.getString(PREFS_KEY_PROXYED, "");
		String[] tordApps;
		StringTokenizer st = new StringTokenizer(tordAppString, "|");
		tordApps = new String[st.countTokens()];
		int tordIdx = 0;
		while (st.hasMoreTokens()) {
			tordApps[tordIdx++] = st.nextToken();
		}
		boolean isOriginalSave = false;
		for (int i = 0; i < tordApps.length; i++) {
			if (tordApps[i].equals(packageName)) {
				isOriginalSave = true;
			}
		}
		StringBuilder sb = new StringBuilder(tordAppString);
		if (!isOriginalSave) {
			sb.append(packageName).append("|");
			SharedPreferences.Editor et = prefs.edit();
			et.putString(PREFS_KEY_PROXYED, sb.toString());
			et.commit();
		}

		SharedPreferences prefs1 = PreferenceManager
				.getDefaultSharedPreferences(context);
		String tordAppString1 = prefs1.getString(PREFS_KEY_PROXYED, "");
		Log.d(TAG,"添加代理包名："+tordAppString1);
	}

	private class ProxyInitRunnable implements Runnable {

		private Context mContext;

		public ProxyInitRunnable(Context context) {
			mContext = context;
		}

		@Override
		public void run() {
			final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
			String versionName;
			try {
				versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
			} catch (PackageManager.NameNotFoundException e) {
				versionName = "NONE";
			}

			if (!settings.getBoolean(versionName, false)) {
				String version;
				try {
					version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
				} catch (PackageManager.NameNotFoundException e) {
					version = "NONE";
				}
				reset(mContext);
				Log.d(TAG, "所有的工作准备完毕!!!!!!");
				SharedPreferences.Editor edit = settings.edit();
				edit.putBoolean(version, true);
				edit.commit();
			}
		}
	}

	public void startProxy(String host, int port) {
		if (mContext != null) {
			boolean result = serviceStart(mContext, host, port);
			Log.d(TAG, "开启service结果:" + result);
		}
	}

	public boolean serviceStop(Context context) {
		if (!isRoot) return false;
		if (!Utils.isWorking()) return false;
		try {
			context.stopService(new Intent(context, ProxyDroidService.class));
			Log.d(TAG, "kill -9 stunnel.pid,shrpx.pid,cntlm.pid");
			Utils.runRootCommand(Utils.getIptables()
					+ " -t nat -F OUTPUT\n"
					+ ProxyDroidService.BASE
					+ "proxy.sh stop\n"
					+ "kill -9 `cat /data/data/" + packageName + "/stunnel.pid`\n"
					+ "kill -9 `cat /data/data/" + packageName + "/shrpx.pid`\n"
					+ "kill -9 `cat /data/data/" + packageName + "/cntlm.pid`\n");
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 直接开启代理模式,若service已运行,根据host,port来判断是否要重置
	 */
	private boolean serviceStart(Context context, String host, int port) {
		try {
			Future<Boolean> future = exec.submit(new WaitStartService(context, host, port));
			Log.d(TAG, "之前是否有service存在:" + future.get());
			if (future.get()) {
				final boolean isGlobalMode = isGlobalMode(context, context.getPackageName(), true);
				Log.d(TAG, "当前是不是全局模式：" + isGlobalMode);
				startService(context, host, port, isGlobalMode);
			}
		} catch (Exception e) {
			Log.d(TAG, e.fillInStackTrace().toString());
			return false;
		}
		return true;
	}


	public boolean isGlobalMode(Context context, String packageName, boolean self) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String tordAppString = prefs.getString(PREFS_KEY_PROXYED, "");
		String[] tordApps;
		StringTokenizer st = new StringTokenizer(tordAppString, "|");
		tordApps = new String[st.countTokens()];
		int tordIdx = 0;
		while (st.hasMoreTokens()) {
			tordApps[tordIdx++] = st.nextToken();
		}

		Arrays.sort(tordApps);

		// else load the apps up
		PackageManager pMgr = context.getPackageManager();

		List<ApplicationInfo> lAppInfo = pMgr.getInstalledApplications(0);

		Iterator<ApplicationInfo> itAppInfo = lAppInfo.iterator();

		Vector<ProxyedApp> vectorApps = new Vector<ProxyedApp>();

		ApplicationInfo aInfo = null;

		int appIdx = 0;

		while (itAppInfo.hasNext()) {
			aInfo = itAppInfo.next();

			// ignore all system apps
			if (aInfo.uid < 10000)
				continue;

			ProxyedApp app = new ProxyedApp();

			app.setUid(aInfo.uid);

			app.setUsername(pMgr.getNameForUid(app.getUid()));

			// check if this application is allowed
			if (aInfo.packageName != null
					&& aInfo.packageName.equals(packageName)) {
				if (self)
					app.setProxyed(true);
			} else if (Arrays.binarySearch(tordApps, app.getUsername()) >= 0) {
				app.setProxyed(true);
			} else {
				app.setProxyed(false);
			}

			if (app.isProxyed())
				vectorApps.add(app);

		}

		ProxyedApp[] apps = new ProxyedApp[vectorApps.size()];
		vectorApps.toArray(apps);
		//如果数据库中存在选中字段,apps.length >１,则表示不是全局状态．
		for (int i = 0; i < apps.length; i++) {
			Log.d(TAG, "要代理的ａｐｐ有:" + apps[i].getUsername());
		}
		if (apps.length > 1) {
			return false;
		}
		return true;
	}

	private void startService(Context context, String host, int port, boolean isGlobalMode) {
		Log.d(TAG, "开始启动service");
		Intent it = new Intent(context, ProxyDroidService.class);
		Bundle bundle = new Bundle();
		bundle.putString("host", host);
		bundle.putString("user", "");
		bundle.putString("bypassAddrs", "");
		bundle.putString("password", "");
		bundle.putString("domain", "");
		bundle.putString("certificate", "");
		bundle.putString("proxyType", "http");
		bundle.putBoolean("isAutoSetProxy", isGlobalMode);
		bundle.putBoolean("isBypassApps", false);
		bundle.putBoolean("isAuth", false);
		bundle.putBoolean("isNTLM", false);
		bundle.putBoolean("isDNSProxy", false);
		bundle.putBoolean("isPAC", false);
		bundle.putInt("port", port);
		bundle.putString("packageName", packageName);
		Log.d(TAG, "host:" + host + "\nport:" + port);
		it.putExtras(bundle);
		context.startService(it);
	}

	class WaitStartService implements Callable<Boolean> {

		private String requestHost;
		private int requestPort;
		private Context mContext;

		public WaitStartService(Context context, String host, int port) {
			requestHost = host;
			requestPort = port;
			mContext = context;
		}

		@Override
		public Boolean call() throws Exception {
			boolean isServiceWorking = Utils.isWorking();
			Log.d(TAG, "当前代理是否在运行状态:" + isServiceWorking);
			String currentHost = Utils.getCurrentHost();
			int currentPort = Utils.getCurrentPort();

			if (requestHost.equals(currentHost) && requestPort == currentPort) {
				if (isServiceWorking) return false;//表明当前就是在运行状态
			} else {
				if (isServiceWorking) {
					reset(mContext);
				}
			}
			return true;
		}
	}

	/**
	 * 重置代理
	 *
	 * @param context
	 */
	public void reset(Context context) {
		Log.d(TAG, "重置代理!!!!");
		try {
			context.stopService(new Intent(context, ProxyDroidService.class));
		} catch (Exception e) {
			// Nothing
			Log.d(TAG, e.fillInStackTrace().toString());
		}

		CopyAssets(context);

		try {
			DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
			Dao<DNSResponse, String> dnsCacheDao = helper.getDNSCacheDao();
			List<DNSResponse> list = dnsCacheDao.queryForAll();
			for (DNSResponse resp : list) {
				dnsCacheDao.delete(resp);
			}
		} catch (Exception ignore) {
			// Nothing
			Log.d(TAG, ignore.fillInStackTrace().toString());
		}

		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (!settings.getBoolean(SP_FILE_IS_SAVE, false)) {
			Log.d(TAG, "修改文件权限!!!!!!!!!!!");
			Utils.runRootCommand("chmod 700 /data/data/" + packageName + "/iptables\n"
					+ "chmod 700 /data/data/" + packageName + "/redsocks\n"
					+ "chmod 700 /data/data/" + packageName + "/proxy.sh\n"
					+ "chmod 700 /data/data/" + packageName + "/cntlm\n"
					+ "chmod 700 /data/data/" + packageName + "/stunnel\n"
					+ "chmod 700 /data/data/" + packageName + "/shrpx\n");
			SharedPreferences.Editor edit = settings.edit();
			edit.putBoolean(SP_FILE_IS_SAVE, true);
			edit.commit();
		}

		Log.d(TAG, "kill -9 stunnel.pid,shrpx.pid,cntlm.pid");

		Utils.runRootCommand(Utils.getIptables()
				+ " -t nat -F OUTPUT\n"
				+ ProxyDroidService.BASE
				+ "proxy.sh stop\n"
				+ "kill -9 `cat /data/data/" + packageName + "/stunnel.pid`\n"
				+ "kill -9 `cat /data/data/" + packageName + "/shrpx.pid`\n"
				+ "kill -9 `cat /data/data/" + packageName + "/cntlm.pid`\n");
	}

	private void CopyAssets(Context context) {
		AssetManager assetManager = context.getAssets();
		String[] files = null;
		try {
			if (Build.VERSION.SDK_INT >= 21)
				files = assetManager.list("api-16");
			else
				files = assetManager.list("");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		if (files != null) {
			Log.d(TAG, "开始copy文件了!!!" + files.length);
			for (String file : files) {
				Log.d(TAG, "文件路径是:" + file);
				InputStream in = null;
				OutputStream out = null;
				try {
					if (Build.VERSION.SDK_INT >= 21)
						in = assetManager.open("api-16/" + file);
					else
						in = assetManager.open(file);
					out = new FileOutputStream("/data/data/" + packageName + "/" + file);
					copyFile(in, out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}


}
