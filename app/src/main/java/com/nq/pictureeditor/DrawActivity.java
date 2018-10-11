package com.nq.pictureeditor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.nq.pictureeditor.control.ModeController;
import com.nq.pictureeditor.task.LoadImageTask;
import com.nq.pictureeditor.task.LoadListener;
import com.nq.pictureeditor.view.ArcColorPicker;
import com.nq.pictureeditor.view.ArcSeekBar;
import com.nq.pictureeditor.view.CornerLayout;
import com.nq.pictureeditor.view.DrawView;
import com.nq.pictureeditor.view.OnShowListener;
import com.nq.pictureeditor.view.Preview;
import com.wang.avi.AVLoadingIndicatorView;

public class DrawActivity extends AppCompatActivity
        implements OnShowListener, LoadListener {

    public final static String TAG = "DrawActivity";

    private DrawView mDrawView;
    private Preview mPreview;

    /*
    private ImageView mSave;
    private ImageView mShare;
    private ImageView mBack;
    private ImageView mForward;
    */

    private CornerLayout mCornerLayout;
    private ArcColorPicker mPenColorPicker;
    private ArcSeekBar mPenSeekBar;
    private ArcSeekBar mMosaicsSeekBar;
    private AVLoadingIndicatorView mLoading;

    private ModeController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        mLoading = findViewById(R.id.loading);
        mLoading.smoothToShow();

        Intent intent = getIntent();
        Uri mImageCaptureUri = intent.getData();
        if (mImageCaptureUri != null) new LoadImageTask(this, this).execute(mImageCaptureUri);

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    /*
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
    */

    private void initControllors(Bitmap bitmap) {
        //BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable(R.drawable.screenshot);
        //Bitmap mBitmap = bitmapDrawable.getBitmap();
        mController = new ModeController(this, bitmap);

        mDrawView = findViewById(R.id.main);
        mPreview = findViewById(R.id.preview);

        mCornerLayout = findViewById(R.id.control);
        mPenColorPicker = findViewById(R.id.pen_color);
        mPenSeekBar = findViewById(R.id.pen_size);
        mMosaicsSeekBar = findViewById(R.id.mosaics_size);

        mController.setDrawListener(mDrawView);
        mController.setModeChangeListener(mCornerLayout);
        mController.setPenColorListener(mPenColorPicker);
        mController.setPenSizeListener(mPenSeekBar);
        mController.setMosaicSizeListener(mMosaicsSeekBar);
        mController.initActionBtn(this);
    }

    @Override
    public void onShow(View view, boolean show) {
        mPreview.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mController.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            initControllors(bitmap);
            mDrawView.invalidate();
            mLoading.smoothToHide();
        }
    }
}
