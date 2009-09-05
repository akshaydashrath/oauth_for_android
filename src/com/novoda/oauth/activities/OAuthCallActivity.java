
package com.novoda.oauth.activities;

import com.novoda.oauth.OAuthObject;
import com.novoda.oauth.provider.OAuth;
import com.novoda.oauth.provider.OAuth.Registry;
import com.novoda.oauth.utils.OAuthAsyncTask;
import com.novoda.oauth.utils.OAuthCall;

import net.oauth.OAuthMessage;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

public class OAuthCallActivity extends Activity implements DialogInterface {

    public static final String TAG = "OAuth:";

    private String packageName;

    private HashMap<String, String> parameters;

    private String endpoint;

    private OAuthObject oauthData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVisible(false);
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
        Cursor cursor = getContentResolver().query(OAuth.Registry.CONTENT_URI, projection,
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

        MyTask task = new MyTask();
        task.execute(new OAuthCall(oauthData, endpoint, parameters));
    }

    public void cancel() {
    }

    public void dismiss() {
    }

    private static String[] projection = new String[] {
            Registry._ID, // 0
            Registry.CONSUMER_KEY, // 1
            Registry.CONSUMER_SECRET, // 2
            Registry.ACCESS_TOKEN, // 3
            Registry.ACCESS_SECRET, // 4
            Registry.ACCESS_TOKEN_URL, // 5
            Registry.REQUEST_TOKEN_URL, // 6
            Registry.AUTHORIZE_URL
    // 7
    };

    private class MyTask extends OAuthAsyncTask {
        @Override
        protected void onPostExecute(OAuthMessage result) {
            super.onPostExecute(result);
            try {
                Log.i(TAG, result.readBodyAsString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        }
    }
}
