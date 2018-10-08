package com.nq.pictureeditor.mode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;

import com.nq.pictureeditor.view.ArcColorPicker;
import com.nq.pictureeditor.view.ArcSeekBar;

public class PenMode extends EditMode  implements
        ArcColorPicker.OnPickListener,
        ArcSeekBar.OnSlideListener {
    protected Path mDrawPath = new Path();
    protected Paint mDrawPaint = new Paint();
    protected Handler mHandler;

    public static final float PEN_MIN_MOVE = 4.0f;

    public PenMode(Context context, Handler handler) {
        super(context);
        mHandler = handler;
    }

    public PenMode(PenMode o) {
        super(o);
        this.mDrawPath.set(o.mDrawPath);
        this.mDrawPaint.set(o.mDrawPaint);
    }

    @Override
    public int getMode() {
        return MODE_PEN;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, Canvas mDrawCanvas, Bitmap mDrawBitmap) {
        return false;
    }

    @Override
    public void turnOn(EditMode clipMode) {
        set(clipMode);
    }

    @Override
    public void turnOff() {

    }

    @Override
    public void redraw(Canvas mDrawCanvas) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data, Canvas mDrawCanvas) {

    }

    @Override
    public void onPick(View view, int color) {
        setColor(color);
    }

    @Override
    public void onSlide(View view, int size) {
        setSize(size);
    }

    public void setColor(int color) {
        mDrawPaint.setColor(color);
    }

    public void setSize(int size) {
        mDrawPaint.setStrokeWidth(size);
    }
}
