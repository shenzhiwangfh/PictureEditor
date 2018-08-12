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

    public RectF pictureRect;
    public RectF clipPictureRect;

    @Override
    public int getMode() {
        return MODE_CLIP;
    }
}
