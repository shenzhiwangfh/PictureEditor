package com.nq.pictureeditor.mode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.os.Handler;

import com.nq.pictureeditor.Utils;
import com.nq.pictureeditor.record.ModeLoopInterface;
import com.nq.pictureeditor.record.RecordManager;
import com.nq.pictureeditor.text.TextActivity;

public class TextMode extends EditMode {

    public final static int REQUEST_CODE = 0;
    public final static int RESULT_CANCEL = 0;
    public final static int RESULT_SELECT = 1;

    private String text = null;
    private Point mapped;

    private Paint mRectPaint = new Paint();
    private RectF textRect = new RectF();
    private boolean selected = false;

    public TextMode(Context context, Handler handler) {
        super(context);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStyle(Paint.Style.FILL);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setTextSize(180);
        //mDrawPaint.setColor(context.getColor(R.color.black));
        //mDrawPaint.setStrokeWidth(context.getResources().getInteger(R.integer.pen_default_size));

        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeCap(Paint.Cap.ROUND);
        mRectPaint.setStrokeJoin(Paint.Join.ROUND);
        mRectPaint.setColor(Color.RED);
        mRectPaint.setStrokeWidth(5);
    }

    public TextMode(TextMode o) {
        super(o);
        //this.mDrawPath.set(o.mDrawPath);
        this.mDrawPaint.set(o.mDrawPaint);
        this.text = o.text;
        this.mapped = new Point(o.mapped.x, o.mapped.y);
        this.mRectPaint.set(o.mRectPaint);
        this.textRect.set(o.textRect);
        this.index = o.index;
    }

    @Override
    public int getMode() {
        return MODE_TEXT;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, Canvas mDrawCanvas) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                if (clipPictureRect.contains(downX, downY)) {
                    final Point downMapped = Utils.mapped(M, downX, downY);

                    boolean ret = RecordManager.getInstance().doLoop(new ModeLoopInterface() {
                        @Override
                        public boolean pickMode(EditMode mode) {
                            if (mode instanceof TextMode) {
                                TextMode textMode = (TextMode) mode;
                                if (textMode.textRect.contains(downMapped.x, downMapped.y)) {
                                    Intent intent = new Intent();
                                    intent.putExtra("x", textMode.mapped.x);
                                    intent.putExtra("y", textMode.mapped.y);
                                    intent.putExtra("text", textMode.text);
                                    intent.putExtra("index", textMode.index);
                                    intent.setClass(mContext, TextActivity.class);
                                    ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE);
                                    return true;
                                }
                            }
                            return false;
                        }
                    });

                    if (ret) {
                        Intent intent = new Intent();
                        intent.putExtra("x", downMapped.x);
                        intent.putExtra("y", downMapped.y);
                        //intent.putExtra("index", RecordManager.getInstance().newIndex());
                        intent.setClass(mContext, TextActivity.class);
                        ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE);
                    }
                }
                break;
        }
        return false;
    }

    @Override
    public void turnOn(EditMode clipMode) {
        set(clipMode);
        text = null;

        Intent intent = new Intent();
        //intent.putExtra("index", RecordManager.getInstance().newIndex());
        intent.setClass(mContext, TextActivity.class);
        ((Activity) mContext).startActivityForResult(intent, 0);
    }

    @Override
    public void turnOff() {

    }

    @Override
    public void redraw(Canvas mDrawCanvas) {
        drawText(mDrawCanvas, text, mapped, mDrawPaint);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data, Canvas mDrawCanvas) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_CANCEL) {
                //
            } else if (resultCode == RESULT_SELECT) {
                if (data != null) {
                    int x = data.getIntExtra("x", (int) (clipBitmapRect.left + clipBitmapRect.width() / 4));
                    int y = data.getIntExtra("y", (int) (clipBitmapRect.top + clipBitmapRect.height() / 4));
                    text = data.getStringExtra("text");
                    index = data.getIntExtra("index", -1);
                    if (text != null && !text.isEmpty()) {
                        mapped = new Point(x, y);
                        drawText(mDrawCanvas, text, mapped, mDrawPaint);
                        RecordManager.getInstance().addRecord(new TextMode(this), false);
                    }
                }
            }
        }
    }

    private void drawText(Canvas canvas, String text, Point mapped, Paint paint) {
        canvas.save();
        //canvas.clipRect(rect);
        canvas.drawText(text, mapped.x, mapped.y, paint);

        float textWidth = paint.measureText(text);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        //float dy = (metrics.bottom - metrics.top) / 2;
        textRect.set(mapped.x, mapped.y + metrics.top, mapped.x + textWidth, mapped.y + metrics.bottom);
        if(selected) canvas.drawRect(textRect, mRectPaint);

        canvas.restore();
    }
}
