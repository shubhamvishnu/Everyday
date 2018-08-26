package com.everyday.skara.everyday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.everyday.skara.everyday.classes.BoardTypes;
import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.OtherBoardViewHolderClass;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.BoardMembersPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.everyday.skara.everyday.pojo.UserProfilePOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherBoardsActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference boardsReference;
    ChildEventListener childEventListener;
    UserProfilePOJO userProfilePOJO;
    UserInfoPOJO userInfoPOJO;

    // View elements
    RecyclerView mBoardsRecyclerView;

    // Dialog
    BottomSheetDialog mNewBoardDialog;

    // Dialog Components
    EditText mTitle;
    TextView mDate;
    Button mDone;

    // RecyclerView
    BoardsAdapter boardsAdapter;
    ArrayList<BoardPOJO> boardPOJOArrayList;
    ArrayList<OtherBoardViewHolderClass> boardViewHolderClassArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_boards);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        mBoardsRecyclerView = findViewById(R.id.recyclerview_other_boards);

        Toolbar myToolbar = findViewById(R.id.other_boards_toolbar);
        myToolbar.setTitle("Other Boards");
        setSupportActionBar(myToolbar);

        // initializing UserProfilePOJO
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.USER_DETAILS, MODE_PRIVATE);
        String name = sharedPreferences.getString("name", null);
        String email = sharedPreferences.getString("email", null);
        String profile_url = sharedPreferences.getString("url", null);
        String user_key = sharedPreferences.getString("user_key", null);
        String login_type = sharedPreferences.getString("login_type", null);
        int user_account_type = sharedPreferences.getInt("user_account_type", 0);

        userProfilePOJO = new UserProfilePOJO(name, email, profile_url, user_key, login_type, user_account_type);
        userInfoPOJO = new UserInfoPOJO(name, email, profile_url, user_key);

        initRecyclerView();


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_other_board_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_my_boards:
              toMainActivity();
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    void initBoards() {
        boardViewHolderClassArrayList = new ArrayList<>();
        boardPOJOArrayList = new ArrayList<>();
        boardsReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_OTHER_BOARDS_INFO + userInfoPOJO.getUser_key());
        boardsReference.keepSynced(true);


        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                BoardPOJO boardPOJO = dataSnapshot.getValue(BoardPOJO.class);
                boardPOJOArrayList.add(boardPOJO);
                // int position, MainActivity.BoardsAdapter boardsAdapter, BoardMembersActivity.MembersAdapter membersAdapter, ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList
                OtherBoardViewHolderClass boardViewHolderClass = new OtherBoardViewHolderClass((boardPOJOArrayList.size() - 1), boardsAdapter, new OtherBoardsActivity.MembersViewAdapter((boardPOJOArrayList.size() - 1)), new ArrayList<BoardMembersPOJO>());
                boardViewHolderClassArrayList.add(boardViewHolderClass);
                boardsAdapter.notifyItemInserted(boardPOJOArrayList.size() - 1);
                fetchBoardMembers(boardPOJO, (boardPOJOArrayList.size() - 1));
                boardsAdapter.notifyItemInserted(boardPOJOArrayList.size() - 1);

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
        boardsReference.addChildEventListener(childEventListener);


    }

    void fetchBoardMembers(final BoardPOJO boardPOJO, final int position) {
        final ArrayList<BoardMembersPOJO> membersPOJOS = new ArrayList<>();
        final DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/members");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        BoardMembersPOJO membersPOJO = snapshot.getValue(BoardMembersPOJO.class);
                        membersPOJOS.add(membersPOJO);
                    }
                    OtherBoardViewHolderClass boardViewHolderClass = boardViewHolderClassArrayList.get(position);
                    boardViewHolderClass.setBoardMembersPOJOArrayList(membersPOJOS);
                    boardViewHolderClassArrayList.set(position, boardViewHolderClass);
                    boardViewHolderClass.getMembersAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void initRecyclerView() {
        boardPOJOArrayList = new ArrayList<>();

        mBoardsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mBoardsRecyclerView.setLayoutManager(linearLayoutManager);
        boardsAdapter = new BoardsAdapter();
        mBoardsRecyclerView.setAdapter(boardsAdapter);

        initBoards();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (childEventListener != null) {
            boardsReference.removeEventListener(childEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (childEventListener != null) {
            boardsReference.removeEventListener(childEventListener);
        }
    }

    void toLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        toMainActivity();
    }

    void toMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }


    void showInternetAlerter() {
        Alerter.create(this)
                .setText("Oops! no internet connection...")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Connectivity.openInternetSettings(getApplicationContext());
                    }
                })
                .setBackgroundColorRes(R.color.colorAccent)
                .show();
    }

    void toBoardActivity(BoardPOJO boardPOJO) {
        Intent intent = null;
        if(boardPOJO.getBoardType() == BoardTypes.BOARD_TYPE_PRODUCTIVITY) {
            intent = new Intent(OtherBoardsActivity.this, BoardActivity.class);
        }else if(boardPOJO.getBoardType() == BoardTypes.BOARD_TYPE_FINANCIAL){
            intent = new Intent(OtherBoardsActivity.this, FinancialBoardActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    public class BoardsAdapter extends RecyclerView.Adapter<BoardsAdapter.BoardsViewHolder> {

        private LayoutInflater inflator;

        public BoardsAdapter() {
            try {
                this.inflator = LayoutInflater.from(OtherBoardsActivity.this);
            } catch (NullPointerException e) {

            }
        }


        @NonNull
        @Override
        public BoardsAdapter.BoardsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_other_boards_row_layout, parent, false);
            BoardsAdapter.BoardsViewHolder viewHolder = new BoardsAdapter.BoardsViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull BoardsAdapter.BoardsViewHolder holder, int position) {
            BoardPOJO boardPOJO = boardPOJOArrayList.get(position);
            holder.boardTitle.setText(boardPOJO.getTitle());
            holder.mName.setText(boardPOJO.getCreatedByProfilePOJO().getName());
            holder.mEmail.setText(boardPOJO.getCreatedByProfilePOJO().getEmail());
            Glide.with(OtherBoardsActivity.this).load(boardPOJO.getCreatedByProfilePOJO().getProfile_url()).into(holder.mProfileImage);

            initBoardMembersRecyclerview(holder, position);
        }

        void initBoardMembersRecyclerview(BoardsAdapter.BoardsViewHolder holder, int position) {
            holder.mMemberRecyclerview.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(holder.mMemberRecyclerview.getContext());
            holder.mMemberRecyclerview.setLayoutManager(linearLayoutManager);
            OtherBoardsActivity.MembersViewAdapter membersAdapter = boardViewHolderClassArrayList.get(position).getMembersAdapter();
            holder.mMemberRecyclerview.setAdapter(membersAdapter);
        }


        @Override
        public int getItemCount() {
            return boardPOJOArrayList.size();
        }

        public class BoardsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public Button boardTitle;
            public RecyclerView mMemberRecyclerview;
            public CircleImageView mProfileImage;
            public TextView mEmail, mName;

            public BoardsViewHolder(View itemView) {
                super(itemView);
                boardTitle = itemView.findViewById(R.id.boards_other_title_button);

                mProfileImage = itemView.findViewById(R.id.admin_profile_image);
                mEmail = itemView.findViewById(R.id.admin_email);
                mName = itemView.findViewById(R.id.admin_name);

                mMemberRecyclerview = itemView.findViewById(R.id.other_boards_view_members_recyclerview);
                boardTitle.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.boards_other_title_button:
                        toBoardActivity(boardPOJOArrayList.get(getPosition()));
                        break;

                }
            }
        }
    }

    public class MembersViewAdapter extends RecyclerView.Adapter<MembersViewAdapter.MembersViewHolder> {

        private LayoutInflater inflator;
        int position;

        public MembersViewAdapter(int position) {
            try {
                this.inflator = LayoutInflater.from(OtherBoardsActivity.this);
                this.position = position;
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public MembersViewAdapter.MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_boards_view_members_row_layout, parent, false);
            MembersViewAdapter.MembersViewHolder viewHolder = new MembersViewAdapter.MembersViewHolder(view);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(@NonNull MembersViewAdapter.MembersViewHolder holder, int position) {
            holder.mName.setText(boardViewHolderClassArrayList.get(this.position).getBoardMembersPOJOArrayList().get(position).getUserInfoPOJO().getName());
            holder.mEmail.setText(boardViewHolderClassArrayList.get(this.position).getBoardMembersPOJOArrayList().get(position).getUserInfoPOJO().getEmail());
            Glide.with(OtherBoardsActivity.this).load(boardViewHolderClassArrayList.get(this.position).getBoardMembersPOJOArrayList().get(position).getUserInfoPOJO().getProfile_url()).into(holder.mProfile);
        }

        @Override
        public int getItemCount() {
            return boardViewHolderClassArrayList.get(position).getBoardMembersPOJOArrayList().size();
        }

        class MembersViewHolder extends RecyclerView.ViewHolder {
            public TextView mName, mEmail;
            public CircleImageView mProfile;

            public MembersViewHolder(View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.name_board_members_view_textview);
                mEmail = itemView.findViewById(R.id.email_board_members_view_textview);
                mProfile = itemView.findViewById(R.id.profile_image_board_members_view);
            }
        }
    }
}
