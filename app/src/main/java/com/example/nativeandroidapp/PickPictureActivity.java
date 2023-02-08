package com.example.nativeandroidapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Objects;

public class PickPictureActivity extends AppCompatActivity {
    String image ;
    ImageView image_Pick , goback ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_picture);
        Objects.requireNonNull(getSupportActionBar()).hide();
        Intent intent = getIntent();
        image  = ""+intent.getStringExtra("pImage");
        initView();
    }
    private void initView() {
        image_Pick = findViewById(R.id.image_Pick);
        goback = findViewById(R.id.goback);
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_face_default).into(image_Pick);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_face_default).into(image_Pick);
        }
    }
}