package com.nq.pictureeditor.mode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
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

    private final static int DEFAULT_COLOR = Color.BLACK;
    private final static int DEFAULT_SIZE = 16;

    private String text = null;
    private Point mapped;

    private Paint mRectPaint = new Paint();
    private RectF textRect = new RectF();
    private boolean selected = false;

    private TextPaint mTextPaint = new TextPaint();
    private int color = DEFAULT_COLOR;
    private int size = DEFAULT_SIZE;

    public TextMode(Context context, Handler handler) {
        super(context);
        //mDrawPaint.setAntiAlias(true);
        //mDrawPaint.setStyle(Paint.Style.FILL);
        //mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        //mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        //mDrawPaint.setTextSize(180);
        //mDrawPaint.setColor(context.getColor(R.color.black));
        //mDrawPaint.setStrokeWidth(context.getResources().getInteger(R.integer.pen_default_size));

        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeCap(Paint.Cap.ROUND);
        mRectPaint.setStrokeJoin(Paint.Join.ROUND);
        mRectPaint.setColor(Color.RED);
        mRectPaint.setStrokeWidth(5);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(color);
        mTextPaint.setTextSize(Utils.textSize2paintSize(size));
    }

    public TextMode(TextMode o) {
        super(o);
        //this.mDrawPath.set(o.mDrawPath);
        //this.mDrawPaint.set(o.mDrawPaint);
        this.mTextPaint.set(o.mTextPaint);
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
                                    intent.putExtra("color", color);
                                    intent.putExtra("size", size);
                                    intent.putExtra("zoom", mZoomScale);
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
                        intent.putExtra("zoom", mZoomScale);
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
        intent.putExtra("zoom", mZoomScale);
        intent.setClass(mContext, TextActivity.class);
        ((Activity) mContext).startActivityForResult(intent, 0);
    }

    @Override
    public void turnOff() {

    }

    @Override
    public void redraw(Canvas mDrawCanvas) {
        drawText(mDrawCanvas, text, mapped, mTextPaint);
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
                    color = data.getIntExtra("color", DEFAULT_COLOR);
                    size = data.getIntExtra("size", DEFAULT_SIZE);
                    if (text != null && !text.isEmpty()) {
                        mapped = new Point(x, y);
                        mTextPaint.setColor(color);
                        mTextPaint.setTextSize(Utils.textSize2paintSize(size));
                        drawText(mDrawCanvas, text, mapped, mTextPaint);
                        RecordManager.getInstance().addRecord(new TextMode(this), false);
                    }
                }
            }
        }
    }

    private void drawText(Canvas canvas, String text, Point mapped, TextPaint paint) {
        canvas.save();

        int textWidth = (int) paint.measureText(text);
        //canvas.clipRect(rect);
        StaticLayout layout = new StaticLayout(
                text,
                paint,
                textWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                true); // 换行

        String subs[] = text.split("\n");
        int maxLength = 0;
        for (String sub : subs) {
            if (paint.measureText(sub) > maxLength) maxLength = (int) paint.measureText(sub);
        }

        Paint.FontMetrics metrics = paint.getFontMetrics();
        //float dy = (metrics.bottom - metrics.top) / 2;
        float left = mapped.x;
        float top = mapped.y + metrics.top;
        float right = mapped.x + maxLength;
        float bottom = top + (metrics.bottom - metrics.top) * layout.getLineCount();
        textRect.set(left, top, right, bottom);
        if (true) canvas.drawRect(textRect, mRectPaint);

        canvas.save();
        canvas.translate(mapped.x, mapped.y + metrics.top);
        layout.draw(canvas);

        canvas.restore();
    }
}
