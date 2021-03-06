package com.example.nativeandroidapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nativeandroidapp.AddNewPost;
import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.ThereProfileActivity;
import com.example.nativeandroidapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder>{
    Context context;
    List<ModelPost> posts ;
    String myUid;

    public AdapterPost(Context context, List<ModelPost> posts) {
        this.context = context;
        this.posts = posts;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.row_post, parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String uid = posts.get(position).getUid();
        String uEmail = posts.get(position).getuEmail();
        String uName = posts.get(position).getuName();
        String uDp = posts.get(position).getuDp();
        String pId = posts.get(position).getpId();
        String pTitle = posts.get(position).getpTitle();
        String pTime = posts.get(position).getpTime();
        String pDescr= posts.get(position).getpDescription();
        String pImage= posts.get(position).getpImage();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTime));
        String pTimeStamp =  DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();

        holder.uName.setText(uName);
        holder.pTime.setText(pTimeStamp);
        holder.pTitle.setText(pTitle);
        holder.pDescription.setText(pDescr);
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_face_default).into(holder.uPicture);

        }catch (Exception e){
            e.getMessage();
        }
        if(pImage.equals("noImage")){
            holder.pImage.setVisibility(View.GONE);

        }else {
            holder.pImage.setVisibility(View.VISIBLE);
            try{
                Picasso.get().load(pImage).into(holder.pImage);

            }catch (Exception e){
                e.getMessage();
            }
        }
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showMoreOption(holder.moreBtn, uid, myUid, pId,pImage);
            }
        });
        holder.likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Like",Toast.LENGTH_SHORT).show();
            }
        });
        holder.commentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Comment",Toast.LENGTH_SHORT).show();
            }
        });
        holder.sharebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Share",Toast.LENGTH_SHORT).show();
            }
        });
        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });
    }

    private void showMoreOption(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        if(uid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 0 , 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1 , 0, "Edit");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0){
                    beginDelete(pId, pImage);
                }
                else if(id == 1){
                    Intent intent = new Intent(context , AddNewPost.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId",pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        if(pImage.equals("noImage")){
            deleteWithoutImage(pId);
        }else {
            deleteWithImage(pId,pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context,"Deleted Successful",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String pId) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(context,"Deleted Successful",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView uPicture, pImage;
        TextView uName, pTime , pDescription, pLike, pTitle ;
        ImageButton moreBtn;
        Button likebtn , commentbtn, sharebtn;
        LinearLayout profileLayout;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            uPicture = itemView.findViewById(R.id.uPicture);
            pImage = itemView.findViewById(R.id.pImages);
            uName = itemView.findViewById(R.id.uNamepost);
            pTime = itemView.findViewById(R.id.pTimes);
            pTitle = itemView.findViewById(R.id.pTitles);
            pDescription = itemView.findViewById(R.id.pDescriptionpost);
            pLike = itemView.findViewById(R.id.pLikes);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likebtn = itemView.findViewById(R.id.likebtn);
            commentbtn = itemView.findViewById(R.id.commentbtn);
            sharebtn = itemView.findViewById(R.id.sharebtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
        }
    }
}
