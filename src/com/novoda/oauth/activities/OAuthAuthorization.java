package com.novoda.oauth.activities;

import android.app.AliasActivity;
import android.os.Bundle;
import android.util.Log;

public class OAuthAuthorization extends AliasActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("OAuth:", "in this");
	}
}
