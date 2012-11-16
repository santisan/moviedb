/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

public class BitmapLoaderAsync extends BitmapLoader
{
    private static final String TAG = "BitmapLoaderAsync";
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int TASK_LIST_CAPACITY = 10;
    private boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    private List<WeakReference<BitmapWorkerTask>> tasks = new Vector<WeakReference<BitmapWorkerTask>>(TASK_LIST_CAPACITY);
    
    public BitmapLoaderAsync() {
        super();
    }
    
    @Override
    public void LoadBitmapAsync(String imagePath, ImageView imageView) {
        LoadBitmapAsync(imagePath, imageView, null);
    }
    
    @Override
    public void LoadBitmapAsync(String imagePath, ImageView imageView, ILoadBitmapListener listener)
    {
        if (!CancelPotentialWork(imagePath, imageView))
        {
            if (listener != null) 
            {
                if (imageView.getDrawable() instanceof BitmapDrawable) {
                    BitmapDrawable drawable = (BitmapDrawable)imageView.getDrawable();
                    listener.OnImageLoaded(drawable.getBitmap());
                }
                else listener.OnImageLoaded(null);
            }
            return;
        }
        
        this.imageWidth = imageView.getLayoutParams().width;
        this.imageHeight = imageView.getLayoutParams().height;
        
        if (imageCache != null) 
        {            
            String key = GetHashKey(imagePath, imageWidth, imageHeight);
            Bitmap bitmap = imageCache.getBitmapFromMemCache(key);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                if (listener != null)
                    listener.OnImageLoaded(bitmap);
                
                return;
            }
        } 
        
