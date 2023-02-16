package com.example.nativeandroidapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.nativeandroidapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class GroupCreateActivity extends AppCompatActivity {
    private ActionBar actionBar ;
    private FirebaseAuth firebaseAuth ;
    private ImageView groupIconIv ;
    private EditText groupTitle , groupDescription ;
    private FloatingActionButton createGroupBtn ;
    private static final int CAMERA_REQUEST_CODE = 100 ;
    private static final int STORAGE_REQUEST_CODE = 200 ;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri image_uri = null ;
    private ProgressDialog progressDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CE0288D1")));
        actionBar.setTitle("Create Group");
        firebaseAuth  =FirebaseAuth.getInstance();

        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE};
        checkUser();
        initView();
    }

    private void initView() {
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitle = findViewById(R.id.groupTitle);
        groupDescription = findViewById(R.id.groupDescription);
        createGroupBtn = findViewById(R.id.createGroupBtn);

        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreatingGroup();
            }
        });
    }

    private void startCreatingGroup() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Group");

        String groupTitles = groupTitle.getText().toString().trim();
        String groupDescriptions = groupDescription.getText().toString().trim();

        if(TextUtils.isEmpty(groupTitles)){
            Toast.makeText(this, "Please enter group title ...", Toast.LENGTH_SHORT).show();
            return ;
        }

        progressDialog.show();
        String time = ""+System.currentTimeMillis();
        if(image_uri == null){
            createGroup(""+time,""+groupTitles,""+groupDescriptions,"");
        }else {
            String  fileNameAndPath = "Group_Imgs/" + "image"+time ;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    while (!task.isSuccessful());
                        Uri downloadUri = task.getResult();
                        if(task.isSuccessful()){
                            createGroup(""+time,""+groupTitles,""+groupDescriptions,""+downloadUri);
                        }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void createGroup(String g_timeStamp, String groupTitle, String groupDescription,String groupIcon) {
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("groupID",""+g_timeStamp);
        hashMap.put("groupTitle",""+groupTitle);
        hashMap.put("groupDescription",""+groupDescription);
        hashMap.put("groupIcon",""+groupIcon);
        hashMap.put("timeStamp",""+g_timeStamp);
        hashMap.put("createBy",""+firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(g_timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                HashMap<String, String> hashMap1 = new HashMap<>();
                hashMap1.put("uid",firebaseAuth.getUid());
                hashMap1.put("role","creator");
                hashMap1.put("timeStamp",g_timeStamp);

                DatabaseReference refs = FirebaseDatabase.getInstance().getReference("Group");
                refs.child(g_timeStamp).child("Participants").child(Objects.requireNonNull(firebaseAuth.getUid())).setValue(hashMap1).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(GroupCreateActivity.this, "Group created... ", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(GroupCreateActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showImagePickDialog() {
        String[] options = {"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image : ").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    if(!checkCameraPermisson()){
                        requestCameraPermissions();
                    }else {
                        pickFromCamera();
                    }
                }else {
                    if(!checkStoragePermissions()){
                        requestStoragePermissions();
                    }else {
                        pickFromGallery();
                    }
                }
            }
        }).show();
    }
    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera(){
        ContentValues cv = new ContentValues() ;
        cv.put(MediaStore.Images.Media.TITLE,"Group Image Icon Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermissions(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermisson(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1 ;
    }
    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }
    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            actionBar.setSubtitle(user.getEmail());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(this, "Camera & Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }else {
                        Toast.makeText(this, "Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                groupIconIv.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                groupIconIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}