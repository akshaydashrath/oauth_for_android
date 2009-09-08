
package com.novoda.oauth.activities;

import com.novoda.oauth.R;
import com.novoda.oauth.provider.OAuth.Consumers;
import com.novoda.oauth.provider.OAuth.Registry;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class OAuthItemViewActivity extends Activity {

    private static final String TAG = "OAuth:";

    private Cursor cursor;

    private PackageManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_service_view);

        manager = getPackageManager();

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        if (intent.getAction().compareTo(Intent.ACTION_VIEW) == 0 && getIntent().getData() != null) {
            cursor = managedQuery(getIntent().getData(), projection, null, null, null);
            if (!cursor.moveToFirst())
                return;
            setup();
        } else {
            finish();
        }
    }

    private void setup() {
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
    }

    private static final String[] projection = new String[] {
            Registry.NAME, Registry.ICON, Registry.DESCRIPTION, Registry.URL, Consumers.ACTIVITY,
            Consumers.PACKAGE_NAME
    };
}
