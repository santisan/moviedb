/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class AuthToken extends Entity
{
    @SerializedName("request_token") private String requestToken;
    @SerializedName("success")       private boolean successful;
    private String authCallback;
    
    public AuthToken()
    {
    }
    
    public AuthToken(Parcel in)
    {
        requestToken = in.readString();
        successful = (in.readInt() == 1) ? true : false;
        authCallback = in.readString();
    }

    public String getRequestToken() {
        return requestToken;
    }
    
    public void setRequestToken(String requestToken) {
        this.requestToken = requestToken;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
    public String getAuthCallback() {
        return authCallback;
    }
    
    public void setAuthCallback(String authCallback) {
        this.authCallback = authCallback;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) 
    {
        dest.writeString(requestToken);
        dest.writeInt(successful ? 1 : 0);
        dest.writeString(authCallback);
    }
    
    public static final Parcelable.Creator<AuthToken> CREATOR = new Parcelable.Creator<AuthToken>() {        
        @Override
        public AuthToken[] newArray(int size) {
            return new AuthToken[size];
        }
        
        @Override
        public AuthToken createFromParcel(Parcel source) {
            return new AuthToken(source);
        }
    };
}
