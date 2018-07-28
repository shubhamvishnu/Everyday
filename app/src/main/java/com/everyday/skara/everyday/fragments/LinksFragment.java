package com.everyday.skara.everyday.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.NewOptionTypes;
import com.everyday.skara.everyday.classes.Todo;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

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
    ImageButton mFilterButton;
    BottomSheetDialog mEditLinksDialog, mFilterDialog;

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

        mFilterButton = getActivity().findViewById(R.id.filter_option_button);
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
        initLinksRecyclerView();
    }
    void showFilterDialog() {
        Button mAsc, mDesc;
        mFilterDialog = new BottomSheetDialog(getActivity());
        mFilterDialog.setContentView(R.layout.dialog_filter_layout);

        mAsc = mFilterDialog.findViewById(R.id.filter_date_ascending);
        mDesc = mFilterDialog.findViewById(R.id.filter_date_descending);

        mAsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortDateAscending();
                mLinksAdapter.notifyDataSetChanged();
                mFilterDialog.dismiss();
            }
        });
        mDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortDateDescending();
                mLinksAdapter.notifyDataSetChanged();
                mFilterDialog.dismiss();
            }
        });

        mFilterDialog.setCanceledOnTouchOutside(true);
        mFilterDialog.show();
    }
    void sortDateAscending() {
        Collections.sort(linkPOJOArrayList, new Comparator<LinkPOJO>() {
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            @Override
            public int compare(LinkPOJO o1, LinkPOJO o2) {
                try {
                    return f.parse(o1.getDate()).compareTo(f.parse(o2.getDate()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }
    void sortDateDescending() {
        Collections.sort(linkPOJOArrayList, new Comparator<LinkPOJO>() {
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            @Override
            public int compare(LinkPOJO o1, LinkPOJO o2) {
                try {
                    return f.parse(o2.getDate()).compareTo(f.parse(o1.getDate()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
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
        CircleImageView mProfileImage;
        TextView mName, mEmail;

        ImageButton mClose;
        Button mDone;
        mEditLinksDialog = new BottomSheetDialog(getActivity());
        mEditLinksDialog.setContentView(R.layout.dialog_edit_links_layout);

        final EditText mTitle = mEditLinksDialog.findViewById(R.id.title_edit_link);
        final EditText mContent = mEditLinksDialog.findViewById(R.id.link_edit_link);

        mClose = mEditLinksDialog.findViewById(R.id.close_edit_link);
        mDone = mEditLinksDialog.findViewById(R.id.done_edit_link);

        mProfileImage = mEditLinksDialog.findViewById(R.id.link_created_member_image_view);
        mName = mEditLinksDialog.findViewById(R.id.link_created_name_view);
        mEmail = mEditLinksDialog.findViewById(R.id.link_created_email_view);

        final LinkPOJO linkPOJO = linkPOJOArrayList.get(position);

        mName.setText(linkPOJO.getUserInfoPOJO().getName());
        mEmail.setText(linkPOJO.getUserInfoPOJO().getEmail());
        Glide.with(mEditLinksDialog.getContext()).load(linkPOJO.getUserInfoPOJO().getProfile_url()).into(mProfileImage);

        mTitle.setText(linkPOJO.getTitle());
        mContent.setText(linkPOJO.getLink());

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

                link.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = linkPOJOArrayList.get(getPosition()).getLink();
                        if (!url.startsWith("http://") || !url.startsWith("https://")) {
                            url = "http://" + url;
                        }
                        openWebBrowser(url);
                    }
                });
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
    void openWebBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(browserIntent);
    }

}
