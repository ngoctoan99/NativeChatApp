package com.example.nativeandroidapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.adapters.AdapterParticipantsAdd;
import com.example.nativeandroidapp.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class GroupParticipantActivity extends AppCompatActivity {
    private RecyclerView user_rv ;
    private ActionBar actionBar ;
    private String groupId ,myGroupRole;
    private FirebaseAuth firebaseAuth ;
    private ArrayList<ModelUsers> users ;
    private AdapterParticipantsAdd adapterParticipantsAdd ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant);
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Add Participant");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CE0288D1")));
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        user_rv = findViewById(R.id.user_rv);
        loadGroupInfo();
    }

    private void getAllUsers() {
        users = new ArrayList<>() ;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelUsers model = ds.getValue(ModelUsers.class);
                    if(!firebaseAuth.getUid().equals(model.getUid())){
                        users.add(model);
                    }
                }
                adapterParticipantsAdd = new AdapterParticipantsAdd(GroupParticipantActivity.this,users,""+groupId,""+myGroupRole);
                user_rv.setAdapter(adapterParticipantsAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupInfo() {
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Group");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.orderByChild("groupID").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String groupId = ""+ ds.child("groupID").getValue() ;
                    final String groupTitle = "" + ds.child("groupTitle").getValue();
                    String groupDescription = "" +ds.child("groupDescription").getValue();
                    String groupIcon = "" + ds.child("groupIcon").getValue();
                    String createBy = "" + ds.child("createBy").getValue();
                    String timeStamp = "" + ds.child("timeStamp").getValue();
                    actionBar.setTitle("Add Participant");
                    ref1.child(groupId).child("Participants").child(Objects.requireNonNull(firebaseAuth.getUid())).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                myGroupRole = "" + snapshot.child("role").getValue();
                                actionBar.setTitle(groupTitle + "{"+myGroupRole+"}");
                                getAllUsers();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}