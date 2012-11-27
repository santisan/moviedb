/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class TrailerList
{
    @SerializedName("youtube") private List<YoutubeTrailer> youtubeTrailers = new ArrayList<YoutubeTrailer>();

    public TrailerList()
    {
    }
    
    public List<YoutubeTrailer> getYoutubeTrailers() {
        return youtubeTrailers;
    }
    
    public void setYoutubeTrailers(List<YoutubeTrailer> youtubeTrailers) {
        this.youtubeTrailers = youtubeTrailers;
    }
    
    public static class YoutubeTrailer extends Entity
    {
        private String name;
        private String size;
        private String source;
        
        public YoutubeTrailer()
        {
        }
        
        public YoutubeTrailer(Parcel in)
        {
            name = in.readString();
            size = in.readString();
            source = in.readString();
        }

        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getSize() {
            return size;
        }
        
        public void setSize(String size) {
            this.size = size;
        }
        
        public String getSource() {
            return source;
        }
        
        public void setSource(String source) {
            this.source = source;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) 
        {
            dest.writeString(name);
            dest.writeString(size);
            dest.writeString(source);
        }
        
        public static final Parcelable.Creator<YoutubeTrailer> CREATOR = new Parcelable.Creator<YoutubeTrailer>() {        
            @Override
            public YoutubeTrailer[] newArray(int size) {
                return new YoutubeTrailer[size];
            }
            
            @Override
            public YoutubeTrailer createFromParcel(Parcel source) {
                return new YoutubeTrailer(source);
            }
        };
    }
}
