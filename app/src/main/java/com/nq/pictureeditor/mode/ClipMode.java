package com.nq.pictureeditor.mode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import com.nq.pictureeditor.R;

public class ClipMode extends EditMode {

    private float ICON_WIDTH;
    private float ICON_SIZE;
    private float LINE_WIDTH;
    private float CLIP_MIN_SIZE;

    public final static int MODE_NORMAL = 0x10;
    public final static int MODE_PICTURE = 0x11;
    public final static int MODE_SCALE = 0x12;
    public final static int MODE_LT_ICON = 0x13;
    public final static int MODE_RT_ICON = 0x14;
    public final static int MODE_LB_ICON = 0x15;
    public final static int MODE_RB_ICON = 0x16;
    public final static int MODE_L_LINE = 0x17;
    public final static int MODE_R_LINE = 0x18;
    public final static int MODE_T_LINE = 0x19;
    public final static int MODE_B_LINE = 0x1A;

    public boolean status = true;

    private RectF prePictureRect = new RectF();

    private RectF clipIconRect;
    private RectF iconLeftTop, iconRightTop, iconLeftBottom, iconRightBottom;
    private RectF lineLeft, lineRight, lineTop, lineBottom;

    private int clipMode;

    public ClipMode(RectF rect, RectF clipRect, Matrix m) {
        super(rect, clipRect, m);
    }

