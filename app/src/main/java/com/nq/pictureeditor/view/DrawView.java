package com.nq.pictureeditor.view;

import android.content.Context;
import android.content.Intent;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;

import com.nq.pictureeditor.mode.ClipMode;
import com.nq.pictureeditor.mode.ModeLoopInterface;
import com.nq.pictureeditor.mode.MosaicsPenMode;
import com.nq.pictureeditor.mode.ColorPenMode;
import com.nq.pictureeditor.DrawInterface;
import com.nq.pictureeditor.R;
import com.nq.pictureeditor.Utils;
import com.nq.pictureeditor.mode.EditMode;
import com.nq.pictureeditor.mode.PenMode;
import com.nq.pictureeditor.mode.RecordAction;
import com.nq.pictureeditor.text.TextActivity;

import java.util.ArrayList;
import java.util.List;

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

    private RectF canvasRect = new RectF();
    private RectF originRect = new RectF();
    //private RectF prePictureRect = new RectF();
    private RectF pictureRect = new RectF();
    private RectF clipPictureRect = new RectF();

    private RectF bitmapRect = new RectF();
    private RectF clipBitmapRect = new RectF();

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

    private List<EditMode> mEditMode = new ArrayList<>();
    private ClipMode mClipMode;
    private PenMode mPenMode;
    private MosaicsPenMode mMosaicsPenMode;


    private int editMode2 = EditMode.MODE_CLIP;
    //private int clipMode = ClipMode.MODE_NORMAL;

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

        mClipMode = new ClipMode(mContext);

        mEditMode.add(mClipMode);
        mEditMode.add(mPenMode);
        mEditMode.add(mMosaicsPenMode);


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

        mClipMode.setPrePictureRect(originRect);
        //prePictureRect.set(originRect);

        mClipMode.setPictureRect(pictureRect, mOriginBitmap, mZoomScale, picOffsetX, picOffsetY);
        mClipMode.setClipPictureRect(pictureRect, clipPictureRect, canvasRect);
        mClipMode.setClipIconRect(0, 0, clipPictureRect);

        mClipMode.setBitmapRect(bitmapRect, mDrawBitmap);
        mClipMode.setClipBitmapRect(pictureRect, clipPictureRect, bitmapRect, clipBitmapRect);
        mClipMode.matrix(M, bitmapRect, pictureRect);

        //editMode = EditMode.MODE_CLIP;
        //mRecordAction.addRecord(new ClipMode(pictureRect, clipPictureRect, M, true), true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() <= 1) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    downY = event.getY();

                    if (editMode2 == EditMode.MODE_CLIP) {
                        mClipMode.computeClipMode(downX, downY, clipPictureRect);
                    } else if (editMode2 == EditMode.MODE_PEN) {
                        mPenPath.reset();
                        Point mapped = Utils.mapped(M, downX, downY);
                        downX = mapped.x;
                        downY = mapped.y;
                        mPenPath.moveTo(downX, downY);
                        //mDrawing = true;
                        mDrawCanvas.clipRect(clipBitmapRect);
                        Log.e(TAG, "clipBitmapRect=" + clipBitmapRect.toString());

                        mDrawCanvas.drawPath(mPenPath, mColorPaint);
                        invalidate();
                    } else if (editMode2 == EditMode.MODE_MOSAICS) {
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
                    if (editMode2 == EditMode.MODE_CLIP) {
                        if(mClipMode.clipMode() >= ClipMode.MODE_LT_ICON && mClipMode.clipMode() <= ClipMode.MODE_B_LINE) {
                            mClipMode.computeIcon(event.getX(), event.getY(), downX, downY,
                                    canvasRect, pictureRect, clipPictureRect);
                            invalidate();
                        } else if (mClipMode.clipMode() == ClipMode.MODE_PICTURE) {
                            picOffsetX = event.getX() - downX;
                            picOffsetY = event.getY() - downY;

                            mClipMode.setPictureRect(pictureRect, mOriginBitmap, mZoomScale, picOffsetX, picOffsetY);
                            mClipMode.matrix(M, bitmapRect, pictureRect);
                            //setPictureRect();
                            //matrix();
                            invalidate();
                        }
                    } else if (editMode2 == EditMode.MODE_PEN) {
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
                    } else if (editMode2 == EditMode.MODE_MOSAICS) {
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
                    if (editMode2 == EditMode.MODE_CLIP) {
                        cluRect();
                    } else if (editMode2 == EditMode.MODE_PEN) {
                        Point mapped = Utils.mapped(M, event.getX(), event.getY());
                        mPenPath.lineTo(mapped.x, mapped.y);
                        mDrawCanvas.clipRect(clipBitmapRect);
                        mDrawCanvas.drawPath(mPenPath, mColorPaint);

                        //mDrawing = false;
                        //mChanged = true;
                        //addRecord(EditMode.MODE_PEN);
                        mRecordAction.addRecord(new ColorPenMode(pictureRect, clipPictureRect, M, mPenPath, mColorPaint, clipBitmapRect), false);

                        redrawBitmap();
                        invalidateBtn();
                        invalidate();
                    } else if (editMode2 == EditMode.MODE_MOSAICS) {
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
                        mRecordAction.addRecord(new MosaicsPenMode(pictureRect, clipPictureRect, M, mPenPath, mMosaicsPaint, clipBitmapRect), false);

                        redrawBitmap();
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

        if(editMode2 == EditMode.MODE_CLIP) {
            canvas.save();
            mDrawPaint.setAlpha(50); // setColor 和 setAlpha 的顺序有关系
            canvas.drawBitmap(mDrawBitmap, M, mDrawPaint);
            canvas.restore();

            mClipMode.onDraw(canvas, mIconPaint);
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

        mClipMode.setPictureRect(pictureRect, mOriginBitmap, mZoomScale, picOffsetX, picOffsetY);
        //setPictureRect();
        mClipMode.matrix(M, bitmapRect, pictureRect);
        invalidate();

        mClipMode.setPrePictureRect(pictureRect);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if(editMode2 == EditMode.MODE_CLIP) {
            //clipMode = ClipMode.MODE_SCALE;
            mClipMode.setClipMode(ClipMode.MODE_SCALE);
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
        if (mClipMode.clipMode() == ClipMode.MODE_SCALE) {
            //clipMode = ClipMode.MODE_NORMAL;
            mClipMode.setClipMode(ClipMode.MODE_NORMAL);

            float widthScale = 1.0f, heightScale = 1.0f;
            if (clipPictureRect.width() > pictureRect.width()) {
                widthScale = clipPictureRect.width() / pictureRect.width();
            }
            if (clipPictureRect.height() > pictureRect.height()) {
                heightScale = clipPictureRect.height() / pictureRect.height();
            }
            float zoomScale = Math.max(widthScale, heightScale);
            mZoomScale = pictureRect.height() * zoomScale / mOriginBitmap.getHeight();

            mClipMode.setPictureRect(pictureRect, mOriginBitmap, mZoomScale, picOffsetX, picOffsetY);
            mClipMode.setClipBitmapRect(pictureRect, clipPictureRect, bitmapRect, clipBitmapRect);
            mClipMode.matrix(M, bitmapRect, pictureRect);
            //setPictureRect();
            //setClipBitmapRect();
            //matrix();

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

            mClipMode.setPictureRect(pictureRect, mOriginBitmap, mZoomScale, picOffsetX, picOffsetY);
            mClipMode.setClipBitmapRect(pictureRect, clipPictureRect, bitmapRect, clipBitmapRect);
            mClipMode.matrix(M, bitmapRect, pictureRect);
            //setPictureRect();
            //setClipBitmapRect();
            //matrix();
            invalidate();
        } else if (mClipMode.clipMode() == ClipMode.MODE_PICTURE) {
            //clipMode = ClipMode.MODE_NORMAL;
            mClipMode.setClipMode(ClipMode.MODE_NORMAL);

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

            mClipMode.setPictureRect(pictureRect, mOriginBitmap, mZoomScale, picOffsetX, picOffsetY);
            mClipMode.setClipBitmapRect(pictureRect, clipPictureRect, bitmapRect, clipBitmapRect);
            mClipMode.matrix(M, bitmapRect, pictureRect);
            //setPictureRect();
            //setClipBitmapRect();
            //matrix();
            invalidate();
        } else if (mClipMode.clipMode() >= ClipMode.MODE_LT_ICON && mClipMode.clipMode() <= ClipMode.MODE_B_LINE) {
            //把裁剪区域放大后，计算picture 的rect，其他就全部可以顺势算出了
            //clipMode = ClipMode.MODE_NORMAL;
            mClipMode.setClipMode(ClipMode.MODE_NORMAL);

            float left, top, right, bottom;
            RectF tmpClip = mClipMode.newRect();

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



            mClipMode.setClipBitmapRect(pictureRect, clipPictureRect, bitmapRect, clipBitmapRect);
            mClipMode.matrix(M, bitmapRect, pictureRect);


            mClipMode.setClipIconRect(0, 0, clipPictureRect);
            invalidate();
        }

        picOffsetX = 0;
        picOffsetY = 0;
        mClipMode.setPrePictureRect(pictureRect);

        //ClipMode clipMode = new ClipMode(pictureRect, clipPictureRect, M, false);
        //mRecordAction.addRecord(clipMode, false);

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
                    ColorPenMode penMode = (ColorPenMode) mode;

                    mDrawCanvas.save();
                    mDrawCanvas.clipRect(penMode.clip);
                    mDrawCanvas.drawPath(penMode.path, penMode.paint);
                    mDrawCanvas.restore();
                } else if (mode.getMode() == EditMode.MODE_MOSAICS) {
                    MosaicsPenMode mosaicsMode = (MosaicsPenMode) mode;
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
            case EditMode.MODE_PEN:
            case EditMode.MODE_MOSAICS:
                //ClipMode clipMode = (ClipMode) preEditMode;
                pictureRect.set(preEditMode.pictureRect);
                clipPictureRect.set(preEditMode.clipPictureRect);
                M.set(preEditMode.M);
                mClipMode.setClipBitmapRect(pictureRect, clipPictureRect, bitmapRect, clipBitmapRect);
                //setClipIconRect(0, 0);
                break;
            //case EditMode.MODE_PEN:
            //case EditMode.MODE_MOSAICS:
            //    break;
        }

        editMode2 = mode;
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
            case EditMode.MODE_PEN:
            case EditMode.MODE_MOSAICS:
                //ClipMode clipMode = (ClipMode) nextEditMode;
                //pictureRect.set(nextEditMode.pictureRect);
                //clipPictureRect.set(nextEditMode.clipPictureRect);
                M.set(nextEditMode.M);
                mClipMode.setClipBitmapRect(pictureRect, clipPictureRect, bitmapRect, clipBitmapRect);
                //setClipIconRect(0, 0);
                break;
            //case EditMode.MODE_PEN:
            //case EditMode.MODE_MOSAICS:
            //    break;
        }

        editMode2 = mode;
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

    public void goShare() {
        if (draw != null) {
            redrawBitmap();
            Bitmap newBitmap = Bitmap.createBitmap(mDrawBitmap,
                    (int) clipBitmapRect.left,
                    (int) clipBitmapRect.top,
                    (int) clipBitmapRect.width(),
                    (int) clipBitmapRect.height());
            draw.share(newBitmap);
        }
    }

    public void setMode(int mode) {
        //if (editMode == EditMode.MODE_CLIP || mode == EditMode.MODE_CLIP) {
        //    ClipMode clipMode = new ClipMode(pictureRect, clipPictureRect, M);
        //    mRecordAction.addRecord(clipMode, false);
        //}

        editMode2 = mode;
        //mChanged = false;
        invalidate();
        invalidateBtn();

        if(editMode2 == EditMode.MODE_CLIP) {
            //ClipMode clipMode = new ClipMode(pictureRect, clipPictureRect, M, true);
            //mRecordAction.addRecord(clipMode, false);
        } else if (editMode2 == EditMode.MODE_MOSAICS) {
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
