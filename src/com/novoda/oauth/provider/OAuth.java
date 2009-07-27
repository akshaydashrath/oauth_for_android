/*
 * Copyright (C) 2009 Novoda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.novoda.oauth.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for OAuthProvider
 */
public final class OAuth {
    public static final String AUTHORITY = "com.novoda.oauth.provider.OAuth";

    // This class cannot be instantiated
    private OAuth() {}

    /**
     * OAuth provider table
     */
    public static final class Providers implements BaseColumns {
        // This class cannot be instantiated
        private Providers() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/providers");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of OAuth providers.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.novoda.oauth";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single OAuth provider.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.novoda.oauth";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        /**
         * the request token URL
         * <P>Type: TEXT</P>
         */
        public static final String REQUEST_TOKEN_URL = "request_token_url";

        /**
         * The access token URL
         * <P>Type: TEXT</P>
         */
        public static final String ACCESS_TOKEN_URL = "access_token_url";       
        
        /**
         * The authorize URL
         * <P>Type: TEXT</P>
         */
        public static final String AUTHORIZE_URL = "authorize_url";

        /**
         * The consumer key for a specific application
         * <P>Type: TEXT</P>
         */
        public static final String CONSUMER_KEY = "consumer_key";
        
        /**
         * The consumer secret for a specific application
         * <P>Type: TEXT</P>
         */
        public static final String CONSUMER_SECRET = "consumer_secret";
        
        /**
         * The request token - should be deleted upon activation
         * <P>Type: TEXT</P>
         */
        public static final String REQUEST_TOKEN = "request_token";
        
        /**
         * The access token
         * <P>Type: TEXT</P>
         */
        public static final String ACCESS_TOKEN = "access_token";
        
        /**
         * The access token secret
         * <P>Type: TEXT</P>
         */
        public static final String ACCESS_SECRET = "access_secret";
        
        /**
         * The timestamp for when the provider was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the provider was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }
}