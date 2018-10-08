package com.nq.pictureeditor.mode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.EditText;

import java.io.Serializable;

public abstract class EditMode implements Serializable {
    public Context mContext;

    public final static int MODE_CLIP = 0;
    public final static int MODE_PEN = 1;
    public final static int MODE_MOSAICS = 2;
    public final static int MODE_TEXT = 3;

    public final static float MIN_SCALE = 0.25f;
    public final static float NORMAL_SCALE = 1.0f;
    public final static float MAX_SCALE = 10.0f;
    public float mZoomScale = NORMAL_SCALE; //默认的缩放比为1 //

    public RectF prePictureRect = new RectF(); //
    public RectF pictureRect = new RectF(); //
    public RectF clipPictureRect = new RectF(); //
    public RectF bitmapRect = new RectF(); //
    public RectF clipBitmapRect = new RectF(); //
    public Matrix M = new Matrix(); //

    public float downX, downY;
    public float picOffsetX = 0.0f, picOffsetY = 0.0f;

    public Paint mDrawPaint;
    public int index = -1;

    public EditMode() {
        mDrawPaint = new Paint(/*Paint.ANTI_ALIAS_FLAG*/);
        mDrawPaint.setColor(Color.BLACK);
    }

    public EditMode(Context context) {
        this();
        mContext = context;
    }

    public EditMode(EditMode o) {
        this();
        set(o);
    }

    public void set(EditMode o) {
        this.pictureRect.set(o.pictureRect);
        this.clipPictureRect.set(o.clipPictureRect);
        this.bitmapRect.set(o.bitmapRect);
        this.clipBitmapRect.set(o.clipBitmapRect);
        this.M.set(o.M);
        this.mZoomScale = o.mZoomScale;
    }

    public abstract int getMode();

    public abstract boolean onTouchEvent(MotionEvent event, Canvas mDrawCanvas);

    public abstract void turnOn(EditMode clipMode);

    public abstract void turnOff();

    public abstract void redraw(Canvas mDrawCanvas);

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data, Canvas mDrawCanvas);

    /*
    public void resetDrawBitmap() {
        mDrawBitmap.recycle();
        mDrawBitmap = Bitmap.createBitmap(mOriginBitmap);
        mDrawCanvas.setBitmap(mDrawBitmap);
    }
    */

    public boolean isClipMode() {
        return getMode() == MODE_CLIP;
    }

    public boolean isPenMode() {
        return getMode() == MODE_PEN;
    }

    public boolean isMosaicsMode() {
        return getMode() == MODE_MOSAICS;
    }

    public boolean isTextMode() {
        return getMode() == MODE_TEXT;
    }

    public Bitmap saveBitmap(Bitmap mDrawBitmap) {
        return Bitmap.createBitmap(mDrawBitmap,
                (int) clipBitmapRect.left,
                (int) clipBitmapRect.top,
                (int) clipBitmapRect.width(),
                (int) clipBitmapRect.height());
    }

    public void onDraw(Canvas canvas, Bitmap mDrawBitmap) {
        canvas.save();
        mDrawPaint.setAlpha(255);
        canvas.clipRect(clipPictureRect);
        canvas.drawBitmap(mDrawBitmap, M, mDrawPaint);
        canvas.restore();
    }

    public boolean onScale(ScaleGestureDetector detector) {
        return true;
    }

    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    public void setPrePictureRect(RectF rect) {
        prePictureRect.set(rect);
    }

    public void setPictureRect(float mZoomScale) {
        this.mZoomScale = mZoomScale;

        float oldWidth = prePictureRect.width();
        float oldHeight = prePictureRect.height();
        float newWidth = bitmapRect.width() * mZoomScale;
        float newHeight = bitmapRect.height() * mZoomScale;

        float left = prePictureRect.left - (newWidth - oldWidth) / 2 + picOffsetX;
        float right = prePictureRect.right + (newWidth - oldWidth) / 2 + picOffsetX;
        float top = prePictureRect.top - (newHeight - oldHeight) / 2 + picOffsetY;
        float bottom = prePictureRect.bottom + (newHeight - oldHeight) / 2 + picOffsetY;

        pictureRect.set(left, top, right, bottom);
    }

    public void setClipPictureRect(RectF canvasRect) {
        float left = Math.max(pictureRect.left, canvasRect.left);
        float right = Math.min(pictureRect.right, canvasRect.right);
        float top = Math.max(pictureRect.top, canvasRect.top);
        float bottom = Math.min(pictureRect.bottom, canvasRect.bottom);

        clipPictureRect.set(left, top, right, bottom);
    }

    public void setBitmapRect(Bitmap mDrawBitmap) {
        bitmapRect.set(0, 0, mDrawBitmap.getWidth(), mDrawBitmap.getHeight());
    }

    public void setClipBitmapRect() {
        float ratio = bitmapRect.height() / pictureRect.height();

        float left = (clipPictureRect.left - pictureRect.left) * ratio;
        float right = left + clipPictureRect.width() * ratio;
        float top = (clipPictureRect.top - pictureRect.top) * ratio;
        float bottom = top + clipPictureRect.height() * ratio;

        clipBitmapRect.set(left, top, right, bottom);
    }

    public void matrix() {
        M.setRectToRect(bitmapRect, pictureRect, Matrix.ScaleToFit.FILL);
    }

    public void setZoomScale(float mZoomScale) {
        this.mZoomScale = mZoomScale;
    }
}
