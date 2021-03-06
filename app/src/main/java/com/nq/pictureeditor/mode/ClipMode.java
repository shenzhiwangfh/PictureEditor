package com.nq.pictureeditor.mode;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.nq.pictureeditor.R;
import com.nq.pictureeditor.record.RecordManager;

public class ClipMode extends EditMode {
    private final float ICON_WIDTH = 54f;
    private final float ICON_SIZE = 96f;
    private final float LINE_WIDTH = 36;
    private final float CLIP_MIN_SIZE = 96f;

    private final int MODE_NORMAL = 0x10;
    private final int MODE_PICTURE = 0x11;
    private final int MODE_SCALE = 0x12;
    private final int MODE_LT_ICON = 0x13;
    private final int MODE_RT_ICON = 0x14;
    private final int MODE_LB_ICON = 0x15;
    private final int MODE_RB_ICON = 0x16;
    private final int MODE_L_LINE = 0x17;
    private final int MODE_R_LINE = 0x18;
    private final int MODE_T_LINE = 0x19;
    private final int MODE_B_LINE = 0x1A;

    public boolean status = true;

    private RectF clipIconRect;
    private RectF iconLeftTop, iconRightTop, iconLeftBottom, iconRightBottom;
    private RectF lineLeft, lineRight, lineTop, lineBottom;
    private RectF canvasRect = new RectF();

    private int clipMode;

    public ClipMode(Context context, Bitmap bitmap) {
        super(context);

        initBitmap(context, bitmap);
        initClipIcon();
        initPaint();
    }

    public ClipMode(ClipMode o) {
        super(o);
        initClipIcon();
        initPaint();
        this.canvasRect.set(o.canvasRect);
    }

    public void setClipMode(ClipMode o) {
        super.set(o);
        initClipIcon();
        initPaint();
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public void turnOn(EditMode clipMode) {

    }

    @Override
    public void turnOff() {

    }

    //@Override
    public void redraw(Canvas mDrawCanvas) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data, Canvas mDrawCanvas) {

    }

    private void initBitmap(Context context, Bitmap bitmap) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        final int screenWidth = dm.widthPixels;
        final int screenHeight = dm.heightPixels;

        RectF originRect;
        final float leftPadding = res.getDimension(R.dimen.picture_left_padding);
        final float topPadding = res.getDimension(R.dimen.picture_top_padding);
        final float rightPadding = res.getDimension(R.dimen.picture_right_padding);
        final float bottomPadding = res.getDimension(R.dimen.picture_bottom_padding);
        canvasRect.set(leftPadding, topPadding, screenWidth - rightPadding, screenHeight - bottomPadding);

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

        setBitmapRect(bitmap);
        setPrePictureRect(originRect);
        setPictureRect(mZoomScale);
        setClipPictureRect(canvasRect);
        setClipBitmapRect();
        matrix();

