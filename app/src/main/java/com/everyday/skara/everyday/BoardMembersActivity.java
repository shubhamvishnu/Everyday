package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.pojo.BoardMembersPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Member;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BoardMembersActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList;
    RecyclerView mMembersRecyclerView;
    MembersAdapter membersAdapter;
    LinearLayout mEmptyLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_members);

        Toolbar myToolbar = findViewById(R.id.board_members_toolbar);
        myToolbar.setTitle("Board Members");
        setSupportActionBar(myToolbar);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void init() {
        Intent intent = getIntent();

        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        boardMembersPOJOArrayList = new ArrayList<>();

        mMembersRecyclerView = findViewById(R.id.board_members_recyclerview);
        mEmptyLinearLayout = (LinearLayout) findViewById(R.id.board_members_empty_linear_layout);

        setEmptyVisibility(0);

        fetchMembers();

    }

    void fetchMembers() {
        boardMembersPOJOArrayList = new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/members/");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {

                    setEmptyVisibility(1);

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        boardMembersPOJOArrayList.add(snapshot.getValue(BoardMembersPOJO.class));
                    }
                    initRecyclerView();
                } else {
                    setEmptyVisibility(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void setEmptyVisibility(int action) {
        switch (action) {
            case 0:
                mMembersRecyclerView.setVisibility(View.INVISIBLE);
                mEmptyLinearLayout.setVisibility(LinearLayout.VISIBLE);
                break;
            case 1:
                mMembersRecyclerView.setVisibility(View.VISIBLE);
                mEmptyLinearLayout.setVisibility(LinearLayout.INVISIBLE);
                break;
        }
    }

    void initRecyclerView() {
        mMembersRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mMembersRecyclerView.setLayoutManager(linearLayoutManager);
        membersAdapter = new MembersAdapter();
        mMembersRecyclerView.setAdapter(membersAdapter);
    }

    public class MembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;

        public MembersAdapter() {
            try {
                this.inflator = LayoutInflater.from(BoardMembersActivity.this);
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_board_members_row_layout, parent, false);
            BoardViewHolder viewHolder = new BoardViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            BoardMembersPOJO boardMembersPOJO = boardMembersPOJOArrayList.get(position);
            UserInfoPOJO userInfoPOJO = boardMembersPOJO.getUserInfoPOJO();

            ((BoardViewHolder) holder).mName.setText(userInfoPOJO.getName());
            ((BoardViewHolder) holder).mEmail.setText(userInfoPOJO.getName());
            Glide.with(BoardMembersActivity.this).load(userInfoPOJO.getProfile_url()).into(((BoardViewHolder) holder).mCircleImageView);

            //todo: implement glide
        }

        @Override
        public int getItemCount() {
            return boardMembersPOJOArrayList.size();
        }

        void deleteMember(int position) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            BoardMembersPOJO boardMembersPOJO = boardMembersPOJOArrayList.get(position);

            firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/members/" + boardMembersPOJO.getMemberKey()).removeValue();
            firebaseDatabase.getReference(FirebaseReferences.FIREBASE_OTHER_BOARDS_INFO + boardMembersPOJO.getUserInfoPOJO().getUser_key() + "/" + boardPOJO.getBoardKey()).removeValue();

            boardMembersPOJOArrayList.remove(position);
            membersAdapter.notifyItemRemoved(position);

            if(boardMembersPOJOArrayList.size() == 0){
                setEmptyVisibility(0);
            }

        }

        public class BoardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public CircleImageView mCircleImageView;
            public TextView mName, mEmail;
            public ImageButton mDelete;

            public BoardViewHolder(View itemView) {
                super(itemView);
                mCircleImageView = itemView.findViewById(R.id.member_profile_image);
                mName = itemView.findViewById(R.id.member_name);
                mEmail = itemView.findViewById(R.id.member_email);
                mDelete = itemView.findViewById(R.id.delete_member_to_group_button);

                mDelete.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.delete_member_to_group_button:
                        if (userInfoPOJO.getUser_key().equals(boardPOJO.getCreatedByProfilePOJO().getUser_key())) {
                            deleteMember(getPosition());
                        } else {
                            Toast.makeText(BoardMembersActivity.this, "Not the admin. You cannot delete a member.", Toast.LENGTH_SHORT).show();
                        }
                        break;

                }
            }
        }
    }

    void toLoginActivity() {
        Intent intent = new Intent(BoardMembersActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
