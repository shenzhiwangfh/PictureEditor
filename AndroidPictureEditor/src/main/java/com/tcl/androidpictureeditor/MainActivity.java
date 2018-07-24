package com.tcl.androidpictureeditor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button mPicker;
    private ImageView mCropImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPicker = findViewById(R.id.picker);
        mCropImage = findViewById(R.id.crop_image);

        mPicker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ShowPickDialog();
            }
        });
    }

    private void ShowPickDialog() {
        new AlertDialog.Builder(this)
                .setTitle("设置头像...")
                .setNegativeButton("相册", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        /**
                         * 刚开始，我自己也不知道ACTION_PICK是干嘛的，后来直接看Intent源码，
                         * 可以发现里面很多东西，Intent是个很强大的东西，大家一定仔细阅读下
                         */
                        Intent intent = new Intent(Intent.ACTION_PICK, null);

                        /**
                         * 下面这句话，与其它方式写是一样的效果，如果：
                         * intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                         * intent.setType(""image/*");设置数据类型
                         * 如果朋友们要限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
                         * 这个地方小马有个疑问，希望高手解答下：就是这个数据URI与类型为什么要分两种形式来写呀？有什么区别？
                         */
                        intent.setDataAndType(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                "image/*");
                        startActivityForResult(intent, 1);

                    }
                })
                .setPositiveButton("拍照", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        /**
                         * 下面这句还是老样子，调用快速拍照功能，至于为什么叫快速拍照，大家可以参考如下官方
                         * 文档，you_sdk_path/docs/guide/topics/media/camera.html
                         * 我刚看的时候因为太长就认真看，其实是错的，这个里面有用的太多了，所以大家不要认为
                         * 官方文档太长了就不看了，其实是错的，这个地方小马也错了，必须改正
                         */
                        Intent intent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        //下面这句指定调用相机拍照后的照片存储的路径
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
                                .fromFile(new File(Environment
                                        .getExternalStorageDirectory(),
                                        "xiaoma.jpg")));
                        startActivityForResult(intent, 2);
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // 如果是直接从相册获取
            case 1:
                startPhotoZoom(data.getData());
                break;
            // 如果是调用相机拍照时
            case 2:
                File temp = new File(Environment.getExternalStorageDirectory()
                        + "/xiaoma.jpg");
                startPhotoZoom(Uri.fromFile(temp));
                break;
            // 取得裁剪后的图片
            case 3:
                if(data != null){
                    setPicToView(data);
                }
                break;
            default:
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪图片方法实现
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        //下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 600);
        intent.putExtra("outputY", 600);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }

    /**
     * 保存裁剪之后的图片数据
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(photo);
            mCropImage.setImageDrawable(drawable);
        }
    }

}
