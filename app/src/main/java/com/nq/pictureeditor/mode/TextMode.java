package com.nq.pictureeditor.mode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import android.os.Handler;

import com.nq.pictureeditor.DrawActivity;
import com.nq.pictureeditor.record.RecordManager;
import com.nq.pictureeditor.text.TextActivity;

public class TextMode extends EditMode {

    private String text = null;

    public TextMode(Context context, Handler handler) {
        super(context);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setTextSize(100);
        //mDrawPaint.setColor(context.getColor(R.color.black));
        //mDrawPaint.setStrokeWidth(context.getResources().getInteger(R.integer.pen_default_size));
    }

    public TextMode(TextMode o) {
        super(o);
        //this.mDrawPath.set(o.mDrawPath);
        this.mDrawPaint.set(o.mDrawPaint);
        this.text = o.text;
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
    public void turnOn(EditMode clipMode) {
        set(clipMode);

        text = null;
        Intent intent = new Intent();
        intent.setClass(mContext, TextActivity.class);
        ((Activity) mContext).startActivityForResult(intent, 0);
    }

    @Override
    public void turnOff() {

    }

    @Override
    public void redraw(Canvas mDrawCanvas) {
        drawText(mDrawCanvas, clipBitmapRect, mDrawPaint);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data, Canvas mDrawCanvas) {
        Log.e(DrawActivity.TAG, "requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (data != null) {
            text = data.getStringExtra("text");
            Log.e(DrawActivity.TAG, "data=" + text);

            drawText(mDrawCanvas, clipBitmapRect, mDrawPaint);
            RecordManager.getInstance().addRecord(new TextMode(this), false);
        }
    }

    private void drawText(Canvas canvas, RectF rect, Paint paint) {
        canvas.save();
        //canvas.clipRect(rect);
        float x = rect.width() / 2;
        float y = rect.height() / 2;
        canvas.drawText(text, rect.left + x, rect.top + y, paint);
        canvas.restore();
    }
}
