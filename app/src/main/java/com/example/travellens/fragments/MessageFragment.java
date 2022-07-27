package com.example.travellens.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travellens.Message;
import com.example.travellens.MyFirebaseUser;
import com.example.travellens.R;
import com.example.travellens.UserAdapter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.ParseUser;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<MyFirebaseUser> firebaseUsers;
    FirebaseUser user;
    private List<String> userList;
    DatabaseReference reference;

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.rvUsers);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getActivity().getSupportFragmentManager().findFragmentById(R.id.afSearchAPI);
        autocompleteFragment.getView().setEnabled(false);
        autocompleteFragment.getView().setVisibility(View.INVISIBLE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message.getSender().equals(user.getUid())) {
                        userList.add(message.getReceiver());
                    }
                    if (message.getReceiver().equals(user.getUid())) {
                        userList.add(message.getSender());
                    }
                }
                Collections.reverse(userList);
                readMessages();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        firebaseUsers = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), firebaseUsers);
        recyclerView.setAdapter(userAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userAdapter.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MyFirebaseUser myFirebaseUser = dataSnapshot.getValue(MyFirebaseUser.class);
                    if (userList.contains(myFirebaseUser.getId())) {
                        firebaseUsers.add(myFirebaseUser);
                    }
                }

                userAdapter.addAll(firebaseUsers);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}