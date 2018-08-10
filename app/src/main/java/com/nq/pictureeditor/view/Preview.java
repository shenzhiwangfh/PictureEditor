package com.nq.pictureeditor.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class Preview extends View {

    private final static String TAG = "PreView";
    private final int COLOR_SIZE = 128;

    private int width, height;

    private Paint paint = new Paint();

    private Bitmap bitmap;

    private boolean pen = true;

    public Preview(Context context) {
        this(context, null);
    }

    public Preview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(int color, int size) {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(color);
        paint.setStrokeWidth(size);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        width = measureWidth(minimumWidth, widthMeasureSpec);
        height = measureHeight(minimumHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int defaultWidth, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.EXACTLY:
                defaultWidth = specSize;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                defaultWidth = Math.max(defaultWidth, specSize);
        }
        return defaultWidth;
    }

    private int measureHeight(int defaultHeight, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.EXACTLY:
                defaultHeight = specSize;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                defaultHeight = Math.max(defaultHeight, specSize);
        }
        return defaultHeight;
    }

    public void setPenSize(int size) {
        paint.setStrokeWidth(size);
        invalidate();
    }

    public void setPenColor(int color) {
        paint.setColor(color);
        paint.setStrokeWidth(COLOR_SIZE);
        pen = true;
        invalidate();
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        pen = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = width / 2;
        int y = height / 2;
        if (pen) {
            canvas.drawPoint(x, y, paint);
        } else {
            canvas.drawBitmap(bitmap, 0, 0, paint);
        }
    }
}
