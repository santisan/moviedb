/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import com.google.gson.annotations.SerializedName;

public class Config
{
    @SerializedName("images") private ImageConfig imageConfig;
    
    public Config()
    {
    }
    
    public ImageConfig getImageConfig() {
        return imageConfig;
    }
    
    public void setImageConfig(ImageConfig imageConfig) {
        this.imageConfig = imageConfig;
    }
    
    public class ImageConfig
    {
        @SerializedName("base_url")         private String baseUrl;
        @SerializedName("poster_sizes")     private String[] posterSizes;
        @SerializedName("backdrop_sizes")   private String[] backdropSizes;
        
        public ImageConfig()
        {
        }
        
        public String[] getBackdropSizes() {
            return backdropSizes;
        }
        
        public void setBackdropSizes(String[] backdropSizes) {
            this.backdropSizes = backdropSizes;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public String[] getPosterSizes() {
            return posterSizes;
        }
        
        public void setPosterSizes(String[] posterSizes) {
            this.posterSizes = posterSizes;
        }
    }
}
