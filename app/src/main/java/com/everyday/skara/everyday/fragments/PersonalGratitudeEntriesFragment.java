package com.everyday.skara.everyday.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.PersonalGratitudeBoardActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.pojo.GratitudePOJO;
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
import java.util.HashMap;
import java.util.Map;

public class PersonalGratitudeEntriesFragment extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    View view;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mEntriesDatabaseReference;
    ChildEventListener mEntriesChildEventListener;
    UserInfoPOJO userInfoPOJO;
    RecyclerView mEntriesRecyclerView;
    EntriesAdapter mEntriesAdapter;
    ArrayList<GratitudePOJO> gratitudePOJOArrayList;

    BottomSheetDialog mEditEntryDialog;
    int moodChoice = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_gratitude_entries_layout, container, false);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            if (user != null) {
                init();
            } else {
                toLoginActivity();
            }
        }
    }

    void init() {
        //Intent intent = getActivity().getIntent();
        userInfoPOJO = (UserInfoPOJO) getArguments().getSerializable("user_profile");
        mEntriesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_GRATITUDE + "/gratitude/");
        mEntriesDatabaseReference.keepSynced(true);

        mEntriesRecyclerView = view.findViewById(R.id.gratitude_entries_recyclerview);
        gratitudePOJOArrayList = new ArrayList<>();

        initEntriesRecyclerView();
    }

    void initEntriesRecyclerView() {
        mEntriesRecyclerView.invalidate();
        mEntriesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mEntriesRecyclerView.setLayoutManager(linearLayoutManager);
        mEntriesAdapter = new EntriesAdapter();
        mEntriesRecyclerView.setAdapter(mEntriesAdapter);

        initEntries();
    }

    void initEntries() {
        gratitudePOJOArrayList = new ArrayList<>();
        mEntriesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                GratitudePOJO gratitudePOJO = dataSnapshot.getValue(GratitudePOJO.class);
                gratitudePOJOArrayList.add(gratitudePOJO);
                mEntriesAdapter.notifyItemInserted(gratitudePOJOArrayList.size() - 1);
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
        mEntriesDatabaseReference.addChildEventListener(mEntriesChildEventListener);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mEntriesChildEventListener != null) {
            mEntriesDatabaseReference.removeEventListener(mEntriesChildEventListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEntriesChildEventListener != null) {
            mEntriesDatabaseReference.removeEventListener(mEntriesChildEventListener);
        }
    }

    public class EntriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;

        public EntriesAdapter() {
            try {
                this.inflator = LayoutInflater.from(getActivity());
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_gratitude_entries_row_layout, parent, false);
            return new EntriesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            GratitudePOJO gratitudePOJO = gratitudePOJOArrayList.get(position);
            ((EntriesViewHolder) holder).date.setText(gratitudePOJO.getDate());
            ((EntriesViewHolder) holder).entry1.setText(gratitudePOJO.getEntry1());
            ((EntriesViewHolder) holder).entry2.setText(gratitudePOJO.getEntry2());
            ((EntriesViewHolder) holder).entry3.setText(gratitudePOJO.getEntry3());
            ((EntriesViewHolder) holder).notes.setText(gratitudePOJO.getNote());
        }

        @Override
        public int getItemCount() {
            return gratitudePOJOArrayList.size();
        }

        void deleteEntry(int position) {
            firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_GRATITUDE + "/gratitude/").child(gratitudePOJOArrayList.get(position).getEntryKey()).removeValue();
            gratitudePOJOArrayList.remove(position);
            notifyItemRemoved(position);
        }

        void showEditDialog(final int position) {
            final GratitudePOJO gratitudePOJO = gratitudePOJOArrayList.get(position);
            mEditEntryDialog = new BottomSheetDialog(getActivity());
            mEditEntryDialog.setContentView(R.layout.dialog_edit_gratitude_entry_layout);
            ImageButton mClose;
            final EditText entry1, entry2, entry3, entryNote;
            Button mDone;

            mClose = mEditEntryDialog.findViewById(R.id.close_gratitude_entry_dialog);
            entry1 = mEditEntryDialog.findViewById(R.id.entry_1_edittext);
            entry2 = mEditEntryDialog.findViewById(R.id.entry_2_edittext);
            entry3 = mEditEntryDialog.findViewById(R.id.entry_3_edittext);
            entryNote = mEditEntryDialog.findViewById(R.id.entry_note_edittext);

            mDone = mEditEntryDialog.findViewById(R.id.done_gratitude_entry_button);

            entry1.setText(gratitudePOJO.getEntry1());
            entry2.setText(gratitudePOJO.getEntry2());
            entry3.setText(gratitudePOJO.getEntry3());

            entryNote.setText(gratitudePOJO.getNote());


            mClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditEntryDialog.dismiss();
                }
            });

            mDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String entry1Value = entry1.getText().toString().trim();
                    String entry2Value = entry2.getText().toString().trim();
                    String entry3Value = entry3.getText().toString().trim();
                    String entryNotevalue = entryNote.getText().toString().trim();
                    if (entryNotevalue.isEmpty()) {
                        entryNotevalue = "";
                    }
                    if (!(entry1Value.isEmpty() || entry2Value.isEmpty() || entry3Value.isEmpty())) {
                        DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_GRATITUDE + "/gratitude/");
                        databaseReference.keepSynced(true);
                        Map<String, Object> entryMap = new HashMap<>();
                        GratitudePOJO gratitudePOJO1 = new GratitudePOJO(gratitudePOJO.getEntryKey(), entry1Value, entry2Value, entry3Value, moodChoice, entryNotevalue, gratitudePOJO.getDate(), userInfoPOJO);
                        entryMap.put(gratitudePOJO.getEntryKey(), gratitudePOJO1);
                        databaseReference.updateChildren(entryMap);
                        gratitudePOJOArrayList.set(position, gratitudePOJO1);
                        mEditEntryDialog.dismiss();
                        notifyItemChanged(position);
                    } else {
                        Toast.makeText(getActivity(), "Cannot be blank", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mEditEntryDialog.setCanceledOnTouchOutside(false);
            mEditEntryDialog.show();
        }


        public class EntriesViewHolder extends RecyclerView.ViewHolder {
            public TextView date, entry1, entry2, entry3, notes;
            public ImageButton edit;
            public ImageButton delete;


            public EntriesViewHolder(View itemView) {
                super(itemView);

                date = itemView.findViewById(R.id.gratitude_entry_view_date);
                entry1 = itemView.findViewById(R.id.entry1_textview);
                entry2 = itemView.findViewById(R.id.entry2_textview);
                entry3 = itemView.findViewById(R.id.entry3_textview);
                notes = itemView.findViewById(R.id.gratitude_note_textview);
                edit = itemView.findViewById(R.id.edit_gratitude_entry_button);
                delete = itemView.findViewById(R.id.delete_gratitude_entry_button);

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDialog(getPosition());
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteEntry(getPosition());
                    }
                });

            }
        }

    }

    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
