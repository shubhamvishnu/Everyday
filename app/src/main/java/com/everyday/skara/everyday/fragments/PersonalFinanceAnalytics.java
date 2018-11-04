package com.everyday.skara.everyday.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.PersonalFinancialBoardActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.Categories;
import com.everyday.skara.everyday.pojo.ExpensePOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class PersonalFinanceAnalytics extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    UserInfoPOJO userInfoPOJO;
    ArrayList<ExpensePOJO> expensePOJOArrayList;
    ChildEventListener mExpenseChildEventListener;
    DatabaseReference mExpensesDatabaseReference;

    BottomSheetDialog mMonthBottomSheetDialog;

    LinkedHashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<ExpensePOJO>>>> catYearMonthExpenseArrayListHashMap;
    ArrayList<Categories> categoriesArrayList;

    FloatingTextButton mMonthSelectionButton;
    TextView mTotalExpenseTextView;
    TextView mCurencyTextView;

    View view;

    int currentYear;
    int currentMonth;


    PieChart mPieChart;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_personal_finance_analytics_layout, container, false);
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
        userInfoPOJO = (UserInfoPOJO) getArguments().getSerializable("user_profile");

        mMonthSelectionButton = view.findViewById(R.id.month_selection_button);
        mTotalExpenseTextView = view.findViewById(R.id.total_amount_textview);
        mCurencyTextView = view.findViewById(R.id.currency_textview);

        mPieChart = view.findViewById(R.id.expense_pie_chart);

        String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
        mCurencyTextView.setText(currency);
        expensePOJOArrayList = new ArrayList<>();

        currentYear = PersonalFinancialBoardActivity.mViewCurrentYear;
        currentMonth = PersonalFinancialBoardActivity.mViewCurrentMonth;

        mTotalExpenseTextView.setText("0.00");

        mMonthSelectionButton.setTitle(String.valueOf(currentMonth));

        mMonthSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthSelectionDialog();
            }
        });
        initCategories();
    }

    void showMonthSelectionDialog() {
        mMonthBottomSheetDialog = new BottomSheetDialog(getActivity());
        mMonthBottomSheetDialog.setContentView(R.layout.dialog_month_selection_layout);
        ImageButton mClose = mMonthBottomSheetDialog.findViewById(R.id.close_month_selection_dialog);
        Button mDone = mMonthBottomSheetDialog.findViewById(R.id.month_selection_done);

        Button m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12;
        m1 = mMonthBottomSheetDialog.findViewById(R.id.month_1_button);
        m2 = mMonthBottomSheetDialog.findViewById(R.id.month_2_button);
        m3 = mMonthBottomSheetDialog.findViewById(R.id.month_3_button);
        m4 = mMonthBottomSheetDialog.findViewById(R.id.month_4_button);
        m5 = mMonthBottomSheetDialog.findViewById(R.id.month_5_button);
        m6 = mMonthBottomSheetDialog.findViewById(R.id.month_6_button);
        m7 = mMonthBottomSheetDialog.findViewById(R.id.month_7_button);
        m8 = mMonthBottomSheetDialog.findViewById(R.id.month_8_button);
        m9 = mMonthBottomSheetDialog.findViewById(R.id.month_9_button);
        m10 = mMonthBottomSheetDialog.findViewById(R.id.month_10_button);
        m11 = mMonthBottomSheetDialog.findViewById(R.id.month_11_button);
        m12 = mMonthBottomSheetDialog.findViewById(R.id.month_12_button);

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonthBottomSheetDialog.dismiss();
            }
        });
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonthBottomSheetDialog.dismiss();
            }
        });
        m1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 0;
                updatePieChart();
                mMonthBottomSheetDialog.dismiss();
            }
        });
        m2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 1;
                updatePieChart();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 2;
                updatePieChart();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 3;
                updatePieChart();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 4;
                updatePieChart();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 5;
                updatePieChart();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 6;
                updatePieChart();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 7;
                updatePieChart();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 8;
                updatePieChart();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 9;
                updatePieChart();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 10;
                updatePieChart();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 11;
                updatePieChart();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        mMonthBottomSheetDialog.setCanceledOnTouchOutside(false);
        mMonthBottomSheetDialog.show();
    }

    void initFinanceRecyclerView() {
       // TODO init Pie chart
        initExpenses();
    }

    void initCategories() {
        categoriesArrayList = new ArrayList<>();

        mExpensesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/categories");
        mExpensesDatabaseReference.keepSynced(true);
        mExpensesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    categoriesArrayList.add(snapshot.getValue(Categories.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        initFinanceRecyclerView();


    }

    void initExpenses() {
        expensePOJOArrayList = new ArrayList<>();

        catYearMonthExpenseArrayListHashMap = new LinkedHashMap<>();

        mExpensesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/expenses");
        mExpensesDatabaseReference.keepSynced(true);
        mExpenseChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ExpensePOJO expensePOJO = dataSnapshot.getValue(ExpensePOJO.class);
                expensePOJOArrayList.add(expensePOJO);

                if (catYearMonthExpenseArrayListHashMap.containsKey(expensePOJO.getCategories().getCategoryKey())) {
                    if (catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).containsKey(expensePOJO.getYear())) {
                        if (catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                            ArrayList<ExpensePOJO> expensePOJOArrayList4 = catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).get(expensePOJO.getMonth());
                            expensePOJOArrayList4.add(expensePOJO);
                            catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList4);
                        } else {
                            ArrayList<ExpensePOJO> expensePOJOArrayList4 = new ArrayList<>();
                            expensePOJOArrayList4.add(expensePOJO);
                            catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList4);

                        }

                    } else {
                        HashMap<Integer, ArrayList<ExpensePOJO>> monthHashMap = new HashMap<>();

                        ArrayList<ExpensePOJO> expensePOJOArrayListTemp = new ArrayList<>();
                        expensePOJOArrayListTemp.add(expensePOJO);

                        monthHashMap.put(expensePOJO.getMonth(), expensePOJOArrayListTemp);

                        catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).put(expensePOJO.getYear(), monthHashMap);
                    }
                } else {
                    HashMap<Integer, HashMap<Integer, ArrayList<ExpensePOJO>>> yearMonthHashMap = new HashMap<>();
                    HashMap<Integer, ArrayList<ExpensePOJO>> monthHashMap = new HashMap<>();

                    ArrayList<ExpensePOJO> expensePOJOArrayListTemp = new ArrayList<>();
                    expensePOJOArrayListTemp.add(expensePOJO);

                    monthHashMap.put(expensePOJO.getMonth(), expensePOJOArrayListTemp);
                    yearMonthHashMap.put(expensePOJO.getYear(), monthHashMap);
                    catYearMonthExpenseArrayListHashMap.put(expensePOJO.getCategories().getCategoryKey(), yearMonthHashMap);
                }

                // reflect updated data
                updatePieChart();
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
        mExpensesDatabaseReference.addChildEventListener(mExpenseChildEventListener);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (mExpenseChildEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseChildEventListener);
        }
        PersonalFinancialBoardActivity.mViewCurrentMonth = currentMonth;
        PersonalFinancialBoardActivity.mViewCurrentYear = currentYear;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mExpenseChildEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseChildEventListener);
        }
        PersonalFinancialBoardActivity.mViewCurrentMonth = currentMonth;
        PersonalFinancialBoardActivity.mViewCurrentYear = currentYear;
    }

    class PieDataXY {
        Categories category;
        double totalExpense;

        public PieDataXY() {

        }

        public PieDataXY(Categories category, double totalExpense) {
            this.category = category;
            this.totalExpense = totalExpense;
        }

        public Categories getCategory() {
            return category;
        }

        public void setCategory(Categories category) {
            this.category = category;
        }

        public double getTotalExpense() {
            return totalExpense;
        }

        public void setTotalExpense(double totalExpense) {
            this.totalExpense = totalExpense;
        }
    }


    void updatePieChart() {
        ArrayList<PieDataXY> pieDataXYArrayList = new ArrayList<>();
        double overallExpense = 0.0;
        for (int position = 0; position < categoriesArrayList.size(); position++) {
            double total = 0.0;
            Categories categories = categoriesArrayList.get(position);
            if (catYearMonthExpenseArrayListHashMap.containsKey(categories.getCategoryKey())) {
                if (catYearMonthExpenseArrayListHashMap.get(categories.getCategoryKey()).containsKey(currentYear)) {
                    if (catYearMonthExpenseArrayListHashMap.get(categories.getCategoryKey()).get(currentYear).containsKey(currentMonth)) {
                        expensePOJOArrayList = catYearMonthExpenseArrayListHashMap.get(categories.getCategoryKey()).get(currentYear).get(currentMonth);
                        for (int i = 0; i < expensePOJOArrayList.size(); i++) {
                            total += expensePOJOArrayList.get(i).getAmount();
                        }

                    }
                }
            }
            overallExpense += total;
            pieDataXYArrayList.add(new PieDataXY(categories, total));
        }
        reflectPieChartData();
    }
    void reflectPieChartData(){
        mPieChart.setUsePercentValues(true);
        Description description = mPieChart.getDescription();
        description.setText("Category wise expenses for the month");
        mPieChart.setDescription(description);
        mPieChart.setDrawHoleEnabled(true);
        // mPieChart.setHoleColor(get);
        mPieChart.setTransparentCircleRadius(10);
        mPieChart.setRotationEnabled(true);
        mPieChart.setRotationAngle(0);

    }
    
    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public Object getElementByIndex(LinkedHashMap map, int index) {
        return map.get((map.keySet().toArray())[index]);
    }
}