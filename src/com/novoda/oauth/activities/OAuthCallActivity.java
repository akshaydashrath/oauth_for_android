
package com.novoda.oauth.activities;

import com.novoda.oauth.OAuthObject;
import com.novoda.oauth.R;
import com.novoda.oauth.provider.OAuth.Consumers;
import com.novoda.oauth.provider.OAuth.Registry;
import com.novoda.oauth.utils.OAuthAsyncTask;
import com.novoda.oauth.utils.OAuthCall;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.HashMap;

public class OAuthCallActivity extends Activity {

    public static final String TAG = "OAuth:";

    private static final int SHOULD_AUTHORISE = 0;

    private String packageName;

    private HashMap<String, String> parameters;

    private String endpoint;

    private OAuthObject oauthData;

    private Intent intent;

    private Uri uri;

    private ComponentName callingActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // no UI, only Dialogs
        setVisible(false);

        packageName = getCallingPackage();
        callingActivity = getCallingActivity();
        intent = getIntent();
        uri = intent.getData();

        // The only intent this activity handles is the OAuth call.
        if (getIntent().getAction().compareTo(com.novoda.oauth.Intent.OAUTH_CALL) != 0)
            finish();

        // Check if the OAuth service has been activated or not. If not, set the
        // result and go back to the calling activity.
        Cursor cur = getContentResolver().query(intent.getData(), new String[] {
            Registry.ACCESS_TOKEN
        }, null, null, null);
        if (cur.moveToFirst()) {
            if (cur.isNull(cur.getColumnIndexOrThrow(Registry.ACCESS_TOKEN))) {
                cur.close();
                setResult(Activity.RESULT_FIRST_USER);
                finish();
            }
        }

        consumer = managedQuery(uri.buildUpon().appendEncodedPath("consumers").build(),
                new String[] {
                    Consumers.IS_BANNED
                }, Consumers.PACKAGE_NAME + "=?", new String[] {
                    packageName
                }, null);

        // This is the first time this activity calls the OAuth
        if (consumer.getCount() == 0) {
            showDialog(SHOULD_AUTHORISE);
        } else {
            if (consumer.moveToFirst()
                    && consumer.getInt(consumer.getColumnIndexOrThrow(Consumers.IS_BANNED)) == 1) {
                Toast.makeText(this,
                        "Current application is banned, use the OAuth application to unban", 2000);
            } else {
                makeRequest();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void makeRequest() {
        oauthData = new OAuthObject();
        endpoint = getIntent().getStringExtra("endpoint");
        parameters = (HashMap<String, String>)getIntent().getSerializableExtra("parameters");
        Cursor cursor = getContentResolver().query(getIntent().getData(), null, null, null, null);
        if (cursor.moveToFirst()) {
            oauthData.setConsumerKey(cursor.getString(cursor
                    .getColumnIndexOrThrow(Registry.CONSUMER_KEY)));
            oauthData.setConsumerSecret(cursor.getString(cursor
                    .getColumnIndexOrThrow(Registry.CONSUMER_SECRET)));
            oauthData.setToken(cursor
                    .getString(cursor.getColumnIndexOrThrow(Registry.ACCESS_TOKEN)));
            oauthData.setTokenSecret(cursor.getString(cursor
                    .getColumnIndexOrThrow(Registry.ACCESS_SECRET)));
            oauthData.setAccessTokenURL(cursor.getString(cursor
                    .getColumnIndexOrThrow(Registry.ACCESS_TOKEN_URL)));
            oauthData.setRequestTokenURL(cursor.getString(cursor
                    .getColumnIndexOrThrow(Registry.REQUEST_TOKEN_URL)));
            oauthData.setAuthorizeURL(cursor.getString(cursor
                    .getColumnIndexOrThrow(Registry.AUTHORIZE_URL)));
            cursor.close();
        }

        MyTask task = new MyTask();
        task.execute(new OAuthCall(oauthData, endpoint, parameters));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SHOULD_AUTHORISE:
                return createAuthoriseDialog();
        }
        return super.onCreateDialog(id);
    }

    private Dialog createAuthoriseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AuthoriseDialogClickListener listener = new AuthoriseDialogClickListener();
        builder.setMessage(getString(R.string.authorise_application));
        builder.setPositiveButton("yes", listener);
        builder.setNegativeButton("no", listener);
        builder.setNeutralButton("always", listener);
        return builder.create();
    }

    private class AuthoriseDialogClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE:
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    ContentValues values = new ContentValues();
                    values.put(Consumers.PACKAGE_NAME, packageName);
                    values.put(Consumers.ACTIVITY, callingActivity.getClassName());
                    values.put(Consumers.IS_AUTHORISED, true);
                    values.put(Consumers.IS_BANNED, false);
                    values.put(Consumers.ACTIVITY, callingActivity.getClassName());
                    getContentResolver().insert(
                            uri.buildUpon().appendEncodedPath("consumers").build(), values);
                    makeRequest();
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    makeRequest();
                    break;
            }
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
    }

    private Cursor consumer;

    private class MyTask extends OAuthAsyncTask {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(OAuthCallActivity.this, "Getting OAuth",
                    "Querying the OAuth servers");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Intent intent = new Intent();
            intent.putExtra("result", result);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}
