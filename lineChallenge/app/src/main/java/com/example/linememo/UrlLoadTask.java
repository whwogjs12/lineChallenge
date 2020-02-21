package com.example.linememo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class UrlLoadTask extends AsyncTask<URL,Integer,Bitmap>
{
    private File cachePath;
    private Uri resultUri = null;

    public UrlLoadTask (File cachePath)
    {
        this.cachePath = cachePath;
    }


    @Override
    protected Bitmap doInBackground(URL... urls)
    {
        Bitmap externalLoadBitmap = null;
        try
        {
            if(isCancelled()) return null;
            URL url = urls[0];
            externalLoadBitmap = BitmapFactory.decodeStream(url.openStream());
            return externalLoadBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onProgressUpdate( Integer ... progress ){
        // do something
    }

    @SuppressLint("WrongThread")
    @Override
    protected void onPostExecute( Bitmap result ){
        if(isCancelled()) result = null;

    }

    public Uri getResultUri()
    {
        return resultUri;
    }

}
