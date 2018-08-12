package com.nq.pictureeditor;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.nq.pictureeditor.record.Record;

public class MosaicsRecord extends Record {

    public Path path;
    public Paint paint;

    public Matrix M;
    public RectF clip;
}
