/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb.ui;

import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
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
import com.santisan.moviedb.model.Casts;
import com.santisan.moviedb.model.Image;
import com.santisan.moviedb.model.Movie;
import com.santisan.moviedb.model.Movie.ImageSize;
import com.santisan.moviedb.model.PostResponse;
import com.santisan.moviedb.model.WatchlistMovie;

public class MovieDetailFragment extends SherlockFragment
{    
    private static final String MOVIE_ID = "movieId";
    public static final String TAG = "MovieDetailFragment";
    private static final String YOUTUBE_URL = "http://m.youtube.com/watch?v="; //"http://youtu.be/";
    private static final String POSITION = "position";
    private static final String MOVIE_LIST_TYPE = "movieListType";
    private static final String MOVIE = "movie";
    
    private static final int NUM_IMAGES_GALLERY = 4;
    
    private TextView titleTextView;
    private ImageView posterImageView;
    private TextView overviewTextView;
    private TextView runtimeTextView;
    private TextView votesTextView;
    private TextView castTextView;
    //private RatingBar ratingBar;
    private Button trailerButton;
    private Button watchlistButton;
    private LinearLayout galleryLayout;
    
    private int movieId = 0;
    private int position;
    private MovieListType movieListType;
    private Movie movie = null;
    private BitmapLoader imageLoader;
    private MovieDbClient client;

    private DisplayMetrics displayMetrics;
    private WindowManager windowManager;      
    
    public static MovieDetailFragment newInstance(int movieId, int position, MovieListType type)
    {
        MovieDetailFragment frag = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putInt(MOVIE_ID, movieId);
        args.putInt(POSITION, position);
        args.putString(MOVIE_LIST_TYPE, type.name());
        frag.setArguments(args);
        return frag;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        imageLoader = new BitmapLoaderAsync(false);       
        client = new MovieDbClient(getSherlockActivity());        
        
        displayMetrics = new DisplayMetrics();         
        windowManager = (WindowManager)getSherlockActivity().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
        
        if (savedInstanceState != null) {
            movieId = savedInstanceState.getInt(MOVIE_ID);
            position = savedInstanceState.getInt(POSITION);
            movieListType = MovieListType.valueOf(savedInstanceState.getString(MOVIE_LIST_TYPE));
            movie = (Movie)savedInstanceState.getParcelable(MOVIE);
        }
        else if (getArguments() != null) {
            movieId = getArguments().getInt(MOVIE_ID, 0);
            position = getArguments().getInt(POSITION, 0);
            movieListType = MovieListType.valueOf(getArguments().getString(MOVIE_LIST_TYPE));
        }
        
        if (movieId == 0)
            Log.e(TAG, "Couldn't get the movieId from arguments nor from savedInstanceState");
        if (position == 0)
            Log.e(TAG, "Couldn't get the position from arguments nor from savedInstanceState");
            
        if (movieId != 0)
            setMovie(movieId, position);
        
        Log.d(TAG, "watchlist: ");
        SparseIntArray wl = MovieDbApp.getUserUtils().getWatchlist();
        for (int i=0; i < wl.size(); i++)
            Log.d(TAG, String.valueOf(wl.keyAt(i)) + ", ");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup arg1, Bundle arg2) 
    {       
        View v = inflater.inflate(R.layout.movie_detail, null);
        titleTextView = (TextView)v.findViewById(R.id.title);
        posterImageView = (ImageView)v.findViewById(R.id.poster);
        trailerButton = (Button)v.findViewById(R.id.trailerBtn);
        overviewTextView = (TextView)v.findViewById(R.id.overview);
        overviewTextView.setMovementMethod(new ScrollingMovementMethod());
        runtimeTextView = (TextView)v.findViewById(R.id.runtime);
        votesTextView = (TextView)v.findViewById(R.id.votes);
        castTextView = (TextView)v.findViewById(R.id.cast);
        //ratingBar = (RatingBar)v.findViewById(R.id.ratingBar);
        watchlistButton = (Button)v.findViewById(R.id.watchlistBtn);
        galleryLayout = (LinearLayout)v.findViewById(R.id.gallery);
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
        outState.putString(MOVIE_LIST_TYPE, movieListType.name());
        outState.putParcelable(MOVIE, movie);
    }   
    
