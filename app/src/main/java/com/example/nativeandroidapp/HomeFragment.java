package com.example.nativeandroidapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.nativeandroidapp.adapters.AdapterPost;
import com.example.nativeandroidapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HomeFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPost adapterPost;
    int click = 0 ;
    public HomeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.postRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
         loadPosts();
        return view;
    }

    private void loadPosts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    postList.add(modelPost);
                    adapterPost = new AdapterPost(getActivity(), postList);
                    adapterPost.setClickInterface(new AdapterPost.ClickInterface() {
                        @Override
                        public void onSelected(ModelPost post) {
                            click ++ ;
                            if(click % 2 == 0){
                                post.setEnable(false);
                                int count  = Integer.parseInt(post.getpLikes()) - 1 ;
                                post.setpLikes(count +"");
                                putCountLike(post);
                                adapterPost.notifyDataSetChanged();
                            }else if(click % 2 != 0){
                                post.setEnable(true);
                                int count  = Integer.parseInt(post.getpLikes()) + 1 ;
                                post.setpLikes(count +"");
                                putCountLike(post);
                                adapterPost.notifyDataSetChanged();
                            }
                        }
                    });
                    Log.d("Toan",modelPost.toString());
                    recyclerView.setAdapter(adapterPost);
                    adapterPost.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(),""+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void putCountLike(ModelPost post){
        if(post.getpImage() != null && !post.getpImage().equals("noImage")){
            HashMap<Object,String> hashMap = new HashMap<>();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            assert user != null;
            String uid = user.getUid();
            hashMap.put("uid",post.getUid());
            hashMap.put("uName",post.getuName());
            hashMap.put("uEmail",post.getuEmail());
            hashMap.put("uDp",post.getuDp());
            hashMap.put("pId",post.getpTime());
            hashMap.put("pTitle",post.getpTitle());
            hashMap.put("pDescription", post.getpDescription());
            hashMap.put("pImage",post.getpImage());
            hashMap.put("pTime", post.getpTime());
            hashMap.put("pLikes",post.getpLikes());
            hashMap.put("pComments","0");
            hashMap.put("uIdLikes",uid);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(post.getpTime()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),""+ e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            assert user != null;
            String uid = user.getUid();
            HashMap<Object, String> hashMap =  new HashMap<>();
            hashMap.put("uid",post.getUid());
            hashMap.put("uName",post.getuName());
            hashMap.put("uEmail",post.getuEmail());
            hashMap.put("uDp",post.getuDp());
            hashMap.put("pId",post.getpTime());
            hashMap.put("pTitle",post.getpTitle());
            hashMap.put("pDescription", post.getpDescription());
            hashMap.put("pImage","noImage");
            hashMap.put("pTime", post.getpTime());
            hashMap.put("pLikes",post.getpLikes());
            hashMap.put("pComments","0");
            hashMap.put("uIdLikes",uid);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(post.getpTime()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
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
    private void searchPosts(String searchQuery){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if(modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) || modelPost.getpDescription().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);
                    }
                    adapterPost = new AdapterPost(getActivity(), postList);
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(),""+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
            //tvporfile.setText(user.getEmail());
        }
        else {
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query)){
                    searchPosts(query);
                }else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    searchPosts(newText);
                }else {
                    loadPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout) {
            PreferencesUtils.deleteAll(getContext());
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if(id == R.id.action_add_newpost) {
            startActivity(new Intent(getActivity(), AddNewPost.class));
        }
        return super.onOptionsItemSelected(item);
    }
}