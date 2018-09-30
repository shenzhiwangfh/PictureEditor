package com.nq.pictureeditor.control;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.nq.pictureeditor.DrawInterface;
import com.nq.pictureeditor.R;
import com.nq.pictureeditor.mode.ClipMode;
import com.nq.pictureeditor.mode.ColorPenMode;
import com.nq.pictureeditor.mode.EditMode;
import com.nq.pictureeditor.mode.MosaicsPenMode;
import com.nq.pictureeditor.mode.TextMode;
import com.nq.pictureeditor.record.ModeLoopInterface;
import com.nq.pictureeditor.record.RecordManager;
import com.nq.pictureeditor.view.CornerLayout;
import com.nq.pictureeditor.view.ViewUtils;

public class ModeController implements
        CornerLayout.OnModeListener, DrawInterface {

    private SparseArray<EditMode> map = new SparseArray<>();
    private EditMode mCurrentMode;
    private ClipMode clipMode;

    private Bitmap mOriginBitmap; //
    private Bitmap mDrawBitmap; //
    //private Bitmap mMosaicBmp;
    private Canvas mDrawCanvas = new Canvas(); //

    private RecordManager mRecordManager = new RecordManager();

    private final static int ADD_RECORD = 0;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case ADD_RECORD:
                    addRecord();
                    break;
            }
            return true;
        }
    });

    public ModeController(Context context, Bitmap bitmap) {
        map.put(EditMode.MODE_CLIP, new ClipMode(context));
        map.put(EditMode.MODE_PEN, new ColorPenMode(context, mHandler));
        map.put(EditMode.MODE_MOSAICS, new MosaicsPenMode(context, mHandler));
        map.put(EditMode.MODE_TEXT, new TextMode(mHandler));

        mCurrentMode = clipMode = (ClipMode) map.get(EditMode.MODE_CLIP);
        initBitmap(context, bitmap, clipMode);
        mRecordManager.addRecord(new ClipMode(clipMode), false);
    }

    public void initBitmap(Context context, Bitmap bitmap, ClipMode clipMode) {
        mOriginBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mDrawBitmap = Bitmap.createBitmap(mOriginBitmap);
        //mMosaicBmp = ViewUtils.BitmapMosaic(mDrawBitmap, 64);
        mDrawCanvas.setBitmap(mDrawBitmap);

        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        final int screenWidth = dm.widthPixels;
        final int screenHeight = dm.heightPixels;

        RectF canvasRect, originRect;
        final float leftPadding = res.getDimension(R.dimen.picture_left_padding);
        final float topPadding = res.getDimension(R.dimen.picture_top_padding);
        final float rightPadding = res.getDimension(R.dimen.picture_right_padding);
        final float bottomPadding = res.getDimension(R.dimen.picture_bottom_padding);
        canvasRect = new RectF(leftPadding, topPadding, screenWidth - rightPadding, screenHeight - bottomPadding);

        float bitmapRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
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
        float mZoomScale = originRect.height() / bitmap.getHeight();

        clipMode.setBitmapRect(mOriginBitmap);
        clipMode.setPrePictureRect(originRect);
        clipMode.setPictureRect(mZoomScale);
        clipMode.setClipPictureRect(canvasRect);
        clipMode.setClipBitmapRect();
        clipMode.matrix();

        clipMode.setCanvasRect(canvasRect);
        clipMode.initClipIcon();
    }

    public void addRecord() {
        switch (mCurrentMode.getMode()) {
            case EditMode.MODE_CLIP: {
                ClipMode o = new ClipMode((ClipMode) mCurrentMode);
                mRecordManager.addRecord(o, false);
            }
            break;
            case EditMode.MODE_PEN: {
                ColorPenMode o = new ColorPenMode((ColorPenMode) mCurrentMode);
                mRecordManager.addRecord(o, false);
            }
            break;
            case EditMode.MODE_MOSAICS: {
                MosaicsPenMode o = new MosaicsPenMode((MosaicsPenMode) mCurrentMode);
                mRecordManager.addRecord(o, false);
            }
            break;
            case EditMode.MODE_TEXT: {

            }
            break;
        }
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        mCurrentMode.onTouchEvent(event, mDrawCanvas, mDrawBitmap);
    }

    @Override
    public void finishAction(boolean back, boolean forward) {

    }

    @Override
    public void save(Bitmap newBitmap) {

    }

    @Override
    public void share(Bitmap newBitmap) {

    }

    @Override
    public void saved() {

    }

    @Override
    public void shared() {

    }

    @Override
    public void redraw() {
        //mCurrentMode.resetDrawBitmap();

        mRecordManager.doLoop(new ModeLoopInterface() {
            @Override
            public void pickMode(EditMode mode) {
                mode.redraw(mDrawCanvas, mDrawBitmap);
            }
        });
    }

    @Override
    public void onDraw(Canvas canvas) {
        mCurrentMode.onDraw(canvas, mDrawBitmap);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        boolean ret = mCurrentMode.onScale(detector);
        return ret;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        boolean ret = mCurrentMode.onScaleBegin(detector);
        return ret;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mCurrentMode.onScaleEnd(detector);
    }

    @Override
    public void onChange(int mode) {
        if (mCurrentMode.isClipMode()) {
            mHandler.sendEmptyMessage(ADD_RECORD);
            clipMode.set(mCurrentMode);
        }

        mCurrentMode = map.get(mode);
        mCurrentMode.turnOn(clipMode, mDrawBitmap);
    }

    public EditMode getEditMode(int mode) {
        return map.get(mode);
    }
}
