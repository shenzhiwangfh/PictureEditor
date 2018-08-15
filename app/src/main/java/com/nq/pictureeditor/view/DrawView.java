package com.nq.pictureeditor.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.nq.pictureeditor.mode.ClipMode;
import com.nq.pictureeditor.mode.ModeLoopInterface;
import com.nq.pictureeditor.mode.MosaicsMode;
import com.nq.pictureeditor.mode.PenMode;
import com.nq.pictureeditor.DrawInterface;
import com.nq.pictureeditor.R;
import com.nq.pictureeditor.Utils;
import com.nq.pictureeditor.mode.EditMode;
import com.nq.pictureeditor.mode.RecordAction;

public class DrawView extends View implements ScaleGestureDetector.OnScaleGestureListener {

    private final static String TAG = "DrawView";

    private Context mContext;
    private ScaleGestureDetector mGesture;
    private Resources mRes;

    private static final float PEN_MIN_MOVE = 4.0f;

    private final static float MIN_SCALE = 0.25f;
    private final static float NORMAL_SCALE = 1.0f;
    private final static float MAX_SCALE = 10.0f;
    private float mZoomScale = NORMAL_SCALE; //默认的缩放比为1

    private float ICON_WIDTH;
    private float ICON_SIZE;
    private float LINE_WIDTH;
    private float CLIP_MIN_SIZE;

    private RectF canvasRect;// = new RectF(0, 0, 960, 960);
    private RectF originRect;
    private RectF prePictureRect;
    private RectF pictureRect;
    private RectF clipPictureRect;

    private RectF clipIconRect;
    private RectF iconLeftTop, iconRightTop, iconLeftBottom, iconRightBottom;
    private RectF lineLeft, lineRight, lineTop, lineBottom;

    private RectF bitmapRect;
    private RectF clipBitmapRect;

    private Paint mDrawPaint;
    private Paint mColorPaint = new Paint();
    private Paint mEraserPaint = new Paint();
    private Paint mMosaicsPaint = new Paint();
    private Paint mIconPaint;
    private PorterDuffXfermode mDuffXfermode;

    private Path mPenPath;
    private Canvas mDrawCanvas;

    private Bitmap mOriginBitmap;
    private Bitmap mDrawBitmap;
    private Bitmap mMosaicBmp;

    private int editMode = EditMode.MODE_CLIP;
    private int clipMode = ClipMode.MODE_NORMAL;

    private Matrix M = new Matrix();
    private float downX, downY;
    private float picOffsetX = 0.0f, picOffsetY = 0.0f;

    //private ArrayList<Record> mRecords = new ArrayList<>();
    //private int mIndex = -1;
    //private boolean mChanged = false;
    private RecordAction mRecordAction = new RecordAction();

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
        mRes = mContext.getResources();
        mGesture = new ScaleGestureDetector(mContext, this);

