package com.nq.pictureeditor.view;

import android.graphics.PointF;

public class ViewUtils {

    public static boolean contain(PointF point, float radius, float x, float y, float scaleSize) {
        float h = Math.abs(x - point.x);
        float v = Math.abs(y - point.y);
        float r = (float) Math.pow(Math.pow(h, 2) + Math.pow(v, 2), 0.5);
        return (r < (radius + scaleSize));
    }

    public static boolean contain(PointF center, float inner, float outer, float x, float y, float scaleSize) {
        float h = Math.abs(x - center.x);
        float v = Math.abs(y - center.y);
        float r = (float) Math.pow(Math.pow(h, 2) + Math.pow(v, 2), 0.5);
        return (r >= (inner - scaleSize)) && (r <= (outer + scaleSize));
    }
}
