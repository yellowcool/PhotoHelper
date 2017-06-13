package com.example.administrator.takephototest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * content 调用相机或者相册的activity
 * Created by coolyellow on 2017/6/12.
 */

public abstract class PhotoActivity extends AppCompatActivity implements OpenPhotosHelper.OnSelectPhotosListener {
    /**
     * 从相机获取照片
     */
    private static final int TAKE_CAMERA = 10;
    /**
     * 从相册获取照片
     */
    public static final int CHOOSE_PHOTOS = 12;

    protected OpenPhotosHelper photosHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photosHelper = new OpenPhotosHelper(this,this,null);
        initView(savedInstanceState);
        loadData();
    }

    /**
     * 数据处理
     */
    protected abstract void loadData();

    /**
     * view的相关初始化
     * @param savedInstanceState
     */
    protected abstract void initView(Bundle savedInstanceState);


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1://请求打开SD、相册的权限
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent("android.intent.action.GET_CONTENT");
                    intent.setType("image/*");
                    startActivityForResult(intent, CHOOSE_PHOTOS);
                } else {
                    Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2://请求打开相机的权限
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photosHelper.startCamera();
                } else {
                    Toast.makeText(this, "没有相机权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTOS://从相册中选择照片
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        photosHelper.getPicture(data, this);
                    } else {
                        photosHelper.getPictureBefore(data, this);
                    }
                }
                break;
            case TAKE_CAMERA://调用摄像头拍摄的照片
                try {
                    photosHelper.setImageFormCamera();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (photosHelper!=null){
            photosHelper.destroy();
        }
    }
}
