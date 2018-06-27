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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.pojo.ActivityPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.TodoInfoPOJO;
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

public class TodoActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, todoDatabaseReference, todoInfoReference;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    EditText mItemEditText, mTodoTitle;
    ImageButton mTodoDone;
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
        databaseReference.keepSynced(true);
        todoDatabaseReference = databaseReference.push();
        todoDatabaseReference.keepSynced(true);
        todoInfoReference = todoDatabaseReference.child("info");
        todoInfoReference.keepSynced(true);

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
            if (!item.isEmpty()) {
                DatabaseReference todoReference = todoDatabaseReference.child("todo_items").push();
                todoReference.keepSynced(true);
                if (title.isEmpty()) {
                    title = "";
                }
                //String title, String date, String todoKey, String lastModified
                TodoInfoPOJO todoInfoPOJO = new TodoInfoPOJO(title, DateTimeStamp.getDate(), todoDatabaseReference.getKey(), DateTimeStamp.getDate());

                todoInfoReference.setValue(todoInfoPOJO);
                final TodoPOJO todoPOJO = new TodoPOJO(item, todoReference.getKey(), false, DateTimeStamp.getDate(), todoDatabaseReference.getKey(), userInfoPOJO);
                todoReference.setValue(todoPOJO);
                ActivityPOJO activityPOJO = new ActivityPOJO("New item added on " + boardPOJO.getDate() + "by" + userInfoPOJO.getName(), boardPOJO.getDate(), userInfoPOJO);
                firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey()).child("activity").push().setValue(activityPOJO);
                todoPOJOArrayList.add(todoPOJO);
                todoListAdapter.notifyItemInserted(todoPOJOArrayList.size());
                Toast.makeText(TodoActivity.this, "Item added", Toast.LENGTH_SHORT).show();
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
        public final int VIEW_MAIN = 1;
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference;
        private LayoutInflater inflator;


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
            ((TodoViewHolder) holder).mCheckBox.setChecked(todoPOJO.isState());
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
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    updateState(getPosition(), isChecked);
                }
            });
        }


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.delete_item:
                    deleteItem(getPosition());
                    break;
            }
        }

        void updateState(final int position, final boolean isChecked) {
            DatabaseReference todoItemReference = databaseReference.child(todoDatabaseReference.getKey()).child("todo_items").child(todoPOJOArrayList.get(position).getItemKey());
            todoItemReference.keepSynced(true);
            HashMap<String, Object> stateMap = new HashMap<>();
            stateMap.put("state", isChecked);
            todoItemReference.updateChildren(stateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    todoPOJOArrayList.get(position).setState(isChecked);
                }
            });
        }

        void deleteItem(final int position) {
            DatabaseReference todoItemReference = databaseReference.child(todoDatabaseReference.getKey()).child("todo_items").child(todoPOJOArrayList.get(position).getItemKey());
            todoItemReference.keepSynced(true);
            todoItemReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    try {
                        todoPOJOArrayList.remove(position);
                        todoListAdapter.notifyItemRemoved(position);

                        if (todoPOJOArrayList.size() == 0) {
                            todoDatabaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    } catch (IndexOutOfBoundsException e) {

                    }
                }
            });
        }
    }
}

