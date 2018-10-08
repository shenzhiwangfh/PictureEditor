package com.nq.pictureeditor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nq.pictureeditor.control.ModeController;
import com.nq.pictureeditor.mode.ClipMode;
import com.nq.pictureeditor.mode.ColorPenMode;
import com.nq.pictureeditor.mode.EditMode;
import com.nq.pictureeditor.mode.MosaicsPenMode;
import com.nq.pictureeditor.mode.PenMode;
import com.nq.pictureeditor.mode.TextMode;
import com.nq.pictureeditor.text.TextActivity;
import com.nq.pictureeditor.view.ArcColorPicker;
import com.nq.pictureeditor.view.ArcSeekBar;
import com.nq.pictureeditor.view.CornerLayout;
import com.nq.pictureeditor.view.DrawView;
import com.nq.pictureeditor.view.MosaicsView;
import com.nq.pictureeditor.view.OnShowListener;
import com.nq.pictureeditor.view.Preview;
import com.nq.pictureeditor.view.ViewUtils;

import java.util.HashMap;

public class DrawActivity extends AppCompatActivity
        implements View.OnClickListener, OnShowListener {

    private final static String TAG = "DrawActivity";

    private DrawView mDrawView;
    private Preview mPreview;

    private ImageView mSave;
    private ImageView mShare;
    private ImageView mBack;
    private ImageView mForward;

    private CornerLayout mCornerLayout;
    private ArcColorPicker mPenColorPicker;
    private ArcSeekBar mPenSeekBar;
    private ArcSeekBar mMosaicsSeekBar;

    private ModeController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable(R.drawable.screenshot);
        Bitmap mBitmap = bitmapDrawable.getBitmap();
        mController = new ModeController(this, mBitmap);

        initActions();
        initControllors();

        mDrawView = findViewById(R.id.main);
        mDrawView.setController(mController);
        mPreview = findViewById(R.id.preview);

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

    private void initActions() {
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
    }

    private void initControllors() {
        mCornerLayout = findViewById(R.id.control);
        mPenColorPicker = findViewById(R.id.pen_color);
        mPenSeekBar = findViewById(R.id.pen_size);
        mMosaicsSeekBar = findViewById(R.id.mosaics_size);

        mController.setModeChangeListener(mCornerLayout);
        mController.setPenColorListener(mPenColorPicker);
        mController.setPenSizeListener(mPenSeekBar);
        mController.setMosaicSizeListener(mMosaicsSeekBar);
    }

    /*
    @Override
    public void finishAction(boolean back, boolean forward) {
        mBack.setEnabled(back);
        mForward.setEnabled(forward);
    }

    @Override
    public void save(Bitmap newBitmap) {
        new SaveImageTask(this, this).execute(newBitmap);
    }

    @Override
    public void share(Bitmap newBitmap) {
        new ShareImageTask(this, this).execute(newBitmap);
    }

    @Override
    public void saved() {
        mSave.setEnabled(true);
    }

    @Override
    public void shared() {
        mShare.setEnabled(true);
    }
    */

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.save: {
                mSave.setEnabled(false);
                //mDrawView.goSave();
                mController.saved();
            }
            break;
            case R.id.share: {
                mShare.setEnabled(false);
                //mDrawView.goShare();
                mController.shared();
            }
            break;
            case R.id.back: {
                //int mode = mDrawView.goBack();
                //mCornerLayout.setIndex(mode);
            }
            break;
            case R.id.forward: {
                //int mode = mDrawView.goForward();
                //mCornerLayout.setIndex(mode);
            }
            break;
        }
    }

    @Override
    public void onShow(View view, boolean show) {
        mPreview.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
