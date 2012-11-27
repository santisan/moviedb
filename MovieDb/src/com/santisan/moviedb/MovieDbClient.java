/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.santisan.moviedb.model.Account;
import com.santisan.moviedb.model.AuthToken;
import com.santisan.moviedb.model.Config;
import com.santisan.moviedb.model.Movie;
import com.santisan.moviedb.model.PagedMovieSet;
import com.santisan.moviedb.model.PostResponse;
import com.santisan.moviedb.model.Session;
import com.santisan.moviedb.model.WatchlistMovie;

public class MovieDbClient
{
    private static final String API_KEY = "a259dab24cf7bc8f209fb45128770016";
    private static final String BASE_URL = "http://api.themoviedb.org/3";
    private static final String TAG = "MovieDbClient";   
    
    public enum MovieListType { Upcoming, NowPlaying, Popular, TopRated, Watchlist }
    
    public enum HttpMethod { GET, POST }
    
    public interface MovieDbResultListener<T> {
        //TODO: change to onSuccess and add onFailure method to improve error handling
        void onResult(T result);
    }  
    
    private Gson gson = new Gson();
    private String url;
    private static Map<MovieListType, String> movieListUrls = new HashMap<MovieListType, String>();    
    {
        movieListUrls.put(MovieListType.NowPlaying, "/movie/now_playing");
        movieListUrls.put(MovieListType.Popular, "/movie/popular");
        movieListUrls.put(MovieListType.TopRated, "/movie/top_rated");
        movieListUrls.put(MovieListType.Upcoming, "/movie/upcoming");
        movieListUrls.put(MovieListType.Watchlist, "/account/{id}/movie_watchlist");
    }
    
    public void getMovieList(MovieListType type, boolean requireSession, MovieDbResultListener<PagedMovieSet> listener) {
        getMovieList(movieListUrls.get(type), 1, requireSession, listener);
    }
    
    public void getMovieList(MovieListType type, int page, boolean requireSession, 
            MovieDbResultListener<PagedMovieSet> listener) 
    {
        getMovieList(movieListUrls.get(type), page, requireSession, listener);
    }
    
    private void getMovieList(final String url, final int page, final boolean requireSession, 
            final MovieDbResultListener<PagedMovieSet> listener)
    {
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValue("page", String.valueOf(page)));
        
        if (requireSession) 
        {
            UserUtils userUtils = MovieDbApp.getUserUtils();
            if (!userUtils.hasSession() || !userUtils.hasAccount()) {
                Log.e(TAG, "There is no valid session!");
                listener.onResult(null);
                return;
            }
            
            params.add(new NameValue("session_id", userUtils.getSessionId()));
            this.url = url.replace("{id}", String.valueOf(userUtils.getAccount().getId()));
        }
        else {
            this.url = url;
        }
        
