
package com.novoda.oauth.utils;

import com.novoda.oauth.OAuthObject;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class OAuthCall implements IOAuthCall {

    @SuppressWarnings("unused")
    private static final String TAG = "OAuth:";

    private OAuthServiceProvider provider;

    private OAuthConsumer consumer;

    private OAuthClient client;

    private OAuthAccessor accessor;

    private String endpoint;

    private Map<String, String> fields;

    public OAuthCall(OAuthObject oauthData, String endpoint, Map<String, String> fields) {
        this.onCreate(oauthData);
        this.endpoint = endpoint;
        if (fields == null)
            this.fields = new HashMap<String, String>();
    }

    @Override
    public void onCreate(OAuthObject oauthData) {
        provider = new OAuthServiceProvider(oauthData.getRequestTokenURL(), oauthData
                .getAuthorizeURL(), oauthData.getAccessTokenURL());
        consumer = new OAuthConsumer(oauthData.getCallback(), oauthData.getConsumerKey(), oauthData
                .getConsumerSecret(), provider);
        client = new OAuthClient(new HttpClient4());
        accessor = new OAuthAccessor(consumer);
        accessor.accessToken = oauthData.getToken();
        accessor.tokenSecret = oauthData.getTokenSecret();
    }

    public OAuthMessage call() {
        return call(endpoint, fields);
    }

    @Override
    public OAuthMessage call(String url, Map<String, String> fields) {
        try {
            fields.put(OAuth.OAUTH_TIMESTAMP, "" + System.currentTimeMillis());
            OAuthMessage request = client.invoke(accessor, url, fields.entrySet());
            return request;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OAuthException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
