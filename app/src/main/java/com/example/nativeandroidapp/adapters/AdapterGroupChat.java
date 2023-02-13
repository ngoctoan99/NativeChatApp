package com.example.nativeandroidapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.activity.PickPictureActivity;
import com.example.nativeandroidapp.models.ModelGroupChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.MyHolder>{

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private ArrayList<ModelGroupChat> chatArrayList ;
    private FirebaseAuth firebaseAuth ;
    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> chatArrayList) {
        this.context = context;
        this.chatArrayList = chatArrayList;
        firebaseAuth = FirebaseAuth.getInstance();

    }

    @NonNull
    @Override
    public AdapterGroupChat.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType  == MSG_TYPE_LEFT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left,parent,false);
            return new MyHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right,parent,false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterGroupChat.MyHolder holder, @SuppressLint("RecyclerView") int position) {
        ModelGroupChat model = chatArrayList.get(position);
        String message = model.getMessage();
        String senderUid = model.getSender();
        String timeStamp = model.getTimestamp();
        String type = model.getType();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm:aa",cal).toString();


        if(type.equals("text")){
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(message);
        }else {
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);
            try{
                Picasso.get().load(message).placeholder(R.drawable.ic_baseline_image_24).into(holder.messageIv);
            }catch(Exception e) {
                holder.messageIv.setImageResource(R.drawable.ic_baseline_image_24);
            }
        }
        holder.timeTv.setText(dateTime);
        setUserName(model, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatArrayList.get(position).getType().equals("image")){
                    Intent intent = new Intent(context , PickPictureActivity.class);
                    intent.putExtra("pImage",message);
                    context.startActivity(intent);
                }
            }
        });
    }

    private void setUserName(ModelGroupChat model, MyHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds : snapshot.getChildren()){
                        String name = "" + ds.child("name").getValue();
                        holder.nameTv.setText(name);
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public int getItemViewType(int position) {
        if(chatArrayList.get(position).getSender().equals(firebaseAuth.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        private TextView nameTv, messageTv, timeTv ;
        private ImageView messageIv ;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageIv = itemView.findViewById(R.id.messageIv);
        }
    }
}
