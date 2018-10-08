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
import com.nq.pictureeditor.SaveImageTask;
import com.nq.pictureeditor.ShareImageTask;
import com.nq.pictureeditor.mode.ClipMode;
import com.nq.pictureeditor.mode.ColorPenMode;
import com.nq.pictureeditor.mode.EditMode;
import com.nq.pictureeditor.mode.MosaicsPenMode;
import com.nq.pictureeditor.mode.TextMode;
import com.nq.pictureeditor.record.ModeLoopInterface;
import com.nq.pictureeditor.record.RecordManager;
import com.nq.pictureeditor.view.ArcColorPicker;
import com.nq.pictureeditor.view.ArcSeekBar;
import com.nq.pictureeditor.view.CornerLayout;
import com.nq.pictureeditor.view.ViewUtils;

public class ModeController implements
        CornerLayout.OnModeListener, DrawInterface {

    private Context mContext;

    private SparseArray<EditMode> map = new SparseArray<>();
    private EditMode mCurrentMode;
    private ClipMode clipMode;
    private ColorPenMode colorPenMode;
    private MosaicsPenMode mosaicsPenMode;
    private TextMode textMode;

    private Bitmap mOriginBitmap;
    private Bitmap mDrawBitmap;
    private Bitmap mMosaicBmp;
    private Canvas mDrawCanvas = new Canvas();

    private RecordManager mRecordManager;// = new RecordManager();


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return true;
        }
    });

    public ModeController(Context context, Bitmap bitmap) {
        mContext = context;

        clipMode = new ClipMode(context);
        colorPenMode = new ColorPenMode(context, mHandler);
        mosaicsPenMode = new MosaicsPenMode(context, mHandler);
        textMode = new TextMode(mHandler);

        map.put(EditMode.MODE_CLIP, clipMode);
        map.put(EditMode.MODE_PEN, colorPenMode);
        map.put(EditMode.MODE_MOSAICS, mosaicsPenMode);
        map.put(EditMode.MODE_TEXT, textMode);
        mCurrentMode = clipMode;

        initBitmap(context, bitmap, clipMode);
        colorPenMode.set(clipMode);
        mosaicsPenMode.set(clipMode);
        textMode.set(clipMode);

        mRecordManager = RecordManager.getInstance();
        mRecordManager.addRecord(new ClipMode(clipMode), false);
    }

    public void initBitmap(Context context, Bitmap bitmap, ClipMode clipMode) {
        mOriginBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mDrawBitmap = Bitmap.createBitmap(mOriginBitmap);
        mMosaicBmp = ViewUtils.BitmapMosaic(mDrawBitmap, 64);
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
        //clipMode.setZoomScale(mZoomScale);
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        if (mCurrentMode.isPenMode()) {
            mCurrentMode.onTouchEvent(event, mDrawCanvas, mDrawBitmap);
        } else if (mCurrentMode.isMosaicsMode()) {
            mCurrentMode.onTouchEvent(event, mDrawCanvas, mMosaicBmp);
        } else {
            mCurrentMode.onTouchEvent(event, mDrawCanvas, mDrawBitmap);
        }
    }

    @Override
    public void finishAction(boolean back, boolean forward) {

    }

    @Override
    public void saved() {
        redraw();
        Bitmap newBitmap = mCurrentMode.saveBitmap(mDrawBitmap);
        new SaveImageTask(mContext, this).execute(newBitmap);
    }

    @Override
    public void shared() {
        redraw();
        Bitmap newBitmap = mCurrentMode.saveBitmap(mDrawBitmap);
        new ShareImageTask(mContext, this).execute(newBitmap);
    }

    @Override
    public void redraw() {
        mDrawBitmap.recycle();
        mDrawBitmap = Bitmap.createBitmap(mOriginBitmap);
        mDrawCanvas.setBitmap(mDrawBitmap);

        mRecordManager.doLoop(new ModeLoopInterface() {
            @Override
            public void pickMode(EditMode mode) {
                if (mode.isPenMode()) {
                    mode.redraw(mDrawCanvas, mDrawBitmap);
                } else if (mode.isMosaicsMode()) {
                    mode.redraw(mDrawCanvas, mMosaicBmp);
                }
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
        mCurrentMode = map.get(mode);
        mCurrentMode.turnOn(clipMode);
    }

    public EditMode getEditMode(int mode) {
        return map.get(mode);
    }

    public void setModeChangeListener(CornerLayout layout) {
        layout.setOnModeListener(this);
    }

    public void setPenColorListener(ArcColorPicker picker) {
        picker.setOnPickListener(colorPenMode);
    }

    public void setPenSizeListener(ArcSeekBar bar) {
        bar.setOnSlideListener(colorPenMode);
    }

    public void setMosaicSizeListener(ArcSeekBar bar) {
        bar.setOnSlideListener(mosaicsPenMode);
    }
}
