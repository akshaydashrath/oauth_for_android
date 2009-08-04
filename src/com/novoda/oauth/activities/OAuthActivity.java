package com.novoda.oauth.activities;

import java.io.Serializable;
import java.util.HashMap;

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
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.novoda.oauth.R;

public class OAuthActivity extends Activity {
	private static final String		TAG			= "OAuth:";

	private String					consumerKey;
	private String					consumerSecret;

	private String					requestTokenURL;
	private String					accessTokenURL;
	private String					authorizeURL;

	// Default callback URL
	private String					callback	= null;
	// Extra parameters to pass
	private HashMap<String, String>	parameters;

	private String					token;
	private String					tokenSecret;

	private String					packageName;
	private String					appName;

	private OAuthAccessor			accessor;

	private WebView					webview;

	// This is launched by the callback
	protected void onNewIntent(Intent intent) {
		Log.i(TAG, intent.toString());
		// Ensure we are called from the browser's callback
		if (intent.getScheme().contains("oauth")) {
			Log.d(TAG, "Getting the OAuth Token: " + intent.getScheme());
			new TokenExchangeTask().execute();
		} else if (intent.getScheme().contains("http") && intent.getData().getHost().contains("oauth.local")) {
			Log.d(TAG, "Getting the OAuth Token: " + intent);
			new TokenExchangeTask().execute();
		}
	};

	private class HelloWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			if (Uri.parse(url).getHost().compareTo("oauth.local") == 0) {
				new TokenExchangeTask().execute();
			}
			return true;
		}

		@Override
		public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
			return false;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			Log.i(TAG, "Receive error: " + failingUrl);
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			super.onLoadResource(view, url);
			if (Uri.parse(url).getHost().compareTo("oauth.local") == 0) {
				new TokenExchangeTask().execute();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.oauth_activity);
		webview = (WebView) findViewById(R.id.webview);
		webview.setWebViewClient(new HelloWebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);

		String action = getIntent().getAction();
		if (action.compareTo(Intent.ACTION_INSERT) == 0) {
			Bundle bundle = getIntent().getExtras();

			// This is needed
			consumerKey = bundle.getString("consumerKey");
			consumerSecret = bundle.getString("consumerSecret");
			requestTokenURL = bundle.getString("requestTokenURL");
			accessTokenURL = bundle.getString("accessTokenURL");
			authorizeURL = bundle.getString("authorizeURL");
			packageName = bundle.getString("packageName");
			appName = bundle.getString("appName");

			parameters = new HashMap<String, String>();
			if (bundle.containsKey("callback"))
				callback = bundle.getString("callback");

			if (bundle.containsKey("params")) {
				Serializable ser = bundle.getSerializable("params");
				if (ser instanceof HashMap<?, ?>) {
					parameters = (HashMap<String, String>) ser;
				}
			}
		}
		new RequestTokenRetrievalTask().execute();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("consumerKey", consumerKey);
		outState.putString("consumerSecret", consumerSecret);
		outState.putString("requestTokenURL", requestTokenURL);
		outState.putString("accessTokenURL", accessTokenURL);
		outState.putString("authorizeURL", authorizeURL);
		outState.putString("packageName", packageName);
		outState.putString("appName", appName);
	}

	private class RequestTokenRetrievalTask extends AsyncTask<Void, Void, Uri> {

		@Override
		protected Uri doInBackground(Void... params) {
			String url = null;
			OAuthServiceProvider provider = new OAuthServiceProvider(requestTokenURL, authorizeURL, accessTokenURL);
			OAuthConsumer consumer = new OAuthConsumer(callback, consumerKey, consumerSecret, provider);
			OAuthClient client = new OAuthClient(new HttpClient4());
			accessor = new OAuthAccessor(consumer);

			try {

				client.getRequestToken(accessor);
				parameters.put(OAuth.OAUTH_TOKEN, accessor.requestToken);
				if (callback != null)
					parameters.put(OAuth.OAUTH_CALLBACK, callback);
				url = OAuth
						.addParameters(accessor.consumer.serviceProvider.userAuthorizationURL, parameters.entrySet());
			} catch (Exception e) {
				Log.e(TAG, "Could not get authorize token from " + requestTokenURL + " " + e.getMessage());
				return null;
			}
			return Uri.parse(url);
		}

		@Override
		protected void onPostExecute(Uri authUrl) {

			// saving to DB
			if (null != authUrl) {
				Toast toast = Toast.makeText(OAuthActivity.this, "redirecting to browser", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();

				// open a browser window to handle authorisation
				// startActivity(new
				// Intent(Intent.ACTION_VIEW).setData(authUrl));
				Log.i(TAG, authUrl.toString());
				webview.loadUrl(authUrl.toString());
			} else {
				Toast toast = Toast.makeText(OAuthActivity.this, "hum something went wrong, can not get token",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}

		@Override
		protected void onPreExecute() {
		}
	}

	private class TokenExchangeTask extends AsyncTask<Object, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Object... params) {
			OAuthClient client = new OAuthClient(new HttpClient4());
			try {
				client.getAccessToken(accessor, null, null);
				Log.d(TAG, "Access token: " + accessor.accessToken);
				Log.d(TAG, "Token secret: " + accessor.tokenSecret);
				token = accessor.accessToken;
				tokenSecret = accessor.tokenSecret;
				return true;
			} catch (Exception e) {
				Log.e(TAG, "Could not get access token from " + accessTokenURL + " " + e.getMessage());
			}
			return new Boolean(false);
		}

		@Override
		protected void onPostExecute(Boolean b) {
			if (b) {
				ContentValues values = new ContentValues(8);

				values.put(com.novoda.oauth.provider.OAuth.Providers.ACCESS_SECRET, tokenSecret);
				values.put(com.novoda.oauth.provider.OAuth.Providers.ACCESS_TOKEN, token);
				values.put(com.novoda.oauth.provider.OAuth.Providers.ACCESS_TOKEN_URL, accessTokenURL);
				values.put(com.novoda.oauth.provider.OAuth.Providers.AUTHORIZE_URL, authorizeURL);
				values.put(com.novoda.oauth.provider.OAuth.Providers.CONSUMER_KEY, consumerKey);
				values.put(com.novoda.oauth.provider.OAuth.Providers.CONSUMER_SECRET, consumerSecret);
				values.put(com.novoda.oauth.provider.OAuth.Providers.REQUEST_TOKEN_URL, requestTokenURL);
				values.put(com.novoda.oauth.provider.OAuth.Providers.APP_NAME, appName);
				values.put(com.novoda.oauth.provider.OAuth.Providers.PACKAGE_NAME, packageName);

				Uri uri = OAuthActivity.this.getContentResolver().insert(
						com.novoda.oauth.provider.OAuth.Providers.CONTENT_URI, values);

				// Granting the package access to the newly created OAuth table.
				// This is actually quite dangerous as anybody could just take
				// that
				// package name...
				// TODO better security mechanism
				OAuthActivity.this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

				if (uri != null) {
					Intent intent = new Intent();
					intent.putExtra("uri", uri);
					OAuthActivity.this.setResult(Activity.RESULT_OK, intent);
				} else {
					OAuthActivity.this.setResult(Activity.RESULT_CANCELED);
				}
			}
		}

		@Override
		protected void onPreExecute() {
		}
	}

}
