/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.model;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class Entity implements Parcelable
{
    public int describeContents() {
        return 0;
    }

    public abstract void writeToParcel(Parcel dest, int flags);
    
}
