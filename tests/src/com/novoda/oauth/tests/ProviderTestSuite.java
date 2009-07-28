package com.novoda.oauth.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.novoda.oauth.providers.OAuthProviderTest;

public class ProviderTestSuite extends TestSuite {
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(SetupHack.class);
		suite.addTestSuite(OAuthProviderTest.class);
		suite.addTestSuite(TearDownHack.class);
        return suite;
    }
}