package com.nq.pictureeditor.mode;

import android.graphics.RectF;

public class ClipMode extends EditMode {
    /*
    public final static int MODE_NORMAL = 1;
    public final static int MODE_PICTURE = 2;
    public final static int MODE_SCALE = 3;
    public final static int MODE_LT_ICON = 4;
    public final static int MODE_RT_ICON = 5;
    public final static int MODE_LB_ICON = 6;
    public final static int MODE_RB_ICON = 7;
    public final static int MODE_L_LINE = 8;
    public final static int MODE_R_LINE = 9;
    public final static int MODE_T_LINE = 10;
    public final static int MODE_B_LINE = 11;

    public int clipMode = MODE_NORMAL;
    */

    public RectF pictureRect;
    public RectF clipPictureRect;

    @Override
    public int getMode() {
        return MODE_CLIP;
    }
}
