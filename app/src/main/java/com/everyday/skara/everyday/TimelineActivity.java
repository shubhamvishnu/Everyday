package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.everyday.skara.everyday.classes.ActionType;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.pojo.ActivityPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.ChatPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.github.vipulasri.timelineview.TimelineView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class TimelineActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ChildEventListener mChildEventListener;

    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    ArrayList<ActivityPOJO> activityPOJOArrayList;

    RecyclerView mTimelineRecyclerView;
    TimelineAdapter mTimelineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        Toolbar myToolbar = findViewById(R.id.boards_toolbar);
        setSupportActionBar(myToolbar);

        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void toLoginActivity() {
        Intent intent = new Intent(TimelineActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        Intent intent = getIntent();
        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");
        activityPOJOArrayList = new ArrayList<>();
        mTimelineRecyclerView = findViewById(R.id.recyclerview_timeline);
        initRecyclerView();
    }

    void initRecyclerView() {
        mTimelineRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mTimelineRecyclerView.setLayoutManager(linearLayoutManager);
        mTimelineAdapter = new TimelineAdapter();
        mTimelineRecyclerView.setAdapter(mTimelineAdapter);
        fetchActivity();

    }

    void fetchActivity() {
        activityPOJOArrayList = new ArrayList<>();
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/activity/");
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ActivityPOJO activityPOJO = dataSnapshot.getValue(ActivityPOJO.class);
                activityPOJOArrayList.add(activityPOJO);
                mTimelineAdapter.notifyItemInserted(activityPOJOArrayList.size() - 1);
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
        databaseReference.addChildEventListener(mChildEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mChildEventListener != null) {
            databaseReference.removeEventListener(mChildEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChildEventListener != null) {
            databaseReference.removeEventListener(mChildEventListener);
        }
    }

    public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;

        public TimelineAdapter() {
            try {
                this.inflator = LayoutInflater.from(TimelineActivity.this);
            } catch (NullPointerException e) {

            }
        }

        @Override
        public int getItemViewType(int position) {
            ActivityPOJO activityPOJO = activityPOJOArrayList.get(position);
            if (activityPOJO.getActionType() == ActionType.ACTION_TYPE_CREATE_BOARD) {
                return ActionType.ACTION_TYPE_CREATE_BOARD;
            } else if (activityPOJO.getActionType() == ActionType.ACTION_TYPE_NEW_TODO) {
                return ActionType.ACTION_TYPE_NEW_TODO;
            }else if (activityPOJO.getActionType() == ActionType.ACTION_TYPE_NEW_NOTE) {
                return ActionType.ACTION_TYPE_NEW_NOTE;
            } else if (activityPOJO.getActionType() == ActionType.ACTION_TYPE_NEW_LINK) {
                return ActionType.ACTION_TYPE_NEW_LINK;
            } else {
                return TimelineView.getTimeLineViewType(position, getItemCount());
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == ActionType.ACTION_TYPE_CREATE_BOARD) {
                View view = View.inflate(parent.getContext(), R.layout.timeline_create_board_row_layout, null);
                return new TimeLineViewHolder(view, viewType);
            } else if (viewType == ActionType.ACTION_TYPE_NEW_TODO) {
                View view = View.inflate(parent.getContext(), R.layout.timeline_new_todo_row_layout, null);
                return new TimeLineViewHolder(view, viewType);
            }  else if (viewType == ActionType.ACTION_TYPE_NEW_NOTE) {
                View view = View.inflate(parent.getContext(), R.layout.timeline_new_note_row_layout, null);
                return new TimeLineViewHolder(view, viewType);
            } else if (viewType == ActionType.ACTION_TYPE_NEW_LINK) {
                View view = View.inflate(parent.getContext(), R.layout.timeline_new_link__row_layout, null);
                return new TimeLineViewHolder(view, viewType);
            } else {
                View view = View.inflate(parent.getContext(), R.layout.item_timeline, null);
                return new TimeLineViewHolder(view, viewType);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ActivityPOJO activityPOJO = activityPOJOArrayList.get(position);
            ((TimeLineViewHolder) holder).mTimelineView.setMarker(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_marker_timeline), ContextCompat.getColor(getApplicationContext(), R.color.blue));
            ((TimeLineViewHolder) holder).mDate.setText(activityPOJO.getTime());
            ((TimeLineViewHolder) holder).mMessage.setText(activityPOJO.getAction());
        }

        @Override
        public int getItemCount() {
            return activityPOJOArrayList.size();
        }

        public class TimeLineViewHolder extends RecyclerView.ViewHolder {
            TextView mDate;
            TextView mMessage;
            TimelineView mTimelineView;

            public TimeLineViewHolder(View itemView, int viewType) {
                super(itemView);
                mDate = itemView.findViewById(R.id.text_timeline_date);
                mMessage = itemView.findViewById(R.id.text_timeline_title);
                mTimelineView = (TimelineView) itemView.findViewById(R.id.time_marker);
                mTimelineView.initLine(viewType);

            }
        }

    }

}
