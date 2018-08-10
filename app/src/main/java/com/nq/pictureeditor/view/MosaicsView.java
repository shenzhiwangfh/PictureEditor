package com.nq.pictureeditor.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DiscretePathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created on 2017/8/3.
 */

public class MosaicsView extends View {

    private Bitmap mOriginBmp;
    private Bitmap mMosaicBmp;
    private Paint mPaint;
    private ArrayList<DrawPath> mPaths;
    private DrawPath mLastPath;

    private RectF mBitmapRectF = new RectF();
    private RectF mCanvasRectF = new RectF();

    private Matrix M;

    private PorterDuffXfermode mDuffXfermode;
    private float tempX, tempY;
    private final float mTargetWidth = 200.0f;

    public MosaicsView(Context context) {
        super(context);
        init();
    }

    public MosaicsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MosaicsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);//描边
        mPaint.setTextAlign(Paint.Align.CENTER);//居中
        mPaint.setStrokeCap(Paint.Cap.ROUND);//圆角
        mPaint.setStrokeJoin(Paint.Join.ROUND);//拐点圆角
        //正常效果
        mPaint.setStrokeWidth(72);
        //抖动效果
        //mPaint.setStrokeWidth(2f);
        //mPaint.setPathEffect(new DiscretePathEffect(0.35f, 40));

        //
        mPaths = new ArrayList<>();

        mBitmapRectF = new RectF();

        //叠加效果
        mDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    /**
     * 清楚操作
     */
    public void clear() {
        mPaths.clear();
        invalidate();
    }

    /**
     * 撤销
     */
    public void undo() {
        int size = mPaths.size();
        if (size > 0) {
            mPaths.remove(size - 1);
            invalidate();
        }
    }

    /*
    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable != null && drawable instanceof BitmapDrawable) {
            scaleBitmap(((BitmapDrawable) drawable).getBitmap());

            //Bitmap mosaicBmp = ViewUtils.BitmapMosaic(((BitmapDrawable) drawable).getBitmap(), 40);
            //scaleBitmap(mosaicBmp);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        scaleBitmap(bm);

        //Bitmap mosaicBmp = ViewUtils.BitmapMosaic(bm, 40);
        //scaleBitmap(mosaicBmp);
    }
    */

    // 生成小图
    private void scaleBitmap(Bitmap bm) {
        Bitmap mosaicBmp = ViewUtils.BitmapMosaic(bm, 42);

        mMosaicBmp = ViewUtils.BitmapMosaic(bm, 48);


        int width = mosaicBmp.getWidth();
        float scale = mTargetWidth / width;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        mMosaicBmp = Bitmap.createBitmap(mosaicBmp, 0, 0, width, mosaicBmp.getHeight(), matrix, true);

    }

    public void initBitmap(Bitmap originBitmap) {
        mOriginBmp = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mMosaicBmp = ViewUtils.BitmapMosaic(originBitmap, 48);

        M = new Matrix();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int screenWidth = dm.widthPixels;
        final int screenHeight = dm.heightPixels;
        final int bitmapWidth = mOriginBmp.getWidth();
        final int bitmapHeight = mOriginBmp.getHeight();

        mBitmapRectF.set(0, 0, bitmapWidth, bitmapHeight);

        float bitmapRatio = (float) bitmapHeight / (float) bitmapWidth;
        float canvasRatio = screenHeight / screenWidth;
        if (bitmapRatio > canvasRatio) {
            //long picture
            float width = screenHeight / bitmapRatio;
            mCanvasRectF.set(0, 0, width, screenHeight);
        } else {
            //normal picture
            float height = screenWidth * bitmapRatio;
            mCanvasRectF.set(0, 0, screenWidth, height);
        }

        M.setRectToRect(mBitmapRectF, mCanvasRectF, Matrix.ScaleToFit.FILL);

        /*
        if (mMosaicBmp != null) {
            mBitmapRectF = getBitmapRect();
            Matrix mosaicMatrix = new Matrix();
            mosaicMatrix.setTranslate(mBitmapRectF.left, mBitmapRectF.top);
            float scaleX = (mBitmapRectF.right - mBitmapRectF.left) / mMosaicBmp.getWidth();
            float scaleY = (mBitmapRectF.bottom - mBitmapRectF.top) / mMosaicBmp.getHeight();
            mosaicMatrix.postScale(scaleX, scaleY);
            // 生成整张模糊图片
            mMosaicBmp = Bitmap.createBitmap(mMosaicBmp, 0, 0, mMosaicBmp.getWidth(), mMosaicBmp.getHeight(),
                    mosaicMatrix, true);
        }
        */
    }


    /**
     * 得到图片展示区域
     */
    private RectF getBitmapRect() {
        /*
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return new RectF();
        }
        // Get image matrix values and place them in an array.
        final float[] matrixValues = new float[9];
        getImageMatrix().getValues(matrixValues);

        // Extract the scale and translation values from the matrix.
        final float scaleX = matrixValues[Matrix.MSCALE_X];
        final float scaleY = matrixValues[Matrix.MSCALE_Y];
        final float transX = matrixValues[Matrix.MTRANS_X] + getPaddingLeft();
        final float transY = matrixValues[Matrix.MTRANS_Y] + getPaddingTop();

        // Get the width and height of the original bitmap.
        final int drawableIntrinsicWidth = drawable.getIntrinsicWidth();
        final int drawableIntrinsicHeight = drawable.getIntrinsicHeight();

        // Calculate the dimensions as seen on screen.
        final int drawableDisplayWidth = Math.round(drawableIntrinsicWidth * scaleX);
        final int drawableDisplayHeight = Math.round(drawableIntrinsicHeight * scaleY);

        // Get the Rect of the displayed image within the ImageView.
        final float left = Math.max(transX, 0);
        final float top = Math.max(transY, 0);
        final float right = Math.min(left + drawableDisplayWidth, getWidth());
        final float bottom = Math.min(top + drawableDisplayHeight, getHeight());

        return new RectF(left, top, right, bottom);
        */

        //DisplayMetrics dm = getResources().getDisplayMetrics();
        //final int screenWidth = dm.widthPixels;
        //final int screenHeight = dm.heightPixels;
        //return new RectF(0, 0, screenWidth, screenHeight);
        return mCanvasRectF;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        /*
        if (mMosaicBmp != null) {
            mBitmapRectF = getBitmapRect();
            Matrix mosaicMatrix = new Matrix();
            mosaicMatrix.setTranslate(mBitmapRectF.left, mBitmapRectF.top);
            float scaleX = (mBitmapRectF.right - mBitmapRectF.left) / mMosaicBmp.getWidth();
            float scaleY = (mBitmapRectF.bottom - mBitmapRectF.top) / mMosaicBmp.getHeight();
            mosaicMatrix.postScale(scaleX, scaleY);
            // 生成整张模糊图片
            mMosaicBmp = Bitmap.createBitmap(mMosaicBmp, 0, 0, mMosaicBmp.getWidth(), mMosaicBmp.getHeight(),
                    mosaicMatrix, true);
        }
        */
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mPaths.isEmpty()) {
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();

            canvas.drawBitmap(mOriginBmp, /*mCanvasRectF.left, mCanvasRectF.top,*/M, mPaint);//画出重叠区域

            //新图层
            int layerId = canvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);
            canvas.clipRect(mCanvasRectF); //限定区域
            for (DrawPath drawPath : mPaths) {
                //滑过的区域
                drawPath.draw(canvas);
            }
            mPaint.setXfermode(mDuffXfermode);//设置叠加模式
            canvas.drawBitmap(mMosaicBmp, /*mCanvasRectF.left, mCanvasRectF.top,*/M, mPaint);//画出重叠区域
            mPaint.setXfermode(null);
            canvas.restoreToCount(layerId);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float downX = event.getX();
                float downY = event.getY();
                mLastPath = new DrawPath();//每次手指下去都是一条新的路径
                mLastPath.moveTo(downX, downY);
                mPaths.add(mLastPath);
                invalidate();
                tempX = downX;
                tempY = downY;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                if (Math.abs(moveX - tempX) > 5 || Math.abs(moveY - tempY) > 5) {
                    mLastPath.quadTo(tempX, tempY, moveX, moveY);
                    invalidate();
                }
                tempX = moveX;
                tempY = moveY;
                break;
            case MotionEvent.ACTION_UP:
                mLastPath.up();
                break;
        }
        return true;
    }

    /**
     * 封装一条路径
     */
    class DrawPath {
        Path path;
        float downX;
        float downY;
        boolean quaded;

        DrawPath() {
            path = new Path();
        }

        void moveTo(float x, float y) {
            downX = x;
            downY = y;
            path.moveTo(x, y);
        }

        void quadTo(float x1, float y1, float x2, float y2) {
            quaded = true;
            path.quadTo(x1, y1, x2, y2);
        }

        void up() {
            if (!quaded) {
                //画点
                path.lineTo(downX, downY + 0.1f);
                invalidate();
            }
        }

        void draw(Canvas canvas) {
            if (path != null) {
                canvas.drawPath(path, mPaint);
            }
        }

    }
}


