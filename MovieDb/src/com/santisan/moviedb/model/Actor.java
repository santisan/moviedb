/**
 * Copyright (C) 2012 Santiago S�nchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import com.google.gson.annotations.SerializedName;

public class Actor
{
    @SerializedName("id")           private int id;
    @SerializedName("name")         private String name;
    @SerializedName("character")    private String character;
    @SerializedName("order")        private int order;
    @SerializedName("cast_id")      private int cast_id;
    @SerializedName("profile_path") private String profilePath;
    
    public Actor()
    {
    }
    
    public int getCast_id() {
        return cast_id;
    }
    
    public void setCast_id(int cast_id) {
        this.cast_id = cast_id;
    }
    
    public String getCharacter() {
        return character;
    }
    
    public void setCharacter(String character) {
        this.character = character;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getOrder() {
        return order;
    }
    
    public void setOrder(int order) {
        this.order = order;
    }
    
    public String getProfilePath() {
        return profilePath;
    }
    
    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }
}
