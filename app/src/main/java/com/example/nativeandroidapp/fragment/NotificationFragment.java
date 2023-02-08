package com.example.nativeandroidapp.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.adapters.AdapterNotification;
import com.example.nativeandroidapp.models.ModelNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationFragment extends Fragment {
    RecyclerView recyclerView_notification ;
    private FirebaseAuth firebaseAuth ;
    private ArrayList<ModelNotification> modelNotifications ;
    private AdapterNotification adapterNotification ;
    public NotificationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView_notification = view .findViewById(R.id.recyclerView_notification);
        firebaseAuth = FirebaseAuth.getInstance();
        getAllNotifications();
        return view;
    }

    private void getAllNotifications() {
        modelNotifications = new ArrayList<>() ;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notification").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelNotifications.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelNotification model = ds.getValue(ModelNotification.class);
                    modelNotifications.add(model);
                }
                adapterNotification = new AdapterNotification(getActivity(),modelNotifications);
                recyclerView_notification.setAdapter(adapterNotification);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}