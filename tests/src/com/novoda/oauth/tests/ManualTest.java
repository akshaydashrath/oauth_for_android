package com.novoda.oauth.tests;

import java.util.HashMap;

import com.novoda.oauth.tests.R;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ManualTest extends Activity {
    private static final String ENDPOINT_TWITTER = "http://twitter.com/statuses/mentions.json";
	private static final String ENDPOINT_JAIKU_NOVODA = "http://jaikunovoda.appspot.com/api/json";
	private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final Button jaiku = (Button)findViewById(R.id.jaiku);
        jaiku.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle extras = new Bundle();
                HashMap<String, String> value = new HashMap<String, String>();
                value.put(com.novoda.oauth.Intent.EXTRA_VALUE_METHOD, "actor_get");
                value.put(com.novoda.oauth.Intent.EXTRA_VALUE_NICK, "carl");
                extras.putString(com.novoda.oauth.Intent.EXTRA_DEST, ENDPOINT_JAIKU_NOVODA);
                extras.putSerializable(com.novoda.oauth.Intent.EXTRA_PARAMS, value);

                intent = new Intent();
                intent.setAction(com.novoda.oauth.Intent.OAUTH_CALL);
                intent.setData(ContentUris.withAppendedId(com.novoda.oauth.provider.OAuth.Registry.CONTENT_URI, 5));
                intent.putExtras(extras);
                startActivityForResult(intent, 1);
            }
        });
        
        Button twitter = (Button)findViewById(com.novoda.oauth.tests.R.id.twitter);
        twitter.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString(com.novoda.oauth.Intent.EXTRA_DEST, ENDPOINT_TWITTER);

                intent = new Intent();
                intent.setAction(com.novoda.oauth.Intent.OAUTH_CALL);
                intent.setData(ContentUris.withAppendedId(com.novoda.oauth.provider.OAuth.Registry.CONTENT_URI, 1));
                intent.putExtras(extras);
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode) {
            case Activity.RESULT_FIRST_USER:
                Toast.makeText(this, "RESULT_FIRST_USER, needs to activate first...", 2000).show();
                break;
            case Activity.RESULT_OK:
                if (data != null) 
                    Toast.makeText(this, data.getStringExtra("result").toString(), 5000).show();
        }
    }
}
