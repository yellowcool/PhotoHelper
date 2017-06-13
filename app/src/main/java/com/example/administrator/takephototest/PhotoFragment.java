package com.example.administrator.takephototest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;

/**
 * content 调用相机或者相册的fragment
 * Created by coolyellow on 2017/6/12.
 */

public abstract class PhotoFragment extends Fragment implements OpenPhotosHelper.OnSelectPhotosListener {
    /**
     * 从相机获取照片
     */
    private static final int TAKE_CAMERA = 10;
    /**
     * 从相册获取照片
     */
    public static final int CHOOSE_PHOTOS = 12;
    protected OpenPhotosHelper photosHelper;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        photosHelper = new OpenPhotosHelper(this,getActivity(),this);
        View view = initView(inflater, container, savedInstanceState);
        loadData();
        return view;
    }

    protected abstract View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * 数据处理
     */
    protected abstract void loadData();


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1://请求打开SD、相册的权限
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent("android.intent.action.GET_CONTENT");
                    intent.setType("image/*");
                    startActivityForResult(intent, CHOOSE_PHOTOS);
                } else {
                    Toast.makeText(getContext(), "没有权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2://请求打开相机的权限
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photosHelper.startCamera();
                } else {
                    Toast.makeText(getContext(), "没有相机权限", Toast.LENGTH_SHORT).show();
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
                        photosHelper.getPicture(data, getActivity());
                    } else {
                        photosHelper.getPictureBefore(data, getActivity());
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

}
