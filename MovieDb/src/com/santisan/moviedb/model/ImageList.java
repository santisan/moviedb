/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import java.util.ArrayList;
import java.util.List;

public class ImageList
{
    private List<Image> backdrops = new ArrayList<Image>();
    private List<Image> posters = new ArrayList<Image>();
    
    public ImageList()
    {
    }
    
    public List<Image> getBackdrops() {
        return backdrops;
    }
    
    public void setBackdrops(List<Image> backdrops) {
        this.backdrops = backdrops;
    }
    
    public List<Image> getPosters() {
        return posters;
    }
    
    public void setPosters(List<Image> posters) {
        this.posters = posters;
    }
    
    public List<Image> getAll() 
    {
        List<Image> list = new ArrayList<Image>(backdrops);
        list.addAll(posters);
        return list;
    }
}
