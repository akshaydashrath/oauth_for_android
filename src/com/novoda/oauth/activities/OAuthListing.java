package com.novoda.oauth.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.novoda.oauth.R;
import com.novoda.oauth.provider.OAuth;
import com.novoda.oauth.provider.OAuth.Providers;

public class OAuthListing extends ListActivity implements OnItemLongClickListener {
	private static final String	TAG	= "OAuth:";

	private Cursor				cursor;
	private PackageManager		manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oauth_list_activity);
		manager = getPackageManager();
		cursor = managedQuery(OAuth.Providers.CONTENT_URI, projection, null, null, null);
		setListAdapter(new OAuthListAdapater(this, cursor));

		getListView().setOnItemLongClickListener(this);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor tmp = getContentResolver().query(ContentUris.withAppendedId(Providers.CONTENT_URI, (int) id),
				projection, null, null, null);
		if (tmp.moveToFirst()) {
			String packageName = cursor.getString(2);
			manager = getPackageManager();
			try {
				Intent intent = manager.getLaunchIntentForPackage(packageName);
				Log.d(TAG, "launching: " + intent.toString() + " for " + packageName);
				startActivity(intent);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (tmp != null)
			tmp.close();
	}

	private int	current;

	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		current = (int) id;
		new AlertDialog.Builder(this).setTitle(R.string.alert_dialog_delete_title).setMessage(
				R.string.alert_dialog_delete).setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Uri uri = ContentUris.withAppendedId(Providers.CONTENT_URI, current);
						Log.i(TAG, "deleting: " + uri);
						getContentResolver().delete(uri, null, null);
					}
				}).setNegativeButton(android.R.string.cancel, null).create().show();
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "in the main: " + getIntent().toString());
	}

	private class OAuthListAdapater extends CursorAdapter {

		private static final String	TAG	= "OAuth:";

		public OAuthListAdapater(Context context, Cursor c) {
			super(context, c);
			manager = context.getPackageManager();
		}

		@Override
		public long getItemId(int position) {
			return super.getItemId(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return super.getView(position, convertView, parent);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ImageView icon = (ImageView) view.findViewById(R.id.app_icon);
			TextView appName = (TextView) view.findViewById(R.id.app_name);
			TextView accessToken = (TextView) view.findViewById(R.id.access_token);
			TextView requestUrl = (TextView) view.findViewById(R.id.request_url);
			TextView tokenSecret = (TextView) view.findViewById(R.id.token_secret);

			appName.setText(cursor.getString(1));
			accessToken.setText(cursor.getString(5));

			requestUrl.setText(Uri.parse(cursor.getString(3)).getHost());
			tokenSecret.setText(cursor.getString(6));

			try {
				icon.setBackgroundDrawable(manager.getApplicationIcon(cursor.getString(2)));
			} catch (NameNotFoundException e) {
				Log.w(TAG, "can not find icon for package: " + cursor.getString(2));
			}

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return View.inflate(context, R.layout.single_provider, null);
		}

	}

	static String[]	projection	= {
			Providers._ID, "app_name", "package_name", Providers.ACCESS_TOKEN_URL, Providers.CONSUMER_KEY,
			Providers.ACCESS_TOKEN, Providers.ACCESS_SECRET, Providers.CREATED_DATE, Providers.MODIFIED_DATE
								};
}
