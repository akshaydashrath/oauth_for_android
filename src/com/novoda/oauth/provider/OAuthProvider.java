package com.novoda.oauth.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Log;

import com.novoda.oauth.provider.OAuth.Providers;

public class OAuthProvider extends ContentProvider {
	private static final String	TAG					= "OAuth:";

	private static final String	PROVIDER_TABLE_NAME	= "providers";
	private static final String	DATABASE_NAME		= "oauth.db";

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		private static final int	DATABASE_VERSION	= 5;

		DatabaseHelper(Context context, String name) {
			super(context, name, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + PROVIDER_TABLE_NAME + " (" + Providers._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + Providers.ACCESS_SECRET + " TEXT,"
					+ Providers.ACCESS_TOKEN + " TEXT," + Providers.ACCESS_TOKEN_URL + " TEXT,"
					+ Providers.AUTHORIZE_URL + " TEXT," + Providers.CONSUMER_KEY + " TEXT UNIQUE,"
					+ Providers.CONSUMER_SECRET + " TEXT," + "package_name" + " TEXT," + "app_name" + " TEXT,"
					+ "signature" + " TEXT," + Providers.REQUEST_TOKEN_URL + " TEXT," + Providers.CREATED_DATE
					+ " INTEGER," + Providers.MODIFIED_DATE + " INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
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
		mOpenHelper = new DatabaseHelper(getContext(), DATABASE_NAME);
		return true;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		PackageManager manager = getContext().getPackageManager();
		String packageName = manager.getPackagesForUid(Binder.getCallingUid())[0];
		PackageInfo pinfo = null;
		Signature si = null;
		try {
			pinfo = manager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			si = pinfo.signatures[0];
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		String name = manager.getApplicationLabel(pinfo.applicationInfo).toString();

		Log.i(TAG, "name : " + name + " packageManager : " + packageName);

		if (sUriMatcher.match(uri) != PROVIDERS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Bare minimum.
		if (!(values.containsKey(Providers.ACCESS_SECRET) && values.containsKey(Providers.ACCESS_TOKEN))
				|| (values == null))
			throw new IllegalArgumentException("Not enought data, access token and its secret is required: "
					+ values.toString());

		values.put("package_name", packageName);
		values.put("app_name", name);
		values.put("signature", si.toByteArray());

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

		// TODO
		Log.e(TAG,
				"if you intend to regenerate a token/secret from a previous conusmer key, use ACTION_UPDATE instead;");
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
		
		if (c.moveToFirst()) {
			String signature = c.getString(c.getColumnIndex("signature"));
			
			PackageManager manager = getContext().getPackageManager();
			String packageName = manager.getPackagesForUid(Binder.getCallingUid())[0];
			PackageInfo pinfo = null;
			Signature si = null;
			try {
				pinfo = manager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
				si = pinfo.signatures[0];
//				if (!isAuthorized(si.toString(), signature))
//					throw new SecurityException("This application is not authorized to query with: " +  uri.getPathSegments().get(1));
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}


		c.requery();
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
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
	
	/*
	 * For each update and read, check that the calling application has the same
	 * signature as the one that inserted the OAuth providers. This is an addon
	 * on top of the fact that the calling application should not insert his
	 * token and secret.
	 */
	protected boolean isAuthorized(String caller, String callee) {
		if (getContext().getPackageManager().checkSignatures(caller, callee) == PackageManager.SIGNATURE_MATCH)
			return true;
		return false;
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

		/** non public entries **/
		sProviderProjectionMap.put("app_name", "app_name");
		sProviderProjectionMap.put("signature", "signature");
		sProviderProjectionMap.put("package_name", "package_name");
	}

}
