package com.nq.pictureeditor.mode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.os.Handler;

import com.nq.pictureeditor.R;
import com.nq.pictureeditor.Utils;
import com.nq.pictureeditor.view.ViewUtils;

public class MosaicsPenMode extends PenMode {

    private PorterDuffXfermode mDuffXfermode;

    private Bitmap mMosaicBmp;

    public MosaicsPenMode(Context context, Handler handler) {
        super(handler);
        mDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setDither(true);
        mDrawPaint.setStyle(Paint.Style.STROKE);//描边
        mDrawPaint.setTextAlign(Paint.Align.CENTER);//居中
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);//圆角
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);//拐点圆角
        mDrawPaint.setStrokeWidth(context.getResources().getInteger(R.integer.pen_default_size));
    }

    public MosaicsPenMode(MosaicsPenMode o) {
        super(o);
    }

    public void setMosaicBmp(Bitmap mDrawBitmap) {
        if (mMosaicBmp != null) mMosaicBmp.recycle();
        mMosaicBmp = ViewUtils.BitmapMosaic(mDrawBitmap, 64);
    }

    @Override
    public int getMode() {
        return MODE_MOSAICS;
    }

    @Override
    public void turnOn(EditMode clipMode, Bitmap mDrawBitmap) {
        super.turnOn(clipMode, mDrawBitmap);
        //setMosaicBmp(mDrawBitmap);
    }

    @Override
    public void redraw(Canvas mDrawCanvas, Bitmap mDrawBitmap) {
        setMosaicBmp(mDrawBitmap);

        int canvasWidth = mDrawCanvas.getWidth();
        int canvasHeight = mDrawCanvas.getHeight();
        int layerId = mDrawCanvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);

        mDrawCanvas.clipRect(clipBitmapRect);
        mDrawCanvas.drawPath(mDrawPath, mDrawPaint);

        mDrawPaint.setXfermode(mDuffXfermode);
        mDrawCanvas.drawBitmap(mMosaicBmp, 0, 0, mDrawPaint); //画出重叠区域
        mDrawPaint.setXfermode(null);
        mDrawCanvas.restoreToCount(layerId);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, Canvas mDrawCanvas, Bitmap mDrawBitmap) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();

                mDrawPath.reset();
                Point mapped = Utils.mapped(M, downX, downY);
                downX = mapped.x;
                downY = mapped.y;
                mDrawPath.moveTo(downX, downY);

                /*
                int canvasWidth = mDrawCanvas.getWidth();
                int canvasHeight = mDrawCanvas.getHeight();
                int layerId = mDrawCanvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);

                mDrawCanvas.clipRect(clipBitmapRect);
                mDrawCanvas.drawPath(mDrawPath, mDrawPaint);

                mDrawPaint.setXfermode(mDuffXfermode);
                mDrawCanvas.drawBitmap(mMosaicBmp, 0, 0, mDrawPaint); //画出重叠区域
                mDrawPaint.setXfermode(null);
                mDrawCanvas.restoreToCount(layerId);
                */

            }
            break;
            case MotionEvent.ACTION_MOVE: {
                Point mapped = Utils.mapped(M, event.getX(), event.getY());
                float x = mapped.x;
                float y = mapped.y;

                float dx = Math.abs(downX - x);
                float dy = Math.abs(downY - y);
                if (dx > PEN_MIN_MOVE || dy > PEN_MIN_MOVE) {
                    mDrawPath.quadTo(downX, downY, (x + downX) / 2, (y + downY) / 2);
                    downX = x;
                    downY = y;

                    /*
                    int canvasWidth = mDrawCanvas.getWidth();
                    int canvasHeight = mDrawCanvas.getHeight();
                    int layerId = mDrawCanvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);

                    mDrawCanvas.clipRect(clipBitmapRect);
                    mDrawCanvas.drawPath(mDrawPath, mDrawPaint);

                    mDrawPaint.setXfermode(mDuffXfermode);
                    mDrawCanvas.drawBitmap(mMosaicBmp, 0, 0, mDrawPaint); //画出重叠区域
                    mDrawPaint.setXfermode(null);
                    mDrawCanvas.restoreToCount(layerId);
                    */
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                Point mapped = Utils.mapped(M, event.getX(), event.getY());
                mDrawPath.lineTo(mapped.x, mapped.y);

                /*
                int canvasWidth = mDrawCanvas.getWidth();
                int canvasHeight = mDrawCanvas.getHeight();
                int layerId = mDrawCanvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);

                mDrawCanvas.clipRect(clipBitmapRect);
                mDrawCanvas.drawPath(mDrawPath, mDrawPaint);

                mDrawPaint.setXfermode(mDuffXfermode);
                mDrawCanvas.drawBitmap(mMosaicBmp, 0, 0, mDrawPaint); //画出重叠区域
                mDrawPaint.setXfermode(null);
                mDrawCanvas.restoreToCount(layerId);
                */

                mHandler.sendEmptyMessage(0);
                //redraw(mDrawCanvas, mDrawBitmap);
            }
            break;
        }
        return true;
    }
}
