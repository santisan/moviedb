/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.santisan.moviedb.BitmapLoader;
import com.santisan.moviedb.BitmapLoaderAsync;
import com.santisan.moviedb.MovieDbApp;
import com.santisan.moviedb.MovieDbClient;
import com.santisan.moviedb.MovieDbClient.MovieDbResultListener;
import com.santisan.moviedb.R;
import com.santisan.moviedb.Utils;
import com.santisan.moviedb.model.Config;
import com.santisan.moviedb.model.Movie;
import com.santisan.moviedb.model.Movie.PosterSize;
import com.santisan.moviedb.model.PagedMovieSet;

public class MainActivity extends SherlockListActivity
{  
    public static final String TAG = "MainActivity";
    private MoviesAdapter adapter;
    private PagedMovieSet pagedMovieSet;
    private BitmapLoader imageLoader;
    private DisplayMetrics displayMetrics;
    private WindowManager windowManager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_list);
        imageLoader = new BitmapLoaderAsync();      
        
        MovieDbClient client = new MovieDbClient();        
        client.getConfig(new MovieDbResultListener<Config>() {            
            @Override
            public void onResult(Config result) 
            {
                if (result == null) {
                    Toast.makeText(MainActivity.this, "web service unavailable, try again later", 
                            Toast.LENGTH_LONG).show();
                    return;
                }
                MovieDbApp.setImageConfig(result.getImageConfig());
            }
        });
        
        client.getNowPlaying(new MovieDbResultListener<PagedMovieSet>() {
            @Override
            public void onResult(PagedMovieSet result) 
            {
                if (result == null) {
                    Toast.makeText(MainActivity.this, "web service unavailable, try again later", 
                            Toast.LENGTH_LONG).show();
                    return;
                }
                pagedMovieSet = result;
                MovieDbApp.setPagedMovieSet(pagedMovieSet);
                adapter = new MoviesAdapter(MainActivity.this);
                setListAdapter(adapter);
            }
        });
        
        displayMetrics = new DisplayMetrics();         
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
    }
    
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) 
    {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_POSITION, position);
        startActivity(intent);
    }
    
    private class MoviesAdapter extends BaseAdapter
    {
        private LayoutInflater inflater = null;
        private ViewHolder viewHolder;
        
        public MoviesAdapter(Context ctx)
        {
            super();
            inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public int getCount() 
        {
            if (pagedMovieSet.getPage() < pagedMovieSet.getTotalPages())
                return pagedMovieSet.getMovies().size() + 1;
            
            return pagedMovieSet.getMovies().size();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            if (pagedMovieSet.getPage() < pagedMovieSet.getTotalPages() && position >= pagedMovieSet.getMovies().size())
            {
                //fetch more results
                MovieDbClient client = new MovieDbClient();                
                client.getNowPlaying(pagedMovieSet.getPage() + 1, new MovieDbResultListener<PagedMovieSet>() {
                    @Override
                    public void onResult(PagedMovieSet result) 
                    {
                        if (result == null) {
                            Toast.makeText(MainActivity.this, "web service unavailable, try again later", 
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        pagedMovieSet.setPage(result.getPage());
                        pagedMovieSet.getMovies().addAll(result.getMovies());
                        notifyDataSetChanged();
                    }
                });
                
                ProgressBar progressBar = new ProgressBar(MainActivity.this);
                progressBar.setIndeterminate(true);
                progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                return progressBar;
            }
            
            if (convertView == null || ProgressBar.class.isInstance(convertView)) 
            {
                convertView = inflater.inflate(R.layout.movies_listview_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.image = (ImageView)convertView.findViewById(R.id.icon);                
                viewHolder.topText = (TextView)convertView.findViewById(R.id.toptext);
                viewHolder.bottomText = (TextView)convertView.findViewById(R.id.bottomtext);  
                viewHolder.ratingBar = (RatingBar)convertView.findViewById(R.id.ratingBar);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            
            Movie movie = (Movie)getItem(position);
            if (movie != null)
            {                
                String url = movie.getPosterUrl(PosterSize.w92);
                if (!Utils.isNullOrWhitespace(url)) {
                    imageLoader.LoadBitmapAsync(url, viewHolder.image);
                }                
                viewHolder.topText.setText(movie.getTitle());
                viewHolder.bottomText.setText(getString(R.string.release_date, movie.getReleaseDate()));
                viewHolder.ratingBar.setRating(movie.getVoteAverage() * 0.5f);
            }
            return convertView;
        }
        
        private class ViewHolder {
            ImageView image;
            TextView topText;
            TextView bottomText;
            RatingBar ratingBar;
        }

        @Override
        public Object getItem(int position) {
            return pagedMovieSet.getMovies().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        imageLoader.CancelTasks();
        BitmapLoader.FlushCache();
    }
    
    @Override
    public void onStop() {
        imageLoader.CancelTasks();
        BitmapLoader.FlushCache();
        super.onStop();
    }  
}
