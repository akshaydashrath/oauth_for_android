
package com.novoda.oauth.activities;

import com.novoda.oauth.R;
import com.novoda.oauth.provider.OAuth;
import com.novoda.oauth.provider.OAuth.Consumers;
import com.novoda.oauth.provider.OAuth.Registry;

import org.xmlpull.v1.XmlPullParser;

import android.app.ExpandableListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class OAuthListing extends ExpandableListActivity {
    private static final String TAG = "OAuth:";

    private Cursor cursor;

    private PackageManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.oauth_list_activity);
        manager = getPackageManager();
        cursor = managedQuery(OAuth.Registry.CONTENT_URI, projection, null, null, null);

        // Check if we have the default values set, if not populate with default
        // values
        if (cursor.getCount() == 0) {
            populateDefault();
            cursor.requery();
        }

        setListAdapter(new AllItemAdapter(cursor, this));
        getExpandableListView().setOnGroupClickListener(new RegistryOnClickListener());
        getExpandableListView().setOnChildClickListener(new ConsumerOnClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void populateDefault() {
        XmlResourceParser providers = getResources().getXml(R.xml.providers);
        getContentResolver().bulkInsert(Registry.CONTENT_URI, parseProviders(providers));
    }

    private class ConsumerOnClickListener implements ExpandableListView.OnChildClickListener {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                int childPosition, long id) {
            return false;
        }
    }

    private class RegistryOnClickListener implements ExpandableListView.OnGroupClickListener {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            if (v.getTag() == null) {
                Uri uri = ContentUris.withAppendedId(Registry.CONTENT_URI, id);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri); 
                startActivity(intent);
                return true;
            }
            return false;
        }
    }

    private class AllItemAdapter extends CursorTreeAdapter {

        public AllItemAdapter(Cursor cursor, Context context) {
            super(cursor, context);
        }

        @Override
        protected void bindChildView(View view, Context context, Cursor cursor, boolean isExpanded) {
            TextView url = (TextView)view.findViewById(R.id.url);
            TextView name = (TextView)view.findViewById(R.id.name);
            name.setText(cursor.getString(cursor.getColumnIndexOrThrow(Consumers.APP_NAME)));
            url.setText(cursor.getString(cursor.getColumnIndexOrThrow(Consumers.PACKAGE_NAME)));
        }

        @Override
        protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
            view.setTag(cursor.getString(cursor.getColumnIndexOrThrow(Registry.ACCESS_TOKEN)));
            TextView url = (TextView)view.findViewById(R.id.url);
            TextView name = (TextView)view.findViewById(R.id.name);
            name.setText(cursor.getString(cursor.getColumnIndexOrThrow(Registry.NAME)));
            url.setText(cursor.getString(cursor.getColumnIndexOrThrow(Registry.URL)));
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            long regId = groupCursor.getLong(cursor.getColumnIndexOrThrow(Registry._ID));
            Uri.Builder builder = Registry.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, regId);
            builder.appendEncodedPath("consumers");
            Uri appperreg = builder.build();
            return managedQuery(appperreg, consumerProjection, null, null, null);
        }

        @Override
        protected View newChildView(Context context, Cursor cursor, boolean isLastChild,
                ViewGroup parent) {
            return View.inflate(context, R.layout.oauth_list_item, null);
        }

        @Override
        protected View newGroupView(Context context, Cursor cursor, boolean isExpanded,
                ViewGroup parent) {
            return View.inflate(context, R.layout.oauth_list_item, null);
        }
    }

    private static String[] projection = {
            Registry._ID, // 0
            Registry.NAME, // 1
            Registry.ICON, // 2
            Registry.URL, // 3
            Registry.ACCESS_TOKEN
    };

    private static String[] consumerProjection = {
            Consumers._ID, // 0
            Consumers.APP_NAME, // 1
            Consumers.PACKAGE_NAME
    };

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
