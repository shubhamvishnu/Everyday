package com.everyday.skara.everyday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.ActivityPOJO;
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
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;

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

    void initBoards() {
        boardPOJOArrayList = new ArrayList<>();
        boardsReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_OTHER_BOARDS_INFO + userInfoPOJO.getUser_key());
        boardsReference.keepSynced(true);


        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                BoardPOJO boardPOJO = dataSnapshot.getValue(BoardPOJO.class);
                boardPOJOArrayList.add(boardPOJO);
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
        Intent intent = new Intent(this, BoardActivity.class);
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
            View view = inflator.inflate(R.layout.recyclerview_boards_row_layout, parent, false);
            BoardsAdapter.BoardsViewHolder viewHolder = new BoardsAdapter.BoardsViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull BoardsAdapter.BoardsViewHolder holder, int position) {
            BoardPOJO boardPOJO = boardPOJOArrayList.get(position);
            holder.boardTitle.setText(boardPOJO.getTitle());
        }

        @Override
        public int getItemCount() {
            return boardPOJOArrayList.size();
        }

        public class BoardsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public Button boardTitle;

            public BoardsViewHolder(View itemView) {
                super(itemView);
                boardTitle = itemView.findViewById(R.id.boards_title_button);

                boardTitle.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.boards_title_button:
                        toBoardActivity(boardPOJOArrayList.get(getPosition()));
                        break;

                }
            }
        }
    }
}
