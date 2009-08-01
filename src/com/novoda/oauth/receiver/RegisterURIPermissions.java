package com.novoda.oauth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.novoda.oauth.provider.OAuth.Providers;

public class RegisterURIPermissions extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Cursor cursor = context.getContentResolver().query(Providers.CONTENT_URI, new String[] {
				"_id", "app_name", "package_name"
		}, null, null, null);
		while (cursor.moveToNext()) {
			context.grantUriPermission(cursor.getString(2), Uri.withAppendedPath(Providers.CONTENT_URI, Integer
					.toString(cursor.getInt(0))), Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}
	}
}
