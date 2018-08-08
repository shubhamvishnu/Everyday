package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.NewOptionTypes;
import com.everyday.skara.everyday.classes.TimeDateStamp;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.ChatPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ChildEventListener chatChildEventListener;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    ArrayList<ChatPOJO> chatPOJOArrayList;
    EditText mMessage;
    ImageButton mSend;
    RecyclerView mChatRecyclerView;
    ChatAdapter chatAdapter;
    BottomSheetDialog mChatInfoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar myToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(myToolbar);

        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        Intent intent = getIntent();
        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        chatPOJOArrayList = new ArrayList<>();

        mMessage = findViewById(R.id.chat_message_edittext);
        mSend = findViewById(R.id.chat_send_button);
        mChatRecyclerView = findViewById(R.id.chats_recyclerview);

        mSend.setOnClickListener(this);
        initRecyclerView();

    }

    void initRecyclerView() {
        mChatRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mChatRecyclerView.setLayoutManager(linearLayoutManager);
        chatAdapter = new ChatAdapter();
        mChatRecyclerView.setAdapter(chatAdapter);
        fetchChats();
    }

    void fetchChats() {
        chatPOJOArrayList = new ArrayList<>();
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/chats");
        databaseReference.keepSynced(true);
        chatChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ChatPOJO chatPOJO = dataSnapshot.getValue(ChatPOJO.class);
                if (!chatPOJO.getSenderInfo().getUser_key().equals(userInfoPOJO.getUser_key())) {
                    ArrayList<UserInfoPOJO> userKeysArrayList;
                    if (chatPOJO.getUserInfoPOJOArrayList() == null || chatPOJO.getUserInfoPOJOArrayList().size() == 0) {
                        userKeysArrayList = new ArrayList<>();
                        userKeysArrayList.add(userInfoPOJO);
                        chatPOJO.setUserInfoPOJOArrayList(userKeysArrayList);

                        DatabaseReference userInfoDatabaseRef = databaseReference;
                        Map<String, Object> chatObjectMap = new HashMap<>();
                        chatObjectMap.put(chatPOJO.getMessageKey(), chatPOJO);
                        userInfoDatabaseRef.updateChildren(chatObjectMap);

                    } else if (!chatPOJO.getUserInfoPOJOArrayList().contains(userInfoPOJO.getUser_key())) {
                        userKeysArrayList = chatPOJO.getUserInfoPOJOArrayList();
                        userKeysArrayList.add(userInfoPOJO);
                        chatPOJO.setUserInfoPOJOArrayList(userKeysArrayList);

                        DatabaseReference userInfoDatabaseRef = databaseReference;
                        Map<String, Object> chatObjectMap = new HashMap<>();
                        chatObjectMap.put(chatPOJO.getMessageKey(), chatPOJO);
                        userInfoDatabaseRef.updateChildren(chatObjectMap);

                    }

                }else{
                    if (chatPOJO.getUserInfoPOJOArrayList() == null || chatPOJO.getUserInfoPOJOArrayList().size() == 0) {
                        chatPOJO.setUserInfoPOJOArrayList(new ArrayList<UserInfoPOJO>());
                    }
                }
                chatPOJOArrayList.add(chatPOJO);
                chatAdapter.notifyItemInserted(chatPOJOArrayList.size() - 1);
                mChatRecyclerView.scrollToPosition(chatPOJOArrayList.size() - 1);
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
        databaseReference.addChildEventListener(chatChildEventListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (chatChildEventListener != null) {
            databaseReference.removeEventListener(chatChildEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatChildEventListener != null) {
            databaseReference.removeEventListener(chatChildEventListener);
        }
    }

    void toLoginActivity() {
        Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void sendMessage() {

        String message = mMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            mMessage.setText(null);
            Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show();
            DatabaseReference chatDatabaseRef = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/chats").push();
            ChatPOJO chatPOJO = new ChatPOJO(chatDatabaseRef.getKey(), 1, userInfoPOJO, message, "date", new ArrayList<UserInfoPOJO>());
            chatDatabaseRef.setValue(chatPOJO);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_send_button:
                sendMessage();
                break;
        }
    }

    public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public int VIEW_SENDER = 1;
        public int VIEW_OTHER = 2;
        private LayoutInflater inflator;


        public ChatAdapter() {
            try {
                this.inflator = LayoutInflater.from(ChatActivity.this);
            } catch (NullPointerException e) {

            }
        }


        @Override
        public int getItemViewType(int position) {
            ChatPOJO chatPOJO = chatPOJOArrayList.get(position);
            if (chatPOJO.getSenderInfo().getUser_key().equals(userInfoPOJO.getUser_key())) {
                return VIEW_SENDER;
            } else {
                return VIEW_OTHER;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_SENDER) {
                View view = inflator.inflate(R.layout.recyclerview_chat_sender_row_layout, parent, false);
                ChatViewSenderHolder viewHolder = new ChatViewSenderHolder(view);
                return viewHolder;
            } else if (viewType == VIEW_OTHER) {
                View view = inflator.inflate(R.layout.recyclerview_chat_other_row_layout, parent, false);
                ChatViewOtherHolder viewHolder = new ChatViewOtherHolder(view);
                return viewHolder;

            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ChatPOJO chatPOJO = chatPOJOArrayList.get(position);
            if (getItemViewType(position) == VIEW_SENDER) {
                ((ChatViewSenderHolder) holder).mMessage.setText(chatPOJO.getMessageText());
            } else if (getItemViewType(position) == VIEW_OTHER) {
                ((ChatViewOtherHolder) holder).mMessage.setText(chatPOJO.getMessageText());
                ((ChatViewOtherHolder) holder).mSenderName.setText(chatPOJO.getSenderInfo().getName());
            }
        }

        @Override
        public int getItemCount() {
            return chatPOJOArrayList.size();
        }

        class ChatViewSenderHolder extends RecyclerView.ViewHolder {
            public TextView mMessage;

            public ChatViewSenderHolder(View itemView) {
                super(itemView);
                mMessage = itemView.findViewById(R.id.chat_message_textview);
                mMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSeenByDialog(chatPOJOArrayList.get(getPosition()));
                    }
                });
            }
        }

        class ChatViewOtherHolder extends RecyclerView.ViewHolder {
            public TextView mMessage, mSenderName;

            public ChatViewOtherHolder(View itemView) {
                super(itemView);
                mMessage = itemView.findViewById(R.id.chat_message_textview);
                mSenderName = itemView.findViewById(R.id.chat_message_sender_name);

                mSenderName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSeenByDialog(chatPOJOArrayList.get(getPosition()));
                    }
                });
                mMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSeenByDialog(chatPOJOArrayList.get(getPosition()));

                    }
                });
            }
        }
    }

    void showSeenByDialog(ChatPOJO chatPOJO) {
        RecyclerView mChatInfoRecyclerView;
        LinearLayout mNoViewsLinearLayout;

        mChatInfoDialog = new BottomSheetDialog(this);
        mChatInfoDialog.setContentView(R.layout.dialog_chat_info_layout);
        ImageButton mClose = mChatInfoDialog.findViewById(R.id.close_chat_info);

        mChatInfoRecyclerView = mChatInfoDialog.findViewById(R.id.recyclerview_chat_info);
        mNoViewsLinearLayout = mChatInfoDialog.findViewById(R.id.no_views_linear_layout);

        if(chatPOJO.getUserInfoPOJOArrayList().size() <= 0){
            mNoViewsLinearLayout.setVisibility(LinearLayout.VISIBLE);
        }else{
            mNoViewsLinearLayout.setVisibility(LinearLayout.INVISIBLE);
        }
        mChatInfoRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mChatInfoRecyclerView.setLayoutManager(linearLayoutManager);
        ChatInfoAdapter chatInfoAdapter= new ChatInfoAdapter(chatPOJO);
        mChatInfoRecyclerView.setAdapter(chatInfoAdapter);

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatInfoDialog.dismiss();
            }
        });
        mChatInfoDialog.setCanceledOnTouchOutside(true);
        mChatInfoDialog.show();

    }

    public class ChatInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;

        ChatPOJO chatPOJO;

        public ChatInfoAdapter(ChatPOJO chatPOJO) {
            try {
                this.inflator = LayoutInflater.from(ChatActivity.this);
                this.chatPOJO = chatPOJO;
            } catch (NullPointerException e) {

            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_chat_info_row_layout, parent, false);
            ChatInfoViewHolder viewHolder = new ChatInfoViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            UserInfoPOJO userInfoPOJO = chatPOJO.getUserInfoPOJOArrayList().get(position);
            ((ChatInfoViewHolder) holder).mName.setText(userInfoPOJO.getName());
            ((ChatInfoViewHolder) holder).mEmail.setText(userInfoPOJO.getEmail());
            Glide.with(ChatActivity.this).load(userInfoPOJO.getProfile_url()).into(((ChatInfoViewHolder) holder).mProfileImage);


        }

        @Override
        public int getItemCount() {
            return chatPOJO.getUserInfoPOJOArrayList().size();
        }

        class ChatInfoViewHolder extends RecyclerView.ViewHolder {
            public TextView mName, mEmail;
            public CircleImageView mProfileImage;

            public ChatInfoViewHolder(View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.chat_member_name);
                mEmail = itemView.findViewById(R.id.chat_member_email);
                mProfileImage = itemView.findViewById(R.id.chat_member_profile_image);
            }
        }
    }
}
