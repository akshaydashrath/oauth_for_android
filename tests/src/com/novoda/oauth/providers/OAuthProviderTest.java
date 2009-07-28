package com.novoda.oauth.providers;

import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

public class OAuthProviderTest extends AndroidTestCase {

	private static final String PROVIDER_URI = "content://com.novoda.oauth.provider.OAuth/providers";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void testAndroidTestCaseSetupProperly() {
		super.testAndroidTestCaseSetupProperly();
	}

	public void testDBCreation() throws Exception {
		assertNotNull(getContext().getContentResolver().query(
				Uri.parse(PROVIDER_URI), null, null, null, null));
	}

	public void testQueryingFirstRow() throws Exception {
		Cursor cursor = getContext().getContentResolver().query(
				Uri.parse(PROVIDER_URI), new String[] {"_id", "access_secret"}, null, null, null);
		assertNotNull(cursor);
		assertEquals(cursor.getColumnCount(), 2);
		assertTrue(cursor.moveToFirst());
		assertEquals(0,cursor.getInt(0));
		assertEquals("access_secret", cursor.getString(1));
		cursor.close();
	}
}
