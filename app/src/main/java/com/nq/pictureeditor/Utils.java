package com.nq.pictureeditor;

import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Environment;

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
}
