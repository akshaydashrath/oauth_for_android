package com.novoda.oauth.tests;

import java.io.File;

import android.test.InstrumentationTestCase;

import com.novoda.oauth.utils.DBUtil;

public class SetupHack extends InstrumentationTestCase {

	private static final String DB_NAME = "oauth.db";

	public void testDBBackup() throws Exception {
		File file = getInstrumentation().getTargetContext().getDatabasePath(
				DB_NAME);

		if (file.exists()) {
			File tmp = new File(file.getAbsolutePath() + ".backup");
			assertTrue(file.renameTo(tmp));
			DBUtil dbu = new DBUtil(getInstrumentation().getTargetContext(),
					DB_NAME);
			assertTrue(dbu.executeDB(getInstrumentation().getContext()
					.getResources().openRawResource(R.raw.oauth)));
		}
	}
}