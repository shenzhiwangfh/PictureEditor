package com.nq.pictureeditor;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class PenRecord extends Record {
    public Path path;
    public Paint paint;

    public Matrix M;
    public RectF clip;
}
