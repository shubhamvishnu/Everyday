package com.everyday.skara.everyday.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.Todo;
import com.everyday.skara.everyday.pojo.ActivityPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.TodoInfoPOJO;
import com.everyday.skara.everyday.pojo.TodoPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tapadoo.alerter.Alerter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

public class TodoFragment extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, todoDatabaseReference, todoInfoReference;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    ChildEventListener childEventListener;
    ArrayList<Todo> todoArrayList;

    // View elements
    RecyclerView mTodoRecyclerView;

    // Adapter
    TodoAdapter todoAdapter;

    // TodoView dialog
    BottomSheetDialog mTodoItemsDialog;
    RecyclerView mTodoItemsRecyclerView;
    TodoItemAdapter todoItemAdapter;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_todo_layout, container, false);
        if (getActivity() != null) {
            if (user != null) {
                init();
            } else {
                toLoginActivity();
            }
        }
        return view;
    }

    void init() {

        //Intent intent = getActivity().getIntent();
        boardPOJO = (BoardPOJO) getArguments().getSerializable("board_pojo");
        userInfoPOJO = (UserInfoPOJO) getArguments().getSerializable("user_profile");
        firebaseDatabase = FirebaseDatabase.getInstance();
        todoArrayList = new ArrayList<>();
        todoDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/");
        todoDatabaseReference.keepSynced(true);
        mTodoRecyclerView = view.findViewById(R.id.todo_view_recycler);

        initTodoRecyclerView();
    }

    void initTodoRecyclerView() {
        mTodoRecyclerView.invalidate();
        mTodoRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mTodoRecyclerView.setLayoutManager(linearLayoutManager);
        todoAdapter = new TodoAdapter();
        mTodoRecyclerView.setAdapter(todoAdapter);

        initTodos();
    }

    void initTodos() {
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/");
        databaseReference.keepSynced(true);
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
                } catch (Exception e) {

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

    void sortDateAscending() {
        Collections.sort(todoArrayList, new Comparator<Todo>() {
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

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
    public void onStop() {
        super.onStop();
        if (childEventListener != null) {
            databaseReference.removeEventListener(childEventListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (childEventListener != null) {
            databaseReference.removeEventListener(childEventListener);
        }
    }

    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public class TodoItemAdapter extends RecyclerView.Adapter<TodoItemAdapter.TodoItemViewHolder> {
        private LayoutInflater inflator;
        int todoPosition;

        public TodoItemAdapter(int todoPosition) {
            try {
                this.inflator = LayoutInflater.from(getActivity());
                this.todoPosition = todoPosition;
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public TodoItemAdapter.TodoItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_item_row_layout, parent, false);
            return new TodoItemAdapter.TodoItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TodoItemViewHolder holder, int position) {
            holder.mCheckbox.setChecked(todoArrayList.get(todoPosition).getTodoPOJOArrayList().get(position).isState());
            holder.mItem.setText(todoArrayList.get(todoPosition).getTodoPOJOArrayList().get(position).getItem());
        }

        @Override
        public int getItemCount() {
            return todoArrayList.get(todoPosition).getTodoPOJOArrayList().size();
        }


        public class TodoItemViewHolder extends RecyclerView.ViewHolder {
            public EditText mItem;
            public ImageButton mDelete;
            public CheckBox mCheckbox;

            public TodoItemViewHolder(View itemView) {
                super(itemView);
                mItem = itemView.findViewById(R.id.todo_item_view);
                mDelete = itemView.findViewById(R.id.dialog_delete_item_view);
                mCheckbox = itemView.findViewById(R.id.todo_checkbox_view);
                mDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteItem(todoPosition, getPosition());
                    }
                });

                mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setState(todoPosition, getPosition(), isChecked);
                    }
                });
            }
        }

    }

    void setState(final int todoPosition, final int position, final boolean isChecked) {
        final DatabaseReference todoReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/" + todoArrayList.get(todoPosition).getTodoKey() + "/todo_items/" + todoArrayList.get(todoPosition).getTodoPOJOArrayList().get(position).getItemKey() + "/");
        HashMap<String, Object> stateMap = new HashMap<>();
        stateMap.put("state", isChecked);
        todoReference.updateChildren(stateMap);
        todoArrayList.get(todoPosition).getTodoPOJOArrayList().get(position).setState(isChecked);

    }

    void deleteItem(final int todoPosition, final int position) {
        final DatabaseReference todoReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/" + todoArrayList.get(todoPosition).getTodoKey());
        todoDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/" + todoArrayList.get(todoPosition).getTodoKey() + "/todo_items/");
        todoDatabaseReference.child(todoArrayList.get(todoPosition).getTodoPOJOArrayList().get(position).getItemKey()).removeValue();
        try {
            todoArrayList.get(todoPosition).getTodoPOJOArrayList().remove(position);
            todoItemAdapter.notifyDataSetChanged();

            if (todoArrayList.get(todoPosition).getTodoPOJOArrayList().size() == 0) {
                todoReference.removeValue();
                todoAdapter.notifyItemRemoved(todoPosition);
                mTodoItemsDialog.dismiss();
            }
        } catch (IndexOutOfBoundsException e) {

        }
    }

    void showInternetAlerter() {
        Alerter.create(getActivity())
                .setText("Oops! no internet connection...")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Connectivity.openInternetSettings(getActivity());
                    }
                })
                .setBackgroundColorRes(R.color.colorAccent)
                .show();
    }

    public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
        private LayoutInflater inflator;

        public TodoAdapter() {
            try {
                this.inflator = LayoutInflater.from(getActivity());
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public TodoAdapter.TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_todo_row_layout, parent, false);
            return new TodoAdapter.TodoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TodoAdapter.TodoViewHolder holder, int position) {
            try {
                Todo todo = todoArrayList.get(position);
                TodoInfoPOJO todoInfoPOJO = todo.getTodoInfoPOJO();

                holder.mTitle.setText(todoInfoPOJO.getTitle());
                holder.mDate.setText(todoInfoPOJO.getDate());

            } catch (Exception e) {

            }
        }


        void showItems(final int position) {
            todoDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/" + todoArrayList.get(position).getTodoKey());
            todoInfoReference = todoDatabaseReference.child("info");

            Button mCloseButton, mAddButton;
            final EditText mItemEditText;
            mTodoItemsDialog = new BottomSheetDialog(getActivity());
            mTodoItemsDialog.setContentView(R.layout.dialog_todo_items_layout);

            mItemEditText = mTodoItemsDialog.findViewById(R.id.dialog_todo_new_item_edittext);
            mAddButton = mTodoItemsDialog.findViewById(R.id.dialog_todo_add_new_item_button);
            mCloseButton = mTodoItemsDialog.findViewById(R.id.close_todo_item_dialog);

            mTodoItemsRecyclerView = mTodoItemsDialog.findViewById(R.id.recyclerview_todo_view_items);
            mTodoItemsRecyclerView.invalidate();
            mTodoItemsRecyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            mTodoItemsRecyclerView.setLayoutManager(linearLayoutManager);
            todoItemAdapter = new TodoItemAdapter(position);
            mTodoItemsRecyclerView.setAdapter(todoItemAdapter);

            mCloseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTodoItemsDialog.dismiss();
                }
            });
            mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String item = mItemEditText.getText().toString();
                        if (!item.isEmpty()) {
                            DatabaseReference todoReference = todoDatabaseReference.child("todo_items").push();
                            todoReference.keepSynced(true);
                            //String title, String date, String todoKey, String lastModified
                            todoArrayList.get(position).getTodoInfoPOJO().setLastModified(DateTimeStamp.getDate());
                            todoInfoReference.setValue(todoArrayList.get(position).getTodoInfoPOJO());
                            final TodoPOJO todoPOJO = new TodoPOJO(item, todoReference.getKey(), false, DateTimeStamp.getDate(), todoDatabaseReference.getKey(), userInfoPOJO);

                            todoReference.setValue(todoPOJO);
                            ActivityPOJO activityPOJO = new ActivityPOJO("New item added on " + boardPOJO.getDate() + "by" + userInfoPOJO.getName(), boardPOJO.getDate(), userInfoPOJO);
                            firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey()).child("activity").push().setValue(activityPOJO);
                            todoArrayList.get(position).getTodoPOJOArrayList().add(todoPOJO);
                            todoItemAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(), "Item added", Toast.LENGTH_SHORT).show();
                        }
                }
            });
            mTodoItemsDialog.setCanceledOnTouchOutside(true);
            mTodoItemsDialog.show();

        }

        @Override
        public int getItemCount() {
            return todoArrayList.size();
        }

        public class TodoViewHolder extends RecyclerView.ViewHolder {
            public TextView mTitle, mDate;
            public Button mMore;

            public TodoViewHolder(View itemView) {
                super(itemView);

                mTitle = itemView.findViewById(R.id.todo_title_view);
                mDate = itemView.findViewById(R.id.todo_date_view);

                mMore = itemView.findViewById(R.id.todo_more);
                mMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showItems(getPosition());
                    }
                });


            }
        }
    }

}
