/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb;

import android.app.Application;

import com.santisan.moviedb.ImageCache.ImageCacheParams;
import com.santisan.moviedb.model.Config;
import com.santisan.moviedb.model.PagedMovieSet;

public class MovieDbApp extends Application
{
    private static final String IMAGE_CACHE_DIR = "thumbs";
    
    private static Config.ImageConfig imageConfig;
    private static PagedMovieSet pagedMovieSet;
    
    @Override
    public void onCreate() 
    {
        super.onCreate();
        
        ImageCacheParams cacheParams = new ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(this, 0.125f);
        cacheParams.diskCacheSize = cacheParams.memCacheSize * 2;
        
        BitmapLoader.SetResources(getResources());
        BitmapLoader.AddImageCache(cacheParams);
    }
    
    @Override
    public void onLowMemory() {
        BitmapLoader.ClearMemCache();
        super.onLowMemory();
    }
    
    public static Config.ImageConfig getImageConfig() {
        return imageConfig;
    }
    
    public static void setImageConfig(Config.ImageConfig imageConfig) {
        MovieDbApp.imageConfig = imageConfig;
    }
    
    public static PagedMovieSet getPagedMovieSet() {
        return pagedMovieSet;
    }
    
    public static void setPagedMovieSet(PagedMovieSet pagedMovieSet) {
        MovieDbApp.pagedMovieSet = pagedMovieSet;
    }
}
