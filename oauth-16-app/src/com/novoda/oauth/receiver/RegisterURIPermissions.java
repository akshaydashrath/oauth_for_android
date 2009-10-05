
package com.novoda.oauth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.novoda.oauth.provider.OAuth.Registry;

public class RegisterURIPermissions extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Cursor cursor = context.getContentResolver().query(Registry.CONTENT_URI, projection, null,
                null, null);
        while (cursor.moveToNext()) {
            context.grantUriPermission(cursor.getString(2), Uri.withAppendedPath(
                    Registry.CONTENT_URI, Integer.toString(cursor.getInt(0))),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    static final String projection[] = new String[] {
            Registry._ID, // 0
           // Registry.APP_NAME, // 1
           // Registry.PACKAGE_NAME, // 2
            Registry.CONSUMER_KEY
    };
}
