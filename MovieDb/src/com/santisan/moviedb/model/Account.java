/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Account
{
    @SerializedName("id")               private int id;
    @SerializedName("include_adult")    private boolean includeAdult;
    @SerializedName("iso_3166_1")       private String countryCode;
    @SerializedName("iso_639_1")        private String languageCode;
    @SerializedName("name")             private String name;
    @SerializedName("username")         private String username;
    
    private Map<Integer, Integer> watchlist = new HashMap<Integer, Integer>();
    
    public Account()
    {
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getLanguageCode() {
        return languageCode;
    }
    
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public boolean getIncludeAdult() {
        return includeAdult;
    }
    
    public void setIncludeAdult(boolean includeAdult) {
        this.includeAdult = includeAdult;
    }
    
    public Map<Integer, Integer> getWatchlist() {
        return watchlist;
    }
    
    public void setWatchlist(Map<Integer, Integer> watchlist) {
        this.watchlist = watchlist;
    }
}
