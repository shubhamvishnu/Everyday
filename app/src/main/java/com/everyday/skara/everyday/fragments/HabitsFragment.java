package com.everyday.skara.everyday.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.PersonalHabitActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.NotificationTypes;
import com.everyday.skara.everyday.pojo.HabitPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HabitsFragment extends android.support.v4.app.Fragment{
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    View view;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mEntriesDatabaseReference;
    ChildEventListener mEntriesChildEventListener;
    UserInfoPOJO userInfoPOJO;
    RecyclerView mEntriesRecyclerView;
    HabitsAdapter mEntriesAdapter;
    ArrayList<HabitPOJO> mHabitsPojoArrayList;

    BottomSheetDialog mEditEntryDialog;
    boolean isStartSelected = true;
    String mStartDateValue, mEndDateValue;
    int mStartDay, mStartMonth, mStartYear;
    int mEndDay, mEndMonth, mEndYear;
    TextView mStartDateTextView, mEndDateTextView;
    Calendar datePickerCalender = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener datePicker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_habits_entries_layout, container, false);
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
        //Intent intent = getActivity().getIntent();
        userInfoPOJO = (UserInfoPOJO) getArguments().getSerializable("user_profile");
        mEntriesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_HABITS + "/habits/");
        mEntriesDatabaseReference.keepSynced(true);

        mEntriesRecyclerView = view.findViewById(R.id.habits_entries_recyclerview);
        mHabitsPojoArrayList = new ArrayList<>();
        datePicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                Calendar cal = new java.util.GregorianCalendar();
                datePickerCalender.set(Calendar.YEAR, year);
                datePickerCalender.set(Calendar.MONTH, monthOfYear);
                datePickerCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                //   new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date())
                if (isStartSelected) {
                    mStartDateValue = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
                    if (!(mStartDateValue.isEmpty() || mStartDateValue.equals(""))) {
                        mStartYear = year;
                        mStartMonth = monthOfYear;
                        mStartDay = dayOfMonth;
                        mStartDateTextView.setText(mStartDateValue);
                    }
                } else {
                    mEndDateValue = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
                    if (!(mEndDateValue.isEmpty() || mEndDateValue.equals(""))) {
                        mEndYear = year;
                        mEndMonth = monthOfYear;
                        mEndDay = dayOfMonth;
                        mEndDateTextView.setText(mEndDateValue);
                    }
                }
            }

        };
        initEntriesRecyclerView();
    }

    void initEntriesRecyclerView() {
        mEntriesRecyclerView.invalidate();
        mEntriesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mEntriesRecyclerView.setLayoutManager(linearLayoutManager);
        mEntriesAdapter = new HabitsAdapter();
        mEntriesRecyclerView.setAdapter(mEntriesAdapter);

        initEntries();
    }

    void initEntries() {
        mHabitsPojoArrayList = new ArrayList<>();
        mEntriesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                HabitPOJO habitPOJO = dataSnapshot.getValue(HabitPOJO.class);
                mHabitsPojoArrayList.add(habitPOJO);
                mEntriesAdapter.notifyItemInserted(mHabitsPojoArrayList.size() - 1);
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
        mEntriesDatabaseReference.addChildEventListener(mEntriesChildEventListener);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mEntriesChildEventListener != null) {
            mEntriesDatabaseReference.removeEventListener(mEntriesChildEventListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEntriesChildEventListener != null) {
            mEntriesDatabaseReference.removeEventListener(mEntriesChildEventListener);
        }
    }

    public class HabitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;

        public HabitsAdapter() {
            try {
                this.inflator = LayoutInflater.from(getActivity());
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_habits_entries_row_layout, parent, false);
            return new HabitsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            HabitPOJO habitPOJO = mHabitsPojoArrayList.get(position);
            ((HabitsViewHolder) holder).date.setText(habitPOJO.getDate());
            ((HabitsViewHolder) holder).mTitle.setText(habitPOJO.getTitle());
            ((HabitsViewHolder) holder).mDesc.setText(habitPOJO.getDescription());
        }

        @Override
        public int getItemCount() {
            return mHabitsPojoArrayList.size();
        }

        void deleteEntry(int position) {
            firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_HABITS + "/habits/").child(mHabitsPojoArrayList.get(position).getHabitEntryKey()).removeValue();
            mHabitsPojoArrayList.remove(position);
            notifyItemRemoved(position);
        }

        void showEditDialog(final int position) {
            final HabitPOJO habitPOJO1 = mHabitsPojoArrayList.get(position);
            mEditEntryDialog = new BottomSheetDialog(getActivity());
            mEditEntryDialog.setContentView(R.layout.dialog_edit_habit_entry_layout);
            ImageButton mClose;
            final EditText mTitle, mDescription;
            final Button mStartDate, mEndDate;
            final CheckBox mForeverCheckbox;
            Button mDone;

            mTitle = mEditEntryDialog.findViewById(R.id.title_habit);
            mDescription = mEditEntryDialog.findViewById(R.id.desc_habit);
            mStartDateTextView = mEditEntryDialog.findViewById(R.id.habit_start_date_textview);
            mEndDateTextView = mEditEntryDialog.findViewById(R.id.habit_end_date_textview);
            mStartDate = mEditEntryDialog.findViewById(R.id.habit_start_date_button);
            mEndDate = mEditEntryDialog.findViewById(R.id.habit_end_date_button);
            mForeverCheckbox = mEditEntryDialog.findViewById(R.id.habit_forever_checkbox);
            mClose = mEditEntryDialog.findViewById(R.id.close_habit_entry_dialog);
            mDone = mEditEntryDialog.findViewById(R.id.done_habit_entry_button);

            mStartDateValue = habitPOJO1.getStartDate();
            mEndDateValue = habitPOJO1.getEndDate();

            mTitle.setText(habitPOJO1.getTitle());
            mDescription.setText(habitPOJO1.getDescription());
            mForeverCheckbox.setChecked(habitPOJO1.isForever());
            mStartDateTextView.setText(habitPOJO1.getStartDate());
            mEndDateTextView.setText(habitPOJO1.getEndDate());

            mStartDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isStartSelected = true;
                    new DatePickerDialog(getActivity(), datePicker, datePickerCalender
                            .get(Calendar.YEAR), datePickerCalender.get(Calendar.MONTH),
                            datePickerCalender.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
            mEndDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isStartSelected = false;
                    new DatePickerDialog(getActivity(), datePicker, datePickerCalender
                            .get(Calendar.YEAR), datePickerCalender.get(Calendar.MONTH),
                            datePickerCalender.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            mClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditEntryDialog.dismiss();
                }
            });
            mDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = mTitle.getText().toString().trim();
                    String desc = mDescription.getText().toString().trim();
                    if (desc.isEmpty()) {
                        desc = "";
                    }
                    if (!(title.isEmpty() || mStartDateValue.isEmpty() || mStartDateValue.equals(""))) {
                        if (mForeverCheckbox.isChecked()) {
                            if (mEndDateValue.isEmpty() || mEndDateValue.equals("")) {
                                mEndDateValue = "";
                                mEndDay = 0;
                                mEndMonth = 0;
                                mEndYear = 0;
                            }
                            mHabitsPojoArrayList.set(position, habitPOJO1);

                            Map<String, Object> habitMap = new HashMap<>();
                            HabitPOJO habitPOJO2 = new HabitPOJO(habitPOJO1.getHabitEntryKey(), title, desc, mStartDateValue, mEndDateValue, mForeverCheckbox.isSelected(), mStartDay, mStartMonth, mStartYear, mEndDay, mEndMonth, mEndYear, NotificationTypes.INTERVAL_ONCE, DateTimeStamp.getDate(), userInfoPOJO);
                            habitMap.put(habitPOJO1.getHabitEntryKey(), habitPOJO2);
                            firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_HABITS + "/habits/").updateChildren(habitMap);

                            mEntriesAdapter.notifyItemChanged(position);
                        } else {
                            Toast.makeText(getActivity(), "End Date Missing", Toast.LENGTH_SHORT).show();
                        }

                        mEditEntryDialog.dismiss();
                    } else

                    {
                        Toast.makeText(getActivity(), "Cannot be blank", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mEditEntryDialog.setCanceledOnTouchOutside(false);
            mEditEntryDialog.show();
        }


        public class HabitsViewHolder extends RecyclerView.ViewHolder {
            public TextView date, mTitle, mDesc;
            public ImageButton edit;
            public ImageButton delete;


            public HabitsViewHolder(View itemView) {
                super(itemView);

                date = itemView.findViewById(R.id.habit_entry_view_date);
                mDesc = itemView.findViewById(R.id.desc_textview_recyclerview);
                mTitle = itemView.findViewById(R.id.title_habit_recyclerview);

                edit = itemView.findViewById(R.id.habit_edit_recyclerview);
                delete = itemView.findViewById(R.id.delete_habit_entry_button);

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDialog(getPosition());
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteEntry(getPosition());
                    }
                });

            }
        }

    }

    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
