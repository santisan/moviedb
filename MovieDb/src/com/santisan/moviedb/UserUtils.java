/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.Toast;

import com.santisan.moviedb.MovieDbClient.MovieDbResultListener;
import com.santisan.moviedb.MovieDbClient.MovieListType;
import com.santisan.moviedb.model.Account;
import com.santisan.moviedb.model.AuthToken;
import com.santisan.moviedb.model.Movie;
import com.santisan.moviedb.model.PagedMovieSet;
import com.santisan.moviedb.model.Session;

public class UserUtils
{
    public interface UserLoginListener {
        void onUserLoggedIn();
        void onError();
    }
    
    protected static final String TAG = "UserUtils";
    private static final String KEY_SESSION_ID = "keySessionId";
    private Session session;
    private AuthToken authToken;
    private Account account;
    private SparseIntArray watchlist = new SparseIntArray();
    private SharedPreferences sharedPreferences;
    private MovieDbClient client = new MovieDbClient();
    
    public UserUtils(Context context)
    {
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String sessionId = sharedPreferences.getString(KEY_SESSION_ID, null);        
        if (!Utils.isNullOrWhitespace(sessionId)) {
            session = new Session(sessionId);
            Log.d(TAG, "loaded sessionId from sharedPreferences: " + sessionId);
        }
    }
    
    public Session getSession() {
        return session;
    }
    
    public boolean hasSession() {
        return session != null;
    }
    
    public String getSessionId() {
        return session.getSessionId();
    }
    
    public AuthToken getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }
    
    public Account getAccount() {
        return account;
    }
    
    public boolean hasAccount() {
        return account != null;
    }
    
    public boolean isLoggedIn() {
        return hasSession() && hasAccount();
    }
    
    public void requestAuthToken(final Activity activity) 
    {
        if (hasSession()) return;
        
        client.getAuthToken(new MovieDbResultListener<AuthToken>() {
            @Override
            public void onResult(AuthToken result) 
            {
                if (result == null || Utils.isNullOrWhitespace(result.getAuthCallback()) || !result.isSuccessful()) {
                    Log.e(TAG, "getAuthToken failed");
                    Toast.makeText(activity, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Log.d(TAG, "RequestToken: " + result.getRequestToken());
                authToken = result;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authToken.getAuthCallback()));
                activity.startActivity(intent);
            }
        });
    }
    
    public void getNewSession(final UserLoginListener listener) 
    {        
        if (hasSession())
        {
            if (!hasAccount()) {
                requestAccountData(listener);
            } 
            else {
                Log.d(TAG, "getNewSession: user was already logged in");
                listener.onUserLoggedIn();
            }
            return;
        }
     
        if (authToken == null) {
            listener.onError();
            return;
        }
        
        client.getSession(authToken.getRequestToken(), new MovieDbResultListener<Session>() {            
            @Override
            public void onResult(Session result) 
            {
                if (result == null || Utils.isNullOrWhitespace(result.getSessionId()) || !result.isSuccessful()) {
                    Log.e(TAG, "getSession failed");
                    listener.onError();
                    return;
                }                
                Log.d(TAG, "SessionId: " + result.getSessionId());
                session = result;
                saveSessionId(getSessionId());
                
                requestAccountData(listener);
            }
        });
    }
    
    public void requestAccountData(final UserLoginListener listener)
    {        
        client.getAccount(new MovieDbResultListener<Account>() {
            @Override
            public void onResult(Account result) 
            {
                if (result == null) {
                    Log.e(TAG, "getAccount result null");
                    listener.onError();
                    return;
                }
                
                Log.d(TAG, "AccountId: " + result.getId());
                account = result;
                getWatchlist(1, listener);                
            }
        });       
    }
    
    private void getWatchlist(int page, final UserLoginListener listener)
    {
        client.getMovieList(MovieListType.Watchlist, page, true, new MovieDbResultListener<PagedMovieSet>() {            
            @Override
            public void onResult(PagedMovieSet result)
            {
                if (result == null || result.getMovies() == null) {
                    Log.e(TAG, "getWatchlist failed");
                    listener.onUserLoggedIn();
                    return;
                }
                
                for (Movie movie : result.getMovies())
                    watchlist.put(movie.getId(), movie.getId());
                
                if (result.getPage() < result.getTotalPages())
                    getWatchlist(result.getPage() + 1, listener);                
                else 
                    listener.onUserLoggedIn();
            }
        });
    }   
    
    public SparseIntArray getWatchlist() {
        return watchlist;
    }
    
    public boolean isMovieInWatchlist(int movieId) {
        return watchlist.get(movieId, -1) > 0;
    }
    
    public void addMovieToWatchlist(int movieId) {
        watchlist.put(movieId, movieId);
    }
    
    public void removeMovieFromWatchlist(int movieId) {
        watchlist.delete(movieId);
    }
    
    private void saveSessionId(String id) {
        Log.d(TAG, "saving sessionId to shared preferences: " + id);
        SharedPreferences.Editor editor = sharedPreferences.edit().putString(KEY_SESSION_ID, id);
        Utils.commitSharedPreferences(editor);
    }
    
    public void logOut() 
    {
        if (!isLoggedIn()) return;
        
        SharedPreferences.Editor editor = sharedPreferences.edit().remove(KEY_SESSION_ID);
        Utils.commitSharedPreferences(editor);        
        authToken = null;
        session = null;
        account = null;
        Log.d(TAG, "logged out");
    }
}