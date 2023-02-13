package com.example.nativeandroidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.activity.GroupChatActivity;
import com.example.nativeandroidapp.models.ModelGroupChatsList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.MyHolder>{
    private Context context ;
    private ArrayList<ModelGroupChatsList> groupChatsLists ;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatsList> groupChatsLists) {
        this.context = context;
        this.groupChatsLists = groupChatsLists;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupchats_list,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelGroupChatsList model  = groupChatsLists.get(position);
        String groupId = model.getGroupID();
        String icon = model.getGroupIcon() ;
        String groupTitle = model.getGroupTitle();

        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");
        loadlastMessage(model , holder);
        holder.groupTitleTv.setText(groupTitle);

        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic_baseline_groups_24).into(holder.groupIconIv);
        }catch(Exception e){
            holder.groupIconIv.setImageResource(R.drawable.ic_baseline_groups_24);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId",groupId);
                context.startActivity(intent);
            }
        });
    }

    private void loadlastMessage(ModelGroupChatsList model, MyHolder holder) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Group");
        ref.child(model.getGroupID()).child("Message").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){

                    String message = "" + ds.child("message").getValue();
                    String timestamp = "" + ds.child("timestamp").getValue();
                    String sender = "" + ds.child("sender").getValue();
                    String type = "" +ds.child("type").getValue();
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm:aa",cal).toString();

                    if(type.equals("image")){
                        holder.messageTv.setText("Send a image");
                    }else {
                        holder.messageTv.setText(message);
                    }
                    holder.timeTv.setText(dateTime);

                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                    ref1.orderByChild("uid").equalTo(sender).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds : snapshot.getChildren()){
                                String name = ""+ds.child("name").getValue();
                                holder.nameTv.setText(name+": ");

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
    public int getItemCount() {
        return groupChatsLists.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        private ImageView groupIconIv ;
        private TextView groupTitleTv , nameTv, messageTv,timeTv;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
