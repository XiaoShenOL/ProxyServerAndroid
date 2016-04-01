package net.youmi.android.libs.common.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础缓存通用数据库,存放方式为key-value,暂时只能创建一个表，有4个字段
 * 
 * @author 林秋明 created on 2012-7-4
 * @author zhitaocai edit on 2014-6-27
 * 
 */
class Base_DB_Cache_Helper extends SQLiteOpenHelper {

	/**
	 * 上锁开关
	 */
	private static final String mLocking = "locking";

	/**
	 * 默认数据库表的名字，如果没有传入数据库表名的话就用这个
	 */
	protected static String TB_NAME = "YINcpuKxQ5cA";

	/**
	 * 字段 Key
	 */
	private static final String LABEL_KEY = "a";

	/**
	 * 字段 Value
	 */
	private static final String LABEL_VALUE = "b";

	/**
	 * 字段 Last Modify Time，表示该缓存的最后更新时间
	 */
	private static final String LABEL_LAST_MODIFY = "c";

	/**
	 * 到期时间戳(long)
	 */
	private static final String LABEL_EXPIRES = "d";

	/**
	 * 创建数据库表SQL
	 */
	private static final String CREATE_TABLE_SQL = "create table if not exists " + TB_NAME
			+ "(_id integer primary key autoincrement," + LABEL_KEY + " text UNIQUE, " + LABEL_VALUE + " blob, "
			+ LABEL_LAST_MODIFY + " integer, " + LABEL_EXPIRES + " integer);";

	/**
	 * 删除数据库表SQL
	 */
	private static final String DROP_TABLE_SQL = "drop table if exists " + TB_NAME;

	protected Base_DB_Cache_Helper(Context context, String dbName, int dbVersion, String tbName) {
		super(context.getApplicationContext(), dbName, null, dbVersion);
		if (tbName != null) {
			if (!"".equals(tbName.trim())) {
				TB_NAME = tbName;
			}
		}
	}

	protected Base_DB_Cache_Helper(Context context, String dbName, int dbVersion) {
		this(context, dbName, dbVersion, null);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// 删除旧表
		db.execSQL(DROP_TABLE_SQL);

		// 创建新表
		db.execSQL(CREATE_TABLE_SQL);
	}

	/**
	 * 保存缓存
	 * 
	 * @param key
	 * @param blob
	 * @param valid_time
	 *            传进一个缓存有效时间，如果是 <=0：表示缓存永远有效， >0:表示使用缓存有效时间。 lastModeif+valid_time=expires,如果 expires<currenttime
	 *            则表示缓存失效
	 * @return
	 */
	public boolean saveCache(String key, byte[] blob, long valid_time) {
		boolean res = false;
		if (Basic_StringUtil.isNullOrEmpty(key)) {
			return res;
		}
		if (blob == null || blob.length == 0) {
			return res;
		}

		// 设置缓存时间
		long start = System.currentTimeMillis();
		long expires = -1;
		if (valid_time > 0) {
			expires = start + valid_time;
		}

		// 这里不能在方法上上锁，要在这里上锁
		synchronized (mLocking) {
			SQLiteDatabase db = null;
			try {
				db = getWritableDatabase();
				if (!checkDb(db)) {
					if (Debug_SDK.isCacheLog) {
						Debug_SDK.te(Debug_SDK.mCacheTag, this, "保存失败, 当前数据库不可用");
					}
				}
				ContentValues values = new ContentValues();
				putValidValue(values, LABEL_KEY, key);
				putValidValue(values, LABEL_VALUE, blob);
				putValidValue(values, LABEL_LAST_MODIFY, start);
				putValidValue(values, LABEL_EXPIRES, expires);
				if (isExisting(db, TB_NAME, LABEL_KEY, key)) {
					res = update(db, TB_NAME, values, LABEL_KEY, key);
				} else {
					res = insert(db, TB_NAME, values);
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
				}
			} finally {
				closeDb(db);
			}

			if (Debug_SDK.isCacheLog) {
				// 如果写入成功就统计耗时看看
				if (res) {
					int cost = (int) (System.currentTimeMillis() - start);
					Debug_SDK.td(Debug_SDK.mCacheTag, this, "[key:%s]写入数据库成功: %d毫秒， 字节长度：%d ", key, cost, blob.length);
				}
			}

		}
		return res;

	}

