package com.nq.pictureeditor.mode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.view.MotionEvent;

import com.nq.pictureeditor.R;
import com.nq.pictureeditor.Utils;

public class ColorPenMode extends PenMode {

    public ColorPenMode(Context context, Handler handler) {
        super(handler);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setColor(context.getColor(R.color.black));
        mDrawPaint.setStrokeWidth(context.getResources().getInteger(R.integer.pen_default_size));
    }

    public ColorPenMode(ColorPenMode o) {
        super(o);
    }

    @Override
    public int getMode() {
        return MODE_PEN;
    }

    @Override
    public void turnOn(EditMode clipMode, Bitmap mDrawBitmap) {
        super.turnOn(clipMode, mDrawBitmap);
    }

    @Override
    public void turnOff() {

    }

    @Override
    public void redraw(Canvas mDrawCanvas, Bitmap mDrawBitmap) {
        mDrawCanvas.save();
        mDrawCanvas.clipRect(clipBitmapRect);
        mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
        mDrawCanvas.restore();
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
                mDrawCanvas.clipRect(clipBitmapRect);
                mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
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
                    mDrawCanvas.clipRect(clipBitmapRect);
                    mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                Point mapped = Utils.mapped(M, event.getX(), event.getY());
                mDrawPath.lineTo(mapped.x, mapped.y);
                mDrawCanvas.clipRect(clipBitmapRect);
                mDrawCanvas.drawPath(mDrawPath, mDrawPaint);

                mHandler.sendEmptyMessage(0);
                //redraw(mDrawCanvas, mDrawBitmap);
            }
            break;
        }
        return true;
    }
}
