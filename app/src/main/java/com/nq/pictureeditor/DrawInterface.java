package com.nq.pictureeditor;

import android.graphics.Bitmap;

public interface DrawInterface {
    void finishAction(boolean back, boolean forward);

    void save(Bitmap newBitmap);
}
