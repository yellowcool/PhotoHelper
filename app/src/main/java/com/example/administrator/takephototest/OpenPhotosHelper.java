package com.example.administrator.takephototest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Content  选择图片的帮助类
 * Created by coolyellow on 2017/3/26.
 */

public class OpenPhotosHelper {
    private OnSelectPhotosListener selectPhotosListener;
    private Activity activity;
    private AlertDialog pictureDialog;
    /**
     * 从相机获取照片
     */
    private static final int TAKE_CAMERA = 10;
    /**
     * 从相册获取照片
     */
    public static final int CHOOSE_PHOTOS = 12;
    private Uri imageUri;

    public OpenPhotosHelper(OnSelectPhotosListener selectPhotosListener, Activity activity) {
        this.selectPhotosListener = selectPhotosListener;
        this.activity = activity;
    }

    /**
     * 打开选择拍摄或者相册对话框
     */
    public void showSelectPictureOrCameraDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.select_picture_or_camera_dialog, null);
        view.findViewById(R.id.select_dialog_camera_bt).setOnClickListener(new View.OnClickListener() {  //通过调用相机上传图片
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, 2);
                } else {
                    startCamera();
                }
                pictureDialog.dismiss();
            }

        });
        view.findViewById(R.id.select_dialog_pctures_bt).setOnClickListener(new View.OnClickListener() {  //通过相册上传图片
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    Intent intent = new Intent("android.intent.action.GET_CONTENT");
                    intent.setType("image/*");
                    activity.startActivityForResult(intent, CHOOSE_PHOTOS);
                }
                pictureDialog.dismiss();
            }
        });
        builder.setView(view);
        pictureDialog = builder.show();
    }
    /**
     * 开启相机，并保存图片
     */
    public void startCamera() {
        File outputImg = new File(activity.getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(activity, "com.company.zhidun.fileprovider", outputImg);
        } else {
            imageUri = Uri.fromFile(outputImg);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(intent, TAKE_CAMERA);
    }
    /**
     * 获取图片真实路径
     *
     * @param uri
     * @param selection
     * @return
     */
    public String getImagePath(Uri uri, String selection, Context context) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
            ;
        }
        return path;
    }

    //在4.4以上系统处理图片
    @TargetApi(19)
    public void getPicture(Intent data, Context context) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(context, uri)) {
            //如果是document类型的Uri,则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];  //解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, context);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null, context);
            }

        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的Uri,则通过普通方式处理
            imagePath = getImagePath(uri, null, context);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的,直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath);//根据图片路径显示图片
    }

    //在4.4以下系统处理图片
    public void getPictureBefore(Intent data, Context context) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null, context);
        displayImage(imagePath);
    }

    /**
     * 对从相册中选择的图片进行处理
     *
     * @param imagePath
     */
    private void displayImage(String imagePath) {
        try {
            selectPhotosListener.setImageFromPhotos(imagePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对从相机中选择的图片进行处理
     */
    public void setImageFormCamera() {
        selectPhotosListener.setImageForCamera(imageUri);
    }



    /**
     * 处理选择的图片回调接口
     */
    public interface OnSelectPhotosListener {
        /**
         * 设置图片从相册
         *
         * @param imagePath 图片的路径
         * @throws FileNotFoundException
         */
        void setImageFromPhotos(String imagePath) throws FileNotFoundException;

        /**
         * 设置图片从相册中
         *
         * @param uri 图片的URI路径
         */
        void setImageForCamera(Uri uri);
    }

    /**
     * 防止内存泄漏
     */
    public void destroy() {
        if (activity!=null){
            activity = null;
        }
    }
}
