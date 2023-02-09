package com.example.nativeandroidapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.nativeandroidapp.MainActivity;
import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.activity.AddNewPost;
import com.example.nativeandroidapp.activity.GroupCreateActivity;
import com.example.nativeandroidapp.adapters.AdapterGroupChatList;
import com.example.nativeandroidapp.models.ModelGroupChatsList;
import com.example.nativeandroidapp.ultil.PreferencesUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class GroupChatsFragment extends Fragment {
    private RecyclerView recyclerView_groupChat ;
    private FirebaseAuth firebaseAuth ;
    private ArrayList<ModelGroupChatsList> groupChatsLists ;
    private AdapterGroupChatList adapterGroupChatList ;
    public GroupChatsFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_group_chats, container, false);
        recyclerView_groupChat = view.findViewById(R.id.recyclerView_groupChat);
        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupChatsList();
        return view ;
    }

    private void loadGroupChatsList() {
        groupChatsLists = new ArrayList<>() ;

        DatabaseReference ref  =FirebaseDatabase.getInstance().getReference("Group");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatsLists.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    if(ds.child("Participants").child(Objects.requireNonNull(firebaseAuth.getUid())).exists()){
                        ModelGroupChatsList model = ds.getValue(ModelGroupChatsList.class);
                        groupChatsLists.add(model);
                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(),groupChatsLists);
                recyclerView_groupChat.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void searchGroupChatsList(String query) {
        groupChatsLists = new ArrayList<>() ;

        DatabaseReference ref  =FirebaseDatabase.getInstance().getReference("Group");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatsLists.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    if(ds.child("Participants").child(Objects.requireNonNull(firebaseAuth.getUid())).exists()){
                        if(ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
                            ModelGroupChatsList model = ds.getValue(ModelGroupChatsList.class);
                            groupChatsLists.add(model);
                        }

                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(),groupChatsLists);
                recyclerView_groupChat.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_newpost).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query.trim())){
                    searchGroupChatsList(query);
                }
                else {
                    loadGroupChatsList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText.trim())){
                    searchGroupChatsList(newText);
                }
                else {
                    loadGroupChatsList();
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
        else if(id == R.id.action_add_newpost) {
            startActivity(new Intent(getActivity(), AddNewPost.class));
        }
        else if(id == R.id.action_create_group) {
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null ){
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}