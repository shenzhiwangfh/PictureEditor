package com.nq.pictureeditor;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nq.pictureeditor.mode.EditMode;
import com.nq.pictureeditor.view.ArcColorPicker;
import com.nq.pictureeditor.view.ArcSeekBar;
import com.nq.pictureeditor.view.CornerLayout;
import com.nq.pictureeditor.view.DrawView;

import javax.security.auth.login.LoginException;

public class DrawActivity extends AppCompatActivity implements DrawInterface, View.OnClickListener {

    private final static String TAG = "DrawActivity";

    private DrawView mDrawView;

    private ImageView mSave;
    private ImageView mBack;
    private ImageView mForward;
    //private ImageView mClip;
    //private ImageView mPen;

    private CornerLayout mCornerLayout;

    private EditMode mEditMode = new EditMode();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable(R.drawable.screenshot);
        Bitmap mBitmap = bitmapDrawable.getBitmap();

        mSave = findViewById(R.id.save);
        mBack = findViewById(R.id.back);
        mForward = findViewById(R.id.forward);
        //mClip = findViewById(R.id.clip);
        //mPen = findViewById(R.id.pen);

        /*
        mArcSeekBar = findViewById(R.id.pen_size);
        mArcColorPicker = findViewById(R.id.pen_color);
        mArcSeekBar.setOnPickListener(new ArcSeekBar.OnChangeListener() {
            @Override
            public void onChange(int value) {
                Log.e(TAG, "value=" + value);
            }
        });
        mArcColorPicker.setOnPickListener(new ArcColorPicker.OnPickListener() {
            @Override
            public void onPick(int color) {
                Log.e(TAG, "color=" + color);
            }
        });
        */


        mBack.setEnabled(false);
        mForward.setEnabled(false);

        mSave.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mForward.setOnClickListener(this);
        //mClip.setOnClickListener(this);
        //mPen.setOnClickListener(this);

        //mDrawView.init(mBitmap);
        //mDrawView.setFinishDraw(this);
        //setMode(0x10);

        mDrawView = findViewById(R.id.penview);
        mDrawView.initBitmap(mBitmap);
        mDrawView.setFinishDraw(this);
        setMode(0x10);

        mCornerLayout = findViewById(R.id.control);
        mCornerLayout.setOnModeListener(new CornerLayout.OnModeListener() {
            @Override
            public void onChange(int mode) {
                if(mode == 0) {
                    setMode(0x10);
                } else {
                    setMode(0x20);
                }
            }
        });

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
                //mDrawView.goSave();
                mDrawView.goSave();
                break;
            case R.id.back:
                mDrawView.goBack();
                break;
            case R.id.forward:
                mDrawView.goForward();
                break;
                /*
            case R.id.clip:
                setMode(0x10);
                break;
            case R.id.pen:
                setMode(0x20);
                break;
                */
        }
    }

    private void setMode(int mode) {
        //mDrawView.setMode(mode);
        mDrawView.setMode(mode);

        switch (mode) {
            case 0x10:
                //mClip.setBackgroundResource(R.drawable.ic_background_pressed);
                //mPen.setBackgroundResource(R.drawable.ic_background);
                break;
            case 0x20:
                //mClip.setBackgroundResource(R.drawable.ic_background);
                //mPen.setBackgroundResource(R.drawable.ic_background_pressed);
                break;
        }
    }
}