        init();
    }

    private void buildColorPaint() {
        mColorPaint.setAntiAlias(true);
        mColorPaint.setStyle(Paint.Style.STROKE);
        mColorPaint.setStrokeCap(Paint.Cap.ROUND);
        mColorPaint.setStrokeJoin(Paint.Join.ROUND);
        //mColorPaint.setColor(color);
        //mColorPaint.setStrokeWidth(size);
    }

    private void buildEraserPaint() {
        mEraserPaint.setAlpha(0);
        //下面这句代码是橡皮擦设置的重点
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    private void buildMosaicsPaint() {
        mDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

        mMosaicsPaint.setAntiAlias(true);
        mMosaicsPaint.setDither(true);
        mMosaicsPaint.setStyle(Paint.Style.STROKE);//描边
        mMosaicsPaint.setTextAlign(Paint.Align.CENTER);//居中
        mMosaicsPaint.setStrokeCap(Paint.Cap.ROUND);//圆角
        mMosaicsPaint.setStrokeJoin(Paint.Join.ROUND);//拐点圆角
    }

    private void init() {
        mPenPath = new Path();

        buildColorPaint();
        buildEraserPaint();
        buildMosaicsPaint();

        mDrawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawPaint.setColor(Color.BLACK);

        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIconPaint.setColor(Color.RED);

        ICON_WIDTH = mRes.getDimension(R.dimen.icon_width);
        ICON_SIZE = mRes.getDimension(R.dimen.icon_size);
        LINE_WIDTH = mRes.getDimension(R.dimen.line_width);
        CLIP_MIN_SIZE = mRes.getDimension(R.dimen.clip_min_size);
    }

    public void initBitmap(Bitmap bitmap) {
        mOriginBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mDrawBitmap = Bitmap.createBitmap(mOriginBitmap);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int screenWidth = dm.widthPixels;
        final int screenHeight = dm.heightPixels;

        mRes = mContext.getResources();
        final float leftPadding = mRes.getDimension(R.dimen.picture_left_padding);
        final float topPadding = mRes.getDimension(R.dimen.picture_top_padding);
        final float rightPadding = mRes.getDimension(R.dimen.picture_right_padding);
        final float bottomPadding = mRes.getDimension(R.dimen.picture_bottom_padding);
        canvasRect = new RectF(leftPadding, topPadding, screenWidth - rightPadding, screenHeight - bottomPadding);

        float bitmapRatio = (float) mOriginBitmap.getHeight() / (float) mOriginBitmap.getWidth();
        float canvasRatio = canvasRect.height() / canvasRect.width();
        if (bitmapRatio > canvasRatio) {
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
        mZoomScale = originRect.height() / mOriginBitmap.getHeight();

        mDrawCanvas = new Canvas();
        mDrawCanvas.setBitmap(mDrawBitmap);

        setPrePictureRect();

        setPictureRect();
        setClipPictureRect();
        setClipIconRect(0, 0);

        setBitmapRect();
        setClipBitmapRect();
        matrix();

        //editMode = EditMode.MODE_CLIP;
        //mRecordAction.addRecord(new ClipMode(pictureRect, clipPictureRect, M, true), true);
    }

    private void setPrePictureRect() {
        if (prePictureRect == null)
            prePictureRect = new RectF(originRect);
        else
            prePictureRect.set(pictureRect);
    }

    private void setPictureRect() {
        float oldWidth = prePictureRect.width();
        float oldHeight = prePictureRect.height();
        float newWidth = mOriginBitmap.getWidth() * mZoomScale;
        float newHeight = mOriginBitmap.getHeight() * mZoomScale;

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
            bitmapRect = new RectF(0, 0, mDrawBitmap.getWidth(), mDrawBitmap.getHeight());
    }

    private void setClipBitmapRect() {
        float ratio = bitmapRect.height() / pictureRect.height();

        float left = (clipPictureRect.left - pictureRect.left) * ratio;
        float right = left + clipPictureRect.width() * ratio;
        float top = (clipPictureRect.top - pictureRect.top) * ratio;
        float bottom = top + clipPictureRect.height() * ratio;

        if (clipBitmapRect != null) {
            clipBitmapRect.set(left, top, right, bottom);
        } else {
            clipBitmapRect = new RectF(left, top, right, bottom);
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
        } else if (clipMode == ClipMode.MODE_LT_ICON) {
            left = offsetX + clipPictureRect.left - ICON_WIDTH;
            top = offsetY + clipPictureRect.top - ICON_WIDTH;
            clipIconRect.set(left, top, clipIconRect.right, clipIconRect.bottom);
        } else if (clipMode == ClipMode.MODE_RT_ICON) {
            right = offsetX + clipPictureRect.right + ICON_WIDTH;
            top = offsetY + clipPictureRect.top - ICON_WIDTH;
            clipIconRect.set(clipIconRect.left, top, right, clipIconRect.bottom);
        } else if (clipMode == ClipMode.MODE_LB_ICON) {
            left = offsetX + clipPictureRect.left - ICON_WIDTH;
            bottom = offsetY + clipPictureRect.bottom + ICON_WIDTH;
            clipIconRect.set(left, clipIconRect.top, clipIconRect.right, bottom);
        } else if (clipMode == ClipMode.MODE_RB_ICON) {
            right = offsetX + clipPictureRect.right + ICON_WIDTH;
            bottom = offsetY + clipPictureRect.bottom + ICON_WIDTH;
            clipIconRect.set(clipIconRect.left, clipIconRect.top, right, bottom);
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

    private void matrix() {
        M.setRectToRect(bitmapRect, pictureRect, Matrix.ScaleToFit.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() <= 1) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    downY = event.getY();

                    if (editMode == EditMode.MODE_CLIP) {
                        if (iconLeftTop.contains(downX, downY)) {
                            clipMode = ClipMode.MODE_LT_ICON;
                        } else if (iconRightTop.contains(downX, downY)) {
                            clipMode = ClipMode.MODE_RT_ICON;
                        } else if (iconLeftBottom.contains(downX, downY)) {
                            clipMode = ClipMode.MODE_LB_ICON;
                        } else if (iconRightBottom.contains(downX, downY)) {
                            clipMode = ClipMode.MODE_RB_ICON;
                        } else if (lineLeft.contains(downX, downY)) {
                            clipMode = ClipMode.MODE_L_LINE;
                        } else if (lineRight.contains(downX, downY)) {
                            clipMode = ClipMode.MODE_R_LINE;
                        } else if (lineTop.contains(downX, downY)) {
                            clipMode = ClipMode.MODE_T_LINE;
                        } else if (lineBottom.contains(downX, downY)) {
                            clipMode = ClipMode.MODE_B_LINE;
                        } else if (clipPictureRect.contains(downX, downY)) {
                            clipMode = ClipMode.MODE_PICTURE;
                        }
                        //invalidate();
                    } else if (editMode == EditMode.MODE_PEN) {
                        mPenPath.reset();
                        Point mapped = Utils.mapped(M, downX, downY);
                        downX = mapped.x;
                        downY = mapped.y;
                        mPenPath.moveTo(downX, downY);
                        //mDrawing = true;
                        mDrawCanvas.clipRect(clipBitmapRect);
                        mDrawCanvas.drawPath(mPenPath, mColorPaint);
                        invalidate();
                    } else if (editMode == EditMode.MODE_MOSAICS) {
                        mPenPath.reset();
                        Point mapped = Utils.mapped(M, downX, downY);
                        downX = mapped.x;
                        downY = mapped.y;
                        mPenPath.moveTo(downX, downY);
                        //mDrawing = true;

                        int canvasWidth = mDrawCanvas.getWidth();
                        int canvasHeight = mDrawCanvas.getHeight();
                        int layerId = mDrawCanvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);

                        mDrawCanvas.clipRect(clipBitmapRect);
                        mDrawCanvas.drawPath(mPenPath, mMosaicsPaint);

                        mMosaicsPaint.setXfermode(mDuffXfermode);
                        mDrawCanvas.drawBitmap(mMosaicBmp, 0, 0, mMosaicsPaint); //画出重叠区域
                        mMosaicsPaint.setXfermode(null);
                        mDrawCanvas.restoreToCount(layerId);
                    }
                }
                break;
                case MotionEvent.ACTION_MOVE: {
                    if (editMode == EditMode.MODE_CLIP) {
                        if (clipMode == ClipMode.MODE_LT_ICON) {
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
                            invalidate();
                        } else if (clipMode == ClipMode.MODE_RT_ICON) {
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
                            invalidate();
                        } else if (clipMode == ClipMode.MODE_LB_ICON) {
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
                            invalidate();
                        } else if (clipMode == ClipMode.MODE_RB_ICON) {
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
                            invalidate();
                        } else if (clipMode == ClipMode.MODE_PICTURE) {
                            picOffsetX = event.getX() - downX;
                            picOffsetY = event.getY() - downY;

                            setPictureRect();
                            matrix();
                            invalidate();
                        }
                    } else if (editMode == EditMode.MODE_PEN) {
                        Point mapped = Utils.mapped(M, event.getX(), event.getY());
                        float x = mapped.x;
                        float y = mapped.y;

                        float dx = Math.abs(downX - x);
                        float dy = Math.abs(downY - y);
                        if (dx > PEN_MIN_MOVE || dy > PEN_MIN_MOVE) {
                            mPenPath.quadTo(downX, downY, (x + downX) / 2, (y + downY) / 2);
                            downX = x;
                            downY = y;
                            mDrawCanvas.clipRect(clipBitmapRect);
                            mDrawCanvas.drawPath(mPenPath, mColorPaint);
                            invalidate();
                        }
                    } else if (editMode == EditMode.MODE_MOSAICS) {
                        Point mapped = Utils.mapped(M, event.getX(), event.getY());
                        float x = mapped.x;
                        float y = mapped.y;

                        float dx = Math.abs(downX - x);
                        float dy = Math.abs(downY - y);
                        if (dx > PEN_MIN_MOVE || dy > PEN_MIN_MOVE) {
                            mPenPath.quadTo(downX, downY, (x + downX) / 2, (y + downY) / 2);
                            downX = x;
                            downY = y;

                            int canvasWidth = mDrawCanvas.getWidth();
                            int canvasHeight = mDrawCanvas.getHeight();
                            int layerId = mDrawCanvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);

                            mDrawCanvas.clipRect(clipBitmapRect);
                            mDrawCanvas.drawPath(mPenPath, mMosaicsPaint);

                            mMosaicsPaint.setXfermode(mDuffXfermode);
                            mDrawCanvas.drawBitmap(mMosaicBmp, 0, 0, mMosaicsPaint); //画出重叠区域
                            mMosaicsPaint.setXfermode(null);
                            mDrawCanvas.restoreToCount(layerId);

                            invalidate();
                        }
                    }
                }
                break;
                case MotionEvent.ACTION_UP:
                    if (editMode == EditMode.MODE_CLIP) {
                        if (clipMode >= ClipMode.MODE_LT_ICON && clipMode <= ClipMode.MODE_B_LINE) {
                            cluRect();
                        } else if (clipMode == ClipMode.MODE_SCALE) {
                            cluRect();
                        } else if (clipMode == ClipMode.MODE_PICTURE) {
                            cluRect();
                        }
                    } else if (editMode == EditMode.MODE_PEN) {
                        Point mapped = Utils.mapped(M, event.getX(), event.getY());
                        mPenPath.lineTo(mapped.x, mapped.y);
                        mDrawCanvas.clipRect(clipBitmapRect);
                        mDrawCanvas.drawPath(mPenPath, mColorPaint);

                        //mDrawing = false;
                        //mChanged = true;
                        //addRecord(EditMode.MODE_PEN);
                        mRecordAction.addRecord(new PenMode(mPenPath, mColorPaint, clipBitmapRect), false);

                        //redrawBitmap();
                        invalidateBtn();
                        invalidate();
                    } else if (editMode == EditMode.MODE_MOSAICS) {
                        Point mapped = Utils.mapped(M, event.getX(), event.getY());
                        mPenPath.lineTo(mapped.x, mapped.y);

                        int canvasWidth = mDrawCanvas.getWidth();
                        int canvasHeight = mDrawCanvas.getHeight();
                        int layerId = mDrawCanvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);

                        mDrawCanvas.clipRect(clipBitmapRect);
                        mDrawCanvas.drawPath(mPenPath, mMosaicsPaint);

                        mMosaicsPaint.setXfermode(mDuffXfermode);
                        mDrawCanvas.drawBitmap(mMosaicBmp, 0, 0, mMosaicsPaint); //画出重叠区域
                        mMosaicsPaint.setXfermode(null);
                        mDrawCanvas.restoreToCount(layerId);

                        //mDrawing = false;
                        //mChanged = true;
                        //addRecord(EditMode.MODE_MOSAICS);
                        mRecordAction.addRecord(new MosaicsMode(mPenPath, mMosaicsPaint, clipBitmapRect), false);

                        //redrawBitmap();
                        invalidateBtn();
                        invalidate();
                    }
                    break;
            }
        } else {
            return mGesture.onTouchEvent(event);
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        mDrawPaint.setAlpha(255);
        canvas.clipRect(clipPictureRect);
        canvas.drawBitmap(mDrawBitmap, M, mDrawPaint);
        canvas.restore();

        if(editMode == EditMode.MODE_CLIP) {
            canvas.save();
            mDrawPaint.setAlpha(100); // setColor 和 setAlpha 的顺序有关系
            canvas.drawBitmap(mDrawBitmap, M, mDrawPaint);
            canvas.restore();

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
        matrix();
        invalidate();

        setPrePictureRect();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if(editMode == EditMode.MODE_CLIP) {
            clipMode = ClipMode.MODE_SCALE;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        cluRect();
    }

    private void cluRect() {
        if (clipMode == ClipMode.MODE_SCALE) {
            clipMode = ClipMode.MODE_NORMAL;

            float widthScale = 1.0f, heightScale = 1.0f;
            if (clipPictureRect.width() > pictureRect.width()) {
                widthScale = clipPictureRect.width() / pictureRect.width();
            }
            if (clipPictureRect.height() > pictureRect.height()) {
                heightScale = clipPictureRect.height() / pictureRect.height();
            }
            float zoomScale = Math.max(widthScale, heightScale);
            mZoomScale = pictureRect.height() * zoomScale / mOriginBitmap.getHeight();
            setPictureRect();
            setClipBitmapRect();
            matrix();

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
            matrix();
            invalidate();
        } else if (clipMode == ClipMode.MODE_PICTURE) {
            clipMode = ClipMode.MODE_NORMAL;

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
            matrix();
            invalidate();
        } else if (clipMode >= ClipMode.MODE_LT_ICON && clipMode <= ClipMode.MODE_B_LINE) {
            //把裁剪区域放大后，计算picture 的rect，其他就全部可以顺势算出了
            clipMode = ClipMode.MODE_NORMAL;

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
            mZoomScale = pictureRect.height() / mOriginBitmap.getHeight();
            clipPictureRect.set(tmpClipLarge);
            setClipBitmapRect();
            matrix();
            setClipIconRect(0, 0);
            invalidate();
        }

        picOffsetX = 0;
        picOffsetY = 0;
        setPrePictureRect();

        ClipMode clipMode = new ClipMode(pictureRect, clipPictureRect, M, false);
        mRecordAction.addRecord(clipMode, false);

        invalidateBtn();
        //mChanged = true;
    }

    private void redrawBitmap() {
        mDrawBitmap.recycle();
        mDrawBitmap = Bitmap.createBitmap(mOriginBitmap);
        mDrawCanvas.setBitmap(mDrawBitmap);

        mRecordAction.doLoop(new ModeLoopInterface() {
            @Override
            public void pickMode(EditMode mode) {
                if (mode.getMode() == EditMode.MODE_PEN) {
                    PenMode penMode = (PenMode) mode;

                    mDrawCanvas.save();
                    mDrawCanvas.clipRect(penMode.clip);
                    mDrawCanvas.drawPath(penMode.path, penMode.paint);
                    mDrawCanvas.restore();
                } else if (mode.getMode() == EditMode.MODE_MOSAICS) {
                    MosaicsMode mosaicsMode = (MosaicsMode) mode;
                    int canvasWidth = mDrawCanvas.getWidth();
                    int canvasHeight = mDrawCanvas.getHeight();
                    int layerId = mDrawCanvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);

                    mDrawCanvas.clipRect(mosaicsMode.clip);
                    mDrawCanvas.drawPath(mosaicsMode.path, mosaicsMode.paint);

                    mosaicsMode.paint.setXfermode(mDuffXfermode);
                    mDrawCanvas.drawBitmap(mMosaicBmp, 0, 0, mosaicsMode.paint); //画出重叠区域
                    mosaicsMode.paint.setXfermode(null);
                    mDrawCanvas.restoreToCount(layerId);
                }
            }
        });

        invalidate();
    }

    private void invalidateBtn() {
        if (draw != null) {
            int index = mRecordAction.getIndex();
            boolean back = index > 0;
            boolean forward = index < (mRecordAction.size() - 1);
            draw.finishAction(back, forward);
        }
    }

    public int goBack() {
        EditMode preEditMode = mRecordAction.getPreMode();
        int mode = preEditMode.getMode();
        switch (mode) {
            case EditMode.MODE_CLIP:
                ClipMode clipMode = (ClipMode) preEditMode;
                pictureRect.set(clipMode.pictureRect);
                clipPictureRect.set(clipMode.clipPictureRect);
                M.set(clipMode.M);
                setClipBitmapRect();
                setClipIconRect(0, 0);
                break;
            case EditMode.MODE_PEN:
            case EditMode.MODE_MOSAICS:
                break;
        }

        editMode = mode;
        mRecordAction.back();
        invalidateBtn();
        //invalidate();
        redrawBitmap();

        return mode;
    }

    public int goForward() {
        EditMode nextEditMode = mRecordAction.getNextMode();
        int mode = nextEditMode.getMode();
        switch (mode) {
            case EditMode.MODE_CLIP:
                ClipMode clipMode = (ClipMode) nextEditMode;
                pictureRect.set(clipMode.pictureRect);
                clipPictureRect.set(clipMode.clipPictureRect);
                M.set(clipMode.M);
                setClipBitmapRect();
                setClipIconRect(0, 0);
                break;
            case EditMode.MODE_PEN:
            case EditMode.MODE_MOSAICS:
                break;
        }

        editMode = mode;
        mRecordAction.forward();
        invalidateBtn();
        //invalidate();
        redrawBitmap();

        return mode;
    }

    public void goSave() {
        if (draw != null) {
            redrawBitmap();
            Bitmap newBitmap = Bitmap.createBitmap(mDrawBitmap,
                    (int) clipBitmapRect.left,
                    (int) clipBitmapRect.top,
                    (int) clipBitmapRect.width(),
                    (int) clipBitmapRect.height());
            draw.save(newBitmap);
        }
    }

    public void setMode(int mode) {
        //if (editMode == EditMode.MODE_CLIP || mode == EditMode.MODE_CLIP) {
        //    ClipMode clipMode = new ClipMode(pictureRect, clipPictureRect, M);
        //    mRecordAction.addRecord(clipMode, false);
        //}

        editMode = mode;
        //mChanged = false;
        invalidate();
        invalidateBtn();

        if(editMode == EditMode.MODE_CLIP) {
            ClipMode clipMode = new ClipMode(pictureRect, clipPictureRect, M, true);
            mRecordAction.addRecord(clipMode, false);
        } else if (editMode == EditMode.MODE_MOSAICS) {
            if (mMosaicBmp != null) mMosaicBmp.recycle();
            mMosaicBmp = ViewUtils.BitmapMosaic(mDrawBitmap, 64);
        }
    }

    public void setPenColor(int color) {
        mColorPaint.setColor(color);
    }

    public void setPenSize(int size) {
        mColorPaint.setStrokeWidth(size);
        mMosaicsPaint.setStrokeWidth(size);
    }
}
