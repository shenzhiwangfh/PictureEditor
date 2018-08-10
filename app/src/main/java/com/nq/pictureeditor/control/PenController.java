package com.nq.pictureeditor.control;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import com.nq.pictureeditor.R;
import com.nq.pictureeditor.view.ArcColorPicker;
import com.nq.pictureeditor.view.ArcSeekBar;
import com.nq.pictureeditor.view.OnShowListener;
import com.nq.pictureeditor.view.Preview;

public class PenController implements ArcColorPicker.OnPickListener, ArcSeekBar.OnSlideListener, OnShowListener {

    private final static String TAG = "PenController";

    private final Context mContext;

    private static final Object mLock = new Object();
    private static PenController mInstance;

    private ArcColorPicker mPenPicker;
    private ArcSeekBar mPenSizePicker;
    private Preview mPreview;

    private int color;
    private int size;
    private boolean eraser = false;

    private PenController(Activity activity, Context context) {
        mContext = context;

        mPenPicker = activity.findViewById(R.id.pen_color);
        mPenSizePicker = activity.findViewById(R.id.pen_size);

        mPenPicker.setOnPickListener(this);
        mPenPicker.setOnShowListener(this);
        mPenSizePicker.setOnSlideListener(this);
        mPenSizePicker.setOnShowListener(this);

        int index = 0;//mPenPicker.getDefauleValue();
        TypedArray array = mContext.getResources().obtainTypedArray(R.array.pen_colors);
        int resId = array.getResourceId(index, 0);
        array.recycle();
        color = mContext.getResources().getColor(resId, null);
        size = mPenSizePicker.getDefaultSize();

        mPreview = activity.findViewById(R.id.preview);
        mPreview.init(color, size);
    }

    public static PenController getInstance(Activity activity) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new PenController(activity, activity.getApplicationContext());
            }
            return mInstance;
        }
    }

    @Override
    public void onPick(View view, int resId) {
        int id = view.getId();
        if (id == R.id.pen_color) {
            try {
                eraser = false;
                color = mContext.getResources().getColor(resId, null);
                mPreview.setPenColor(color);
                if(pi != null) pi.setPaint(color, size, eraser);
            } catch (Resources.NotFoundException e) {
                eraser = true;
                BitmapDrawable drawable = (BitmapDrawable) mContext.getResources().getDrawable(resId, null);
                mPreview.setBitmap(drawable.getBitmap());
                if(pi != null) pi.setPaint(color, size, eraser);
            }
        }
    }

    @Override
    public void onSlide(View view, int value) {
        int id = view.getId();
        if (id == R.id.pen_size) {
            size = value;
            mPreview.setPenSize(value);
            if(pi != null) pi.setPaint(color, size, eraser);
        }
    }

    @Override
    public void onShow(View view, boolean show) {
        int id = view.getId();
        if (id == R.id.pen_color || id == R.id.pen_size) {
            mPreview.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private PenInterface pi;
    public void setPenInterface(PenInterface pi) {
        this.pi = pi;
    }

    public void initPen() {
        if(pi != null) pi.setPaint(color, size, eraser);
    }
}
