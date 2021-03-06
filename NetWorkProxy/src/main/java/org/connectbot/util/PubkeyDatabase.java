package org.connectbot.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.connectbot.bean.PubkeyBean;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zyq 16-3-6
 */
public class PubkeyDatabase extends RobustSQLiteOpenHelper {

	public final static String TAG = "CB.PubkeyDatabase";

	public final static String DB_NAME = "pubkeys";
	public final static int DB_VERSION = 2;

	public final static String TABLE_PUBKEYS = "pubkeys";
	public final static String FIELD_PUBKEY_NICKNAME = "nickname";
	public final static String FIELD_PUBKEY_TYPE = "type";
	public final static String FIELD_PUBKEY_PRIVATE = "private";
	public final static String FIELD_PUBKEY_PUBLIC = "public";
	public final static String FIELD_PUBKEY_ENCRYPTED = "encrypted";
	public final static String FIELD_PUBKEY_STARTUP = "startup";
	public final static String FIELD_PUBKEY_CONFIRMUSE = "confirmuse";
	public final static String FIELD_PUBKEY_LIFETIME = "lifetime";

	public final static String KEY_TYPE_RSA = "RSA",
			KEY_TYPE_DSA = "DSA",
			KEY_TYPE_IMPORTED = "IMPORTED",
			KEY_TYPE_EC = "EC";

	private Context context;

	static {
		addTableName(TABLE_PUBKEYS);
	}

	private static final Object sInstanceLock = new Object();

	private static PubkeyDatabase sInstance;

	public static PubkeyDatabase get(Context context) {
		synchronized (sInstanceLock) {
			if (sInstance != null) {
				return sInstance;
			}

			Context appContext = context.getApplicationContext();
			sInstance = new PubkeyDatabase(appContext);
			return sInstance;
		}
	}

	private PubkeyDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);

		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		super.onCreate(db);

		db.execSQL("CREATE TABLE " + TABLE_PUBKEYS
				+ " (_id INTEGER PRIMARY KEY, "
				+ FIELD_PUBKEY_NICKNAME + " TEXT, "
				+ FIELD_PUBKEY_TYPE + " TEXT, "
				+ FIELD_PUBKEY_PRIVATE + " BLOB, "
				+ FIELD_PUBKEY_PUBLIC + " BLOB, "
				+ FIELD_PUBKEY_ENCRYPTED + " INTEGER, "
				+ FIELD_PUBKEY_STARTUP + " INTEGER, "
				+ FIELD_PUBKEY_CONFIRMUSE + " INTEGER DEFAULT 0, "
				+ FIELD_PUBKEY_LIFETIME + " INTEGER DEFAULT 0)");
	}

	@Override
	public void onRobustUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) throws SQLiteException {
		switch (oldVersion) {
			case 1:
				db.execSQL("ALTER TABLE " + TABLE_PUBKEYS
						+ " ADD COLUMN " + FIELD_PUBKEY_CONFIRMUSE + " INTEGER DEFAULT 0");
				db.execSQL("ALTER TABLE " + TABLE_PUBKEYS
						+ " ADD COLUMN " + FIELD_PUBKEY_LIFETIME + " INTEGER DEFAULT 0");
		}
	}

	/**
	 * Delete a specific host by its <code>_id</code> value.
	 */
	//todo 暂时不做数据库.
