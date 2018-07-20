package com.nq.pictureeditor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
    private final static float MIN_SCALE = 0.5f;
    private final static float NORMAL_SCALE = 1.0f;
    private final static float MAX_SCALE = 100.0f;
    private float ICON_WIDTH;
    private float ICON_SIZE;
    private float LINE_WIDTH;
    private float CLIP_MIN_SIZE;

    private ScaleGestureDetector mGesture;//用与处理双手的缩放手势
    private float mZoomScale = NORMAL_SCALE;//默认的缩放比为1

    private ArrayList<Record> mRecords = new ArrayList<>();
    private int mIndex = -1;

    private Paint mOriginPaint;
    private Paint mClipPaint;
    //private Paint mCanvasPaint;
    private Paint mIconPaint;
    private Paint mPenPaint;

    private Bitmap mBitmap;

    private RectF canvasRect;// = new RectF(0, 0, 960, 960);
    private RectF originRect;

    private RectF prePictureRect;
    private RectF pictureRect;
    private RectF clipPictureRect;
    private Rect bitmapRect;
    private Rect clipBitmapRect;

    private RectF clipIconRect;
    private RectF tmpClipIconRect;
    private RectF iconLeftTop, iconRightTop, iconLeftBottom, iconRightBottom;
    private RectF lineLeft, lineRight, lineTop, lineBottom;

    private Path mPenPath;
    private Canvas mDrawCanvas;
    private Bitmap mDrawBitmap;
    private boolean mDrawing = false;

    private final static int MODE_CLIP = 0x10;
    private final static int MODE_PICTURE = 0x11;
    private final static int MODE_SCALE = 0x12;
    private final static int MODE_LT_ICON = 0x13;
    private final static int MODE_RT_ICON = 0x14;
    private final static int MODE_LB_ICON = 0x15;
    private final static int MODE_RB_ICON = 0x16;
    private final static int MODE_L_LINE = 0x17;
    private final static int MODE_R_LINE = 0x18;
    private final static int MODE_T_LINE = 0x19;
    private final static int MODE_B_LINE = 0x1A;
    private final static int MODE_PEN = 0x20;
    private int moveMode = MODE_CLIP;
    private float downX, downY;
    private float picOffsetX = 0.0f, picOffsetY = 0.0f;

    private static final float PEN_MIN_MOVE = 4.0f;

    public interface DrawInterface {
        void finishAction(boolean back, boolean forward);

        void save(Bitmap newBitmap);
    }

    private DrawInterface draw;

    public void setFinishDraw(DrawInterface draw) {
        this.draw = draw;
    }

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
        final float paddingTop = mRes.getDimension(R.dimen.picture_top_padding);
        final float paddingLeft = mRes.getDimension(R.dimen.picture_left_padding);
        final float maxHeight = mRes.getDimension(R.dimen.picture_max_height);
        canvasRect = new RectF(paddingLeft, paddingTop, screenWidth - paddingLeft, paddingTop + maxHeight);

        ICON_WIDTH = mRes.getDimension(R.dimen.icon_width);
        ICON_SIZE = mRes.getDimension(R.dimen.icon_size);
        LINE_WIDTH = mRes.getDimension(R.dimen.line_width);
        CLIP_MIN_SIZE = mRes.getDimension(R.dimen.clip_min_size);
    }

    private void initPaint() {
        //mCanvasPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOriginPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPenPaint = new Paint();
        mPenPaint.setAntiAlias(true);
        mPenPaint.setStyle(Paint.Style.STROKE);
        mPenPaint.setStrokeCap(Paint.Cap.ROUND);
        mPenPaint.setStrokeJoin(Paint.Join.ROUND);
        mPenPaint.setColor(Color.BLACK);
        mPenPaint.setStrokeWidth(10);

        //mCanvasPaint.setColor(Color.GREEN);
        mOriginPaint.setColor(Color.GRAY);
        mIconPaint.setColor(Color.RED);
    }

    public void init(Bitmap bitmap) {
        mBitmap = bitmap;
        initPaint();
        mPenPath = new Path();
        mDrawCanvas = new Canvas();
        //mDrawCanvas.setBitmap(mBitmap);

        float bitmapRatio = (float) mBitmap.getHeight() / (float) mBitmap.getWidth();
        float maxRatio = canvasRect.height() / canvasRect.width();
        if (bitmapRatio > maxRatio) {
            //long picture
            float width = canvasRect.height() / bitmapRatio;
            float left = canvasRect.left + ((canvasRect.width() - width) / 2);
            originRect = new RectF(left, canvasRect.top, left + width, canvasRect.bottom);
        } else {
            //normal picture
            float height = canvasRect.width() * bitmapRatio;
            float top = canvasRect.top + ((canvasRect.height() - height) / 2);
            originRect = new RectF(canvasRect.left, top, canvasRect.right, top + height);
        }

        setPrePictureRect();
        setPictureRect();
        setClipPictureRect();
        setBitmapRect();
        setClipBitmapRect();
        setClipIconRect(0, 0);
        postInvalidate();
        mGesture = new ScaleGestureDetector(mContext, this);
        addRecord(MODE_CLIP);
    }

    private void setPrePictureRect() {
        if (prePictureRect != null) {
            prePictureRect.set(pictureRect);
        } else {
            prePictureRect = new RectF(originRect);
        }
    }

    private void setPictureRect() {
        float oldWidth = prePictureRect.width();
        float oldHeight = prePictureRect.height();
        float newWidth = originRect.width() * mZoomScale;
        float newHeight = originRect.height() * mZoomScale;

        float left = prePictureRect.left - (newWidth - oldWidth) / 2 + picOffsetX;
        float right = prePictureRect.right + (newWidth - oldWidth) / 2 + picOffsetX;
        float top = prePictureRect.top - (newHeight - oldHeight) / 2 + picOffsetY;
        float bottom = prePictureRect.bottom + (newHeight - oldHeight) / 2 + picOffsetY;
        if (pictureRect != null) {
            pictureRect.set(left, top, right, bottom);
        } else {
            pictureRect = new RectF(left, top, right, bottom);
        }
    }

    private void setClipPictureRect() {
        float left = Math.max(pictureRect.left, canvasRect.left);
        float right = Math.min(pictureRect.right, canvasRect.right);
        float top = Math.max(pictureRect.top, canvasRect.top);
        float bottom = Math.min(pictureRect.bottom, canvasRect.bottom);

        if (clipPictureRect != null) {
            clipPictureRect.set(left, top, right, bottom);
        } else {
            clipPictureRect = new RectF(left, top, right, bottom);
        }
    }

    private void setBitmapRect() {
        if (bitmapRect == null)
            bitmapRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
    }

    private void setClipBitmapRect() {
        float ratio = bitmapRect.height() / pictureRect.height();

        int left = (int) ((clipPictureRect.left - pictureRect.left) * ratio);
        int right = left + (int) (clipPictureRect.width() * ratio);
        int top = (int) ((clipPictureRect.top - pictureRect.top) * ratio);
        int bottom = top + (int) (clipPictureRect.height() * ratio);

        if (clipBitmapRect != null) {
            clipBitmapRect.set(left, top, right, bottom);
        } else {
            clipBitmapRect = new Rect(left, top, right, bottom);
        }
    }

    private void setClipIconRect(float offsetX, float offsetY) {
        float left, right, top, bottom;

        if (clipIconRect == null) {
            left = clipPictureRect.left - ICON_WIDTH;
            top = clipPictureRect.top - ICON_WIDTH;
            right = clipPictureRect.right + ICON_WIDTH;
            bottom = clipPictureRect.bottom + ICON_WIDTH;
            clipIconRect = new RectF(left, top, right, bottom);
            //return;
            tmpClipIconRect = new RectF(clipIconRect);
        } else {
            if (moveMode == MODE_CLIP) {
                left = clipPictureRect.left - ICON_WIDTH;
                top = clipPictureRect.top - ICON_WIDTH;
                right = clipPictureRect.right + ICON_WIDTH;
                bottom = clipPictureRect.bottom + ICON_WIDTH;
                clipIconRect.set(left, top, right, bottom);
            } else if (moveMode == MODE_LT_ICON) {
                left = offsetX + clipPictureRect.left - ICON_WIDTH;
                top = offsetY + clipPictureRect.top - ICON_WIDTH;
                clipIconRect.set(left, top, clipIconRect.right, clipIconRect.bottom);
            } else if (moveMode == MODE_RT_ICON) {
                right = offsetX + clipPictureRect.right + ICON_WIDTH;
                top = offsetY + clipPictureRect.top - ICON_WIDTH;
                clipIconRect.set(clipIconRect.left, top, right, clipIconRect.bottom);
            } else if (moveMode == MODE_LB_ICON) {
                left = offsetX + clipPictureRect.left - ICON_WIDTH;
                bottom = offsetY + clipPictureRect.bottom + ICON_WIDTH;
                clipIconRect.set(left, clipIconRect.top, clipIconRect.right, bottom);
            } else if (moveMode == MODE_RB_ICON) {
                right = offsetX + clipPictureRect.right + ICON_WIDTH;
                bottom = offsetY + clipPictureRect.bottom + ICON_WIDTH;
                clipIconRect.set(clipIconRect.left, clipIconRect.top, right, bottom);
            }
        }
        setIconRect();
        setLineRect();
    }

    private void setIconRect() {
        float left, right, top, bottom;

        //left_top icon
        left = clipIconRect.left;
        right = left + ICON_SIZE;
        top = clipIconRect.top;
        bottom = top + ICON_SIZE;
        if (iconLeftTop != null) {
            iconLeftTop.set(left, top, right, bottom);
        } else {
            iconLeftTop = new RectF(left, top, right, bottom);
        }

        //right_top icon
        left = clipIconRect.right - ICON_SIZE;
        right = left + ICON_SIZE;
        top = clipIconRect.top;
        bottom = top + ICON_SIZE;
        if (iconRightTop != null) {
            iconRightTop.set(left, top, right, bottom);
        } else {
            iconRightTop = new RectF(left, top, right, bottom);
        }

        //left_bottom icon
        left = clipIconRect.left;
        right = left + ICON_SIZE;
        top = clipIconRect.bottom - ICON_SIZE;
        bottom = top + ICON_SIZE;
        if (iconLeftBottom != null) {
            iconLeftBottom.set(left, top, right, bottom);
        } else {
            iconLeftBottom = new RectF(left, top, right, bottom);
        }

        //right_bottom icon
        left = clipIconRect.right - ICON_SIZE;
        right = left + ICON_SIZE;
        top = clipIconRect.bottom - ICON_SIZE;
        bottom = top + ICON_SIZE;
        if (iconRightBottom != null) {
            iconRightBottom.set(left, top, right, bottom);
        } else {
            iconRightBottom = new RectF(left, top, right, bottom);
        }
    }

    private void setLineRect() {
        float left, right, top, bottom;

        //left
        left = clipIconRect.left + ICON_WIDTH - LINE_WIDTH;
        right = left + LINE_WIDTH;
        top = clipIconRect.top + ICON_SIZE;
        bottom = clipIconRect.bottom - ICON_SIZE;
        if (lineLeft != null) {
            lineLeft.set(left, top, right, bottom);
        } else {
            lineLeft = new RectF(left, top, right, bottom);
        }

        //right
        left = clipIconRect.right - ICON_WIDTH;
        right = left + LINE_WIDTH;
        top = clipIconRect.top + ICON_SIZE;
        bottom = clipIconRect.bottom - ICON_SIZE;
        if (lineRight != null) {
            lineRight.set(left, top, right, bottom);
        } else {
            lineRight = new RectF(left, top, right, bottom);
        }

        //top
        left = clipIconRect.left + ICON_SIZE;
        right = clipIconRect.right - ICON_SIZE;
        top = clipIconRect.top + ICON_WIDTH - LINE_WIDTH;
        bottom = top + LINE_WIDTH;
        if (lineTop != null) {
            lineTop.set(left, top, right, bottom);
        } else {
            lineTop = new RectF(left, top, right, bottom);
        }

        //bottom
        left = clipIconRect.left + ICON_SIZE;
        right = clipIconRect.right - ICON_SIZE;
        top = clipIconRect.bottom - ICON_WIDTH;
        bottom = clipIconRect.bottom - ICON_WIDTH + LINE_WIDTH;
        if (lineBottom != null) {
            lineBottom.set(left, top, right, bottom);
        } else {
            lineBottom = new RectF(left, top, right, bottom);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() <= 1) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    downY = event.getY();
                    Log.e(TAG, "ACTION_DOWN," + downX + "," + downY);

                    if ((moveMode & MODE_CLIP) == MODE_CLIP) {
                        if (iconLeftTop.contains(downX, downY)) {
                            moveMode = MODE_LT_ICON;
                            tmpClipIconRect.set(clipIconRect);
                        } else if (iconRightTop.contains(downX, downY)) {
                            moveMode = MODE_RT_ICON;
                            tmpClipIconRect.set(clipIconRect);
                        } else if (iconLeftBottom.contains(downX, downY)) {
                            moveMode = MODE_LB_ICON;
                            tmpClipIconRect.set(clipIconRect);
                        } else if (iconRightBottom.contains(downX, downY)) {
                            moveMode = MODE_RB_ICON;
                            tmpClipIconRect.set(clipIconRect);
                        } else if (lineLeft.contains(downX, downY)) {
                            moveMode = MODE_L_LINE;
                            tmpClipIconRect.set(clipIconRect);
                        } else if (lineRight.contains(downX, downY)) {
                            moveMode = MODE_R_LINE;
                            tmpClipIconRect.set(clipIconRect);
                        } else if (lineTop.contains(downX, downY)) {
                            moveMode = MODE_T_LINE;
                            tmpClipIconRect.set(clipIconRect);
                        } else if (lineBottom.contains(downX, downY)) {
                            moveMode = MODE_B_LINE;
                            tmpClipIconRect.set(clipIconRect);
                        } else if (clipPictureRect.contains(downX, downY)) {
                            moveMode = MODE_PICTURE;
                            tmpClipIconRect.set(pictureRect);
                        }
                        //postInvalidate();
                    } else if ((moveMode & MODE_PEN) == MODE_PEN) {
                        mPenPath.reset();
                        mPenPath.moveTo(downX, downY);
                        mDrawing = true;
                        //mDrawCanvas.clipRect(clipPictureRect);
                        //mDrawCanvas.drawPath(mPenPath, mPenPaint);
                        invalidate();
                    }
                    Log.e(TAG, "moveMode=" + moveMode);
                }
                break;
                case MotionEvent.ACTION_MOVE: {
                    if (moveMode == MODE_LT_ICON) {
                        float clipOffsetX = event.getX() - downX;
                        float clipOffsetY = event.getY() - downY;

                        if ((clipPictureRect.right - clipPictureRect.left - clipOffsetX) < CLIP_MIN_SIZE) {
                            clipOffsetX = clipPictureRect.right - clipPictureRect.left - CLIP_MIN_SIZE;
                        }
                        if ((clipPictureRect.bottom - clipPictureRect.top - clipOffsetY) < CLIP_MIN_SIZE) {
                            clipOffsetY = clipPictureRect.bottom - clipPictureRect.top - CLIP_MIN_SIZE;
                        }
                        float left = Math.max(canvasRect.left, pictureRect.left);
                        float top = Math.max(canvasRect.top, pictureRect.top);
                        if (clipPictureRect.left + clipOffsetX < left)
                            clipOffsetX = left - clipPictureRect.left;
                        if (clipPictureRect.top + clipOffsetY < top)
                            clipOffsetY = top - clipPictureRect.top;

                        setClipIconRect(clipOffsetX, clipOffsetY);
                        postInvalidate();
                    } else if (moveMode == MODE_RT_ICON) {
                        float clipOffsetX = event.getX() - downX;
                        float clipOffsetY = event.getY() - downY;

                        if ((clipPictureRect.right - clipPictureRect.left + clipOffsetX) < CLIP_MIN_SIZE) {
                            clipOffsetX = clipPictureRect.left - clipPictureRect.right + CLIP_MIN_SIZE;
                        }
                        if ((clipPictureRect.bottom - clipPictureRect.top - clipOffsetY) < CLIP_MIN_SIZE) {
                            clipOffsetY = clipPictureRect.bottom - clipPictureRect.top - CLIP_MIN_SIZE;
                        }
                        float right = Math.min(canvasRect.right, pictureRect.right);
                        float top = Math.max(canvasRect.top, pictureRect.top);
                        if (clipPictureRect.right + clipOffsetX > right)
                            clipOffsetX = right - clipPictureRect.right;
                        if (clipPictureRect.top + clipOffsetY < top)
                            clipOffsetY = top - clipPictureRect.top;

                        setClipIconRect(clipOffsetX, clipOffsetY);
                        postInvalidate();
                    } else if (moveMode == MODE_LB_ICON) {
                        float clipOffsetX = event.getX() - downX;
                        float clipOffsetY = event.getY() - downY;

                        if ((clipPictureRect.right - clipPictureRect.left - clipOffsetX) < CLIP_MIN_SIZE) {
                            clipOffsetX = clipPictureRect.right - clipPictureRect.left - CLIP_MIN_SIZE;
                        }
                        if ((clipPictureRect.bottom - clipPictureRect.top + clipOffsetY) < CLIP_MIN_SIZE) {
                            clipOffsetY = clipPictureRect.top - clipPictureRect.bottom + CLIP_MIN_SIZE;
                        }
                        float left = Math.max(canvasRect.left, pictureRect.left);
                        float bottom = Math.min(canvasRect.bottom, pictureRect.bottom);
                        if (clipPictureRect.left + clipOffsetX < left)
                            clipOffsetX = left - clipPictureRect.left;
                        if (clipPictureRect.bottom + clipOffsetY > bottom)
                            clipOffsetY = bottom - clipPictureRect.bottom;

                        setClipIconRect(clipOffsetX, clipOffsetY);
                        postInvalidate();
                    } else if (moveMode == MODE_RB_ICON) {
                        float clipOffsetX = event.getX() - downX;
                        float clipOffsetY = event.getY() - downY;

                        if ((clipPictureRect.right - clipPictureRect.left + clipOffsetX) < CLIP_MIN_SIZE) {
                            clipOffsetX = clipPictureRect.left - clipPictureRect.right + CLIP_MIN_SIZE;
                        }
                        if ((clipPictureRect.bottom - clipPictureRect.top + clipOffsetY) < CLIP_MIN_SIZE) {
                            clipOffsetY = clipPictureRect.top - clipPictureRect.bottom + CLIP_MIN_SIZE;
                        }
                        float right = Math.min(canvasRect.right, pictureRect.right);
                        float bottom = Math.min(canvasRect.bottom, pictureRect.bottom);
                        if (clipPictureRect.right + clipOffsetX > right)
                            clipOffsetX = right - clipPictureRect.right;
                        if (clipPictureRect.bottom + clipOffsetY > bottom)
                            clipOffsetY = bottom - clipPictureRect.bottom;

                        setClipIconRect(clipOffsetX, clipOffsetY);
                        postInvalidate();
                    } else if (moveMode == MODE_PICTURE) {
                        picOffsetX = event.getX() - downX;
                        picOffsetY = event.getY() - downY;

                        setPictureRect();
                        postInvalidate();
                    } else if (moveMode == MODE_PEN) {
                        float x = event.getX();
                        float y = event.getY();
                        float dx = Math.abs(downX - x);
                        float dy = Math.abs(downY - y);
                        if (dx > PEN_MIN_MOVE || dy > PEN_MIN_MOVE) {
                            mPenPath.quadTo(downX, downY, (x + downX) / 2, (y + downY) / 2);
                            downX = x;
                            downY = y;
                            //mDrawCanvas.clipRect(clipPictureRect);
                            //mDrawCanvas.drawPath(mPenPath, mPenPaint);
                            invalidate();
                        }
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {
                    Log.e(TAG, "ACTION_UP=" + moveMode);
                    if ((moveMode & MODE_CLIP) == MODE_CLIP) {
                        cluRect();
                    } else if ((moveMode & MODE_PEN) == MODE_PEN) {
                        mPenPath.lineTo(downX, downY);
                        //mDrawCanvas.clipRect(clipPictureRect);
                        //mDrawCanvas.drawPath(mPenPath, mPenPaint);
                        addRecord(MODE_PEN);

                        mDrawing = false;
                        invalidate();
                    }
                    //mHandler.sendEmptyMessageDelayed(1, 1000);
                }
                break;
            }
        } else {
            //Log.e(TAG, "mGesture");
            if ((moveMode & MODE_CLIP) == MODE_CLIP) {
                for (int i = 0; i < event.getPointerCount(); i++) {
                    float x = event.getX(i);
                    float y = event.getY(i);
                    if (!pictureRect.contains(x, y)) {
                        //Log.e(TAG, "out of CLIP!!");
                        return false;
                    }
                }
                return mGesture.onTouchEvent(event);
            }
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawColor(Color.GRAY);
        //canvas.drawRect(canvasRect, mCanvasPaint);

        if (moveMode == MODE_PEN) {
            canvas.clipRect(clipPictureRect);
        }
        mOriginPaint.setAlpha(255);
        canvas.drawBitmap(mBitmap, bitmapRect, pictureRect, mOriginPaint);

        if (moveMode == MODE_CLIP) {
            mOriginPaint.setColor(Color.GRAY);
            mOriginPaint.setAlpha(200); // setColor 和 setAlpha 的顺序有关系
            canvas.drawRect(pictureRect, mOriginPaint);
            canvas.drawBitmap(mBitmap, clipBitmapRect, clipPictureRect, mClipPaint);
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

        canvas.clipRect(clipPictureRect);

        if(mDrawing) canvas.drawPath(mPenPath, mPenPaint);
        for (int i = 0; i <= mIndex; i++) {

            Log.e(TAG, "i=" + i + ",mIndex=" + mIndex);

            Record record = mRecords.get(i);
            if (record instanceof PenRecord) {

                Log.e(TAG, "PenRecord");

                PenRecord penRecord = (PenRecord) record;
                canvas.drawPath(penRecord.path, penRecord.paint);
            }
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float previousSpan = detector.getPreviousSpan(); //缩放发生前的两点距离
        float currentSpan = detector.getCurrentSpan(); //缩放发生时的两点距离
        if (previousSpan < currentSpan) { //放大
            mZoomScale = mZoomScale + (currentSpan - previousSpan) / previousSpan;
        } else {
            mZoomScale = mZoomScale - (previousSpan - currentSpan) / previousSpan;
        }

        if (mZoomScale > MAX_SCALE) {
            mZoomScale = MAX_SCALE;
        } else if (mZoomScale < MIN_SCALE) {
            mZoomScale = MIN_SCALE;
        }

        setPictureRect();
        postInvalidate();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        moveMode = MODE_SCALE;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        //cluRect();
        Log.e(TAG, "onScaleEnd");
    }

    private void cluRect() {
        if (moveMode == MODE_SCALE) {
            //int mode = moveMode;
            moveMode = MODE_CLIP;
            float widthScale = 1.0f, heightScale = 1.0f;
            if (clipPictureRect.width() > pictureRect.width()) {
                widthScale = clipPictureRect.width() / pictureRect.width();
            }
            if (clipPictureRect.height() > pictureRect.height()) {
                heightScale = clipPictureRect.height() / pictureRect.height();
            }
            float zoomScale = Math.max(widthScale, heightScale);
            mZoomScale = pictureRect.height() * zoomScale / originRect.height();
            setPictureRect();
            //setClipBitmapRect();

            if (pictureRect.left > clipPictureRect.left) {
                picOffsetX = picOffsetX + clipPictureRect.left - pictureRect.left;
            }
            if (pictureRect.right < clipPictureRect.right) {
                picOffsetX = picOffsetX + clipPictureRect.right - pictureRect.right;
            }
            if (pictureRect.top > clipPictureRect.top) {
                picOffsetY = picOffsetY + clipPictureRect.top - pictureRect.top;
            }
            if (pictureRect.bottom < clipPictureRect.bottom) {
                picOffsetY = picOffsetY + clipPictureRect.bottom - pictureRect.bottom;
            }
            setPictureRect();
            setClipBitmapRect();

            postInvalidate();
        } else if (moveMode == MODE_PICTURE) {
            //int mode = moveMode;
            moveMode = MODE_CLIP;
            if (pictureRect.left > clipPictureRect.left) {
                picOffsetX = picOffsetX + clipPictureRect.left - pictureRect.left;
            }
            if (pictureRect.right < clipPictureRect.right) {
                picOffsetX = picOffsetX + clipPictureRect.right - pictureRect.right;
            }
            if (pictureRect.top > clipPictureRect.top) {
                picOffsetY = picOffsetY + clipPictureRect.top - pictureRect.top;
            }
            if (pictureRect.bottom < clipPictureRect.bottom) {
                picOffsetY = picOffsetY + clipPictureRect.bottom - pictureRect.bottom;
            }
            setPictureRect();
            //setClipPictureRect();
            setClipBitmapRect();
            //setClipIconRect(0, 0);
            postInvalidate();
        } else if (moveMode >= MODE_LT_ICON && moveMode <= MODE_B_LINE) {
            //把裁剪区域放大后，计算picture 的rect，其他就全部可以顺势算出了
            moveMode = MODE_CLIP;
            float left, top, right, bottom;

            RectF tmpClip = new RectF(clipIconRect.left + ICON_WIDTH,
                    clipIconRect.top + ICON_WIDTH,
                    clipIconRect.right - ICON_WIDTH,
                    clipIconRect.bottom - ICON_WIDTH);
            RectF tmpClipLarge = new RectF(tmpClip);
            float ratioTmp = tmpClip.width() / tmpClip.height();
            float ratioCanvas = canvasRect.width() / canvasRect.height();
            if (ratioTmp > ratioCanvas) {
                // clip区域扁扁的，width 拉到canvas 的width
                left = canvasRect.left;
                right = canvasRect.right;
                float tmpHeight = canvasRect.width() / ratioTmp;
                top = canvasRect.top + ((canvasRect.height() - tmpHeight) / 2.0f);
                bottom = canvasRect.bottom - ((canvasRect.height() - tmpHeight) / 2.0f);
            } else {
                top = canvasRect.top;
                bottom = canvasRect.bottom;
                float tmpWidth = canvasRect.height() * ratioTmp;
                left = canvasRect.left + ((canvasRect.width() - tmpWidth) / 2.0f);
                right = canvasRect.right - ((canvasRect.width() - tmpWidth) / 2.0f);
            }
            tmpClipLarge.set(left, top, right, bottom);
            float scale = tmpClipLarge.width() / tmpClip.width();

            left = tmpClipLarge.left - ((tmpClip.left - pictureRect.left) * scale);
            top = tmpClipLarge.top - ((tmpClip.top - pictureRect.top) * scale);
            right = left + pictureRect.width() * scale;
            bottom = top + pictureRect.height() * scale;
            pictureRect.set(left, top, right, bottom);
            mZoomScale = pictureRect.height() / originRect.height();
            clipPictureRect.set(tmpClipLarge);
            setClipBitmapRect();
            setClipIconRect(0, 0);
            postInvalidate();
        }

        picOffsetX = 0;
        picOffsetY = 0;
        setPrePictureRect();

        //addRecord(MODE_CLIP);
        invalidateBtn();
    }

    private void addRecord(int mode) {
        if (mIndex < (mRecords.size() - 1)) {
            for (int i = (mIndex + 1); i < mRecords.size(); i++) {
                mRecords.remove(i);
            }
        }

        if ((mode & MODE_CLIP) == MODE_CLIP) {
            ClipRecord record = new ClipRecord();
            record.pictureRect = new RectF(pictureRect);
            record.clipPictureRect = new RectF(clipPictureRect);
            mRecords.add(record);
        } else if ((mode & MODE_PEN) == MODE_PEN) {
            PenRecord record = new PenRecord();
            record.paint = new Paint(mPenPaint);
            record.path = new Path(mPenPath);
            mRecords.add(record);
        }
        mIndex++;
    }

    private void invalidateBtn() {
        if (draw != null) {
            boolean back = mIndex > 0;
            boolean forward = mIndex < (mRecords.size() - 1);
            draw.finishAction(back, forward);
        }
    }

    public void goBack() {
        Record record = mRecords.get(mIndex - 1);
        if (record instanceof ClipRecord) {
            ClipRecord clipRecord = (ClipRecord) record;
            pictureRect.set(clipRecord.pictureRect);
            clipPictureRect.set(clipRecord.clipPictureRect);
            setClipBitmapRect();
            setClipIconRect(0, 0);
            mIndex--;
            invalidateBtn();
            postInvalidate();
        } else if(record instanceof PenRecord) {
            //PenRecord penRecord = (PenRecord) record;
            mIndex--;
            invalidateBtn();
            postInvalidate();
        }
    }

    public void goForward() {
        Record record = mRecords.get(mIndex + 1);
        if (record instanceof ClipRecord) {
            ClipRecord clipRecord = (ClipRecord) record;
            pictureRect.set(clipRecord.pictureRect);
            clipPictureRect.set(clipRecord.clipPictureRect);
            setClipBitmapRect();
            setClipIconRect(0, 0);
            mIndex++;
            invalidateBtn();
            postInvalidate();
        } else if(record instanceof PenRecord) {
            //PenRecord penRecord = (PenRecord) record;
            mIndex++;
            invalidateBtn();
            postInvalidate();
        }
    }

    public void goSave() {
        if (draw != null) {
            Bitmap newBitmap = Bitmap.createBitmap(mBitmap,
                    clipBitmapRect.left,
                    clipBitmapRect.top,
                    clipBitmapRect.width(),
                    clipBitmapRect.height());
            draw.save(newBitmap);
        }
    }

    public void setMode(int mode) {
        if (mode == MODE_PEN) {
            addRecord(MODE_CLIP);
            invalidateBtn();

            /*
            mDrawBitmap = Bitmap.createBitmap(mBitmap,
                    clipBitmapRect.left,
                    clipBitmapRect.top,
                    clipBitmapRect.width(),
                    clipBitmapRect.height());
                    */

            mDrawBitmap = Bitmap.createBitmap(clipBitmapRect.width(), clipBitmapRect.height(), Bitmap.Config.ARGB_8888);
            mDrawCanvas.setBitmap(mDrawBitmap);
        }
        moveMode = mode;
        postInvalidate();
    }
}
