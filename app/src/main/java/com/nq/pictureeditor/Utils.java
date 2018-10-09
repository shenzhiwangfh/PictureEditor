package com.nq.pictureeditor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.TypedValue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    private static final String SCREENSHOT_FILE_NAME_TEMPLATE = "Screenshot_%s.jpeg";
    private static final String SCREENSHOTS_DIR_NAME = "Screenshots";

    public static String createScreenName() {
        long imageTime = System.currentTimeMillis();
        String imageDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(imageTime));
        String imageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, imageDate);

        File screenshotDir;
        screenshotDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), SCREENSHOTS_DIR_NAME);

        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs();
        }
        String imageFilePath = new File(screenshotDir, imageFileName).getAbsolutePath();
        return imageFilePath;
    }

    public static Point mapped(Matrix M, float x, float y) {
        float[] srcPoint = new float[]{x, y};
        float[] mapPoint = new float[2];
        Matrix invMat = new Matrix(M);
        M.invert(invMat);
        invMat.mapPoints(mapPoint, srcPoint);
        Point mapped = new Point((int) mapPoint[0], (int) mapPoint[1]);

        return mapped;
    }

    public static Bitmap getBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }

    public static float textSize2paintSize(float size) {
        Resources r = Resources.getSystem();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, r.getDisplayMetrics());
    }
}
