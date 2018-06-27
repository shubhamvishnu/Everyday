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
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.LinkPOJO;
import com.everyday.skara.everyday.pojo.NotePOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class LinksFragment extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    FirebaseDatabase firebaseDatabase;
    DatabaseReference linksDatabaseReference;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    ChildEventListener childEventListener;

    RecyclerView mLinksRecyclerView;
    LinksAdapter mLinksAdapter;
    ArrayList<LinkPOJO> linkPOJOArrayList;
    View view;

    BottomSheetDialog mEditLinksDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_links_layout, container, false);
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

        linksDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/links/");
        linksDatabaseReference.keepSynced(true);
        linkPOJOArrayList = new ArrayList<>();
        mLinksRecyclerView = view.findViewById(R.id.links_view_recycler);
        initLinksRecyclerView();
    }
    void initLinksRecyclerView(){
        mLinksRecyclerView.invalidate();
        mLinksRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mLinksRecyclerView.setLayoutManager(linearLayoutManager);
        mLinksAdapter = new LinksAdapter();
        mLinksRecyclerView.setAdapter(mLinksAdapter);

        initLinks();
    }
    void initLinks(){
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                LinkPOJO linkPOJO = dataSnapshot.getValue(LinkPOJO.class);
                linkPOJOArrayList.add(linkPOJO);
                mLinksAdapter.notifyItemInserted(linkPOJOArrayList.size()-1);

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

        linksDatabaseReference.addChildEventListener(childEventListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (childEventListener != null) {
            linksDatabaseReference.removeEventListener(childEventListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (childEventListener != null) {
            linksDatabaseReference.removeEventListener(childEventListener);
        }
    }
    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    void showEditLinkDialog(final int position) {
        Button mClose, mDone;
        TextView mDate;
        mEditLinksDialog = new BottomSheetDialog(getActivity());
        mEditLinksDialog.setContentView(R.layout.dialog_edit_links_layout);

        final EditText mTitle = mEditLinksDialog.findViewById(R.id.title_edit_link);
        final EditText mContent = mEditLinksDialog.findViewById(R.id.link_edit_link);

        mDate = mEditLinksDialog.findViewById(R.id.date_edit_link);
        mClose = mEditLinksDialog.findViewById(R.id.close_edit_link);
        mDone = mEditLinksDialog.findViewById(R.id.done_edit_link);


        final LinkPOJO linkPOJO = linkPOJOArrayList.get(position);

        mTitle.setText(linkPOJO.getTitle());
        mContent.setText(linkPOJO.getLink());
        mDate.setText(linkPOJO.getDate());

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditLinksDialog.dismiss();
            }
        });
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = mTitle.getText().toString();
                final String content = mContent.getText().toString();
                linkPOJO.setTitle(title);
                linkPOJO.setLink(content);
                DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/links/" + linkPOJO.getLinkKey() + "/");
                databaseReference.keepSynced(true);
                databaseReference.setValue(linkPOJO);
                linkPOJOArrayList.set(position, linkPOJO);
                mLinksAdapter.notifyItemChanged(position);
                mEditLinksDialog.dismiss();
            }
        });


        mEditLinksDialog.setCanceledOnTouchOutside(false);
        mEditLinksDialog.show();
    }
    void deleteLink(final int position){

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
                        firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/links/" + linkPOJOArrayList.get(position).getLinkKey() + "/").removeValue();
                        linkPOJOArrayList.remove(position);
                        mLinksAdapter.notifyItemRemoved(position);
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

    public class LinksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;

        public LinksAdapter() {
            try {
                this.inflator = LayoutInflater.from(getActivity());
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_links_view_row_layout, parent, false);
            return new LinksViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            LinkPOJO linkPOJO = linkPOJOArrayList.get(position);
            ((LinksViewHolder) holder).date.setText(linkPOJO.getDate());
            ((LinksViewHolder) holder).title.setText(linkPOJO.getTitle());
            SpannableString content = new SpannableString(linkPOJO.getLink());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            ((LinksViewHolder) holder).link.setText(content);
        }

        @Override
        public int getItemCount() {
            return linkPOJOArrayList.size();
        }


        public class LinksViewHolder extends RecyclerView.ViewHolder {
            public TextView date, title, link;
            public ImageButton edit, delete;

            public LinksViewHolder(View itemView) {
                super(itemView);

                date = itemView.findViewById(R.id.links_view_date);
                title = itemView.findViewById(R.id.links_view_title);
                link = itemView.findViewById(R.id.links_view_link);
                edit = itemView.findViewById(R.id.edit_link_button);
                delete = itemView.findViewById(R.id.delete_links_button);
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditLinkDialog(getPosition());
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteLink(getPosition());
                    }
                });

            }
        }

    }

}
