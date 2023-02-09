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

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.activity.GroupChatActivity;
import com.example.nativeandroidapp.models.ModelGroupChatsList;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
