package com.nq.pictureeditor.mode;

import android.graphics.Matrix;
import android.graphics.RectF;

public class ClipMode extends EditMode {
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

    public ClipMode(RectF rect, RectF clipRect, Matrix m) {
        super(rect, clipRect, m);
    }

    public ClipMode(RectF rect, RectF clipRect, Matrix m, boolean status) {
        //pictureRect = new RectF(rect);
        //clipPictureRect = new RectF(clipRect);
        //M = new Matrix(m);
        super(rect, clipRect, m);
        this.status = status;
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
}
