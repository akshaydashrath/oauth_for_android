
package com.novoda.oauth.utils;

import net.oauth.OAuthMessage;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

public class OAuthAsyncTask extends AsyncTask<OAuthCall, Void, OAuthMessage> {

    private static final String TAG = "OAuth:";

    @Override
    protected OAuthMessage doInBackground(OAuthCall... params) {
        return params[0].call();
    }

    @Override
    protected void onPostExecute(OAuthMessage result) {
        super.onPostExecute(result);
        try {
            Log.i(TAG, "returned: " + result.readBodyAsString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
// / new OAuthAsyncTask.execute(new JSONOACall(endpoit, map param))
