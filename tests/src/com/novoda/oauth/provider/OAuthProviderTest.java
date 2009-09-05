
package com.novoda.oauth.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.mock.MockContentResolver;

import com.novoda.oauth.provider.OAuth.Registry;

/*
 * tearDown not implemented yet. Ensure the DB is deleted after the run.
 * The ContentResolver prepend "test." to the name of the db. Ensure this is run against an emulator as 
 * it might mess up the content resolver
 */
public class OAuthProviderTest extends ProviderTestCase3<OAuthProvider> {

    private static final Uri REGISTRY_URI = Registry.CONTENT_URI;

    private SQLiteDatabase mDB;

    private MockContentResolver mResolver;

    public OAuthProviderTest(Class<OAuthProvider> providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    public OAuthProviderTest() {
        super(OAuthProvider.class, OAuth.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getMockContext();
        mResolver = getMockContentResolver();
        mDB = getProvider().getDatabase();
        assertTrue(mDB.isOpen());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        clearDB();
    }

    @Override
    public void testAndroidTestCaseSetupProperly() {
        super.testAndroidTestCaseSetupProperly();
    }

    public void testDBCreation() throws Exception {
        assertNotNull(mResolver.query(REGISTRY_URI, null, null, null, null));
    }

    public void testQueryingFirstRow() throws Exception {
        Cursor cursor = mResolver.query(REGISTRY_URI, new String[] {
                "_id", "access_secret"
        }, null, null, null);
        assertNotNull(cursor);
        assertEquals(cursor.getColumnCount(), 2);
        cursor.close();
    }

    public void testGettingScheme() throws Exception {
        assertEquals("vnd.android.cursor.dir/vnd.novoda.oauth", getProvider().getType(REGISTRY_URI));
        assertEquals("vnd.android.cursor.item/vnd.novoda.oauth", getProvider().getType(
                ContentUris.withAppendedId(REGISTRY_URI, 1)));
    }

    public void testSimpleInsert() throws Exception {
        ContentValues values = new ContentValues();

        values.put("consumer_key", "test");
        values.put("consumer_secret", "946295dfa48b47c284a32c50e848d7a1");
        values.put("request_token_url", "http://jaikunovoda.appspot.com/api/request_token");
        values.put("access_token_url", "http://jaikunovoda.appspot.com/api/access_token");
        values.put("authorize_url", "http://jaikunovoda.appspot.com/api/authorize");
        values.put("access_secret", "te");
        values.put("access_token", "htee");

        mResolver.insert(REGISTRY_URI, values);
    }

    public void testSignature() throws Exception {
        OAuthProvider pro = (OAuthProvider)getProvider();
        // assertTrue(pro.isAuthorized("test", "test"));
    }

    public void testShouldOnlyReturnApplicationsOAuthInfo() throws Exception {
        setPackage("com.mypackage");
        ContentValues values = new ContentValues();
        values.put("consumer_key", "test");
        values.put("consumer_secret", "946295dfa48b47c284a32c50e848d7a1");
        values.put("request_token_url", "http://jaikunovoda.appspot.com/api/request_token");
        values.put("access_token_url", "http://jaikunovoda.appspot.com/api/access_token");
        values.put("authorize_url", "http://jaikunovoda.appspot.com/api/authorize");
        values.put("access_secret", "te");
        values.put("access_token", "htee");
        assertNotNull(mResolver.insert(REGISTRY_URI, values));
        Cursor cur = mDB.rawQuery("SELECT " + "signature"
                + " from registry where consumer_secret=?", new String[] {
            "946295dfa48b47c284a32c50e848d7a1"
        });
        assertTrue(cur.moveToFirst());
        assertEquals("com.mypackage", cur.getString(0));
        cur.close();
    }

    /* SQL */
    private static String insert1 = "INSERT INTO providers VALUES(1,'te','htee','http://jaikunovoda.appspot.com/api/access_token',"
            + "'http://jaikunovoda.appspot.com/api/authorize','6a02c9a8ae584673a7dd26a4bdf7d63e','946295dfa48b47c284a32c50e848d7a1', "
            + "'com.novoda.test' , 'jaikunovoda' ,'sig123','http://jaikunovoda.appspot.com/api/request_token',"
            + "1250272171512,1250272171512)";

    private static String insert2 = "INSERT INTO providers VALUES(2,'access_secret','access_token','access_token_url','authorize_url',"
            + "'consumer_key','consumer_secret','com.novoda.test2' , 'jaikunovoda2' ,'sig1234','request_token_url',1,2)";

    private void clearDB() {
        Cursor cur = mDB.rawQuery(
                "SELECT name FROM sqlite_master WHERE type=? AND name!=? AND name!=?",
                new String[] {
                        "table", "android_metada", "sqlite_sequence"
                });
        while (cur.moveToNext())
            mDB.execSQL("DELETE FROM " + cur.getString(0) + ";");
        if (cur != null)
            cur.close();
    }
}
