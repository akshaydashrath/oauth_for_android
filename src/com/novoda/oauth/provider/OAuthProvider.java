
package com.novoda.oauth.provider;

import com.novoda.oauth.provider.OAuth.Consumers;
import com.novoda.oauth.provider.OAuth.Registry;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;

public class OAuthProvider extends ContentProvider {
    private static final String TAG = "OAuth:";

    private static final String REGISTRY_TABLE_NAME = "registry";

    public static final String CONSUMER_TABLE_NAME = "consumers";

    public static final String _REGISTRY_CONSUMER = "registry_consumer";

    private static final String DATABASE_NAME = "oauth.db";

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 7;

        DatabaseHelper(Context context, String name) {
            super(context, name, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_REGISTRY);
            db.execSQL(CREATE_TABLE_CONSUMERS);
            db.execSQL(CREATE_TABLE_REGISTRY_CONSUMER);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + REGISTRY_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + CONSUMER_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + _REGISTRY_CONSUMER);
            onCreate(db);
        }

        private static final String CREATE_TABLE_REGISTRY = "CREATE TABLE " + REGISTRY_TABLE_NAME
                + " (" + Registry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Registry.ACCESS_SECRET + " TEXT," + Registry.ACCESS_TOKEN + " TEXT,"
                + Registry.ACCESS_TOKEN_URL + " TEXT," + Registry.AUTHORIZE_URL + " TEXT,"
                + Registry.CONSUMER_KEY + " TEXT UNIQUE," + Registry.CONSUMER_SECRET + " TEXT,"
                + Registry.NAME + " TEXT," + Registry.ICON + " TEXT," + Registry.DESCRIPTION
                + " TEXT," + Registry.URL + " TEXT," + Registry.REQUEST_TOKEN_URL + " TEXT,"
                + Registry.CREATED_DATE + " INTEGER," + Registry.MODIFIED_DATE + " INTEGER" + ");";

        private static final String CREATE_TABLE_CONSUMERS = "CREATE TABLE " + CONSUMER_TABLE_NAME
                + " (" + Consumers._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                + Consumers.APP_NAME + " TEXT,"
                + Consumers.PACKAGE_NAME + " TEXT," 
                + Consumers.ICON + " TEXT);";

        private static final String CREATE_TABLE_REGISTRY_CONSUMER = "CREATE TABLE "
                + _REGISTRY_CONSUMER + " ( fk_registry_id INTEGER NOT NULL REFERENCES "
                + REGISTRY_TABLE_NAME + " (" + Registry._ID
                + ") , fk_consumer_id INTEGER NOT NULL REFERENCES " + CONSUMER_TABLE_NAME + " ("
                + Consumers._ID
                + "), authorized BOOLEAN, PRIMARY KEY (fk_registry_id, fk_consumer_id));";

        // TODO creating triggers for the FK and logging
        // private static final String CREATE_TRIGGERS = "";
    }

    private static final int REGISTRY = 0;

    private static final int REGISTRY_ID = 1;

    private static UriMatcher sUriMatcher;

    private static HashMap<String, String> sProviderProjectionMap;

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext(), DATABASE_NAME);
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (sUriMatcher.match(uri) != REGISTRY) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Bare minimum.
        if (!(values.containsKey(Registry.CONSUMER_KEY)
                && values.containsKey(Registry.ACCESS_TOKEN_URL)
                && values.containsKey(Registry.AUTHORIZE_URL)
                && values.containsKey(Registry.REQUEST_TOKEN_URL) && values
                .containsKey(Registry.CONSUMER_SECRET))
                || (values == null))
            throw new IllegalArgumentException(
                    "Not enought data: consumer key, consumer secret, all 3 OAuth URLs are required: "
                            + values.toString());

        Long now = Long.valueOf(System.currentTimeMillis());

        // values.put(Registry.PACKAGE_NAME, getContext().getPackageName());
        values.put(OAuth.Registry.CREATED_DATE, now);
        values.put(OAuth.Registry.MODIFIED_DATE, now);

        // setting the name if none provided
        if (!values.containsKey(Registry.NAME) && values.containsKey(Registry.URL)) {
            values.put(Registry.NAME, Uri.parse(values.getAsString(Registry.URL)).getHost());
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(REGISTRY_TABLE_NAME, "", values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Registry.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        // TODO
        Log
                .e(
                        TAG,
                        "if you intend to regenerate a token/secret from a previous conusmer key, use ACTION_UPDATE instead;");
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case REGISTRY:
                qb.setTables(REGISTRY_TABLE_NAME);
                qb.setProjectionMap(sProviderProjectionMap);
                break;

            case REGISTRY_ID:
                qb.setTables(REGISTRY_TABLE_NAME);
                qb.setProjectionMap(sProviderProjectionMap);
                qb.appendWhere(Registry._ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = OAuth.Registry.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        // if
        // (getContext().checkCallingPermission("com.novoda.oauth.ACCESS_OAUTH_INFORMATION")
        // == PackageManager.PERMISSION_GRANTED) {
        // Cursor c = qb.query(db, projection, selection, selectionArgs, null,
        // null, orderBy);
        // }

        Log.i(TAG, getContext().getPackageName());

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case REGISTRY:
                count = db.delete(REGISTRY_TABLE_NAME, where, whereArgs);
                break;

            case REGISTRY_ID:
                String providerId = uri.getPathSegments().get(1);
                count = db.delete(REGISTRY_TABLE_NAME, Registry._ID + "=" + providerId
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case REGISTRY_ID:
                return OAuth.Registry.CONTENT_ITEM_TYPE;
            case REGISTRY:
                return OAuth.Registry.CONTENT_TYPE;
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
    protected boolean isAuthorized(Signature s1, Signature s2) {
        return Arrays.equals(s1.toByteArray(), s2.toByteArray());
    }

    protected SQLiteDatabase getDatabase() {
        return mOpenHelper.getWritableDatabase();
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(OAuth.AUTHORITY, "registry", REGISTRY);
        sUriMatcher.addURI(OAuth.AUTHORITY, "registry/#", REGISTRY_ID);

        sProviderProjectionMap = new HashMap<String, String>();
        sProviderProjectionMap.put(Registry._ID, Registry._ID);
        sProviderProjectionMap.put(Registry.ACCESS_SECRET, Registry.ACCESS_SECRET);
        sProviderProjectionMap.put(Registry.ACCESS_TOKEN, Registry.ACCESS_TOKEN);
        sProviderProjectionMap.put(Registry.ACCESS_TOKEN_URL, Registry.ACCESS_TOKEN_URL);
        sProviderProjectionMap.put(Registry.AUTHORIZE_URL, Registry.AUTHORIZE_URL);
        sProviderProjectionMap.put(Registry.CONSUMER_KEY, Registry.CONSUMER_KEY);
        sProviderProjectionMap.put(Registry.CONSUMER_SECRET, Registry.CONSUMER_SECRET);
        sProviderProjectionMap.put(Registry.REQUEST_TOKEN_URL, Registry.REQUEST_TOKEN_URL);
        sProviderProjectionMap.put(Registry.NAME, Registry.NAME);
        sProviderProjectionMap.put(Registry.ICON, Registry.ICON);
        sProviderProjectionMap.put(Registry.DESCRIPTION, Registry.DESCRIPTION);
        sProviderProjectionMap.put(Registry.URL, Registry.URL);
        sProviderProjectionMap.put(Registry.CREATED_DATE, Registry.CREATED_DATE);
        sProviderProjectionMap.put(Registry.MODIFIED_DATE, Registry.MODIFIED_DATE);
    }
}
