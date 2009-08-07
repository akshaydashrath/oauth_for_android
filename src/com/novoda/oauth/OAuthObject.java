package com.novoda.oauth;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

import java.io.Serializable;
import java.util.HashMap;

public class OAuthObject implements Serializable {
	/**
	 * 
	 */
	private static final long		serialVersionUID	= 1L;

	private String					consumerKey;
	private String					consumerSecret;
	private String					requestTokenURL;
	private String					accessTokenURL;
	private String					authorizeURL;
	private String					callback			= null;
	private HashMap<String, String>	parameters;
	private String					token;
	private String					tokenSecret;
	private String					packageName;
	private String					appName;

	public OAuthObject() {};
	public OAuthObject(String consumerKey, String consumerSecret, String requestTokenURL, String accessTokenURL,
			String authorizeURL, String callback, HashMap<String, String> parameters, String token, String tokenSecret,
			String packageName, String appName) {
		super();
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.requestTokenURL = requestTokenURL;
		this.accessTokenURL = accessTokenURL;
		this.authorizeURL = authorizeURL;
		this.callback = callback;
		this.parameters = parameters;
		this.token = token;
		this.tokenSecret = tokenSecret;
		this.packageName = packageName;
		this.appName = appName;
	}

	public static OAuthObject fromBundle(Bundle bundle) {
		return new OAuthObject(bundle.getString("consumerKey"), bundle.getString("consumerSecret"), bundle
				.getString("requestTokenURL"), bundle.getString("accessTokenURL"), bundle.getString("authorizeURL"),
				bundle.getString("callback"), (HashMap<String, String>) bundle.getSerializable("parameters"), bundle.getString("token"), bundle
						.getString("tokenSecret"), bundle.getString("packageName"), bundle.getString("appName"));
	}
	
	private ContentValues toContentValues() {
		return null;
	}
    public String getConsumerKey() {
        return consumerKey;
    }
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }
    public String getConsumerSecret() {
        return consumerSecret;
    }
    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }
    public String getRequestTokenURL() {
        return requestTokenURL;
    }
    public void setRequestTokenURL(String requestTokenURL) {
        this.requestTokenURL = requestTokenURL;
    }
    public String getAccessTokenURL() {
        return accessTokenURL;
    }
    public void setAccessTokenURL(String accessTokenURL) {
        this.accessTokenURL = accessTokenURL;
    }
    public String getAuthorizeURL() {
        return authorizeURL;
    }
    public void setAuthorizeURL(String authorizeURL) {
        this.authorizeURL = authorizeURL;
    }
    public String getCallback() {
        return callback;
    }
    public void setCallback(String callback) {
        this.callback = callback;
    }
    public HashMap<String, String> getParameters() {
        return parameters;
    }
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getTokenSecret() {
        return tokenSecret;
    }
    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }
    public String getPackageName() {
        return packageName;
    }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public String getAppName() {
        return appName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}
