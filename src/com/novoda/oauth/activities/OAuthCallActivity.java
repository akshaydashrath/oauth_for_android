
package com.novoda.oauth.activities;

import com.novoda.oauth.OAuthObject;
import com.novoda.oauth.provider.OAuth;
import com.novoda.oauth.provider.OAuth.Providers;
import com.novoda.oauth.utils.OAuthAsyncTask;
import com.novoda.oauth.utils.OAuthCall;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;

import java.util.HashMap;

public class OAuthCallActivity extends Activity implements DialogInterface {

    private String packageName;

    private HashMap<String, String> parameters;

    private String endpoint;

    private OAuthObject oauthData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageName = getCallingPackage();
        oauthData = new OAuthObject();
        // This should not happen
        if (getIntent().getAction().compareTo(com.novoda.oauth.Intent.OAUTH_CALL) != 0)
            finish();

        endpoint = getIntent().getStringExtra("endpoint");
        parameters = (HashMap<String, String>)getIntent().getSerializableExtra("parameters");
    }

    @Override
    protected void onResume() {
        super.onResume();
        String selection = "package_name=?";
        Cursor cursor = getContentResolver().query(OAuth.Providers.CONTENT_URI, projection,
                selection, new String[] {
                    packageName
                }, null);
        if (cursor.moveToFirst()) {
            oauthData.setConsumerKey(cursor.getString(1));
            oauthData.setConsumerSecret(cursor.getString(2));
            oauthData.setToken(cursor.getString(3));
            oauthData.setTokenSecret(cursor.getString(4));
            oauthData.setAccessTokenURL(cursor.getString(5));
            oauthData.setRequestTokenURL(cursor.getString(6));
            oauthData.setAuthorizeURL(cursor.getString(7));
            cursor.close();
        }

        OAuthAsyncTask task = new OAuthAsyncTask();
        task.execute(new OAuthCall(oauthData, endpoint, parameters));
    }

    public void cancel() {
    }

    public void dismiss() {
    }

    private static String[] projection = new String[] {
            Providers._ID, // 0
            Providers.CONSUMER_KEY, // 1
            Providers.CONSUMER_SECRET, // 2
            Providers.ACCESS_TOKEN, // 3
            Providers.ACCESS_SECRET, // 4
            Providers.ACCESS_TOKEN_URL, // 5
            Providers.REQUEST_TOKEN_URL, // 6
            Providers.AUTHORIZE_URL
    // 7
    };

}