        new AsyncTask<Void, Void, PagedMovieSet>() {
            @Override
            protected PagedMovieSet doInBackground(Void... taskParams) {
                return makeRequest(MovieDbClient.this.url, HttpMethod.GET, PagedMovieSet.class, params);
            }
            
            @Override
            protected void onPostExecute(PagedMovieSet result) {
                listener.onResult(result);
            }           
        }.execute();
    }
    
    public void getConfig(final MovieDbResultListener<Config> listener)
    {
        final String url = "/configuration";
        new AsyncTask<Void, Void, Config>() {
            @Override
            protected Config doInBackground(Void... params) {
                return makeRequest(url, HttpMethod.GET, Config.class);
            }
            
            @Override
            protected void onPostExecute(Config result) {
                listener.onResult(result);
            }
        }.execute();
    }
    
    public void getMovie(int id, MovieDbResultListener<Movie> listener) {
        getMovie(id, null, listener);
    }
    
    public void getMovieWithCasts(int id, MovieDbResultListener<Movie> listener) {
        getMovie(id, new String[] { "casts" }, listener);
    }
    
    public void getMovieWithTrailers(int id, MovieDbResultListener<Movie> listener) {
        getMovie(id, new String[] { "trailers" }, listener);
    }
    
    private void getMovie(final int id, String[] extraQueries, final MovieDbResultListener<Movie> listener)
    {
        final String url = "/movie/" + String.valueOf(id);
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
                
        if (extraQueries != null && extraQueries.length > 0)
        {
            int numQueries = extraQueries.length;
            String append = "";
            for (int i=0; i < numQueries - 1; i++) {
                append += extraQueries[i].trim() + ",";
            }
            append += extraQueries[numQueries - 1];
            
            params.add(new NameValue("append_to_response", append));
        }
        
        new AsyncTask<Void, Void, Movie>() {
            @Override
            protected Movie doInBackground(Void... taskParams) {
                return makeRequest(url, HttpMethod.GET, Movie.class, params);
            }
            
            @Override
            protected void onPostExecute(Movie result) {
                listener.onResult(result);
            }
        }.execute();
    }
    
    public void getAuthToken(final MovieDbResultListener<AuthToken> listener)
    {
        final String url = "/authentication/token/new";        
        new AsyncTask<Void, Void, AuthToken>() 
        {            
            @Override
            protected AuthToken doInBackground(Void... params) {                
                return makeRequest(url, HttpMethod.GET, AuthToken.class);
            }
            
            @Override
            protected void onPostExecute(AuthToken result) 
            {
                if (Utils.isNullOrWhitespace(result.getAuthCallback())) {
                    Log.e(TAG, "getAuthToken failed to receive Authorization-Callback");
                    result.setAuthCallback(null);
                }
                listener.onResult(result);
            }
        }.execute();
    }
    
    public void getSession(final String requestToken, final MovieDbResultListener<Session> listener)
    {
        final String url = "/authentication/session/new";
        final List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
        requestParams.add(new NameValue("request_token", requestToken));
        
        new AsyncTask<Void, Void, Session>() {
            @Override
            protected Session doInBackground(Void... params) {
                return makeRequest(url, HttpMethod.GET, Session.class, requestParams);
            }
            
            @Override
            protected void onPostExecute(Session result) {
                listener.onResult(result);
            }
        }.execute();
    }
    
    public void getAccount(final MovieDbResultListener<Account> listener)
    {
        final String url = "/account";        
        
        UserUtils userUtils = MovieDbApp.getUserUtils();
        if (!userUtils.hasSession()) {
            Log.e(TAG, "There is no valid session!");
            listener.onResult(null);
            return;
        }
        if (userUtils.hasAccount()) {
            Log.e(TAG, "There is already an account! Log out first!");
            listener.onResult(null);
            return;
        }
        
        final List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
        requestParams.add(new NameValue("session_id", userUtils.getSessionId()));
        
        new AsyncTask<Void, Void, Account>() {
            @Override
            protected Account doInBackground(Void... params) {
                return makeRequest(url, HttpMethod.GET, Account.class, requestParams);
            }
            
            @Override
            protected void onPostExecute(Account result) {
                listener.onResult(result);
            }
        }.execute();
    }
    
    public void addOrRemoveFromWatchlist(final WatchlistMovie movie, final MovieDbResultListener<PostResponse> listener)
    {
        final String url = "/account/{id}/movie_watchlist";             
        
        UserUtils userUtils = MovieDbApp.getUserUtils();
        if (!userUtils.isLoggedIn()) {
            Log.e(TAG, "There is no valid session!");
            listener.onResult(null);
            return;
        }
        
        final List<NameValuePair> requestParams = new ArrayList<NameValuePair>(); 
        requestParams.add(new NameValue("session_id", userUtils.getSessionId()));
        this.url = url.replace("{id}", String.valueOf(userUtils.getAccount().getId()));
        
        final List<Object> jsonParams = new ArrayList<Object>();
        if (movie != null) {
            jsonParams.add(movie);
        }
        
        new AsyncTask<Void, Void, PostResponse>() {
            @Override
            protected PostResponse doInBackground(Void... params) {
                return makeRequest(MovieDbClient.this.url, HttpMethod.POST, PostResponse.class, requestParams, jsonParams);
            }
            
            @Override
            protected void onPostExecute(PostResponse result) {
                listener.onResult(result);
            }
        }.execute();
    }
    
    /**
     **********   HTTP request methods   **********   
     */
    
    private <T> T makeRequest(String url, HttpMethod httpMethod, Class<T> classOfT) {
        return makeRequest(url, httpMethod, classOfT, new ArrayList<NameValuePair>());
    }
    
    private <T> T makeRequest(String url, HttpMethod httpMethod, Class<T> classOfT, List<NameValuePair> params) {
        return makeRequest(url, httpMethod, classOfT, params, null);
    }
    
    private <T> T makeRequest(String url, HttpMethod httpMethod, Class<T> classOfT, List<NameValuePair> params,
            List<Object> jsonParams)
    {
        params.add(new NameValue("api_key", API_KEY));
        
        int numParams = params.size();
        for (int i=0; i < numParams; i++) 
        {
            String separator = (i == 0) ? "?" : "&";
            NameValuePair param = params.get(i);
            url += separator + param.getName() + "=" + encodeUrl(param.getValue());
        }  
        
        url = BASE_URL + url;
        Log.d(TAG, "url: " + url);
                        
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("com.santisan.moviedb");
        String acceptType = "application/json";
        String contentType = "application/json"; //acceptType + "; charset=" + HTTP.UTF_8;
        T result = null;        
        HttpUriRequest request = null;
        
        try {
            if (httpMethod == HttpMethod.GET) {
                request = new HttpGet(url);
            }
            else if (httpMethod == HttpMethod.POST) 
            {                
                HttpPost post = new HttpPost(url);
                if (jsonParams != null && jsonParams.size() > 0) 
                {
                    StringBuilder builder = new StringBuilder();
                    for (Object param : jsonParams) {
                        gson.toJson(param, builder);
                    }
                    String body = builder.toString();
                    StringEntity requestEntity = new StringEntity(body, HTTP.UTF_8);                
                    post.setEntity(requestEntity);
                    Log.d(TAG, "POST request body: " + body);
                }
                request = post;
            }
            
            request.addHeader("Accept", acceptType);
            request.setHeader("Content-Type", contentType);
                    
            HttpResponse response = httpClient.execute(request);            
            final int statusCode = response.getStatusLine().getStatusCode();
            
            if ((statusCode / 100) != 2) {
                Log.e(TAG, "Error " + statusCode + " for URL " + url);
            }
            else {
                HttpEntity getResponseEntity = response.getEntity();                
                if (getResponseEntity != null) 
                {               
                    String json = EntityUtils.toString(getResponseEntity, HTTP.UTF_8);
                    result = gson.fromJson(json, classOfT);
                    if (result != null) 
                    {            
                        if (AuthToken.class.isInstance(result)) 
                        {
                            String headerName = "Authentication-Callback";
                            if (response.containsHeader(headerName))
                                ((AuthToken)result).setAuthCallback(response.getFirstHeader(headerName).getValue());
                        } 
                        
                        Log.d(TAG, "response: " + json);
                        /*Log.d(TAG, "headers:");
                        for (Header h : response.getAllHeaders()) {
                            Log.d(TAG, h.getName() + ": " + h.getValue());
                        } */
                    }
                    else {
                        Log.e(TAG, "Error, unable to deserialize json response");
                    }
                }
                else {
                    Log.e(TAG, "Error, entity response null");
                }
            }
        } 
        catch(IOException e) 
        {
            if (request != null) {
                request.abort();
            }
            Log.e(TAG, "Error for URL " + url + " " + e);
            e.printStackTrace();
            return null;
        }
        finally {
            httpClient.close();
        }        
        return result;
    }
    
    public static String encodeUrl(String s) {
        String encoded = null;
        try {
            if (s == null) s = "";
            encoded = URLEncoder.encode(s, HTTP.UTF_8);
        }
        catch (UnsupportedEncodingException exc) {
            exc.printStackTrace();
            Log.e(TAG, exc.getMessage());
        }
        return encoded;
    }
    
    /*private <T> T makeRequest(String url, Class<T> cls, Map<String, Object> params)
    {        
        params.put("api_key", API_KEY);
        return restTemplate.getForObject(BASE_URL + url, cls, params);
    }
    
    private <T> T makeRequest(String url, Class<T> cls, Map<String, Object> params)
    {          
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
        HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

        params.put("api_key", API_KEY);
        ResponseEntity<T> responseEntity = restTemplate.exchange(BASE_URL + url, HttpMethod.GET, requestEntity, cls, params);
        return responseEntity.getBody();
    }*/
}
