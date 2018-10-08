package com.nq.pictureeditor.mode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.os.Handler;

import com.nq.pictureeditor.R;
import com.nq.pictureeditor.Utils;
import com.nq.pictureeditor.record.RecordManager;
import com.nq.pictureeditor.view.ViewUtils;

public class MosaicsPenMode extends PenMode {

    private PorterDuffXfermode mDuffXfermode;

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
        mDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    @Override
    public int getMode() {
        return MODE_MOSAICS;
    }

    @Override
    public void turnOn(EditMode clipMode) {
        super.turnOn(clipMode);
    }

    @Override
    public void redraw(Canvas mDrawCanvas, Bitmap mMosaicBmp) {
        drawPath(mDrawCanvas, clipBitmapRect, mMosaicBmp, mDrawPath, mDrawPaint, mDuffXfermode);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, Canvas mDrawCanvas, Bitmap mMosaicBmp) {
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
                drawPath(mDrawCanvas, clipBitmapRect, mMosaicBmp, mDrawPath, mDrawPaint, mDuffXfermode);

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
                    drawPath(mDrawCanvas, clipBitmapRect, mMosaicBmp, mDrawPath, mDrawPaint, mDuffXfermode);
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                Point mapped = Utils.mapped(M, event.getX(), event.getY());
                mDrawPath.lineTo(mapped.x, mapped.y);
                drawPath(mDrawCanvas, clipBitmapRect, mMosaicBmp, mDrawPath, mDrawPaint, mDuffXfermode);
                RecordManager.getInstance().addRecord(new MosaicsPenMode(this), false);
            }
            break;
        }
        return true;
    }

    private void drawPath(Canvas canvas, RectF rect, Bitmap bitmap, Path path, Paint paint, PorterDuffXfermode mode) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        int layerId = canvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);

        canvas.clipRect(rect);
        canvas.drawPath(path, paint);

        paint.setXfermode(mode);
        canvas.drawBitmap(bitmap, 0, 0, paint); //画出重叠区域
        paint.setXfermode(null);
        canvas.restoreToCount(layerId);
    }
}
