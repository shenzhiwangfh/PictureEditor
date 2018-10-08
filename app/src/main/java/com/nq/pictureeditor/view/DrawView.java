package com.nq.pictureeditor.view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class DrawView extends View implements ScaleGestureDetector.OnScaleGestureListener {

    private final static String TAG = "DrawView";

    private Context mContext;
    private ScaleGestureDetector mGesture;

    private DrawInterface draw;

    public void setController(DrawInterface draw) {
        this.draw = draw;
    }

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mGesture = new ScaleGestureDetector(mContext, this);
    }

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    invalidate();
                    break;
                case 1:
                    invalidateBtn();
                    break;
            }
            return true;
        }
    });

    /*
    private void buildEraserPaint() {
        mEraserPaint.setAlpha(0);
        //下面这句代码是橡皮擦设置的重点
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
    }
    */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() <= 1) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    draw.onTouchEvent(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    draw.onTouchEvent(event);
                    invalidate();
                    invalidateBtn();
                    break;
                case MotionEvent.ACTION_UP:
                    draw.onTouchEvent(event);
                    invalidate();
                    invalidateBtn();
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
        draw.onDraw(canvas);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        boolean ret = draw.onScale(detector);
        invalidate();
        invalidateBtn();
        return ret;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        boolean ret = draw.onScaleBegin(detector);
        invalidate();
        invalidateBtn();
        return ret;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        draw.onScaleEnd(detector);
        invalidate();
        invalidateBtn();
    }

    private void invalidateBtn() {
        /*
        if (draw != null) {
            int index = mRecordManager.getIndex();
            boolean back = index > 0;
            boolean forward = index < (mRecordManager.size() - 1);
            draw.finishAction(back, forward);
        }
        */
    }

    /*
    public int goBack() {
        EditMode preEditMode = mRecordManager.getPreMode();
        int mode = preEditMode.getMode();
        switch (mode) {
            case EditMode.MODE_CLIP:
            case EditMode.MODE_PEN:
            case EditMode.MODE_MOSAICS:
                //pictureRect.set(preEditMode.pictureRect);
                //clipPictureRect.set(preEditMode.clipPictureRect);
                //M.set(preEditMode.M);
                //mClipMode.setClipBitmapRect(pictureRect, clipPictureRect, bitmapRect, clipBitmapRect);
                break;
        }

        //editMode2 = mode;
        mRecordManager.back();
        invalidateBtn();
        redrawBitmap();

        return mode;
    }

    public int goForward() {
        EditMode nextEditMode = mRecordManager.getNextMode();
        int mode = nextEditMode.getMode();
        switch (mode) {
            case EditMode.MODE_CLIP:
            case EditMode.MODE_PEN:
            case EditMode.MODE_MOSAICS:
                //M.set(nextEditMode.M);
                //mClipMode.setClipBitmapRect(pictureRect, clipPictureRect, bitmapRect, clipBitmapRect);
                break;
        }

        //editMode2 = mode;
        mRecordManager.forward();
        invalidateBtn();
        redrawBitmap();
        return mode;
    }
    */

    /*
    public void setMode(int mode) {
        EditMode editMode;
        if(mCurrentMode.isClipMode()) {
            editMode = (ClipMode)((ClipMode) mCurrentMode).clone();
            mRecordManager.addRecord(editMode, false);
        } else if(mCurrentMode.isPenMode()) {
            editMode = (ColorPenMode)((ColorPenMode) mCurrentMode).clone();
            mRecordManager.addRecord(editMode, false);
        } else if(mCurrentMode.isMosaicsMode()) {
            editMode = (MosaicsPenMode)((MosaicsPenMode) mCurrentMode).clone();
            mRecordManager.addRecord(editMode, false);
        } else if(mCurrentMode.isTextMode()) {
            editMode = (TextMode)((TextMode) mCurrentMode).clone();
            mRecordManager.addRecord(editMode, false);
        }

        mCurrentMode = mEditMode.get(mode);
        invalidate();
        invalidateBtn();

        mCurrentMode.turnOn();
    }
    */
}