        //setCanvasRect(canvasRect);
    }

    public void initPaint() {
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setColor(Color.RED);
    }

    public void initClipIcon() {
        setClipIconRect(0, 0, clipPictureRect);
    }

    @Override
    public int getMode() {
        return MODE_CLIP;
    }

    private void setClipIconRect(float offsetX, float offsetY, RectF clipPictureRect) {
        float left, right, top, bottom;

        if (clipIconRect == null) {
            left = clipPictureRect.left - ICON_WIDTH;
            top = clipPictureRect.top - ICON_WIDTH;
            right = clipPictureRect.right + ICON_WIDTH;
            bottom = clipPictureRect.bottom + ICON_WIDTH;
            clipIconRect = new RectF(left, top, right, bottom);
        } else if (clipMode == MODE_LT_ICON) {
            left = offsetX + clipPictureRect.left - ICON_WIDTH;
            top = offsetY + clipPictureRect.top - ICON_WIDTH;
            clipIconRect.set(left, top, clipIconRect.right, clipIconRect.bottom);
        } else if (clipMode == MODE_RT_ICON) {
            right = offsetX + clipPictureRect.right + ICON_WIDTH;
            top = offsetY + clipPictureRect.top - ICON_WIDTH;
            clipIconRect.set(clipIconRect.left, top, right, clipIconRect.bottom);
        } else if (clipMode == MODE_LB_ICON) {
            left = offsetX + clipPictureRect.left - ICON_WIDTH;
            bottom = offsetY + clipPictureRect.bottom + ICON_WIDTH;
            clipIconRect.set(left, clipIconRect.top, clipIconRect.right, bottom);
        } else if (clipMode == MODE_RB_ICON) {
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

    private void computeClipMode(float downX, float downY) {
        if (iconLeftTop.contains(downX, downY)) {
            clipMode = MODE_LT_ICON;
        } else if (iconRightTop.contains(downX, downY)) {
            clipMode = MODE_RT_ICON;
        } else if (iconLeftBottom.contains(downX, downY)) {
            clipMode = MODE_LB_ICON;
        } else if (iconRightBottom.contains(downX, downY)) {
            clipMode = MODE_RB_ICON;
        } else if (lineLeft.contains(downX, downY)) {
            clipMode = MODE_L_LINE;
        } else if (lineRight.contains(downX, downY)) {
            clipMode = MODE_R_LINE;
        } else if (lineTop.contains(downX, downY)) {
            clipMode = MODE_T_LINE;
        } else if (lineBottom.contains(downX, downY)) {
            clipMode = MODE_B_LINE;
        } else if (clipPictureRect.contains(downX, downY)) {
            clipMode = MODE_PICTURE;
        }
        //return clipMode;
    }

    private void computeIcon(float nowX, float nowY, float downX, float downY,
                             RectF canvasRect, RectF pictureRect, RectF clipPictureRect) {
        if (clipMode == MODE_LT_ICON) {
            float clipOffsetX = nowX - downX;
            float clipOffsetY = nowY - downY;

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

            setClipIconRect(clipOffsetX, clipOffsetY, clipPictureRect);
        } else if (clipMode == MODE_RT_ICON) {
            float clipOffsetX = nowX - downX;
            float clipOffsetY = nowY - downY;

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

            setClipIconRect(clipOffsetX, clipOffsetY, clipPictureRect);
        } else if (clipMode == MODE_LB_ICON) {
            float clipOffsetX = nowX - downX;
            float clipOffsetY = nowY - downY;

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

            setClipIconRect(clipOffsetX, clipOffsetY, clipPictureRect);
        } else if (clipMode == MODE_RB_ICON) {
            float clipOffsetX = nowX - downX;
            float clipOffsetY = nowY - downY;

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

            setClipIconRect(clipOffsetX, clipOffsetY, clipPictureRect);
        }
    }

    @Override
    public void onDraw(Canvas canvas, Bitmap mDrawBitmap) {
        super.onDraw(canvas, mDrawBitmap);
        drawClip(canvas, mDrawBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, Canvas mDrawCanvas) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                computeClipMode(downX, downY);
                break;
            case MotionEvent.ACTION_MOVE:
                if (clipMode >= MODE_LT_ICON && clipMode <= MODE_B_LINE) {
                    computeIcon(event.getX(), event.getY(), downX, downY,
                            canvasRect, pictureRect, clipPictureRect);
                } else if (clipMode == MODE_PICTURE) {
                    picOffsetX = event.getX() - downX;
                    picOffsetY = event.getY() - downY;

                    setPictureRect(mZoomScale);
                    matrix();
                }
                break;
            case MotionEvent.ACTION_UP:
                cluRect();
                RecordManager.getInstance().addRecord(new ClipMode(this), false);
                break;
        }
        return true;
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

        setPictureRect(mZoomScale);
        matrix();
        setPrePictureRect(pictureRect);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        clipMode = MODE_SCALE;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        cluRect();
    }

    private void cluRect() {
        if (clipMode == MODE_SCALE) {
            clipMode = MODE_NORMAL;

            float widthScale = 1.0f, heightScale = 1.0f;
            if (clipPictureRect.width() > pictureRect.width()) {
                widthScale = clipPictureRect.width() / pictureRect.width();
            }
            if (clipPictureRect.height() > pictureRect.height()) {
                heightScale = clipPictureRect.height() / pictureRect.height();
            }
            float zoomScale = Math.max(widthScale, heightScale);
            mZoomScale = pictureRect.height() * zoomScale / bitmapRect.height();

            setPictureRect(mZoomScale);
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

            setPictureRect(mZoomScale);
            setClipBitmapRect();
            matrix();
            //invalidate();
        } else if (clipMode == MODE_PICTURE) {
            clipMode = MODE_NORMAL;

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

            setPictureRect(mZoomScale);
            setClipBitmapRect();
            matrix();
        } else if (clipMode >= MODE_LT_ICON && clipMode <= MODE_B_LINE) {
            //把裁剪区域放大后，计算picture 的rect，其他就全部可以顺势算出了
            clipMode = MODE_NORMAL;

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
            mZoomScale = pictureRect.height() / bitmapRect.height();
            clipPictureRect.set(tmpClipLarge);

            setClipBitmapRect();
            matrix();
            setClipIconRect(0, 0, clipPictureRect);
        }

        picOffsetX = 0;
        picOffsetY = 0;
        setPrePictureRect(pictureRect);
    }

    private void drawClip(Canvas canvas, Bitmap bitmap) {
        canvas.save();
        mDrawPaint.setAlpha(100); // setColor 和 setAlpha 的顺序有关系
        canvas.drawBitmap(bitmap, M, mDrawPaint);
        canvas.restore();

        //draw icon
        canvas.drawRect(iconLeftTop.left, iconLeftTop.top, iconLeftTop.right, iconLeftTop.top + ICON_WIDTH, mDrawPaint);
        canvas.drawRect(iconLeftTop.left, iconLeftTop.top, iconLeftTop.left + ICON_WIDTH, iconLeftTop.bottom, mDrawPaint);
        canvas.drawRect(iconRightTop.left, iconRightTop.top, iconRightTop.right, iconLeftTop.top + ICON_WIDTH, mDrawPaint);
        canvas.drawRect(iconRightTop.right - ICON_WIDTH, iconRightTop.top, iconRightTop.right, iconLeftTop.bottom, mDrawPaint);
        canvas.drawRect(iconLeftBottom.left, iconLeftBottom.bottom - ICON_WIDTH, iconLeftBottom.right, iconLeftBottom.bottom, mDrawPaint);
        canvas.drawRect(iconLeftBottom.left, iconLeftBottom.top, iconLeftBottom.left + ICON_WIDTH, iconLeftBottom.bottom, mDrawPaint);
        canvas.drawRect(iconRightBottom.left, iconRightBottom.bottom - ICON_WIDTH, iconRightBottom.right, iconRightBottom.bottom, mDrawPaint);
        canvas.drawRect(iconRightBottom.right - ICON_WIDTH, iconRightBottom.top, iconRightBottom.right, iconRightBottom.bottom, mDrawPaint);

        //draw line
        canvas.drawRect(lineLeft, mDrawPaint);
        canvas.drawRect(lineRight, mDrawPaint);
        canvas.drawRect(lineTop, mDrawPaint);
        canvas.drawRect(lineBottom, mDrawPaint);
    }
}
