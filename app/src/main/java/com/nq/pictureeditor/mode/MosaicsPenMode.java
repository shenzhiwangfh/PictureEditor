package com.nq.pictureeditor.mode;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class MosaicsPenMode extends PenMode {

    public MosaicsPenMode(RectF rect, RectF clipRect, Matrix m, Path path, Paint paint, RectF clip) {
        super(rect, clipRect, m, path, paint, clip);
    }

    @Override
    public int getMode() {
        return MODE_MOSAICS;
    }
}
