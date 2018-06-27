package com.everyday.skara.everyday.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.HashMap;

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

    BottomSheetDialog mEditNotesDialog;

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
        notesDatabaseReference.keepSynced(true);
        mNotesRecyclerView = view.findViewById(R.id.notes_view_recycler);

        notePOJOArrayList = new ArrayList<>();
        initNotesRecyclerView();
    }

    void initNotesRecyclerView() {
        mNotesRecyclerView.invalidate();
        mNotesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mNotesRecyclerView.setLayoutManager(linearLayoutManager);
        mNotesAdapter = new NotesAdapter();
        mNotesRecyclerView.setAdapter(mNotesAdapter);

        initNotes();

    }

    void initNotes() {
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                NotePOJO notePOJO = dataSnapshot.getValue(NotePOJO.class);
                notePOJOArrayList.add(notePOJO);
                mNotesAdapter.notifyItemInserted(notePOJOArrayList.size() - 1);
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

    void showEditNotesDialog(final int position) {
        Button mClose, mDone;
        TextView mDate;
        mEditNotesDialog = new BottomSheetDialog(getActivity());
        mEditNotesDialog.setContentView(R.layout.dialog_edit_notes_layout);

        final EditText mTitle = mEditNotesDialog.findViewById(R.id.title_edit_notes);
        final EditText mContent = mEditNotesDialog.findViewById(R.id.content_edit_notes);

        mDate = mEditNotesDialog.findViewById(R.id.date_edit_notes);
        mClose = mEditNotesDialog.findViewById(R.id.close_edit_notes);
        mDone = mEditNotesDialog.findViewById(R.id.done_edit_notes);


        final NotePOJO notePOJO = notePOJOArrayList.get(position);

        mTitle.setText(notePOJO.getTitle());
        mContent.setText(notePOJO.getContent());
        mDate.setText(notePOJO.getDate());

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditNotesDialog.dismiss();
            }
        });
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = mTitle.getText().toString();
                final String content = mContent.getText().toString();
                notePOJO.setTitle(title);
                notePOJO.setContent(content);
                DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/notes/" + notePOJO.getNoteKey() + "/");
                databaseReference.keepSynced(true);
                databaseReference.setValue(notePOJO);
                notePOJOArrayList.set(position, notePOJO);
                mNotesAdapter.notifyItemChanged(position);
                mEditNotesDialog.dismiss();
            }
        });


        mEditNotesDialog.setCanceledOnTouchOutside(false);
        mEditNotesDialog.show();
    }

    void deleteNote(final int position){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        // set title
        alertDialogBuilder.setTitle("Delete");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you want to delete?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/notes/" + notePOJOArrayList.get(position).getNoteKey() + "/").removeValue();
                        notePOJOArrayList.remove(position);
                        mNotesAdapter.notifyItemRemoved(position);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
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
            public Button edit;
            public Button delete;

            public NotesViewHolder(View itemView) {
                super(itemView);

                date = itemView.findViewById(R.id.notes_view_date);
                title = itemView.findViewById(R.id.notes_view_title);
                content = itemView.findViewById(R.id.notes_view_content);

                edit = itemView.findViewById(R.id.edit_notes_button);
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditNotesDialog(getPosition());
                    }
                });

                delete = itemView.findViewById(R.id.delete_notes_button);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteNote(getPosition());
                    }
                });

            }
        }

    }
}
