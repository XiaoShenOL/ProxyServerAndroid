package net.youmi.android.libs.common.download.ext;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import net.youmi.android.libs.common.debug.AppDebugConfig;
import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * 下载任务数据库记录帮手类
 * boolean is store as int, 0(false) 1(true)
 *
 * @author CsHeng
 * @author yxf
 * @date 2013-8-30
 * @date 2014-9-30
 */
public class DBHelper_Download extends SQLiteOpenHelper implements OplayDownloadManager
		.OnDownloadStatusChangeListener, OplayDownloadManager.OnProgressUpdateListener {
	private final static boolean DEBUG = false;
	private static int VERSION = 2;
	private static String DB_NAME = "owan_download.db";
	private static String TABLE_NAME = "download";
	private static DBHelper_Download instance = null;

	private final String KEY_OF_APPNAME = "a";
	private final String KEY_OF_ICONURL = "b";
	private final String KEY_OF_MD5SUM = "c";
	private final String KEY_OF_PACKAGENAME = "d";
	private final String KEY_OF_RAWURL = "e";
	private final String KEY_OF_DOWNLOADSTATUS = "f";
	private final String KEY_OF_PATH_APK = "g";
	private final String KEY_OF_PATH_OPK = "h";
	private final String KEY_OF_VERSIONCODE = "i";
	private final String KEY_OF_IS_OFFER_APP = "j";
	private final String KEY_OF_APP_ID = "k";
	private final String KEY_OF_VERSION_NAME = "l";
	private final String KEY_OF_COMPLETE_PERCENTAGE = "m";
	private final String KEY_OF_TASK_ORIGIN = "n";
	private final String KEY_OF_IS_OPK = "o";
	private final String KEY_OF_IS_DELETE = "p";
	private final String KEY_OF_UPDATE_TIME = "q";

	private final String[] AllColumns = new String[]{KEY_OF_APPNAME, KEY_OF_ICONURL, KEY_OF_MD5SUM, KEY_OF_PACKAGENAME,
			KEY_OF_RAWURL, KEY_OF_DOWNLOADSTATUS, KEY_OF_PATH_APK, KEY_OF_PATH_OPK, KEY_OF_VERSIONCODE,
			KEY_OF_IS_OFFER_APP, KEY_OF_APP_ID, KEY_OF_VERSION_NAME, KEY_OF_COMPLETE_PERCENTAGE, KEY_OF_TASK_ORIGIN,
			KEY_OF_IS_OPK, KEY_OF_IS_DELETE, KEY_OF_UPDATE_TIME
	};
	private final String mOrderBy = KEY_OF_UPDATE_TIME + " DESC";
	private Context mAppContext;

	public DBHelper_Download(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		mAppContext = context.getApplicationContext();
	}

	public static synchronized DBHelper_Download getInstance(Context context) {
		if (instance == null) {
			instance = new DBHelper_Download(context, DB_NAME, null, VERSION);
		}
		return instance;
	}

	//关闭数据库
	@Override
	public synchronized void close() {
		try {
			if (getReadableDatabase() != null) {
				getReadableDatabase().close();
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		super.close();
	}

	//创建
	@Override
	public void onCreate(SQLiteDatabase db) {
		final String sql = getCreateDbSql();
		db.execSQL(sql);
	}

	//更新
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			if (oldVersion != newVersion) {
				db.execSQL("drop table if exists " + TABLE_NAME);
				db.execSQL(getCreateDbSql());
			}

		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	//创建数据库的SQL语句
	private String getCreateDbSql() {
		return String.format(
				"CREATE TABLE IF NOT EXISTS %s (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s INTEGER,%s TEXT, %s TEXT,"
						+ "%s INTEGER,%s INTEGER,%s INTEGER,%s TEXT,%s INTEGER,"
						+ "%s INTEGER,%s INTEGER,%s INTEGER,%s INTEGER"
						+ ");",
				TABLE_NAME, KEY_OF_APPNAME, KEY_OF_ICONURL, KEY_OF_MD5SUM, KEY_OF_PACKAGENAME, KEY_OF_RAWURL,
				KEY_OF_DOWNLOADSTATUS, KEY_OF_PATH_APK, KEY_OF_PATH_OPK, KEY_OF_VERSIONCODE, KEY_OF_IS_OFFER_APP,
				KEY_OF_APP_ID,
				KEY_OF_VERSION_NAME, KEY_OF_COMPLETE_PERCENTAGE, KEY_OF_TASK_ORIGIN, KEY_OF_IS_OPK,
				KEY_OF_IS_DELETE, KEY_OF_UPDATE_TIME
		);
	}

	public boolean addDownloadTask(SimpleAppInfo taskVo) {
		try {
			if (taskVo == null || TextUtils.isEmpty(taskVo.getOwkUrl())) {
				return false;
			}
			return updateElseInsert(getWritableDatabase(), taskVo);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return false;
	}

	public boolean updateDownloadProgress(String url, int percent) {
		Cursor cursor = null;
		try {
			if (url == null) return false;
			if (isDebug()) {
				AppDebugConfig.logMethodWithParams(this, url, percent);
			}
			if (TextUtils.isEmpty(url)) {
				return false;
			}
			final String selection = String.format("%s=?", KEY_OF_RAWURL);
			final String[] selectionArgs = new String[1];
			selectionArgs[0] = url;
			final ContentValues values = new ContentValues(1);
			values.put(KEY_OF_COMPLETE_PERCENTAGE, percent);
			final int updateAffected = getWritableDatabase().update(TABLE_NAME, values, selection, selectionArgs);
			return updateAffected != 0;
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

	public boolean deleteDownloadTask(SimpleAppInfo downloadTask) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				Log.i("DBHelper_Download", "DownloadTaskToDeleteInDB:" + downloadTask);
			}
			if (downloadTask == null) {
				return false;
			}
			final String packageName = downloadTask.getPackageName();
			final String rawUrl = downloadTask.getOwkUrl();
			if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(rawUrl)) {
				return false;
			}
			final String selection = String.format("%s=? and %s=?", KEY_OF_PACKAGENAME, KEY_OF_RAWURL);
			final String[] selectionArgs = {packageName, rawUrl};
			final boolean res = getReadableDatabase().delete(TABLE_NAME, selection, selectionArgs) != 0;
			if (AppDebugConfig.IS_DEBUG) {
				Log.i("DBHelper_Download", String.format("Delete DownloadRecord %s: %b", rawUrl, res));
			}
			return res;
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return false;
	}

	public void getDownloadList() {
		Cursor cursor = null;
		try {
			final SQLiteDatabase db = getReadableDatabase();
			cursor = db.query(TABLE_NAME, AllColumns, null, null, null, null, mOrderBy);
			SimpleAppInfo info;
			while (cursor.moveToNext()) {
				info = getDownloadTaskFromCursor(cursor);
				DownloadStatus ds = info.getDownloadStatus();
				info.setContext(mAppContext);
				info.initFile();
				switch (ds) {
					case DOWNLOADING:
					case PENDING:
					case PAUSED:
					case FAILED:
						info.setDownloadStatus(DownloadStatus.PAUSED);
						OplayDownloadManager.getInstance(mAppContext).addPausedTask(info);
						break;
					case FINISHED:
						if (!info.isFileExists()) {
							deleteDownloadTask(info);
							continue;
						}
						OplayDownloadManager.getInstance(mAppContext).addFinishedTask(info);
						break;

				}
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		} finally {
			try {
				if (cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				Debug_SDK.e(e);
			}
		}
	}

	private boolean updateElseInsert(SQLiteDatabase db, SimpleAppInfo downloadTask) {
		try {
			if (db == null || downloadTask == null) {
				return false;
			}
			final String selection = String.format("%s=? and %s=?", KEY_OF_PACKAGENAME, KEY_OF_RAWURL);
			final String[] selectionArgs = new String[2];
			selectionArgs[0] = downloadTask.getPackageName();
			selectionArgs[1] = downloadTask.getOwkUrl();

			final int updateAffected = db.update(TABLE_NAME, getUpdateAppContentValues(downloadTask), selection,
					selectionArgs);
			if (updateAffected != 0) {// success update
				if (AppDebugConfig.IS_DEBUG) {
					Log.v("DBHelper_Download", "Success Update pn:" + downloadTask.getPackageName());
				}
				return true;
			} else {
				final long newRowId = db.insert(TABLE_NAME, null, getUpdateAppContentValues(downloadTask));
				if (newRowId != -1) {
					if (AppDebugConfig.IS_DEBUG) {
						Log.v("DBHelper_Download", "Insert NewRowId:" + newRowId + "," +
								"pn:" + downloadTask.getPackageName());
					}
					return true;// success insert
				}
			}
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.d("ERROR! Cannot Update Or Insert pn:" + downloadTask.getPackageName());
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return false;
	}

	private SimpleAppInfo getDownloadTaskFromCursor(Cursor cursor) {
		try {
			if (cursor == null) return null;
			final int appId = cursor.getInt(cursor.getColumnIndex(KEY_OF_APP_ID));
			final String rawUrl = cursor.getString(cursor.getColumnIndex(KEY_OF_RAWURL));
			final String packageName = cursor.getString(cursor.getColumnIndex(KEY_OF_PACKAGENAME));
			final String appName = cursor.getString(cursor.getColumnIndex(KEY_OF_APPNAME));
			final int versionCode = cursor.getInt(cursor.getColumnIndex(KEY_OF_VERSIONCODE));
			final String iconUrl = cursor.getString(cursor.getColumnIndex(KEY_OF_ICONURL));
			final String serverFileMd5 = cursor.getString(cursor.getColumnIndex(KEY_OF_MD5SUM));
			final int downloadStatus = cursor.getInt(cursor.getColumnIndex(KEY_OF_DOWNLOADSTATUS));
			final String versionName = cursor.getString(cursor.getColumnIndex(KEY_OF_VERSION_NAME));
			final int percentage = cursor.getInt(cursor.getColumnIndex(KEY_OF_COMPLETE_PERCENTAGE));
			final SimpleAppInfo downloadTask = new SimpleAppInfo();
			downloadTask.setAppId(appId);
			downloadTask.setOwkUrl(rawUrl);
			downloadTask.setPackageName(packageName);
			downloadTask.setAppName(appName);
			downloadTask.setVersionCode(versionCode);
			downloadTask.setAppIcon(iconUrl);
			downloadTask.setApkMd5(serverFileMd5);
			downloadTask.setDownloadStatus(DownloadStatus.value2Name(downloadStatus));
			downloadTask.setVersionName(versionName);
			downloadTask.setCompleteProgress(percentage);
			downloadTask.initInstallStatus(mAppContext);
			return downloadTask;
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return null;
	}

	private ContentValues getUpdateAppContentValues(SimpleAppInfo downloadTaskVo) {
		final ContentValues contentValues = new ContentValues();
		if (!TextUtils.isEmpty(downloadTaskVo.getAppName())) {
			contentValues.put(KEY_OF_APPNAME, downloadTaskVo.getAppName());
		}
		if (downloadTaskVo.getVersionCode() > 0) {
			contentValues.put(KEY_OF_VERSIONCODE, downloadTaskVo.getVersionCode());
		}
		if (!TextUtils.isEmpty(downloadTaskVo.getPackageName())) {
			contentValues.put(KEY_OF_PACKAGENAME, downloadTaskVo.getPackageName());
		}
		if (!TextUtils.isEmpty(downloadTaskVo.getAppIcon())) {
			contentValues.put(KEY_OF_ICONURL, downloadTaskVo.getAppIcon());
		}
		if (!TextUtils.isEmpty(downloadTaskVo.getOwkUrl())) {
			contentValues.put(KEY_OF_RAWURL, downloadTaskVo.getOwkUrl());
		}
		if (!TextUtils.isEmpty(downloadTaskVo.getApkMd5())) {
			contentValues.put(KEY_OF_MD5SUM, downloadTaskVo.getApkMd5());
		}
		final int status = downloadTaskVo.getDownloadStatus().ordinal();
		contentValues.put(KEY_OF_DOWNLOADSTATUS, status);
		contentValues.put(KEY_OF_UPDATE_TIME, System.currentTimeMillis());
		if (downloadTaskVo.getAppId() > 0) {
			contentValues.put(KEY_OF_APP_ID, downloadTaskVo.getAppId());
		}
		if (!TextUtils.isEmpty(downloadTaskVo.getVersionName())) {
			contentValues.put(KEY_OF_VERSION_NAME, downloadTaskVo.getVersionName());
		}
		if (downloadTaskVo.getCompleteProgress() > 0) {
			contentValues.put(KEY_OF_COMPLETE_PERCENTAGE, downloadTaskVo.getCompleteProgress());
		}
		return contentValues;
	}

	@Override
	public void onDownloadStatusChanged(SimpleAppInfo info) {
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMethodWithParams(this, "recieve the command");
		}
		try {
			if (info == null || TextUtils.isEmpty(info.getOwkUrl())) {
				return;
			}
			final SimpleAppInfo item = info;
			new Thread(new Runnable() {
				@Override
				public void run() {
					OplayDownloadManager dm = OplayDownloadManager.getInstance(mAppContext);
					String url = item.getOwkUrl();
					//1、如果状态不存在->删除
					//2、如果状态还存在->更新
					if (dm.getAppDownloadStatus(url) == null) {
						deleteDownloadTask(item);
					} else {
						updateElseInsert(getWritableDatabase(), item);
					}
				}
			}).start();

		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	@Override
	public void onProgressUpdate(String url, int percent, long speedBytesPerS) {
		updateDownloadProgress(url, percent);
	}

	private boolean isDebug() {
		return DEBUG && AppDebugConfig.IS_DEBUG;
	}
}
