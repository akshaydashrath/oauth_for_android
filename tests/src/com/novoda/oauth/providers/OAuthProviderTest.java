package com.novoda.oauth.providers;

import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

public class OAuthProviderTest extends AndroidTestCase {
	@Override
	public void testAndroidTestCaseSetupProperly() {
		super.testAndroidTestCaseSetupProperly();
	}

	public void testSetup() throws Exception {
		Cursor cursor = getContext()
				.getContentResolver()
				.query(
						Uri
								.parse("content://com.novoda.oauth.provider.OAuth/providers"),
						null, null, null, null);
		assertNotNull(cursor);
	}
}
