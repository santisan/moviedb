/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */

package com.santisan.moviedb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.santisan.moviedb.MovieDbApp;
import com.santisan.moviedb.MovieDbClient;
import com.santisan.moviedb.MovieDbClient.MovieDbResultListener;
import com.santisan.moviedb.MovieDbClient.MovieListType;
import com.santisan.moviedb.R;
import com.santisan.moviedb.model.PagedMovieSet;

//TODO: agregar feedback visual para saber la posicion de la imagen actual en el album y si hay mas hacia los costados
//TODO: hacer transparente la imagen que se va mientras se esta arrastrando
public class MovieDetailActivity extends SherlockFragmentActivity
{    
    private static final String TAG = "MovieDetailActivity";
    public static final String EXTRA_MOVIE_POSITION = "extraMoviePosition";
    public static final String EXTRA_MOVIE_LIST_TYPE = "extraMovieListType";
    public static final String EXTRA_REQUIRE_SESSION = "extraRequireSession";

    private MoviePagerAdapter adapter;
    private ViewPager pager;
    private PagedMovieSet movieSet;
    private boolean requireSession;
    private MovieListType movieListType;    

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {        
        super.onCreate(savedInstanceState);        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.movie_detail_pager);        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        movieSet = MovieDbApp.getPagedMovieSet();
        
        String type = getIntent().getStringExtra(EXTRA_MOVIE_LIST_TYPE);
        requireSession = getIntent().getBooleanExtra(EXTRA_REQUIRE_SESSION, false);
        if (type == null) 
            movieListType = MovieListType.Popular;
        else 
            movieListType = MovieListType.valueOf(type);
        
        SetupViewPager();
    }   
    
    private void SetupViewPager()
    {      
        adapter = new MoviePagerAdapter(getSupportFragmentManager());        
        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.setPageMargin((int)getResources().getDimension(R.dimen.image_detail_pager_margin));
        pager.setOffscreenPageLimit(2);       
            
        final int currentItem = getIntent().getIntExtra(EXTRA_MOVIE_POSITION, -1);
        if (currentItem != -1) {
            pager.setCurrentItem(currentItem);
        }
    } 

    @Override
    public void onResume() 
    {
        super.onResume();        
        adapter.notifyDataSetChanged();
    }  
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        if (item.getItemId() == android.R.id.home) 
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there
     * could be a large number of items in the ViewPager and we don't want to retain them all in
     * memory at once but create/destroy them on the fly.
     */
    private class MoviePagerAdapter extends FragmentStatePagerAdapter 
    {
        public MoviePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() 
        {
            if (movieSet.getPage() < movieSet.getTotalPages())
                return movieSet.getMovies().size() + 1;
            
            return movieSet.getMovies().size();
        }

        @Override
        public Fragment getItem(final int position) 
        {
            //final MovieDetailFragment fragment = MovieDetailFragment.newInstance((int)getItemId(position), position);
            
            if (movieSet.getPage() < movieSet.getTotalPages() && position + 3 >= movieSet.getMovies().size())
            {               
                MovieDbClient client = new MovieDbClient();
                client.getMovieList(movieListType, movieSet.getPage() + 1, requireSession,
                        new MovieDbResultListener<PagedMovieSet>() 
                {               
                    @Override
                    public void onResult(PagedMovieSet result) 
                    {
                        if (result == null) {
                            Toast.makeText(MovieDetailActivity.this, "web service unavailable, try again later", 
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        movieSet.setPage(result.getPage());
                        movieSet.getMovies().addAll(result.getMovies());
                        notifyDataSetChanged();
                        
                        //if (fragment != null && fragment.getPosition() == position)
                        //    fragment.setMovie((int)getItemId(position), progressDialog);
                    }
                });
                
                //return MovieDetailFragment.newInstance(-1, position);
            }
           
            return MovieDetailFragment.newInstance((int)getItemId(position), position); //fragment;
        }
        
        @Override
        public CharSequence getPageTitle(int position) 
        {
            if (position < movieSet.getMovies().size())
                return movieSet.getMovies().get(position).getTitle();
            
            return "Movie " + position;
        }
                
        //@Override
        public long getItemId(int position) 
        {
            if (position < movieSet.getMovies().size())
                return movieSet.getMovies().get(position).getId();
            
            return position;
        }        
    }      
    
}
