/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class PagedMovieSet
{
    @SerializedName("page")             private int page;
    @SerializedName("results")          private List<Movie> movies;
    @SerializedName("total_pages")      private int totalPages;
    @SerializedName("total_results")    private int totalMovies;
    
    public PagedMovieSet()
    {
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public List<Movie> getMovies() {
        return movies;
    }
    
    public void setMovies(List<Movie> results) {
        this.movies = results;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public int getTotalMovies() {
        return totalMovies;
    }
    
    public void setTotalMovies(int totalResults) {
        this.totalMovies = totalResults;
    }
}
