package com.nq.pictureeditor.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nq.pictureeditor.R;

public class ArcColorPicker extends View {

    private final static String TAG = "ArcColorPicker";
    private final static int SelectedEdge = 3;
    private final static int SeekBarWidth = 180;
    private final static int StickWidth = 4;
    private final static int SliderRadius = 42;

    private final static int TOUCH_NULL = 0;
    private final static int TOUCH_SLIDER = 1;
    private final static int TOUCH_COLOR = 2;

    private int radius;
    private int colorRadius;
    private boolean eraser;
    private Bitmap eraserBmp;
    private boolean hasSeekBar;
    private int[] colors;
    private int count;

    private Rect eraserBmpRect;
    private RectF eraserRect;

    private PointF rootPoint = new PointF();
    private PointF eraserPoint = new PointF();
    private PointF colorPoints[];// = new PointF[];

    private Paint mEraserPaint;
    private Paint mColorPaint;
    private Paint mSeekBarPaint;

    private RectF seekbarRect = new RectF();
    private RectF stickRect = new RectF();
    private PointF sliderPoint = new PointF();

    private int touchMode;
    private float downX, downY;

    private int mIndex = -1;

    public interface OnPickListener {
        void onPick(int color);
    }

    private OnPickListener l;

    public void setOnPickListener(OnPickListener onPickListener) {
        l = onPickListener;
    }

    public ArcColorPicker(Context context) {
        this(context, null);
    }

