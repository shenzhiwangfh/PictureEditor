package com.nq.pictureeditor.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class LoadImageTask extends AsyncTask<Uri, Integer, Bitmap> {
    private WeakReference<Context> mContext;
    private LoadListener listener;

    public LoadImageTask(Context context, LoadListener listener) {
        this.mContext = new WeakReference<>(context);
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(Uri... imageCaptureUri) {
        Context context = mContext.get();
        Uri uri = imageCaptureUri[0];

        Bitmap photoBmp = null;
        try {
            photoBmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return photoBmp;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (listener != null) listener.setBitmap(bitmap);
    }
}
