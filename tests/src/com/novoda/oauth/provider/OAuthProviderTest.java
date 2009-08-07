package com.novoda.oauth.provider;

import com.novoda.oauth.provider.OAuth.Providers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;

/*
 * tearDown not implemented yet. Ensure the DB is deleted after the run.
 * The mockContentResolver prepend "test." to the name of the db.
 */
public class OAuthProviderTest extends ProviderTestCase2<OAuthProvider> {

	private static final String	PROVIDER_URI	= "content://com.novoda.oauth.provider.OAuth/providers";
	private static final String	DATABASE_NAME	= "oauth.db";

	private static final String	simpleInsert	= "INSERT INTO providers (access_secret,access_token,access_token_url,authorize_url,consumer_key,consumer_secret,request_token_url,created,modified)"
														+ " VALUES (\"access_secret\",\"access_token\",\"access_token_url\",\"authorize_url\",\"consumer_key\",\"consumer_secret\",\"request_token_url\", 1, 2);";
	private static final String	TAG				= "OAuth:";
	private ContentResolver		resolver;

	public OAuthProviderTest(Class<OAuthProvider> providerClass, String providerAuthority) {
		super(providerClass, providerAuthority);
	}

	public OAuthProviderTest() throws IllegalAccessException, InstantiationException {
		super(OAuthProvider.class, "com.novoda.oauth.provider.OAuth");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		resolver = newResolverWithContentProviderFromSql(getContext(), "test.", OAuthProvider.class,
				"com.novoda.oauth.provider.OAuth", "oauth.db", 3, create);

		SQLiteDatabase db = SQLiteDatabase.openDatabase(mContext.getDatabasePath("test." + DATABASE_NAME)
				.getCanonicalPath(), null, SQLiteDatabase.OPEN_READWRITE);
		db.execSQL(simpleInsert);
	}

	@Override
	public void testAndroidTestCaseSetupProperly() {
		super.testAndroidTestCaseSetupProperly();
	}

	public void testDBCreation() throws Exception {
		assertNotNull(resolver.query(Uri.parse(PROVIDER_URI), null, null, null, null));
	}

	public void testQueryingFirstRow() throws Exception {
		Cursor cursor = resolver.query(Uri.parse(PROVIDER_URI), new String[] { "_id", "access_secret" }, null, null,
				null);
		assertNotNull(cursor);
		assertEquals(cursor.getColumnCount(), 2);
		cursor.close();
	}

	public void testGettingScheme() throws Exception {
		assertEquals("vnd.android.cursor.dir/vnd.novoda.oauth", getProvider().getType(Uri.parse(PROVIDER_URI)));
		assertEquals("vnd.android.cursor.item/vnd.novoda.oauth", getProvider().getType(Uri.parse(PROVIDER_URI + "/1")));
	}

	public void testSimpleInsert() throws Exception {
		ContentValues values = new ContentValues();

		values.put("consumer_key", "6a02c9a8ae584673a7dd26a4bdf7d63e");
		values.put("consumer_secret", "946295dfa48b47c284a32c50e848d7a1");
		values.put("request_token_url", "http://jaikunovoda.appspot.com/api/request_token");
		values.put("access_token_url", "http://jaikunovoda.appspot.com/api/access_token");
		values.put("authorize_url", "http://jaikunovoda.appspot.com/api/authorize");
		values.put("access_secret", "te");
		values.put("access_token", "htee");

		resolver.insert(Uri.parse("content://com.novoda.oauth.provider.OAuth/providers"), values);
	}

	public void testSignature() throws Exception {
		OAuthProvider pro = (OAuthProvider) getProvider();
		//assertTrue(pro.isAuthorized("test", "test"));
	}

	/* SQL */
	private static String	create	= "CREATE TABLE " + "providers " + " (" + Providers._ID
											+ " INTEGER PRIMARY KEY AUTOINCREMENT," + Providers.ACCESS_SECRET
											+ " TEXT," + Providers.ACCESS_TOKEN + " TEXT," + Providers.ACCESS_TOKEN_URL
											+ " TEXT," + Providers.AUTHORIZE_URL + " TEXT," + Providers.CONSUMER_KEY
											+ " TEXT," + Providers.CONSUMER_SECRET + " TEXT," + "package_name"
											+ " TEXT," + "app_name" + " TEXT," + "signature" + " TEXT,"
											+ Providers.REQUEST_TOKEN_URL + " TEXT," + Providers.CREATED_DATE
											+ " INTEGER," + Providers.MODIFIED_DATE + " INTEGER" + ");";
}
