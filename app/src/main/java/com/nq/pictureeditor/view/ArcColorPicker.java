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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import com.nq.pictureeditor.R;

public class ArcColorPicker extends View {

    private final static String TAG = "ArcColorPicker";
    private final static int SelectedEdge = 3;

    private final static int TOUCH_NULL = 0;
    private final static int TOUCH_COLOR = 1;

    private int radius;
    private int colorRadius;
    private int[] colors;
    private int[] values;
    private int defauleValue;
    private int count;

    private PointF rootPoint = new PointF();
    private PointF colorPoints[];// = new PointF[];
    private RectF imageRectF = new RectF();
    private Rect bitmapRect = new Rect();

    private Paint mColorPaint;

    private int touchMode;
    private float downX, downY;

    private int mIndex = 0;

    public interface OnPickListener {
        void onPick(View view, int resId);
    }

    private OnPickListener pickListener;
    private OnShowListener showListener;

    public void setOnPickListener(OnPickListener onPickListener) {
        pickListener = onPickListener;
    }

    public void setOnShowListener(OnShowListener onShowListener) {
        this.showListener = onShowListener;
    }

    public ArcColorPicker(Context context) {
        this(context, null);
    }

    public ArcColorPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcColorPicker);
        radius = a.getDimensionPixelOffset(R.styleable.ArcColorPicker_picker_radius, 200);
        colorRadius = a.getDimensionPixelOffset(R.styleable.ArcColorPicker_color_radius, 20);
        mIndex = defauleValue = a.getInteger(R.styleable.ArcColorPicker_default_color, 0);

        int valuesResId = a.getResourceId(R.styleable.ArcColorPicker_values, 0);
        if (valuesResId == 0) {
            throw new IllegalArgumentException("ArcColorPicker: error - colors is not specified");
        }
        values = a.getResources().getIntArray(valuesResId);

        valuesResId = a.getResourceId(R.styleable.ArcColorPicker_colors, 0);
        if (valuesResId == 0) {
            throw new IllegalArgumentException("ArcColorPicker: error - colors is not specified");
        }
        //colors = a.getResources().getIntArray(valuesResId);
        TypedArray array = a.getResources().obtainTypedArray(valuesResId);
        colors = new int[array.length()];
        for (int i = 0; i < array.length(); i++) {
            colors[i] = array.getResourceId(i, 0);
        }
        array.recycle();

        a.recycle();
        init();
    }

    private void init() {
        count = colors.length;

        colorPoints = new PointF[count];
        for (int i = 0; i < count; i++) {
            colorPoints[i] = new PointF();
        }

        mColorPaint = new Paint();
        mColorPaint.setAntiAlias(true);
        mColorPaint.setStyle(Paint.Style.FILL);
        mColorPaint.setColor(Color.GRAY);
    }

    float sweepArc;
    float startArc;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        sweepArc = (float) Math.PI / 2.0f / (float) (count - 1);
        startArc = (float) Math.PI / 2.0f * 3.0f;

        for (int i = 0; i < count; i++) {
            float itemArc = startArc + sweepArc * i;
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

        for (int i = 0; i < count; i++) {
            if (mIndex == i) {
                mColorPaint.setColor(Color.GRAY);
                canvas.drawCircle(colorPoints[i].x, colorPoints[i].y, colorRadius + SelectedEdge, mColorPaint);
            }

            if (values[i] == 0) {
                int color = getResources().getColor(colors[i], null);
                if (color == Color.WHITE) {
                    mColorPaint.setColor(Color.BLACK);
                    canvas.drawCircle(colorPoints[i].x, colorPoints[i].y, colorRadius, mColorPaint);

                    mColorPaint.setColor(color);
                    canvas.drawCircle(colorPoints[i].x, colorPoints[i].y, colorRadius - SelectedEdge, mColorPaint);
                } else {
                    mColorPaint.setColor(color);
                    canvas.drawCircle(colorPoints[i].x, colorPoints[i].y, colorRadius, mColorPaint);
                }
            } else if (values[i] == 1) {
                BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(colors[i], null);
                if (drawable != null) {
                    mColorPaint.setColor(Color.BLACK);
                    canvas.drawCircle(colorPoints[i].x, colorPoints[i].y, colorRadius, mColorPaint);

                    Bitmap bitmap = drawable.getBitmap();
                    bitmapRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    drawBitmap(colorPoints[i], colorRadius, bitmapRect);
                    canvas.drawBitmap(bitmap, bitmapRect, imageRectF, mColorPaint);
                }
            }
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

                for (int i = 0; i < count; i++) {
                    if (ViewUtils.contain(colorPoints[i], colorRadius, downX, downY, 10)) {
                        mIndex = i;
                        if (pickListener != null) pickListener.onPick(this, colors[mIndex]);
                        if (showListener != null) showListener.onShow(this, true);

                        touchMode = TOUCH_COLOR;
                        invalidate();
                    }
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                float y = event.getY();
                float inner = radius - colorRadius;
                float outer = radius + colorRadius;

                if (ViewUtils.contain(rootPoint, inner, outer, x, y, 60) &&
                        //(x >= rootPoint.x) &&
                        //(x <= rootPoint.x + arcRadius) &&
                        //(y >= rootPoint.y - arcRadius) &&
                        //(y <= rootPoint.y) &&
                        (touchMode == TOUCH_COLOR)) {

                    if (x < rootPoint.x) x = rootPoint.x;
                    if (x > rootPoint.x + radius) x = rootPoint.x + radius;
                    if (y < rootPoint.y - radius) y = rootPoint.y - radius;
                    if (y > rootPoint.y) y = rootPoint.y;

                    mIndex = position2index(x, y);
                    if (pickListener != null) pickListener.onPick(this, colors[mIndex]);

                    invalidate();
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                if (showListener != null) showListener.onShow(this, false);
            }
            break;
        }
        return (touchMode == TOUCH_COLOR);
    }

    private int position2index(float x, float y) {
        int index = 0;

        float h = Math.abs(x - rootPoint.x);
        float v = Math.abs(y - rootPoint.y);
        float r = (float) Math.pow(Math.pow(h, 2) + Math.pow(v, 2), 0.5);
        float sweep = (float) Math.asin(h / r);

        for (; index < count; index++) {
            if (sweep <= sweepArc * index) {
                break;
            }
        }

        return index;
    }

    private void drawBitmap(PointF center, float r, Rect bmpRect) {
        float left, top, right, bottom;
        r = r - 6; //contract

        float offsetx = (r * 2 - bmpRect.width()) / 2;
        float offsety = (r * 2 - bmpRect.height()) / 2;
        if (offsetx < 0) offsetx = 0.0f;
        if (offsety < 0) offsety = 0.0f;

        left = center.x - r + offsetx;
        right = center.x + r - offsetx;
        top = center.y - r + offsety;
        bottom = center.y + r - offsety;

        imageRectF.set(left, top, right, bottom);
    }

    public int getDefauleValue() {
        return defauleValue;
    }
}
