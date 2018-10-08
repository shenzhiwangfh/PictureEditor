package com.nq.pictureeditor.view;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public interface DrawInterface {
    void onTouchEvent(MotionEvent event);
    void onDraw(Canvas canvas);
    boolean onScale(ScaleGestureDetector detector);
    boolean onScaleBegin(ScaleGestureDetector detector);
    void onScaleEnd(ScaleGestureDetector detector);
}
