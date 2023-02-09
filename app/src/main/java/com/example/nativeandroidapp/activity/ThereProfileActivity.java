package com.example.nativeandroidapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nativeandroidapp.MainActivity;
import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.adapters.AdapterPost;
import com.example.nativeandroidapp.models.ModelPost;
import com.example.nativeandroidapp.ultil.PreferencesUtils;
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
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {
    RecyclerView postRecyclerView;
    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid;
    FirebaseAuth firebaseAuth;
    ImageView avatar,covertv ;
    TextView nametv , phonetv, emailtv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        postRecyclerView = findViewById(R.id.recyclerView_posts);
        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        nametv = findViewById(R.id.nameuser);
        phonetv = findViewById(R.id.phoneuser);
        emailtv = findViewById(R.id.emailuser);
        avatar = findViewById(R.id.avatar);
        covertv = findViewById(R.id.covertv);

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String name  = ""+dataSnapshot.child("name").getValue();
                    String phone  = ""+dataSnapshot.child("phone").getValue();
                    String email  = ""+dataSnapshot.child("email").getValue();
                    String image  = ""+dataSnapshot.child("image").getValue();
                    String cover  = ""+dataSnapshot.child("cover").getValue();

                    nametv.setText(name);
                    phonetv.setText(phone);
                    emailtv.setText(email);
                    try {
                        Picasso.get().load(image).into(avatar);
                    }catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_face).into(avatar);
                    }
                    try {
                        Picasso.get().load(cover).into(covertv);
                    }catch (Exception e) {
                        //Picasso.get().load(R.drawable.ic_face).into(avatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        postList = new ArrayList<>();

        checkUserStatus();
        loadHisPost();
    }

    private void loadHisPost() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        postRecyclerView.setLayoutManager(linearLayoutManager);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelPost myPost = ds.getValue(ModelPost.class);
                    postList.add(myPost);
                    adapterPost = new AdapterPost(ThereProfileActivity.this,postList);
                    postRecyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this,""+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchHisPost(final String searchQuery){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        postRecyclerView.setLayoutManager(linearLayoutManager);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelPost myPost = ds.getValue(ModelPost.class);
                    if(myPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())|| myPost.getpDescription().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(myPost);
                    }
                    adapterPost = new AdapterPost(ThereProfileActivity.this,postList);
                    postRecyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this,""+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_newpost).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query)){
                    searchHisPost(query);
                }else {
                    loadHisPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    searchHisPost(newText);
                }else {
                    loadHisPost();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout) {
            PreferencesUtils.deleteAll(this);
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if(id == R.id.action_add_newpost) {
            startActivity(new Intent(ThereProfileActivity.this, AddNewPost.class));
        }
        return super.onOptionsItemSelected(item);
    }
    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
        }
        else {
            startActivity(new Intent(ThereProfileActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}