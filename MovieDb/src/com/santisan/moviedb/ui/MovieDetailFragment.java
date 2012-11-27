/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.santisan.moviedb.BitmapLoader;
import com.santisan.moviedb.BitmapLoaderAsync;
import com.santisan.moviedb.MovieDbApp;
import com.santisan.moviedb.MovieDbClient;
import com.santisan.moviedb.MovieDbClient.MovieDbResultListener;
import com.santisan.moviedb.R;
import com.santisan.moviedb.Utils;
import com.santisan.moviedb.model.Casts;
import com.santisan.moviedb.model.Movie;
import com.santisan.moviedb.model.Movie.PosterSize;
import com.santisan.moviedb.model.PostResponse;
import com.santisan.moviedb.model.WatchlistMovie;

public class MovieDetailFragment extends SherlockFragment
{    
    private static final String MOVIE_ID = "movieId";
    public static final String TAG = "MovieDetailFragment";
    private static final String YOUTUBE_URL = "http://m.youtube.com/watch?v="; //"http://youtu.be/";
    private static final String POSITION = "position";
    
    private TextView titleTextView;
    private ImageView posterImageView;
    private TextView overviewTextView;
    private TextView runtimeTextView;
    private TextView votesTextView;
    private RatingBar ratingBar;
    private Button trailerButton;
    private Button watchlistButton;
    
    private int movieId = 0;
    private int position;
    private Movie movie;    
    private BitmapLoader imageLoader;

    private DisplayMetrics displayMetrics;
    private WindowManager windowManager;
    
    
    public static MovieDetailFragment newInstance(int movieId, int position)
    {
        MovieDetailFragment frag = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putInt(MOVIE_ID, movieId);
        args.putInt(POSITION, position);
        frag.setArguments(args);
        return frag;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        imageLoader = new BitmapLoaderAsync();       
        
        displayMetrics = new DisplayMetrics();         
        windowManager = (WindowManager)getSherlockActivity().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
        
        if (getArguments() != null) {
            movieId = getArguments().getInt(MOVIE_ID, 0);
            position = getArguments().getInt(POSITION, 0);
        }
        if (movieId == 0 && savedInstanceState != null) {
            movieId = savedInstanceState.getInt(MOVIE_ID);
            position = savedInstanceState.getInt(POSITION);
        }
        if (movieId == 0)
            Log.e(TAG, "Couldn't get the movieId from arguments nor from savedInstanceState");
        if (position == 0)
            Log.e(TAG, "Couldn't get the position from arguments nor from savedInstanceState");
            
        if (movieId != 0)
            setMovie(movieId, position);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup arg1, Bundle arg2) 
    {       
        View v = inflater.inflate(R.layout.movie_detail, null);
        titleTextView = (TextView)v.findViewById(R.id.title);
        posterImageView = (ImageView)v.findViewById(R.id.poster);
        trailerButton = (Button)v.findViewById(R.id.trailerBtn);
        overviewTextView = (TextView)v.findViewById(R.id.overview);
        runtimeTextView = (TextView)v.findViewById(R.id.runtime);
        votesTextView = (TextView)v.findViewById(R.id.votes);
        ratingBar = (RatingBar)v.findViewById(R.id.ratingBar);
        watchlistButton = (Button)v.findViewById(R.id.watchlistBtn);
        return v;
    }
    
    @Override
    public void onConfigurationChanged(Configuration arg0) {
        super.onConfigurationChanged(arg0);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MOVIE_ID, movieId);
        outState.putInt(POSITION, position);
    }
    
    private void setMovie(final int movieId, final int position)
    {     
        MovieDbClient client = new MovieDbClient();
        client.getMovieWithTrailers(movieId, new MovieDbResultListener<Movie>() {
            @Override
            public void onResult(Movie result)
            {            
                if (result == null) {
                    Log.e(TAG, "movie null for id " + movieId);
                    return;
                }
                if (!MovieDetailFragment.this.isAdded()) {
                    Log.e(TAG, "fragment is not added");
                    return;
                }
                
                movie = result;
                movie.setInWatchlist(MovieDbApp.getUserUtils().isMovieInWatchlist(movie.getId()));
                Casts casts = MovieDbApp.getPagedMovieSet().getMovies().get(position).getCasts();
                movie.setCasts(casts);
            
                imageLoader.LoadBitmapAsync(movie.getPosterUrl(getPosterSize()), posterImageView);                
                titleTextView.setText(movie.getTitle());
                overviewTextView.setText(movie.getOverview());
                runtimeTextView.setText(getString(R.string.runtime, movie.getRuntime()));
                votesTextView.setText(getString(R.string.votes, movie.getVoteCount()));
                ratingBar.setRating(movie.getVoteAverage() * 0.5f);                
                trailerButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        watchTrailer();
                    }
                });
                watchlistButton.setText(movie.isInWatchlist() ? getString(R.string.remove_from_watchlist) : 
                    getString(R.string.add_to_watchlist));
                watchlistButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addOrRemoveFromWatchlist();
                    }                   
                });          
                getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
            }
        });
    }
    
    private void addOrRemoveFromWatchlist() 
    {
        if (!MovieDbApp.getUserUtils().hasSession()) 
        {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(getSherlockActivity());
            dialog.setTitle(R.string.add_to_watchlist).
            setMessage(R.string.login_dialog_message).
            setPositiveButton(R.string.log_in, new DialogInterface.OnClickListener() {                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MovieDbApp.getUserUtils().getAuthToken(); //TODO: add call to getSession when back to the activity
                }
            }).
            setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
            return;
        }
        
        MovieDbClient client = new MovieDbClient();
        //TODO: check if the movie is in watchlist
        final boolean addToWatchlist = !movie.isInWatchlist();
        WatchlistMovie watchlistMovie = new WatchlistMovie(movie.getId(), addToWatchlist);
        client.addOrRemoveFromWatchlist(watchlistMovie, new MovieDbResultListener<PostResponse>() {            
            @Override
            public void onResult(PostResponse result) 
            {
                if (!result.getStatusMessage().toLowerCase().equals("success")) {
                    Toast.makeText(getSherlockActivity(), R.string.operation_failed, 
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    movie.setInWatchlist(addToWatchlist);
                    watchlistButton.setText(movie.isInWatchlist() ? getString(R.string.remove_from_watchlist) : 
                        getString(R.string.add_to_watchlist));
                }
            }
        });
    }
    
    private void watchTrailer()
    {
        if (Utils.isNullOrWhitespace(movie.getYoutubeTrailer())) 
        {
            Toast.makeText(getSherlockActivity(), R.string.trailer_unavailable, 
                    Toast.LENGTH_SHORT).show();
        }
        else {
            String url = YOUTUBE_URL + movie.getYoutubeTrailer() + "&hd=1";
            Log.d(TAG, "trailer URL: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }
    
    private PosterSize getPosterSize()
    {
        PosterSize posterSize = PosterSize.w342;
        if (displayMetrics.widthPixels <= 600)
            posterSize = PosterSize.w185;
        else if (displayMetrics.widthPixels >= 800)
            posterSize = PosterSize.w500;
            
        Log.d(TAG, "posterSize: " + posterSize.name());
        return posterSize;
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
    
    public int getPosition() {
        return position;
    }
}
