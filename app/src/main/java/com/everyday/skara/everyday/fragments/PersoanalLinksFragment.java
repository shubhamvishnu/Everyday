package com.everyday.skara.everyday.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.BasicSettings;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.LinkPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersoanalLinksFragment extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    FirebaseDatabase firebaseDatabase;
    DatabaseReference linksDatabaseReference;
    UserInfoPOJO userInfoPOJO;
    ChildEventListener childEventListener;

    RecyclerView mLinksRecyclerView;
    LinksAdapter mLinksAdapter;
    ArrayList<LinkPOJO> linkPOJOArrayList;
    View view;
    ImageButton mFilterButton;
    BottomSheetDialog mEditLinksDialog;
    LinearLayout mEmptyLinearLayout, mFragmentLinearLayout;
    public static boolean clicked = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences sp = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        int theme = sp.getInt("theme", BasicSettings.DEFAULT_THEME);
        if(theme == BasicSettings.LIGHT_THEME) {
            view = inflater.inflate(R.layout.fragment_links_layout_light, container, false);
            return view;

        }else{
            view = inflater.inflate(R.layout.fragment_links_layout, container, false);
            return view;

        }
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
        firebaseDatabase = FirebaseDatabase.getInstance();

        linksDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() +"/"+FirebaseReferences.FIREBASE_PERSONAL_BOARD_PROD + "/links/");
        linksDatabaseReference.keepSynced(true);
        linkPOJOArrayList = new ArrayList<>();
        mLinksRecyclerView = view.findViewById(R.id.links_view_recycler);

        //mFilterButton = getActivity().findViewById(R.id.filter_option_button);

        mEmptyLinearLayout = (LinearLayout) getActivity().findViewById(R.id.board_no_link_linear_layout);
        mEmptyLinearLayout.setVisibility(View.GONE);

        mFragmentLinearLayout = (LinearLayout) getActivity().findViewById(R.id.linear_layout_links_fragment);

//        mFilterButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                clicked = !clicked;
//                if (clicked) {
//                    mFilterButton.setRotation(180);
//                    sortDateAscending();
//                    mLinksAdapter.notifyDataSetChanged();
//                } else {
//                    mFilterButton.setRotation(0);
//                    sortDateDescending();
//                    mLinksAdapter.notifyDataSetChanged();
//                }
//            }
//        });
        initLinksRecyclerView();
    }

    void setEmptyVisibility(int action) {
        switch (action) {
            case 0:
                mFragmentLinearLayout.setVisibility(LinearLayout.GONE);
                mEmptyLinearLayout.setVisibility(LinearLayout.VISIBLE);
                break;
            case 1:
                mFragmentLinearLayout.setVisibility(LinearLayout.VISIBLE);
                mEmptyLinearLayout.setVisibility(LinearLayout.GONE);
                break;
        }
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

    void initLinksRecyclerView() {
        mLinksRecyclerView.invalidate();
        mLinksRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mLinksRecyclerView.setLayoutManager(linearLayoutManager);
        mLinksAdapter = new LinksAdapter();
        mLinksRecyclerView.setAdapter(mLinksAdapter);

        initLinks();
    }

    void initLinks() {
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                setEmptyVisibility(1);
                LinkPOJO linkPOJO = dataSnapshot.getValue(LinkPOJO.class);
                linkPOJOArrayList.add(linkPOJO);
                mLinksAdapter.notifyItemInserted(linkPOJOArrayList.size() - 1);
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
        SharedPreferences sp = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        int theme = sp.getInt("theme", BasicSettings.DEFAULT_THEME);
        if(theme == BasicSettings.LIGHT_THEME) {
            mEditLinksDialog.setContentView(R.layout.dialog_edit_links_layout_light);

        }else{
            mEditLinksDialog.setContentView(R.layout.dialog_edit_links_layout);

        }

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
                DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/links/" + linkPOJO.getLinkKey() + "/");
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

    void deleteLink(final int position) {

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
                        firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/links/"+ linkPOJOArrayList.get(position).getLinkKey() + "/").removeValue();
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
            SharedPreferences sp = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
            int theme = sp.getInt("theme", BasicSettings.DEFAULT_THEME);
            if(theme == BasicSettings.LIGHT_THEME) {
                View view = inflator.inflate(R.layout.recyclerview_links_view_row_layout_light, parent, false);
                return new LinksViewHolder(view);

            }else{
                View view = inflator.inflate(R.layout.recyclerview_links_view_row_layout, parent, false);
                return new LinksViewHolder(view);

            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            LinkPOJO linkPOJO = linkPOJOArrayList.get(position);
            ((LinksAdapter.LinksViewHolder) holder).date.setText(linkPOJO.getDate());
            ((LinksAdapter.LinksViewHolder) holder).title.setText(linkPOJO.getTitle());
            SpannableString content = new SpannableString(linkPOJO.getLink());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            ((LinksAdapter.LinksViewHolder) holder).link.setText(content);
        }

        @Override
        public int getItemCount() {
            if (linkPOJOArrayList.size() <= 0) {
                setEmptyVisibility(0);
            } else {
                setEmptyVisibility(1);
            }
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
