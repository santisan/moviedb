/**
 * Copyright (C) 2012 Santiago S�nchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import com.google.gson.annotations.SerializedName;

public class PostResponse
{
    @SerializedName("status_code")      private int statusCode;
    @SerializedName("status_message")   private String statusMessage;
    
    public PostResponse()
    {
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
