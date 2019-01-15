package com.everyday.skara.everyday.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.MonthDates;
import com.everyday.skara.everyday.classes.NotificationTypes;
import com.everyday.skara.everyday.pojo.HabitCheckedPOJO;
import com.everyday.skara.everyday.pojo.HabitPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class HabitsFragment extends android.support.v4.app.Fragment implements com.philliphsu.bottomsheetpickers.date.DatePickerDialog.OnDateSetListener{
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    View view;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mEntriesDatabaseReference;
    ChildEventListener mEntriesChildEventListener;
    UserInfoPOJO userInfoPOJO;
    RecyclerView mEntriesRecyclerView;
    HabitsAdapter mEntriesAdapter;
    ArrayList<HabitPOJO> mHabitsPojoArrayList;
    HashMap<String, ArrayList<HabitCheckedPOJO>> mHabitCheckedPOJOHashMap;
    BottomSheetDialog mEditEntryDialog;
    BottomSheetDialog mShowHabitDialog;
    String mDate;
    int mDay, mMonth, mYear;
    TextView mEndDateTextView;
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
        mHabitCheckedPOJOHashMap = new HashMap<>();

        initEntriesRecyclerView();
    }

    void initEntriesRecyclerView() {
        mEntriesRecyclerView.invalidate();
        mEntriesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mEntriesRecyclerView.setLayoutManager(linearLayoutManager);
        mEntriesAdapter = new HabitsAdapter();
        mEntriesRecyclerView.setAdapter(mEntriesAdapter);

        initCheckedHabits();
    }

    void initCheckedHabits() {
        mHabitCheckedPOJOHashMap = new HashMap<>();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_HABITS + "/habitsChecked");
        databaseReference.keepSynced(true);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            HabitCheckedPOJO habitCheckedPOJO = childSnapshot.getValue(HabitCheckedPOJO.class);
                            if (mHabitCheckedPOJOHashMap.containsKey(snapshot.getKey())) {
                                ArrayList<HabitCheckedPOJO> habitCheckedPOJOList = mHabitCheckedPOJOHashMap.get(snapshot.getKey());
                                habitCheckedPOJOList.add(habitCheckedPOJO);
                                mHabitCheckedPOJOHashMap.put(snapshot.getKey(), habitCheckedPOJOList);
                            } else {
                                ArrayList<HabitCheckedPOJO> habitCheckedPOJOList = new ArrayList<>();
                                habitCheckedPOJOList.add(habitCheckedPOJO);
                                mHabitCheckedPOJOHashMap.put(snapshot.getKey(), habitCheckedPOJOList);
                            }
                        }
                    }

                }
                initEntries();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

            ((HabitsViewHolder) holder).date.setText(habitPOJO.getDate());
            ((HabitsViewHolder) holder).mTitle.setText(habitPOJO.getTitle());
            ((HabitsViewHolder) holder).mDesc.setText(habitPOJO.getDescription());

            // created date calender


            // end date calender
            Calendar endCalender = new GregorianCalendar();
            endCalender.set(Calendar.DAY_OF_MONTH, habitPOJO.getmDay());
            endCalender.set(Calendar.MONTH, habitPOJO.getmMonth());
            endCalender.set(Calendar.YEAR, habitPOJO.getmYear());

            // today's date calender
            Calendar todaysCalendar = new GregorianCalendar();


            if (!(todaysCalendar.before(endCalender) || todaysCalendar.equals(endCalender))) {
                ((HabitsViewHolder) holder).mDoneCheckbox.setEnabled(false);
            }
            if (mHabitCheckedPOJOHashMap.containsKey(habitPOJO.getHabitEntryKey())) {
                ArrayList<HabitCheckedPOJO> tempHabitCheckedPOJOArrayList = mHabitCheckedPOJOHashMap.get(habitPOJO.getHabitEntryKey());

                Calendar calendar = new GregorianCalendar();
                String todaysDateCheck = formatter.format(calendar.getTime());

                for (int i = 0; i < tempHabitCheckedPOJOArrayList.size(); i++) {
                    if (tempHabitCheckedPOJOArrayList.get(i).getDate().equals(todaysDateCheck)) {
                        ((HabitsViewHolder) holder).mDoneCheckbox.setChecked(tempHabitCheckedPOJOArrayList.get(i).isState());
                    }
                }
            }


            // created date Calendar
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            try {
                date = sdf.parse(habitPOJO.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar createdCalender = Calendar.getInstance();
            createdCalender.setTime(date);
            Log.d("createdTimeForHabit", formatter.format(createdCalender.getTime()));

            Calendar prev1 = Calendar.getInstance();
            prev1.add(Calendar.DATE, -1);
            ((HabitsViewHolder) holder).mPrev1.setText(formatter.format(prev1.getTime()));

            Calendar prev2 = Calendar.getInstance();
            prev2.add(Calendar.DATE, -2);
            ((HabitsViewHolder) holder).mPrev2.setText(formatter.format(prev2.getTime()));


            Calendar prev3 = Calendar.getInstance();
            prev3.add(Calendar.DATE, -3);
            ((HabitsViewHolder) holder).mPrev3.setText(formatter.format(prev3.getTime()));


            Calendar prev4 = Calendar.getInstance();
            prev4.add(Calendar.DATE, -4);
            ((HabitsViewHolder) holder).mPrev4.setText(formatter.format(prev4.getTime()));


            Calendar prev5 = Calendar.getInstance();
            prev5.add(Calendar.DATE, -5);
            ((HabitsViewHolder) holder).mPrev5.setText(formatter.format(prev5.getTime()));


            Calendar prev6 = Calendar.getInstance();
            prev6.add(Calendar.DATE, -6);
            ((HabitsViewHolder) holder).mPrev6.setText(formatter.format(prev6.getTime()));

            // Log.d("previous1", formatter.format(prev1.getTime()) + "-After or equal-" + formatter.format(createdCalender.getTime()));


            if (prev1.after(createdCalender) || prev1.equals(createdCalender)) {
                ((HabitsViewHolder) holder).mPrev1CheckBox.setEnabled(true);
                updateStateForCheckedBox(holder, habitPOJO, formatter.format(prev1.getTime()), 1);
            } else {
                ((HabitsViewHolder) holder).mPrev1CheckBox.setEnabled(false);
            }

            if (prev2.after(createdCalender) || prev2.equals(createdCalender)) {
                ((HabitsViewHolder) holder).mPrev2CheckBox.setEnabled(true);
                updateStateForCheckedBox(holder, habitPOJO, formatter.format(prev2.getTime()), 2);

            } else {
                ((HabitsViewHolder) holder).mPrev2CheckBox.setEnabled(false);
            }

            if (prev3.after(createdCalender) || prev3.equals(createdCalender)) {
                ((HabitsViewHolder) holder).mPrev3CheckBox.setEnabled(true);
                updateStateForCheckedBox(holder, habitPOJO, formatter.format(prev3.getTime()), 3);
            } else {
                ((HabitsViewHolder) holder).mPrev3CheckBox.setEnabled(false);
            }

            if (prev4.after(createdCalender) || prev4.equals(createdCalender)) {
                ((HabitsViewHolder) holder).mPrev4CheckBox.setEnabled(true);
                updateStateForCheckedBox(holder, habitPOJO, formatter.format(prev4.getTime()), 4);
            } else {
                ((HabitsViewHolder) holder).mPrev4CheckBox.setEnabled(false);
            }

            if (prev5.after(createdCalender) || prev5.equals(createdCalender)) {
                ((HabitsViewHolder) holder).mPrev5CheckBox.setEnabled(true);
                updateStateForCheckedBox(holder, habitPOJO, formatter.format(prev5.getTime()), 5);
            } else {
                ((HabitsViewHolder) holder).mPrev5CheckBox.setEnabled(false);
            }

            if (prev6.after(createdCalender) || prev6.equals(createdCalender)) {
                ((HabitsViewHolder) holder).mPrev6CheckBox.setEnabled(true);
                updateStateForCheckedBox(holder, habitPOJO, formatter.format(prev6.getTime()), 6);
            } else {
                ((HabitsViewHolder) holder).mPrev6CheckBox.setEnabled(false);
            }
        }

        void updateStateForCheckedBox(RecyclerView.ViewHolder holder, HabitPOJO habitPOJO, String date, int prevCount) {
            if (mHabitCheckedPOJOHashMap.containsKey(habitPOJO.getHabitEntryKey())) {
                ArrayList<HabitCheckedPOJO> tempHabitCheckedPOJOArrayList = mHabitCheckedPOJOHashMap.get(habitPOJO.getHabitEntryKey());

                for (int i = 0; i < tempHabitCheckedPOJOArrayList.size(); i++) {
                    if (tempHabitCheckedPOJOArrayList.get(i).getDate().equals(date)) {
                        switch (prevCount) {
                            case 1:
                                ((HabitsViewHolder) holder).mPrev1CheckBox.setChecked(tempHabitCheckedPOJOArrayList.get(i).isState());
                                break;
                            case 2:
                                ((HabitsViewHolder) holder).mPrev2CheckBox.setChecked(tempHabitCheckedPOJOArrayList.get(i).isState());
                                break;
                            case 3:
                                ((HabitsViewHolder) holder).mPrev3CheckBox.setChecked(tempHabitCheckedPOJOArrayList.get(i).isState());
                                break;
                            case 4:
                                ((HabitsViewHolder) holder).mPrev4CheckBox.setChecked(tempHabitCheckedPOJOArrayList.get(i).isState());
                                break;
                            case 5:
                                ((HabitsViewHolder) holder).mPrev5CheckBox.setChecked(tempHabitCheckedPOJOArrayList.get(i).isState());
                                break;
                            case 6:
                                ((HabitsViewHolder) holder).mPrev6CheckBox.setChecked(tempHabitCheckedPOJOArrayList.get(i).isState());
                                break;
                        }
                    }
                }
            }
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
                    mDate = new String("");
                    mDay = 0;
                    mMonth = 0;
                    mYear = 0;

                    DialogFragment dialog = createDialog();
                    dialog.show(getActivity().getSupportFragmentManager(), "date");
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
                        HabitPOJO habitPOJO2 = new HabitPOJO(habitPOJO1.getHabitEntryKey(), title, desc, mDate, habitPOJO1.getmTime(), mDay, mMonth, mYear, NotificationTypes.INTERVAL_ONCE, DateTimeStamp.getDate(), userInfoPOJO);
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

        void updateHabitCheck(boolean state, int position) {
            HabitPOJO habitPOJO1 = mHabitsPojoArrayList.get(position);
            boolean updated = false;

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_HABITS + "/habitsChecked").child(habitPOJO1.getHabitEntryKey());
            databaseReference.keepSynced(true);


            // check if the checked array list has the habit
            if (mHabitCheckedPOJOHashMap.containsKey(habitPOJO1.getHabitEntryKey())) {

                // get the list of all the checked dates for that habit
                ArrayList<HabitCheckedPOJO> habitCheckedPOJOArrayList = mHabitCheckedPOJOHashMap.get(habitPOJO1.getHabitEntryKey());

                // check if the ticked date is available in the list
                for (int i = 0; i < habitCheckedPOJOArrayList.size(); i++) {

                    HabitCheckedPOJO habitCheckedPOJO = habitCheckedPOJOArrayList.get(i);

                    Calendar calendar = new GregorianCalendar();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String todaysDateCheck = formatter.format(calendar.getTime());

                    if (habitCheckedPOJO.getDate().equals(todaysDateCheck)) {
                        habitCheckedPOJO.setState(state);
                        Map<String, Object> checkMap = new HashMap<>();
                        checkMap.put(habitCheckedPOJO.getDateCheckedKey(), habitCheckedPOJO);
                        databaseReference.updateChildren(checkMap);
                        updated = true;
                        break;
                    }

                }
                if (!updated) {
                    DatabaseReference checkedReference = databaseReference.push();
                    Calendar calendar = new GregorianCalendar();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String format = formatter.format(calendar.getTime());
                    checkedReference.setValue(new HabitCheckedPOJO(checkedReference.getKey(), format, state));
                    ArrayList<HabitCheckedPOJO> habitCheckedPOJOArrayList1 = new ArrayList<>();
                    habitCheckedPOJOArrayList1.add(new HabitCheckedPOJO(checkedReference.getKey(), format, state));
                    mHabitCheckedPOJOHashMap.put(habitPOJO1.getHabitEntryKey(), habitCheckedPOJOArrayList1);
                }
            } else {
                DatabaseReference checkedReference = databaseReference.push();
                Calendar calendar = new GregorianCalendar();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String format = formatter.format(calendar.getTime());
                checkedReference.setValue(new HabitCheckedPOJO(checkedReference.getKey(), format, state));
                ArrayList<HabitCheckedPOJO> habitCheckedPOJOArrayList1 = new ArrayList<>();
                habitCheckedPOJOArrayList1.add(new HabitCheckedPOJO(checkedReference.getKey(), format, state));
                mHabitCheckedPOJOHashMap.put(habitPOJO1.getHabitEntryKey(), habitCheckedPOJOArrayList1);

            }


        }

        void updatePrevCheck(int position, boolean state, int prevCount) {
            HabitPOJO habitPOJO = mHabitsPojoArrayList.get(position);
            switch (prevCount) {
                case -1:
                    updatePrevHabitCheck(habitPOJO, state, prevCount);
                    break;
                case -2:
                    updatePrevHabitCheck(habitPOJO, state, prevCount);
                    break;
                case -3:
                    updatePrevHabitCheck(habitPOJO, state, prevCount);
                    break;
                case -4:
                    updatePrevHabitCheck(habitPOJO, state, prevCount);
                    break;
                case -5:
                    updatePrevHabitCheck(habitPOJO, state, prevCount);
                    break;
                case -6:
                    updatePrevHabitCheck(habitPOJO, state, prevCount);
                    break;

            }
        }

        void updatePrevHabitCheck(HabitPOJO habitPOJO1, boolean state, int prevCount) {
            boolean updated = false;

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_HABITS + "/habitsChecked").child(habitPOJO1.getHabitEntryKey());
            databaseReference.keepSynced(true);


            // check if the checked array list has the habit
            if (mHabitCheckedPOJOHashMap.containsKey(habitPOJO1.getHabitEntryKey())) {

                // get the list of all the checked dates for that habit
                ArrayList<HabitCheckedPOJO> habitCheckedPOJOArrayList = mHabitCheckedPOJOHashMap.get(habitPOJO1.getHabitEntryKey());

                // check if the ticked date is available in the list
                for (int i = 0; i < habitCheckedPOJOArrayList.size(); i++) {

                    HabitCheckedPOJO habitCheckedPOJO = habitCheckedPOJOArrayList.get(i);

                    Calendar calendar = new GregorianCalendar();
                    calendar.add(Calendar.DATE, prevCount);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String todaysDateCheck = formatter.format(calendar.getTime());

                    if (habitCheckedPOJO.getDate().equals(todaysDateCheck)) {
                        habitCheckedPOJO.setState(state);
                        Map<String, Object> checkMap = new HashMap<>();
                        checkMap.put(habitCheckedPOJO.getDateCheckedKey(), habitCheckedPOJO);
                        databaseReference.updateChildren(checkMap);
                        updated = true;
                        break;
                    }

                }
                if (!updated) {
                    DatabaseReference checkedReference = databaseReference.push();
                    Calendar calendar = new GregorianCalendar();
                    calendar.add(Calendar.DATE, prevCount);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String format = formatter.format(calendar.getTime());
                    checkedReference.setValue(new HabitCheckedPOJO(checkedReference.getKey(), format, state));
                    ArrayList<HabitCheckedPOJO> habitCheckedPOJOArrayList1 = new ArrayList<>();
                    habitCheckedPOJOArrayList1.add(new HabitCheckedPOJO(checkedReference.getKey(), format, state));
                    mHabitCheckedPOJOHashMap.put(habitPOJO1.getHabitEntryKey(), habitCheckedPOJOArrayList1);
                }
            } else {
                DatabaseReference checkedReference = databaseReference.push();
                Calendar calendar = new GregorianCalendar();
                calendar.add(Calendar.DATE, prevCount);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String format = formatter.format(calendar.getTime());
                checkedReference.setValue(new HabitCheckedPOJO(checkedReference.getKey(), format, state));
                ArrayList<HabitCheckedPOJO> habitCheckedPOJOArrayList1 = new ArrayList<>();
                habitCheckedPOJOArrayList1.add(new HabitCheckedPOJO(checkedReference.getKey(), format, state));
                mHabitCheckedPOJOHashMap.put(habitPOJO1.getHabitEntryKey(), habitCheckedPOJOArrayList1);

            }
        }

        public class HabitsViewHolder extends RecyclerView.ViewHolder {
            public TextView date, mTitle, mDesc;
            public ImageButton edit;
            public ImageButton delete;
            public TextView mPrev1, mPrev2, mPrev3, mPrev4, mPrev5, mPrev6;
            public CheckBox mPrev1CheckBox, mPrev2CheckBox, mPrev3CheckBox, mPrev4CheckBox, mPrev5CheckBox, mPrev6CheckBox;
            public CheckBox mDoneCheckbox;


            public HabitsViewHolder(View itemView) {
                super(itemView);

                date = itemView.findViewById(R.id.habit_entry_view_date);
                mDesc = itemView.findViewById(R.id.desc_textview_recyclerview);
                mTitle = itemView.findViewById(R.id.title_habit_recyclerview);
                mDoneCheckbox = itemView.findViewById(R.id.done_today_checkbox);

                mPrev1 = itemView.findViewById(R.id.prev_date_1_textview);
                mPrev2 = itemView.findViewById(R.id.prev_date_2_textview);
                mPrev3 = itemView.findViewById(R.id.prev_date_3_textview);
                mPrev4 = itemView.findViewById(R.id.prev_date_4_textview);
                mPrev5 = itemView.findViewById(R.id.prev_date_5_textview);
                mPrev6 = itemView.findViewById(R.id.prev_date_6_textview);

                mPrev1CheckBox = itemView.findViewById(R.id.prev_date_1_checkbox);
                mPrev2CheckBox = itemView.findViewById(R.id.prev_date_2_checkbox);
                mPrev3CheckBox = itemView.findViewById(R.id.prev_date_3_checkbox);
                mPrev4CheckBox = itemView.findViewById(R.id.prev_date_4_checkbox);
                mPrev5CheckBox = itemView.findViewById(R.id.prev_date_5_checkbox);
                mPrev6CheckBox = itemView.findViewById(R.id.prev_date_6_checkbox);


                mPrev1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        updatePrevCheck(getPosition(), b, -1);
                    }
                });
                mPrev2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        updatePrevCheck(getPosition(), b, -2);
                    }
                });
                mPrev3CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        updatePrevCheck(getPosition(), b, -3);

                    }
                });
                mPrev4CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        updatePrevCheck(getPosition(), b, -4);

                    }
                });
                mPrev5CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        updatePrevCheck(getPosition(), b, -5);

                    }
                });
                mPrev6CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        updatePrevCheck(getPosition(), b, -6);
                    }
                });

                edit = itemView.findViewById(R.id.habit_edit_recyclerview);
                delete = itemView.findViewById(R.id.delete_habit_entry_button);

                mDoneCheckbox.setText(DateTimeStamp.getDate());

                mDoneCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            updateHabitCheck(true, getPosition());
                        } else {
                            updateHabitCheck(false, getPosition());
                        }
                    }
                });
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
            ((HabitDurationViewHolder) holder).mMonthTitle.setText("" + monthDates.getMonth());


            RecyclerView recyclerView = ((HabitDurationViewHolder) holder).mHabitDatesDurationRecyclerview;
            recyclerView.invalidate();
            recyclerView.setHasFixedSize(true);
            //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 15));
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
                mMonthTitle = itemView.findViewById(R.id.month_title_button);
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
            SimpleDateFormat formatter = new SimpleDateFormat("dd");
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

                mDateValue = itemView.findViewById(R.id.date_value_button);

            }
        }

    }

    /*------------------------------------------------------------------------------ */

    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    /**
     * Bottom sheet picker for date and time
     * [STARTS HERE]
     */

    @Override
    public void onDateSet(com.philliphsu.bottomsheetpickers.date.DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        //   new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date())
        mDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
        this.mYear = year;
        this.mMonth = monthOfYear;
        this.mDay = dayOfMonth;
        mEndDateTextView.setText(mDate);

    }

    private DialogFragment createDialog() {
        return createDialogWithSetters();
    }

    private DialogFragment createDialogWithSetters() {
        BottomSheetPickerDialog dialog = null;
        boolean themeDark = true;
        Calendar refCal = new GregorianCalendar();

        Calendar now = Calendar.getInstance();
        dialog = com.philliphsu.bottomsheetpickers.date.DatePickerDialog.newInstance(
                this,
                now.get(refCal.YEAR),
                now.get(refCal.MONTH),
                now.get(refCal.DAY_OF_MONTH));

        com.philliphsu.bottomsheetpickers.date.DatePickerDialog dateDialog = (com.philliphsu.bottomsheetpickers.date.DatePickerDialog) dialog;
        dateDialog.setYearRange(refCal.YEAR, 2050);
        dateDialog.setMinDate(refCal);
        dialog.setThemeDark(themeDark);

        return dialog;
    }

    //[ENDS HERE]
}
