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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.nq.pictureeditor.R;

public class CornerLayout extends RelativeLayout implements GestureDetector.OnGestureListener {

    private final static String TAG = "CornerLayout";

    private final static float EDGE = 3;
    private final static float TOUCH_EDGE = 60;

    private int itemRadius;
    private Bitmap[] bitmaps;
    private int[] values;

    private PointF mRootPoint = new PointF();
    private RectF mRootRectF = new RectF();
    private Rect mImgRect = new Rect();

    private Paint mRootPaint;

    private GestureDetector mGesture;
    private boolean touch = false;
    private boolean open = true;

    private int mChildCount;
    private int mIndex = 0;

    public interface OnModeListener {
        void onChange(int mode);
    }

    private OnModeListener l;

    public void setOnModeListener(OnModeListener onModeListener) {
        l = onModeListener;
    }

    public CornerLayout(Context context) {
        this(context, null);
    }

    public CornerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerLayout);
        itemRadius = typedArray.getDimensionPixelOffset(R.styleable.CornerLayout_element_radius, 20);

        //images
        int valuesResId = typedArray.getResourceId(R.styleable.CornerLayout_element_imgs, 0);
        if (valuesResId == 0) {
            throw new IllegalArgumentException("ArcColorPicker: error - colors is not specified");
        }
        TypedArray images = typedArray.getResources().obtainTypedArray(valuesResId);
        bitmaps = new Bitmap[images.length()];

        for (int i = 0; i < images.length(); i++) {
            int id = images.getResourceId(i, 0);
            BitmapDrawable bitmap = (BitmapDrawable) typedArray.getResources().getDrawable(id, null);
            if (bitmap != null) bitmaps[i] = bitmap.getBitmap();
        }
        mImgRect.set(0, 0, bitmaps[0].getWidth(), bitmaps[0].getHeight());

        //value
        valuesResId = typedArray.getResourceId(R.styleable.CornerLayout_element_values, 0);
        if (valuesResId == 0) {
            throw new IllegalArgumentException("ArcColorPicker: error - colors is not specified");
        }
        values = typedArray.getResources().getIntArray(valuesResId);

        typedArray.recycle();

        mRootPaint = new Paint();
        mRootPaint.setAntiAlias(true);
        mRootPaint.setStyle(Paint.Style.FILL);
        mRootPaint.setColor(Color.GRAY);

        mGesture = new GestureDetector(context, this);
        setLongClickable(true);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mChildCount = getChildCount();

        /*
        mChildCount = getChildCount();
        if (mChildCount > 1) {
            float sweepArc = (float) Math.PI / 2.0f / (float) (mChildCount - 1);
            float startArc = (float) Math.PI / 2.0f * 3.0f;

            for (int i = 0; i < mChildCount; i++) {
                View child = getChildAt(i);
                float itemArc = startArc + sweepArc * i;
                double sin = Math.sin(itemArc);
                double cos = Math.cos(itemArc);

                int x = (int) (mRootPoint.x + radius * cos);
                int y = (int) (mRootPoint.y + radius * sin);
                child.layout(x - itemRadius, y - itemRadius, x + itemRadius, y + itemRadius);
            }
        }
        */
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

        mRootPoint.x = getPaddingStart() + itemRadius;
        mRootPoint.y = size - getPaddingBottom() - itemRadius;
        mRootRectF.set(mRootPoint.x - itemRadius,
                mRootPoint.y - itemRadius,
                mRootPoint.x + itemRadius,
                mRootPoint.y + itemRadius);
    }

    private int measureWidth(int defaultWidth, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultWidth = itemRadius + getPaddingLeft() + getPaddingRight();
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
                defaultHeight = itemRadius + getPaddingTop() + getPaddingBottom();
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

        mRootPaint.setColor(Color.GRAY);
        canvas.drawCircle(mRootPoint.x, mRootPoint.y, itemRadius, mRootPaint);
        mRootPaint.setColor(Color.RED);
        canvas.drawCircle(mRootPoint.x, mRootPoint.y, itemRadius - EDGE, mRootPaint);

        canvas.drawBitmap(bitmaps[mIndex], mImgRect, mRootRectF, mRootPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchRadius =
                (float) Math.pow((Math.pow((mRootPoint.x - event.getX()), 2) + Math.pow((mRootPoint.y - event.getY()), 2)), 0.5);
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (touchRadius < (itemRadius + TOUCH_EDGE)) {
                    touch = true;
                    mGesture.onTouchEvent(event);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (touch) {
                    mGesture.onTouchEvent(event);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (touch) {
                    mGesture.onTouchEvent(event);
                    touch = false;
                    return true;
                }
                break;
        }

        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (velocityX > 50f && velocityY > 50f) {
            mIndex = (mIndex + 1) % mChildCount;
            if(l != null) l.onChange(mIndex);

            Log.e(TAG, "onFling,next=" + mIndex);
        } else if (velocityX < -50f && velocityY < -50f) {
            mIndex = (mIndex - 1 + mChildCount) % mChildCount;
            if(l != null) l.onChange(mIndex);

            Log.e(TAG, "onFling,pre=" + mIndex);
        } else if (velocityX > 50f && velocityY < -50f) {
            open = true;
            for (int i = 0; i < mChildCount; i++) {
                View child = getChildAt(i);
                child.setVisibility(View.VISIBLE);
            }


            Log.e(TAG, "onFling,open");
        } else if (velocityX < -50f && velocityY > 50f) {
            open = false;
            for (int i = 0; i < mChildCount; i++) {
                View child = getChildAt(i);
                child.setVisibility(View.GONE);
            }

            Log.e(TAG, "onFling,close");
        } else {
            Log.e(TAG, "onFling,ignore");
        }

        invalidate();
        return false;
    }

    /*
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
    */
}
