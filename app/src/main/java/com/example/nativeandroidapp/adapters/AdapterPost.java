package com.example.nativeandroidapp.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nativeandroidapp.activity.AddNewPost;
import com.example.nativeandroidapp.activity.PickPictureActivity;
import com.example.nativeandroidapp.activity.PostDetailActivity;
import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.activity.ThereProfileActivity;
import com.example.nativeandroidapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder>{
    Context context;
    List<ModelPost> posts ;
    String myUid;
    DatabaseReference likesRef  ;
    DatabaseReference postsRef ;
    boolean mProcessLike = false ;
    boolean isImageFitToScreen ;
//    private ClickInterface clickInterface;
    private int count = 0 ;
    public AdapterPost(Context context, List<ModelPost> posts) {
        this.context = context;
        this.posts = posts;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.row_post, parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        String uid = posts.get(position).getUid();
        String uEmail = posts.get(position).getuEmail();
        String uName = posts.get(position).getuName();
        String uDp = posts.get(position).getuDp();
        String pId = posts.get(position).getpId();
        String pTitle = posts.get(position).getpTitle();
        String pTime = posts.get(position).getpTime();
        String pDescr= posts.get(position).getpDescription();
        String pImage= posts.get(position).getpImage();
        String pLikes = posts.get(position).getpLikes();
        String pComments = posts.get(position).getpComments();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTime));
        String pTimeStamp =  DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();

        holder.uName.setText(uName);
        holder.pTime.setText(pTimeStamp);
        holder.pTitle.setText(pTitle);
        holder.pDescription.setText(pDescr);
        holder.pLikes.setText(pLikes + " Likes");
        holder.pLikes.setTextColor(Color.parseColor("#CE0288D1"));

        holder.pComments.setText(pComments + " Comments");
        holder.pComments.setTextColor(Color.parseColor("#CE0288D1"));

        setLikes(holder , pId);
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
        holder.pImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context , PickPictureActivity.class);
                intent.putExtra("pImage",pImage);
                context.startActivity(intent);
            }
        });

        holder.commentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context , PostDetailActivity.class);
                intent.putExtra("postId",pId);
                context.startActivity(intent);
            }
        });
        holder.sharebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.pImage.getDrawable();
                if(bitmapDrawable == null){
                    shareTextOnly(pTitle, pDescr);
                }else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle,pDescr,bitmap);
                }
            }
        });
        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context.getClass().toString().equals("class " +ThereProfileActivity.class.getName())){

                }
                else {
                    Intent intent = new Intent(context, ThereProfileActivity.class);
                    intent.putExtra("uid",uid);
                    context.startActivity(intent);
                }
            }
        });

        holder.likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pLikes  =Integer.parseInt(posts.get(position).getpLikes());
                mProcessLike = true ;
                String postIde =posts.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProcessLike){
                            if(snapshot.child(postIde).hasChild(myUid)){
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false ;
                            }
                            else {
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked");
                                mProcessLike = false ;
                                addToHisNotification("" + uid, ""+pId,"Like your post");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void shareImageAndText(String pTitle, String pDescr, Bitmap bitmap) {
        String shareBody = pTitle + "\n" + pDescr;
        Uri uri = saveImageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.putExtra(Intent.EXTRA_TEXT,shareBody);
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        intent.setType("image/png");
        context.startActivity(Intent.createChooser(intent,"Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null ;
        try{
            imageFolder.mkdirs();
            File file = new File(imageFolder,"shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context,"com.example.nativeandroidapp.fileprovider",file);



        }catch (Exception e){
            Toast.makeText(context,""+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri ;
    }

    private void shareTextOnly(String pTitle, String pDescr) {
        String shareBody = pTitle + " \n" + pDescr;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(intent , "Share Via"));
    }

    private void  addToHisNotification(String hisUid, String pId,String notification){
        if(hisUid.equals(myUid)){

        }else {
            String timeStamp = "" + System.currentTimeMillis();
            HashMap<Object, String> hashMap = new HashMap<>() ;
            hashMap.put("pId",pId);
            hashMap.put("timeStamp",timeStamp);
            hashMap.put("pUid",hisUid);
            hashMap.put("notification",notification);
            hashMap.put("sUid",myUid);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(hisUid).child("Notification").child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    private void setLikes(MyHolder holder, final String pId) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(pId).hasChild(myUid)){
                    holder.likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    holder.likebtn.setText("Liked");
                }
                else {
                    holder.likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    holder.likebtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showMoreOption(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        if(uid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 0 , 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1 , 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE, 2 , 0, "View Detail");
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
                } else if (id == 2){
                    Intent intent = new Intent(context , PostDetailActivity.class);
                    intent.putExtra("postId",pId);
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
        TextView uName, pTime , pDescription, pLikes, pTitle ,pComments;
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
            pLikes = itemView.findViewById(R.id.pLikes);
            pComments = itemView.findViewById(R.id.pComment);
            moreBtn = itemView.findViewById(R.id.moreBtns);
            likebtn = itemView.findViewById(R.id.likebtn);
            commentbtn = itemView.findViewById(R.id.commentbtn);
            sharebtn = itemView.findViewById(R.id.sharebtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
        }
    }

//    public void setClickInterface(ClickInterface clickInterface) {
//        this.clickInterface = clickInterface;
//    }
//
//    public interface ClickInterface {
//        void onSelected(ModelPost post);
//    }
}
