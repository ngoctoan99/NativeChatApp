package com.example.nativeandroidapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.nativeandroidapp.activity.DashboardActivity;
import com.example.nativeandroidapp.activity.LoginActivity;
import com.example.nativeandroidapp.activity.RegisterActivity;
import com.example.nativeandroidapp.ultil.PreferencesUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    CardView btnregister , btnlogin;
    private FirebaseAuth auth;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        setContentView(R.layout.activity_main);
        initView();
        actionView();
        if(PreferencesUtils.getString(MainActivity.this, "email")!= null && PreferencesUtils.getString(MainActivity.this, "pass") != null ){
            String email = PreferencesUtils.getString(MainActivity.this, "email") ;
            String pass = PreferencesUtils.getString(MainActivity.this, "pass");
            checkLogin(email,pass);
        }
    }
    private void actionView(){
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    private void initView(){
        btnregister = findViewById(R.id.btnregister);
        btnlogin = findViewById(R.id.btnlogin);
        auth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);

    }
    private void checkLogin(String email , String pass){
        pd.setMessage("Check login...");
        pd.show();
        auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    pd.dismiss();
                    startActivity(new Intent(MainActivity.this , DashboardActivity.class));
                    finish();
                }
                else {
                    pd.dismiss();
                    Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Log.e("Toan",e.getMessage());
            }
        });
    }

}