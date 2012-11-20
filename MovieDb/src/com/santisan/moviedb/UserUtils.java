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
import android.widget.Toast;

import com.santisan.moviedb.MovieDbClient.MovieDbResultListener;
import com.santisan.moviedb.model.Account;
import com.santisan.moviedb.model.AuthToken;
import com.santisan.moviedb.model.Session;

public class UserUtils
{
    protected static final String TAG = "UserUtils";
    private static final String KEY_SESSION_ID = "keySessionId";
    private Context context;
    private Session session;
    private AuthToken authToken;
    private Account account;
    private SharedPreferences sharedPreferences;
    private MovieDbClient client = new MovieDbClient();
    
    public UserUtils(Context ctx) 
    {
        context = ctx;
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String sessionId = sharedPreferences.getString(KEY_SESSION_ID, null);
        if (!Utils.isNullOrWhitespace(sessionId)) {
            session = new Session(sessionId);
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
    
    public void requestAuthToken(final Activity activity) 
    {
        if (hasSession()) return;
        
        client.getAuthToken(new MovieDbResultListener<AuthToken>() {
            @Override
            public void onResult(AuthToken result) 
            {
                if (result == null || Utils.isNullOrWhitespace(result.getAuthCallback()) || !result.isSuccessful()) {
                    Toast.makeText(context, R.string.getAuthTokenError, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Log.d(TAG, "RequestToken: " + result.getRequestToken());
                authToken = result;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authToken.getAuthCallback()));
                activity.startActivity(intent);
            }
        });
    }
    
    public void getNewSession() 
    {
        if (authToken == null) return;        
        if (hasSession()) 
        {
            if (hasAccount()) return;
            else requestAccountData();
        }
        
        client.getSession(authToken.getRequestToken(), new MovieDbResultListener<Session>() {            
            @Override
            public void onResult(Session result) 
            {
                if (result == null || Utils.isNullOrWhitespace(result.getSessionId()) || !result.isSuccessful()) {
                    Toast.makeText(context, R.string.getAuthTokenError, Toast.LENGTH_SHORT).show();
                    return;
                }                
                Log.d(TAG, "SessionId: " + result.getSessionId());
                session = result;                
                saveSessionId(getSessionId());
                
                requestAccountData();
            }
        });
    }
    
    public void requestAccountData()
    {
        client.getAccount(getSessionId(), new MovieDbResultListener<Account>() {                    
            @Override
            public void onResult(Account result) 
            {
                if (result == null) {
                    Toast.makeText(context, R.string.getAuthTokenError, Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "AccountId: " + result.getId());
                account = result;
            }
        });
    }
    
    private void saveSessionId(String id) {
        sharedPreferences.edit().putString(KEY_SESSION_ID, id);
    }
}