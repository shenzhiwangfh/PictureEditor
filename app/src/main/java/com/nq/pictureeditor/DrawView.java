package com.nq.pictureeditor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;

public class DrawView extends View implements ScaleGestureDetector.OnScaleGestureListener {

    private final static String TAG = "DrawView";

    private Context mContext;
    private Resources mRes;
    private final static float MIN_SCALE = 1.2f;
    private final static float MAX_SCALE = 4.0f;
    private int ICON_WIDTH;
    private int ICON_SIZE;
    private int LINE_WIDTH;

    private ScaleGestureDetector mGesture;//用与处理双手的缩放手势
    private float mZoomScale = MIN_SCALE;//默认的缩放比为1

    private ArrayList<Record> mRecords = new ArrayList<>();

    private Paint mOriginPaint;
    private Paint mClipPaint;
    private Paint mCanvasPaint;
    private Paint mIconPaint;

    private Bitmap mBitmap;

    //private int mScreenWidth;
    //private int mScreenHeight;

    //private int mPicturePadding;
    //private int mPictureMaxWidth;
    //private int mPictureMaxHeight;

    private Rect canvasRect;// = new Rect(0, 0, 960, 960);
    private Rect originRect;

    private Rect prePictureRect;
    private Rect pictureRect;
    private Rect clipPictureRect;
    private Rect bitmapRect;
    private Rect clipBitmapRect;

    private Rect clipIconRect;
    private Rect iconLeftTop, iconRightTop, iconLeftBottom, iconRightBottom;
    private Rect lineLeft, lineRight, lineTop, lineBottom;

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int screenWidth = dm.widthPixels;
        final int screenHeight = dm.heightPixels;

        mRes = mContext.getResources();
        final int padding = (int) mRes.getDimension(R.dimen.picture_padding);
        final int maxHeight = (int) mRes.getDimension(R.dimen.picture_max_height);
        canvasRect = new Rect(padding, padding, screenWidth - padding, padding + maxHeight);

