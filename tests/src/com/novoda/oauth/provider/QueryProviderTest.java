
package com.novoda.oauth.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.mock.MockContentResolver;

import com.novoda.oauth.provider.OAuth.Consumers;
import com.novoda.oauth.provider.OAuth.Registry;

/*
 * tearDown not implemented yet. Ensure the DB is deleted after the run.
 * The ContentResolver prepend "test." to the name of the db. Ensure this is run against an emulator as 
 * it might mess up the content resolver
 * 
 */
public class QueryProviderTest extends ProviderTestCase3<OAuthProvider> {

    private static final Uri REGISTRY_URI = Registry.CONTENT_URI;

    private static final Uri AUTHORIZED_URI = Registry.CONTENT_URI.buildUpon()
            .appendQueryParameter("authorized", "true").build();

    private static final Uri ALL_CONSUMERS_FOR_FIRST_REGISTRY = Registry.CONTENT_URI.buildUpon()
            .appendEncodedPath("1/consumers").build();

    private static final String PACKAGE_UNDER_TEST = "com.mytest.package";

    private static final String SIGNATURE_FOR_PACKAGE = "sig1";

    private static final String PACKAGE_UNDER_TEST_2 = "com.myothertest";

    private static final String SIGNATURE_FOR_PACKAGE_2 = "sig2";

    private SQLiteDatabase mDB;

    private MockContentResolver mResolver;

