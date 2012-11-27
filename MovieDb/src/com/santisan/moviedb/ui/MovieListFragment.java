/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.ui;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.santisan.moviedb.BitmapLoader;
import com.santisan.moviedb.BitmapLoaderAsync;
import com.santisan.moviedb.MovieDbApp;
import com.santisan.moviedb.MovieDbClient;
import com.santisan.moviedb.MovieDbClient.MovieDbResultListener;
import com.santisan.moviedb.MovieDbClient.MovieListType;
import com.santisan.moviedb.R;
import com.santisan.moviedb.Utils;
import com.santisan.moviedb.model.Movie;
import com.santisan.moviedb.model.Movie.BackdropSize;
import com.santisan.moviedb.model.PagedMovieSet;

public class MovieListFragment extends SherlockFragment implements OnItemClickListener, OnGlobalLayoutListener
{
    private interface OnGetMoviesInfoListener {
        void onGetMoviesInfoDone();
    }  
    
    public static final String TAG = "MovieListFragment";
    private static final String MOVIE_LIST_TYPE = "MovieListType";
    private static final String REQUIRE_SESSION = "RequireSession";
    private static final String NUM_MOVIES = "NumMovies";
    private MoviesAdapter adapter;
    private PagedMovieSet pagedMovieSet = null;
    private BitmapLoader imageLoader;
    private DisplayMetrics displayMetrics;
    private WindowManager windowManager;
    private GridView gridView;
    private MovieListType movieListType;
    private boolean requireSession = false;
    private int numMovies = -1;
    private MovieDbClient client = new MovieDbClient();
    
    private int itemWidth;
    private int itemHeight;
    private int itemSpacing;
    
    public static MovieListFragment newInstance(MovieListType movieListType) {
        return newInstance(movieListType, false);
    }
    
    public static MovieListFragment newInstance(MovieListType movieListType, int numMovies) {
        return newInstance(movieListType, false);
    }
    
    public static MovieListFragment newInstance(MovieListType movieListType, boolean requireSession) {
        return newInstance(movieListType, requireSession, -1);
    }
    
    public static MovieListFragment newInstance(MovieListType movieListType, boolean requireSession, int numMovies)
    {        
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        args.putString(MOVIE_LIST_TYPE, movieListType.name());
        args.putBoolean(REQUIRE_SESSION, requireSession);
        args.putInt(NUM_MOVIES, numMovies);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        adapter = new MoviesAdapter(getSherlockActivity());
        imageLoader = new BitmapLoaderAsync(false);
        
        displayMetrics = new DisplayMetrics();
        windowManager = (WindowManager)getSherlockActivity().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);   
        
