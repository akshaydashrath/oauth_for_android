package com.novoda.oauth.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.novoda.oauth.provider.OAuth.Providers;

public class OAuthProvider extends ContentProvider {

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "oauth.db";
		private static final int DATABASE_VERSION = 1;
		private static final String PROVIDER_TABLE_NAME = "providers";
		private static final String TAG = "OAuth:";

		DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + PROVIDER_TABLE_NAME + " ("
                    + Providers._ID + " INTEGER PRIMARY KEY,"
                    + Providers.ACCESS_SECRET + " TEXT,"
                    + Providers.ACCESS_TOKEN + " TEXT,"
                    + Providers.ACCESS_TOKEN_URL + " TEXT,"
                    + Providers.AUTHORIZE_URL + " TEXT,"
                    + Providers.CONSUMER_KEY + " TEXT,"
                    + Providers.CONSUMER_SECRET + " TEXT,"
                    + Providers.REQUEST_TOKEN + " TEXT,"
                    + Providers.REQUEST_TOKEN_URL + " TEXT,"
                    + Providers.CREATED_DATE + " INTEGER,"
                    + Providers.MODIFIED_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + PROVIDER_TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
