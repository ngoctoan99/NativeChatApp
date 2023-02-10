package com.example.nativeandroidapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nativeandroidapp.MainActivity;
import com.example.nativeandroidapp.ultil.PreferencesUtils;
import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.adapters.AdapterChat;
import com.example.nativeandroidapp.models.ModelChat;
import com.example.nativeandroidapp.models.ModelUsers;
import com.example.nativeandroidapp.notification.APIService;
import com.example.nativeandroidapp.notification.Client;
import com.example.nativeandroidapp.notification.Data;
import com.example.nativeandroidapp.notification.Response;
import com.example.nativeandroidapp.notification.Sender;
import com.example.nativeandroidapp.notification.Token;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;


public class ChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profiletv;
    TextView nametv, statustv;
    EditText messageedit;
    ImageButton sendbtn;
    ImageButton attachButton ;
    ImageView btn_back_chat ;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userdbR;
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    List<ModelChat> chatList;
    AdapterChat adapterChat;
    String hisUid;
    String  myUid;
    String hisimage;
    APIService apiService;
    boolean notify = false;
    private static final  int CAMERA_REQUEST = 100 ;
    private static final  int GALLERY_REQUEST = 200 ;
    private static  final int IMAGE_PICK_CAMERA_REQUEST_CODE = 300;
    private static  final int IMAGE_PICK_GALLERY_REQUEST_CODE = 400;
    String [] cameraPermissions;
    String [] galleryPermission;
    Uri image_uri = null;
    boolean shouldAllowBack = false ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar1 = findViewById(R.id.toolbarsds);
        setSupportActionBar(toolbar1);
        toolbar1.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerview);
        profiletv = findViewById(R.id.profiless);
        nametv = findViewById(R.id.yournametv);
        statustv = findViewById(R.id.userStatusTV);
        messageedit = findViewById(R.id.chatmessage);
        sendbtn = findViewById(R.id.sendbtn);
        attachButton = findViewById(R.id.attachButton);

        btn_back_chat = findViewById(R.id.btn_back_chat);

        galleryPermission  = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        cameraPermissions  = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        btn_back_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userdbR = firebaseDatabase.getReference("Users");
        Query userquery = userdbR.orderByChild("uid").equalTo(hisUid);
        userquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()) {
                    String name = ""+ ds.child("name").getValue();
                    hisimage = ""+ ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typingTo").getValue();
                    if(typingStatus.equals(myUid)){
                        statustv.setText("Typing...");
                    }else {

                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        if(onlineStatus.equals("online")){
                            statustv.setText(onlineStatus);
                        }else {
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm:aa",cal).toString();
                            statustv.setText("Last seen at: "+dateTime);
                        }
                    }
                    nametv.setText(name);
                    try {
                        Picasso.get().load(hisimage).placeholder(R.drawable.ic_face_default).into(profiletv);
                    }catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_face_default).into(profiletv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String messages = messageedit.getText().toString().trim();
                if(TextUtils.isEmpty(messages)){
                    Toast.makeText(ChatActivity.this,"Can't send the empty message..",Toast.LENGTH_SHORT).show();
                }
                else {
                    sendMessage(messages);
                }
                messageedit.setText("");
            }
        });
        messageedit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0 ){
                    checkTypingStatus("noOne");
                }else {
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
        readMessage();
        seenMessage();
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    assert chat != null;
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String ,Object> hasSeen = new HashMap<>();
                        hasSeen.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeen);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage() {
        chatList = new ArrayList<>();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    assert modelChat != null;
                    if(modelChat.getReceiver().equals(myUid) && modelChat.getSender().equals(hisUid) ||
                    modelChat.getReceiver().equals(hisUid) && modelChat.getSender().equals(myUid)){
                        chatList.add(modelChat);
                    }
                    adapterChat = new AdapterChat(ChatActivity.this,chatList,hisimage);
//                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);
                }
                recyclerView.scrollToPosition(chatList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String messages) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",messages);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);
        hashMap.put("type","text");
        databaseReference.child("Chats").push().setValue(hashMap);
        String msg = messages;
        final DatabaseReference database =  FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers users = snapshot.getValue(ModelUsers.class);
                if(notify){
                    assert users != null;
                    sendNotification(hisUid,users.getName(), messages);
                }
                notify = false;
                hideKeyboard(ChatActivity.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUid);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUid).child(myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        recyclerView.scrollToPosition(chatList.size() - 1);
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void sendImageMessage(Uri image_uri) throws IOException {

        notify =true ;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image ...");
        progressDialog.show();
        String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/" + "post_"+ timeStamp;
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[]  data = baos.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (! uriTask.isSuccessful());
                String download = uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    DatabaseReference databaseReference  = FirebaseDatabase.getInstance().getReference();
                    HashMap<String, Object> hashMap  = new HashMap<>();
                    hashMap.put("sender" , myUid);
                    hashMap.put("receiver",hisUid);
                    hashMap.put("message",download);
                    hashMap.put("timestamp",timeStamp);
                    hashMap.put("type","image");
                    hashMap.put("isSeen",false);
                    databaseReference.child("Chats").push().setValue(hashMap);

                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelUsers users = snapshot.getValue(ModelUsers.class);
                            if(notify){
                                assert users != null;
                                sendNotification(hisUid,users.getName(),"Sent you a photo ...");
                            }
                            notify = false ;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUid);
                    chatRef1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                chatRef1.child("id").setValue(hisUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUid).child(myUid);
                    chatRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                chatRef2.child("id").setValue(myUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

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

    private void sendNotification(String hisUid, String name, String messages) {
        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myUid,name+":"+messages,"New Message", hisUid, R.drawable.ic_face_default);
                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkOnlineStatus(String status){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap =  new HashMap<>();
        hashMap.put("onlineStatus", status);
        databaseReference.updateChildren(hashMap);
    }
    private void checkTypingStatus(String status){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap =  new HashMap<>();
        hashMap.put("typingTo", status);
        databaseReference.updateChildren(hashMap);
    }
    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
            //tvporfile.setText(user.getEmail());
            myUid = user.getUid();
        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
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
        ActivityCompat.requestPermissions(this, galleryPermission,CAMERA_REQUEST);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_newpost).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
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
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onBackPressed() {
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