        ICON_WIDTH = (int) mRes.getDimension(R.dimen.icon_width);
        ICON_SIZE = (int) mRes.getDimension(R.dimen.icon_size);
        LINE_WIDTH = (int) mRes.getDimension(R.dimen.line_width);
    }

    private void initPaint() {
        mCanvasPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOriginPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mCanvasPaint.setColor(Color.GREEN);
        mOriginPaint.setColor(Color.GRAY);
        mIconPaint.setColor(Color.RED);
    }

    public void init(Bitmap bitmap) {
        mBitmap = bitmap;
        initPaint();

        float bitmapRatio = (float) mBitmap.getHeight() / (float) mBitmap.getWidth();
        float maxRatio = (float) canvasRect.height() / (float) canvasRect.width();
        if (bitmapRatio > maxRatio) {
            //long picture
            int width = (int) (canvasRect.height() / bitmapRatio);
            int left = canvasRect.left + ((canvasRect.width() - width) / 2);
            originRect = new Rect(left, canvasRect.top, left + width, canvasRect.bottom);
        } else {
            //normal picture
            int height = (int) (canvasRect.width() * bitmapRatio);
            int top = canvasRect.top + ((canvasRect.height() - height) / 2);
            originRect = new Rect(canvasRect.left, top, canvasRect.right, top + height);
        }

        setPrePictureRect();
        setPictureRect();
        setClipPictureRect();
        setBitmapRect();
        setClipBitmapRect();
        setClipIconRect(0, 0, 0);
        postInvalidate();

        mGesture = new ScaleGestureDetector(mContext, this);
    }

    private void setPrePictureRect() {
        if (prePictureRect != null) {
            prePictureRect.set(pictureRect);
        } else {
            prePictureRect = new Rect(originRect);
        }
    }

    private void setPictureRect() {
        int left = originRect.left - (int) ((mZoomScale - 1.0f) * originRect.width() / 2);
        int right = originRect.right + (int) ((mZoomScale - 1.0f) * originRect.width() / 2);
        int top = originRect.top - (int) ((mZoomScale - 1.0f) * originRect.height() / 2);
        int bottom = originRect.bottom + (int) ((mZoomScale - 1.0f) * originRect.height() / 2);

        left = left + offsetX + moveOffsetX;
        right = right + offsetX + moveOffsetX;
        top = top + offsetY + moveOffsetY;
        bottom = bottom + offsetY + moveOffsetY;

        if (pictureRect != null) {
            pictureRect.set(left, top, right, bottom);
        } else {
            pictureRect = new Rect(left, top, right, bottom);
        }
        //pictureRect.offset(offsetX + moveOffsetX, offsetY + moveOffsetY);
    }

    private void setClipPictureRect() {
        int left = Math.max(pictureRect.left, canvasRect.left);
        int right = Math.min(pictureRect.right, canvasRect.right);
        int top = Math.max(pictureRect.top, canvasRect.top);
        int bottom = Math.min(pictureRect.bottom, canvasRect.bottom);

        if (clipPictureRect != null) {
            clipPictureRect.set(left, top, right, bottom);
        } else {
            clipPictureRect = new Rect(left, top, right, bottom);
        }
    }

    private void setBitmapRect() {
        if (bitmapRect == null)
            bitmapRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
    }

    private void setClipBitmapRect() {
        float ratio = (float) bitmapRect.height() / (float) pictureRect.height();

        int left = (int) ((float) (clipPictureRect.left - pictureRect.left) * ratio);
        int right = left + (int) (clipPictureRect.width() * ratio);
        int top = (int) ((float) (clipPictureRect.top - pictureRect.top) * ratio);
        int bottom = top + (int) (clipPictureRect.height() * ratio);

        if (clipBitmapRect != null) {
            clipBitmapRect.set(left, top, right, bottom);
        } else {
            clipBitmapRect = new Rect(left, top, right, bottom);
        }
    }

    private void setClipIconRect(int type, int x, int y) {
        int left, right, top, bottom;

        if (clipIconRect == null) {
            left = clipPictureRect.left - ICON_WIDTH;
            top = clipPictureRect.top - ICON_WIDTH;
            right = clipPictureRect.right + ICON_WIDTH;
            bottom = clipPictureRect.bottom + ICON_WIDTH;
            clipIconRect = new Rect(left, top, right, bottom);
            //return;
        } else {
            left = clipPictureRect.left - ICON_WIDTH;
            top = clipPictureRect.top - ICON_WIDTH;
            right = clipPictureRect.right + ICON_WIDTH;
            bottom = clipPictureRect.bottom + ICON_WIDTH;
            clipIconRect.set(left, top, right, bottom);
        }

        setIconRect();
        setLineRect();
    }

    private void setIconRect() {
        int left, right, top, bottom;

        //left_top icon
        left = clipIconRect.left;
        right = left + ICON_SIZE;
        top = clipIconRect.top;
        bottom = top + ICON_SIZE;
        if (iconLeftTop != null) {
            iconLeftTop.set(left, top, right, bottom);
        } else {
            iconLeftTop = new Rect(left, top, right, bottom);
        }

        //right_top icon
        left = clipIconRect.right - ICON_SIZE;
        right = left + ICON_SIZE;
        top = clipIconRect.top;
        bottom = top + ICON_SIZE;
        if (iconRightTop != null) {
            iconRightTop.set(left, top, right, bottom);
        } else {
            iconRightTop = new Rect(left, top, right, bottom);
        }

        //left_bottom icon
        left = clipIconRect.left;
        right = left + ICON_SIZE;
        top = clipIconRect.bottom - ICON_SIZE;
        bottom = top + ICON_SIZE;
        if (iconLeftBottom != null) {
            iconLeftBottom.set(left, top, right, bottom);
        } else {
            iconLeftBottom = new Rect(left, top, right, bottom);
        }

        //right_bottom icon
        left = clipIconRect.right - ICON_SIZE;
        right = left + ICON_SIZE;
        top = clipIconRect.bottom - ICON_SIZE;
        bottom = top + ICON_SIZE;
        if (iconRightBottom != null) {
            iconRightBottom.set(left, top, right, bottom);
        } else {
            iconRightBottom = new Rect(left, top, right, bottom);
        }
    }

    private void setLineRect() {
        int left, right, top, bottom;

        //left
        left = clipIconRect.left + ICON_WIDTH - LINE_WIDTH;
        right = left + LINE_WIDTH;
        top = clipIconRect.top + ICON_SIZE;
        bottom = clipIconRect.bottom - ICON_SIZE;
        if (lineLeft != null) {
            lineLeft.set(left, top, right, bottom);
        } else {
            lineLeft = new Rect(left, top, right, bottom);
        }

        //right
        left = clipIconRect.right - ICON_WIDTH;
        right = left + LINE_WIDTH;
        top = clipIconRect.top + ICON_SIZE;
        bottom = clipIconRect.bottom - ICON_SIZE;
        if (lineRight != null) {
            lineRight.set(left, top, right, bottom);
        } else {
            lineRight = new Rect(left, top, right, bottom);
        }

        //top
        left = clipIconRect.left + ICON_SIZE;
        right = clipIconRect.right - ICON_SIZE;
        top = clipIconRect.top + ICON_WIDTH - LINE_WIDTH;
        bottom = top + LINE_WIDTH;
        if (lineTop != null) {
            lineTop.set(left, top, right, bottom);
        } else {
            lineTop = new Rect(left, top, right, bottom);
        }

        //bottom
        left = clipIconRect.left + ICON_SIZE;
        right = clipIconRect.right - ICON_SIZE;
        top = clipIconRect.bottom - ICON_WIDTH;
        bottom = clipIconRect.bottom - ICON_WIDTH + LINE_WIDTH;
        if (lineBottom != null) {
            lineBottom.set(left, top, right, bottom);
        } else {
            lineBottom = new Rect(left, top, right, bottom);
        }
    }

    private final static int MODE_NONE = -1;
    private final static int MODE_PICTURE = 0;
    private final static int MODE_LT_ICON = 1;
    private final static int MODE_RT_ICON = 2;
    private final static int MODE_LB_ICON = 3;
    private final static int MODE_RB_ICON = 4;
    private final static int MODE_L_LINE = 5;
    private final static int MODE_R_LINE = 6;
    private final static int MODE_T_LINE = 7;
    private final static int MODE_B_LINE = 8;
    private int moveMode = MODE_NONE;
    private int downX, downY;
    private int offsetX = 0, offsetY = 0;
    private int moveOffsetX = 0, moveOffsetY = 0;
    private Rect tmpRect = new Rect();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() <= 1) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    downX = (int) event.getX();
                    downY = (int) event.getY();
                    Log.e(TAG, "ACTION_DOWN," + downX + "," + downY);

                    if (iconLeftTop.contains(downX, downY)) {
                        moveMode = MODE_LT_ICON;
                        tmpRect.set(clipIconRect);
                    } else if (iconRightTop.contains(downX, downY)) {
                        moveMode = MODE_RT_ICON;
                        tmpRect.set(clipIconRect);
                    } else if (iconLeftBottom.contains(downX, downY)) {
                        moveMode = MODE_LB_ICON;
                        tmpRect.set(clipIconRect);
                    } else if (iconRightBottom.contains(downX, downY)) {
                        moveMode = MODE_RB_ICON;
                        tmpRect.set(clipIconRect);
                    } else if (lineLeft.contains(downX, downY)) {
                        moveMode = MODE_L_LINE;
                        tmpRect.set(clipIconRect);
                    } else if (lineRight.contains(downX, downY)) {
                        moveMode = MODE_R_LINE;
                        tmpRect.set(clipIconRect);
                    } else if (lineTop.contains(downX, downY)) {
                        moveMode = MODE_T_LINE;
                        tmpRect.set(clipIconRect);
                    } else if (lineBottom.contains(downX, downY)) {
                        moveMode = MODE_B_LINE;
                        tmpRect.set(clipIconRect);
                    } else if (clipPictureRect.contains(downX, downY)) {
                        moveMode = MODE_PICTURE;
                        tmpRect.set(pictureRect);
                    }

                    Log.e(TAG, "moveMode=" + moveMode);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (moveMode == MODE_PICTURE) {
                        moveOffsetX = (int) (event.getX() - downX);
                        moveOffsetY = (int) (event.getY() - downY);

                        setPictureRect();
                        //setClipPictureRect();
                        //setClipBitmapRect();
                        postInvalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (moveMode == MODE_PICTURE) {
                        moveOffsetX = (int) (event.getX() - downX);
                        moveOffsetY = (int) (event.getY() - downY);

                        setPictureRect();
                        //setClipPictureRect();
                        //setClipBitmapRect();
                        postInvalidate();
                    }

                    //moveMode = MODE_NONE;
                    //cluRect();

                    mHandler.sendEmptyMessageDelayed(1, 1000);
                    break;
            }
        } else {
            for (int i = 0; i < event.getPointerCount(); i++) {
                int x = (int) event.getX(i);
                int y = (int) event.getY(i);
                if (!clipPictureRect.contains(x, y)) {
                    Log.e(TAG, "out of CLIP!!");
                    return false;
                }
            }
            return mGesture.onTouchEvent(event);
        }
        return true;
    }

    /*
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //由ViewDragHelper处理拦截
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }
    */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        canvas.drawRect(canvasRect, mCanvasPaint);

        mOriginPaint.setAlpha(255);
        canvas.drawBitmap(mBitmap, bitmapRect, pictureRect, mOriginPaint);

        switch (moveMode) {
            case MODE_NONE:
                mOriginPaint.setAlpha(100); // setColor 和 setAlpha 的顺序有关系
                canvas.drawRect(pictureRect, mOriginPaint);

                canvas.drawBitmap(mBitmap, clipBitmapRect, clipPictureRect, mClipPaint);
                break;
            case MODE_PICTURE:
                break;
        }

        //draw icon
        canvas.drawRect(iconLeftTop.left, iconLeftTop.top, iconLeftTop.right, iconLeftTop.top + ICON_WIDTH, mIconPaint);
        canvas.drawRect(iconLeftTop.left, iconLeftTop.top, iconLeftTop.left + ICON_WIDTH, iconLeftTop.bottom, mIconPaint);
        canvas.drawRect(iconRightTop.left, iconRightTop.top, iconRightTop.right, iconLeftTop.top + ICON_WIDTH, mIconPaint);
        canvas.drawRect(iconRightTop.right - ICON_WIDTH, iconRightTop.top, iconRightTop.right, iconLeftTop.bottom, mIconPaint);
        canvas.drawRect(iconLeftBottom.left, iconLeftBottom.bottom - ICON_WIDTH, iconLeftBottom.right, iconLeftBottom.bottom, mIconPaint);
        canvas.drawRect(iconLeftBottom.left, iconLeftBottom.top, iconLeftBottom.left + ICON_WIDTH, iconLeftBottom.bottom, mIconPaint);
        canvas.drawRect(iconRightBottom.left, iconRightBottom.bottom - ICON_WIDTH, iconRightBottom.right, iconRightBottom.bottom, mIconPaint);
        canvas.drawRect(iconRightBottom.right - ICON_WIDTH, iconRightBottom.top, iconRightBottom.right, iconRightBottom.bottom, mIconPaint);

        //draw line
        canvas.drawRect(lineLeft, mIconPaint);
        canvas.drawRect(lineRight, mIconPaint);
        canvas.drawRect(lineTop, mIconPaint);
        canvas.drawRect(lineBottom, mIconPaint);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float previousSpan = detector.getPreviousSpan();//缩放发生前的两点距离
        float currentSpan = detector.getCurrentSpan();//缩放发生时的两点距离
        if (previousSpan < currentSpan) { //放大
            mZoomScale = mZoomScale + (currentSpan - previousSpan) / previousSpan;
        } else {
            mZoomScale = mZoomScale - (previousSpan - currentSpan) / previousSpan;
        }
        //确保放大最多为2倍，最少不能小于原图
        if (mZoomScale > MAX_SCALE) {
            mZoomScale = MAX_SCALE;
        } else if (mZoomScale < MIN_SCALE) {
            mZoomScale = MIN_SCALE;
        }

        setPictureRect();
        //setClipPictureRect();
        //setClipBitmapRect();
        //setClipIconRect(0, 0, 0);

        postInvalidate();
        //setScaleX(mZoomScale);
        //setScaleY(mZoomScale);
        //这里调用的是本自定义View的方法，是对本自定义view进行的缩放
        /*在这里调用getChildView（index）的进行缩放，虽然控件显示大小改变了，但是在ViewDragHelper的回调方法中获得的View child的getWidth（）和getHeigit（）是原来的大小，不会发生改变*/
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        setPictureRect();
        setClipPictureRect();
        setClipBitmapRect();
        setClipIconRect(0, 0, 0);
        postInvalidate();

        //cluRect();
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case 1:
                    cluRect();
                    break;
            }

            return false;
        }
    });

    private void cluRect() {
        Log.e(TAG, "pictureRect1=" + pictureRect.left + "," + pictureRect.right + "," + pictureRect.top + "," + pictureRect.bottom);
        Log.e(TAG, "clipPictureRect=" + clipPictureRect.left + "," + clipPictureRect.right + "," + clipPictureRect.top + "," + clipPictureRect.bottom);

        if (pictureRect.contains(clipPictureRect)) {
            Log.e(TAG, "contains");
            return;
        } else {
            Log.e(TAG, "NOT contains1=" + moveOffsetX + "," + moveOffsetY);

            if (pictureRect.left > clipPictureRect.left) {
                moveOffsetX = moveOffsetX + clipPictureRect.left - pictureRect.left;
            }
            if (pictureRect.right < clipPictureRect.right) {
                moveOffsetX = moveOffsetX + clipPictureRect.right - pictureRect.right;
            }
            if (pictureRect.top > clipPictureRect.top) {
                moveOffsetY = moveOffsetY + clipPictureRect.top - pictureRect.top;
            }
            if (pictureRect.bottom < clipPictureRect.bottom) {
                moveOffsetY = moveOffsetY + clipPictureRect.bottom - pictureRect.bottom;
            }

            Log.e(TAG, "NOT contains2=" + moveOffsetX + "," + moveOffsetY);

            setPictureRect();
            //Log.e(TAG, "pictureRect2=" + pictureRect.left + "," + pictureRect.right + "," + pictureRect.top + "," + pictureRect.bottom);

            setClipPictureRect();
            setClipBitmapRect();
            setClipIconRect(0, 0, 0);
            postInvalidate();
        }

        offsetX = offsetX + moveOffsetX;
        offsetY = offsetY + moveOffsetY;
        moveOffsetX = 0;
        moveOffsetY = 0;

        moveMode = MODE_NONE;
    }
}
