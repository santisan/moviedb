/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb;

import android.app.Application;
import android.util.Log;

import com.santisan.moviedb.ImageCache.ImageCacheParams;
import com.santisan.moviedb.MovieDbClient.MovieDbResultListener;
import com.santisan.moviedb.model.Config;
import com.santisan.moviedb.model.PagedMovieSet;

public class MovieDbApp extends Application
{    
    private static final String IMAGE_CACHE_DIR = "thumbs";
    
    private static Config.ImageConfig imageConfig;
    private static PagedMovieSet pagedMovieSet;
    private static UserUtils userUtils;
    
    @Override
    public void onCreate() 
    {
        super.onCreate();
        
        loadConfig();
        
        ImageCacheParams cacheParams = new ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(this, 0.125f);
        cacheParams.diskCacheSize = cacheParams.memCacheSize * 2;
        
        BitmapLoader.SetResources(getResources());
        BitmapLoader.AddImageCache(cacheParams);
        
        userUtils = new UserUtils(this);
    }
    
    private void loadConfig() 
    {
        MovieDbClient client = new MovieDbClient();
        client.getConfig(new MovieDbResultListener<Config>() {            
            @Override
            public void onResult(Config result) 
            {
                if (result == null) {
                    //Toast.makeText(MovieDbApp.this, "web service unavailable, try again later", 
                    //        Toast.LENGTH_LONG).show();
                    Log.e("getConfig", "web service unavailable");
                    return;
                }
                MovieDbApp.setImageConfig(result.getImageConfig());
            }
        });
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
    
    public static UserUtils getUserUtils() {
        return userUtils;
    }
    
    public static void setUserUtils(UserUtils userUtils) {
        MovieDbApp.userUtils = userUtils;
    }
}
