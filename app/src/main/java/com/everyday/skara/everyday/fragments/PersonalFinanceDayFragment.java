package com.everyday.skara.everyday.fragments;

import android.animation.ObjectAnimator;
import android.content.Context;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.PersonalFinancialBoardFragment;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.BasicSettings;
import com.everyday.skara.everyday.classes.DateExpenseHolder;
import com.everyday.skara.everyday.classes.ExpenseTypes;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.FinanceEntryPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class PersonalFinanceDayFragment extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    UserInfoPOJO userInfoPOJO;
    ArrayList<FinanceEntryPOJO> expensePOJOArrayList;
    RecyclerView mPersonalFinanceRecyclerView;
    PersonalFinanceAdapter mPersonalFinanceAdapter;
    ChildEventListener mExpenseChildEventListener;
    DatabaseReference mExpensesDatabaseReference;

    BottomSheetDialog mMonthBottomSheetDialog;

    HashMap<Integer, HashMap<Integer, HashMap<String, ArrayList<FinanceEntryPOJO>>>> yearMonthDateHashMap;

    FloatingTextButton mMonthSelectionButton;
    TextView mTotalExpenseTextView, mIncomeTextView, mRemaining;
    TextView mCurencyTextView, mPosCurr;
    ImageButton mFilterButton;
    View view;
    public static boolean clicked = false;
    int currentYear;
    int currentMonth;

    Double totalExpense = 0.0;
    Double totalIncome = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            view = inflater.inflate(R.layout.fragment_personal_finance_layout_light, container, false);

        } else {
            view = inflater.inflate(R.layout.fragment_personal_finance_layout, container, false);
        }
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

        mPersonalFinanceRecyclerView = view.findViewById(R.id.personal_finance_recyclerview);
        mMonthSelectionButton = view.findViewById(R.id.month_selection_button);
        mTotalExpenseTextView = view.findViewById(R.id.total_amount_textview);
        mIncomeTextView = view.findViewById(R.id.total_income_textview);
        mCurencyTextView = view.findViewById(R.id.currency_textview);
        mPosCurr = view.findViewById(R.id.positive_currency_all_textview);
        mRemaining = view.findViewById(R.id.total_remaining);
        mFilterButton = getActivity().findViewById(R.id.filter_finance_option_button);
        String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
        mCurencyTextView.setText(currency);
        mPosCurr.setText(currency);
        expensePOJOArrayList = new ArrayList<>();

        yearMonthDateHashMap = new HashMap<>();


        currentYear = PersonalFinancialBoardFragment.mViewCurrentYear;
        currentMonth = PersonalFinancialBoardFragment.mViewCurrentMonth;

        mTotalExpenseTextView.setText("0.00");
        mIncomeTextView.setText("0.00");
        mRemaining.setText("0.00");

        updateMonthTitle();

        mMonthSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthSelectionDialog();
            }
        });
        initFinanceRecyclerView();
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = !clicked;
                if (clicked) {
                    mFilterButton.setRotation(180);
                    updateExpenses();
                } else {
                    mFilterButton.setRotation(0);
                    updateExpenses();
                }
            }
        });
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
                updateExpenses();
                mMonthBottomSheetDialog.dismiss();
            }
        });
        m2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 1;
                updateExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 2;
                updateExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 3;
                updateExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 4;
                updateExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 5;
                updateExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 6;
                updateExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 7;
                updateExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 8;
                updateExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 9;
                updateExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 10;
                updateExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 11;
                updateExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        mMonthBottomSheetDialog.setCanceledOnTouchOutside(true);
        mMonthBottomSheetDialog.show();
    }

    void initFinanceRecyclerView() {
        mPersonalFinanceRecyclerView.invalidate();
        mPersonalFinanceRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mPersonalFinanceRecyclerView.setLayoutManager(linearLayoutManager);
        mPersonalFinanceAdapter = new PersonalFinanceAdapter(new HashMap<String, ArrayList<FinanceEntryPOJO>>());
        mPersonalFinanceRecyclerView.setAdapter(mPersonalFinanceAdapter);

        initExpenses();
    }

    void initExpenses() {
        expensePOJOArrayList = new ArrayList<>();

        yearMonthDateHashMap = new HashMap<>();

        mExpensesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/expenses");
        mExpensesDatabaseReference.keepSynced(true);
        mExpenseChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FinanceEntryPOJO expensePOJO = dataSnapshot.getValue(FinanceEntryPOJO.class);
                expensePOJOArrayList.add(expensePOJO);

                // expense year and month wise
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

                // reflect updated data
                updateExpenses();
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


    void updateExpenses() {
        totalExpense = 0.0;
        totalIncome = 0.0;
        mTotalExpenseTextView.setText("0.00");
        mRemaining.setText("0.00");
        mIncomeTextView.setText("0.00");

        updateMonthTitle();
        mPersonalFinanceRecyclerView.invalidate();
        if (yearMonthDateHashMap.containsKey(currentYear)) {
            if (yearMonthDateHashMap.get(currentYear).containsKey(currentMonth)) {
                mPersonalFinanceAdapter = new PersonalFinanceAdapter(yearMonthDateHashMap.get(currentYear).get(currentMonth));
                updateView(yearMonthDateHashMap.get(currentYear).get(currentMonth));
            } else {
                mPersonalFinanceAdapter = new PersonalFinanceAdapter(new HashMap<String, ArrayList<FinanceEntryPOJO>>());
                updateView(new HashMap<String, ArrayList<FinanceEntryPOJO>>());
            }
        }

        mPersonalFinanceRecyclerView.setAdapter(mPersonalFinanceAdapter);
        mPersonalFinanceAdapter.notifyDataSetChanged();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mExpenseChildEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseChildEventListener);
        }
        PersonalFinancialBoardFragment.mViewCurrentMonth = currentMonth;
        PersonalFinancialBoardFragment.mViewCurrentYear = currentYear;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mExpenseChildEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseChildEventListener);
        }
        PersonalFinancialBoardFragment.mViewCurrentMonth = currentMonth;
        PersonalFinancialBoardFragment.mViewCurrentYear = currentYear;
    }


    void updateView(HashMap<String, ArrayList<FinanceEntryPOJO>> expensePOJOHashMapArrayList) {
        totalExpense = 0.0;
        totalIncome = 0.0;

        for (Map.Entry<String, ArrayList<FinanceEntryPOJO>> entry : expensePOJOHashMapArrayList.entrySet()) {

            DateExpenseHolder dateExpenseHolder = new DateExpenseHolder(entry.getKey(), entry.getValue());
            ArrayList<FinanceEntryPOJO> expensePOJOArrayList = dateExpenseHolder.getExpensePOJOArrayList();
            for (int i = 0; i < expensePOJOArrayList.size(); i++) {
                FinanceEntryPOJO expensePOJO = expensePOJOArrayList.get(i);
                if (expensePOJO.getEntryType() == ExpenseTypes.ENTRY_TYPE_EXPENSE) {
                    totalExpense += expensePOJO.getAmount();
                } else if (expensePOJO.getEntryType() == ExpenseTypes.ENTRY_TYPE_INCOME) {
                    totalIncome += expensePOJO.getAmount();
                }
            }
        }

        mTotalExpenseTextView.setText(String.format(Locale.getDefault(), "%.2f", totalExpense));
        mIncomeTextView.setText(String.format(Locale.getDefault(), "%.2f", totalIncome));
        updateRemainingIncomeView(totalExpense, totalIncome);
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

    ArrayList<DateExpenseHolder> sortDateAscending(ArrayList<DateExpenseHolder> dateExpenseHolderArrayList) {
        ArrayList<DateExpenseHolder> sortDateExpenseHolder = dateExpenseHolderArrayList;

        Collections.sort(sortDateExpenseHolder, new Comparator<DateExpenseHolder>() {
            DateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

            @Override
            public int compare(DateExpenseHolder o1, DateExpenseHolder o2) {
                try {
                    return f.parse(o2.getDate()).compareTo(f.parse(o1.getDate()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
        return sortDateExpenseHolder;
    }

    ArrayList<DateExpenseHolder> sortDateDescending(ArrayList<DateExpenseHolder> dateExpenseHolderArrayList) {
        ArrayList<DateExpenseHolder> sortDateExpenseHolder = dateExpenseHolderArrayList;

        Collections.sort(sortDateExpenseHolder, new Comparator<DateExpenseHolder>() {
            DateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

            @Override
            public int compare(DateExpenseHolder o1, DateExpenseHolder o2) {
                try {
                    return f.parse(o1.getDate()).compareTo(f.parse(o2.getDate()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
        return sortDateExpenseHolder;
    }

    public class PersonalFinanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;
        ArrayList<DateExpenseHolder> dateExpenseHolderArrayList;

        public PersonalFinanceAdapter(HashMap<String, ArrayList<FinanceEntryPOJO>> expensePOJOHashMapArrayList) {
            try {
                this.inflator = LayoutInflater.from(getActivity());
                mTotalExpenseTextView.setText(String.format(Locale.getDefault(), "%.2f", totalExpense));

                dateExpenseHolderArrayList = new ArrayList<>();
                for (Map.Entry<String, ArrayList<FinanceEntryPOJO>> entry : expensePOJOHashMapArrayList.entrySet()) {
                    dateExpenseHolderArrayList.add(new DateExpenseHolder(entry.getKey(), entry.getValue()));
                }
                if (clicked) {
                    dateExpenseHolderArrayList = sortDateDescending(dateExpenseHolderArrayList);
                } else {
                    dateExpenseHolderArrayList = sortDateAscending(dateExpenseHolderArrayList);
                }
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
            if (theme == BasicSettings.LIGHT_THEME) {
                View view = inflator.inflate(R.layout.recyclerview_day_wise_row_layout_light, parent, false);
                return new PersonalFinanceAdapter.PersonalFinanceViewHolder(view);
            } else {
                View view = inflator.inflate(R.layout.recyclerview_day_wise_row_layout, parent, false);
                return new PersonalFinanceAdapter.PersonalFinanceViewHolder(view);

            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
            ((PersonalFinanceViewHolder) holder).mCurrency.setText(currency);
            ((PersonalFinanceViewHolder) holder).mIncomeCurrecy.setText(currency);
            DateExpenseHolder dateExpenseHolder = dateExpenseHolderArrayList.get(position);
            ArrayList<FinanceEntryPOJO> expensePOJOArrayList = dateExpenseHolder.getExpensePOJOArrayList();
            double dayAmount = 0.0;
            double dayIncome = 0.0;
            for (int i = 0; i < expensePOJOArrayList.size(); i++) {
                FinanceEntryPOJO expensePOJO = expensePOJOArrayList.get(i);
                if (expensePOJO.getEntryType() == ExpenseTypes.ENTRY_TYPE_EXPENSE) {
                    dayAmount += expensePOJO.getAmount();
                } else if (expensePOJO.getEntryType() == ExpenseTypes.ENTRY_TYPE_INCOME) {
                    dayIncome += expensePOJO.getAmount();
                }
            }
            mTotalExpenseTextView.setText(String.format(Locale.getDefault(), "%.2f", totalExpense));
            double percentageValue = 0.0;
            if (totalExpense > 0) {
                if (dayAmount > 0) {
                    percentageValue = (dayAmount * 100) / totalExpense;
                }
            }
            ((PersonalFinanceViewHolder) holder).mDate.setText(dateExpenseHolder.getDate());
            ((PersonalFinanceViewHolder) holder).mAmount.setText(String.format(Locale.getDefault(), "%.2f", dayAmount));
            ((PersonalFinanceViewHolder) holder).mIncomeAmount.setText(String.format(Locale.getDefault(), "%.2f", dayIncome));
            ((PersonalFinanceViewHolder) holder).mPercentageValue.setText(String.format(Locale.getDefault(), "%.2f", percentageValue));


            //  ((PersonalFinanceViewHolder)holder).mProgressBar.setMax(totalExpense);
            setProgressAnimate(((PersonalFinanceViewHolder) holder).mProgressBar, Integer.parseInt(String.valueOf(Math.round(percentageValue))));
        }

        private void setProgressAnimate(ProgressBar pb, int progressTo) {
            ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", 0, progressTo);
            animation.setDuration(500);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }

        @Override
        public int getItemCount() {
            return this.dateExpenseHolderArrayList.size();
        }

        public class PersonalFinanceViewHolder extends RecyclerView.ViewHolder {
            public TextView mDate, mAmount, mIncomeAmount, mPercentageValue;
            public ProgressBar mProgressBar;
            public TextView mCurrency, mIncomeCurrecy;

            public PersonalFinanceViewHolder(View itemView) {
                super(itemView);
                mAmount = itemView.findViewById(R.id.date_expense_amount_textview);
                mDate = itemView.findViewById(R.id.date_expense_textview);
                mIncomeAmount = itemView.findViewById(R.id.date_income_amount_textview);
                mProgressBar = itemView.findViewById(R.id.expense_percentage);
                mCurrency = itemView.findViewById(R.id.date_expense_currency_textview);
                mIncomeCurrecy = itemView.findViewById(R.id.date_income_currency_textview);
                mPercentageValue = itemView.findViewById(R.id.expense_percentage_value_textview);
            }
        }

    }

    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
