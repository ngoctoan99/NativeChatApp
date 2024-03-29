package com.example.nativeandroidapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nativeandroidapp.MainActivity;
import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.adapters.AdapterComments;
import com.example.nativeandroidapp.adapters.AdapterHashTag;
import com.example.nativeandroidapp.adapters.AdapterUsers;
import com.example.nativeandroidapp.models.ModelComment;
import com.example.nativeandroidapp.models.ModelUsers;
import com.example.nativeandroidapp.ultil.PreferencesUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PostDetailActivity extends AppCompatActivity {


    String myUid , myEmail , myName , myDp , postId , pLikes, hisDp , hisName ,pImage;
    boolean mProcessComment = false ;
    boolean mProcessLike = false ;
    ImageView uPicture , pImageIv ;
    TextView  nameTv, pTimeTv, pTitleTv , pDescriptionTv, pLikesTv , pCommentsTv, emptyComment;
    ImageButton moreBtn ;
    Button likeBtn , shareBtn ;
    LinearLayout profileLayout ;
    String hisUid ;
    AutoCompleteTextView commentEt ;
    ImageButton sendBtn ;
    ImageView cAvatarIv ;
    RecyclerView listComment ;
    ProgressDialog pd ;
    List<ModelComment> commentList;
    AdapterComments adapterComments ;
    SpannableString mspanable;
    String hashTag = "" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Intent intent = getIntent();
        postId = ""+intent.getStringExtra("postId");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CE0288D1")));
        initView();

        actionBar.setSubtitle("SignedIn as : " + myEmail);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment(hashTag);
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likePost();
            }
        });
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOption();
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pTitle = pTitleTv.getText().toString().trim();
                String pDescription = pDescriptionTv.getText().toString().trim();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) pImageIv.getDrawable();
                if(bitmapDrawable == null){
                    shareTextOnly(pTitle, pDescription);
                }else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle,pDescription,bitmap);
                }

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
        startActivity(Intent.createChooser(intent,"Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null ;
        try{
            imageFolder.mkdirs();
            File file = new File(imageFolder,"shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this,"com.example.nativeandroidapp.fileprovider",file);



        }catch (Exception e){
            Toast.makeText(this,""+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri ;
    }

    private void shareTextOnly(String pTitle, String pDescr) {
        String shareBody = pTitle + " \n" + pDescr;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent , "Share Via"));
    }

    private void likePost() {

        mProcessLike = true ;
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessLike){
                    if(snapshot.child(postId).hasChild(myUid)){
                        postRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likeRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false ;
                    }
                    else {
                        postRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likeRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLike = false ;
                        addToHisNotification(""+hisUid,""+postId,"Like your post");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment(String hashTags) {
        pd  = new ProgressDialog(this);
        pd.setMessage("Adding comment ...");
        String comment = commentEt.getText().toString().trim();
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Comment is empty ...", Toast.LENGTH_SHORT).show();
            return ;
        }
        String timeStamp  = String.valueOf(System.currentTimeMillis());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comment");
        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("cId",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName", myName);
        hashMap.put("hashtagId",hashTag);

        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "Comment Added ...", Toast.LENGTH_SHORT).show();
                commentEt.setText("");
                updateCommentCount();
                if(!Objects.equals(hashTag, "")){
                    addToHisNotificationHashTag(""+ hashTag, ""+postId,myName+" mentions you in  post of " + nameTv.getText().toString());
                }else {
                    addToHisNotification(""+ hisUid, ""+postId,"Comment on your post");
                }

                hashTag = "";
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCommentCount() {
        mProcessComment = true ;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessComment){
                    String comments = "" + snapshot.child("pComments").getValue() ;
                    int newCommentBal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue(""+newCommentBal);
                    mProcessComment = false ;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showMoreOption() {
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        if(hisUid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 0 , 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1 , 0, "Edit");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0){
                    beginDelete();
                }
                else if(id == 1){
                    Intent intent = new Intent(PostDetailActivity.this , AddNewPost.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId",postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }
    private void beginDelete() {
        if(pImage.equals("noImage")){
            deleteWithoutImage();
        }else {
            deleteWithImage();
        }
    }
    private void deleteWithImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailActivity.this,"Deleted Successful",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostDetailActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this,"Deleted Successful",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initView() {
        uPicture = findViewById(R.id.uPicture);
        pImageIv = findViewById(R.id.pImages);
        nameTv = findViewById(R.id.uNamepost);
        pTimeTv = findViewById(R.id.pTimes);
        pTitleTv = findViewById(R.id.pTitles);
        pDescriptionTv = findViewById(R.id.pDescriptionpost);
        pLikesTv = findViewById(R.id.pLikes);
        pCommentsTv = findViewById(R.id.pComment);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likebtn);
        shareBtn = findViewById(R.id.sharebtn);
        profileLayout = findViewById(R.id.profileLayout);

        commentEt = findViewById(R.id.commentEt);
        getAllUsers();

        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        listComment = findViewById(R.id.listComment);
        emptyComment= findViewById(R.id.emptyComment);
         pImageIv.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(PostDetailActivity.this , PickPictureActivity.class);
                 intent.putExtra("pImage",pImage);
                 startActivity(intent);
             }
         });
        
        loadPostInfo();
        checkUserStatus();
        loadUserInfo();
        setLikes();
        loadComment();
    }
    private void loadComment() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        listComment.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comment");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelComment comment = ds.getValue(ModelComment.class);
                    commentList.add(comment);
                    adapterComments = new AdapterComments(getApplicationContext(),commentList);
                    listComment.setAdapter(adapterComments);
                    checkComment();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void checkComment(){
        if(commentList != null ||commentList.size() > 0){
            emptyComment.setVisibility(View.GONE);
            listComment.setVisibility(View.VISIBLE);
        }
        else {
            emptyComment.setVisibility(View.VISIBLE);
            listComment.setVisibility(View.GONE);
        }
    }
    private void  addToHisNotification(String hisUid, String pId,String notification){
        if(hisUid.equals(myUid)){
            Log.d("toansd" , "test");
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
    private void  addToHisNotificationHashTag(String hisUid, String pId,String notification){
        if(hisUid.equals(myUid)){
            Log.d("toansd" , "test");
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
    private void getAllUsers() {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        List<ModelUsers> list = new ArrayList<>();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelUsers users = ds.getValue(ModelUsers.class);
                    assert users != null;
                    assert fuser != null;
                    if(!users.getUid().equals(fuser.getUid())) {
                        list.add(users);
                    }
                    setDataHashTag(list);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setDataHashTag(List<ModelUsers> usersList) {
        AdapterHashTag adapterHashTag = new AdapterHashTag(PostDetailActivity.this, R.layout.row_hashtag,usersList);
        commentEt.setAdapter(adapterHashTag);
        commentEt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                hashTag = usersList.get(i).getUid() ;
                mspanable = new SpannableString(usersList.get(i).getName());
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {

                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                };
                mspanable.setSpan(clickableSpan, 0, commentEt.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                commentEt.setText(mspanable);
                commentEt.setSelection(commentEt.length());
                commentEt.setMovementMethod(LinkMovementMethod.getInstance());
            }
        });
    }

    private void loadUserInfo() {
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    myName = "" + ds.child("name").getValue();
                    myDp = "" + ds.child("image").getValue();
                    try{
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_face_default).into(cAvatarIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_face_default).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadPostInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescr = "" + ds.child("pDescription").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp  = "" + ds.child("pTime").getValue();
                    pImage  = ""+ ds.child("pImage").getValue() ;
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    String pComment = "" + ds.child("pComments").getValue();
                    hisName = "" + ds.child("uName").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTimeStamps =  DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();

                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes+" Likes");
                    pCommentsTv.setText(pComment+" Comment");
                    pTimeTv.setText(pTimeStamps);
                    nameTv.setText(hisName);

                    if(pImage.equals("noImage")){
                        pImageIv.setVisibility(View.GONE);

                    }else {
                        pImageIv.setVisibility(View.VISIBLE);
                        try{
                            Picasso.get().load(pImage).into(pImageIv);

                        }catch (Exception e){
                            e.getMessage();
                        }
                    }

                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_face_default).into(uPicture);
                    }catch(Exception e) {
                        e.printStackTrace();
                        Picasso.get().load(R.drawable.ic_face_default).into(uPicture);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            myEmail = user.getEmail();
            myUid = user.getUid();

        }else {
            startActivity(new Intent(this , MainActivity.class));
            finish();
        }
    }

    private void setLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postId).hasChild(myUid)){
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    likeBtn.setText("Liked");
                }
                else {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_newpost).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_information_group).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout) {
            PreferencesUtils.deleteAll(getApplicationContext());
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }


}