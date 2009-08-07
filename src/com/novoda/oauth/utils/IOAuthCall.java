
package com.novoda.oauth.utils;

import com.novoda.oauth.OAuthObject;

import net.oauth.OAuthMessage;

import java.util.Map;

public interface IOAuthCall {
    public abstract void onCreate(OAuthObject oauthData);
    public abstract OAuthMessage call(final String url, final Map<String, String> fields);
}
