package com.android.proxy.client;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import org.proxydroid.ProxyDroidService;
import org.proxydroid.db.DNSResponse;
import org.proxydroid.db.DatabaseHelper;
import org.proxydroid.utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
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
		isRoot = Utils.isRoot();
		if (!isRoot) {
			Log.d(TAG, "当前没有root");
		}
		reset(context);
	}

	public void startProxy(String host, int port) {
		if (mContext != null && isRoot) {
			boolean result = serviceStart(mContext, host, port);
			Log.d(TAG, "开启service结果:" + result);
		}
	}

	public boolean serviceStop(Context context) {
		if (!isRoot) return false;
		if (!Utils.isWorking()) return false;
		try {
			context.stopService(new Intent(context, ProxyDroidService.class));
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
			if (future.get()) {
				startService(context, host, port);
			}
		} catch (Exception e) {
			Log.d(TAG, e.fillInStackTrace().toString());
			return false;
		}
		return true;
	}

	private void startService(Context context, String host, int port) {
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
		bundle.putBoolean("isAutoSetProxy", true);
		bundle.putBoolean("isBypassApps", false);
		bundle.putBoolean("isAuth", false);
		bundle.putBoolean("isNTLM", false);
		bundle.putBoolean("isDNSProxy", false);
		bundle.putBoolean("isPAC", false);
		bundle.putInt("port", port);
		bundle.putString("packageName",packageName);
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
		}

		Utils.runRootCommand(Utils.getIptables()
				+ " -t nat -F OUTPUT\n"
				+ "/data/data/"+packageName+"/"
				+ "proxy.sh stop\n"
				+ "kill -9 `cat /data/data/"+packageName+"/stunnel.pid`\n"
				+ "kill -9 `cat /data/data/"+packageName+"/shrpx.pid`\n"
				+ "kill -9 `cat /data/data/"+packageName+"/cntlm.pid`\n");

		Utils.runRootCommand("chmod 700 /data/data/"+packageName+"/iptables\n"
				+ "chmod 700 /data/data/"+packageName+"/redsocks\n"
				+ "chmod 700 /data/data/"+packageName+"/proxy.sh\n"
				+ "chmod 700 /data/data/"+packageName+"/cntlm\n"
				+ "chmod 700 /data/data/"+packageName+"/stunnel\n"
				+ "chmod 700 /data/data/"+packageName+"/shrpx\n");
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
					out = new FileOutputStream("/data/data/" + packageName + "/"+file);
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
