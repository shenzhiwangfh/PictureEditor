package com.nq.pictureeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nq.pictureeditor.log.LogUtils;
import com.nq.pictureeditor.mode.EditMode;
import com.nq.pictureeditor.record.ModeLoopInterface;
import com.nq.pictureeditor.record.RecordManager;
import com.nq.pictureeditor.task.LoadImageTask;
import com.nq.pictureeditor.task.LoadListener;

public class PickPicture extends AppCompatActivity implements View.OnClickListener, LoadListener {

    private final static String TAG = "PickPicture";
    private final static int REQUEST_CODE_QUERY_PIC = 0;
    private final static int REQUEST_CODE_AFTER_EDIT = 1;

    private ImageView mSource;
    private ImageView mResult;

    private Uri mImageCaptureUri;
    private Canvas canvas = new Canvas();
    private Bitmap drawBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_picture);

        mSource = findViewById(R.id.source);
        mResult = findViewById(R.id.result);

        mSource.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.source) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*"); //选择图片
            //intent.setType("audio/*"); //选择音频
            //intent.setType("video/*"); //选择视频 （mp4/3gp 是android支持的视频格式）
            //intent.setType("video/*;image/*"); //同时选择视频和图片
            //intent.setType("*/*"); //无类型限制
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_CODE_QUERY_PIC);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_QUERY_PIC) {
                mImageCaptureUri = data.getData();
                Intent intent = new Intent(this, DrawActivity.class);
                intent.setData(mImageCaptureUri);
                startActivityForResult(intent, REQUEST_CODE_AFTER_EDIT);
            } else if (requestCode == REQUEST_CODE_AFTER_EDIT) {
                if (mImageCaptureUri != null)
                    new LoadImageTask(this, this).execute(mImageCaptureUri);
            }
        }
    }

    @Override
    public void setBitmap(Bitmap bitmap) {
        mSource.setImageBitmap(bitmap);

        if (drawBitmap != null) drawBitmap.recycle();
        drawBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvas.setBitmap(drawBitmap);
        RecordManager.getInstance().doLoop(new ModeLoopInterface() {
            @Override
            public boolean pickMode(EditMode mode) {
                mode.redraw(canvas);
                return false;
            }
        });
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        canvas.drawRect(RecordManager.getInstance().getCurrentMode().clipBitmapRect, paint);

        mResult.setImageBitmap(drawBitmap);
    }
}
