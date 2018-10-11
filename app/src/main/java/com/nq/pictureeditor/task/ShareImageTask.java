package com.nq.pictureeditor.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.nq.pictureeditor.Utils;
import com.nq.pictureeditor.view.DrawInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class ShareImageTask extends AsyncTask<Bitmap, Integer, Uri> {
    private WeakReference<Context> mContext;
    private SaveListener listener;

    public ShareImageTask(Context context, SaveListener listener) {
        this.mContext = new WeakReference<>(context);
        this.listener = listener;
    }

    @Override
    protected Uri doInBackground(Bitmap... bitmaps) {
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
            return uri;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return imagePath;

        return null;
    }

    @Override
    protected void onPostExecute(Uri uri) {
        if (listener != null) listener.saved();

        Context context = mContext.get();
        //Toast.makeText(context, imagePath, Toast.LENGTH_LONG).show();
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        //if (subject != null && !"".equals(subject)) {
        //    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        //}
        //if (content != null && !"".equals(content)) {
        //    intent.putExtra(Intent.EXTRA_TEXT, content);
        //}

        // 设置弹出框标题
        //if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
        //    context.startActivity(Intent.createChooser(intent, dlgTitle));
        //} else { // 系统默认标题
        context.startActivity(intent);
        //}

        //if(draw != null) draw.shared();
    }
}

