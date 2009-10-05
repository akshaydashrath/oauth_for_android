
package com.novoda.oauth.utils;

import com.novoda.oauth.OAuthObject;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class OAuthCall implements IOAuthCall {

    @SuppressWarnings("unused")
    private static final String TAG = "OAuth:";

    private String endpoint;

    private Map<String, String> fields;

    private oauth.signpost.OAuthConsumer consumer2;

    public OAuthCall(OAuthObject oauthData, String endpoint, Map<String, String> fields) {
        this.onCreate(oauthData);
        this.endpoint = endpoint;
        if (fields == null)
            this.fields = new HashMap<String, String>();
        else
            this.fields = fields;
    }

    public void onCreate(OAuthObject oauthData) {
        consumer2 = new CommonsHttpOAuthConsumer(oauthData.getConsumerKey(), oauthData
                .getConsumerSecret(), SignatureMethod.HMAC_SHA1);
        consumer2.setTokenWithSecret(oauthData.getToken(), oauthData.getTokenSecret());
    }

    public String call() {
        return call(endpoint, fields);
    }

    public String call(String url, Map<String, String> fields) {

        // create an HTTP request to a protected resource
        HttpGet request = new HttpGet(endpoint);

        HttpParams p = new BasicHttpParams();

        for (Entry<String, String> en : fields.entrySet()) {
            p.setParameter(en.getKey(), en.getValue());
        }

        request.setParams(p);

        // sign the request
        try {
            consumer2.sign(request);
        } catch (OAuthMessageSignerException e) {
            e.printStackTrace();
        } catch (OAuthExpectationFailedException e) {
            e.printStackTrace();
        }

        // send the request
        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpResponse response = httpClient.execute(request);
            return convertStreamToString(response.getEntity().getContent());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
