package net.youmi.android.libs.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.os.Environment;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.util.Util_System_SDCard_Util;

public abstract class Basic_SQLiteOpenHelper {

    private final static String mTag = "db";
    private final String mDbFilePath;
    private final CursorFactory mFactory;
    private final int mNewVersion;
    private Context mContext;
    private SQLiteDatabase mDatabase = null;
    private boolean mIsInitializing = false;
    private boolean mIsSdcard = false;// 如果是sdcard，每次获取db时必须先检查一下sdcard状态。

    public Basic_SQLiteOpenHelper(Context context, String path, CursorFactory factory, int version, boolean isSdcard) {
        // if (version < 1) {
        // throw new IllegalArgumentException("Version must be >= 1, was "
        // + version);
        // }
        //
        // if (path == null) {
        // throw new IllegalArgumentException("db path is null");
        // }

        if (version < 1) {
            version = 1;
        }

        mDbFilePath = path;
        mFactory = factory;
        mNewVersion = version;
        mIsSdcard = isSdcard;
        mContext = context;
    }

    public Basic_SQLiteOpenHelper(Context context, String path,
                                  CursorFactory factory, int version) {
        // if (version < 1) {
        // throw new IllegalArgumentException("Version must be >= 1, was "
        // + version);
        // }
        //
        // if (path == null) {
        // throw new IllegalArgumentException("db path is null");
        // }

        if (version < 1) {
            version = 1;
        }

        mDbFilePath = path;
        mFactory = factory;
        mNewVersion = version;
        mIsSdcard = path.startsWith(Environment.getExternalStorageDirectory().getPath());
        mContext = context;
    }

    public synchronized SQLiteDatabase getWritableDatabase() {

        if (mIsSdcard) {
            // sdcard
            if (!Util_System_SDCard_Util.IsSdCardCanWrite(mContext)) {
                // sdcard不可写，返回null
                return null;
            }
        }

        if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
            return mDatabase; // The database is already open for business
        }

        if (mIsInitializing) {
            if (Debug_SDK.isDebug) {
                Debug_SDK.de(mTag, "getWritableDatabase called recursively");
            }
            return null;
        }

        // If we have a read-only database open, someone could be using it
        // (though they shouldn't), which would cause a lock to be held on
        // the file, and our attempts to open the database read-write would
        // fail waiting for the file lock. To prevent that, we acquire the
        // lock on the read-only database, which shuts out other users.

        if (mDbFilePath == null) {
            if (Debug_SDK.isDebug) {
                Debug_SDK.de(mTag, "db path is null");
            }
            return null;
        }

        boolean success = false;
        SQLiteDatabase db = null;
        try {
            mIsInitializing = true;

            db = SQLiteDatabase.openOrCreateDatabase(mDbFilePath, mFactory);

            int version = db.getVersion();
            if (version != mNewVersion) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        onUpgrade(db, version, mNewVersion);
                    }
                    db.setVersion(mNewVersion);
                    db.setTransactionSuccessful();
                } catch (Throwable e) {
                    if (Debug_SDK.isDebug) {
                        Debug_SDK.de(mTag, e);
                    }
                } finally {
                    db.endTransaction();
                }
            }

            onOpen(db);
            success = true;
            return db;
        } catch (Throwable e) {
            if (Debug_SDK.isDebug) {
                Debug_SDK.de(mTag, e);
            }
        } finally {

            try {

                mIsInitializing = false;
                if (success) {
                    if (mDatabase != null) {
                        try {
                            mDatabase.close();
                        } catch (Throwable e) {
                        }
                    }
                    mDatabase = db;
                } else {
                    if (db != null) {
                        db.close();
                    }
                }

            } catch (Throwable e2) {
                // handle exception
            }
        }

        if (Debug_SDK.isDebug) {
            Debug_SDK.de(mTag, "Exception on getWritableDatabase");
        }

        return null;
    }

    public synchronized SQLiteDatabase getReadableDatabase() {

        if (mIsSdcard) {
            // sdcard
            if (!Util_System_SDCard_Util.IsSdCardCanRead()) {
                // sdcard不可写，返回null
                return null;
            }
        }

        if (mDatabase != null && mDatabase.isOpen()) {
            return mDatabase; // The database is already open for business
        }

        if (mIsInitializing) {
            if (Debug_SDK.isDebug) {
                Debug_SDK.de(mTag, "getReadableDatabase called recursively");
            }
            return null;
        }

        try {
            SQLiteDatabase wdb = getWritableDatabase();

            if (wdb != null) {
                return wdb;
            }

        } catch (SQLiteException e) {
            if (Debug_SDK.isDebug) {
                Debug_SDK.de(mTag, "Couldn't open " + mDbFilePath + " for writing (will try read-only):", e);
            }
        }

        if (mDbFilePath == null) {
            if (Debug_SDK.isDebug) {
                Debug_SDK.de(mTag, "dbPath is null ,error ,on getReadableDatabse");
            }
            return null;
        }

        SQLiteDatabase db = null;

        try {
            mIsInitializing = true;

            db = SQLiteDatabase.openDatabase(mDbFilePath, mFactory,
                    SQLiteDatabase.OPEN_READWRITE);
            // 直接返回吧
            // if (db.getVersion() != mNewVersion) {
            // throw new SQLiteException(
            // "Can't upgrade read-only database from version "
            // + db.getVersion() + " to " + mNewVersion + ": "
            // + mDbFilePath);
            // }

            onOpen(db);

            if (Debug_SDK.isDebug) {
                Debug_SDK.de(mTag, "Opened " + mDbFilePath + " in read-only mode");
            }
            mDatabase = db;
            return mDatabase;
        } catch (Throwable e) {
            if (Debug_SDK.isDebug) {
                Debug_SDK.de(mTag, e);
            }
        } finally {
            try {

                mIsInitializing = false;
                if (db != null && db != mDatabase)
                    db.close();

            } catch (Throwable e) {
            }
        }


        return null;
    }

    public synchronized void close() {
        try {

            if (mIsInitializing) {
                if (Debug_SDK.isDebug) {
                    Debug_SDK.de(mTag, "Closed during initialization");
                }
                return;
            }

            if (mDatabase != null && mDatabase.isOpen()) {
                mDatabase.close();
                mDatabase = null;
            }
        } catch (Throwable e) {
            // handle exception
        }
    }

    public abstract void onCreate(SQLiteDatabase database);

    public abstract void onUpgrade(SQLiteDatabase database, int version,
                                   int newVersion);

    public void onOpen(SQLiteDatabase db) {

    }

    protected void putValidValue(ContentValues contentValues, String key, String value) {
        if (contentValues == null || Basic_StringUtil.isNullOrEmpty(value)
                || Basic_StringUtil.isNullOrEmpty(key)) {
            return;
        }
        contentValues.put(key, value);
    }

    protected void putValidValue(ContentValues contentValues, String key, byte[] value) {
        if (contentValues == null || Basic_StringUtil.isNullOrEmpty(key)
                || value == null || value.length == 0) {
            return;
        }
        contentValues.put(key, value);
    }

    protected void putValidValue(ContentValues contentValues, String key, long value) {
        if (contentValues == null || Basic_StringUtil.isNullOrEmpty(key)) {
            return;
        }
        contentValues.put(key, value);
    }

}
