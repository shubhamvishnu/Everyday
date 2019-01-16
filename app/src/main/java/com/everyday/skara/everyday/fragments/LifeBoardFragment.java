package com.everyday.skara.everyday.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.DonutProgress;
import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.BasicSettings;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.FinanceEntryPOJO;
import com.everyday.skara.everyday.pojo.LifeBoardPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class LifeBoardFragment extends android.support.v4.app.Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    View view;
    UserInfoPOJO userInfoPOJO;
    DatabaseReference mEntriesDatabaseReference;
    ValueEventListener mEntriesValueEventListener;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    RecyclerView mDatesRecyclerView;
    HashMap<Integer, ArrayList<LifeBoardPOJO>> lifeBoardPOJOMap;
    HabitDurationAdapter datesAdapter;
    RatingBar mRatingBar;
    ImageButton mDayThumbsUp, mDayThumsbDown;
    TextView mTodayDateTextView, mTodayDayTextView;
    DonutProgress mDonutProgress;
    TextView mDaysElapsed, mUseful, mWaste;
    HabitDurationDatesAdapter habitDurationAdapter;
    static String[] suffixes =
            //    0     1     2     3     4     5     6     7     8     9
            {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                    //    10    11    12    13    14    15    16    17    18    19
                    "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                    //    20    21    22    23    24    25    26    27    28    29
                    "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                    //    30    31
                    "th", "st"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_lifeboard_entiries_layout, container, false);
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

        mDatesRecyclerView = view.findViewById(R.id.recyclerview_date_textview);
        mRatingBar = view.findViewById(R.id.rating_bar);
        mDayThumbsUp = view.findViewById(R.id.day_thumbs_up);
        mDayThumsbDown = view.findViewById(R.id.day_thumbs_down);
        mTodayDateTextView = view.findViewById(R.id.today_date_textview);
        mTodayDayTextView = view.findViewById(R.id.today_day_textview);

        mDaysElapsed = view.findViewById(R.id.days_elapsed);
        mUseful = view.findViewById(R.id.useful_days);
        mWaste = view.findViewById(R.id.wasted_days);

        mDaysElapsed.setText("--");
        mUseful.setText("--");
        mWaste.setText("--");

        mDonutProgress = view.findViewById(R.id.life_donut);
        mDonutProgress.setProgress(0.0f);

        Calendar todayCalendar = Calendar.getInstance();
        Log.d("sdklcjsdklcj", userInfoPOJO.getYear() + "");
        int difference = todayCalendar.get(Calendar.YEAR) - userInfoPOJO.getYear();
        mDonutProgress.setText("" + difference);
        mDonutProgress.setProgress(difference);


        SimpleDateFormat formatter = new SimpleDateFormat("dd");
        SimpleDateFormat formatter1 = new SimpleDateFormat("E");
        String format = formatter.format(todayCalendar.getTime());
        String format1 = formatter1.format(todayCalendar.getTime());
        mTodayDateTextView.setText(format + "" + suffixes[Integer.parseInt(format)]);
        mTodayDayTextView.setText(format1);


        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Log.d("ratingbar", rating + "boolean" + fromUser);
            }
        });

        mDayThumbsUp.setColorFilter(getResources().getColor(R.color.grey));
        mDayThumsbDown.setColorFilter(getResources().getColor(R.color.grey));

        mDayThumbsUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDayThumbsUp.setColorFilter(getResources().getColor(R.color.green_));
                mDayThumsbDown.setColorFilter(getResources().getColor(R.color.grey));
                mDayThumbsUp.setBackgroundColor(getResources().getColor(R.color.transparent));
                mDayThumsbDown.setBackgroundColor(getResources().getColor(R.color.transparent));

                updateToday(1);
            }
        });
        mDayThumsbDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDayThumsbDown.setColorFilter(getResources().getColor(R.color.red));
                mDayThumbsUp.setColorFilter(getResources().getColor(R.color.grey));
                mDayThumbsUp.setBackgroundColor(getResources().getColor(R.color.transparent));
                mDayThumsbDown.setBackgroundColor(getResources().getColor(R.color.transparent));
                updateToday(0);
            }
        });
        initDateChecks();

    }

    void updateToday(final int choice) {

        //String key, String dateUniqueId, String day, String month, String year, String date, String rating, int choice
        final Calendar todayCalendar = Calendar.getInstance();
        final Integer key = Integer.parseInt(String.valueOf(todayCalendar.get(Calendar.YEAR)) + String.valueOf(todayCalendar.get(Calendar.MONTH)));

        final DatabaseReference todaysReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_LIFE + "/lifeboard/");
        todaysReference.keepSynced(true);
        todaysReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean notAvailable = true;
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        LifeBoardPOJO lifeBoardPOJO = snapshot.getValue(LifeBoardPOJO.class);
                        if ((lifeBoardPOJO.getYear() == todayCalendar.get(Calendar.YEAR)) && (lifeBoardPOJO.getMonth() == todayCalendar.get(Calendar.MONTH)) && (lifeBoardPOJO.getDay() == todayCalendar.get(Calendar.DAY_OF_MONTH))) {
                            lifeBoardPOJO.setChoice(choice);
                            Map<String, Object> map = new HashMap<>();
                            map.put(lifeBoardPOJO.getKey(), lifeBoardPOJO);
                            todaysReference.updateChildren(map);
                            notAvailable = false;
                        }
                    }
                    if (notAvailable) {
                        DatabaseReference pushForTodayReference = todaysReference.push();
                        LifeBoardPOJO lifeBoardPOJO1 = new LifeBoardPOJO(pushForTodayReference.getKey(), key, todayCalendar.get(Calendar.DAY_OF_MONTH), todayCalendar.get(Calendar.MONTH), todayCalendar.get(Calendar.YEAR), todayCalendar.getTime().toString(), "null", choice, userInfoPOJO);
                        pushForTodayReference.setValue(lifeBoardPOJO1);
                        Log.d("dateipdatedvalue", lifeBoardPOJO1.getDate());
                    }
                } else {
                    DatabaseReference pushForTodayReference = todaysReference.push();
                    LifeBoardPOJO lifeBoardPOJO1 = new LifeBoardPOJO(pushForTodayReference.getKey(), key, todayCalendar.get(Calendar.DAY_OF_MONTH), todayCalendar.get(Calendar.MONTH), todayCalendar.get(Calendar.YEAR), todayCalendar.getTime().toString(), "null", choice, userInfoPOJO);
                    pushForTodayReference.setValue(lifeBoardPOJO1);
                    Log.d("dateipdatedvalue", lifeBoardPOJO1.getDate());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    void initDateChecks() {
        final Calendar todayCalendar = Calendar.getInstance();

        lifeBoardPOJOMap = new HashMap<>();

        mEntriesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_LIFE + "/lifeboard/");
        mEntriesDatabaseReference.keepSynced(true);
        mEntriesValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int useFulCount = 0;
                int wastedCount = 0;
                if (dataSnapshot.hasChildren()) {
                    lifeBoardPOJOMap = new HashMap<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        LifeBoardPOJO lifeBoardPOJO = snapshot.getValue(LifeBoardPOJO.class);
                        if ((lifeBoardPOJO.getYear() == todayCalendar.get(Calendar.YEAR)) && (lifeBoardPOJO.getMonth() == todayCalendar.get(Calendar.MONTH)) && (lifeBoardPOJO.getDay() == todayCalendar.get(Calendar.DAY_OF_MONTH))) {
                            switch (lifeBoardPOJO.getChoice()) {
                                case 0:
                                    mDayThumsbDown.setColorFilter(getResources().getColor(R.color.red));
                                    mDayThumbsUp.setColorFilter(getResources().getColor(R.color.grey));
                                    mDayThumbsUp.setBackgroundColor(getResources().getColor(R.color.transparent));
                                    mDayThumsbDown.setBackgroundColor(getResources().getColor(R.color.transparent));
                                    ++wastedCount;
                                    break;
                                case 1:
                                    mDayThumbsUp.setColorFilter(getResources().getColor(R.color.green_));
                                    mDayThumsbDown.setColorFilter(getResources().getColor(R.color.grey));
                                    mDayThumbsUp.setBackgroundColor(getResources().getColor(R.color.transparent));
                                    mDayThumsbDown.setBackgroundColor(getResources().getColor(R.color.transparent));
                                    ++useFulCount;
                                    break;

                            }
                            if (lifeBoardPOJOMap.containsKey(lifeBoardPOJO.getDateUniqueId())) {
                                ArrayList<LifeBoardPOJO> lifeBoardPOJOS = lifeBoardPOJOMap.get(lifeBoardPOJO.getDateUniqueId());
                                lifeBoardPOJOS.add(lifeBoardPOJO);
                                lifeBoardPOJOMap.put(lifeBoardPOJO.getDateUniqueId(), lifeBoardPOJOS);
                            } else {
                                ArrayList<LifeBoardPOJO> lifeBoardPOJOS = new ArrayList<>();
                                lifeBoardPOJOS.add(lifeBoardPOJO);
                                lifeBoardPOJOS.add(lifeBoardPOJO);
                                lifeBoardPOJOMap.put(lifeBoardPOJO.getDateUniqueId(), lifeBoardPOJOS);
                            }
                        } else {
                            switch (lifeBoardPOJO.getChoice()) {
                                case 0:
                                    ++wastedCount;
                                    break;
                                case 1:
                                    ++useFulCount;
                                    break;

                            }

                            if (lifeBoardPOJOMap.containsKey(lifeBoardPOJO.getDateUniqueId())) {
                                ArrayList<LifeBoardPOJO> lifeBoardPOJOS = lifeBoardPOJOMap.get(lifeBoardPOJO.getDateUniqueId());
                                lifeBoardPOJOS.add(lifeBoardPOJO);
                                lifeBoardPOJOMap.put(lifeBoardPOJO.getDateUniqueId(), lifeBoardPOJOS);
                            } else {
                                ArrayList<LifeBoardPOJO> lifeBoardPOJOS = new ArrayList<>();
                                lifeBoardPOJOS.add(lifeBoardPOJO);
                                lifeBoardPOJOMap.put(lifeBoardPOJO.getDateUniqueId(), lifeBoardPOJOS);
                            }
                        }

                    }
                } else {
                    lifeBoardPOJOMap = new HashMap<>();
                }
                mUseful.setText(useFulCount + "");
                mWaste.setText(wastedCount + "");
                initDatesAdapter();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mEntriesDatabaseReference.addValueEventListener(mEntriesValueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mEntriesValueEventListener != null) {
            mEntriesDatabaseReference.removeEventListener(mEntriesValueEventListener);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEntriesValueEventListener != null) {
            mEntriesDatabaseReference.removeEventListener(mEntriesValueEventListener);
        }
    }


    void initDatesAdapter() {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.YEAR, userInfoPOJO.getYear());
        startCalendar.set(Calendar.MONTH, userInfoPOJO.getMonth());
        startCalendar.set(Calendar.DAY_OF_MONTH, userInfoPOJO.getDay());
        Calendar endCalendar = Calendar.getInstance();


        HashMap<Integer, MonthDates> monthDatesHashMap = getDaysBetweenDates(startCalendar, endCalendar);
        Map<Integer, MonthDates> map = new TreeMap<>(monthDatesHashMap).descendingMap();
        int size = getSize(map);
        mDaysElapsed.setText("" + size);


        mDatesRecyclerView.invalidate();
        mDatesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mDatesRecyclerView.setLayoutManager(linearLayoutManager);
        datesAdapter = new HabitDurationAdapter(monthDatesHashMap);
        mDatesRecyclerView.setAdapter(datesAdapter);


    }

    int getSize(Map<Integer, MonthDates> datesHashMap) {
        int size = 0;
        Iterator it = datesHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            MonthDates monthDates = (MonthDates) pair.getValue();
            for (int i = 0; i < monthDates.getDates().size(); i++) {
                ++size;
            }
        }
        return size;
    }

    public class DatesViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;
        Map<Integer, MonthDates> datesHashMap;
        ArrayList<Integer> keysArrayList;
        int size = 0;

        public DatesViewAdapter(HashMap<Integer, MonthDates> datesHashMap) {
            try {
                this.inflator = LayoutInflater.from(getActivity());
                Map<Integer, MonthDates> map = new TreeMap<>(datesHashMap).descendingMap();
                this.datesHashMap = map;
                keysArrayList = getKeysList(this.datesHashMap);
                size = getSize(this.datesHashMap);
            } catch (NullPointerException e) {

            }
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_dates_view_row_layout, parent, false);
            return new DatesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//            MonthDates monthDates = datesHashMap.get(keysArrayList.get(position));
            int pos = position + 1;
            ((DatesViewHolder) holder).mDate.setText("" + pos);

        }

        @Override
        public int getItemCount() {
            return size;
        }


        public class DatesViewHolder extends RecyclerView.ViewHolder {
            public TextView mDate;

            public DatesViewHolder(View itemView) {
                super(itemView);
                mDate = itemView.findViewById(R.id.date_count_textview);

            }
        }

    }

    public HashMap<Integer, MonthDates> getDaysBetweenDates(Calendar startdate, Calendar enddate) {
        HashMap<Integer, MonthDates> monthDatesHashMap = new HashMap<>();

        while (startdate.before(enddate) || startdate.equals(enddate)) {
            Integer key = Integer.parseInt(String.valueOf(startdate.get(Calendar.YEAR)) + String.valueOf(startdate.get(Calendar.MONTH)));
            if (monthDatesHashMap.containsKey(key)) {
                Date result = startdate.getTime();
                monthDatesHashMap.get(key).getDates().add(result);

            } else {
                Date result = startdate.getTime();
                ArrayList<Date> dateList = new ArrayList<>();
                dateList.add(result);
                MonthDates monthDates = new MonthDates(key, startdate.get(Calendar.YEAR), startdate.get(Calendar.MONTH), dateList);
                monthDatesHashMap.put(key, monthDates);

            }
            Log.d("DATESLOG", "MONTH: " + startdate.get(Calendar.MONTH) + " YEAR: " + startdate.get(Calendar.YEAR) + " key: " + key);
            Log.d("DATESLOG", "--------------------------");
            startdate.add(Calendar.DATE, 1);
        }
        return monthDatesHashMap;

    }

    public class HabitDurationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;
        Map<Integer, MonthDates> datesHashMap;
        ArrayList<Integer> keysArrayList;

        public HashMap<Integer, MonthDates> sortByValue(HashMap<Integer, MonthDates> hm) {
            // Create a list from elements of HashMap
            List<Map.Entry<Integer, MonthDates>> list =
                    new LinkedList<Map.Entry<Integer, MonthDates>>(hm.entrySet());

            // Sort the list
            Collections.sort(list, new Comparator<Map.Entry<Integer, MonthDates>>() {
                public int compare(Map.Entry<Integer, MonthDates> o1,
                                   Map.Entry<Integer, MonthDates> o2) {
                    return o1.getValue().sortKey.compareToIgnoreCase(o2.getValue().getSortKey());
                }
            });

            // put data from sorted list to hashmap
            HashMap<Integer, MonthDates> temp = new LinkedHashMap<Integer, MonthDates>();
            for (Map.Entry<Integer, MonthDates> aa : list) {
                temp.put(aa.getKey(), aa.getValue());
            }
            return temp;
        }

        public HabitDurationAdapter(HashMap<Integer, MonthDates> datesHashMap) {
            try {
                this.inflator = LayoutInflater.from(getActivity());
                //Map<Integer, MonthDates> map = new TreeMap<>(datesHashMap).descendingMap();
                //this.datesHashMap = map;
                this.datesHashMap = this.sortByValue(datesHashMap);
                keysArrayList = getKeysList(this.datesHashMap);
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

            SimpleDateFormat formatter = new SimpleDateFormat("MMM");

            Calendar currentCal = Calendar.getInstance();
            currentCal.set(Calendar.MONTH, monthDates.getMonth());
            String format = formatter.format(currentCal.getTime());
            ((HabitDurationViewHolder) holder).mYearTitle.setText("" + monthDates.getYear());
            ((HabitDurationViewHolder) holder).mMonthTitle.setText(format);


            RecyclerView recyclerView = ((HabitDurationViewHolder) holder).mHabitDatesDurationRecyclerview;
            recyclerView.invalidate();
            recyclerView.setHasFixedSize(true);
            //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 15);
            recyclerView.setLayoutManager(gridLayoutManager);
            gridLayoutManager.setReverseLayout(true);
            habitDurationAdapter = new HabitDurationDatesAdapter(monthDates, keysArrayList.get(position));
            recyclerView.setAdapter(habitDurationAdapter);


        }

        @Override
        public int getItemCount() {
            return datesHashMap.size();
        }


        public class HabitDurationViewHolder extends RecyclerView.ViewHolder {
            public Button mMonthTitle, mYearTitle;
            public RecyclerView mHabitDatesDurationRecyclerview;

            public HabitDurationViewHolder(View itemView) {
                super(itemView);
                mMonthTitle = itemView.findViewById(R.id.month_title_button);
                mYearTitle = itemView.findViewById(R.id.year_title_button);
                mHabitDatesDurationRecyclerview = itemView.findViewById(R.id.habits_duration_dates_recyclerview);
                mMonthTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "" + getPosition(), Toast.LENGTH_SHORT).show();
                    }
                });

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
        public int VIEW_TYPE_NO_INPUT = 1;
        public int VIEW_TYPE_YES = 2;
        public int VIEW_TYPE_NO = 3;

        int keys = 0;

        public HabitDurationDatesAdapter(MonthDates monthDates, int keys) {
            try {
                this.inflator = LayoutInflater.from(getActivity());
                this.monthDates = monthDates;
                this.keys = keys;
            } catch (NullPointerException e) {

            }
        }

        @Override
        public int getItemViewType(int position) {
            Date date = monthDates.getDates().get(position);
            if (lifeBoardPOJOMap.containsKey(this.keys)) {
                boolean isAvailable = false;
                ArrayList<LifeBoardPOJO> lifeBoardPOJOArrayList = lifeBoardPOJOMap.get(this.keys);
                for (int i = 0; i < lifeBoardPOJOArrayList.size(); i++) {
                    int year = lifeBoardPOJOArrayList.get(i).getYear();
                    int month = lifeBoardPOJOArrayList.get(i).getMonth();
                    int day = lifeBoardPOJOArrayList.get(i).getDay();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    if ((year == calendar.get(Calendar.YEAR)) && (month == calendar.get(Calendar.MONTH)) && (day == calendar.get(Calendar.DAY_OF_MONTH))) {
                        Toast.makeText(getActivity(), "" + year + ":" + month + ":" + day + "", Toast.LENGTH_SHORT).show();
                        isAvailable = true;
                        if (lifeBoardPOJOArrayList.get(i).getChoice() == 1) {
                            return VIEW_TYPE_YES;
                        } else if (lifeBoardPOJOArrayList.get(i).getChoice() == 0) {
                            return VIEW_TYPE_NO;
                        }
                    }
                }
                if (!isAvailable) {
                    return VIEW_TYPE_NO_INPUT;
                }
            } else {
                return VIEW_TYPE_NO_INPUT;
            }
            return super.getItemViewType(position);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_NO_INPUT) {
                View view = inflator.inflate(R.layout.recyclerview_month_dates_snapshot_row_layout, parent, false);
                return new HabitDurationDatesViewHolder(view);
            } else if (viewType == VIEW_TYPE_YES) {
                View view = inflator.inflate(R.layout.recyclerview_month_dates_snapshot_yes_row_layout, parent, false);
                return new HabitDurationDatesViewHolder(view);
            } else if (viewType == VIEW_TYPE_NO) {
                View view = inflator.inflate(R.layout.recyclerview_month_dates_snapshot_no_row_layout, parent, false);
                return new HabitDurationDatesViewHolder(view);
            } else {
                View view = inflator.inflate(R.layout.recyclerview_month_dates_snapshot_row_layout, parent, false);
                return new HabitDurationDatesViewHolder(view);
            }

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(monthDates.getDates().get(position));

