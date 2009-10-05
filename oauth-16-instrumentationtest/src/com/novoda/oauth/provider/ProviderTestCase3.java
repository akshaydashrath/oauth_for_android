
package com.novoda.oauth.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.DatabaseUtils;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;
import android.test.mock.MockPackageManager;

public abstract class ProviderTestCase3<T extends ContentProvider> extends AndroidTestCase {

    Class<T> mProviderClass;

    String mProviderAuthority;

    private IsolatedContext mProviderContext;

    private MockContentResolver mResolver;

    private String testPackage;

    private String testSignature;

    private boolean hasPermission;

    public ProviderTestCase3(Class<T> providerClass, String providerAuthority) {
        mProviderClass = providerClass;
        mProviderAuthority = providerAuthority;
    }

    /**
     * The content provider that will be set up for use in each test method.
     */
    private T mProvider;

    public T getProvider() {
        return mProvider;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        testPackage = getContext().getPackageName();
        testSignature = "test123";

        mResolver = new MockContentResolver();
        final String filenamePrefix = "test.";
        RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(
                new MockContext(), // The context that most methods are
                // delegated to
                getContext(), // The context that file methods are delegated to
                filenamePrefix);
        mProviderContext = new MyIsolatedContext(mResolver, targetContextWrapper);

        mProvider = mProviderClass.newInstance();
        mProvider.attachInfo(mProviderContext, null);
        assertNotNull(mProvider);
        mResolver.addProvider(mProviderAuthority, getProvider());
    }

    public MockContentResolver getMockContentResolver() {
        return mResolver;
    }

    public IsolatedContext getMockContext() {
        return mProviderContext;
    }

    public static <T extends ContentProvider> ContentResolver newResolverWithContentProviderFromSql(
            Context targetContext, String filenamePrefix, Class<T> providerClass, String authority,
            String databaseName, int databaseVersion, String sql) throws IllegalAccessException,
            InstantiationException {
        MockContentResolver resolver = new MockContentResolver();
        RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(
                new MockContext(), // The context that most methods are
                // delegated to
                targetContext, // The context that file methods are delegated to
                filenamePrefix);
        Context context = new IsolatedContext(resolver, targetContextWrapper);
        DatabaseUtils.createDbFromSqlStatements(context, databaseName, databaseVersion, sql);

        T provider = providerClass.newInstance();
        provider.attachInfo(context, null);
        resolver.addProvider(authority, provider);

        return resolver;
    }

    public void setPackage(String pck) {
        this.testPackage = pck;
    }

    public void setSignature(String sig) {
        this.testSignature = sig;
    }

    protected void setPermission(boolean permission) {
        this.hasPermission = permission;
    }

    private class MyIsolatedContext extends IsolatedContext {
        public MyIsolatedContext(ContentResolver resolver, Context targetContext) {
            super(resolver, targetContext);
        }

        @Override
        public String getPackageName() {
            return testPackage;
        }

        @Override
        public PackageManager getPackageManager() {
            return new MyPckManager();
        }

        @Override
        public int checkCallingPermission(String permission) {
            if (hasPermission)
                return PackageManager.PERMISSION_GRANTED;
            else
                return PackageManager.PERMISSION_DENIED;
        }
    }

    private class MyPckManager extends MockPackageManager {
        @Override
        public String[] getPackagesForUid(int uid) {
            return new String[] {
                testPackage
            };
        }

        @Override
        public PackageInfo getPackageInfo(String packageName, int flags)
                throws NameNotFoundException {

            PackageInfo info = new PackageInfo();
            info.signatures = new Signature[] {
                new Signature(testSignature)
            };
            return info;
        }
    }
}
