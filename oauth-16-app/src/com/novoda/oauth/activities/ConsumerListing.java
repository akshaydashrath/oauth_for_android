
package com.novoda.oauth.activities;

import com.novoda.oauth.R;
import com.novoda.oauth.provider.OAuth.Consumers;

import android.app.ListActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ConsumerListing extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Cursor cursor = managedQuery(getIntent().getData(), null, null, null, null);
        setListAdapter(new ConsumerListAdapter(this, cursor));
    }

    private class ConsumerListAdapter extends CursorAdapter {

        
        private PackageManager pckManager;

        public ConsumerListAdapter(Context context, Cursor c) {
            super(context, c);
            pckManager = context.getPackageManager();
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView icon = (ImageView)view.findViewById(R.id.icon);
            try {
                icon.setImageDrawable(pckManager.getApplicationIcon(cursor.getString(cursor
                        .getColumnIndexOrThrow(Consumers.PACKAGE_NAME))));
            } catch (NameNotFoundException e) {
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.icon));
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            TextView name = (TextView)view.findViewById(R.id.name);
            name.setText(cursor.getString(cursor.getColumnIndexOrThrow(Consumers.APP_NAME)));
            
            TextView activity = (TextView)view.findViewById(R.id.activity);
            activity.setText(cursor.getString(cursor.getColumnIndexOrThrow(Consumers.ACTIVITY)));
            
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.consumer_list_item, null);
        }
    }
}
