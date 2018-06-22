package com.everyday.skara.everyday;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.pojo.ActivityPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.TodoPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TodoActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, todoDatabaseReference;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    EditText mItemEditText, mTodoTitle;
    Button mTodoDone;
    ArrayList<TodoPOJO> todoPOJOArrayList;
    RecyclerView mTodoRecyclerView;
    TodoListAdapter todoListAdapter;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

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
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/");
        todoDatabaseReference = databaseReference.push();

        todoPOJOArrayList = new ArrayList<>();

        mItemEditText = findViewById(R.id.todo_item_edittext);
        mTodoTitle = findViewById(R.id.todo_title_edittext);
        mTodoDone = findViewById(R.id.todo_done);
        mTodoRecyclerView = findViewById(R.id.todo_recyclerview);

        mTodoDone.setOnClickListener(this);

        initRecyclerView();
    }

    void initRecyclerView() {
        mTodoRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mTodoRecyclerView.setLayoutManager(linearLayoutManager);
        todoListAdapter = new TodoListAdapter();
        mTodoRecyclerView.setAdapter(todoListAdapter);
    }

    void toLoginActivity() {
        Intent intent = new Intent(TodoActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.todo_done:
                    addTodo();
                break;
        }
    }

    void addTodo() {
        String item = mItemEditText.getText().toString();
        String title = mTodoTitle.getText().toString();
        if (Connectivity.checkInternetConnection(this)) {
            if (!item.isEmpty()) {
                DatabaseReference todoReference = todoDatabaseReference.push();

                //String item, String itemKey, boolean state, int itemLevel, String parentKey, String date, UserInfoPOJO userInfoPOJO
                final TodoPOJO todoPOJO = new TodoPOJO(item, todoReference.getKey(), false, todoDatabaseReference.getKey(), DateTimeStamp.getDate(), userInfoPOJO);
                Map<String, Object> todoTitleMap = new HashMap<>();
                if (title.isEmpty()) {
                    todoTitleMap.put("title", " ");
                } else {
                    todoTitleMap.put("title", title);
                }
                todoDatabaseReference.updateChildren(todoTitleMap);
                todoReference.setValue(todoPOJO).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ActivityPOJO activityPOJO = new ActivityPOJO("New item added on " + boardPOJO.getDate() + "by" + userInfoPOJO.getName(), boardPOJO.getDate(), userInfoPOJO);
                        firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey()).child("activity").push().setValue(activityPOJO).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                todoPOJOArrayList.add(todoPOJO);
                                todoListAdapter.notifyItemInserted(todoPOJOArrayList.size());
                                Toast.makeText(TodoActivity.this, "Item added", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });
            }
        } else {
            showInternetAlerter();
        }
    }

    void showInternetAlerter() {
        Alerter.create(this)
                .setText("No internet connection. Will be saved to cloud later.")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Connectivity.openInternetSettings(getApplicationContext());
                    }
                })
                .setBackgroundColorRes(R.color.colorAccent)
                .show();
    }

    void toBoardsActivity() {
        Intent intent = new Intent(TodoActivity.this, BoardActivity.class);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public class TodoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference;
        private LayoutInflater inflator;
        public final int VIEW_MAIN = 1;


        public TodoListAdapter() {
            try {
                this.inflator = LayoutInflater.from(getApplicationContext());
            } catch (NullPointerException e) {

            }
        }

        @Override
        public int getItemViewType(int position) {
            return VIEW_MAIN;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_MAIN) {
                View view = inflator.inflate(R.layout.recyclerview_todo_item_row_layout, parent, false);
                TodoViewHolder viewHolder = new TodoViewHolder(view);
                return viewHolder;
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TodoPOJO todoPOJO = todoPOJOArrayList.get(position);
            ((TodoViewHolder) holder).mItem.setText(todoPOJO.getItem());
        }

        @Override
        public int getItemCount() {
            return todoPOJOArrayList.size();
        }
    }

    class TodoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CheckBox mCheckBox;
        public EditText mItem;
        public ImageButton mDelete;

        public TodoViewHolder(View itemView) {
            super(itemView);
            mCheckBox = itemView.findViewById(R.id.todo_checkbox);
            mItem = itemView.findViewById(R.id.todo_item);
            mDelete = itemView.findViewById(R.id.delete_item);
            mDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.delete_item:
                    deleteItem(getPosition());
                    break;
            }
        }

        void deleteItem(final int position) {
            todoDatabaseReference.child(todoPOJOArrayList.get(position).getItemKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    try {
                        todoPOJOArrayList.remove(position);
                        todoListAdapter.notifyItemRemoved(position);

                        if(todoPOJOArrayList.size() == 0){
                            todoDatabaseReference.child("title").removeValue();
                        }
                    } catch (IndexOutOfBoundsException e) {

                    }
                }
            });
        }
    }
}

