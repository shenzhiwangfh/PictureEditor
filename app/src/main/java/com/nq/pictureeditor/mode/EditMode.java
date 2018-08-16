package com.nq.pictureeditor.mode;

import android.graphics.Matrix;
import android.graphics.RectF;

public abstract class EditMode {
    public final static int MODE_CLIP = 0;
    public final static int MODE_PEN = 1;
    public final static int MODE_MOSAICS = 2;
    public final static int MODE_TEXT = 3;

    public RectF pictureRect;
    public RectF clipPictureRect;
    public Matrix M;

    public EditMode(RectF rect, RectF clipRect, Matrix m) {
        pictureRect = new RectF(rect);
        clipPictureRect = new RectF(clipRect);
        M = new Matrix(m);
    }

    public abstract int getMode();
}
