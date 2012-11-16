package com.santisan.moviedb;

import org.apache.http.NameValuePair;
import android.util.Pair;

public class NameValue extends Pair<String, String> implements NameValuePair {
        
    public NameValue(String key, String value) {
        super(key, value);
    }

    @Override
    public String getName() {
        return first;
    }

    @Override
    public String getValue() {
        return second;
    }     
}