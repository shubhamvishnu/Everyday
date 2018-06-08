package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.TodoItem;
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

public class TodoActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, todoDatabaseReference;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    TodoItem todoItem;
    EditText mItemEditText;
    Button mTodoDone;

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
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/");
        todoDatabaseReference = databaseReference.push();
        Intent intent = getIntent();

        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        todoItem = new TodoItem(TodoItem.LEVEL_MAIN, TodoItem.PARENT_KEY);

        mItemEditText = findViewById(R.id.todo_item_edittext);
        mTodoDone = findViewById(R.id.todo_done);

        mTodoDone.setOnClickListener(this);
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
        if (Connectivity.checkInternetConnection(this)) {
            if (item.isEmpty()) {
                DatabaseReference todoReference = null;
                switch (todoItem.getParentKey()) {
                    case TodoItem.PARENT_KEY:
                        todoReference = todoDatabaseReference.child(TodoItem.PARENT_KEY).push();
                        break;
                    default:
                        todoReference = todoDatabaseReference.child(TodoItem.PARENT_KEY).push();
                        break;
                }

                //String item, String itemKey, boolean state, int itemLevel, String parentKey, String date, UserInfoPOJO userInfoPOJO
                TodoPOJO todoPOJO = new TodoPOJO(item, todoReference.getKey(), TodoItem.NOT_CHECKED, TodoItem.LEVEL_MAIN, TodoItem.PARENT_KEY, DateTimeStamp.getDate(), userInfoPOJO);
                todoReference.setValue(todoPOJO).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ActivityPOJO activityPOJO = new ActivityPOJO("New item added on " + boardPOJO.getDate() + "by" + userInfoPOJO.getName(), boardPOJO.getDate(), userInfoPOJO);
                        firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey()).child("activity").push().setValue(activityPOJO).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
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
}