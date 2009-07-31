package com.novoda.oauth.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;

public class OAuthListing extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(this, OAuthActivity.class);
		intent.setAction(Intent.ACTION_INSERT);
		
		Bundle bundle = new Bundle();
		bundle.putString("consumerKey","6a02c9a8ae584673a7dd26a4bdf7d63e");
		bundle.putString("consumerSecret", "946295dfa48b47c284a32c50e848d7a1");
		bundle.putString("requestTokenURL","http://jaikunovoda.appspot.com/api/request_token");
		bundle.putString("accessTokenURL","http://jaikunovoda.appspot.com/api/access_token");
		bundle.putString("authorizeURL","http://jaikunovoda.appspot.com/api/authorize");
		
		intent.putExtras(bundle);
		startActivity(intent);
		
	}
}
