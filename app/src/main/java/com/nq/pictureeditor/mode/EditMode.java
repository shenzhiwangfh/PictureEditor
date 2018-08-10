package com.nq.pictureeditor.mode;

public abstract class EditMode {
    public final static int MODE_CLIP = 0;
    public final static int MODE_PEN = 1;
    public final static int MODE_MOSAICS = 2;
    public final static int MODE_TEXT = 3;

    //private int mode = MODE_CLIP;
    //private ClipMode clipMode = new ClipMode();
    //private PenMode penMode = new PenMode();

    //public void setMode(int mdoe) {
    //    this.mode = mode;
    //}

    public abstract int getMode();
}
