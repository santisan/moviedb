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
import android.view.animation.AnimationUtils;
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
        
        String type = getIntent().getStringExtra(EXTRA_MOVIE_LIST_TYPE);
        requireSession = getIntent().getBooleanExtra(EXTRA_REQUIRE_SESSION, false);
        if (type == null) 
            movieListType = MovieListType.Popular;
        else 
            movieListType = MovieListType.valueOf(type);
                
        movieSet = MovieDbApp.getPagedMovieSet(movieListType);
        SetupViewPager();
    }
    
    private void SetupViewPager()
    {      
        adapter = new MoviePagerAdapter(getSupportFragmentManager());        
        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.setPageMargin((int)getResources().getDimension(R.dimen.image_detail_pager_margin));
        pager.setOffscreenPageLimit(2);
        pager.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            
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
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
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
                    }
                });                
            }
           
            return MovieDetailFragment.newInstance((int)getItemId(position), position, movieListType);
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
