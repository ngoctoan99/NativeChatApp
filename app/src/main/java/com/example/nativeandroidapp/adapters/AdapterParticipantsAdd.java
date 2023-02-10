package com.example.nativeandroidapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterParticipantsAdd extends RecyclerView.Adapter<AdapterParticipantsAdd.MyHolder>{
    private Context context ;
    private ArrayList<ModelUsers> users ;
    String groupId , myGroupRole ;

    public AdapterParticipantsAdd(Context context, ArrayList<ModelUsers> users, String groupId, String myGroupRole) {
        this.context = context;
        this.users = users;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_group_participant ,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelUsers model = users.get(position);
        String name = model.getName();
        String email = model.getEmail();
        String image = model.getImage();
        String uid = model.getUid();

        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_face_default).into(holder.avatarIv);
        }catch (Exception e) {
            holder.avatarIv.setImageResource(R.drawable.ic_face_default);
        }

        checkIfAlreadyExists(model,holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Group");
                ref1.child(groupId).child("Participants").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String hispreiousRole = "" + snapshot.child("role").getValue();
                            String [] option;
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Choose Option");
                            if(myGroupRole.equals("creator")){
                                if(hispreiousRole.equals("admin")){
                                    option = new String []{"Remove Admin", "Remove User"};
                                    builder.setItems(option, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(i == 0){
                                                 removeAdmin(model);
                                            }else {
                                                removeParticipant(model);
                                            }
                                        }
                                    }).show();
                                }
                                else if(hispreiousRole.equals("participant")){
                                    option = new String []{"Make Admin", "Remove User"};
                                    builder.setItems(option, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(i == 0){
                                                makeAdmin(model);
                                            }else {
                                                removeParticipant(model);
                                            }
                                        }
                                    }).show();
                                }
                            }
                            else if(myGroupRole.equals("admin")){
                                if(hispreiousRole.equals("creator")){
                                    Toast.makeText(context, "Creator of Group", Toast.LENGTH_SHORT).show();
                                }else if(hispreiousRole.equals("admin")){
                                    option = new String []{"Make Admin", "Remove User"};
                                    builder.setItems(option, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(i == 0){
                                                makeAdmin(model);
                                            }else {
                                                removeParticipant(model);
                                            }
                                        }
                                    }).show();
                                }
                                else if(hispreiousRole.equals("participant")){
                                    option = new String []{"Make Admin", "Remove User"};
                                    builder.setItems(option, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(i == 0){
                                                makeAdmin(model);
                                            }else {
                                                removeParticipant(model);
                                            }
                                        }
                                    }).show();
                                }
                            }
                        }
                        else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Add Participants").setMessage("Add this user in this group")
                                    .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            addParticipants(model);
                                        }
                                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void makeAdmin(ModelUsers model) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, Object > hashMap = new HashMap<>();
        hashMap.put("role","admin");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).child("Participants").child(model.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "The user is now admin...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void removeParticipant(ModelUsers model) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).child("Participants").child(model.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addParticipants(ModelUsers model) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String > hashMap = new HashMap<>();
        hashMap.put("uid",model.getUid());
        hashMap.put("role","participant");
        hashMap.put("timeStamp",timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).child("Participants").child(model.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Added successfully...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void removeAdmin(ModelUsers model) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, Object > hashMap = new HashMap<>();
        hashMap.put("role","participant");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).child("Participants").child(model.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "The user is participant...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAlreadyExists(ModelUsers model, MyHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(groupId).child("Participants").child(model.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String hisRole  = "" +snapshot.child("role").getValue();
                    holder.statusTv.setText(hisRole);
                }else {
                    holder.statusTv.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView avatarIv ;
        TextView nameTv , emailTv , statusTv;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