	/**
	 * 批量插入或更新多个缓存信息
	 * 
	 * @param list
	 * @return
	 */
	public boolean saveCacheList(List<Cache_Model> list) {
		if (list == null || list.size() == 0) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, "列表为空，缓存失败");
			}
			return false;
		}
		long start = System.currentTimeMillis();
		// 传入的内容长度，用于打印log
		long countLen = 0;
		// 可以进行缓存的个数，用于打印log
		int num = 0;
		// 最后成功写入数据库的个数，用于打印log
		int res = 0;
		// 这里不能在方法上上锁，要在这里上锁
		synchronized (mLocking) {
			SQLiteDatabase db = null;
			try {
				db = getWritableDatabase();
				if (!checkDb(db)) {
					if (Debug_SDK.isCacheLog) {
						Debug_SDK.te(Debug_SDK.mCacheTag, this, "保存失败, 当前数据库不可用");
					}
				}

				db.beginTransaction();

				for (int i = 0; i < list.size(); i++) {

					Cache_Model item = list.get(i);
					String key = item.getKey();
					if (Basic_StringUtil.isNullOrEmpty(key)) {
						continue;
					}

					byte[] blob = item.getData();
					if (blob == null) {
						continue;
					}
					if (blob.length == 0) {
						continue;
					}
					countLen += blob.length;

					long expires = -1;
					if (item.getValidTime_ms() > 0) {
						expires = start + item.getValidTime_ms();// 缓存到期时间
					}
					num++;

					ContentValues values = new ContentValues();
					putValidValue(values, LABEL_KEY, key);
					putValidValue(values, LABEL_VALUE, blob);
					putValidValue(values, LABEL_LAST_MODIFY, System.currentTimeMillis());
					putValidValue(values, LABEL_EXPIRES, expires);
					if (isExisting(db, TB_NAME, LABEL_KEY, key)) {
						if (update(db, TB_NAME, values, LABEL_KEY, key)) {
							res++;
						}
					} else {
						if (insert(db, TB_NAME, values)) {
							res++;
						}
					}
				}

				db.setTransactionSuccessful();
				return true;
			} catch (Throwable e) {
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
				}
			} finally {
				try {
					if (db != null) {
						db.endTransaction();
					}
				} catch (Throwable e1) {
					if (Debug_SDK.isCacheLog) {
						Debug_SDK.te(Debug_SDK.mCacheTag, this, e1);
					}
				} finally {
					closeDb(db);
				}
				if (Debug_SDK.isCacheLog) {
					int cost = (int) (System.currentTimeMillis() - start);
					Debug_SDK.td(Debug_SDK.mCacheTag, this, "缓存写入数据库(多个),花费时间:%d毫秒，总长度:%d,总缓存个数：%d|%d，成功个数:%d", cost,
							countLen, list.size(), num, res);
				}
			}
		}
		return false;
	}

	/**
	 * 获取所有已经缓存了的key
	 * 
	 * @return String[]
	 */
	public String[] getKeys() {
		synchronized (mLocking) {
			Cursor cursor = null;
			SQLiteDatabase db = null;
			try {
				db = getWritableDatabase();
				if (!checkDb(db)) {
					if (Debug_SDK.isCacheLog) {
						Debug_SDK.te(Debug_SDK.mCacheTag, this, "保存失败, 当前数据库不可用");
					}
				}

				cursor = db.query(TB_NAME, new String[] { LABEL_KEY }, null, null, null, null, null);

				ArrayList<String> keys = new ArrayList<String>();

				while (cursor.moveToNext()) {
					keys.add(cursor.getString(cursor.getColumnIndex(LABEL_KEY)));
				}

				if (keys.size() == 0) {
					return null;
				}

				String[] rtKeys = new String[keys.size()];
				return keys.toArray(rtKeys);

			} catch (Throwable e) {
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
				}
			} finally {
				closeCursor(cursor);
				closeDb(db);
			}
		}
		return null;
	}

	/**
	 * 根据指定的key获取value
	 * 
	 * @param key
	 * @return byte[]
	 */
	public byte[] getCache(String key) {
		long start = System.currentTimeMillis();
		byte[] result = null;
		synchronized (mLocking) {
			Cursor cursor = null;
			SQLiteDatabase db = null;
			try {
				db = getWritableDatabase();
				if (!checkDb(db)) {
					if (Debug_SDK.isCacheLog) {
						Debug_SDK.te(Debug_SDK.mCacheTag, this, "保存失败, 当前数据库不可用");
					}
				}
				cursor = db.query(TB_NAME, null, LABEL_KEY + "=?", new String[] { key }, null, null, null);
				if (cursor.moveToNext()) {
					result = cursor.getBlob(cursor.getColumnIndex(LABEL_VALUE));
					long expires = cursor.getLong(cursor.getColumnIndex(LABEL_EXPIRES));
					long nt = System.currentTimeMillis();
					if ((expires > 0) && (expires < nt)) {
						result = null;
						// 缓存超时，这里应该删除缓存
						if (Debug_SDK.isCacheLog) {
							Debug_SDK.td(Debug_SDK.mCacheTag, this, "缓存超时:%d<%d", expires, nt);
						}

						try {
							// 删除缓存
							deleteCacheByCacheKey(key);
						} catch (Throwable e) {
							if (Debug_SDK.isCacheLog) {
								Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
							}
						}
					}
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
				}
			} finally {

				closeCursor(cursor);
				checkDb(db);

				if (Debug_SDK.isCacheLog) {
					int cost = (int) (System.currentTimeMillis() - start);
					Debug_SDK.td(Debug_SDK.mCacheTag, this, "从数据库中读取缓存[key:%s]: %d毫秒, 缓存大小: %d字节", key, cost,
							result != null ? result.length : 0);
				}
			}
		}
		return result;
	}

	// /**
	// * 清空表(貌似没有用到，而且也不该给出去用)
	// */
	// public void clearTable() {
	// synchronized (mLocking) {
	// SQLiteDatabase db = null;
	// try {
	// db = getWritableDatabase();
	// if (!checkDb(db)) {
	// if (Debug_SDK.isCacheLog) {
	// Debug_SDK.te(Debug_SDK.mCacheTag, this, "保存失败, 当前数据库不可用");
	// }
	// }
	//
	// // 执行删除
	// db.delete(TB_NAME, null, null);
	//
	// } catch (Throwable e) {
	// if (Debug_SDK.isCacheLog) {
	// Debug_SDK.de(e);
	// }
	// }
	// }
	// }

	/**
	 * 清除所有过期缓存
	 */
	public void removeAllExpiredCache() {
		synchronized (mLocking) {
			SQLiteDatabase db = null;
			try {
				db = getWritableDatabase();
				if (!checkDb(db)) {
					if (Debug_SDK.isCacheLog) {
						Debug_SDK.te(Debug_SDK.mCacheTag, this, "移除过期缓存失败, 当前数据库不可用");
					}
				}
				long currentTime = System.currentTimeMillis();
				int count = db.delete(TB_NAME, LABEL_EXPIRES + "<? and " + LABEL_EXPIRES + ">?",
						new String[] { Long.toString(currentTime), "-1" });
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.td(Debug_SDK.mCacheTag, this, "删除数据库超期的缓存，成功删除数量:%d", count);
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
				}
			} finally {
				closeDb(db);
			}
		}
	}

	/**
	 * 根据指定的键删除缓存
	 * 
	 * @param key
	 * @return
	 */
	public boolean deleteCacheByCacheKey(String key) {

		boolean ret = false;
		synchronized (mLocking) {
			SQLiteDatabase db = null;
			try {
				db = getWritableDatabase();
				if (!checkDb(db)) {
					if (Debug_SDK.isCacheLog) {
						Debug_SDK.td(Debug_SDK.mCacheTag, this, "移除指定的键值缓存失败, 当前数据库不可用");
					}
				}
				ret = db.delete(TB_NAME, LABEL_KEY + " =? ", new String[] { key }) > 0;

			} catch (Throwable e) {
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
				}
			} finally {
				checkDb(db);
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.td(Debug_SDK.mCacheTag, this, "删除指定缓存:%s", ret ? "成功" : "失败");
				}
			}
		}
		return ret;
	}

	private void putValidValue(ContentValues contentValues, String key, String value) {
		if (contentValues == null || Basic_StringUtil.isNullOrEmpty(value) || Basic_StringUtil.isNullOrEmpty(key)) {
			return;
		}
		contentValues.put(key, value);
	}

	private void putValidValue(ContentValues contentValues, String key, byte[] value) {
		if (contentValues == null || Basic_StringUtil.isNullOrEmpty(key)) {
			return;
		}
		if (value == null) {
			return;
		}
		if (value.length == 0) {
			return;
		}
		contentValues.put(key, value);
	}

	private void putValidValue(ContentValues contentValues, String key, long value) {
		if (contentValues == null || Basic_StringUtil.isNullOrEmpty(key)) {
			return;
		}
		contentValues.put(key, value);
	}

	/**
	 * 查询数据库中指定字段是否存在指定的值
	 * 
	 * @param db
	 *            外部传入，外部关闭这个db，内部可以直接进行db操作，db不需要初始化，也不需要上锁
	 * @param tbName
	 *            表名
	 * @param field
	 *            数据库中指定的字段
	 * @param value
	 *            要查询的值
	 * @return
	 */
	private boolean isExisting(SQLiteDatabase db, String tbName, String field, String value) {
		boolean res = false;
		Cursor cursor = null;
		try {
			cursor = db.query(tbName, null, field + "=?", new String[] { value }, null, null, null);
			res = cursor.moveToFirst();
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		} finally {
			closeCursor(cursor);
		}
		return res;
	}

	/**
	 * 更新
	 * 
	 * @param db
	 *            外部传入，外部关闭这个db，内部可以直接进行db操作，db不需要初始化，也不需要上锁
	 * @param tbName
	 * @param values
	 *            更新的值
	 * @param whereClause
	 *            条件字段
	 * @param whereArgs
	 *            条件
	 * @return
	 */
	private boolean update(SQLiteDatabase db, String tbName, ContentValues values, String whereClause, String whereArgs) {
		try {
			long err = db.update(tbName, values, whereClause + "=?", new String[] { whereArgs });
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.td(Debug_SDK.mCacheTag, this, "更新数量 ：%d", err);
			}
			if (err > 0) {
				return true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return false;
	}

	/**
	 * 插入
	 * 
	 * @param db
	 *            外部传入，外部关闭这个db，内部可以直接进行db操作，db不需要初始化，也不需要上锁
	 * @param tbName
	 * @param values
	 * @return
	 */
	private boolean insert(SQLiteDatabase db, String tbName, ContentValues values) {
		try {
			long err = db.insert(tbName, null, values);
			return err > 0;
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return false;
	}

	/**
	 * 检查数据库是否可以使用。
	 * 
	 * @param db
	 * @return
	 */
	private boolean checkDb(SQLiteDatabase db) {
		if (db == null) {
			return false;
		}
		return db.isOpen();
	}

	/**
	 * 关闭游标
	 * 
	 * @param cursor
	 */
	private void closeCursor(Cursor cursor) {
		try {
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
	}

	/**
	 * 关闭数据库
	 * 
	 * @param db
	 */
	private void closeDb(SQLiteDatabase db) {
		try {
			if (checkDb(db)) {
				db.close();
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
	}

}