    private void setMovie(final int movieId, final int position)
    {
        if (movie != null) {
            setMovieData();
            return;
        }
        
        client.getMovieFull(movieId, new MovieDbResultListener<Movie>() {
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
                Casts casts = MovieDbApp.getPagedMovieSet(movieListType).getMovies().get(position).getCasts();
                movie.setCasts(casts);
            
                setMovieData();
            }
        });
    }
    
    private void setMovieData()
    {
        imageLoader.LoadBitmapAsync(movie.getPosterUrl(getPosterSize()), posterImageView);
        String year = movie.getReleaseDate().substring(0, 4);
        titleTextView.setText(movie.getTitle() + " (" + year + ")");
        overviewTextView.setText(movie.getOverview());
        runtimeTextView.setText(getString(R.string.runtime, movie.getRuntime()));
        votesTextView.setText(getString(R.string.votes, movie.getVoteAverage(), movie.getVoteCount()));
        if (movie.getCasts() != null) {
            castTextView.setText(movie.getCasts().getCastShortString());
        }
        //ratingBar.setRating(movie.getVoteAverage() * 0.5f);
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
        
        posterImageView.setOnClickListener(new OnClickListener() {            
            @Override
            public void onClick(View v) 
            {
                Intent intent = new Intent(getSherlockActivity(), ImageActivity.class);
                intent.putExtra(ImageActivity.IMAGE_URL_EXTRA, movie.getPosterUrl(getPosterSizeBig()));
                getSherlockActivity().startActivity(intent);
            }
        });
        
        setGalleryData();
    }
    
    private void setGalleryData() 
    {
        int imageWidth = (int)(displayMetrics.widthPixels * 1.5f) / NUM_IMAGES_GALLERY;
        int imageHeight = (int)(imageWidth * 0.5625f);
        
        List<Image> images = movie.getImages().getAll();
        int numImages = Math.min(images.size(), NUM_IMAGES_GALLERY);
        Log.d(TAG, "num images gallery: " + numImages);
        for (int i=0; i < numImages; i++)
        {
            ImageView image = new ImageView(getSherlockActivity());
            MarginLayoutParams params = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.rightMargin = 8;
            image.setLayoutParams(new LinearLayout.LayoutParams(params));
            image.setScaleType(ScaleType.FIT_XY);
            imageLoader.LoadBitmapAsync(images.get(i).getUrl(getGallerySize()), image, imageWidth, imageHeight);
            galleryLayout.addView(image);
        }
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
        
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);        
        final boolean addToWatchlist = !movie.isInWatchlist();
        WatchlistMovie watchlistMovie = new WatchlistMovie(movie.getId(), addToWatchlist);
                
        client.addOrRemoveFromWatchlist(watchlistMovie, new MovieDbResultListener<PostResponse>() {            
            @Override
            public void onResult(PostResponse result) 
            {
                if (!result.getStatusMessage().toLowerCase(Locale.US).equals("success")) {
                    Toast.makeText(getSherlockActivity(), R.string.operation_failed, 
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    movie.setInWatchlist(addToWatchlist);                 
                    if (movie.isInWatchlist()) {
                        MovieDbApp.getUserUtils().addMovieToWatchlist(movie.getId());
                        watchlistButton.setText(getString(R.string.remove_from_watchlist));
                    }
                    else {
                        MovieDbApp.getUserUtils().removeMovieFromWatchlist(movie.getId());
                        watchlistButton.setText(getString(R.string.add_to_watchlist));
                    }
                }
                getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
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
    
    private ImageSize getPosterSize()
    {
        ImageSize posterSize = ImageSize.w342;
        if (displayMetrics.widthPixels <= 600)
            posterSize = ImageSize.w92;
        else if (displayMetrics.widthPixels >= 800)
            posterSize = ImageSize.w500;
            
        Log.d(TAG, "posterSize: " + posterSize.name());
        return posterSize;
    }
    
    private ImageSize getPosterSizeBig()
    {
        ImageSize posterSize = ImageSize.w342;
        if (displayMetrics.widthPixels >= 700)
            posterSize = ImageSize.w500;
            
        Log.d(TAG, "posterSizeBig: " + posterSize.name());
        return posterSize;
    }
    
    private ImageSize getGallerySize()
    {
        ImageSize size = ImageSize.w92;
        if (displayMetrics.widthPixels >= 600)
            size = ImageSize.w185;
        else if (displayMetrics.widthPixels >= 360)
            size = ImageSize.w154;
            
        Log.d(TAG, "gallerySize: " + size.name());
        return size;
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