//            if (lifeBoardPOJOMap.containsKey()) {
//                LifeBoardPOJO lifeBoardPOJO = lifeBoardPOJOMap.get(calendar.getTime().toString());
//                Log.d("OOOOOOO", lifeBoardPOJO.toString());
//                Log.d("OOOOOOO", "-------------------");
//
//            }
            SimpleDateFormat formatter = new SimpleDateFormat("dd");
            String format = formatter.format(calendar.getTime());
            ((HabitDurationDatesViewHolder) holder).mDateValue.setText(format);
        }

        @Override
        public int getItemCount() {
            return monthDates.getDates().size();
        }

        BottomSheetDialog mEditDateChoiceDialog;
        ImageButton dialogThumbsUp, dialogThumbsDown;
        Button mDoneDate;
        void updateDateDialog(final int position) {


            mEditDateChoiceDialog = new BottomSheetDialog(getActivity());
            int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
            if (theme == BasicSettings.LIGHT_THEME) {
                mEditDateChoiceDialog.setContentView(R.layout.dialog_update_date_choice_dialog_light);
            } else {
                mEditDateChoiceDialog.setContentView(R.layout.dialog_update_date_choice_dialog);
            }

            ImageButton mClose = mEditDateChoiceDialog.findViewById(R.id.close_edit_date_dialog);
            dialogThumbsUp = mEditDateChoiceDialog.findViewById(R.id.date_thumbs_up);
            dialogThumbsDown = mEditDateChoiceDialog.findViewById(R.id.date_thumbs_down);
            mDoneDate = mEditDateChoiceDialog.findViewById(R.id.done_date_update);

            dialogThumbsUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDayThumbsUp.setColorFilter(getResources().getColor(R.color.green_));
                    mDayThumsbDown.setColorFilter(getResources().getColor(R.color.grey));
                    mDayThumbsUp.setBackgroundColor(getResources().getColor(R.color.transparent));
                    mDayThumsbDown.setBackgroundColor(getResources().getColor(R.color.transparent));
                    updateDate(position, 1);
                }
            });
            dialogThumbsDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDayThumbsUp.setColorFilter(getResources().getColor(R.color.red));
                    mDayThumsbDown.setColorFilter(getResources().getColor(R.color.grey));
                    mDayThumbsUp.setBackgroundColor(getResources().getColor(R.color.transparent));
                    mDayThumsbDown.setBackgroundColor(getResources().getColor(R.color.transparent));
                    updateDate(position, 0);

                }
            });


            mClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditDateChoiceDialog.dismiss();
                }
            });

            mDoneDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditDateChoiceDialog.dismiss();
                }
            });


            mEditDateChoiceDialog.setCanceledOnTouchOutside(true);
            mEditDateChoiceDialog.show();
        }

        void updateDate(final int position, final int choice) {
            final DatabaseReference todaysReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_LIFE + "/lifeboard/");
            todaysReference.keepSynced(true);
            todaysReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(monthDates.getDates().get(position));
                    final Integer key = Integer.parseInt(String.valueOf(calendar.get(Calendar.YEAR)) + String.valueOf(calendar.get(Calendar.MONTH)));

                    boolean notAvailable = true;
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            LifeBoardPOJO lifeBoardPOJO = snapshot.getValue(LifeBoardPOJO.class);
                            if ((lifeBoardPOJO.getYear() == calendar.get(Calendar.YEAR)) && (lifeBoardPOJO.getMonth() == calendar.get(Calendar.MONTH)) && (lifeBoardPOJO.getDay() == calendar.get(Calendar.DAY_OF_MONTH))) {
                                lifeBoardPOJO.setChoice(choice);
                                Map<String, Object> map = new HashMap<>();
                                map.put(lifeBoardPOJO.getKey(), lifeBoardPOJO);
                                todaysReference.updateChildren(map);
                                notAvailable = false;
                            }
                        }
                        if (notAvailable) {
                            DatabaseReference pushForTodayReference = todaysReference.push();
                            LifeBoardPOJO lifeBoardPOJO1 = new LifeBoardPOJO(pushForTodayReference.getKey(), key, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR), calendar.getTime().toString(), "null", choice, userInfoPOJO);
                            pushForTodayReference.setValue(lifeBoardPOJO1);
                            Log.d("dateipdatedvalue", lifeBoardPOJO1.getDate());
                        }

                        if (mEditDateChoiceDialog.isShowing()) {
                            mEditDateChoiceDialog.dismiss();
                        }
                    } else {
                        DatabaseReference pushForTodayReference = todaysReference.push();
                        LifeBoardPOJO lifeBoardPOJO1 = new LifeBoardPOJO(pushForTodayReference.getKey(), key, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR), calendar.getTime().toString(), "null", choice, userInfoPOJO);
                        pushForTodayReference.setValue(lifeBoardPOJO1);
                        Log.d("dateipdatedvalue", lifeBoardPOJO1.getDate());
                        if (mEditDateChoiceDialog.isShowing()) {
                            mEditDateChoiceDialog.dismiss();
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public class HabitDurationDatesViewHolder extends RecyclerView.ViewHolder {
            public TextView mDateValue;

            public HabitDurationDatesViewHolder(View itemView) {
                super(itemView);

                mDateValue = itemView.findViewById(R.id.date_value_button);
                mDateValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDateDialog(getPosition());
                    }
                });

            }
        }

    }


    public class MonthDates {
        int monthDatesKey; // year+month
        int year;
        int month;
        String sortKey;
        ArrayList<Date> dates;

        public MonthDates() {
        }

        public MonthDates(int monthDatesKey, int year, int month, ArrayList<Date> dates) {
            this.monthDatesKey = monthDatesKey;
            this.year = year;
            this.month = month;
            this.dates = dates;
            this.sortKey = String.valueOf(year) + "-" + String.valueOf(month + 111);
        }

        public String getSortKey() {
            return sortKey;
        }

        public void setSortKey(String sortKey) {
            this.sortKey = sortKey;
        }

        public int getMonthDatesKey() {
            return monthDatesKey;
        }

        public void setMonthDatesKey(int monthDatesKey) {
            this.monthDatesKey = monthDatesKey;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public ArrayList<Date> getDates() {
            return dates;
        }

        public void setDates(ArrayList<Date> dates) {
            this.dates = dates;
        }
    }

    public static ArrayList<Integer> getKeysList(Map<Integer, MonthDates> mp) {
        ArrayList<Integer> keysArrayList = new ArrayList<>();
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            keysArrayList.add((Integer) pair.getKey());
        }
        return keysArrayList;
    }

    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getKey())
                        .compareTo(((Map.Entry) (o2)).getKey());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

}


