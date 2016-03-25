package com.android.sms.proxy.entity;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

/**
 * @author zyq 16-3-21
 */
public class UserContentProvider extends ContentProvider {

	private static HashMap<String, String> sUsersProjectionMap;

	//访问表的所有列
	public static final int INCOMING_USER_COLLECTION = 1;
	//访问单独的列
	public static final int INCOMING_USER_SINGLE = 2;
	//操作URI的类
	public static final UriMatcher uriMatcher;

	private DatabaseHelper mDatabaseHelper;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(UserContentProviderMetaData.AUTHORITIES, "/users", INCOMING_USER_COLLECTION);
		uriMatcher.addURI(UserContentProviderMetaData.AUTHORITIES, "/users/#", INCOMING_USER_SINGLE);

		sUsersProjectionMap = new HashMap<String, String>();
		sUsersProjectionMap.put(UserContentProviderMetaData.UserTableMetaData._ID, UserContentProviderMetaData
				.UserTableMetaData._ID);
		sUsersProjectionMap.put(UserContentProviderMetaData.UserTableMetaData.PHONE_NAME, UserContentProviderMetaData
				.UserTableMetaData.PHONE_NAME);
	}


	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Nullable
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(UserContentProviderMetaData.UserTableMetaData.TABLE_NAME);

		switch (uriMatcher.match(uri)) {
			case INCOMING_USER_COLLECTION:
				qb.setProjectionMap(sUsersProjectionMap);
				break;
			case INCOMING_USER_SINGLE:
				qb.setProjectionMap(sUsersProjectionMap);
				qb.appendWhere(UserContentProviderMetaData.UserTableMetaData._ID + "=" + uri.getPathSegments().get(1));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = UserContentProviderMetaData.UserTableMetaData.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		//tell the cursor what uri to watch,so it knows when its source data changes.
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Nullable
	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
			case INCOMING_USER_COLLECTION:
				return UserContentProviderMetaData.UserTableMetaData.CONTENT_TYPE;
			case INCOMING_USER_SINGLE:
				return UserContentProviderMetaData.UserTableMetaData.CONTENT_TYPE_ITEM;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Nullable
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (uriMatcher.match(uri) != INCOMING_USER_COLLECTION) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		if (values.containsKey(UserContentProviderMetaData.UserTableMetaData.PHONE_NAME) == false) {
			values.put(UserContentProviderMetaData.UserTableMetaData.PHONE_NAME, "");
		}

		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		long rowId = db.insert(UserContentProviderMetaData.UserTableMetaData.TABLE_NAME, UserContentProviderMetaData
				.UserTableMetaData.PHONE_NAME, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(UserContentProviderMetaData.UserTableMetaData.CONTENT_URI, rowId);
			Log.d("contentProvider", "添加" + noteUri);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}
		throw new IllegalArgumentException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
			case INCOMING_USER_COLLECTION:
				count = db.delete(UserContentProviderMetaData.UserTableMetaData.TABLE_NAME, selection, selectionArgs);
				break;
			case INCOMING_USER_SINGLE:
				String noteId = uri.getPathSegments().get(1);
				count = db.delete(UserContentProviderMetaData.UserTableMetaData.TABLE_NAME,
						UserContentProviderMetaData.UserTableMetaData._ID + "=" + noteId
								+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
			case INCOMING_USER_COLLECTION:
				count = db.update(UserContentProviderMetaData.UserTableMetaData.TABLE_NAME, values, selection,
						selectionArgs);
				break;
			case INCOMING_USER_SINGLE:
				String noteId = uri.getPathSegments().get(1);
				count = db.update(UserContentProviderMetaData.UserTableMetaData.TABLE_NAME, values,
						UserContentProviderMetaData.UserTableMetaData._ID + "=" + noteId
								+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