    public QueryProviderTest(Class<OAuthProvider> providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    public QueryProviderTest() {
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

    public void testShouldOnlyReturnPublicAndOwned() throws Exception {
        standardInsert();

        setPackage(PACKAGE_UNDER_TEST);
        setSignature(SIGNATURE_FOR_PACKAGE);
        Cursor cur = mResolver.query(REGISTRY_URI, new String[] {
            "_id"
        }, null, null, null);
        assertNotNull(cur);

        // should only return the owned and public ones... (e.g. not 6 but 2 for
        // the owned one and 1 for the public)
        assertEquals(3, cur.getCount());
        cur.close();
    }

    public void testShouldReturnAllIfPermissionMet() throws Exception {
        standardInsert();
        setPermission(true);
        Cursor cur = mResolver.query(REGISTRY_URI, new String[] {
            "_id"
        }, null, null, null);
        assertNotNull(cur);
        assertEquals(6, cur.getCount());
        cur.close();
    }

    public void testQueryByPackage() throws Exception {
        // TODO
    }

    public void testQueryByID() throws Exception {
        // TODO
    }

    public void testBannedAppShouldNeverReturn() throws Exception {
        // TODO
    }

    public void testQueryForAuthorizedApp() throws Exception {
        standardInsert();
        setPermission(true);
        Cursor cur = mResolver.query(AUTHORIZED_URI, new String[] {
            "_id"
        }, Registry.ACCESS_TOKEN + "!=?", new String[] {
            "NULL"
        }, null);
        assertNotNull(cur);
        assertEquals(4, cur.getCount());
        cur.close();
    }

    public void testQueryForAuthorizedAppWithoutPerm() throws Exception {
        standardInsert();
        setPackage(PACKAGE_UNDER_TEST);
        setPermission(false);
        Cursor cur = mResolver.query(AUTHORIZED_URI, new String[] {
            "_id"
        }, Registry.ACCESS_TOKEN + "!=?", new String[] {
            "NULL"
        }, null);
        assertNotNull(cur);
        assertEquals(1, cur.getCount());
        cur.close();
    }

    public void testQueryAllConsumersForKey() throws Exception {
        standardInsert();
        setPermission(true);
        Cursor cur = mResolver.query(ALL_CONSUMERS_FOR_FIRST_REGISTRY, new String[] {
            "_id"
        }, null, null, null);
        assertEquals(1, cur.getCount());
    }

    public void testShouldReturnOwnersPackageAndActivity() throws Exception {
        standardInsert();

        ContentValues values = createReg("regkey123");
        setPermission(true);
        setPackage(PACKAGE_UNDER_TEST);
        values.put(Consumers.ACTIVITY, ".MyActivity");
        assertNotNull(mResolver.insert(REGISTRY_URI, values));

        Cursor cur = mResolver.query(REGISTRY_URI, new String[] {
                Consumers.ACTIVITY, Consumers.PACKAGE_NAME
        }, Registry.CONSUMER_KEY + "=?", new String[] {
            "regkey123"
        }, null);

        assertEquals(1, cur.getCount());
        assertTrue(cur.moveToFirst());
        assertEquals(PACKAGE_UNDER_TEST, cur.getString(cur
                .getColumnIndexOrThrow(Consumers.PACKAGE_NAME)));
        assertEquals(".MyActivity", cur.getString(cur.getColumnIndexOrThrow(Consumers.ACTIVITY)));
    }

    public void testShouldReturnTheApplicationForRegistry() throws Exception {
        standardInsert();

        Cursor cur = mResolver.query(REGISTRY_URI.buildUpon().appendEncodedPath("1/consumers")
                .build(), new String[] {
            Consumers.IS_BANNED
        }, Consumers.PACKAGE_NAME + "=?", new String[] {
            PACKAGE_UNDER_TEST
        }, null);

        assertEquals(1, cur.getCount());
        assertTrue(cur.moveToFirst());
        assertEquals(0, cur.getInt(cur.getColumnIndexOrThrow(Consumers.IS_BANNED)));
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

    private ContentValues createReg(String rand) {
        ContentValues value = new ContentValues();
        value.put(Registry.ACCESS_TOKEN_URL, "http://access");
        value.put(Registry.REQUEST_TOKEN_URL, "http://request");
        value.put(Registry.AUTHORIZE_URL, "http://authorize");
        value.put(Registry.CONSUMER_KEY, rand);
        value.put(Registry.CONSUMER_SECRET, "secret");
        value.put(Registry.URL, "http://twitter.com");
        return value;
    }

    private ContentValues createRegAuth(String string) {
        ContentValues value = createReg(string);
        value.put(Registry.ACCESS_TOKEN, "token");
        value.put(Registry.ACCESS_SECRET, "secret");
        return value;
    }

    private ContentValues createConsumer(String packageName, long regId, Signature sig,
            boolean isPublic) {
        ContentValues value = new ContentValues();
        value.put(Consumers.PACKAGE_NAME, packageName);
        value.put(Consumers.SIGNATURE, sig.toByteArray());
        value.put(Consumers.IS_SERVICE_PUBLIC, isPublic);
        value.put(Consumers.IS_BANNED, false);
        value.put(Consumers.OWNS_CONSUMER_KEY, true);
        value.put(Consumers.REGISTRY_ID, regId);
        return value;
    }

    private void standardInsert() {
        long[] ids = new long[6];
        ids[0] = mDB.insert("registry", "", createReg("test1"));
        ids[1] = mDB.insert("registry", "", createReg("test2"));
        ids[2] = mDB.insert("registry", "", createRegAuth("test3"));
        ids[3] = mDB.insert("registry", "", createRegAuth("test4"));
        ids[4] = mDB.insert("registry", "", createRegAuth("test5"));
        ids[5] = mDB.insert("registry", "", createRegAuth("test6"));

        mDB.insert("consumers", "", createConsumer(PACKAGE_UNDER_TEST, 1, new Signature(
                SIGNATURE_FOR_PACKAGE), true));
        mDB.insert("consumers", "", createConsumer(PACKAGE_UNDER_TEST, 2, new Signature(
                SIGNATURE_FOR_PACKAGE), true));

        mDB.insert("consumers", "", createConsumer(PACKAGE_UNDER_TEST_2, 3, new Signature(
                SIGNATURE_FOR_PACKAGE_2), true));
        mDB.insert("consumers", "", createConsumer(PACKAGE_UNDER_TEST_2, 4, new Signature(
                SIGNATURE_FOR_PACKAGE_2), false));
        mDB.insert("consumers", "", createConsumer(PACKAGE_UNDER_TEST_2, 5, new Signature(
                SIGNATURE_FOR_PACKAGE_2), false));
        mDB.insert("consumers", "", createConsumer(PACKAGE_UNDER_TEST_2, 6, new Signature(
                SIGNATURE_FOR_PACKAGE_2), false));
    }
}
