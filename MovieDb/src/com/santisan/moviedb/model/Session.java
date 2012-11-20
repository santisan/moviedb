/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import com.google.gson.annotations.SerializedName;

public class Session
{
    @SerializedName("session_id") private String sessionId;
    @SerializedName("success")    private boolean successful;
    
    public Session()
    {
    }
    
    public Session(String id) {
        sessionId = id;
        successful = true;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
