
package com.novoda.oauth.activities;


import android.content.ContentValues;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.novoda.oauth.tests.R;

public class OauthListingUnitTest extends ActivityUnitTestCase<OAuthListing> {

    private Intent mStartIntent;

    public OauthListingUnitTest(Class<OAuthListing> activityClass) {
        super(activityClass);
    }

    public OauthListingUnitTest() {
        super(OAuthListing.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mStartIntent = new Intent(Intent.ACTION_MAIN);
    }

    public void testParsingXML() throws Exception {
        OAuthListing a = startActivity(mStartIntent, null, null);
        ContentValues[] list = a.parseProviders(getInstrumentation().getContext().getResources().getXml(R.xml.providers));
        ContentValues twitter = list[0];
        ContentValues jaiku = list[1];
        assertEquals("Twitter", twitter.get("name"));
        assertEquals("url", twitter.get("url"));
        assertEquals("Jaiku", jaiku.get("name"));
        assertEquals("url", jaiku.get("url"));
    }
}
