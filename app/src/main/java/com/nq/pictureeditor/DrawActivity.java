package com.nq.pictureeditor;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.nq.pictureeditor.control.ModeController;
import com.nq.pictureeditor.view.ArcColorPicker;
import com.nq.pictureeditor.view.ArcSeekBar;
import com.nq.pictureeditor.view.CornerLayout;
import com.nq.pictureeditor.view.DrawView;
import com.nq.pictureeditor.view.OnShowListener;
import com.nq.pictureeditor.view.Preview;

public class DrawActivity extends AppCompatActivity
        implements OnShowListener {

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

    private ModeController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        //initActions();
        initControllors();

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

    /*
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
    */

    private void initControllors() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable(R.drawable.screenshot);
        Bitmap mBitmap = bitmapDrawable.getBitmap();
        mController = new ModeController(this, mBitmap);

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
}
