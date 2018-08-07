package com.nq.pictureeditor.mode;

public abstract class EditMode {
    public final static int MODE_CLIP = 0x10;
    public final static int MODE_PEN = 0x20;
    public final static int MODE_MOSAIC = 0x30;
    public final static int MODE_TEXT = 0x40;

    //private int mode = MODE_CLIP;
    //private ClipMode clipMode = new ClipMode();
    //private PenMode penMode = new PenMode();

    //public void setMode(int mdoe) {
    //    this.mode = mode;
    //}

    public abstract int getMode();
}
