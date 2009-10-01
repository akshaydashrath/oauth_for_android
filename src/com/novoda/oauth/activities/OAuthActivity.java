
package com.novoda.oauth.activities;

import com.novoda.oauth.R;
import com.novoda.oauth.provider.OAuth.Registry;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.Serializable;
import java.util.HashMap;

public class OAuthActivity extends Activity {
    private static final String TAG = "OAuth:";

    private static final int BROWSER_ACTIVITY = 0;

    private String consumerKey;

    private String consumerSecret;

    private String requestTokenURL;

    private String accessTokenURL;

    private String authorizeURL;

    // Default callback URL
    private String callback = null;

    // Extra parameters to pass
    private HashMap<String, String> parameters;

    private String token;

    private String tokenSecret;

    private String packageName;

    private String appName;

    private OAuthAccessor accessor;

    // This is launched by the callback
    protected void onNewIntent(Intent intent) {
        // Ensure we are called from the browser's callback
        if (intent.getScheme().contains("oauth")) {
            Log.d(TAG, "Getting the OAuth Token for request token: "
                    + Uri.parse(intent.getDataString()).getQueryParameter(OAuth.OAUTH_TOKEN));
            finishActivity(BROWSER_ACTIVITY);
            new TokenExchangeTask().execute();
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();

        this.setVisible(false);

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
                    parameters = (HashMap<String, String>)ser;
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

            oauth.signpost.OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey,
                    consumerSecret, SignatureMethod.HMAC_SHA1);

            oauth.signpost.OAuthProvider provider = new DefaultOAuthProvider(consumer,
                    requestTokenURL, accessTokenURL, authorizeURL);

            try {

                url = provider.retrieveRequestToken(callback);
            } catch (OAuthMessageSignerException e) {
                e.printStackTrace();
            } catch (OAuthNotAuthorizedException e) {
                e.printStackTrace();
            } catch (OAuthExpectationFailedException e) {
                e.printStackTrace();
            } catch (OAuthCommunicationException e) {
                e.printStackTrace();
            }

            // OAuthServiceProvider provider = new
            // OAuthServiceProvider(requestTokenURL, authorizeURL,
            // accessTokenURL);
            // OAuthConsumer consumer = new OAuthConsumer(callback, consumerKey,
            // consumerSecret,
            // provider);
            // OAuthClient client = new OAuthClient(new HttpClient4());
            // accessor = new OAuthAccessor(consumer);

            // try {
            //
            // client.getRequestToken(accessor);
            // parameters.put(OAuth.OAUTH_TOKEN, accessor.requestToken);
            // if (callback != null)
            // parameters.put(OAuth.OAUTH_CALLBACK, callback);
            // url =
            // OAuth.addParameters(accessor.consumer.serviceProvider.userAuthorizationURL,
            // parameters.entrySet());
            // } catch (Exception e) {
            // Log.e(TAG, "Could not get authorize token from " +
            // requestTokenURL + " "
            // + e.getMessage());
            // return null;
            // }
            return Uri.parse(url);
        }

        @Override
        protected void onPostExecute(Uri authUrl) {

            // saving to DB
            if (null != authUrl) {
                Toast toast = Toast.makeText(OAuthActivity.this, R.string.browser_redirect,
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                // open a browser window to handle authorisation
                startActivity(new Intent(Intent.ACTION_VIEW).setData(authUrl).addFlags(
                        Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
                                | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));

                Log.i(TAG, authUrl.toString());
            } else {
                Toast toast = Toast.makeText(OAuthActivity.this,
                        "hum something went wrong, can not get token", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
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
                Log.e(TAG, "Could not get access token from " + accessTokenURL + " "
                        + e.getMessage());
            }
            return new Boolean(false);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if (b) {
                ContentValues values = new ContentValues(8);

                values.put(Registry.ACCESS_SECRET, tokenSecret);
                values.put(Registry.ACCESS_TOKEN, token);
                values.put(Registry.ACCESS_TOKEN_URL, accessTokenURL);
                values.put(Registry.AUTHORIZE_URL, authorizeURL);
                values.put(Registry.CONSUMER_KEY, consumerKey);
                values.put(Registry.CONSUMER_SECRET, consumerSecret);
                values.put(Registry.REQUEST_TOKEN_URL, requestTokenURL);
                // values.put(com.novoda.oauth.provider.OAuth.Registry.APP_NAME,
                // appName);
                // values.put(com.novoda.oauth.provider.OAuth.Registry.PACKAGE_NAME,
                // packageName);

                Uri uri = OAuthActivity.this.getContentResolver().insert(
                        com.novoda.oauth.provider.OAuth.Registry.CONTENT_URI, values);

                // Granting the package access to the newly created OAuth table.
                // This is actually quite dangerous as anybody could just take
                // that
                // package name...
                // TODO better security mechanism
                OAuthActivity.this.grantUriPermission(packageName, uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (uri != null) {
                    Intent intent = new Intent();
                    intent.putExtra("uri", uri);
                    OAuthActivity.this.setResult(Activity.RESULT_OK, intent);
                } else {
                    OAuthActivity.this.setResult(Activity.RESULT_CANCELED);
                }
                finish();
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }

}
