package com.nq.pictureeditor;

import android.Manifest;
import android.content.Context;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.nq.pictureeditor.mode.ClipMode;
import com.nq.pictureeditor.mode.EditMode;
import com.nq.pictureeditor.mode.PenMode;
import com.nq.pictureeditor.view.ArcColorPicker;
import com.nq.pictureeditor.view.ArcSeekBar;
import com.nq.pictureeditor.view.CornerLayout;
import com.nq.pictureeditor.view.DrawView;
import com.nq.pictureeditor.view.MosaicsView;
import com.nq.pictureeditor.view.OnShowListener;
import com.nq.pictureeditor.view.Preview;
import com.nq.pictureeditor.view.ViewUtils;

public class DrawActivity extends AppCompatActivity
        implements View.OnClickListener,
        DrawInterface, OnShowListener,
        ArcColorPicker.OnPickListener,
        ArcSeekBar.OnSlideListener,
        CornerLayout.OnModeListener {

    private final static String TAG = "DrawActivity";

    private DrawView mDrawView;
    private Preview mPreview;

    private ImageView mSave;
    private ImageView mShare;
    private ImageView mBack;
    private ImageView mForward;

    private CornerLayout mCornerLayout;
    private ArcColorPicker mColorPicker;
    private ArcSeekBar mSeekBar;

    //private PenController penController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable(R.drawable.screenshot);
        Bitmap mBitmap = bitmapDrawable.getBitmap();

        initActions();
        initControllors();

        int size = mSeekBar.getDefaultSize();
        int color = mColorPicker.getDefauleColor();

        mDrawView = findViewById(R.id.main);
        mDrawView.initBitmap(mBitmap);
        mDrawView.setPenSize(size);
        mDrawView.setPenColor(color);
        mDrawView.setFinishDraw(this);
        mDrawView.setMode(EditMode.MODE_CLIP);
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
        mColorPicker = findViewById(R.id.pen_color);
        mSeekBar = findViewById(R.id.pen_size);

        mCornerLayout.setOnModeListener(this);
        mColorPicker.setOnPickListener(this);
        mColorPicker.setOnShowListener(this);
        mSeekBar.setOnSlideListener(this);
        mSeekBar.setOnShowListener(this);
    }

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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.save: {
                mSave.setEnabled(false);
                mDrawView.goSave();
            }
            break;
            case R.id.share: {
                mShare.setEnabled(false);
                mDrawView.goShare();
            }
            break;
            case R.id.back: {
                int mode = mDrawView.goBack();
                mCornerLayout.setIndex(mode);
            }
            break;
            case R.id.forward: {
                int mode = mDrawView.goForward();
                mCornerLayout.setIndex(mode);
            }
            break;
        }
    }

    @Override
    public void onChange(int mode) {
        mDrawView.setMode(mode);
    }

    @Override
    public void onPick(View view, int color) {
        mDrawView.setPenColor(color);
    }

    @Override
    public void onSlide(View view, int value) {
        mDrawView.setPenSize(value);
    }

    @Override
    public void onShow(View view, boolean show) {
        mPreview.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
