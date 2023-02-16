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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nativeandroidapp.MainActivity;
import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.ultil.PreferencesUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddNewPost extends AppCompatActivity {
    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    EditText title , description ;
    ImageView imagePost;
    TextView textImage ;
    DatabaseReference databaseReference;
    Button btnUpload;
    Uri image_uri = null;
    String name, email, uid, dp;
    ProgressDialog pd;
    private static final  int CAMERA_REQUEST = 100 ;
    private static final  int GALLERY_REQUEST = 200 ;
    private static  final int IMAGE_PICK_CAMERA_REQUEST_CODE = 300;
    private static  final int IMAGE_PICK_GALLERY_REQUEST_CODE = 400;
    String [] cameraPermissions;
    String [] galleryPermission;

    String editTitle , editDescription , editImage ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_post);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CE0288D1")));
        firebaseAuth = FirebaseAuth.getInstance();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        checkUserStatus();
        title = findViewById(R.id.pTitle);
        description = findViewById(R.id.pDescription);
        imagePost = findViewById(R.id.pImage);
        imagePost.setImageDrawable(null);
        btnUpload = findViewById(R.id.btnUpload);
        textImage =  findViewById(R.id.textImage) ;
        checkImage() ;
        Intent intent = getIntent();
        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");
        if(isUpdateKey.equals("editPost")){
            actionBar.setTitle("Update Post");
            btnUpload.setText("Update");
            loadPostData(editPostId);
        }else{
            actionBar.setTitle("Add New Post");
            btnUpload.setText("Update");
        }
        pd = new ProgressDialog(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = databaseReference.orderByChild("email").equalTo(email);
        actionBar.setSubtitle(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    name = ""+ ds.child("name").getValue();
                    email = ""+ ds.child("email").getValue();
                    dp = ""+ ds.child("image").getValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        galleryPermission  = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        cameraPermissions  = new String[]{Manifest.permission.CAMERA,Manifest.permission.CAMERA};
        imagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titles = title.getText().toString().trim();
                String descriptions = description.getText().toString().trim();
                if(TextUtils.isEmpty(titles)){
                    Toast.makeText(AddNewPost.this,"Enter title...",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(descriptions)){
                    Toast.makeText(AddNewPost.this,"Enter description...",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isUpdateKey.equals("editPost")){
                    beginUpdate(titles , descriptions , editPostId);
                }
                else {
                    upLoadData(titles,descriptions);
                }
            }
        });
    }

    private void checkImage() {
        if(imagePost.getDrawable() != null){
            textImage.setText("");
        }else {
            textImage.setText("Click here to add image");
        }
    }

    private void beginUpdate(String titles, String descriptions, String editPostId) {
        pd.setMessage("Updating Post...");
        pd.show();
        if(!editImage.equals("noImage")){
            updateWasWithImage(titles, descriptions, editPostId);
        }else if(imagePost.getDrawable() != null ){
            updateWithNowImage(titles, descriptions, editPostId);
        }
        else {
            updateWithoutImage(titles, descriptions, editPostId);
        }
    }

    private void updateWithoutImage(String titles, String descriptions, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pTitle", titles);
        hashMap.put("pDescription", descriptions);
        hashMap.put("pImage", "noImage");
        hashMap.put("pLikes","0");
        hashMap.put("pComments","0");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(AddNewPost.this,"Update...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddNewPost.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateWithNowImage(final String titles, final String descriptions, final String editPostId) {
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePath = "Posts/"+"post_"+timeStamp;
        Bitmap bitmap = ((BitmapDrawable)imagePost.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100 , byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePath);
        reference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());

                String downloadUri = uriTask.getResult().toString();
                if(uriTask.isSuccessful()){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", uid);
                        hashMap.put("uName", name);
                        hashMap.put("uEmail", email);
                        hashMap.put("uDp", dp);
                        hashMap.put("pTitle", titles);
                        hashMap.put("pDescription", descriptions);
                        hashMap.put("pImage", downloadUri);
                        hashMap.put("pLikes","0");
                        hashMap.put("pComments","0");
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pd.dismiss();
                                Toast.makeText(AddNewPost.this,"Update...", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(AddNewPost.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddNewPost.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWasWithImage(final String titles, final String descriptions,final String editPostId) {
        StorageReference mPisture = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPisture.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                final String timeStamp = String.valueOf(System.currentTimeMillis());
                String filePath = "Posts/"+"post_"+ timeStamp;
                Bitmap bitmap = ((BitmapDrawable)imagePost.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100 , byteArrayOutputStream);
                byte[] data = byteArrayOutputStream.toByteArray();
                StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePath);
                reference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uDp", dp);
                                hashMap.put("pTitle", titles);
                                hashMap.put("pDescription", descriptions);
                                hashMap.put("pImage", downloadUri);
                                hashMap.put("pLikes","0");
                                hashMap.put("pComments","0");
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        pd.dismiss();
                                        Toast.makeText(AddNewPost.this,"Update...", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(AddNewPost.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddNewPost.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddNewPost.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("pId").equalTo(editPostId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    editTitle = ""+ds.child("pTitle").getValue();
                    editDescription = ""+ds.child("pDescription").getValue();
                    editImage = ""+ds.child("pImage").getValue();

                    title.setText(editTitle);
                    description.setText(editDescription);

                    if(!editImage.equals("noImage")){
                        try {
                            Picasso.get().load(editImage).into(imagePost);
                        }catch (Exception e){

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void upLoadData(final String titles,final  String descriptions) {
        pd.setMessage("Publishing Post...");
        pd.show();
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;
        if(imagePost.getDrawable() != null){
            Bitmap bitmap = ((BitmapDrawable)imagePost.getDrawable()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100 , byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            reference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    String dowloadUri = uriTask.getResult().toString();
                    if(uriTask.isSuccessful()){
                        HashMap<Object, String> hashMap =  new HashMap<>();
                        hashMap.put("uid",uid);
                        hashMap.put("uName",name);
                        hashMap.put("uEmail",email);
                        hashMap.put("uDp",dp);
                        hashMap.put("pId",timeStamp);
                        hashMap.put("pTitle",titles);
                        hashMap.put("pDescription", descriptions);
                        hashMap.put("pImage",dowloadUri);
                        hashMap.put("pTime", timeStamp);
                        hashMap.put("pLikes","0");
                        hashMap.put("pComments","0");

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pd.dismiss();
//                                Toast.makeText(AddNewPost.this,"Post published",Toast.LENGTH_SHORT).show();
//                                title.setText("");
//                                description.setText("");
//                                imagePost.setImageURI(null);
//                                image_uri = null;
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(AddNewPost.this,""+ e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                    Toast.makeText(AddNewPost.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            HashMap<Object, String> hashMap =  new HashMap<>();
            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pId",timeStamp);
            hashMap.put("pTitle",titles);
            hashMap.put("pDescription", descriptions);
            hashMap.put("pImage","noImage");
            hashMap.put("pTime", timeStamp);
            hashMap.put("pLikes","0");
            hashMap.put("pComments","0");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    pd.dismiss();
                    Toast.makeText(AddNewPost.this,"Post published",Toast.LENGTH_SHORT).show();
                    title.setText("");
                    description.setText("");
                    imagePost.setImageURI(null);
                    image_uri = null;
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddNewPost.this,""+ e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showImagePickDialog() {
        String[] options = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which== 0){
                    if(!checkCameraPermissions()){
                        requestCameraPermissions();
                    }else {
                        PickFromCamera();
                    }
                }
                if(which == 1){
                    if(!checkStoragePermissions()){
                        requestStoragePermissions();
                    }else {
                        PickFromStorage();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void PickFromStorage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    private void PickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_REQUEST_CODE);
    }

    private boolean checkStoragePermissions(){
        boolean  result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(this, galleryPermission,GALLERY_REQUEST);
    }
    private boolean checkCameraPermissions(){
        boolean results = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean  result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && results;
    }
    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this, cameraPermissions,CAMERA_REQUEST);
    }
    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_newpost).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_information_group).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout) {
            PreferencesUtils.deleteAll(this);
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if(id == R.id.action_add_newpost) {
            startActivity(new Intent(this, AddNewPost.class));
        }
        return super.onOptionsItemSelected(item);
    }
    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
            //tvporfile.setText(user.getEmail());
            email = user.getEmail();
            uid = user.getUid();
        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST:{
                if(grantResults.length>0){
                    boolean cameraAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccept ){
                        PickFromCamera();
                    }else {

                    }
                }else {

                }
            }break;
            case GALLERY_REQUEST:{
                if(grantResults.length>0){
                    boolean storageAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccept){
                        PickFromStorage();
                    }
                }else {

                }
            }break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE){
                image_uri = data.getData();
                imagePost.setImageURI(image_uri);
                textImage.setText("");

            }else if(requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                imagePost.setImageURI(image_uri);
                textImage.setText("");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}