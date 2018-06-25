package com.everyday.skara.everyday.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.NotePOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class NotesFragment extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    FirebaseDatabase firebaseDatabase;
    DatabaseReference notesDatabaseReference;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    ChildEventListener childEventListener;
    ArrayList<NotePOJO> notePOJOArrayList;

    RecyclerView mNotesRecyclerView;
    NotesAdapter mNotesAdapter;

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes_layout, container, false);
        if (getActivity() != null) {
            if (user != null) {
                init();
            } else {
                toLoginActivity();
            }
        }
        return view;
    }

    void init() {
        //Intent intent = getActivity().getIntent();
        boardPOJO = (BoardPOJO) getArguments().getSerializable("board_pojo");
        userInfoPOJO = (UserInfoPOJO) getArguments().getSerializable("user_profile");
        firebaseDatabase = FirebaseDatabase.getInstance();

        notesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/notes/");

        mNotesRecyclerView = view.findViewById(R.id.notes_view_recycler);

        notePOJOArrayList = new ArrayList<>();
        initNotesRecyclerView();
    }
    void initNotesRecyclerView(){
        mNotesRecyclerView.invalidate();
        mNotesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mNotesRecyclerView.setLayoutManager(linearLayoutManager);
        mNotesAdapter = new NotesAdapter();
        mNotesRecyclerView.setAdapter(mNotesAdapter);

        initNotes();

    }
    void initNotes(){
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                NotePOJO notePOJO = dataSnapshot.getValue(NotePOJO.class);
                notePOJOArrayList.add(notePOJO);
                mNotesAdapter.notifyItemInserted(notePOJOArrayList.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        notesDatabaseReference.addChildEventListener(childEventListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (childEventListener != null) {
            notesDatabaseReference.removeEventListener(childEventListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (childEventListener != null) {
            notesDatabaseReference.removeEventListener(childEventListener);
        }
    }
    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;

        public NotesAdapter() {
            try {
                this.inflator = LayoutInflater.from(getActivity());
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_notes_view_row_layout, parent, false);
            return new NotesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                NotePOJO notePOJO = notePOJOArrayList.get(position);
            ((NotesViewHolder) holder).date.setText(notePOJO.getDate());
            ((NotesViewHolder) holder).title.setText(notePOJO.getTitle());
            ((NotesViewHolder) holder).content.setText(notePOJO.getContent());


        }

        @Override
        public int getItemCount() {
            return notePOJOArrayList.size();
        }


        public class NotesViewHolder extends RecyclerView.ViewHolder {
            public TextView date, title, content;

            public NotesViewHolder(View itemView) {
                super(itemView);

                date = itemView.findViewById(R.id.notes_view_date);
                title = itemView.findViewById(R.id.notes_view_title);
                content = itemView.findViewById(R.id.notes_view_content);

            }
        }

    }
}
