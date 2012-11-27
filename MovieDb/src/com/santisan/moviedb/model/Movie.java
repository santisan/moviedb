/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.santisan.moviedb.MovieDbApp;
import com.santisan.moviedb.Utils;
import com.santisan.moviedb.model.TrailerList.YoutubeTrailer;

public class Movie extends Entity
{
    public enum ImageSize { w92, w154, w185, w300, w342, w500, w780, w1280, original }
    //public enum ImageSize { w300, w780, w1280, original }    
    
    @SerializedName("backdrop_path")    private String backdropPath;
    @SerializedName("id")               private int id;
    @SerializedName("original_title")   private String originalTitle = "";
    @SerializedName("release_date")     private String releaseDate = "";
    @SerializedName("poster_path")      private String posterPath;
    @SerializedName("title")            private String title = "";
    @SerializedName("vote_average")     private float voteAverage;
    @SerializedName("vote_count")       private int voteCount;
    @SerializedName("overview")         private String overview = "";
    @SerializedName("runtime")          private int runtime;
    @SerializedName("trailers")         private TrailerList trailers;
    @SerializedName("casts")            private Casts casts;
    @SerializedName("images")           private ImageList images;
    private boolean inWatchlist = false;
    
    public Movie()
    {
    }
    
    public Movie(Parcel in)
    {
        backdropPath = in.readString();
        id = in.readInt();
        originalTitle = in.readString();
        releaseDate = in.readString();
        posterPath = in.readString();
        title = in.readString();
        voteAverage = in.readFloat();
        voteCount = in.readInt();
        overview = in.readString();
        runtime = in.readInt();
        trailers = new TrailerList();
        in.readTypedList(trailers.getYoutubeTrailers(), YoutubeTrailer.CREATOR);
        casts = new Casts();
        in.readTypedList(casts.getCast(), Actor.CREATOR);
        images = new ImageList();
        in.readTypedList(images.getBackdrops(), Image.CREATOR);
        in.readTypedList(images.getPosters(), Image.CREATOR);
        inWatchlist = (in.readInt() == 1);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) 
    {
        dest.writeString(backdropPath);
        dest.writeInt(id);
        dest.writeString(originalTitle);
        dest.writeString(releaseDate);
        dest.writeString(posterPath);
        dest.writeString(title);
        dest.writeFloat(voteAverage);
        dest.writeInt(voteCount);
        dest.writeString(overview);
        dest.writeInt(runtime);
        dest.writeTypedList(trailers.getYoutubeTrailers());
        dest.writeTypedList(casts.getCast());
        dest.writeTypedList(images.getBackdrops());
        dest.writeTypedList(images.getPosters());
        dest.writeInt(inWatchlist ? 1 : 0);
    }
    
    public String getBackdropPath() {
        return backdropPath;
    }
    
    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getOriginalTitle() {
        return originalTitle;
    }
    
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }
    
    public String getPosterPath() {
        return posterPath;
    }
    
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
    
    public String getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public float getVoteAverage() {
        return voteAverage;
    }
    
    public void setVoteAverage(float voteAverage) {
        this.voteAverage = voteAverage;
    }
    
    public int getVoteCount() {
        return voteCount;
    }
    
    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }
    
    public String getOverview() {
        return overview;
    }
    
    public void setOverview(String overview) {
        this.overview = overview;
    }
    
    public int getRuntime() {
        return runtime;
    }
    
    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }
    
    public TrailerList getTrailers() {
        return trailers;
    }
    
    public void setTrailers(TrailerList trailers) {
        this.trailers = trailers;
    }
    
    public Casts getCasts() {
        return casts;
    }
    
    public void setCasts(Casts casts) {
        this.casts = casts;
    }
    
    public boolean isInWatchlist() {
        return inWatchlist;
    }
    
    public void setInWatchlist(boolean inWatchlist) {
        this.inWatchlist = inWatchlist;
    }
    
    public ImageList getImages() {
        return images;
    }
    
    public void setImages(ImageList images) {
        this.images = images;
    }
    
    public String getYoutubeTrailer()
    {
        String defaultSource = "";
        for (YoutubeTrailer trailer : getTrailers().getYoutubeTrailers()) 
        {
            if (trailer.getSize().toUpperCase().equals("HD")) {
                return trailer.getSource();
            }
            defaultSource = trailer.getSource();
        }
        return defaultSource;
    }
    
    public String getPosterUrl(ImageSize size)
    {
        if (Utils.isNullOrWhitespace(posterPath)) return "";
        return MovieDbApp.getImageConfig().getBaseUrl() + size.name() + posterPath;
    }

    public String getBackdropUrl(ImageSize size) 
    {
        if (Utils.isNullOrWhitespace(backdropPath)) return "";
        return MovieDbApp.getImageConfig().getBaseUrl() + size.name() + backdropPath;
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {        
        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
        
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }
    };
}
