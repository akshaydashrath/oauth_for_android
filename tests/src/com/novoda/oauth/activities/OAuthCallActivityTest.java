
package com.novoda.oauth.activities;


import com.novoda.oauth.Intent;
import com.novoda.oauth.provider.OAuth.Registry;

import android.content.ContentUris;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;

public class OAuthCallActivityTest extends ActivityInstrumentationTestCase2<OAuthCallActivity> {

    private Intent intent;

    public OAuthCallActivityTest() {
        super("com.novoda.oauth", OAuthCallActivity.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        intent = new Intent();
        intent.setAction(Intent.OAUTH_CALL);
        // default for jaikunovoda to speed up testing
        intent.setData(ContentUris.withAppendedId(Registry.CONTENT_URI, 5));
        setActivityIntent(intent);
    }
    
    public void testStartingActivity() throws Exception {
        Bundle extras = new Bundle();
        
        launchActivity("com.novoda.oauth", OAuthCallActivity.class, extras);
    }
    
}
