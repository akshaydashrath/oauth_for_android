
package com.novoda.oauth.tests;

import java.util.HashMap;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ManualTest extends Activity {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.novoda.oauth.tests.R.layout.main);
        
        Button but = (Button)findViewById(com.novoda.oauth.tests.R.id.jaiku);
        but.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                intent = new Intent();
                intent.setAction("com.novoda.oauth.action.OAUTH_CALL");
                
                // default for jaikunovoda to speed up testing
                intent.setData(ContentUris.withAppendedId(Uri
                        .parse("content://com.novoda.oauth.provider.OAuth/registry"), 5));
                
                Bundle extras = new Bundle();
                extras.putString("endpoint", "http://jaikunovoda.appspot.com/api/json");
                HashMap<String, String> value = new HashMap<String, String>();
                value.put("method", "actor_get");
                value.put("nick", "carl");
                extras.putSerializable("parameters", value);

                intent.putExtras(extras);

                startActivityForResult(intent, 1);
            }
        });
        
        Button twitter = (Button)findViewById(com.novoda.oauth.tests.R.id.twitter);
        twitter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                intent = new Intent();
                intent.setAction("com.novoda.oauth.action.OAUTH_CALL");
                
                // default for jaikunovoda to speed up testing
                intent.setData(ContentUris.withAppendedId(Uri
                        .parse("content://com.novoda.oauth.provider.OAuth/registry"), 1));
                
                Bundle extras = new Bundle();
                extras.putString("endpoint", "http://twitter.com/statuses/mentions.json");
//                HashMap<String, String> value = new HashMap<String, String>();
//                value.put("method", "actor_get");
//                value.put("nick", "carl");
//                extras.putSerializable("parameters", value);

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
