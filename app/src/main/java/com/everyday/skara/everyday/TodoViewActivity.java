package com.everyday.skara.everyday;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.Todo;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.TodoInfoPOJO;
import com.everyday.skara.everyday.pojo.TodoPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class TodoViewActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    ChildEventListener childEventListener;
    ArrayList<Todo> todoArrayList;

    // View elements
    RecyclerView mTodoRecyclerView;

    // Adapter
    TodoAdapter todoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_view);
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
        todoArrayList = new ArrayList<>();

        mTodoRecyclerView = findViewById(R.id.todo_view_recycler);

        initTodoRecyclerView();
    }
    void initTodoRecyclerView(){
        mTodoRecyclerView.invalidate();
        mTodoRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mTodoRecyclerView.setLayoutManager(linearLayoutManager);
        todoAdapter = new TodoAdapter();
        mTodoRecyclerView.setAdapter(todoAdapter);

        initTodos();
    }
    void initTodos(){
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    if (dataSnapshot.hasChildren()) {
                        ArrayList<TodoPOJO> todoPOJOArrayList = new ArrayList<>();
                        TodoInfoPOJO todoInfoPOJO = dataSnapshot.child("info").getValue(TodoInfoPOJO.class);
                        for (DataSnapshot snapshot : dataSnapshot.child("todo_items").getChildren()) {
                            TodoPOJO todoPOJO = snapshot.getValue(TodoPOJO.class);
                            todoPOJOArrayList.add(todoPOJO);
                        }
                        Todo todo = new Todo(todoInfoPOJO.getTodoKey(), todoInfoPOJO.getDate(), todoInfoPOJO, todoPOJOArrayList);
                        todoArrayList.add(todo);
                        sortDateAscending();
                        todoAdapter.notifyItemInserted(todoPOJOArrayList.size() - 1);
                    }
                } catch (DatabaseException d) {
                }catch (Exception e){

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
        databaseReference.addChildEventListener(childEventListener);

    }
    void sortDateAscending(){
        Collections.sort(todoArrayList, new Comparator<Todo>() {
            DateFormat f =new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            @Override
            public int compare(Todo o1, Todo o2) {
                try {
                    return f.parse(o1.getDate()).compareTo(f.parse(o2.getDate()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (childEventListener != null) {
            databaseReference.removeEventListener(childEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (childEventListener != null) {
            databaseReference.removeEventListener(childEventListener);
        }
    }

    void toLoginActivity() {
        Intent intent = new Intent(TodoViewActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
        private LayoutInflater inflator;

        public TodoAdapter() {
            try {
                this.inflator = LayoutInflater.from(TodoViewActivity.this);
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_todo_row_layout, parent, false);
            return new TodoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
          try {
              Todo todo = todoArrayList.get(position);
              TodoInfoPOJO todoInfoPOJO = todo.getTodoInfoPOJO();

              holder.mTitle.setText(todoInfoPOJO.getTitle());
              holder.mDate.setText(todoInfoPOJO.getDate());
          }catch (Exception e){

          }
        }

        @Override
        public int getItemCount() {
            return todoArrayList.size();
        }

        public class TodoViewHolder extends RecyclerView.ViewHolder{
            public TextView mTitle, mDate;
            public TodoViewHolder(View itemView) {
                super(itemView);

                mTitle = itemView.findViewById(R.id.todo_title_view);
                mDate = itemView.findViewById(R.id.todo_date_view);



            }
        }
    }

}
