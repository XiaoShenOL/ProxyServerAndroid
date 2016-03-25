package com.android.sms.proxy.entity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author zyq 16-3-21
 *         保存手机号，配合ContentProvider使用．．．．
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "user.db";
	private static final int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + UserContentProviderMetaData.UserTableMetaData.TABLE_NAME + " (" +
						UserContentProviderMetaData.UserTableMetaData._ID + " INTEGER PRIMARY KEY," +
						UserContentProviderMetaData.UserTableMetaData.PHONE_NAME + " TEXT);"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXITS " + UserContentProviderMetaData.UserTableMetaData.TABLE_NAME);
		onCreate(db);
	}
}
