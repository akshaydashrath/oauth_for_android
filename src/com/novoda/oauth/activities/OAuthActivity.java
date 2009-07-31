package com.novoda.oauth.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.protocol.RequestAddCookies;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class OAuthActivity extends Activity {
	private static final String	TAG			= "OAuth:";

	private String				consumerKey;
	private String				consumerSecret;

	private String				requestTokenURL;
	private String				accessTokenURL;
	private String				authorizeURL;
	private String				callback	= "oauth:///";

	private String				token;
	private String				tokenSecret;

	private OAuthAccessor		accessor;

	// This is launched by the callback
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, intent.getScheme());
		// token

		// Ensure we are called from the browser's callback
		if (intent.getScheme().contains("oauth")) {
			Log.d(TAG, "in the loop" + intent.getScheme());
			new TokenExchangeTask().execute();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "create: " + consumerKey);

		String action = getIntent().getAction();

		if (action.compareTo(Intent.ACTION_INSERT) == 0) {
			Bundle bundle = getIntent().getExtras();
			consumerKey = bundle.getString("consumerKey");
			consumerSecret = bundle.getString("consumerSecret");
			requestTokenURL = bundle.getString("requestTokenURL");
			accessTokenURL = bundle.getString("accessTokenURL");
			authorizeURL = bundle.getString("authorizeURL");
		}
		ArrayList<Map.Entry<String, String>> tmp = new ArrayList<Map.Entry<String, String>>();
		tmp.add(new OAuth.Parameter("perms", "delete"));
		new RequestTokenRetrievalTask().execute(tmp);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop " + consumerKey + getIntent().toString());
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause " + consumerKey + getIntent().toString());
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "resume " + consumerKey + getIntent().toString());
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG, "rest " + consumerKey + getIntent().toString());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Save away the original text, so we still have it if the activity
		// needs to be killed while paused.
		// outState.putString(ORIGINAL_CONTENT, mOriginalContent);
		Log.i(TAG, "on save " + consumerKey + getIntent().toString());
	}

	private class RequestTokenRetrievalTask extends AsyncTask<Collection<? extends Entry<String, String>>, Void, Uri> {

		@Override
		protected Uri doInBackground(Collection<? extends Entry<String, String>>... params) {
			String url = null;
			OAuthServiceProvider provider = new OAuthServiceProvider(requestTokenURL, authorizeURL, accessTokenURL);
			OAuthConsumer consumer = new OAuthConsumer(callback, consumerKey, consumerSecret, provider);
			OAuthClient client = new OAuthClient(new HttpClient4());
			accessor = new OAuthAccessor(consumer);
			try {
				if (params.length == 1) {
					client.getRequestToken(accessor, "GET", toOAuthParams(params[0]));
					url = accessor.consumer.serviceProvider.userAuthorizationURL + "?oauth_token=" + accessor.requestToken + "&oauth_callback=" + consumer.callbackURL + "&"
							+ toParams(params[0]);
				} else {
					client.getRequestToken(accessor, "GET");
					url = accessor.consumer.serviceProvider.userAuthorizationURL + "?oauth_token=" + accessor.requestToken + "&oauth_callback=" + consumer.callbackURL;
				}
			} catch (Exception e) {
				Log.e(TAG, "Could not get authorize token from " + requestTokenURL + " " + e.getMessage());
				return null;
			}
			Log.d(TAG, url);
			Log.i(TAG, "accessor" + accessor.tokenSecret);
			Log.i(TAG, "accessor" + accessor.accessToken);

			return Uri.parse(url);
		}

		private Collection<? extends Entry> toOAuthParams(Collection<? extends Entry<String, String>> params) {
			ArrayList<Map.Entry<String, String>> tmp = new ArrayList<Map.Entry<String, String>>();
			for (Entry<String, String> entry : params) {
				tmp.add(new OAuth.Parameter(entry.getKey(), entry.getValue()));
			}
			return tmp;
		}

		private String toParams(Collection<? extends Entry<String, String>> params) {
			StringBuffer tmp = new StringBuffer();
			for (Entry<String, String> entry : params) {
				tmp.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
			return tmp.substring(0, tmp.length() - 1).toString();
		}

		@Override
		protected void onPostExecute(Uri authUrl) {

			// saving to DB
			if (null != authUrl) {
				Toast toast = Toast.makeText(OAuthActivity.this, "redirecting to browser autho", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();

				// open a browser window to handle authorisation
				startActivity(new Intent(Intent.ACTION_VIEW).setData(authUrl));
			} else {
				Toast toast = Toast.makeText(OAuthActivity.this, "hum something went wrong, can not get token", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}

		@Override
		protected void onPreExecute() {
		}
	}

	private class TokenExchangeTask extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			OAuthClient client = new OAuthClient(new HttpClient4());
			try {
				client.getAccessToken(accessor, "POST", null);
				Log.d(TAG, "Access token: " + accessor.accessToken);
				Log.d(TAG, "Token secret: " + accessor.tokenSecret);
				token = accessor.accessToken;
				tokenSecret = accessor.tokenSecret;
			} catch (Exception e) {
				Log.e(TAG, "Could not get access token from " + accessTokenURL + " " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			ContentValues values = new ContentValues(8);
			values.put(com.novoda.oauth.provider.OAuth.Providers.ACCESS_SECRET, tokenSecret);
			values.put(com.novoda.oauth.provider.OAuth.Providers.ACCESS_TOKEN, token);
			values.put(com.novoda.oauth.provider.OAuth.Providers.ACCESS_TOKEN_URL, accessTokenURL);
			values.put(com.novoda.oauth.provider.OAuth.Providers.AUTHORIZE_URL, authorizeURL);
			values.put(com.novoda.oauth.provider.OAuth.Providers.CONSUMER_KEY, consumerKey);
			values.put(com.novoda.oauth.provider.OAuth.Providers.CONSUMER_SECRET, consumerSecret);
			values.put(com.novoda.oauth.provider.OAuth.Providers.REQUEST_TOKEN_URL, requestTokenURL);

			Uri uri = OAuthActivity.this.getContentResolver().insert(com.novoda.oauth.provider.OAuth.Providers.CONTENT_URI, values);
			if (uri != null) {
				Intent intent = new Intent();
				intent.putExtra("uri", uri);
				OAuthActivity.this.setResult(Activity.RESULT_OK, intent);
			} else {
				OAuthActivity.this.setResult(Activity.RESULT_CANCELED);
			}
		}

		@Override
		protected void onPreExecute() {
		}
	}
}
