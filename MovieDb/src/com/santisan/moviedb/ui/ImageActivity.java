/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.santisan.moviedb.BitmapLoader;
import com.santisan.moviedb.BitmapLoaderAsync;
import com.santisan.moviedb.R;
import com.santisan.moviedb.Utils;

public class ImageActivity extends SherlockActivity
{
    public static final String IMAGE_URL_EXTRA = "imageUrlExtra";

    @TargetApi(11)
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        
        DisplayMetrics display = new DisplayMetrics();         
        WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(display);
        
        String imageUrl = getIntent().getStringExtra(IMAGE_URL_EXTRA);
        
        BitmapLoader loader = new BitmapLoaderAsync(false);
        final ImageView imageView = (ImageView)findViewById(R.id.image);
        loader.LoadBitmapAsync(imageUrl, imageView , display.widthPixels, display.heightPixels);
        
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.hide();
        
        if (Utils.hasHoneycomb())
        {
            imageView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int vis)
                {
                    if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0)
                        actionBar.hide();
                    else
                        actionBar.show();
                }
            });        
        }
        
        imageView.setOnClickListener(new OnClickListener() {            
            @Override
            public void onClick(View v) 
            {
                if (Utils.hasHoneycomb()) 
                {
                    final int vis = imageView.getSystemUiVisibility();
                    if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0)
                        imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);            
                    else
                        imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                }
                else {
                    if (actionBar.isShowing())
                        actionBar.hide();
                    else
                        actionBar.show();
                }
            }
        });        
    }
}
