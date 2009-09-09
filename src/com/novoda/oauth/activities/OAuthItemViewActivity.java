
package com.novoda.oauth.activities;

import com.novoda.oauth.R;
import com.novoda.oauth.provider.OAuth.Consumers;
import com.novoda.oauth.provider.OAuth.Registry;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class OAuthItemViewActivity extends Activity {

    private static final String TAG = "OAuth:";

    private Cursor cursor;

    private Intent intent;

    private PackageManager manager;

    // OAuth Stuff
    private String consumerKey;

    private String consumerSecret;

    private String requestTokenURL;

    private String accessTokenURL;

    private String authorizeURL;

    public OAuthAccessor accessor;

    public HashMap<String, String> parameters = new HashMap<String, String>();

    public String token;

    public String tokenSecret;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, intent.toString());
        TokenExchangeTask task = new TokenExchangeTask();
        task.execute(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_service_view);

        manager = getPackageManager();

        intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        if (intent.getAction().compareTo(Intent.ACTION_VIEW) == 0 && getIntent().getData() != null) {
            cursor = managedQuery(getIntent().getData(), projection, null, null, null);
            if (!cursor.moveToFirst())
                return;
            setupUI();
            setupOAuth();

            registerCallBack("android");
        } else {
            finish();
        }
    }

    private void setupUI() {
        ImageView icon = (ImageView)findViewById(R.id.icon);
        String pck = cursor.getString(cursor.getColumnIndexOrThrow(Consumers.PACKAGE_NAME));
        String activity = cursor.getString(cursor.getColumnIndexOrThrow(Consumers.ACTIVITY));
        try {
            icon.setImageDrawable(manager.getActivityIcon(new ComponentName(pck, pck + activity)));
            Log.d(TAG, new ComponentName(pck, pck + activity).flattenToShortString());
        } catch (NameNotFoundException e) {
            Log.w(TAG, "could not find the icon for the activity: " + e.getMessage());
            icon.setImageDrawable(manager.getDefaultActivityIcon());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        TextView name = (TextView)findViewById(R.id.name);
        name.setText(cursor.getString(cursor.getColumnIndexOrThrow(Registry.NAME)));

        TextView url = (TextView)findViewById(R.id.url);
        url.setText(cursor.getString(cursor.getColumnIndexOrThrow(Registry.URL)));

        TextView description = (TextView)findViewById(R.id.description);
        description.setText(cursor.getString(cursor.getColumnIndexOrThrow(Registry.DESCRIPTION)));

        Button authorise = (Button)findViewById(R.id.authorize);
        authorise.setOnClickListener(new OnClickListener() {
            @Override
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
        dialog
                .setMessage("Accessing the remote service for authorization token. After done so, click on OK "
                        + "which will redirect you to the browser for which you ll need to login and authorize the application.");
        dialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancel", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "tests cancel");

            }
        });

        dialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "tests click");
                RequestTokenRetrievalTask t = new RequestTokenRetrievalTask();
                t.execute(null);
            }
        });
        return dialog;
    }

    private void registerCallBack(String scheme) {
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_BROWSABLE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(Intent.ACTION_VIEW);
        filter.addDataScheme(scheme);
        // registerReceiver(new CallBackReceiver(), filter);
    }

    private static final String[] projection = new String[] {
            Registry.NAME, Registry.ICON, Registry.DESCRIPTION, Registry.URL,
            Registry.CONSUMER_KEY, Registry.CONSUMER_SECRET, Registry.ACCESS_TOKEN_URL,
            Registry.AUTHORIZE_URL, Registry.REQUEST_TOKEN_URL, Consumers.ACTIVITY,
            Consumers.PACKAGE_NAME
    };

    private class CallBackReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, intent.toString());
        }
    }

    private class RequestTokenRetrievalTask extends AsyncTask<Void, Void, Uri> {

        @Override
        protected Uri doInBackground(Void... params) {
            String url = null;
            OAuthServiceProvider provider = new OAuthServiceProvider(requestTokenURL, authorizeURL,
                    accessTokenURL);
            OAuthConsumer consumer = new OAuthConsumer("x-oauth-android://callback", consumerKey,
                    consumerSecret, provider);
            OAuthClient client = new OAuthClient(new HttpClient4());
            accessor = new OAuthAccessor(consumer);

            try {
                client.getRequestToken(accessor);
                parameters.put(OAuth.OAUTH_TOKEN, accessor.requestToken);
                parameters.put(OAuth.OAUTH_CALLBACK, "x-oauth-android://callback");
                url = OAuth.addParameters(accessor.consumer.serviceProvider.userAuthorizationURL,
                        parameters.entrySet());
            } catch (Exception e) {
                Log.e(TAG, "Could not get authorize token from " + requestTokenURL + " "
                        + e.getMessage());
                e.printStackTrace();
                return null;
            }
            return Uri.parse(url);
        }

        @Override
        protected void onPostExecute(Uri authUrl) {

            // saving to DB
            if (null != authUrl) {
                // open a browser window to handle authorisation
                startActivity(new Intent(Intent.ACTION_VIEW).setData(authUrl));
                // .addFlags(
                // Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
                // | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                // | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));

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
