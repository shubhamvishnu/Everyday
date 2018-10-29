package com.everyday.skara.everyday.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LongDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.MonthDates;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HabitsFragment extends android.support.v4.app.Fragment {
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
    BottomSheetDialog mShowHabitDialog;
    String mDate;
    int mDay, mMonth, mYear;
    TextView mEndDateTextView;
    Calendar datePickerCalender = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener datePicker;

    RecyclerView mDurationRecyclerview;

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
                mDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                mEndDateTextView.setText(mDate);

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

        void showHabitDialog(int position) {
            final HabitPOJO habitPOJO1 = mHabitsPojoArrayList.get(position);

            ImageButton mClose;
            final TextView mTitle, mDescription;
            final TextView mEndDate, mCreatedDate;

            mShowHabitDialog = new BottomSheetDialog(getActivity());
            mShowHabitDialog.setContentView(R.layout.dialog_show_habit_entry_dialog);
            mTitle = mShowHabitDialog.findViewById(R.id.title_show_habit_dialog);
            mDescription = mShowHabitDialog.findViewById(R.id.desc_show_habit_dialog);
            mCreatedDate = mShowHabitDialog.findViewById(R.id.created_date_habit_dialog);
            mEndDate = mShowHabitDialog.findViewById(R.id.end_date_habit_dialog);
            mClose = mShowHabitDialog.findViewById(R.id.close_show_habit_dialog);
            mDurationRecyclerview = mShowHabitDialog.findViewById(R.id.duration_snapshot_recyclerview);

            mTitle.setText(habitPOJO1.getTitle());
            mDescription.setText(habitPOJO1.getDescription());
            mCreatedDate.setText(habitPOJO1.getDate());
            mEndDate.setText(habitPOJO1.getmDate());


            mClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mShowHabitDialog.dismiss();
                }
            });


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = null;
            try {
                date = sdf.parse(habitPOJO1.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            Calendar endCalender = new GregorianCalendar();
            endCalender.set(Calendar.DAY_OF_MONTH, habitPOJO1.getmDay());
            endCalender.set(Calendar.MONTH, habitPOJO1.getmMonth());
            endCalender.set(Calendar.YEAR, habitPOJO1.getmYear());

            mDurationRecyclerview.invalidate();
            mDurationRecyclerview.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            mDurationRecyclerview.setLayoutManager(linearLayoutManager);
            HabitDurationAdapter habitDurationAdapter = new HabitDurationAdapter(getDaysBetweenDates(cal, endCalender));
            mDurationRecyclerview.setAdapter(habitDurationAdapter);

            mShowHabitDialog.setCanceledOnTouchOutside(false);
            mShowHabitDialog.show();
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
            final Button mEndDate;
            Button mDone;

            mTitle = mEditEntryDialog.findViewById(R.id.title_habit);
            mDescription = mEditEntryDialog.findViewById(R.id.desc_habit);
            mEndDateTextView = mEditEntryDialog.findViewById(R.id.habit_end_date_textview);
            mEndDate = mEditEntryDialog.findViewById(R.id.habit_end_date_button);
            mClose = mEditEntryDialog.findViewById(R.id.close_habit_entry_dialog);
            mDone = mEditEntryDialog.findViewById(R.id.done_habit_entry_button);

            mDate = habitPOJO1.getDate();

            mTitle.setText(habitPOJO1.getTitle());
            mDescription.setText(habitPOJO1.getDescription());

            mEndDateTextView.setText(habitPOJO1.getDate());


            mEndDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                    if (!(title.isEmpty() || mDate.isEmpty() || mDate.equals(""))) {
                        Map<String, Object> habitMap = new HashMap<>();
                        HabitPOJO habitPOJO2 = new HabitPOJO(habitPOJO1.getHabitEntryKey(), title, desc, mDate, habitPOJO1.getmTime(), mDay, mMonth, mYear, habitPOJO1.getmHours(), habitPOJO1.getmMinutes(), NotificationTypes.INTERVAL_ONCE, DateTimeStamp.getDate(), userInfoPOJO);
                        habitMap.put(habitPOJO1.getHabitEntryKey(), habitPOJO2);
                        firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_HABITS + "/habits/").updateChildren(habitMap);
                        mHabitsPojoArrayList.set(position, habitPOJO2);
                        mEditEntryDialog.dismiss();
                        mEntriesAdapter.notifyItemChanged(position);

                        // TODO: update reminder
                    } else {
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

                mTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showHabitDialog(getPosition());
                    }
                });
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


    public static HashMap<Integer, MonthDates> getDaysBetweenDates(Calendar startdate, Calendar enddate) {
        HashMap<Integer, MonthDates> monthDatesHashMap = new HashMap<>();

        while (startdate.before(enddate)) {
            int key = startdate.get(Calendar.MONTH) + startdate.get(Calendar.YEAR);
            if (monthDatesHashMap.containsKey(key)) {
                Date result = startdate.getTime();
                monthDatesHashMap.get(key).getDates().add(result);
                startdate.add(Calendar.DATE, 1);
            } else {
                Date result = startdate.getTime();
                ArrayList<Date> dateList = new ArrayList<>();
                dateList.add(result);
                MonthDates monthDates = new MonthDates(key, startdate.get(Calendar.YEAR), startdate.get(Calendar.MONTH), dateList);
                monthDatesHashMap.put(key, monthDates);
                startdate.add(Calendar.DATE, 1);
            }
        }
        return monthDatesHashMap;

    }

    public static void printMap(HashMap<Integer, MonthDates> mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            MonthDates monthDates = (MonthDates) pair.getValue();
            Log.d("month_dates_hash_map", "key: - " + String.valueOf(pair.getKey()));
            Log.d("month_dates_hash_map", "value: Year - " + monthDates.getYear());
            Log.d("month_dates_hash_map", "value: month - " + monthDates.getMonth());
            for (int i = 0; i < monthDates.getDates().size(); i++) {
                Log.d("month_dates_hash_map", "----" + monthDates.getDates().get(i));
            }
            Log.d("month_dates_hash_map", "==========================================");

            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public static ArrayList<Integer> getKeysList(HashMap<Integer, MonthDates> mp) {
        ArrayList<Integer> keysArrayList = new ArrayList<>();
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            keysArrayList.add((int) pair.getKey());
        }
        return keysArrayList;
    }

    /*------------------------------------------------------------------------------ */


    /**
     * Adapter for Duration Recyclerview
     */

    public class HabitDurationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;
        HashMap<Integer, MonthDates> datesHashMap;
        ArrayList<Integer> keysArrayList;

        public HabitDurationAdapter(HashMap<Integer, MonthDates> datesHashMap) {
            try {
                this.inflator = LayoutInflater.from(getActivity());
                this.datesHashMap = datesHashMap;
                keysArrayList = getKeysList(datesHashMap);
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_duration_snapshot_layout, parent, false);
            return new HabitDurationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MonthDates monthDates = datesHashMap.get(keysArrayList.get(position));
            ((HabitDurationViewHolder) holder).mMonthTitle.setText("" +monthDates.getMonth());


            RecyclerView recyclerView = ((HabitDurationViewHolder) holder).mHabitDatesDurationRecyclerview;
            recyclerView.invalidate();
            recyclerView.setHasFixedSize(true);
            //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 10));
            HabitDurationDatesAdapter habitDurationAdapter = new HabitDurationDatesAdapter(monthDates);
            recyclerView.setAdapter(habitDurationAdapter);


        }

        @Override
        public int getItemCount() {
            return datesHashMap.size();
        }


        public class HabitDurationViewHolder extends RecyclerView.ViewHolder {
            public TextView mMonthTitle;
            public RecyclerView mHabitDatesDurationRecyclerview;
            public HabitDurationViewHolder(View itemView) {
                super(itemView);
                mMonthTitle = itemView.findViewById(R.id.month_title_textview);
                mHabitDatesDurationRecyclerview = itemView.findViewById(R.id.habits_duration_dates_recyclerview);

            }
        }

    }

    /*------------------------------------------------------------------------------ */

    /**
     * Adapter for Month dates Recyclerview
     */
    public class HabitDurationDatesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;
        MonthDates monthDates;

        public HabitDurationDatesAdapter(MonthDates monthDates) {
            try {
                this.inflator = LayoutInflater.from(getActivity());
                this.monthDates = monthDates;
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_month_dates_snapshot_row_layout, parent, false);
            return new HabitDurationDatesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(monthDates.getDates().get(position));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
            String format = formatter.format(calendar.getTime());
            ((HabitDurationDatesViewHolder) holder).mDateValue.setText(format);
        }

        @Override
        public int getItemCount() {
            return monthDates.getDates().size();
        }


        public class HabitDurationDatesViewHolder extends RecyclerView.ViewHolder {
            public TextView mDateValue;

            public HabitDurationDatesViewHolder(View itemView) {
                super(itemView);

                mDateValue = itemView.findViewById(R.id.date_value_textview);

            }
        }

    }


    /*------------------------------------------------------------------------------ */

    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
