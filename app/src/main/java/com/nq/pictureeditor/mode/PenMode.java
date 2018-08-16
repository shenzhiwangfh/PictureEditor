package com.nq.pictureeditor.mode;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class PenMode extends EditMode {
    public Path path;
    public Paint paint;
    public RectF clip;

    public PenMode(RectF rect, RectF clipRect, Matrix m, Path path, Paint paint, RectF clip) {
        super(rect, clipRect, m);
        this.path = new Path(path);
        this.paint = new Paint(paint);
        this.clip = new RectF(clip);
    }

    @Override
    public int getMode() {
        return 0;
    }
}
