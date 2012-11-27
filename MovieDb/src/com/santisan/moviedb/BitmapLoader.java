/**
 * Copyright (C) 2012 Santiago Sánchez - All Rights Reserved.
 */
package com.santisan.moviedb;

import java.io.File;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.santisan.moviedb.ImageCache.ImageCacheParams;

public abstract class BitmapLoader
{
    public interface ILoadBitmapListener {
        void OnImageLoaded(Bitmap imageBitmap);
    }
    
    private static final String TAG = "BitmapLoader";
    protected static Resources resources;
    protected static Bitmap placeHolderBitmap = null;
    protected static ImageCache imageCache = null;
    protected static ImageCacheParams imageCacheParams;
    protected int imageWidth = 100;
    protected int imageHeight = 100;
    
    // Cache operations:
    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;
    
    protected BitmapLoader() {
        this(false);
    }
    
    protected BitmapLoader(boolean usePlaceholderBitmap) 
    {        
        if (usePlaceholderBitmap  && placeHolderBitmap == null) 
        {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    placeHolderBitmap = BitmapFactory.decodeResource(resources, R.drawable.empty_photo);
                    return null;
                }
            }.execute();
        }
    }
    
    public static void SetResources(Resources res) {
        resources = res;
    }
    
    public Bitmap GetPlaceHolderBitmap() {
        return placeHolderBitmap;
    }
    
    public void SetPlaceHolderBitmap(Bitmap bitmap) {
        placeHolderBitmap = bitmap;
    }
    
    @TargetApi(9)
    public void SetPlaceHolderBitmap(String imagePath, int width, int height) 
    {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.w(TAG, "SetPlaceHolderBitmap: imagePath is null or empty");
            return;
        }
        
        LoadBitmapAsync(imagePath, width, height, new ILoadBitmapListener() 
        {            
            public void OnImageLoaded(Bitmap imageBitmap) {                
                SetPlaceHolderBitmap(imageBitmap);
            }
        });
    }
    
    public Bitmap LoadBitmap(String imagePath)
    {       
        return BitmapFactory.decodeFile(imagePath);
    }
    
    public Bitmap LoadBitmap(String imagePath, int width, int height)
    {
        return DecodeSampledBitmap(imagePath, width, height);
    }
    
    public void LoadBitmap(String imagePath, ImageView imageView)
    {
        LayoutParams params = imageView.getLayoutParams();
        Bitmap bitmap = DecodeSampledBitmap(imagePath, params.width, params.height);
        imageView.setImageBitmap(bitmap);
    }
    
    public abstract void LoadBitmapAsync(String imagePath, ImageView imageView);
    
    public abstract void LoadBitmapAsync(String imagePath, ImageView imageView, ILoadBitmapListener listener);
    
    public abstract void LoadBitmapAsync(String imagePath, int width, int height, ILoadBitmapListener listener);
    
    public abstract void LoadBitmapAsync(String imagePath, ImageView imageView, int width, int height);
    
    public abstract void LoadBitmapAsync(String imagePath, ImageView imageView, int width, int height,
            ILoadBitmapListener listener);
    
    public abstract void CancelTask(ImageView imageView);
    
    public abstract void CancelTasks();
    
    public abstract void SetPauseTasks(boolean pause);    
    
    protected static Bitmap DecodeSampledBitmap(String pathName, int reqWidth, int reqHeight) 
    {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        options.inSampleSize = CalculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Log.d(TAG, "DecodeSampledBitmap: sampleSize=" + options.inSampleSize);
        Log.d(TAG, "DecodeSampledBitmap: input w=" + options.outWidth + ", h=" + options.outHeight);
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);
        Log.d(TAG, "DecodeSampledBitmap: output w=" + options.outWidth + ", h=" + options.outHeight);
        return bitmap;
    }
    
    protected static Bitmap DecodeSampledBitmap(byte[] bytes, int reqWidth, int reqHeight) 
    {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        options.inSampleSize = CalculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Log.d(TAG, "DecodeSampledBitmap: sampleSize=" + options.inSampleSize);
        Log.d(TAG, "DecodeSampledBitmap: input w=" + options.outWidth + ", h=" + options.outHeight);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        Log.d(TAG, "DecodeSampledBitmap: output w=" + options.outWidth + ", h=" + options.outHeight);
        return bitmap;
    }
    
    protected static int CalculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) 
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }   
    
    public static String GetHashKey(String imagePath, int width, int height) 
    {
        String key = imagePath + File.separator + Integer.toString(width) + 
                File.separator + Integer.toString(height);
        return ImageCache.hashKeyForDisk(key);
    }
    
    protected static class CacheAsyncTask extends AsyncTask<Integer, Void, Void> 
    {
        protected Void doInBackground(Integer... params) {
            switch (params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected static void initDiskCacheInternal() {
        if (imageCache != null)
            imageCache.initDiskCache();
    }

    protected static void clearCacheInternal() {
        if (imageCache != null)
            imageCache.clearCache();
    }

    protected static void flushCacheInternal() {
        if (imageCache != null)
            imageCache.flush();
    }

    protected static void closeCacheInternal() {
        if (imageCache != null)
            imageCache.close(); //closes disk cache only
    }
    
    public static void AddImageCache(ImageCacheParams cacheParams) 
    {
        if (imageCache == null) {
            imageCacheParams = cacheParams;
            imageCache = new ImageCache(imageCacheParams);
            InitDiskCache();
        }
    }
    
    public static void InitDiskCache() {
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    public static void ClearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }
    
    public static void ClearMemCache() {
        imageCache.clearMemCache();
    }

    public static void FlushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public static void CloseDiskCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }    
}
