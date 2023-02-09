package com.example.nativeandroidapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.nativeandroidapp.R;

public class GroupChatActivity extends AppCompatActivity {
    String groupId ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        Intent intent = new Intent();
        groupId = intent.getStringExtra("groupId");
    }
}