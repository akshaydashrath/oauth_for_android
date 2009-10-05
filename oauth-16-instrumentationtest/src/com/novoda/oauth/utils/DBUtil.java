package com.novoda.oauth.utils;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBUtil {
	private Context				mContext;
	private SQLiteDatabase		mDB;

	//private DatabaseArchiver	mDBA;

	public DBUtil(Context context, String dbName) {
		mContext = context;
		mDB = context.openOrCreateDatabase(dbName, Context.MODE_WORLD_WRITEABLE, null);
	}

	private Context getContext() {
		return mContext;
	}

	/**
	 * 
	 * Will execute the SQL file, line by line. There is no strict checking for
	 * the SQL so it is recommended to test the SQL file using external tools
	 * first. For instance the following tools can help ensure the SQL is
	 * correct:
	 * 
	 * <a href=http://sqlitebrowser.sourceforge.net/>SQLiteBrowser</a> <a
	 * href=https://addons.mozilla.org/en-US/firefox/addon/5817>FF addin for
	 * SQLite</a>
	 * 
	 * @param resId
	 *            the resource id (under raw/)
	 */
	public boolean executeDB(int resId) {
		SqlScanner insertDB = new SqlScanner(getContext().getResources().openRawResource(resId));
		mDB.beginTransaction();
		while (insertDB.hasNext()) {
			try {
				mDB.execSQL(insertDB.next());
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		mDB.setTransactionSuccessful();
		mDB.endTransaction();
		
		try {
			insertDB.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean executeDB(InputStream in) {
		SqlScanner insertDB = new SqlScanner(in);
		mDB.beginTransaction();
		while (insertDB.hasNext()) {
			try {
				mDB.execSQL(insertDB.next());
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		mDB.setTransactionSuccessful();
		mDB.endTransaction();
		
		try {
			insertDB.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public SQLiteDatabase getDatabase() {
		return mDB;
	}
}
