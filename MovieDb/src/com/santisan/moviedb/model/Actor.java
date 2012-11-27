/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Actor extends Entity
{
    @SerializedName("id")           private int id;
    @SerializedName("name")         private String name;
    @SerializedName("character")    private String character;
    @SerializedName("order")        private int order;
    @SerializedName("castId")       private int castId;
    @SerializedName("profile_path") private String profilePath;
    
    public Actor()
    {
    }
    
    public Actor(Parcel in)
    {
        id = in.readInt();
        name = in.readString();
        character = in.readString();
        order = in.readInt();
        castId = in.readInt();
        profilePath = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) 
    {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(character);
        dest.writeInt(order);
        dest.writeInt(castId);
        dest.writeString(profilePath);
    }

    public int getCastId() {
        return castId;
    }
    
    public void setCastId(int cast_id) {
        this.castId = cast_id;
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
    
    public static final Parcelable.Creator<Actor> CREATOR = new Parcelable.Creator<Actor>() {        
        @Override
        public Actor[] newArray(int size) {
            return new Actor[size];
        }
        
        @Override
        public Actor createFromParcel(Parcel source) {
            return new Actor(source);
        }
    };   
}
