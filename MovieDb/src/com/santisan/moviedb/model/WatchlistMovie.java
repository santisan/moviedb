/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import com.google.gson.annotations.SerializedName;

public class WatchlistMovie
{
    @SerializedName("movie_id")         private int movieId;
    @SerializedName("movie_watchlist")  private boolean addToWatchlist;
    
    public WatchlistMovie()
    {
    }
    
    public WatchlistMovie(int id, boolean add) {
        this.movieId = id;
        this.addToWatchlist = add;
    }
    
    public int getMovieId() {
        return movieId;
    }
    
    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }
    
    public boolean getAddToWatchlist() {
        return addToWatchlist;
    }
    
    public void setAddToWatchlist(boolean addToWatchlist) {
        this.addToWatchlist = addToWatchlist;
    }
}
