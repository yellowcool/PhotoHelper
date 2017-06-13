package com.example.administrator.takephototest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.FileNotFoundException;

public class MainActivity extends PhotoActivity {

    private ImageView iv1;
    private ImageView iv2;

    @Override
    protected void loadData() {

    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.photo_activity);
        iv1 = (ImageView) findViewById(R.id.iv1);
        iv2 = (ImageView) findViewById(R.id.iv2);
    }

    @Override
    public void setImageFromPhotos(String imagePath) throws FileNotFoundException {
        Glide.with(this).load(imagePath).into(iv1);
    }

    @Override
    public void setImageForCamera(Uri uri) {
        Glide.with(this).load(uri).into(iv2);
    }

    public void selectPic(View v) {
        photosHelper.showSelectPictureOrCameraDialog();
    }
}