/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.santisan.moviedb.MovieDbApp;
import com.santisan.moviedb.MovieDbClient.MovieListType;
import com.santisan.moviedb.R;
import com.santisan.moviedb.UserUtils;
import com.santisan.moviedb.UserUtils.UserLoginListener;
import com.santisan.moviedb.model.AuthToken;

public class MainActivity extends SherlockFragmentActivity
{
    public static final String TAG = "MainActivity";
    private static final String SELECTED_TAB_INDEX = "tabIndex";
    private static final String AUTH_TOKEN = "authToken";
    
    private UserUtils userUtils;
    private Menu menu;
    private ProgressDialog progressDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        
        userUtils = MovieDbApp.getUserUtils();       
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        if (savedInstanceState != null) {
            userUtils.setAuthToken((AuthToken)savedInstanceState.getParcelable(AUTH_TOKEN)); 
            Log.d(TAG, "onCreate: restoring instance state");
        }
        getNewSession();
        
        Tab tab = actionBar.newTab().setText(getString(R.string.now_playing)).
                setTabListener(new TabListener(MovieListFragment.newInstance(MovieListType.NowPlaying)));
        actionBar.addTab(tab);
        
        tab = actionBar.newTab().setText(getString(R.string.upcoming)).
                setTabListener(new TabListener(MovieListFragment.newInstance(MovieListType.Upcoming)));
        actionBar.addTab(tab);
        
        tab = actionBar.newTab().setText(getString(R.string.popular)).
                setTabListener(new TabListener(MovieListFragment.newInstance(MovieListType.Popular)));
        actionBar.addTab(tab);
        
        tab = actionBar.newTab().setText(getString(R.string.top_rated)).
                setTabListener(new TabListener(MovieListFragment.newInstance(MovieListType.TopRated)));
        actionBar.addTab(tab);
        
        tab = actionBar.newTab().setText(getString(R.string.watchlist)).
                setTabListener(new TabListener(MovieListFragment.newInstance(MovieListType.Watchlist, true)));
        actionBar.addTab(tab);
        
        if (savedInstanceState != null)
            actionBar.setSelectedNavigationItem(savedInstanceState.getInt(SELECTED_TAB_INDEX));
    }
    
    private void getNewSession()
    {        
        if (userUtils.isLoggedIn() || userUtils.getAuthToken() == null)
            return;
        
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(R.string.login_in_progress);
            progressDialog.setMessage(getString(R.string.please_wait));
        }
        progressDialog.show();
        
        userUtils.getNewSession(loginListener);
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
        userUtils.getNewSession(loginListener);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        userUtils.getNewSession(loginListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) 
    {
        super.onSaveInstanceState(state);
        state.putInt(SELECTED_TAB_INDEX, getSupportActionBar().getSelectedNavigationIndex());
        state.putParcelable(AUTH_TOKEN, userUtils.getAuthToken());
        Log.d(TAG, "onSaveInstanceState");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.menu = menu;
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        updateLoginMenuItems();
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
        if (item.getItemId() == R.id.log_out) 
        {
            userUtils.logOut();
            updateLoginMenuItems();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private final UserLoginListener loginListener = new UserLoginListener() {        
        @Override
        public void onUserLoggedIn() {
            updateLoginMenuItems();
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        }

        @Override
        public void onError() {
            Toast.makeText(MainActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        }
    };
    
    private void updateLoginMenuItems()
    {
        if (menu == null) return;
        
        MenuItem itemToHide;
        MenuItem itemToShow;
        if (userUtils.isLoggedIn()) { 
            itemToHide = menu.findItem(R.id.log_in);
            itemToShow = menu.findItem(R.id.log_out);
        }
        else { 
            itemToHide = menu.findItem(R.id.log_out);
            itemToShow = menu.findItem(R.id.log_in);
        }
        itemToHide.setVisible(false);
        itemToHide.setEnabled(false);
        itemToShow.setVisible(true);
        itemToShow.setEnabled(true);
    }
    
    private static class TabListener implements ActionBar.TabListener
    {
        private boolean isFragmentAdded = false;
        private Fragment fragment;
        
        public TabListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) 
        {
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                        
            if (isFragmentAdded) {
                ft.attach(fragment);
            }
            else {
                ft.add(R.id.container, fragment);
                isFragmentAdded = true;
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (isFragmentAdded)          
                ft.detach(fragment);
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            
        }       
    } 
}
