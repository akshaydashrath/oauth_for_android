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
 * Convenience definitions for OAuthProvider. This contains all the columns for
 * accessing the OAuth.
 */
public final class OAuth {
    public static final String AUTHORITY = "com.novoda.oauth.provider.OAuth";

    // This class cannot be instantiated
    private OAuth() {
    }

    /**
     * OAuth provider table which contains all the necessary information to
     * connect to a Service provider. If the {@link #ACCESS_TOKEN} is null then
     * the key has not been authorised by the user yet.
     * <p>
     * You can activate the key by calling TODO document intent
     * </p>
     */
    public static final class Registry implements BaseColumns {
        // This class cannot be instantiated
        private Registry() {
        }

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/registry");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of OAuth
         * providers.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.novoda.oauth";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * OAuth provider.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.novoda.oauth";

        /**
         * The default sort order for this table 
         * TODO work on the table name
         */
        public static final String DEFAULT_SORT_ORDER = "registry.modified DESC";

        /**
         * the common name given to the service (e.g. twitter). Will default to
         * the host of the {@link #URL}.
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String NAME = "name";

        /**
         * the description of the OAuth service provider. Useful if the user
         * does not have an account and needs to register.
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String DESCRIPTION = "description";

        /**
         * the common name given to the service (e.g. twitter). Will default to
         * the host of the {@link #REQUEST_TOKEN_URL}.
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String URL = "url";

        /**
         * the common icon given to this service
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String ICON = "_data";

        /**
         * the request token URL
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String REQUEST_TOKEN_URL = "request_token_url";

        /**
         * The access token URL
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String ACCESS_TOKEN_URL = "access_token_url";

        /**
         * The authorize URL
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String AUTHORIZE_URL = "authorize_url";

        /**
         * The consumer key for a specific service.
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String CONSUMER_KEY = "consumer_key";

        /**
         * The consumer secret for a specific application
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String CONSUMER_SECRET = "consumer_secret";

        /**
         * The access token
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String ACCESS_TOKEN = "access_token";

        /**
         * The access token secret
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String ACCESS_SECRET = "access_secret";

        /**
         * The timestamp for when the provider was created
         * <P>
         * Type: INTEGER (long from System.curentTimeMillis())
         * </P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the provider was last modified
         * <P>
         * Type: INTEGER (long from System.curentTimeMillis())
         * </P>
         */
        public static final String MODIFIED_DATE = "modified";
    }

    /**
     * The consumer represents an Android application that accesses OAuth
     * service providers. The consumer can be created either by accessing
     * providers via generic OAuth access authorised by the user through the
     * OAuth application or by registering to the OAuth provider via a call to
     * the content provider accessing the {@link Registry} table. This table is
     * never accessed by an external application. It is used and controlled
     * internally.
     * 
     * <pre>
     * ContentValues values = new ContentValues(); 
     * values.put('request_token_url', 'http://mysite.com/request_token');
     * values.put('access_token_url', 'http://mysite.com/access_token');
     * values.put('authorize_url', 'http://mysite.com/authorize');
     * values.put('consumer_key', 'myconsumerkey');
     * values.put('consumer_secret', 'mysecret'); 
     * getContentResolver().insert(Uri.parse('content://com.novoda.oauth.providers/registry'), values)
     * </pre>
     * 
     * @see TODO activity to register
     * @see TODO activity to call generic oauth
     */
    public static final class Consumers implements BaseColumns {
        private Consumers() {
        }

        /**
         * The content:// style URL for the consumer table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/consumers");

        /**
         * The package name.
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String PACKAGE_NAME = "package_name";

        /**
         * The application name.
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String APP_NAME = "app_name";

        /**
         * The _id of the row in the registry against which this application is
         * querying.
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String REGISTRY_ID = "fk_registry_id";

        /**
         * If this application owns the consumer key for the service defined by
         * {@link #REGISTRY_ID}.
         * <P>
         * Type: BOOLEAN
         * </P>
         */
        public static final String OWNS_CONSUMER_KEY = "is_owner";

        /**
         * If {@link #OWNS_CONSUMER_KEY} is set to true, check if the service
         * should be shared with other applications.
         * <P>
         * Type: BOOLEAN
         * </P>
         */
        public static final String IS_SERVICE_PUBLIC = "is_public";

        /**
         * The icon. default to the application's icon if none provided
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String ICON = "_data";

        /**
         * The application's signature.
         * <P>
         * Type: BLOB
         * </P>
         */
        public static final String SIGNATURE = "sig";

        /**
         * True if the application is banned. default to false
         * <P>
         * Type: BOOLEAN
         * </P>
         */
        public static final String IS_BANNED = "is_banned";

        /**
         * True if the application is authorise. default to false. This is set
         * if the user set the always authorised option when the request is
         * made.
         * <P>
         * Type: BOOLEAN
         * </P>
         */
        public static final String IS_AUTHORISED = "is_authorised";

        /**
         * The timestamp for when the provider was created
         * <P>
         * Type: INTEGER (long from System.curentTimeMillis())
         * </P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the provider was last modified
         * <P>
         * Type: INTEGER (long from System.curentTimeMillis())
         * </P>
         */
        public static final String MODIFIED_DATE = "modified";

        public static final String DEFAULT_SORT_ORDER = "consumers.modified DESC";;
    }
}
