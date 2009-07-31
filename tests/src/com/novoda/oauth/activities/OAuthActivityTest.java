package com.novoda.oauth.activities;

import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;

public class OAuthActivityTest extends ActivityInstrumentationTestCase2<OAuthActivity> {

	public OAuthActivityTest(String pkg, Class<OAuthActivity> activityClass) {
		super(pkg, activityClass);
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_INSERT);
		this.setActivityIntent(intent);
		
	}

	public OAuthActivityTest() {
		super("com.novoda.oauth", OAuthActivity.class);
	}
	
	public void testInsert() throws Exception {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_INSERT);
		Bundle bundle = new Bundle();
		bundle.putString("consumerKey","");
		bundle.putString("requestTokenURL","requestTokenURL");
		bundle.putString("accessTokenURL","");
		bundle.putString("authorizeURL","");
		intent.putExtras(bundle);
		launchActivityWithIntent("com.novoda.oauth",  OAuthActivity.class, intent);
	}

	@Override
	protected void runTest() throws Throwable {
		super.runTest();
	}
}
