package com.nq.pictureeditor.mode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import android.os.Handler;

public class TextMode extends EditMode {

    public TextMode(Handler handler) {
        super();
    }

    @Override
    public int getMode() {
        return MODE_TEXT;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, Canvas mDrawCanvas, Bitmap mMosaicBmp) {
        return false;
    }

    @Override
    public void turnOn(EditMode clipMode, Bitmap mDrawBitmap) {
        set(clipMode);
    }

    @Override
    public void turnOff() {

    }

    @Override
    public void redraw(Canvas mDrawCanvas, Bitmap mMosaicBmp) {

    }
}
