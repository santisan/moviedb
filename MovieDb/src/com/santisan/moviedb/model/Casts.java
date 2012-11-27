/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Casts
{
    private final static int NUM_ACTORS_FOR_SHORT_STRING = 3;
    
    private List<Actor> cast = new ArrayList<Actor>();
    //private List<Crew> crew;
    
    public Casts()
    {
    }
    
    public List<Actor> getCast() {
        return cast;
    }
    
    public void setCast(List<Actor> cast) {
        this.cast = cast;
    }

    public String getCastShortString() 
    {   
        if (cast.isEmpty()) return "";
        
        int castSize = cast.size(); 
        if (castSize > 1) 
        {
            Collections.sort(cast, new Comparator<Actor>() {
                @Override
                public int compare(Actor lhs, Actor rhs) 
                {
                    if (lhs.getOrder() < rhs.getOrder())
                        return -1;
                    if (lhs.getOrder() == rhs.getOrder())
                        return 0;
                    
                    return 1;
                }
            });
        }
        
        StringBuilder result = new StringBuilder(cast.get(0).getName());
        for (int i=1; i < castSize && i < NUM_ACTORS_FOR_SHORT_STRING; i++) 
        {
            result.append(", ").append(cast.get(i).getName());
        }
        
        return result.toString();
    }
}
