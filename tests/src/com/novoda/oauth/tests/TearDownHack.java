package com.novoda.oauth.tests;

import java.io.File;

import android.test.InstrumentationTestCase;

public class TearDownHack extends InstrumentationTestCase {
	
	private static final String DB_NAME = "oauth.db";

	public void testDBrollback() throws Exception {
		File file = getInstrumentation().getTargetContext().getDatabasePath(
				DB_NAME);
		File tmp = new File(file.getAbsolutePath() + ".backup");
		if (file.exists() && tmp.exists()) {
			// delete test db
			if (tmp.renameTo(file)){
				assertTrue(file.delete());
			}
		}
	}
}
