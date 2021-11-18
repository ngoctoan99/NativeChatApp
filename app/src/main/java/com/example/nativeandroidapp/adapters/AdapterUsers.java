package com.example.nativeandroidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nativeandroidapp.ChatActivity;
import com.example.nativeandroidapp.models.ModelUsers;
import com.example.nativeandroidapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{
    Context context;
    List<ModelUsers> usersList;

    public AdapterUsers(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_users,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String hisUID = usersList.get(position).getUid();
        String usersImage = usersList.get(position).getImage();
        String usersName = usersList.get(position).getName();
        String usersEmail = usersList.get(position).getEmail();
        holder.nNameTv.setText(usersName);
        holder.mEmailTv.setText(usersEmail);
        try{
            Picasso.get().load(usersImage)
                    .placeholder(R.drawable.ic_face_custom)
                    .into(holder.avatarIv);

        }catch (Exception e){

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   Intent intent = new Intent(context, ChatActivity.class);
                   intent.putExtra("hisUid", hisUID);
                   context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView avatarIv;
        TextView nNameTv , mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarS);
            nNameTv = itemView.findViewById(R.id.nametv);
            mEmailTv = itemView.findViewById(R.id.emailtv);

        }
    }
}