    public ClipMode(Context context) {
        //pictureRect = new RectF(rect);
        //clipPictureRect = new RectF(clipRect);
        //M = new Matrix(m);
        //super(rect, clipRect, m);
        this.status = status;

        ICON_WIDTH = context.getResources().getDimension(R.dimen.icon_width);
        ICON_SIZE = context.getResources().getDimension(R.dimen.icon_size);
        LINE_WIDTH = context.getResources().getDimension(R.dimen.line_width);
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    //public void setStatus(int status) {
    //    this.status = status;
    //}

    @Override
    public int getMode() {
        return MODE_CLIP;
    }

    public void setClipIconRect(float offsetX, float offsetY, RectF clipPictureRect) {
        float left, right, top, bottom;

        if (clipIconRect == null) {
            left = clipPictureRect.left - ICON_WIDTH;
            top = clipPictureRect.top - ICON_WIDTH;
            right = clipPictureRect.right + ICON_WIDTH;
            bottom = clipPictureRect.bottom + ICON_WIDTH;
            clipIconRect = new RectF(left, top, right, bottom);
        } else if (clipMode == ClipMode.MODE_LT_ICON) {
            left = offsetX + clipPictureRect.left - ICON_WIDTH;
            top = offsetY + clipPictureRect.top - ICON_WIDTH;
            clipIconRect.set(left, top, clipIconRect.right, clipIconRect.bottom);
        } else if (clipMode == ClipMode.MODE_RT_ICON) {
            right = offsetX + clipPictureRect.right + ICON_WIDTH;
            top = offsetY + clipPictureRect.top - ICON_WIDTH;
            clipIconRect.set(clipIconRect.left, top, right, clipIconRect.bottom);
        } else if (clipMode == ClipMode.MODE_LB_ICON) {
            left = offsetX + clipPictureRect.left - ICON_WIDTH;
            bottom = offsetY + clipPictureRect.bottom + ICON_WIDTH;
            clipIconRect.set(left, clipIconRect.top, clipIconRect.right, bottom);
        } else if (clipMode == ClipMode.MODE_RB_ICON) {
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

    public int computeClipMode(float downX, float downY, RectF clipPictureRect) {
        if (iconLeftTop.contains(downX, downY)) {
            clipMode = ClipMode.MODE_LT_ICON;
        } else if (iconRightTop.contains(downX, downY)) {
            clipMode = ClipMode.MODE_RT_ICON;
        } else if (iconLeftBottom.contains(downX, downY)) {
            clipMode = ClipMode.MODE_LB_ICON;
        } else if (iconRightBottom.contains(downX, downY)) {
            clipMode = ClipMode.MODE_RB_ICON;
        } else if (lineLeft.contains(downX, downY)) {
            clipMode = ClipMode.MODE_L_LINE;
        } else if (lineRight.contains(downX, downY)) {
            clipMode = ClipMode.MODE_R_LINE;
        } else if (lineTop.contains(downX, downY)) {
            clipMode = ClipMode.MODE_T_LINE;
        } else if (lineBottom.contains(downX, downY)) {
            clipMode = ClipMode.MODE_B_LINE;
        } else if (clipPictureRect.contains(downX, downY)) {
            clipMode = ClipMode.MODE_PICTURE;
        }

        return clipMode;
    }

    public void computeIcon(float nowX, float nowY, float downX, float downY,
                            RectF canvasRect, RectF pictureRect, RectF clipPictureRect) {
        if (clipMode == ClipMode.MODE_LT_ICON) {
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
        } else if (clipMode == ClipMode.MODE_RT_ICON) {
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
        } else if (clipMode == ClipMode.MODE_LB_ICON) {
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
        } else if (clipMode == ClipMode.MODE_RB_ICON) {
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

    public void onDraw(Canvas canvas, Paint paint) {
        //draw icon
        canvas.drawRect(iconLeftTop.left, iconLeftTop.top, iconLeftTop.right, iconLeftTop.top + ICON_WIDTH, paint);
        canvas.drawRect(iconLeftTop.left, iconLeftTop.top, iconLeftTop.left + ICON_WIDTH, iconLeftTop.bottom, paint);
        canvas.drawRect(iconRightTop.left, iconRightTop.top, iconRightTop.right, iconLeftTop.top + ICON_WIDTH, paint);
        canvas.drawRect(iconRightTop.right - ICON_WIDTH, iconRightTop.top, iconRightTop.right, iconLeftTop.bottom, paint);
        canvas.drawRect(iconLeftBottom.left, iconLeftBottom.bottom - ICON_WIDTH, iconLeftBottom.right, iconLeftBottom.bottom, paint);
        canvas.drawRect(iconLeftBottom.left, iconLeftBottom.top, iconLeftBottom.left + ICON_WIDTH, iconLeftBottom.bottom, paint);
        canvas.drawRect(iconRightBottom.left, iconRightBottom.bottom - ICON_WIDTH, iconRightBottom.right, iconRightBottom.bottom, paint);
        canvas.drawRect(iconRightBottom.right - ICON_WIDTH, iconRightBottom.top, iconRightBottom.right, iconRightBottom.bottom, paint);

        //draw line
        canvas.drawRect(lineLeft, paint);
        canvas.drawRect(lineRight, paint);
        canvas.drawRect(lineTop, paint);
        canvas.drawRect(lineBottom, paint);
    }

    public RectF newRect() {
        return new RectF(clipIconRect.left + ICON_WIDTH,
                clipIconRect.top + ICON_WIDTH,
                clipIconRect.right - ICON_WIDTH,
                clipIconRect.bottom - ICON_WIDTH);
    }

    public int clipMode() {
        return clipMode;
    }

    public void setClipMode(int clipMode) {
        this.clipMode = clipMode;
    }

    public void setPrePictureRect(RectF rect) {
        prePictureRect.set(rect);
    }

    public void setPictureRect(RectF pictureRect, Bitmap mOriginBitmap, float mZoomScale, float picOffsetX, float picOffsetY) {
        float oldWidth = prePictureRect.width();
        float oldHeight = prePictureRect.height();
        float newWidth = mOriginBitmap.getWidth() * mZoomScale;
        float newHeight = mOriginBitmap.getHeight() * mZoomScale;

        float left = prePictureRect.left - (newWidth - oldWidth) / 2 + picOffsetX;
        float right = prePictureRect.right + (newWidth - oldWidth) / 2 + picOffsetX;
        float top = prePictureRect.top - (newHeight - oldHeight) / 2 + picOffsetY;
        float bottom = prePictureRect.bottom + (newHeight - oldHeight) / 2 + picOffsetY;

        pictureRect.set(left, top, right, bottom);
        //prePictureRect.set(left, top, right, bottom);
    }

    public void setClipPictureRect(RectF pictureRect, RectF clipPictureRect, RectF canvasRect) {
        float left = Math.max(pictureRect.left, canvasRect.left);
        float right = Math.min(pictureRect.right, canvasRect.right);
        float top = Math.max(pictureRect.top, canvasRect.top);
        float bottom = Math.min(pictureRect.bottom, canvasRect.bottom);

        clipPictureRect.set(left, top, right, bottom);
    }

    public void setBitmapRect(RectF bitmapRect, Bitmap mDrawBitmap) {
        bitmapRect.set(0, 0, mDrawBitmap.getWidth(), mDrawBitmap.getHeight());
    }

    public void setClipBitmapRect(RectF pictureRect, RectF clipPictureRect, RectF bitmapRect, RectF clipBitmapRect) {
        float ratio = bitmapRect.height() / pictureRect.height();

        float left = (clipPictureRect.left - pictureRect.left) * ratio;
        float right = left + clipPictureRect.width() * ratio;
        float top = (clipPictureRect.top - pictureRect.top) * ratio;
        float bottom = top + clipPictureRect.height() * ratio;

        clipBitmapRect.set(left, top, right, bottom);
    }

    public void matrix(Matrix M, RectF bitmapRect, RectF pictureRect) {
        M.setRectToRect(bitmapRect, pictureRect, Matrix.ScaleToFit.FILL);
    }
}
