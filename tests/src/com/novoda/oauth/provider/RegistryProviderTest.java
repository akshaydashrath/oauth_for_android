
package com.novoda.oauth.provider;

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
 * 
 * Use case:
 * 1. Application with no permission can not:
 *      a. query the registry
 *      b. 
 * 
 */
public class RegistryProviderTest extends ProviderTestCase3<OAuthProvider> {

    private static final Uri REGISTRY_URI = Registry.CONTENT_URI;

    private SQLiteDatabase mDB;

    private MockContentResolver mResolver;

    public RegistryProviderTest(Class<OAuthProvider> providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    public RegistryProviderTest() {
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

    public void testSimpleInsertForExternalApplication() throws Exception {
        ContentValues value = new ContentValues();
        value.put(Registry.NAME, "twitter");
        value.put(Registry.ACCESS_TOKEN_URL, "http://access");
        value.put(Registry.REQUEST_TOKEN_URL, "http://request");
        value.put(Registry.AUTHORIZE_URL, "http://authorize");
        value.put(Registry.CONSUMER_KEY, "key");
        value.put(Registry.CONSUMER_SECRET, "secret");
        assertNotNull(mResolver.insert(REGISTRY_URI, value));
    }

    public void testShouldFailIfNotEnoughtDataProvided() throws Exception {
        ContentValues value = new ContentValues();
        value.put(Registry.NAME, "twitter");
        value.put(Registry.REQUEST_TOKEN_URL, "http://request");
        value.put(Registry.AUTHORIZE_URL, "http://authorize");
        value.put(Registry.CONSUMER_KEY, "key");
        value.put(Registry.CONSUMER_SECRET, "secret");
        try {
            mResolver.insert(REGISTRY_URI, value);
        } catch (IllegalArgumentException e) {
            assert (true);
            return;
        }
        fail();
    }

    public void testNameShouldBeTheURLHostWhenNoNameProvided() throws Exception {
        ContentValues value = new ContentValues();
        value.put(Registry.ACCESS_TOKEN_URL, "http://access");
        value.put(Registry.REQUEST_TOKEN_URL, "http://request");
        value.put(Registry.AUTHORIZE_URL, "http://authorize");
        value.put(Registry.CONSUMER_KEY, "key");
        value.put(Registry.CONSUMER_SECRET, "secret");
        value.put(Registry.URL, "http://twitter.com");
        assertNotNull(mResolver.insert(REGISTRY_URI, value));
        Cursor cur = mDB.rawQuery("SELECT " + Registry.NAME + " FROM registry", null);
        assertTrue(cur.moveToFirst());
        assertEquals("twitter.com", cur.getString(0));
    }
    
    public void testInsertShouldCreateAnRowInAppTable() throws Exception {
        ContentValues value = new ContentValues();
        value.put(Registry.ACCESS_TOKEN_URL, "http://access");
        value.put(Registry.REQUEST_TOKEN_URL, "http://request");
        value.put(Registry.AUTHORIZE_URL, "http://authorize");
        value.put(Registry.CONSUMER_KEY, "key");
        value.put(Registry.CONSUMER_SECRET, "secret");
        value.put(Registry.URL, "http://twitter.com");
        assertNotNull(mResolver.insert(REGISTRY_URI, value));
        
    }

    /* SQL */
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
