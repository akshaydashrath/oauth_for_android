
package com.novoda.oauth.utils;

import android.os.Bundle;

import java.util.HashMap;

public class BundleWrapper {
    static HashMap<String, Object> toMap(Bundle bundle) {
        HashMap<String, Object> map = new HashMap<String, Object>(bundle.size());
        for (String key : bundle.keySet()) {
            map.put(key, bundle.get(key));
        }
        return map;
    }
}