    public ArcColorPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcColorPicker);
        radius = a.getDimensionPixelOffset(R.styleable.ArcColorPicker_picker_radius, 200);
        colorRadius = a.getDimensionPixelOffset(R.styleable.ArcColorPicker_color_radius, 20);
        eraser = a.getBoolean(R.styleable.ArcColorPicker_eraser, false);
        if (eraser) {
            BitmapDrawable eraserImg = (BitmapDrawable) a.getDrawable(R.styleable.ArcColorPicker_eraser_img);
            if (eraserImg == null) {
                eraserImg = (BitmapDrawable) a.getResources().getDrawable(R.drawable.ic_menu_crop, null);
            }
            eraserBmp = eraserImg.getBitmap();
            eraserBmpRect = new Rect(0, 0, eraserBmp.getWidth(), eraserBmp.getHeight());
            eraserRect = new RectF();
        }
        hasSeekBar = a.getBoolean(R.styleable.ArcColorPicker_seekbar, false);
        int valuesResId = a.getResourceId(R.styleable.ArcColorPicker_colors, 0);
        if (valuesResId == 0) {
            throw new IllegalArgumentException("ArcColorPicker: error - colors is not specified");
        }
        colors = a.getResources().getIntArray(valuesResId);
        a.recycle();

        init();
    }

    private void init() {
        count = colors.length;
        //if(eraser) count++;

        if (hasSeekBar) {
            rootPoint.x = rootPoint.x + SeekBarWidth;
        }

        colorPoints = new PointF[count];
        for (int i = 0; i < count; i++) {
            colorPoints[i] = new PointF();
        }

        mColorPaint = new Paint();
        mColorPaint.setAntiAlias(true);
        mColorPaint.setStyle(Paint.Style.FILL);
        mColorPaint.setColor(Color.GRAY);

        mEraserPaint = new Paint();

        mSeekBarPaint = new Paint();
        mSeekBarPaint.setColor(Color.GRAY);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        int allCount = eraser ? count : (count - 1);
        float sweepArc = (float) Math.PI / 2.0f / (float) allCount;
        float startArc = (float) Math.PI / 2.0f * 3.0f;

        if (eraser) {
            //float itemArc = startArc;
            double sin = Math.sin(startArc);
            double cos = Math.cos(startArc);

            int x = (int) (rootPoint.x + radius * cos);
            int y = (int) (rootPoint.y + radius * sin);
            eraserPoint.set(x, y);
        }

        for (int i = 0; i < count; i++) {
            int index = eraser ? (i + 1) : i;
            float itemArc = startArc + sweepArc * index;
            double sin = Math.sin(itemArc);
            double cos = Math.cos(itemArc);

            int x = (int) (rootPoint.x + radius * cos);
            int y = (int) (rootPoint.y + radius * sin);
            colorPoints[i].set(x, y);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int width = measureWidth(minimumWidth, widthMeasureSpec);
        int height = measureHeight(minimumHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
        int size = (width > height) ? height : width; // Choose the smaller

        rootPoint.x = getPaddingStart() + colorRadius;
        rootPoint.y = size - getPaddingBottom() - colorRadius;

        if (hasSeekBar) {
            seekbarRect.set(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + SeekBarWidth, height - getPaddingBottom());
            sliderPoint.x = getPaddingLeft() + SeekBarWidth / 2;
            sliderPoint.y = getPaddingTop() + colorRadius;
            stickRect.set(sliderPoint.x - StickWidth / 2, sliderPoint.y, sliderPoint.x + StickWidth / 2, sliderPoint.y + radius);
        }
    }

    private int measureWidth(int defaultWidth, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultWidth = radius + getPaddingLeft() + getPaddingRight();
                break;
            case MeasureSpec.EXACTLY:
                defaultWidth = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultWidth = Math.max(defaultWidth, specSize);
        }
        return defaultWidth;
    }

    private int measureHeight(int defaultHeight, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultHeight = radius + getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.EXACTLY:
                defaultHeight = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultHeight = Math.max(defaultHeight, specSize);
        }
        return defaultHeight;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (hasSeekBar) {
            mSeekBarPaint.setColor(Color.DKGRAY);
            canvas.drawRect(stickRect, mSeekBarPaint);
            mSeekBarPaint.setColor(Color.GRAY);
            canvas.drawCircle(sliderPoint.x, sliderPoint.y, SliderRadius, mSeekBarPaint);
        }

        if (eraser) {
            if (mIndex == -1) {
                mColorPaint.setColor(Color.GRAY);
                canvas.drawCircle(eraserPoint.x, eraserPoint.y, colorRadius + SelectedEdge, mColorPaint);
            }
            mColorPaint.setColor(Color.RED);
            canvas.drawCircle(eraserPoint.x, eraserPoint.y, colorRadius, mColorPaint);

            float left = eraserPoint.x - colorRadius;
            float top = eraserPoint.y - colorRadius;
            float right = eraserPoint.x + colorRadius;
            float bottom = eraserPoint.y + colorRadius;
            drawBitmap(canvas, mEraserPaint, left, top, right, bottom, eraserBmpRect);
        }

        for (int i = 0; i < count; i++) {
            if (mIndex == i) {
                mColorPaint.setColor(Color.GRAY);
                canvas.drawCircle(colorPoints[i].x, colorPoints[i].y, colorRadius + SelectedEdge, mColorPaint);
            }

            mColorPaint.setColor(colors[i]);
            canvas.drawCircle(colorPoints[i].x, colorPoints[i].y, colorRadius, mColorPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                touchMode = TOUCH_NULL;
                downX = event.getX();
                downY = event.getY();

                if(ViewUtils.contain(sliderPoint, SliderRadius, downX, downY, 10)) {
                    touchMode = TOUCH_SLIDER;
                } else if (ViewUtils.contain(eraserPoint, colorRadius, downX, downY, 10)) {
                    if (l != null) l.onPick(-1);
                    mIndex = -1;
                    touchMode = TOUCH_COLOR;
                    invalidate();
                } else {
                    for (int i = 0; i < count; i++) {
                        if (ViewUtils.contain(colorPoints[i], colorRadius, downX, downY, 10)) {
                            if (l != null) l.onPick(colors[i]);
                            mIndex = i;
                            touchMode = TOUCH_COLOR;
                            invalidate();
                        }
                    }
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                float y = event.getY();

                if(seekbarRect.contains(x, y) &&
                        (y >= stickRect.top) &&
                        (y <= stickRect.bottom) &&
                        (touchMode == TOUCH_SLIDER)) {
                    sliderPoint.y = y;
                    invalidate();
                } else if(touchMode == TOUCH_NULL) {

                } else if(touchMode == TOUCH_COLOR) {
                    for (int i = 0; i < count; i++) {
                        if (ViewUtils.contain(colorPoints[i], colorRadius, x, y, 10)) {
                            if (l != null) l.onPick(colors[i]);
                            mIndex = i;
                            invalidate();
                        }
                    }
                }
            }
            break;
        }
        return (touchMode == TOUCH_COLOR);
    }

    private void drawBitmap(Canvas canvas, Paint paint, float left, float top, float right, float bottom, Rect bmpRect) {

        float offsetx = ((right - left) - bmpRect.width()) / 2;
        float offsety = ((bottom - top) - bmpRect.height()) / 2;
        if (offsetx > 0) {
            left = left + offsetx;
            right = right - offsetx;
        }
        if (offsety > 0) {
            top = top + offsety;
            bottom = bottom - offsety;
        }
        eraserRect.set(left, top, right, bottom);
        canvas.drawBitmap(eraserBmp, eraserBmpRect, eraserRect, paint);
    }
}
