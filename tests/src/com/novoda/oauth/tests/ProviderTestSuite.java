package com.novoda.oauth.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.novoda.oauth.provider.OAuthProviderTest;

public class ProviderTestSuite extends TestSuite {
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(OAuthProviderTest.class);
        return suite;
    }
}