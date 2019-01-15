package com.everyday.skara.everyday.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.FirebaseReferences;
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
    HashMap<Integer, LifeBoardPOJO> lifeBoardPOJOMap;
    HabitDurationAdapter datesAdapter;
    RatingBar mRatingBar;
    ImageButton mDayThumbsUp, mDayThumsbDown;

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

        Calendar todayCalendar = Calendar.getInstance();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.YEAR, 1996);
        startCalendar.set(Calendar.MONTH, 0);
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        Calendar endCalendar = todayCalendar;
        endCalendar.add(Calendar.DATE, -1);

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
                mDayThumsbDown.setColorFilter(getResources().getColor(R.color.green_));
                mDayThumbsUp.setColorFilter(getResources().getColor(R.color.grey));
                mDayThumbsUp.setBackgroundColor(getResources().getColor(R.color.transparent));
                mDayThumsbDown.setBackgroundColor(getResources().getColor(R.color.transparent));
                updateToday(0);
            }
        });
        initDateChecks();

        initDatesAdapter(startCalendar, endCalendar);
    }

    void updateToday(int choice) {
        //String key, String dateUniqueId, String day, String month, String year, String date, String rating, int choice
        Calendar todayCalendar = Calendar.getInstance();
        Integer key = Integer.parseInt(String.valueOf(todayCalendar.get(Calendar.YEAR)) + String.valueOf(todayCalendar.get(Calendar.MONTH)));

        DatabaseReference todaysReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_LIFE + "/lifeboard/");
        todaysReference.keepSynced(true);
        DatabaseReference pushForTodayReference = todaysReference.push();
        LifeBoardPOJO lifeBoardPOJO = new LifeBoardPOJO(pushForTodayReference.getKey(), Integer.valueOf(key), todayCalendar.get(Calendar.DAY_OF_MONTH), todayCalendar.get(Calendar.DAY_OF_MONTH), todayCalendar.get(Calendar.DAY_OF_MONTH), String.valueOf(todayCalendar.get(Calendar.DATE)), "null", choice);
        pushForTodayReference.setValue(lifeBoardPOJO);
        Log.d("dateipdatedvalue", lifeBoardPOJO.getDate());
        switch (choice) {
            case 0:
                break;
            case 1:
                break;

        }

    }

    void initDateChecks() {
        lifeBoardPOJOMap = new HashMap<>();
        mEntriesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_LIFE + "/lifeboard/");
        mEntriesDatabaseReference.keepSynced(true);
        mEntriesValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        LifeBoardPOJO lifeBoardPOJO = snapshot.getValue(LifeBoardPOJO.class);
                        lifeBoardPOJOMap.put(lifeBoardPOJO.getDateUniqueId(), lifeBoardPOJO);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    void initDatesAdapter(Calendar startDate, Calendar endDate) {
        mDatesRecyclerView.invalidate();
        mDatesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mDatesRecyclerView.setLayoutManager(linearLayoutManager);
        datesAdapter = new HabitDurationAdapter(getDaysBetweenDates(startDate, endDate));
        mDatesRecyclerView.setAdapter(datesAdapter);

    }

    public HashMap<Integer, MonthDates> getDaysBetweenDates(Calendar startdate, Calendar enddate) {
        HashMap<Integer, MonthDates> monthDatesHashMap = new HashMap<>();

        while (startdate.before(enddate)) {
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

        public HabitDurationAdapter(HashMap<Integer, MonthDates> datesHashMap) {
            try {
                this.inflator = LayoutInflater.from(getActivity());
                Map<Integer, MonthDates> map = new TreeMap<>(datesHashMap).descendingMap();
                this.datesHashMap = map;
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
            ((HabitDurationViewHolder) holder).mYearTitle.setText("" + monthDates.getYear());
            ((HabitDurationViewHolder) holder).mMonthTitle.setText("" + monthDates.getMonth());


            RecyclerView recyclerView = ((HabitDurationViewHolder) holder).mHabitDatesDurationRecyclerview;
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
            public TextView mMonthTitle, mYearTitle;
            public RecyclerView mHabitDatesDurationRecyclerview;

            public HabitDurationViewHolder(View itemView) {
                super(itemView);
                mMonthTitle = itemView.findViewById(R.id.month_title_textview);
                mYearTitle = itemView.findViewById(R.id.year_title_textview);
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


        public class HabitDurationDatesViewHolder extends RecyclerView.ViewHolder {
            public TextView mDateValue;

            public HabitDurationDatesViewHolder(View itemView) {
                super(itemView);

                mDateValue = itemView.findViewById(R.id.date_value_textview);

            }
        }

    }

    public class MonthDates {
        int monthDatesKey; // year+month
        int year;
        int month;
        ArrayList<Date> dates;

        public MonthDates() {
        }

        public MonthDates(int monthDatesKey, int year, int month, ArrayList<Date> dates) {
            this.monthDatesKey = monthDatesKey;
            this.year = year;
            this.month = month;
            this.dates = dates;
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