        final BitmapWorkerTask task = new BitmapWorkerTask(imageView, listener);
        AddTaskToList(task);
        final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, placeHolderBitmap, task);
        imageView.setImageDrawable(asyncDrawable);
        task.execute(imagePath);
    }
    
    @Override
    public void LoadBitmapAsync(String imagePath, int width, int height, ILoadBitmapListener listener)
    {
        if (imageCache != null) 
        {
            String key = GetHashKey(imagePath, width, height);
            Bitmap bitmap = imageCache.getBitmapFromMemCache(key);
            if (bitmap != null) {
                listener.OnImageLoaded(bitmap);
                return;
            }
        }
        
        this.imageWidth = width;
        this.imageHeight = height;        
        final BitmapWorkerTask task = new BitmapWorkerTask(listener);
        AddTaskToList(task);
        task.execute(imagePath);
    }
    
    private void AddTaskToList(BitmapWorkerTask task) 
    {
        tasks.add(new WeakReference<BitmapWorkerTask>(task));
        Log.d(TAG, "Task count = " + tasks.size());
    }
    
    private static boolean CancelPotentialWork(String imagePath, ImageView imageView) 
    {
        final BitmapWorkerTask bitmapWorkerTask = GetBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapPath = bitmapWorkerTask.imagePath;
            if (bitmapPath != imagePath) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                Log.d(TAG, "Work already in progress for image path " + imagePath);
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }
    
    @Override
    public void CancelTask(ImageView imageView) 
    {
        final BitmapWorkerTask bitmapWorkerTask = GetBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null)
            bitmapWorkerTask.cancel(true);
    }
    
    @Override
    public void CancelTasks()
    {        
        for (WeakReference<BitmapWorkerTask> taskRef : tasks) 
        {
            BitmapWorkerTask task = taskRef.get();
            if (task != null)
                task.cancel(true);
        }
        tasks.clear();
    }
    
    @Override
    public void SetPauseTasks(boolean pause) 
    {        
        synchronized (mPauseWorkLock) 
        {
            if (mPauseWork != pause) {
                mPauseWork = pause;
                if (!mPauseWork) {
                    mPauseWorkLock.notifyAll();
                    Log.d(TAG, "Tasks resumed");
                }
                else Log.d(TAG, "Tasks paused");
            }
        }
    }
    
    private static BitmapWorkerTask GetBitmapWorkerTask(ImageView imageView) 
    {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
         }
         return null;
    }
    
    private void HandleLoadedBitmap(BitmapWorkerTask task, Bitmap bitmap, 
            WeakReference<ILoadBitmapListener> listenerRef, WeakReference<ImageView> imageViewRef) 
    {    
        if (bitmap == null) Log.w(TAG, "HandleLoadedBitmap: bitmap is null");
        
        if (imageViewRef != null && imageViewRef.get() != null) 
        {
            ImageView imageView = imageViewRef.get();
            final BitmapWorkerTask bitmapWorkerTask = GetBitmapWorkerTask(imageView);
            if (bitmapWorkerTask == task)
                imageView.setImageBitmap(bitmap);
        }
        
        if (listenerRef != null && listenerRef.get() != null)
            listenerRef.get().OnImageLoaded(bitmap);
    }   
    
    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> 
    {        
        public String imagePath = "";
        private WeakReference<ImageView> imageViewRef = null;
        private WeakReference<ILoadBitmapListener> listenerRef = null;
        
        public BitmapWorkerTask(ImageView imageView, ILoadBitmapListener listener) {
            imageViewRef = new WeakReference<ImageView>(imageView);
            listenerRef = new WeakReference<BitmapLoader.ILoadBitmapListener>(listener);
        }
        
        @SuppressWarnings("unused")
        public BitmapWorkerTask(ImageView imageView) {
            imageViewRef = new WeakReference<ImageView>(imageView);
        }
        
        public BitmapWorkerTask(ILoadBitmapListener listener) {
            listenerRef = new WeakReference<BitmapLoader.ILoadBitmapListener>(listener);
        }

        @Override
        protected Bitmap doInBackground(String... params) 
        {
            imagePath = params[0];
            Bitmap bitmap = null;
            
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {                       
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }              

            if (imageCache != null && !isCancelled()) 
            {
                String key = GetHashKey(imagePath, imageWidth, imageHeight);
                bitmap = imageCache.getBitmapFromDiskCache(key);
                if (bitmap != null) {
                    //esta en cache de disco pero no en memoria, lo agrego
                    imageCache.addBitmapToMemCache(key, bitmap);
                    return bitmap;
                }
            }
            
            if (!isCancelled()) 
            {
                if (imagePath.toLowerCase().startsWith("http")) 
                {
                    ByteArrayOutputStream outStream = new ByteArrayOutputStream(); 
                    if (DownloadUrlToStream(imagePath, outStream))
                        bitmap = DecodeSampledBitmap(outStream.toByteArray(), imageWidth, imageHeight);
                }
                else {
                    bitmap = DecodeSampledBitmap(imagePath, imageWidth, imageHeight);
                }
                
                if (bitmap != null && imageCache != null)
                {
                    String key = GetHashKey(imagePath, imageWidth, imageHeight);
                    imageCache.addBitmapToCache(key, bitmap);
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) 
        {
            if (isCancelled()) {
                bitmap = null;                
            }
            HandleLoadedBitmap(this, bitmap, listenerRef, imageViewRef);
            ClearRefs();
            RemoveFromTaskList();
        }
        
        private void ClearRefs() {
            listenerRef.clear();
            listenerRef = null;
            imageViewRef.clear();
            imageViewRef = null;
        }
        
        private void RemoveFromTaskList() 
        {
            int count = tasks.size();
            for (int i=count-1; i >= 0; i--) {
                BitmapWorkerTask task = tasks.get(i).get();
                if (task == this) {
                    tasks.remove(i);
                    return;
                }
            }
        }
                
        @Override
        protected void onCancelled(Bitmap bitmap) 
        {
            //super.onCancelled(bitmap);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
            RemoveFromTaskList();
            Log.d(TAG, "Task cancelled: " + imagePath);
        }
    }
    
    private static class AsyncDrawable extends BitmapDrawable 
    {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) 
        {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
    
    public boolean DownloadUrlToStream(String urlString, OutputStream outputStream) 
    {
        DisableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (final IOException e) {
            Log.e(TAG, "Error in DownloadUrlToStream - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {}
        }
        return false;
    }

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void DisableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }
}
