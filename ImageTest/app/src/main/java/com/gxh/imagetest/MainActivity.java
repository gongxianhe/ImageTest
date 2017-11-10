package com.gxh.imagetest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import static com.gxh.imagetest.R.drawable.maskkk;

public class MainActivity extends AppCompatActivity {


    Bitmap dog;
    Bitmap maskSecond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dog = BitmapFactory.decodeResource(this.getResources(), R.drawable.dog);
        final Bitmap mask = BitmapFactory.decodeResource(this.getResources(), maskkk);
        maskSecond = BitmapFactory.decodeResource(this.getResources(), R.drawable.masksecond);
        AlbumImageView img = new AlbumImageView(this, AlbumImageView.ALBUM_IMAGE_FRAME, dog, new Bitmap[]{mask, maskSecond}, 0, 0);
        setContentView(img);
    }

}
