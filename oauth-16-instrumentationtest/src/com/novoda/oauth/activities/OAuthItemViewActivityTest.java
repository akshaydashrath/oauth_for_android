
package com.novoda.oauth.activities;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Button;

import com.novoda.oauth.R;
import com.novoda.oauth.provider.OAuth.Registry;

public class OAuthItemViewActivityTest extends ActivityUnitTestCase<OAuthItemViewActivity> {

    private Intent mStartIntent;
    private Button mButton;
    
    public OAuthItemViewActivityTest() {
        super(OAuthItemViewActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mStartIntent = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Registry.CONTENT_URI,
                "1"));
    }
    
    @MediumTest
    public void testPreconditions() {
        startActivity(mStartIntent, null, null);
        mButton = (Button) getActivity().findViewById(R.id.authorize);
        
        assertNotNull(getActivity());
        assertNotNull(mButton);
    }

}
