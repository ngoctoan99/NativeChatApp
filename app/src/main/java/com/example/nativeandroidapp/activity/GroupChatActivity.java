package com.example.nativeandroidapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.adapters.AdapterGroupChat;
import com.example.nativeandroidapp.models.ModelGroupChat;
import com.example.nativeandroidapp.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private String groupId, myGroupRole="";
    private Toolbar toolbar;
    private ActionBar actionBar;
    private ImageView groupIconIv , btn_back_chat;
    private TextView groupTitle;
    private ImageButton attachBtn, sendBtn;
    private EditText messageEt;
    private String role;
    private RecyclerView recyclerview_groupchat;

    private ArrayList<ModelGroupChat> chatArrayList;
    private AdapterGroupChat adapterGroupChat;


    private static final  int CAMERA_REQUEST = 100 ;
    private static final  int GALLERY_REQUEST = 200 ;
    private static  final int IMAGE_PICK_CAMERA_REQUEST_CODE = 300;
    private static  final int IMAGE_PICK_GALLERY_REQUEST_CODE = 400;
    String [] cameraPermissions;
    String [] galleryPermission;
    Uri image_uri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        initView();
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadGroupMessage();
        loadMyGroupRole();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(GroupChatActivity.this, "Can't send empty message...", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);
                }
            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    myGroupRole = "" + ds.child("role").getValue();
                    invalidateOptionsMenu();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadGroupMessage() {
        chatArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).child("Message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                    chatArrayList.add(model);
                }
                adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, chatArrayList);
                recyclerview_groupchat.setAdapter(adapterGroupChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void sendMessage(String message) {
        String timestamp = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + firebaseAuth.getUid());
        hashMap.put("message", "" + message);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("type", "text");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).child("Message").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                messageEt.setText("");
                hideKeyboard(GroupChatActivity.this);
                recyclerview_groupchat.scrollToPosition(chatArrayList.size() - 1 );

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.orderByChild("groupID").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String groupTitles = "" + ds.child("groupTitle").getValue();
                    String groupDescriptions = "" + ds.child("groupDescription").getValue();
                    String groupIcons = "" + ds.child("groupIcon").getValue();
                    String timeStamps = "" + ds.child("timeStamp").getValue();
                    String createBys = "" + ds.child("createBy").getValue();
                    groupTitle.setText(groupTitles);
                    try {
                        Picasso.get().load(groupIcons).placeholder(R.drawable.ic_baseline_groups_24).into(groupIconIv);
                    } catch (Exception e) {
                        groupIconIv.setImageResource(R.drawable.ic_baseline_groups_24);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitle = findViewById(R.id.groupTitle);
        attachBtn = findViewById(R.id.attachButton);
        sendBtn = findViewById(R.id.sendbtn);
        messageEt = findViewById(R.id.messageEt);
        recyclerview_groupchat = findViewById(R.id.recyclerview_groupchat);
        btn_back_chat = findViewById(R.id.btn_back_chat) ;
        btn_back_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_participant) {
            Intent intent = new Intent(this, GroupParticipantActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_newpost).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(myGroupRole.equals("creator") || myGroupRole.equals("admin"));
        return super.onCreateOptionsMenu(menu);
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
        boolean  result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
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
        ActivityCompat.requestPermissions(this, galleryPermission,CAMERA_REQUEST);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST:{
                if(grantResults.length>0){
                    boolean cameraAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    boolean storageAccept = grantResults[1] == PackageManager.PERMISSION_GRANTED;
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
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else if(requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("Sending image ...");
        progressDialog.show();
        String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/" + timeStamp;
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[]  data = baos.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (! uriTask.isSuccessful());
                Uri downloads = uriTask.getResult();

                if(uriTask.isSuccessful()){
                    HashMap<String, Object> hashMap  = new HashMap<>();
                    hashMap.put("sender" , "" + firebaseAuth.getUid());
                    hashMap.put("message","" + downloads);
                    hashMap.put("timestamp","" + timeStamp);
                    hashMap.put("type","image");
                    DatabaseReference databaseReference  = FirebaseDatabase.getInstance().getReference("Group");
                    databaseReference.child(groupId).child("Message").child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            messageEt.setText("");
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        boolean shouldAllowBack = false ;
        if (shouldAllowBack) {
            super.onBackPressed();
        } else {
            doSomething();
        }
    }

    private void doSomething() {
        Toast.makeText(this, "You can't back physical button", Toast.LENGTH_SHORT).show();
    }

}