//	public void deletePubkey(PubkeyBean pubkey) {
//		HostDatabase hostdb = HostDatabase.get(context);
//		hostdb.stopUsingPubkey(pubkey.getId());
//
//		SQLiteDatabase db = getWritableDatabase();
//		db.beginTransaction();
//		try {
//			db.delete(TABLE_PUBKEYS, "_id = ?", new String[] {Long.toString(pubkey.getId())});
//			db.setTransactionSuccessful();
//		} finally {
//			db.endTransaction();
//		}
//	}

	public List<PubkeyBean> allPubkeys() {
		return getPubkeys(null, null);
	}

	public List<PubkeyBean> getAllStartPubkeys() {
		return getPubkeys(FIELD_PUBKEY_STARTUP + " = 1 AND " + FIELD_PUBKEY_ENCRYPTED + " = 0", null);
	}

	public List<PubkeyBean> getPubkeysByNick(String nick){
		return  getPubkeys(FIELD_PUBKEY_NICKNAME +" = ?",new String[]{nick});

	}
	private List<PubkeyBean> getPubkeys(String selection, String[] selectionArgs) {
		SQLiteDatabase db = getReadableDatabase();

		List<PubkeyBean> pubkeys = new LinkedList<PubkeyBean>();

		Cursor c = db.query(TABLE_PUBKEYS, null, selection, selectionArgs, null, null, null);

		if (c != null) {
			final int COL_ID = c.getColumnIndexOrThrow("_id"),
					COL_NICKNAME = c.getColumnIndexOrThrow(FIELD_PUBKEY_NICKNAME),
					COL_TYPE = c.getColumnIndexOrThrow(FIELD_PUBKEY_TYPE),
					COL_PRIVATE = c.getColumnIndexOrThrow(FIELD_PUBKEY_PRIVATE),
					COL_PUBLIC = c.getColumnIndexOrThrow(FIELD_PUBKEY_PUBLIC),
					COL_ENCRYPTED = c.getColumnIndexOrThrow(FIELD_PUBKEY_ENCRYPTED),
					COL_STARTUP = c.getColumnIndexOrThrow(FIELD_PUBKEY_STARTUP),
					COL_CONFIRMUSE = c.getColumnIndexOrThrow(FIELD_PUBKEY_CONFIRMUSE),
					COL_LIFETIME = c.getColumnIndexOrThrow(FIELD_PUBKEY_LIFETIME);

			while (c.moveToNext()) {
				PubkeyBean pubkey = new PubkeyBean();

				pubkey.setId(c.getLong(COL_ID));
				pubkey.setNickname(c.getString(COL_NICKNAME));
				pubkey.setType(c.getString(COL_TYPE));
				pubkey.setPrivateKey(c.getBlob(COL_PRIVATE));
				pubkey.setPublicKey(c.getBlob(COL_PUBLIC));
				pubkey.setEncrypted(c.getInt(COL_ENCRYPTED) > 0);
				pubkey.setStartup(c.getInt(COL_STARTUP) > 0);
				pubkey.setConfirmUse(c.getInt(COL_CONFIRMUSE) > 0);
				pubkey.setLifetime(c.getInt(COL_LIFETIME));

				pubkeys.add(pubkey);
			}

			c.close();
		}

		return pubkeys;
	}

	/**
	 * @param pubkeyId database ID for a desired pubkey
	 * @return object representing the pubkey
	 */
	public PubkeyBean findPubkeyById(long pubkeyId) {
		SQLiteDatabase db = getReadableDatabase();

		Cursor c = db.query(TABLE_PUBKEYS, null,
				"_id = ?", new String[] { String.valueOf(pubkeyId) },
				null, null, null);

		PubkeyBean pubkey = null;

		if (c != null) {
			if (c.moveToFirst())
				pubkey = createPubkeyBean(c);

			c.close();
		}

		return pubkey;
	}

	private PubkeyBean createPubkeyBean(Cursor c) {
		PubkeyBean pubkey = new PubkeyBean();

		pubkey.setId(c.getLong(c.getColumnIndexOrThrow("_id")));
		pubkey.setNickname(c.getString(c.getColumnIndexOrThrow(FIELD_PUBKEY_NICKNAME)));
		pubkey.setType(c.getString(c.getColumnIndexOrThrow(FIELD_PUBKEY_TYPE)));
		pubkey.setPrivateKey(c.getBlob(c.getColumnIndexOrThrow(FIELD_PUBKEY_PRIVATE)));
		pubkey.setPublicKey(c.getBlob(c.getColumnIndexOrThrow(FIELD_PUBKEY_PUBLIC)));
		pubkey.setEncrypted(c.getInt(c.getColumnIndexOrThrow(FIELD_PUBKEY_ENCRYPTED)) > 0);
		pubkey.setStartup(c.getInt(c.getColumnIndexOrThrow(FIELD_PUBKEY_STARTUP)) > 0);
		pubkey.setConfirmUse(c.getInt(c.getColumnIndexOrThrow(FIELD_PUBKEY_CONFIRMUSE)) > 0);
		pubkey.setLifetime(c.getInt(c.getColumnIndexOrThrow(FIELD_PUBKEY_LIFETIME)));

		return pubkey;
	}

	/**
	 * Pull all values for a given column as a list of Strings, probably for use
	 * in a ListPreference. Sorted by <code>_id</code> ascending.
	 */
	public List<CharSequence> allValues(String column) {
		List<CharSequence> list = new LinkedList<CharSequence>();

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(TABLE_PUBKEYS, new String[] { "_id", column },
				null, null, null, null, "_id ASC");

		if (c != null) {
			int COL = c.getColumnIndexOrThrow(column);

			while (c.moveToNext())
				list.add(c.getString(COL));

			c.close();
		}

		return list;
	}

	public String getNickname(long id) {
		String nickname = null;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(TABLE_PUBKEYS, new String[] { "_id",
						FIELD_PUBKEY_NICKNAME }, "_id = ?",
				new String[] { Long.toString(id) }, null, null, null);

		if (c != null) {
			if (c.moveToFirst())
				nickname = c.getString(c.getColumnIndexOrThrow(FIELD_PUBKEY_NICKNAME));

			c.close();
		}

		return nickname;
	}

	/**
	 * @param pubkey
	 */
	public PubkeyBean savePubkey(PubkeyBean pubkey) {
		SQLiteDatabase db = this.getWritableDatabase();
		boolean success = false;

		ContentValues values = pubkey.getValues();

		db.beginTransaction();
		try {
            Log.d(TAG,"原本数据:"+pubkey.getId());
			if (pubkey.getId() > 0) {
				values.remove("_id");
				if (db.update(TABLE_PUBKEYS, values, "_id = ?", new String[] {String.valueOf(pubkey.getId())}) > 0)
					success = true;
			}

			if (!success) {
				long id = db.insert(TABLE_PUBKEYS, null, pubkey.getValues());
				if (id != -1) {
					// TODO add some error handling here?
					pubkey.setId(id);
				}
			}

            Log.d(TAG,"保存数据:"+pubkey.getId());

			db.setTransactionSuccessful();
		}finally {
			db.endTransaction();
		}
		return pubkey;
	}
}
