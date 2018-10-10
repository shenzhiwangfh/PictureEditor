package com.nq.pictureeditor.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.media.Image;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.nq.pictureeditor.view.DrawInterface;
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
import com.nq.pictureeditor.view.DrawView;
import com.nq.pictureeditor.view.ViewUtils;

public class ModeController implements
        CornerLayout.OnModeListener, DrawInterface, View.OnClickListener {

    private Context mContext;

    private SparseArray<EditMode> map = new SparseArray<>();
    private EditMode mCurrentMode;
    private ClipMode clipMode;
    private ColorPenMode colorPenMode;
    private MosaicsPenMode mosaicsPenMode;
    private TextMode textMode;

    private Bitmap mOriginBitmap;
    private Bitmap mDrawBitmap;
    //private Bitmap mMosaicBmp;
    private Canvas mDrawCanvas = new Canvas();

    private RecordManager mRecordManager;// = new RecordManager();
    private Handler mHandler;

    private ImageView mSave;
    private ImageView mShare;
    private ImageView mBack;
    private ImageView mForward;
    private DrawView mDrawView;
    private CornerLayout mCornerLayout;

    public ModeController(Context context, Bitmap bitmap) {
        mContext = context;

        clipMode = new ClipMode(context);
        colorPenMode = new ColorPenMode(context, mHandler);
        mosaicsPenMode = new MosaicsPenMode(context, mHandler, bitmap);
        textMode = new TextMode(context, mHandler);

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
        mRecordManager.clear();
        mRecordManager.addRecord(new ClipMode(clipMode), false);
    }

    public void initActionBtn(Activity activity) {
        mSave = activity.findViewById(R.id.save);
        mShare = activity.findViewById(R.id.share);
        mBack = activity.findViewById(R.id.back);
        mForward = activity.findViewById(R.id.forward);

        mBack.setEnabled(false);
        mForward.setEnabled(false);
        mSave.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mForward.setOnClickListener(this);

        refreshBtn();
        //redraw();
    }

    private void initBitmap(Context context, Bitmap bitmap, ClipMode clipMode) {
        mOriginBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mDrawBitmap = Bitmap.createBitmap(mOriginBitmap);
        mDrawCanvas.setBitmap(mDrawBitmap);
        //mMosaicBmp = ViewUtils.BitmapMosaic(mDrawBitmap, 64);

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
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.save: {
                mSave.setEnabled(false);
                saved();
            }
            break;
            case R.id.share: {
                mShare.setEnabled(false);
                shared();
            }
            break;
            case R.id.back: {
                mCurrentMode = mRecordManager.back();
                mCornerLayout.setIndex(mCurrentMode.getMode());
                //clipMode.set(mode);
                //onChange(mCurrentMode.getMode());
                refreshBtn();
                redraw();
            }
            break;
            case R.id.forward: {
                mCurrentMode = mRecordManager.forward();
                mCornerLayout.setIndex(mCurrentMode.getMode());
                //clipMode.set(mode);
                //onChange(mCurrentMode.getMode());
                refreshBtn();
                redraw();
            }
            break;
        }
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        if (mCurrentMode.isTextMode()) {
            mCurrentMode.onTouchEvent(event, mDrawCanvas);
        } else if (mCurrentMode.isPenMode()) {
            mCurrentMode.onTouchEvent(event, mDrawCanvas);
        } else if (mCurrentMode.isMosaicsMode()) {
            mCurrentMode.onTouchEvent(event, mDrawCanvas);
        } else {
            mCurrentMode.onTouchEvent(event, mDrawCanvas);
        }

        if(event.getAction() == MotionEvent.ACTION_UP) {
            refreshBtn();
        }
    }

    public void saved() {
        redraw();
        Bitmap newBitmap = mCurrentMode.saveBitmap(mDrawBitmap);
        new SaveImageTask(mContext, this).execute(newBitmap);
    }

    public void shared() {
        redraw();
        Bitmap newBitmap = mCurrentMode.saveBitmap(mDrawBitmap);
        new ShareImageTask(mContext, this).execute(newBitmap);
    }

    private void redraw() {
        mDrawBitmap.recycle();
        mDrawBitmap = Bitmap.createBitmap(mOriginBitmap);
        mDrawCanvas.setBitmap(mDrawBitmap);

        mRecordManager.doLoop(new ModeLoopInterface() {
            @Override
            public boolean pickMode(EditMode mode) {
                mode.redraw(mDrawCanvas);
                return false;
            }
        });

        mDrawView.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        mCurrentMode.onDraw(canvas, mDrawBitmap);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return mCurrentMode.onScale(detector);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return mCurrentMode.onScaleBegin(detector);
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mCurrentMode.onScaleEnd(detector);
    }

    @Override
    public void onChange(int mode) {
        mCurrentMode = map.get(mode);
        mCurrentMode.turnOn(clipMode);
        mDrawView.invalidate();
    }

    public EditMode getEditMode(int mode) {
        return map.get(mode);
    }

    public void setModeChangeListener(CornerLayout layout) {
        mCornerLayout = layout;
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

    public void setDrawListener(DrawView drawView) {
        mDrawView = drawView;
        drawView.setController(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCurrentMode.onActivityResult(requestCode, resultCode, data, mDrawCanvas);
        redraw();
    }

    private void refreshBtn() {
        mBack.setEnabled(mRecordManager.canBack());
        mForward.setEnabled(mRecordManager.canForward());
    }
}
