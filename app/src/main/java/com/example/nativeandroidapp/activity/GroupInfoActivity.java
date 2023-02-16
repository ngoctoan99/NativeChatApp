package com.example.nativeandroidapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.adapters.AdapterParticipantsAdd;
import com.example.nativeandroidapp.models.ModelUsers;
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
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TooManyListenersException;

public class GroupInfoActivity extends AppCompatActivity {
    private String groupId ;
    private String myGroupRole = "" ;
    private FirebaseAuth firebaseAuth ;
    ActionBar actionBar;
    private ImageView  groupIconIv ;
    private TextView groupDescriptionTv  ,createByTv , editGroupTv, addParticipantTv,leaveGroupTv, participantsTv ;
    private RecyclerView rv_participants ;

    private ArrayList<ModelUsers> usersArrayList ;
    private AdapterParticipantsAdd adapterParticipantsAdd ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        actionBar =  getSupportActionBar() ;
        assert actionBar != null;
        actionBar.setTitle("Group Information");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CE0288D1")));
        groupId = getIntent().getStringExtra("groupId");
        firebaseAuth = FirebaseAuth.getInstance();
        initView();

        loadGroupInfo();
        loadMyGroupRole();

        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupParticipantActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButtonTitle = "";
                if(myGroupRole.equals("creator")){
                    dialogTitle = "Delete Group";
                    dialogDescription = " Are you sure you want to Delete group permanently ?" ;
                    positiveButtonTitle = " DELETE" ;
                }else {
                    dialogTitle = "Leave Group";
                    dialogDescription = " Are you sure you want to Leave group permanently ?" ;
                    positiveButtonTitle = " LEAVE" ;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle);
                builder.setMessage(dialogDescription);
                builder.setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(myGroupRole.equals("creator")){
                            deleteGroup();
                        }else {
                            leaveGroup();
                        }
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                    }
                }).show();
            }
        });
        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this , GroupEditActivity.class);
                intent.putExtra("groupId" , groupId);
                startActivity(intent);
            }
        });
    }

    private void leaveGroup() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(GroupInfoActivity.this, "Leave group successfully ...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(GroupInfoActivity.this, "Delete group successfully ...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(GroupInfoActivity.this , DashboardActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    myGroupRole = "" + ds.child("role").getValue();
                    actionBar.setSubtitle(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail() + "(" + myGroupRole+")");
                    if(myGroupRole.equals("participant")){
                        editGroupTv.setVisibility(View.GONE);
                        addParticipantTv.setVisibility(View.GONE);
                        leaveGroupTv.setText("Leave Group");
                    }
                    else if (myGroupRole.equals("admin")){
                        editGroupTv.setVisibility(View.GONE);
                        addParticipantTv.setVisibility(View.VISIBLE);
                        leaveGroupTv.setText("Leave Group");
                    }
                    else if(myGroupRole.equals("creator")){
                        editGroupTv.setVisibility(View.VISIBLE);
                        addParticipantTv.setVisibility(View.VISIBLE);
                        leaveGroupTv.setText("Delete Group");
                    }
                }
                loadParticipants();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadParticipants() {
        usersArrayList = new ArrayList<>() ;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    String uid = "" + ds.child("uid").getValue() ;
                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                    ref1.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                ModelUsers users = ds.getValue(ModelUsers.class);
                                usersArrayList.add(users);
                            }
                            adapterParticipantsAdd = new AdapterParticipantsAdd(GroupInfoActivity.this,usersArrayList , groupId , myGroupRole);
                            rv_participants.setAdapter(adapterParticipantsAdd);
                            participantsTv.setText("Participants (" + usersArrayList.size() +")");
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

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.orderByChild("groupID").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String groupId = "" + ds.child("groupID").getValue();
                    String groupDescription = "" + ds.child("groupDescription").getValue();
                    String groupIcon = "" + ds.child("groupIcon").getValue();
                    String groupTitle = "" + ds.child("groupTitle").getValue();
                    String timeStamp = "" + ds.child("timeStamp").getValue();
                    String createBy = "" + ds.child("createBy").getValue();

                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timeStamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm:aa",cal).toString();
                    loadCreatorInfo(dateTime,createBy);
                    actionBar.setTitle(groupTitle);
                    groupDescriptionTv.setText(groupDescription);
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

    private void loadCreatorInfo(String dateTime, String createBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String name  = "" + ds.child("name").getValue();
                    createByTv.setText("Created by " + name +" on " + dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initView() {
        groupIconIv = findViewById(R.id.groupIconIv);
        groupDescriptionTv = findViewById(R.id.groupDescriptionTv);
        createByTv = findViewById(R.id.createByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantsTv = findViewById(R.id.participantsTv);
        rv_participants = findViewById(R.id.rv_participants);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}