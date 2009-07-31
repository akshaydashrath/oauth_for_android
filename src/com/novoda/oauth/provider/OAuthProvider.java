package com.novoda.oauth.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.novoda.oauth.provider.OAuth.Providers;

public class OAuthProvider extends ContentProvider {
	private static final String	TAG					= "OAuth:";

	private static final String	PROVIDER_TABLE_NAME	= "providers";

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String	DATABASE_NAME		= "oauth.db";
		private static final int	DATABASE_VERSION	= 1;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + PROVIDER_TABLE_NAME + " (" + Providers._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Providers.ACCESS_SECRET + " TEXT,"
					+ Providers.ACCESS_TOKEN + " TEXT," + Providers.ACCESS_TOKEN_URL + " TEXT," + Providers.AUTHORIZE_URL + " TEXT," + Providers.CONSUMER_KEY + " TEXT,"
					+ Providers.CONSUMER_SECRET + " TEXT," + Providers.REQUEST_TOKEN_URL + " TEXT," + Providers.CREATED_DATE + " INTEGER," + Providers.MODIFIED_DATE + " INTEGER"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + PROVIDER_TABLE_NAME);
			onCreate(db);
		}
	}

	private static final int				PROVIDERS	= 0;
	private static final int				PROVIDER_ID	= 1;

	private static UriMatcher				sUriMatcher;

	private static HashMap<String, String>	sProviderProjectionMap;

	private DatabaseHelper					mOpenHelper;

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
		switch (sUriMatcher.match(uri)) {
			case PROVIDER_ID:
				return OAuth.Providers.CONTENT_ITEM_TYPE;
			case PROVIDERS:
				return OAuth.Providers.CONTENT_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (sUriMatcher.match(uri) != PROVIDERS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Bare minimum.
		if (!(values.containsKey(Providers.ACCESS_SECRET) && values.containsKey(Providers.ACCESS_TOKEN)) || (values == null))
			throw new IllegalArgumentException("Not enought data, access token and its secret is required: " + values.toString());

		Long now = Long.valueOf(System.currentTimeMillis());

		values.put(OAuth.Providers.CREATED_DATE, now);
		values.put(OAuth.Providers.MODIFIED_DATE, now);
        
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
		long rowId = db.insert(PROVIDER_TABLE_NAME, "", values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Providers.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
			case PROVIDERS:
				qb.setTables(PROVIDER_TABLE_NAME);
				qb.setProjectionMap(sProviderProjectionMap);
				break;

			case PROVIDER_ID:
				qb.setTables(PROVIDER_TABLE_NAME);
				qb.setProjectionMap(sProviderProjectionMap);
				qb.appendWhere(Providers._ID + "=" + uri.getPathSegments().get(1));
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = OAuth.Providers.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(OAuth.AUTHORITY, "providers", PROVIDERS);
		sUriMatcher.addURI(OAuth.AUTHORITY, "providers/#", PROVIDER_ID);

		sProviderProjectionMap = new HashMap<String, String>();
		sProviderProjectionMap.put(Providers._ID, Providers._ID);
		sProviderProjectionMap.put(Providers.ACCESS_SECRET, Providers.ACCESS_SECRET);
		sProviderProjectionMap.put(Providers.ACCESS_TOKEN, Providers.ACCESS_TOKEN);
		sProviderProjectionMap.put(Providers.ACCESS_TOKEN_URL, Providers.ACCESS_TOKEN_URL);
		sProviderProjectionMap.put(Providers.AUTHORIZE_URL, Providers.AUTHORIZE_URL);
		sProviderProjectionMap.put(Providers.CONSUMER_KEY, Providers.CONSUMER_KEY);
		sProviderProjectionMap.put(Providers.CONSUMER_SECRET, Providers.CONSUMER_SECRET);
		sProviderProjectionMap.put(Providers.REQUEST_TOKEN_URL, Providers.REQUEST_TOKEN_URL);

		sProviderProjectionMap.put(Providers.CREATED_DATE, Providers.CREATED_DATE);
		sProviderProjectionMap.put(Providers.MODIFIED_DATE, Providers.MODIFIED_DATE);
	}

}
