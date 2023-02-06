package com.example.nativeandroidapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {


    String myUid , myEmail , myName , myDp , postId , pLikes, hisDp , hisName;

    ImageView uPicture , pImageIv ;
    TextView  nameTv, pTimeTv, pTitleTv , pDescriptionTv, pLikesTv , pCommentsTv;
    ImageButton moreBtn ;
    Button likeBtn , shareBtn ;
    LinearLayout profileLayout ;

    EditText commentEt ;
    ImageButton sendBtn ;
    ImageView cAvatarIv ;

    ProgressDialog pd ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        initView();

        actionBar.setSubtitle("SignedIn as : " + myEmail);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment();
            }
        });
    }

    private void postComment() {
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

        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "Comment Added ...", Toast.LENGTH_SHORT).show();
                commentEt.setText("");
                updateCommentCount();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    boolean mProcessComment = false ;
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

    private void initView() {
        uPicture = findViewById(R.id.uPicture);
        pImageIv = findViewById(R.id.pImages);
        nameTv = findViewById(R.id.nameTv);
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
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);
        
        loadPostInfo();
        checkUserStatus();
        loadUserInfo();

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
                        Picasso.get().load(R.drawable.ic_face_default).into(cAvatarIv);
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
                    String pImage  = ""+ ds.child("pImage").getValue() ;
                    hisDp = "" + ds.child("uDp").getValue();
                    String uid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisName = "" + ds.child("uName").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTimeStamps =  DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();

                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes+"Likes");
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