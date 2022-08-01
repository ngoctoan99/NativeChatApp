package com.example.nativeandroidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nativeandroidapp.ChatActivity;
import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.models.ModelChatlist;
import com.example.nativeandroidapp.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder>{
    Context context;
    List<ModelUsers> usersList;
    private HashMap<String, String> lastMessMap ;

    public AdapterChatlist(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
        lastMessMap =new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String hisUid = usersList.get(position).getUid();
        String userImage = usersList.get(position).getImage();
        String hisName = usersList.get(position).getName();
        String lastMess = lastMessMap.get(hisUid);

        holder.tvName.setText(hisName);
        if(lastMess == null || lastMess.equals("default")){
            holder.tvLastMess.setVisibility(View.GONE);
        }else {
            holder.tvLastMess.setVisibility(View.VISIBLE);
            holder.tvLastMess.setText(lastMess);
        }
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_face_default).into(holder.profileTv);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_face_default).into(holder.profileTv);
        }
        if(usersList.get(position).getOnlineStatus().equals("online")){
            holder.onlineStatus.setImageResource(R.drawable.circle_online);
        }else{
            holder.onlineStatus.setImageResource(R.drawable.circle_offline);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
            }
        });
    }
    public void setLastMessMap(String userId, String lastMess){
        lastMessMap.put(userId, lastMess);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profileTv , onlineStatus;
        TextView tvName , tvLastMess;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileTv = itemView.findViewById(R.id.profileiv);
            onlineStatus  = itemView.findViewById(R.id.onlineStatus);
            tvName = itemView.findViewById(R.id.nameTv);
            tvLastMess = itemView.findViewById(R.id.lastMess);
        }
    }
}
