/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TrailerList
{
    @SerializedName("youtube") private List<YoutubeTrailer> youtubeTrailers;

    public TrailerList()
    {
    }
    
    public List<YoutubeTrailer> getYoutubeTrailers() {
        return youtubeTrailers;
    }
    
    public void setYoutubeTrailers(List<YoutubeTrailer> youtubeTrailers) {
        this.youtubeTrailers = youtubeTrailers;
    }
    
    public class YoutubeTrailer
    {
        private String name;
        private String size;
        private String source;
        
        public YoutubeTrailer()
        {
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
    }
}
