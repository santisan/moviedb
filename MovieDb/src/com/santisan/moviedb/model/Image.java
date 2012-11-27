/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import com.google.gson.annotations.SerializedName;
import com.santisan.moviedb.MovieDbApp;
import com.santisan.moviedb.Utils;
import com.santisan.moviedb.model.Movie.ImageSize;

import android.os.Parcel;
import android.os.Parcelable;

public class Image extends Entity
{
    @SerializedName("file_path") private String filePath;
    @SerializedName("width")     private int width;
    @SerializedName("height")    private int height;
    
    public Image()
    {
    }

    public Image(Parcel in)
    {
        filePath = in.readString();
        width = in.readInt();
        height = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) 
    {
        dest.writeString(filePath);
        dest.writeInt(width);
        dest.writeInt(height);
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public String getUrl(ImageSize size)
    {
        if (Utils.isNullOrWhitespace(filePath)) return "";
        return MovieDbApp.getImageConfig().getBaseUrl() + size.name() + filePath;
    }
    
    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {        
        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
        
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }
    };
}
