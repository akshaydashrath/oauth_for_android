
package com.novoda.oauth.tests;

import java.util.HashMap;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ManualTest extends Activity {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent();
        intent.setAction("com.novoda.oauth.action.OAUTH_CALL");
        // default for jaikunovoda to speed up testing
        intent.setData(ContentUris.withAppendedId(Uri
                .parse("content://com.novoda.oauth.provider.OAuth/registry"), 5));
        // intent.setData(Uri.parse("content://com.novoda.oauth.provider.OAuth/registry"));
        Bundle extras = new Bundle();
        extras.putString("endpoint", "http://jaikunovoda.appspot.com/api/json");
        HashMap<String, String> value = new HashMap<String, String>();
        value.put("method", "actor_get");
        value.put("nick", "carl");
        extras.putSerializable("parameters", value);
        
        intent.putExtras(extras);

        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("OAuth:", data.toString());
    }
}
