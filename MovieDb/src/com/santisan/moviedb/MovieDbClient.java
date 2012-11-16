/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.santisan.moviedb.model.Config;
import com.santisan.moviedb.model.Movie;
import com.santisan.moviedb.model.PagedMovieSet;

public class MovieDbClient
{
    private static final String API_KEY = "a259dab24cf7bc8f209fb45128770016";
    private static final String BASE_URL = "http://api.themoviedb.org/3";
    private static final String TAG = "MovieDbClient";
    
    private Gson gson = new Gson();
    
    public enum HttpMethod { GET, POST }
    
    public interface MovieDbResultListener<T> {
        void onResult(T result);
    }  
    
    public void getNowPlaying(final MovieDbResultListener<PagedMovieSet> listener) {
        getNowPlaying(1, listener);
    }
    
    public void getNowPlaying(final int page, final MovieDbResultListener<PagedMovieSet> listener)
    {
        final String url = "/movie/now_playing";
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValue("page", String.valueOf(page)));
        
        new AsyncTask<Void, Void, PagedMovieSet>() {
            @Override
            protected PagedMovieSet doInBackground(Void... taskParams) {
                return makeRequest(url, HttpMethod.GET, PagedMovieSet.class, params);
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
    
    public void getMovie(final int id, final MovieDbResultListener<Movie> listener)
    {
        final String url = "/movie/" + String.valueOf(id);
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValue("append_to_response", "trailers"));
        
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
    
    private <T> T makeRequest(String url, HttpMethod httpMethod, Class<T> classOfT) {
        return makeRequest(url, httpMethod, classOfT, new ArrayList<NameValuePair>());
    }
    
    private <T> T makeRequest(String url, HttpMethod httpMethod, Class<T> classOfT, List<NameValuePair> params)
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
        
        AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        HttpUriRequest request = null;
        String acceptType = "application/json";
        String contentType = acceptType + "; charset=" + HTTP.UTF_8;
        T result = null;
        try {
            if (httpMethod == HttpMethod.GET) {
                request = new HttpGet(url);
                //contentType = acceptType + "; charset=" + HTTP.UTF_8;
            }
            else if (httpMethod == HttpMethod.POST) {
                //HttpPost post = new HttpPost(url);
                //UrlEncodedFormEntity requestEntity = new UrlEncodedFormEntity(formParams, HTTP.UTF_8);
                //post.setEntity(requestEntity);
                request = new HttpPost(url); //post;
                //contentType = "application/x-www-form-urlencoded; charset=" + HTTP.UTF_8;
            }
            
            request.addHeader("Accept", acceptType);
            request.setHeader("Content-Type", contentType);
        
            HttpResponse getResponse = client.execute(request);            
            final int statusCode = getResponse.getStatusLine().getStatusCode();
            
            if ((statusCode / 100) != 2) {
                Log.e(TAG, "Error " + statusCode + " for URL " + url);
            }
            else {
                HttpEntity getResponseEntity = getResponse.getEntity();                
                if (getResponseEntity != null) {
                    String json = EntityUtils.toString(getResponseEntity, HTTP.UTF_8);
                    Log.d(TAG, "response: " + json);                    
                    result = gson.fromJson(json, classOfT);
                }
                else {
                    Log.e(TAG, "Error, respuesta null");
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
            client.close();
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
