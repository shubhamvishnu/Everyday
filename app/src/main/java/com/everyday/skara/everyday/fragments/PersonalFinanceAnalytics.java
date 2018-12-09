package com.everyday.skara.everyday.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.PersonalFinancialBoardActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.DateExpenseHolder;
import com.everyday.skara.everyday.classes.ExpenseTypes;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.Categories;
import com.everyday.skara.everyday.pojo.FinanceEntryPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class PersonalFinanceAnalytics extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    UserInfoPOJO userInfoPOJO;
    ArrayList<FinanceEntryPOJO> expensePOJOArrayList;
    ValueEventListener mExpenseValueEventListener;
    DatabaseReference mExpensesDatabaseReference;

    BottomSheetDialog mMonthBottomSheetDialog;

    HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<FinanceEntryPOJO>>>> catYearMonthExpenseArrayListHashMap;
    HashMap<Integer, HashMap<Integer, HashMap<String, ArrayList<FinanceEntryPOJO>>>> yearMonthDateHashMap;

    ArrayList<Categories> categoriesArrayList;

    FloatingTextButton mMonthSelectionButton;
    TextView mTotalExpenseTextView, mTotalIncomeTextView, mRemaining;
    TextView mCurencyTextView, mPositiveCurr;

    View view;

    int currentYear;
    int currentMonth;
    HashMap<Integer, HashMap<Integer, ArrayList<FinanceEntryPOJO>>> yearMonthExpenseArrayListHashMap;
    HashMap<Integer, HashMap<Integer, ArrayList<FinanceEntryPOJO>>> yearMonthIncomeArrayListHashMap;

    ArrayList<CategoryExpensePOJO> mCategoryExpenseArrayList = new ArrayList<>();
    HashMap<String, ArrayList<WeekDayWiseExpense>> weekWiseExpenseHashMap;

    TextView mMaxExpenseCat, mMaxExpenseCatName;
    TextView mExpenseDateTextView, mExpenseDayAmount;
    TextView mExpensiveExpenseTextView, mExpensiveExpenseCatTextView;
    PieChart mPieChart;
    LineChart mExpensesLineChart;

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
        mTotalIncomeTextView = view.findViewById(R.id.total_income_textview);
        mCurencyTextView = view.findViewById(R.id.currency_textview);
        mMaxExpenseCatName = view.findViewById(R.id.max_expense_cat_name_textview);
        mPositiveCurr = view.findViewById(R.id.positive_currency_all_textview);
        mMaxExpenseCat = view.findViewById(R.id.max_expense_cat_textview);
        mExpenseDateTextView = view.findViewById(R.id.max_expense_day_date_textview);
        mExpenseDayAmount = view.findViewById(R.id.max_expense_day_textview);
        mRemaining = view.findViewById(R.id.total_remaining);
        mExpensiveExpenseTextView = view.findViewById(R.id.maximum_expense_amount);
        mExpensiveExpenseCatTextView = view.findViewById(R.id.maximum_expense_cat);
        mExpensesLineChart = view.findViewById(R.id.expenses_line_chart);

        mPieChart = view.findViewById(R.id.expense_pie_chart);

        String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
        mCurencyTextView.setText(currency);
        mPositiveCurr.setText(currency);
        expensePOJOArrayList = new ArrayList<>();

        currentYear = PersonalFinancialBoardActivity.mViewCurrentYear;
        currentMonth = PersonalFinancialBoardActivity.mViewCurrentMonth;

        mTotalExpenseTextView.setText("0.00");
        mTotalIncomeTextView.setText("0.00");
        mRemaining.setText("0.00");
        updateMonthTitle();

        mMonthSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthSelectionDialog();
            }
        });

        mMaxExpenseCatName.setText(null);
        mMaxExpenseCat.setText(null);
        mExpenseDateTextView.setText(null);
        mExpenseDayAmount.setText(null);
        mExpensiveExpenseTextView.setText(null);
        mExpensiveExpenseCatTextView.setText(null);
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
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();
                mMonthBottomSheetDialog.dismiss();
            }
        });
        m2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 1;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 2;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 3;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 4;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 5;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 6;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 7;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 8;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 9;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 10;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 11;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        mMonthBottomSheetDialog.setCanceledOnTouchOutside(false);
        mMonthBottomSheetDialog.show();
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

        initExpenses();


    }

    void updateMonthTitle() {
        switch (currentMonth) {
            case 0:
                mMonthSelectionButton.setTitle("Jan");
                break;
            case 1:
                mMonthSelectionButton.setTitle("Feb");
                break;
            case 2:
                mMonthSelectionButton.setTitle("Mar");
                break;
            case 3:
                mMonthSelectionButton.setTitle("Apr");
                break;
            case 4:
                mMonthSelectionButton.setTitle("May");
                break;
            case 5:
                mMonthSelectionButton.setTitle("Jun");
                break;
            case 6:
                mMonthSelectionButton.setTitle("Jul");
                break;
            case 7:
                mMonthSelectionButton.setTitle("Aug");
                break;
            case 8:
                mMonthSelectionButton.setTitle("Sep");
                break;
            case 9:
                mMonthSelectionButton.setTitle("Oct");
                break;
            case 10:
                mMonthSelectionButton.setTitle("Nov");
                break;
            case 11:
                mMonthSelectionButton.setTitle("Dec");
                break;

        }
    }

    void initExpenses() {
        expensePOJOArrayList = new ArrayList<>();

        yearMonthExpenseArrayListHashMap = new HashMap<>();
        yearMonthIncomeArrayListHashMap = new HashMap<>();
        yearMonthDateHashMap = new HashMap<>();
        catYearMonthExpenseArrayListHashMap = new HashMap<>();

        mExpensesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/expenses");
        mExpensesDatabaseReference.keepSynced(true);
        mExpenseValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        FinanceEntryPOJO expensePOJO = dataSnapshot.getValue(FinanceEntryPOJO.class);
                        if (expensePOJO.getEntryType() == ExpenseTypes.ENTRY_TYPE_EXPENSE) {
                            expensePOJOArrayList.add(expensePOJO);

                            /**----------------------------------------------------------------------*/
                            // expense YEAR/MONTH wise
                            if (yearMonthExpenseArrayListHashMap.containsKey(expensePOJO.getYear())) {
                                if (yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth());
                                    expensePOJOArrayList2.add(expensePOJO);
                                    yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList2);
                                } else {
                                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = new ArrayList<>();
                                    expensePOJOArrayList2.add(expensePOJO);
                                    yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList2);
                                }
                            } else {
                                HashMap<Integer, ArrayList<FinanceEntryPOJO>> monthHashMap = new HashMap<>();
                                ArrayList<FinanceEntryPOJO> expensePOJOArrayListTemp = new ArrayList<>();
                                expensePOJOArrayListTemp.add(expensePOJO);
                                monthHashMap.put(expensePOJO.getMonth(), expensePOJOArrayListTemp);
                                yearMonthExpenseArrayListHashMap.put(expensePOJO.getYear(), monthHashMap);
                            }

                            /**----------------------------------------------------------------------*/
                            // expense YEAR/MONTH/DATE wise
                            if (yearMonthDateHashMap.containsKey(expensePOJO.getYear())) {
                                if (yearMonthDateHashMap.get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                                    if (yearMonthDateHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth()).containsKey(expensePOJO.getDate())) {
                                        ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = yearMonthDateHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth()).get(expensePOJO.getDate());
                                        expensePOJOArrayList2.add(expensePOJO);
                                        yearMonthDateHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth()).put(expensePOJO.getDate(), expensePOJOArrayList2);
                                    } else {
                                        ArrayList<FinanceEntryPOJO> expensePOJOArrayList5 = new ArrayList<>();
                                        expensePOJOArrayList5.add(expensePOJO);
                                        yearMonthDateHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth()).put(expensePOJO.getDate(), expensePOJOArrayList5);
                                    }
                                } else {
                                    HashMap<String, ArrayList<FinanceEntryPOJO>> dateExpenseHashMap = new HashMap<>();
                                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = new ArrayList<>();
                                    expensePOJOArrayList2.add(expensePOJO);
                                    dateExpenseHashMap.put(expensePOJO.getDate(), expensePOJOArrayList2);
                                    yearMonthDateHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), dateExpenseHashMap);
                                }
                            } else {
                                HashMap<Integer, HashMap<String, ArrayList<FinanceEntryPOJO>>> monthDateExpenseHashMap = new HashMap<>();
                                HashMap<String, ArrayList<FinanceEntryPOJO>> dateExpenseHashMap = new HashMap<>();
                                ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = new ArrayList<>();
                                expensePOJOArrayList2.add(expensePOJO);
                                dateExpenseHashMap.put(expensePOJO.getDate(), expensePOJOArrayList2);
                                monthDateExpenseHashMap.put(expensePOJO.getMonth(), dateExpenseHashMap);
                                yearMonthDateHashMap.put(expensePOJO.getYear(), monthDateExpenseHashMap);
                            }
                            /**----------------------------------------------------------------------*/
                            if (catYearMonthExpenseArrayListHashMap.containsKey(expensePOJO.getCategories().getCategoryKey())) {
                                if (catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).containsKey(expensePOJO.getYear())) {
                                    if (catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                                        ArrayList<FinanceEntryPOJO> expensePOJOArrayList4 = catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).get(expensePOJO.getMonth());
                                        expensePOJOArrayList4.add(expensePOJO);
                                        catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList4);
                                    } else {
                                        ArrayList<FinanceEntryPOJO> expensePOJOArrayList4 = new ArrayList<>();
                                        expensePOJOArrayList4.add(expensePOJO);
                                        catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList4);

                                    }

                                } else {
                                    HashMap<Integer, ArrayList<FinanceEntryPOJO>> monthHashMap = new HashMap<>();

                                    ArrayList<FinanceEntryPOJO> expensePOJOArrayListTemp = new ArrayList<>();
                                    expensePOJOArrayListTemp.add(expensePOJO);

                                    monthHashMap.put(expensePOJO.getMonth(), expensePOJOArrayListTemp);

                                    catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).put(expensePOJO.getYear(), monthHashMap);
                                }
                            } else {
                                HashMap<Integer, HashMap<Integer, ArrayList<FinanceEntryPOJO>>> yearMonthHashMap = new HashMap<>();
                                HashMap<Integer, ArrayList<FinanceEntryPOJO>> monthHashMap = new HashMap<>();

                                ArrayList<FinanceEntryPOJO> expensePOJOArrayListTemp = new ArrayList<>();
                                expensePOJOArrayListTemp.add(expensePOJO);

                                monthHashMap.put(expensePOJO.getMonth(), expensePOJOArrayListTemp);
                                yearMonthHashMap.put(expensePOJO.getYear(), monthHashMap);
                                catYearMonthExpenseArrayListHashMap.put(expensePOJO.getCategories().getCategoryKey(), yearMonthHashMap);
                            }
                        } else if (expensePOJO.getEntryType() == ExpenseTypes.ENTRY_TYPE_INCOME) {
                            /** ----------------------------------------------------*/
                            // income YEAR/MONTH wise
                            if (yearMonthIncomeArrayListHashMap.containsKey(expensePOJO.getYear())) {
                                if (yearMonthIncomeArrayListHashMap.get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                                    ArrayList<FinanceEntryPOJO> incomePOJOArrayList2 = yearMonthIncomeArrayListHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth());
                                    incomePOJOArrayList2.add(expensePOJO);
                                    yearMonthIncomeArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), incomePOJOArrayList2);
                                } else {
                                    ArrayList<FinanceEntryPOJO> incomePOJOArrayList2 = new ArrayList<>();
                                    incomePOJOArrayList2.add(expensePOJO);
                                    yearMonthIncomeArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), incomePOJOArrayList2);
                                }
                            } else {
                                HashMap<Integer, ArrayList<FinanceEntryPOJO>> monthHashMap = new HashMap<>();
                                ArrayList<FinanceEntryPOJO> incomePOJOArrayList2Temp = new ArrayList<>();
                                incomePOJOArrayList2Temp.add(expensePOJO);
                                monthHashMap.put(expensePOJO.getMonth(), incomePOJOArrayList2Temp);
                                yearMonthIncomeArrayListHashMap.put(expensePOJO.getYear(), monthHashMap);
                            }
                        }
                    }
                }

                /**----------------------------------------------------------------------*/

                // reflect updated data
                updateTotalExpense();
                updateCategoryExpenses();
                updateDayExpenses();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mExpensesDatabaseReference.addValueEventListener(mExpenseValueEventListener);
    }

    void updateTotalExpense() {
        double tempTotal = 0.0;
        updateMonthTitle();
        if (yearMonthExpenseArrayListHashMap.containsKey(currentYear)) {
            if (yearMonthExpenseArrayListHashMap.get(currentYear).containsKey(currentMonth)) {
                if (yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth).size() == 0) {
                    mTotalExpenseTextView.setText("0.00");
                } else {
                    ArrayList<FinanceEntryPOJO> tempExpensePOJO = yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth);
                    tempTotal = 0.0;
                    for (int i = 0; i < tempExpensePOJO.size(); i++) {
                        if (tempExpensePOJO.get(i).getEntryType() == ExpenseTypes.ENTRY_TYPE_EXPENSE) {
                            tempTotal += tempExpensePOJO.get(i).getAmount();
                        }
                    }
                    mTotalExpenseTextView.setText(String.format(Locale.getDefault(), "%.2f", tempTotal));
                }
            }
        }

        updateTotalIncome(tempTotal);
    }

    void updateTotalIncome(double tempExpense) {
        double tempTotal = 0.0;
        if (yearMonthIncomeArrayListHashMap.containsKey(currentYear)) {
            if (yearMonthIncomeArrayListHashMap.get(currentYear).containsKey(currentMonth)) {
                if (yearMonthIncomeArrayListHashMap.get(currentYear).get(currentMonth).size() == 0) {
                    mTotalIncomeTextView.setText("0.00");
                } else {
                    ArrayList<FinanceEntryPOJO> tempIncomePOJO = yearMonthIncomeArrayListHashMap.get(currentYear).get(currentMonth);
                    tempTotal = 0.0;
                    for (int i = 0; i < tempIncomePOJO.size(); i++) {
                        if (tempIncomePOJO.get(i).getEntryType() == ExpenseTypes.ENTRY_TYPE_INCOME) {
                            tempTotal += tempIncomePOJO.get(i).getAmount();
                        }
                    }
                    mTotalIncomeTextView.setText(String.format(Locale.getDefault(), "%.2f", tempTotal));
                }
            }
        }
        updateRemainingIncomeView(tempExpense, tempTotal);

    }

    void updateRemainingIncomeView(double expense, double income) {
        String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
        double difference = income - expense;
        if (difference > 0) {
            mRemaining.setTextColor(getResources().getColor(R.color.green_selected));
            mRemaining.setText(" + " + currency + String.format(Locale.getDefault(), "%.2f", difference));
        } else {
            mRemaining.setTextColor(getResources().getColor(R.color.red));
            mRemaining.setText(" - " + currency + String.format(Locale.getDefault(), "%.2f", Math.abs(difference)));
        }
    }

    class DayExpensePOJO {
        double maxDayExpenseAmount;
        String dateOfMaxExpense;
        ArrayList<FinanceEntryPOJO> expensePOJOArrayList;

        public DayExpensePOJO() {
        }

        public DayExpensePOJO(double maxDayExpenseAmount, String dateOfMaxExpense, ArrayList<FinanceEntryPOJO> expensePOJOArrayList) {
            this.maxDayExpenseAmount = maxDayExpenseAmount;
            this.dateOfMaxExpense = dateOfMaxExpense;
            this.expensePOJOArrayList = expensePOJOArrayList;
        }


        public double getMaxDayExpenseAmount() {
            return maxDayExpenseAmount;
        }

        public void setMaxDayExpenseAmount(double maxDayExpenseAmount) {
            this.maxDayExpenseAmount = maxDayExpenseAmount;
        }

        public String getDateOfMaxExpense() {
            return dateOfMaxExpense;
        }

        public void setDateOfMaxExpense(String dateOfMaxExpense) {
            this.dateOfMaxExpense = dateOfMaxExpense;
        }

        public ArrayList<FinanceEntryPOJO> getExpensePOJOArrayList() {
            return expensePOJOArrayList;
        }

        public void setExpensePOJOArrayList(ArrayList<FinanceEntryPOJO> expensePOJOArrayList) {
            this.expensePOJOArrayList = expensePOJOArrayList;
        }
    }

    class WeekDayWiseExpense {
        String weekDay;
        ArrayList<FinanceEntryPOJO> expensePOJOArrayList;
        double totalWeekDayExpense;
        String date;

        public WeekDayWiseExpense(String weekDay, ArrayList<FinanceEntryPOJO> expensePOJOArrayList, double totalWeekDayExpense, String date) {
            this.weekDay = weekDay;
            this.expensePOJOArrayList = expensePOJOArrayList;
            this.totalWeekDayExpense = totalWeekDayExpense;
            this.date = date;
        }


        public String getWeekDay() {
            return weekDay;
        }

        public void setWeekDay(String weekDay) {
            this.weekDay = weekDay;
        }

        public ArrayList<FinanceEntryPOJO> getExpensePOJOArrayList() {
            return expensePOJOArrayList;
        }

        public void setExpensePOJOArrayList(ArrayList<FinanceEntryPOJO> expensePOJOArrayList) {
            this.expensePOJOArrayList = expensePOJOArrayList;
        }

        public double getTotalWeekDayExpense() {
            return totalWeekDayExpense;
        }

        public void setTotalWeekDayExpense(double totalWeekDayExpense) {
            this.totalWeekDayExpense = totalWeekDayExpense;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    void updateDayExpenses() {
        DayExpensePOJO dayExpensePOJO = new DayExpensePOJO(0.0, "-", new ArrayList<FinanceEntryPOJO>());
        FinanceEntryPOJO maxExpensePOJO = new FinanceEntryPOJO();
        maxExpensePOJO.setAmount(0.0);
        ArrayList<DateExpenseHolder> dateExpenseHolderArrayList;
        weekWiseExpenseHashMap = new HashMap<>();
        if (yearMonthDateHashMap.containsKey(currentYear)) {
            if (yearMonthDateHashMap.get(currentYear).containsKey(currentMonth)) {
                HashMap<String, ArrayList<FinanceEntryPOJO>> expensePOJOHashMapArrayList = yearMonthDateHashMap.get(currentYear).get(currentMonth);
                dateExpenseHolderArrayList = new ArrayList<>();
                for (Map.Entry<String, ArrayList<FinanceEntryPOJO>> entry : expensePOJOHashMapArrayList.entrySet()) {
                    dateExpenseHolderArrayList.add(new DateExpenseHolder(entry.getKey(), entry.getValue()));
                }

                for (int i = 0; i < dateExpenseHolderArrayList.size(); i++) {
                    DateExpenseHolder dateExpenseHolder = dateExpenseHolderArrayList.get(i);
                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList = dateExpenseHolder.getExpensePOJOArrayList();
                    double dayAmount = 0.0;
                    for (int j = 0; j < expensePOJOArrayList.size(); j++) {
                        FinanceEntryPOJO expensePOJO = expensePOJOArrayList.get(j);
                        dayAmount += expensePOJO.getAmount();
                        if (expensePOJO.getAmount() >= maxExpensePOJO.getAmount()) {
                            maxExpensePOJO = expensePOJO;
                        }
                    }

                    if (dayAmount >= dayExpensePOJO.getMaxDayExpenseAmount()) {
                        dayExpensePOJO = new DayExpensePOJO(dayAmount, dateExpenseHolder.getDate(), expensePOJOArrayList);
                    }

                    /**---------------------------------------------------------------------------*/
                    String dayOfWeek = null;
                    try {
                        dayOfWeek = new SimpleDateFormat("EE").format(new SimpleDateFormat("dd/MM/yyyy").parse(dateExpenseHolder.getDate()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (weekWiseExpenseHashMap.containsKey(dayOfWeek)) {
                        WeekDayWiseExpense weekDayWiseExpense = new WeekDayWiseExpense(dayOfWeek, expensePOJOArrayList, dayAmount, dateExpenseHolder.getDate());
                        ArrayList<WeekDayWiseExpense> weekDayWiseExpensesArrayList = weekWiseExpenseHashMap.get(dayOfWeek);
                        weekDayWiseExpensesArrayList.add(weekDayWiseExpense);
                        weekWiseExpenseHashMap.put(dayOfWeek, weekDayWiseExpensesArrayList);
                    } else {
                        WeekDayWiseExpense weekDayWiseExpense = new WeekDayWiseExpense(dayOfWeek, expensePOJOArrayList, dayAmount, dateExpenseHolder.getDate());
                        ArrayList<WeekDayWiseExpense> weekDayWiseExpensesArrayList = new ArrayList<>();
                        weekDayWiseExpensesArrayList.add(weekDayWiseExpense);
                        weekWiseExpenseHashMap.put(dayOfWeek, weekDayWiseExpensesArrayList);
                    }
                    /**---------------------------------------------------------------------------*/

                }

                mExpenseDateTextView.setText(dayExpensePOJO.getDateOfMaxExpense());
                mExpenseDayAmount.setText(String.format(Locale.getDefault(), "%.2f", dayExpensePOJO.maxDayExpenseAmount));
                mExpensiveExpenseCatTextView.setText(maxExpensePOJO.getCategories().getCategoryName());
                mExpensiveExpenseTextView.setText(String.format(Locale.getDefault(), "%.2f", maxExpensePOJO.getAmount()));
            } else {
                // no spendings this month

            }

        }

        reflectLineChart();
    }

    void reflectLineChart() {

    }


    @Override
    public void onStop() {
        super.onStop();
        if (mExpenseValueEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseValueEventListener);
        }
        PersonalFinancialBoardActivity.mViewCurrentMonth = currentMonth;
        PersonalFinancialBoardActivity.mViewCurrentYear = currentYear;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mExpenseValueEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseValueEventListener);
        }
        PersonalFinancialBoardActivity.mViewCurrentMonth = currentMonth;
        PersonalFinancialBoardActivity.mViewCurrentYear = currentYear;
    }

    class CategoryExpensePOJO {
        Categories category;
        double totalExpense;

        public CategoryExpensePOJO() {

        }

        public CategoryExpensePOJO(Categories category, double totalExpense) {
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

    void updateCategoryExpenses() {
        mCategoryExpenseArrayList = new ArrayList<>();
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
            mCategoryExpenseArrayList.add(new CategoryExpensePOJO(categories, total));
        }
        reflectPieChartData(overallExpense);
        logExpenses();
        analyzeCategories();
    }

    void logExpenses() {
        Log.d("expense_log", "-------------------------------------------------------");
        Log.d("expense_log_size", mCategoryExpenseArrayList.size() + "");

        for (int i = 0; i < mCategoryExpenseArrayList.size(); i++) {
            Log.d("expenses_log", mCategoryExpenseArrayList.get(i).getCategory().getCategoryName() + ":" + mCategoryExpenseArrayList.get(i).getTotalExpense() + "----");
        }
        Log.d("expenses_log", "------------------------------------------------------");

    }

    class CategoricalAnalysis {
        boolean isAvailable;
        Categories maxCategory;
        double max;

        public CategoricalAnalysis() {
        }

        public CategoricalAnalysis(boolean isAvailable, Categories maxCategory, double max) {
            this.isAvailable = isAvailable;
            this.maxCategory = maxCategory;
            this.max = max;
        }

        public Categories getMaxCategory() {
            return maxCategory;
        }

        public void setMaxCategory(Categories maxCategory) {
            this.maxCategory = maxCategory;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
            this.max = max;
        }
    }

    void analyzeCategories() {
        CategoricalAnalysis categoricalAnalysis = new CategoricalAnalysis(false, new Categories(), 0.0);
        for (int i = 0; i < mCategoryExpenseArrayList.size(); i++) {
            double tempTotal = mCategoryExpenseArrayList.get(i).getTotalExpense();
            if (tempTotal >= categoricalAnalysis.getMax()) {
                categoricalAnalysis = new CategoricalAnalysis(true, mCategoryExpenseArrayList.get(i).getCategory(), mCategoryExpenseArrayList.get(i).getTotalExpense());
            }
        }

        mMaxExpenseCatName.setText(categoricalAnalysis.getMaxCategory().getCategoryName());
        mMaxExpenseCat.setText(categoricalAnalysis.getMax() + "");
        //TODO: show maximum expenditure category
    }

    void reflectPieChartData(double overallExpense) {
        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < mCategoryExpenseArrayList.size(); i++) {
            if (mCategoryExpenseArrayList.get(i).getTotalExpense() != 0.0) {
                double catPercentage = ((mCategoryExpenseArrayList.get(i).getTotalExpense()) / overallExpense) * 100;
                Log.d("qwerty", "reflectPieChartData: " + (float) catPercentage + " -- Category name :" + mCategoryExpenseArrayList.get(i).getCategory().getCategoryName());
                Log.d("qwerty", "--expense" + mCategoryExpenseArrayList.get(i).getTotalExpense());
                Log.d("qwerty", "-----------------------------------");

                entries.add(new PieEntry((float) (catPercentage), mCategoryExpenseArrayList.get(i).getCategory().getCategoryName()));
            }

        }
        PieDataSet set = new PieDataSet(entries, "Expense Breakdown");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(set);
        Legend legend = mPieChart.getLegend();
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        legend.setWordWrapEnabled(true);
        mPieChart.setData(data);
        mPieChart.setDrawEntryLabels(false);


        mPieChart.setUsePercentValues(true);
        Description description = mPieChart.getDescription();
        description.setText("Category wise expenses for the month");
        mPieChart.setDescription(description);
        mPieChart.setDrawHoleEnabled(true);
        // mPieChart.setHoleColor(get);
        mPieChart.setTransparentCircleRadius(10);
        mPieChart.setRotationEnabled(true);
        mPieChart.setRotationAngle(0);

        mPieChart.invalidate();

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