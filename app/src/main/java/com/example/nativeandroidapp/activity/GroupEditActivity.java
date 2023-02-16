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
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.notification.Data;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class GroupEditActivity extends AppCompatActivity {
    String groupId ;
    private ActionBar actionBar ;
    private ImageView groupIconIv ;
    private EditText groupTitle , groupDescription ;
    private FloatingActionButton editGroupBtn ;
    private static final int CAMERA_REQUEST_CODE = 100 ;
    private static final int STORAGE_REQUEST_CODE = 200 ;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    private FirebaseAuth firebaseAuth ;
    private Uri image_uri = null ;
    private ProgressDialog progressDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);
        groupId = getIntent().getStringExtra("groupId");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait ...");
        progressDialog.setCanceledOnTouchOutside(false);
        actionBar = getSupportActionBar() ;
        assert actionBar != null;
        actionBar.setTitle("Edit Group");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CE0288D1")));
        firebaseAuth  =FirebaseAuth.getInstance();
        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE};
        checkUser();
        initView();
        loadGroupInfo();
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.orderByChild("groupID").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String groupId = "" + ds.child("groupID").getValue();
                    String groupDescriptions = "" + ds.child("groupDescription").getValue();
                    String groupIcon = "" + ds.child("groupIcon").getValue();
                    String groupTitles = "" + ds.child("groupTitle").getValue();
                    String timeStamp = "" + ds.child("timeStamp").getValue();
                    String createBy = "" + ds.child("createBy").getValue();

                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timeStamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm:aa",cal).toString();
                    groupTitle.setText(groupTitles);
                    groupDescription.setText(groupDescriptions);
                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_baseline_groups_24).into(groupIconIv);
                    }catch (Exception e){
                        groupIconIv.setImageResource(R.drawable.ic_baseline_groups_24);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void initView() {
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitle = findViewById(R.id.groupTitle);
        groupDescription = findViewById(R.id.groupDescription);
        editGroupBtn = findViewById(R.id.editGroupBtn);

        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
        editGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreatingGroup();
            }
        });
    }

    private void startCreatingGroup() {
        String groupTitles = groupTitle.getText().toString().trim();
        String groupDescriptions = groupDescription.getText().toString().trim();
        if (TextUtils.isEmpty(groupTitles)) {
            Toast.makeText(this, "Group Title is required...", Toast.LENGTH_SHORT).show();
            return ;

        }
        progressDialog.setMessage("Updating Group Info...");
        progressDialog.show();
        if(image_uri == null) {
            HashMap<String , Object> hashMap = new HashMap<>( );
            hashMap.put("groupTitle",groupTitles);
            hashMap.put("groupDescription",groupDescriptions);


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
            ref.child(groupId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this, "Group Info updated ...", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            String time = ""  + System.currentTimeMillis();
            String filePathAndName = "Group_Imgs/" + "image"+"_"+time;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl() ;
                    while (!task.isSuccessful());
                    Uri downloadUri  = task.getResult() ;
                    if (task.isSuccessful()){
                        HashMap<String , Object> hashMap = new HashMap<>( );
                        hashMap.put("groupTitle",groupTitles);
                        hashMap.put("groupDescription",groupDescriptions);
                        hashMap.put("groupIcon",downloadUri) ;

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
                        ref.child(groupId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(GroupEditActivity.this, "Group Info updated ...", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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