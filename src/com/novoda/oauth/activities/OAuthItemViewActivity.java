
package com.novoda.oauth.activities;

import com.novoda.oauth.R;
import com.novoda.oauth.provider.OAuth.Consumers;
import com.novoda.oauth.provider.OAuth.Registry;

import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class OAuthItemViewActivity extends TabActivity {

    private static final String TAG = "OAuth:";

    private Cursor cursor;

    private Intent intent;

    private PackageManager manager;

    // OAuth Stuff
    private static final String CALLBACK = "x-oauth-android://callback";

    private String consumerKey;

    private String consumerSecret;

    private String requestTokenURL;

    private String accessTokenURL;

    private String authorizeURL;

    public HashMap<String, String> parameters = new HashMap<String, String>();

    public String token;

    public String tokenSecret;

    private TabHost mTabHost;

    public DefaultOAuthProvider provider;

    public String oauth_verifier = "";

    public String token2 = "";

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, intent.toString());

        // Ensure we are called from the browser's callback
        if (intent.getScheme().contains("x-oauth-android")) {
            oauth_verifier = Uri.parse(intent.getDataString()).getQueryParameter("oauth_verifier");
            token2 = Uri.parse(intent.getDataString()).getQueryParameter("oauth_token");

            Log.d(TAG, "Getting the OAuth Token for request token: " + oauth_verifier + " "
                    + token2);
        }
        TokenExchangeTask task = new TokenExchangeTask();
        task.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_service_view);

        Intent viewConsumers = new Intent(Intent.ACTION_VIEW, getIntent().getData().buildUpon()
                .appendEncodedPath("consumers").build());

        mTabHost = getTabHost();

        mTabHost.addTab(mTabHost.newTabSpec("info").setIndicator("",
                getResources().getDrawable(android.R.drawable.ic_menu_info_details)).setContent(
                R.id.ll));

        mTabHost.addTab(mTabHost.newTabSpec("consumers").setIndicator("",
                getResources().getDrawable(android.R.drawable.ic_menu_share)).setContent(
                viewConsumers));

        // TODO logs tab which will contain all the logs for this service
        // mTabHost.addTab(mTabHost.newTabSpec("logs").setIndicator("",
        // getResources().getDrawable(R.drawable.ic_menu_account_list)).setContent(
        // R.id.textview3));

        mTabHost.setCurrentTab(0);

        manager = getPackageManager();

        intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        // this is a stupid check TODO change it!
        if (intent.getAction().compareTo(Intent.ACTION_VIEW) == 0 && getIntent().getData() != null) {

            cursor = managedQuery(getIntent().getData(), projection, null, null, null);

            if (!cursor.moveToFirst())
                return;

            setupUI();
            setupOAuth();

        } else {
            finish();
        }
    }

    private void setupUI() {
        ImageView icon = (ImageView)findViewById(R.id.icon);
        String pck = cursor.getString(cursor.getColumnIndexOrThrow(Consumers.PACKAGE_NAME));
        String activity = cursor.getString(cursor.getColumnIndexOrThrow(Consumers.ACTIVITY));
        try {
            icon.setImageDrawable(manager.getActivityIcon(new ComponentName(pck, activity)));
        } catch (NameNotFoundException e) {
            Log.w(TAG, "could not find the icon for the activity: " + e.getMessage());
            icon.setImageDrawable(manager.getDefaultActivityIcon());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        TextView name = (TextView)findViewById(R.id.name);
        name.setText(cursor.getString(cursor.getColumnIndexOrThrow(Registry.NAME)));
        setTitle("OAuth > " + cursor.getString(cursor.getColumnIndexOrThrow(Registry.NAME)));

        TextView url = (TextView)findViewById(R.id.url);
        url.setText(cursor.getString(cursor.getColumnIndexOrThrow(Registry.URL)));

        TextView description = (TextView)findViewById(R.id.description);
        description.setText(cursor.getString(cursor.getColumnIndexOrThrow(Registry.DESCRIPTION)));

        Button authorise = (Button)findViewById(R.id.authorize);
        authorise.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                buildProgressDialog().show();
            }
        });
    }

    private void setupOAuth() {
        consumerKey = cursor.getString(cursor.getColumnIndexOrThrow(Registry.CONSUMER_KEY));
        consumerSecret = cursor.getString(cursor.getColumnIndexOrThrow(Registry.CONSUMER_SECRET));
        requestTokenURL = cursor
                .getString(cursor.getColumnIndexOrThrow(Registry.REQUEST_TOKEN_URL));
        accessTokenURL = cursor.getString(cursor.getColumnIndexOrThrow(Registry.ACCESS_TOKEN_URL));
        authorizeURL = cursor.getString(cursor.getColumnIndexOrThrow(Registry.AUTHORIZE_URL));
    }

    private ProgressDialog buildProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(OAuthItemViewActivity.this);

        dialog.setTitle("Authorizing");

        dialog.setMessage(getString(R.string.token_request_message));

        dialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancel", new Dialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "tests cancel");
            }
        });

        dialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new Dialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "tests click");
                RequestTokenRetrievalTask t = new RequestTokenRetrievalTask();
                t.execute();
            }
        });
        return dialog;
    }

    private static final String[] projection = new String[] {
            Registry.NAME, Registry.ICON, Registry.DESCRIPTION, Registry.URL,
            Registry.CONSUMER_KEY, Registry.CONSUMER_SECRET, Registry.ACCESS_TOKEN_URL,
            Registry.AUTHORIZE_URL, Registry.REQUEST_TOKEN_URL, Consumers.ACTIVITY,
            Consumers.PACKAGE_NAME
    };

    private class RequestTokenRetrievalTask extends AsyncTask<Void, Void, Uri> {

        @Override
        protected Uri doInBackground(Void... params) {
            String url = null;

            oauth.signpost.OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey,
                    consumerSecret, SignatureMethod.HMAC_SHA1);

            provider = new DefaultOAuthProvider(consumer, requestTokenURL, accessTokenURL,
                    authorizeURL);
            try {
                url = provider.retrieveRequestToken(CALLBACK);
            } catch (OAuthMessageSignerException e) {
                e.printStackTrace();
            } catch (OAuthNotAuthorizedException e) {
                e.printStackTrace();
            } catch (OAuthExpectationFailedException e) {
                e.printStackTrace();
            } catch (OAuthCommunicationException e) {
                e.printStackTrace();
            }
            return Uri.parse(url);
        }

        @Override
        protected void onPostExecute(Uri authUrl) {
            // saving to DB
            if (null != authUrl) {
                // open a browser window to handle authorisation
                startActivity(new Intent(Intent.ACTION_VIEW).setData(authUrl));
            } else {
                Toast toast = Toast.makeText(OAuthItemViewActivity.this,
                        "hum something went wrong, can not get token", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    private class TokenExchangeTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                provider.retrieveAccessToken(oauth_verifier);
                token = provider.getConsumer().getToken();
                tokenSecret = provider.getConsumer().getTokenSecret();
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
                values.put(com.novoda.oauth.provider.OAuth.Registry.ACCESS_SECRET, tokenSecret);
                values.put(com.novoda.oauth.provider.OAuth.Registry.ACCESS_TOKEN, token);
                int i = OAuthItemViewActivity.this.getContentResolver().update(intent.getData(),
                        values, null, null);
                if (i == 1) {
                    OAuthItemViewActivity.this.setResult(Activity.RESULT_OK);
                } else {
                    OAuthItemViewActivity.this.setResult(Activity.RESULT_CANCELED);
                }
                finish();
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }
}
