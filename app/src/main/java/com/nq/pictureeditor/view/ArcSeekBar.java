package com.nq.pictureeditor.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.nq.pictureeditor.R;

public class ArcSeekBar extends View {

    private final static String TAG = "ArcSeekBar";

    private final static double START_ARC = Math.PI / 2 * 3;
    private final static double END_ARC = Math.PI * 2;
    private final static double SWEEP_ARC = END_ARC - START_ARC;

    private final static int TOUCH_NULL = 0;
    private final static int TOUCH_SLIDER = 1;

    private int arcRadius;
    private int arcWidth;
    private int sliderRadius;
    private int maxValue;
    private int minValue;
    private int value;
    private float percent;
    private int defaultValue;

    private RectF arcRect = new RectF();

    private PointF rootPoint = new PointF();
    private PointF sliderPoint = new PointF();

    private Paint mArcPaint;
    private Paint mSliderPaint;

    private int touchMode;
    private float downX, downY;

    public interface OnSlideListener {
        void onSlide(View view, int size);
    }

    private OnSlideListener slideListener;
    private OnShowListener showListener;

    public void setOnSlideListener(OnSlideListener onSlideListener) {
        this.slideListener = onSlideListener;
    }

    public void setOnShowListener(OnShowListener onShowListener) {
        this.showListener = onShowListener;
    }

    public ArcSeekBar(Context context) {
        this(context, null);
    }

    public ArcSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcSeekBar);
        arcRadius = a.getDimensionPixelOffset(R.styleable.ArcSeekBar_arc_radius, 100);
        arcWidth = a.getDimensionPixelOffset(R.styleable.ArcSeekBar_arc_width, 10);
        sliderRadius = a.getDimensionPixelOffset(R.styleable.ArcSeekBar_slider_radius, 40);
        maxValue = a.getInteger(R.styleable.ArcSeekBar_max_value, 100);
        minValue = a.getInteger(R.styleable.ArcSeekBar_min_value, 0);
        value = defaultValue = a.getInteger(R.styleable.ArcSeekBar_default_value, 0);
        a.recycle();

        init();
    }

    private void init() {
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(arcWidth);
        mArcPaint.setColor(Color.GRAY);

        mSliderPaint = new Paint();
        mSliderPaint.setAntiAlias(true);
        mSliderPaint.setStyle(Paint.Style.FILL);
        mSliderPaint.setColor(Color.GRAY);

        percent = (float) (value - minValue) / (float) (maxValue - minValue);
        //percent2Position(percent);
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

        rootPoint.x = getPaddingStart();
        rootPoint.y = size - getPaddingBottom();

        arcRect.set(rootPoint.x - arcRadius,
                rootPoint.y - arcRadius,
                rootPoint.x + arcRadius,
                rootPoint.y + arcRadius);

        if(touchMode == TOUCH_NULL) percent2Position(percent);
    }

    private int measureWidth(int defaultWidth, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultWidth = arcRadius + getPaddingStart() + getPaddingEnd();
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
                defaultHeight = arcRadius + getPaddingTop() + getPaddingBottom();
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
        canvas.drawCircle(sliderPoint.x, sliderPoint.y, sliderRadius, mSliderPaint);
        canvas.drawArc(arcRect, 270, 90, false, mArcPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                touchMode = TOUCH_NULL;
                if (ViewUtils.contain(sliderPoint, sliderRadius, downX, downY, 30)) {
                    touchMode = TOUCH_SLIDER;
                    if(showListener != null) showListener.onShow(this, true);
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                float y = event.getY();
                float inner = arcRadius - sliderRadius;
                float outer = arcRadius + sliderRadius;

                if (ViewUtils.contain(rootPoint, inner, outer, x, y, 60) &&
                        //(x >= rootPoint.x) &&
                        //(x <= rootPoint.x + arcRadius) &&
                        //(y >= rootPoint.y - arcRadius) &&
                        //(y <= rootPoint.y) &&
                        (touchMode == TOUCH_SLIDER)) {

                    if(x < rootPoint.x) x = rootPoint.x;
                    if(x > rootPoint.x + arcRadius) x = rootPoint.x + arcRadius;
                    if(y < rootPoint.y - arcRadius) y = rootPoint.y - arcRadius;
                    if(y > rootPoint.y) y = rootPoint.y;

                    float p = position2percent(x, y);
                    percent2Position(p);
                    invalidate();

                    int v = minValue + (int) ((maxValue - minValue) * p);
                    if (slideListener != null) slideListener.onSlide(this, v);
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                if(showListener != null) showListener.onShow(this, false);
            }
            break;
        }

        return (touchMode == TOUCH_SLIDER);
    }

    private float position2percent(float x, float y) {
        float h = Math.abs(x - rootPoint.x);
        float v = Math.abs(y - rootPoint.y);
        float r = (float) Math.pow(Math.pow(h, 2) + Math.pow(v, 2), 0.5);
        float sweep = (float) Math.asin(h / r);
        return sweep / (float) (END_ARC - START_ARC);
    }

    private void percent2Position(float percent) {
        sliderPoint.x = rootPoint.x + arcRadius * (float) Math.cos(START_ARC + SWEEP_ARC * percent);
        sliderPoint.y = rootPoint.y + arcRadius * (float) Math.sin(START_ARC + SWEEP_ARC * percent);
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getDefaultSize() {
        return defaultValue;
    }
}
