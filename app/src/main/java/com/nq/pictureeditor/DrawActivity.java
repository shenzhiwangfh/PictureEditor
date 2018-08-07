package com.nq.pictureeditor;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.nq.pictureeditor.control.PenController;
import com.nq.pictureeditor.control.PenInterface;
import com.nq.pictureeditor.mode.PenMode;
import com.nq.pictureeditor.view.CornerLayout;
import com.nq.pictureeditor.view.DrawView;


public class DrawActivity extends AppCompatActivity implements DrawInterface, PenInterface, View.OnClickListener, CornerLayout.OnModeListener {

    private final static String TAG = "DrawActivity";

    private DrawView mDrawView;

    private ImageView mSave;
    private ImageView mShare;
    private ImageView mBack;
    private ImageView mForward;

    private CornerLayout mCornerLayout;

    private PenController penController;
    private PenMode mPenMode = new PenMode();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable(R.drawable.screenshot);
        Bitmap mBitmap = bitmapDrawable.getBitmap();

        mCornerLayout = findViewById(R.id.control);
        mCornerLayout.setOnModeListener(this);

        mSave = findViewById(R.id.save);
        mShare = findViewById(R.id.share);
        mBack = findViewById(R.id.back);
        mForward = findViewById(R.id.forward);

        mBack.setEnabled(false);
        mForward.setEnabled(false);
        mSave.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mForward.setOnClickListener(this);

        mDrawView = findViewById(R.id.main);
        mDrawView.initBitmap(mBitmap);
        mDrawView.setFinishDraw(this);

        penController = PenController.getInstance(this);
        penController.setPenInterface(this);
        penController.initPen();

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
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
            case R.id.share:
                break;
            case R.id.back:
                mDrawView.goBack();
                break;
            case R.id.forward:
                mDrawView.goForward();
                break;
        }
    }

    @Override
    public void onChange(int mode) {
        switch (mode) {
            case 0: //clip
                mDrawView.setMode(0x10);
                break;
            case 1: //pen
                mDrawView.setMode(0x20);
                break;
            case 2: //mosaic
                mDrawView.setMode(0x40);
                break;
            case 3: //text
                mDrawView.setMode(0x80);
                break;
        }
    }

    @Override
    public void setPaint(int color, int size, boolean eraser) {
        mDrawView.setPen(color, size, eraser);
    }
}
