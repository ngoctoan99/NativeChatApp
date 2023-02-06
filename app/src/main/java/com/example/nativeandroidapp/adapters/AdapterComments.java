package com.example.nativeandroidapp.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.models.ModelComment;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder>{
    Context context ;
    List<ModelComment> commentList ;

    public AdapterComments(Context context, List<ModelComment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comment,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String uid = commentList.get(position).getUid();
        String uEmail = commentList.get(position).getuEmail();
        String comment = commentList.get(position).getComment();
        String timeStamp = commentList.get(position).getTimestamp();
        String cid = commentList.get(position).getcId();
        String uDp = commentList.get(position).getuDp();
        String name = commentList.get(position).getuName();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String pTimeStamps =  DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();


        holder.nameTv.setText(name);
        holder.timeTv.setText(pTimeStamps);
        holder.commentTv.setText(comment);
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_face_default).into(holder.avatarIv);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView avatarIv;
        TextView nameTv, commentTv , timeTv ;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
