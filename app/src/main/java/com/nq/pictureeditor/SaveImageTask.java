/* Copyright (C) 2018 Tcl Corporation Limited */
package com.nq.pictureeditor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * Created by linzhen.yang on 10/11/16.
 */
public class SaveImageTask extends AsyncTask<Bitmap, Integer, String> {
    private WeakReference<Context> mContext;

    public SaveImageTask(Context context) {
        this.mContext = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(Bitmap... bitmaps) {
        Context context = mContext.get();

        String imagePath = Utils.createScreenName();
        File file = new File(imagePath);
        Bitmap bitmap = bitmaps[0];
        try {
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            String absPath = file.getAbsolutePath();
            String fileName = file.getName();
            ContentValues values = new ContentValues();
            ContentResolver resolver = context.getContentResolver();
            values.put(MediaStore.Images.ImageColumns.DATA, absPath);
            values.put(MediaStore.Images.ImageColumns.TITLE, fileName);
            values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.ImageColumns.WIDTH, bitmap.getWidth());
            values.put(MediaStore.Images.ImageColumns.HEIGHT, bitmap.getHeight());
            values.put(MediaStore.Images.ImageColumns.SIZE, file.length());
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            //return uri;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imagePath;
    }

    @Override
    protected void onPostExecute(String imagePath) {
        Context context = mContext.get();
        Toast.makeText(context, imagePath, Toast.LENGTH_LONG).show();
    }
}
