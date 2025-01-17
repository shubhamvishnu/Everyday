package com.everyday.skara.everyday.fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.TodoActivity;
import com.everyday.skara.everyday.classes.ActionType;
import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.TimeDateStamp;
import com.everyday.skara.everyday.classes.Todo;
import com.everyday.skara.everyday.pojo.ActivityPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.TodoInfoPOJO;
import com.everyday.skara.everyday.pojo.TodoPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.everyday.skara.everyday.receivers.TodoReminderReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;
import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.grid.GridTimePickerDialog;
import com.tapadoo.alerter.Alerter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class TodoFragment extends Fragment implements BottomSheetTimePickerDialog.OnTimeSetListener, com.philliphsu.bottomsheetpickers.date.DatePickerDialog.OnDateSetListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, todoDatabaseReference, todoInfoReference;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    ChildEventListener childEventListener;
    ArrayList<Todo> todoArrayList = new ArrayList<>();

    // View elements
    RecyclerView mTodoRecyclerView;

    // Adapter
    TodoAdapter todoAdapter;

    // TodoView dialog
    BottomSheetDialog mTodoItemsDialog;
    RecyclerView mTodoItemsRecyclerView;
    TodoItemAdapter todoItemAdapter;
    View view;
    ImageButton mFilterButton;
    BottomSheetDialog mEditTodoDialog;
    public static boolean clicked = false;
    LinearLayout mEmptyLinearLayout, mFragmentLinearLayout;
    String date, time;
    int day, month, year;
    int hours, minutes;
    TextView mReminder;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_todo_layout, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            if (user != null) {
                init();
            } else {
                toLoginActivity();
            }
        }
    }

    void init() {
        todoArrayList = new ArrayList<>();
        mFilterButton = getActivity().findViewById(R.id.filter_option_button);

        //Intent intent = getActivity().getIntent();
        boardPOJO = (BoardPOJO) getArguments().getSerializable("board_pojo");
        userInfoPOJO = (UserInfoPOJO) getArguments().getSerializable("user_profile");
        firebaseDatabase = FirebaseDatabase.getInstance();


        todoDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/");
        todoDatabaseReference.keepSynced(true);
        mTodoRecyclerView = view.findViewById(R.id.todo_view_recycler);
        mEmptyLinearLayout = (LinearLayout) getActivity().findViewById(R.id.board_no_todos_linear_layout);
        mEmptyLinearLayout.setVisibility(View.INVISIBLE);

        mFragmentLinearLayout = (LinearLayout) getActivity().findViewById(R.id.linear_layout_todo_fragment);

        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = !clicked;
                if (clicked) {
                    mFilterButton.setRotation(180);
                    sortDateAscending();
                    todoAdapter.notifyDataSetChanged();
                } else {
                    mFilterButton.setRotation(0);
                    sortDateDescending();
                    todoAdapter.notifyDataSetChanged();
                }
            }
        });
        initTodoRecyclerView();
    }

    void setEmptyVisibility(int action) {
        switch (action) {
            case 0:
                mFragmentLinearLayout.setVisibility(LinearLayout.INVISIBLE);
                mEmptyLinearLayout.setVisibility(LinearLayout.VISIBLE);
                break;
            case 1:
                mFragmentLinearLayout.setVisibility(LinearLayout.VISIBLE);
                mEmptyLinearLayout.setVisibility(LinearLayout.INVISIBLE);
                break;
        }
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
        todoArrayList = new ArrayList<>();
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/");
        databaseReference.keepSynced(true);
        childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    if (dataSnapshot.hasChildren()) {
                        setEmptyVisibility(1);
                        ArrayList<TodoPOJO> todoPOJOArrayList = new ArrayList<>();
                        TodoInfoPOJO todoInfoPOJO = dataSnapshot.child("info").getValue(TodoInfoPOJO.class);
                        for (DataSnapshot snapshot : dataSnapshot.child("todo_items").getChildren()) {
                            TodoPOJO todoPOJO = snapshot.getValue(TodoPOJO.class);
                            todoPOJOArrayList.add(todoPOJO);
                        }
                        Todo todo = new Todo(todoInfoPOJO.getTodoKey(), todoInfoPOJO.getDate(), todoInfoPOJO, todoPOJOArrayList);
                        todoArrayList.add(todo);
                        sortDateDescending();
                        todoAdapter.notifyItemInserted(todoPOJOArrayList.size() - 1);

                    } else {
                        setEmptyVisibility(0);
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

    void sortDateDescending() {
        Collections.sort(todoArrayList, new Comparator<Todo>() {
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            @Override
            public int compare(Todo o1, Todo o2) {
                try {
                    return f.parse(o2.getDate()).compareTo(f.parse(o1.getDate()));
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
            View view = inflator.inflate(R.layout.recyclerview_todo_item_row_layout, parent, false);
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
            public TextView mItem;
            public ImageButton mDelete;
            public CheckBox mCheckbox;

            public TodoItemViewHolder(View itemView) {
                super(itemView);
                mCheckbox = itemView.findViewById(R.id.todo_checkbox);
                mItem = itemView.findViewById(R.id.todo_item);
                mDelete = itemView.findViewById(R.id.delete_item);
                mDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteItem(todoPosition, getPosition());
                    }
                });

                mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (mCheckbox.isPressed()) {
                            setState(todoPosition, getPosition(), isChecked);
                        }
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
        todoAdapter.notifyItemChanged(todoPosition);

    }

    void deleteItem(final int todoPosition, final int position) {
        final DatabaseReference todoReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/" + todoArrayList.get(todoPosition).getTodoKey());
        todoDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/" + todoArrayList.get(todoPosition).getTodoKey() + "/todo_items/");
        try {
            todoDatabaseReference.child(todoArrayList.get(todoPosition).getTodoPOJOArrayList().get(position).getItemKey()).removeValue();
            try {
                todoArrayList.get(todoPosition).getTodoPOJOArrayList().remove(position);
                todoItemAdapter.notifyDataSetChanged();

                if (todoArrayList.get(todoPosition).getTodoPOJOArrayList().size() == 0) {
                    todoReference.removeValue();
                    mTodoItemsDialog.dismiss();
                    init();
                }
            } catch (IndexOutOfBoundsException e) {

            }
        } catch (Exception e) {

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
                int counter = 0;
                Todo todo = todoArrayList.get(position);
                if (todo.getTodoPOJOArrayList().size() > 0) {
                    for (int i = 0; i < todo.getTodoPOJOArrayList().size(); i++) {
                        if (todo.getTodoPOJOArrayList().get(i).isState()) {
                            ++counter;
                        }
                    }
                    String stat = counter + "/" + (todo.getTodoPOJOArrayList().size());
                    holder.mStats.setText(stat);
                } else {
                    holder.mStats.setText("0/0");
                }


                TodoInfoPOJO todoInfoPOJO = todo.getTodoInfoPOJO();

                holder.mTitle.setText(todoInfoPOJO.getTitle());
                holder.mDate.setText(todoInfoPOJO.getDate());

            } catch (Exception e) {

            }
        }


        void showItems(final int position) {
            todoDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/" + todoArrayList.get(position).getTodoKey());
            todoInfoReference = todoDatabaseReference.child("info");

            ImageButton mCloseButton, mAddButton, mCalendar;


            final EditText mItemEditText;
            mTodoItemsDialog = new BottomSheetDialog(getActivity());
            mTodoItemsDialog.setContentView(R.layout.dialog_todo_items_layout);

            mItemEditText = mTodoItemsDialog.findViewById(R.id.dialog_todo_new_item_edittext);
            mAddButton = mTodoItemsDialog.findViewById(R.id.dialog_todo_add_new_item_button);
            mCloseButton = mTodoItemsDialog.findViewById(R.id.close_todo_item_dialog);
            mCalendar = mTodoItemsDialog.findViewById(R.id.todo_set_reminder);
            mReminder = mTodoItemsDialog.findViewById(R.id.todo_set_reminder_textview);

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
                        todoArrayList.get(position).getTodoPOJOArrayList().add(todoPOJO);
                        todoItemAdapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), "Item added", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.support.v4.app.DialogFragment dialog = createDialog();
                    dialog.show(getActivity().getSupportFragmentManager(), "date");
                }
            });
            date = new String("");
            time = new String("");

            mTodoItemsDialog.setCanceledOnTouchOutside(true);
            mTodoItemsDialog.show();

        }

        void showEditTodoDialog(final int position) {
            final EditText mTitle;
            ImageButton mClose;
            Button mDone;

            Todo todo = todoArrayList.get(position);
            final TodoInfoPOJO todoInfoPOJO = todo.getTodoInfoPOJO();

            mEditTodoDialog = new BottomSheetDialog(getActivity());
            mEditTodoDialog.setContentView(R.layout.dialog_edit_todo_layout);

            mTitle = mEditTodoDialog.findViewById(R.id.title_edit_todo_dialog);
            mClose = mEditTodoDialog.findViewById(R.id.close_todo_edit_dialog);
            mDone = mEditTodoDialog.findViewById(R.id.done_edit_todo_dialog);


            mTitle.setText(todoInfoPOJO.getTitle());

            mClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditTodoDialog.dismiss();
                }
            });
            mDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    todoInfoPOJO.setTitle(mTitle.getText().toString());
                    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/" + todoInfoPOJO.getTodoKey() + "/info/");
                    databaseReference.setValue(todoInfoPOJO);
                    todoArrayList.get(position).setTodoInfoPOJO(todoInfoPOJO);
                    notifyItemChanged(position);
                    mEditTodoDialog.dismiss();
                }
            });

            mEditTodoDialog.setCanceledOnTouchOutside(false);
            mEditTodoDialog.show();
        }

        @Override
        public int getItemCount() {
            if (todoArrayList.size() <= 0) {
                setEmptyVisibility(0);
            } else {
                setEmptyVisibility(1);
            }
            return todoArrayList.size();
        }

        public class TodoViewHolder extends RecyclerView.ViewHolder {
            public TextView mTitle, mDate;
            public ImageButton mMore, mEdit, mDelete;
            public TextView mStats;

            public TodoViewHolder(View itemView) {
                super(itemView);

                mTitle = itemView.findViewById(R.id.todo_title_view);
                mDate = itemView.findViewById(R.id.todo_date_view);
                mEdit = itemView.findViewById(R.id.edit_todo_button);
                mDelete = itemView.findViewById(R.id.todo_delete_button);
                mStats = itemView.findViewById(R.id.todo_item_stats_textview);

                mMore = itemView.findViewById(R.id.todo_more);
                mTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showItems(getPosition());
                    }
                });
                mStats.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showItems(getPosition());
                    }
                });
                mMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showItems(getPosition());
                    }
                });

                mEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditTodoDialog(getPosition());
                    }
                });

                mDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeleteTodoAlert(getPosition());
                    }
                });


            }
        }
    }

    void deleteTodo(int position) {
        Todo todo = todoArrayList.get(position);
        firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/todos/" + todo.getTodoInfoPOJO().getTodoKey()).removeValue();
        todoArrayList.remove(position);
        todoAdapter.notifyItemRemoved(position);
    }

    void showDeleteTodoAlert(final int position) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        // set title
        alertDialogBuilder.setTitle("Delete");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you want to delete?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteTodo(position);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    /**
     * Bottom sheet picker for date and time
     * [STARTS HERE]
     */
    @Override
    public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        time = android.text.format.DateFormat.getTimeFormat(getActivity()).format(cal.getTime());
        if (!(time.isEmpty() || time.equals(""))) {

            hours = hourOfDay;
            minutes = minute;

            if (mReminder != null)
                mReminder.setText("Reminder set at " + time + " on " + date);
            //setReminder();
        }
    }

    void setReminder() {
        AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(TodoActivity.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);

        Intent intent = new Intent(getActivity(), TodoReminderReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);

// With setInexactRepeating(), you have to use one of the AlarmManager interval
// constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    @Override
    public void onDateSet(com.philliphsu.bottomsheetpickers.date.DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        date = android.text.format.DateFormat.getDateFormat(getActivity()).format(cal.getTime());

        if (!(date.isEmpty() || date.equals(""))) {
            this.year = year;
            this.month = monthOfYear;
            this.day = dayOfMonth;

            // calling the time dialog
            android.support.v4.app.DialogFragment dialog1 = createTimeDialog();
            dialog1.show(getActivity().getSupportFragmentManager(), "time");
        }
    }


    private android.support.v4.app.DialogFragment createDialog() {
        return createDialogWithSetters();
    }

    private android.support.v4.app.DialogFragment createTimeDialog() {
        return createTimeDialogWithSetters();
    }

    private android.support.v4.app.DialogFragment createTimeDialogWithSetters() {
        BottomSheetPickerDialog dialog = null;
        boolean custom = false;
        boolean customDark = false;
        boolean themeDark = true;

        Calendar now = Calendar.getInstance();
        dialog = GridTimePickerDialog.newInstance(
                TodoFragment.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                android.text.format.DateFormat.is24HourFormat(getActivity()));
        GridTimePickerDialog gridDialog = (GridTimePickerDialog) dialog;
        dialog.setThemeDark(themeDark);

        return dialog;
    }

    private android.support.v4.app.DialogFragment createDialogWithSetters() {
        BottomSheetPickerDialog dialog = null;
        boolean themeDark = true;

        Calendar now = Calendar.getInstance();
        dialog = com.philliphsu.bottomsheetpickers.date.DatePickerDialog.newInstance(
                TodoFragment.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));

        com.philliphsu.bottomsheetpickers.date.DatePickerDialog dateDialog = (com.philliphsu.bottomsheetpickers.date.DatePickerDialog) dialog;
        Calendar minCalendar = TimeDateStamp.getCalendar("dd/MM/yyyy", "29/07/2018");
        Calendar maxCalendar = TimeDateStamp.getCalendar("dd/MM/yyyy", "29/07/2025");
        dateDialog.setMinDate(minCalendar);
        dateDialog.setMaxDate(maxCalendar);
        dateDialog.setYearRange(1920, 2050);
        dialog.setThemeDark(themeDark);

        return dialog;
    }
    //[ENDS HERE]

}
