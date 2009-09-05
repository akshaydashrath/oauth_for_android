
package com.novoda.oauth.utils;

import net.oauth.OAuthMessage;

import android.os.AsyncTask;

public abstract class OAuthAsyncTask extends AsyncTask<OAuthCall, Void, OAuthMessage> {

    @SuppressWarnings("unused")
    private static final String TAG = "OAuth:";

    @Override
    protected OAuthMessage doInBackground(OAuthCall... params) {
        return params[0].call();
    }

//    @Override
//    protected void onPostExecute(OAuthMessage result) {
//        super.onPostExecute(result);
//        try {
//            Log.i(TAG, "returned: " + result.readBodyAsString());
//            
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}