        movieListType = MovieListType.valueOf(getArguments().getString(MOVIE_LIST_TYPE));
        requireSession = getArguments().getBoolean(REQUIRE_SESSION);
        numMovies = getArguments().getInt(NUM_MOVIES);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle args) 
    {
        View v = inflater.inflate(R.layout.movie_list, null);
        gridView = (GridView)v.findViewById(R.id.gridView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        
        itemWidth =  gridView.getWidth() / 2;
        itemHeight = (int)getResources().getDimension(R.dimen.list_item_background_height);
        itemSpacing = (int)getResources().getDimension(R.dimen.movie_cell_spacing);
        
        gridView.setColumnWidth(itemWidth);
        gridView.setHorizontalSpacing(itemSpacing);
        gridView.setVerticalSpacing(itemSpacing);
        gridView.setNumColumns(2);
        
        return v;
    }
    
    @Override
    public void onActivityCreated(Bundle args) 
    {
        super.onActivityCreated(args);
        
        client.getMovieList(movieListType, requireSession, new MovieDbResultListener<PagedMovieSet>() {
            @Override
            public void onResult(PagedMovieSet result) 
            {
                if (result == null) {
                    Toast.makeText(getSherlockActivity(), "web service unavailable, try again later", 
                            Toast.LENGTH_LONG).show();
                    return;
                }
                
                pagedMovieSet = result;
                MovieDbApp.setPagedMovieSet(pagedMovieSet);                
                                
                getMoviesInfo(result.getMovies(), new OnGetMoviesInfoListener() {                    
                    @Override
                    public void onGetMoviesInfoDone() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }          
        });       
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id) 
    {
        Intent intent = new Intent(getSherlockActivity(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_POSITION, position);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_LIST_TYPE, movieListType.name());
        intent.putExtra(MovieDetailActivity.EXTRA_REQUIRE_SESSION, requireSession);
        startActivity(intent);
    }   
    
    @Override
    public void onGlobalLayout() 
    {
        if (adapter.getNumColumns() != 0) return;
        
        adapter.setNumColumns(2);
        adapter.setItemSize(itemWidth - itemSpacing, itemHeight - itemSpacing);
    }  
    
    private void getMoviesInfo(final List<Movie> movies, final OnGetMoviesInfoListener listener) 
    {        
        final int movieCount = movies.size();
        for (final Movie movie : movies)
        {
            final int index = movies.indexOf(movie);            
            
            client.getMovieWithCasts(movie.getId(), new MovieDbResultListener<Movie>() {                
                @Override
                public void onResult(Movie result) 
                {                    
                    movies.remove(index);
                    movies.add(index, result);
                    
                    if (index == movieCount - 1)
                        listener.onGetMoviesInfoDone();
                }               
            });
        }
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
    
    private class MoviesAdapter extends BaseAdapter
    {
        private LayoutInflater inflater = null;
        private ViewHolder viewHolder;
        private int itemWidth;
        private int itemHeight;
        private int numColumns = 0;        
        private AbsListView.LayoutParams layoutParams;
        
        public MoviesAdapter(Context ctx)
        {
            super();
            inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layoutParams = new AbsListView.LayoutParams(itemWidth, itemHeight);
        }       

        @Override
        public int getCount() 
        {
            if (pagedMovieSet == null) return 1;
            
            if (numMovies > 0) return numMovies;
            
            if (pagedMovieSet.getPage() < pagedMovieSet.getTotalPages())
                return pagedMovieSet.getMovies().size() + 1;
            
            return pagedMovieSet.getMovies().size();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            if (pagedMovieSet == null) return getProgressBar();
            
            if (pagedMovieSet.getPage() < pagedMovieSet.getTotalPages() && position >= pagedMovieSet.getMovies().size())
            {
                //fetch more results
                MovieDbClient client = new MovieDbClient();                
                client.getMovieList(movieListType, pagedMovieSet.getPage() + 1, requireSession,
                        new MovieDbResultListener<PagedMovieSet>() 
                {
                    @Override
                    public void onResult(final PagedMovieSet result) 
                    {
                        if (result == null) {
                            Toast.makeText(getSherlockActivity(), "web service unavailable, try again later", 
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        getMoviesInfo(result.getMovies(), new OnGetMoviesInfoListener() {                            
                            @Override
                            public void onGetMoviesInfoDone() 
                            {
                                pagedMovieSet.setPage(result.getPage());
                                pagedMovieSet.getMovies().addAll(result.getMovies());
                                notifyDataSetChanged();
                            }
                        });                      
                    }
                });
                
                return getProgressBar();
            }
            
            if (convertView == null || ProgressBar.class.isInstance(convertView)) 
            {
                convertView = inflater.inflate(R.layout.movies_listview_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.background = (ImageView)convertView.findViewById(R.id.background);
                viewHolder.title = (TextView)convertView.findViewById(R.id.title);
                viewHolder.actors = (TextView)convertView.findViewById(R.id.actors);
                //viewHolder.ratingBar = (RatingBar)convertView.findViewById(R.id.ratingBar);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            
            if (convertView.getLayoutParams().width != layoutParams.width && 
                    convertView.getLayoutParams().height != layoutParams.height)
            {
                convertView.setLayoutParams(layoutParams);
            }
            
            Movie movie = (Movie)getItem(position);
            if (movie != null)
            {         
                String url = movie.getBackdropUrl(BackdropSize.w1280);
                if (!Utils.isNullOrWhitespace(url)) {
                    imageLoader.LoadBitmapAsync(url, viewHolder.background, Math.min(displayMetrics.widthPixels, 1280) / 4, 
                            (int)getResources().getDimension(R.dimen.list_item_background_height));
                }
                //String year = movie.getReleaseDate().substring(0, 4);
                viewHolder.title.setText(movie.getTitle()); //+ " (" + year + ")");
                if (movie.getCasts() != null) {
                    viewHolder.actors.setText(movie.getCasts().getCastShortString());
                }
                //viewHolder.ratingBar.setRating(movie.getVoteAverage() * 0.5f);
            }
            return convertView;
        }
        
        private class ViewHolder {
            ImageView background;
            TextView title;
            TextView actors;
            //RatingBar ratingBar;
        }

        @Override
        public Object getItem(int position) 
        {
            if (pagedMovieSet == null) return null;
            return pagedMovieSet.getMovies().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        
        public int getNumColumns() {
            return numColumns;
        }
        
        public void setNumColumns(int numColumns) {
            this.numColumns = numColumns;
        }
        
        public void setItemSize(int width, int height) 
        {
            if (width == itemWidth && height == itemHeight)
                return;
            
            itemWidth = width;
            itemHeight = height;
            layoutParams = new AbsListView.LayoutParams(itemWidth, itemHeight);
            notifyDataSetChanged();
        }
        
        private ProgressBar getProgressBar()
        {
            ProgressBar progressBar = new ProgressBar(getSherlockActivity());
            progressBar.setIndeterminate(true);
            progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            return progressBar;
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
