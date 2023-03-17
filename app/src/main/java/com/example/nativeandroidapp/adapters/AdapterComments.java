package com.example.nativeandroidapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.activity.PostDetailActivity;
import com.example.nativeandroidapp.activity.ThereProfileActivity;
import com.example.nativeandroidapp.models.ModelComment;
import com.example.nativeandroidapp.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {

        String uid = commentList.get(position).getUid();
        String uEmail = commentList.get(position).getuEmail();
        String comment = commentList.get(position).getComment();
        String timeStamp = commentList.get(position).getTimestamp();
        String cid = commentList.get(position).getcId();
        String uDp = commentList.get(position).getuDp();
        String name = commentList.get(position).getuName();
        String[] parts = comment.split(" ");

        /// get id hashTag to get data
        String hashTagId =commentList.get(position).getHashtagId() ;

        /// access data user hashTag
        if(!Objects.equals(hashTagId, "")){
            Query myRef = FirebaseDatabase.getInstance().getReference("Users");
            myRef.orderByChild("uid").equalTo(hashTagId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds : snapshot.getChildren()){
                        String name = "" + ds.child("name").getValue();
                        String uid = "" + ds.child("uid").getValue();

                        /// set up hashTap and action click hashTap
                        SpannableString mspanable = new SpannableString(comment);
                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                Intent intent = new Intent(context, ThereProfileActivity.class);
                                intent.putExtra("uid",uid);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };
                        mspanable.setSpan(clickableSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.commentTv.setText(mspanable);
                        holder.commentTv.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            holder.commentTv.setText(comment);
        }
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String pTimeStamps =  DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();
        holder.nameTv.setText(name);
        holder.timeTv.setText(pTimeStamps);
//        holder.commentTv.setText(comment);
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
