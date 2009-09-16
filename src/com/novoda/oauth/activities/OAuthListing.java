
package com.novoda.oauth.activities;

import com.novoda.oauth.R;
import com.novoda.oauth.provider.OAuth;
import com.novoda.oauth.provider.OAuth.Consumers;
import com.novoda.oauth.provider.OAuth.Registry;

import org.xmlpull.v1.XmlPullParser;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class OAuthListing extends ListActivity {
    private static final String TAG = "OAuth:";

    private Cursor registry;

    private PackageManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.oauth_list_activity);
        manager = getPackageManager();
        registry = managedQuery(OAuth.Registry.CONTENT_URI, null, null, null, null);

        // Check if we have the default values set, if not populate with default
        // values
        if (registry.getCount() == 0) {
            populateDefault();
            registry.requery();
        }

        setListAdapter(new RegistryListAdapater(this, registry));
        getListView().setOnItemClickListener(new RegistryOnClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void populateDefault() {
        XmlResourceParser providers = getResources().getXml(R.xml.providers);
        getContentResolver().bulkInsert(Registry.CONTENT_URI, parseProviders(providers));
    }

    private class RegistryOnClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            if (view.getTag() == null) {
                Uri uri = ContentUris.withAppendedId(Registry.CONTENT_URI, id);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        }
    }

    private class RegistryListAdapater extends CursorAdapter {

        public RegistryListAdapater(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView active = (ImageView)view.findViewById(R.id.tb);
            if (cursor.isNull(cursor.getColumnIndexOrThrow(Registry.ACCESS_TOKEN))) {
                active.setImageDrawable(context.getResources().getDrawable(
                        R.drawable.btn_check_buttonless_off));
            } else {
                active.setImageDrawable(context.getResources().getDrawable(
                        R.drawable.btn_check_buttonless_on));
            }

            view.setTag(cursor.getString(cursor.getColumnIndexOrThrow(Registry.ACCESS_TOKEN)));

            TextView name = (TextView)view.findViewById(R.id.name);
            name.setText(cursor.getString(cursor.getColumnIndexOrThrow(Registry.NAME)));

            TextView url = (TextView)view.findViewById(R.id.url);
            url.setText(cursor.getString(cursor.getColumnIndexOrThrow(Registry.URL)));

            ImageView icon = (ImageView)view.findViewById(R.id.icon);
            setIcon(icon, cursor.getString(cursor.getColumnIndexOrThrow(Consumers.PACKAGE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(Consumers.ACTIVITY)));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return View.inflate(context, R.layout.oauth_list_item, null);
        }

        private void setIcon(ImageView icon, String pck, String activity) {
            try {
                icon.setImageDrawable(manager.getActivityIcon(new ComponentName(pck, activity)));
            } catch (NameNotFoundException e) {
                Log.w(TAG, "could not find the icon for the activity: " + e.getMessage());
                icon.setImageDrawable(manager.getDefaultActivityIcon());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * Parse the default XML for providers value. The XML should have the tag
     * name similar to the columns' name in the DB
     */
    protected ContentValues[] parseProviders(XmlPullParser xpp) {
        List<ContentValues> ret = new ArrayList<ContentValues>();
        ContentValues value = new ContentValues();
        String key = null;
        String starttag = null;
        String endtag = null;
        try {
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                } else if (eventType == XmlPullParser.END_DOCUMENT) {
                } else if (eventType == XmlPullParser.START_TAG) {
                    starttag = xpp.getName();
                    if (starttag.compareTo("provider") == 0) {
                        value.put(Registry.NAME, xpp.getAttributeValue(0));
                    } else if (starttag.compareTo("providers") == 0) {
                        // do nothing
                    } else {
                        key = starttag;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    endtag = xpp.getName();
                    if (endtag.compareTo("provider") == 0) {
                        ret.add(new ContentValues(value));
                        value.clear();
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (starttag.compareTo("providers") == 0 || starttag.compareTo("provider") == 0) {
                        // do nothing
                    } else {
                        value.put(key, xpp.getText());
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "error parsing: " + e.getMessage());
            e.printStackTrace();
        }
        ContentValues[] array = new ContentValues[ret.size()];
        ret.toArray(array);
        return array;
    }
}
