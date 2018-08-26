package com.everyday.skara.everyday;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.everyday.skara.everyday.classes.BoardMembersType;
import com.everyday.skara.everyday.classes.BoardTypes;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.pojo.BoardMembersPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddBoardMembersActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    RecyclerView mMemberSearchRecyclerView;
    Button mDoneButton;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference childDatabaseReference;
    ChildEventListener memberChildEventListener;
    SearchAdapter searchAdapter;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList;
    ArrayList<UserInfoPOJO> userInfoPOJOArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_board_members);
        Toolbar myToolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(myToolbar);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void toLoginActivity() {
        Intent intent = new Intent(AddBoardMembersActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void init() {
        Intent intent = getIntent();

        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        userInfoPOJOArrayList = new ArrayList<>();

        mMemberSearchRecyclerView = findViewById(R.id.add_members_recyclerview);
        mDoneButton = findViewById(R.id.done_adding_members_button);

        mDoneButton.setOnClickListener(this);
        initRecyclerView();
    }

    void initRecyclerView() {
        userInfoPOJOArrayList.clear();
        mMemberSearchRecyclerView.invalidate();
        mMemberSearchRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mMemberSearchRecyclerView.setLayoutManager(linearLayoutManager);
        searchAdapter = new SearchAdapter();
        mMemberSearchRecyclerView.setAdapter(searchAdapter);
    }

    void checkExistingBoardMembers(final String input) {
        boardMembersPOJOArrayList = new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference1 = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/members/");
        databaseReference1.keepSynced(true);
        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        boardMembersPOJOArrayList.add(dataSnapshot.getValue(BoardMembersPOJO.class));
                    }
                } else {
                    if (!input.equalsIgnoreCase("")) {
                        fetchFromFirebase(input);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;

        public SearchAdapter() {
            try {
                this.inflator = LayoutInflater.from(AddBoardMembersActivity.this);
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_search_add_members_row_layout, parent, false);
            SearchViewHolder viewHolder = new SearchViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            UserInfoPOJO userInfoPOJO = userInfoPOJOArrayList.get(position);
            ((SearchViewHolder) holder).mName.setText(userInfoPOJO.getName());
            ((SearchViewHolder) holder).mEmail.setText(userInfoPOJO.getEmail());
            Glide.with(AddBoardMembersActivity.this).load(userInfoPOJO.getProfile_url()).into(((SearchViewHolder) holder).mCircleImageView);
        }

        @Override
        public int getItemCount() {
            return userInfoPOJOArrayList.size();
        }


        public class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public CircleImageView mCircleImageView;
            public TextView mName, mEmail;
            public ImageButton mDone;

            public SearchViewHolder(View itemView) {
                super(itemView);
                mCircleImageView = itemView.findViewById(R.id.search_member_profile_image);
                mName = itemView.findViewById(R.id.search_member_name);
                mEmail = itemView.findViewById(R.id.search_member_email);
                mDone = itemView.findViewById(R.id.add_member_to_group_button);

                mDone.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.add_member_to_group_button:
                        addMembersToBoard(getPosition());
                        break;

                }
            }
        }
    }

    void addMembersToBoard(int position) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference1 = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/members/");

        UserInfoPOJO userInfoPOJO = userInfoPOJOArrayList.get(position);
        DatabaseReference memberDatabaseReference = databaseReference1.push();
        BoardMembersPOJO boardMembersPOJO = new BoardMembersPOJO(memberDatabaseReference.getKey(), DateTimeStamp.getDate(), userInfoPOJO, BoardMembersType.TYPE_MEMBER);
        memberDatabaseReference.setValue(boardMembersPOJO);

        // other board info
        DatabaseReference databaseReference2 = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_OTHER_BOARDS_INFO + userInfoPOJO.getUser_key() + "/" + boardPOJO.getBoardKey());
        databaseReference2.setValue(boardPOJO);

        userInfoPOJOArrayList.remove(position);
        searchAdapter.notifyItemRemoved(position);
    }

    void fetchFromFirebase(final String input) {
        userInfoPOJOArrayList = new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        childDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS);
        childDatabaseReference.keepSynced(true);
        memberChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {

                    UserInfoPOJO userInfoPOJO1 = dataSnapshot.getValue(UserInfoPOJO.class);
                    if (!userInfoPOJO1.getUser_key().equals(userInfoPOJO.getUser_key())) {

                        if (!checkForExistenceFromList(userInfoPOJO1)) {
                            if (userInfoPOJO1.getName().contains(input) || userInfoPOJO1.getEmail().contains(input)) {
                                userInfoPOJOArrayList.add(userInfoPOJO1);
                                searchAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                } catch (NullPointerException e) {
                }
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
        childDatabaseReference.addChildEventListener(memberChildEventListener);
    }

    boolean checkForExistenceFromList(UserInfoPOJO userInfoPOJO) {
        if ((boardMembersPOJOArrayList.size() > 0)) {
            for (int i = 0; i < boardMembersPOJOArrayList.size(); i++) {
                if (boardMembersPOJOArrayList.get(i).getUserInfoPOJO().getUser_key().equals(userInfoPOJO)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeChildEventListener();
    }

    void removeChildEventListener() {
        if (memberChildEventListener != null) {
            childDatabaseReference.removeEventListener(memberChildEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeChildEventListener();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.done_adding_members_button:
                removeChildEventListener();
                toBoardsActivity();
                break;
        }
    }

    void toBoardsActivity() {
        Intent intent = null;
        if(boardPOJO.getBoardType() == BoardTypes.BOARD_TYPE_PRODUCTIVITY) {
            intent = new Intent(AddBoardMembersActivity.this, BoardActivity.class);
        }else if(boardPOJO.getBoardType() == BoardTypes.BOARD_TYPE_FINANCIAL){
            intent = new Intent(AddBoardMembersActivity.this, FinancialBoardActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_members_activity, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.white));
        ImageView searchClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(R.drawable.ic_close_white_24dp);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                removeChildEventListener();
                checkExistingBoardMembers(newText);
                return false;
            }
        });
        searchView.setQueryHint("Search");
        return super.onCreateOptionsMenu(menu);
    }
}
