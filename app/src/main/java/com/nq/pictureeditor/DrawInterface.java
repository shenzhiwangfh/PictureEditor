package com.nq.pictureeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public interface DrawInterface {
    void finishAction(boolean back, boolean forward);
    void saved();
    void shared();
    void redraw();
    void onTouchEvent(MotionEvent event);
    void onDraw(Canvas canvas);
    boolean onScale(ScaleGestureDetector detector);
    boolean onScaleBegin(ScaleGestureDetector detector);
    void onScaleEnd(ScaleGestureDetector detector);
}
