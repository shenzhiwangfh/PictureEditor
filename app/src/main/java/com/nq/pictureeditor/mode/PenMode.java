package com.nq.pictureeditor.mode;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class PenMode extends EditMode {
    public Path path;
    public Paint paint;

    public Matrix M;
    public RectF clip;
}
