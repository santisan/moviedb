/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.ui;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.santisan.moviedb.MovieDbApp;
import com.santisan.moviedb.MovieDbClient.MovieListType;
import com.santisan.moviedb.R;
import com.santisan.moviedb.UserUtils;
import com.santisan.moviedb.model.AuthToken;

public class MainActivity extends SherlockFragmentActivity
{  
    public static final String TAG = "MainActivity";
    private static final String AUTH_TOKEN = "authToken";
    
    private UserUtils userUtils;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        userUtils = MovieDbApp.getUserUtils();
        if (savedInstanceState != null) {
            userUtils.setAuthToken((AuthToken)savedInstanceState.getParcelable(AUTH_TOKEN));            
        }        
        userUtils.getNewSession();
        
        getSupportFragmentManager().beginTransaction().
            replace(R.id.container1, MovieListFragment.newInstance(MovieListType.NowPlaying, 4)).
            replace(R.id.container2, MovieListFragment.newInstance(MovieListType.Upcoming, 4)).
        commit();
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
        userUtils.getNewSession();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        userUtils.getNewSession();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable(AUTH_TOKEN, userUtils.getAuthToken());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        if (item.getItemId() == R.id.log_in) 
        {
            userUtils.requestAuthToken(this);
            return true;
        }
        if (item.getItemId() == R.id.movies) 
        {
            startActivity(new Intent(this, MovieListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
}
