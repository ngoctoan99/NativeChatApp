package com.example.nativeandroidapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.adapters.AdapterGroupChat;
import com.example.nativeandroidapp.models.ModelGroupChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

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