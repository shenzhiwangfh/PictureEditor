package com.nq.pictureeditor;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;

public class DrawActivity extends AppCompatActivity implements DrawView.DrawInterface, View.OnClickListener {

    private DrawView mDrawView;
    private ImageView mSave;
    private ImageView mBack;
    private ImageView mForward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable(R.drawable.screenshot);
        Bitmap mBitmap = bitmapDrawable.getBitmap();

        mDrawView = findViewById(R.id.picture);
        mDrawView.init(mBitmap);
        mDrawView.setFinishDraw(this);

        mSave = findViewById(R.id.save);
        mBack = findViewById(R.id.back);
        mForward = findViewById(R.id.forward);

        mBack.setEnabled(false);
        mForward.setEnabled(false);

        mSave.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mForward.setOnClickListener(this);

        requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void finishAction(boolean back, boolean forward) {
        mBack.setEnabled(back);
        mForward.setEnabled(forward);
    }

    @Override
    public void save(Bitmap newBitmap) {
        new SaveImageTask(this).execute(newBitmap);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.save:
                mDrawView.goSave();
                break;
            case R.id.back:
                mDrawView.goBack();
                break;
            case R.id.forward:
                mDrawView.goForward();
                break;
        }
    }
}
