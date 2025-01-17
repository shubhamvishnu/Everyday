package com.everyday.skara.everyday.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.classes.NewOptionTypes;
import com.everyday.skara.everyday.fragments.PersonalFinancialBoardFragment;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.BasicSettings;
import com.everyday.skara.everyday.classes.ExpenseTypes;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.Categories;
import com.everyday.skara.everyday.pojo.FinanceEntryPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class PersonalFinanceFragment extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    UserInfoPOJO userInfoPOJO;
    RecyclerView mPersonalFinanceRecyclerView;
    PersonalFinanceAdapter mPersonalFinanceAdapter;
    ChildEventListener mExpenseChildEventListener;
    DatabaseReference mExpensesDatabaseReference;

    BottomSheetDialog mMonthBottomSheetDialog;

    HashMap<String, ArrayList<FinanceEntryPOJO>> dateExpenseArrayListHashMap;
    HashMap<Integer, HashMap<Integer, ArrayList<FinanceEntryPOJO>>> yearMonthExpenseArrayListHashMap;

    FloatingTextButton mMonthSelectionButton;
    TextView mTotalExpenseTextView, mTotalIncomeTextView;
    TextView mCurencyTextView, mPositiveCurrency;
    ImageButton mFilterButton;
    View view;

    Double totalExpense = 0.0;
    Double totalIncome = 0.0;
    public static boolean clicked = false;
    int currentYear;
    int currentMonth;

    LinearLayout mEmptyLinearLayout,mFragmentLinearLayout;
    BottomSheetDialog mEditExpenseDialog;
    TextView mRemaining;


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
        mTotalIncomeTextView = view.findViewById(R.id.total_income_textview);
        mRemaining = view.findViewById(R.id.total_remaining);

        mFilterButton = getActivity().findViewById(R.id.filter_finance_option_button);

        mEmptyLinearLayout = view.findViewById(R.id.board_no_entries_linear_layout);
        mEmptyLinearLayout.setVisibility(View.INVISIBLE);
        mFragmentLinearLayout = view.findViewById(R.id.entries_linear_layout);

        mCurencyTextView = view.findViewById(R.id.currency_textview);
        mPositiveCurrency = view.findViewById(R.id.positive_currency_all_textview);
        String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
        mCurencyTextView.setText(currency);
        mPositiveCurrency.setText(currency);


        dateExpenseArrayListHashMap = new HashMap<>();
        yearMonthExpenseArrayListHashMap = new HashMap<>();

        currentYear = PersonalFinancialBoardFragment.mViewCurrentYear;
        currentMonth = PersonalFinancialBoardFragment.mViewCurrentMonth;

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

        setEmptyVisibility(0);
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
    void setEmptyVisibility(int action) {
        switch (action) {
            case 0:
                mFragmentLinearLayout.setVisibility(LinearLayout.GONE);
                mEmptyLinearLayout.setVisibility(LinearLayout.VISIBLE);
                break;
            case 1:
                mFragmentLinearLayout.setVisibility(LinearLayout.VISIBLE);
                mEmptyLinearLayout.setVisibility(LinearLayout.GONE);
                break;
        }
    }
    void showMonthSelectionDialog() {
        mMonthBottomSheetDialog = new BottomSheetDialog(getActivity());
        int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            mMonthBottomSheetDialog.setContentView(R.layout.dialog_month_selection_layout_light);

        } else {
            mMonthBottomSheetDialog.setContentView(R.layout.dialog_month_selection_layout);
        }
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
        mPersonalFinanceAdapter = new PersonalFinanceAdapter();
        mPersonalFinanceRecyclerView.setAdapter(mPersonalFinanceAdapter);

        initExpenses();
    }

    void initExpenses() {

        dateExpenseArrayListHashMap = new HashMap<>();
        yearMonthExpenseArrayListHashMap = new HashMap<>();


        mExpensesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/expenses");
        mExpensesDatabaseReference.keepSynced(true);
        mExpenseChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FinanceEntryPOJO expensePOJO = dataSnapshot.getValue(FinanceEntryPOJO.class);

                // expense year and month wise
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

                // expenses date wise
                if (dateExpenseArrayListHashMap.containsKey(expensePOJO.getDate())) {
                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList1 = dateExpenseArrayListHashMap.get(expensePOJO.getDate());
                    expensePOJOArrayList1.add(expensePOJO);
                    dateExpenseArrayListHashMap.put(expensePOJO.getDate(), expensePOJOArrayList1);
                } else {
                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList1 = new ArrayList<>();
                    expensePOJOArrayList1.add(expensePOJO);
                    dateExpenseArrayListHashMap.put(expensePOJO.getDate(), expensePOJOArrayList1);
                }

                // reflect updated data
                updateExpenses();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FinanceEntryPOJO expensePOJO = dataSnapshot.getValue(FinanceEntryPOJO.class);
                // expense year and month wise
                if (yearMonthExpenseArrayListHashMap.containsKey(expensePOJO.getYear())) {
                    if (yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                        ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth());
                        for (int i = 0; i < expensePOJOArrayList2.size(); i++) {
                            if (expensePOJOArrayList2.get(i).getEntryKey().equals(expensePOJO.getEntryKey())) {
                                expensePOJOArrayList2.set(i, expensePOJO);
                            }
                        }
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

                // reflect updated data
                updateExpenses();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                FinanceEntryPOJO expensePOJO = dataSnapshot.getValue(FinanceEntryPOJO.class);

                // expense year and month wise
                if (yearMonthExpenseArrayListHashMap.containsKey(expensePOJO.getYear())) {
                    if (yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                        ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth());
                        for (int i = 0; i < expensePOJOArrayList2.size(); i++) {
                            if (expensePOJOArrayList2.get(i).getEntryKey().equals(expensePOJO.getEntryKey())) {
                                expensePOJOArrayList2.remove(i);
                            }
                        }
                        yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList2);
                    }
                }

                updateExpenses();
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

    void updateExpenses() {
        totalExpense = 0.0;
        totalIncome = 0.0;
        mTotalExpenseTextView.setText("0.00");
        mRemaining.setText("0.00");
        mTotalIncomeTextView.setText("0.00");

        updateMonthTitle();
        if (clicked) {
            sortDateAscending();
        } else {
            sortDateDescending();
        }
        if (yearMonthExpenseArrayListHashMap.containsKey(currentYear)) {
            if (yearMonthExpenseArrayListHashMap.get(currentYear).containsKey(currentMonth)) {
                if (yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth).size() == 0) {
                    mTotalExpenseTextView.setText("0.00");
                    mTotalIncomeTextView.setText("0.00");
                    setEmptyVisibility(0);
                } else {
                    setEmptyVisibility(1);
                    ArrayList<FinanceEntryPOJO> tempExpensePOJO = yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth);
                    double tempTotal = 0.0;
                    double tempIncome = 0.0;
                    for (int i = 0; i < tempExpensePOJO.size(); i++) {
                        if (tempExpensePOJO.get(i).getEntryType() == ExpenseTypes.ENTRY_TYPE_EXPENSE) {
                            tempTotal += tempExpensePOJO.get(i).getAmount();
                        } else if (tempExpensePOJO.get(i).getEntryType() == ExpenseTypes.ENTRY_TYPE_INCOME) {
                            tempIncome += tempExpensePOJO.get(i).getAmount();
                        }
                    }


                    mTotalExpenseTextView.setText(String.format(Locale.getDefault(), "%.2f", tempTotal));
                    mTotalIncomeTextView.setText(String.format(Locale.getDefault(), "%.2f", tempIncome));
                    updateRemainingIncomeView(tempTotal, tempIncome);
                }
            }else{
                setEmptyVisibility(0);
            }
        }else{

            setEmptyVisibility(0);
        }
        mPersonalFinanceAdapter.notifyDataSetChanged();
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


    void sortDateAscending() {
        ArrayList<FinanceEntryPOJO> sortFinanceArrayList;
        if (yearMonthExpenseArrayListHashMap.containsKey(currentYear)) {
            if (yearMonthExpenseArrayListHashMap.get(currentYear).containsKey(currentMonth)) {
                sortFinanceArrayList = yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth);
                Collections.sort(sortFinanceArrayList, new Comparator<FinanceEntryPOJO>() {
                    DateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

                    @Override
                    public int compare(FinanceEntryPOJO o1, FinanceEntryPOJO o2) {
                        try {
                            return f.parse(o2.getDate()).compareTo(f.parse(o1.getDate()));
                        } catch (ParseException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                });
                yearMonthExpenseArrayListHashMap.get(currentYear).put(currentMonth, sortFinanceArrayList);
            }

        }
    }

    void sortDateDescending() {
        ArrayList<FinanceEntryPOJO> sortFinanceArrayList;
        if (yearMonthExpenseArrayListHashMap.containsKey(currentYear)) {
            if (yearMonthExpenseArrayListHashMap.get(currentYear).containsKey(currentMonth)) {
                sortFinanceArrayList = yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth);
                Collections.sort(sortFinanceArrayList, new Comparator<FinanceEntryPOJO>() {
                    DateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

                    @Override
                    public int compare(FinanceEntryPOJO o1, FinanceEntryPOJO o2) {
                        try {
                            return f.parse(o1.getDate()).compareTo(f.parse(o2.getDate()));
                        } catch (ParseException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                });
                yearMonthExpenseArrayListHashMap.get(currentYear).put(currentMonth, sortFinanceArrayList);
            }

        }
    }

    public class PersonalFinanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        public final static int VIEW_EXPENSE = 1;
        public final static int VIEW_INCOME = 2;
        private LayoutInflater inflator;

        public PersonalFinanceAdapter() {
            try {
                this.inflator = LayoutInflater.from(getActivity());
            } catch (NullPointerException e) {

            }
        }

        @Override
        public int getItemViewType(int position) {
            FinanceEntryPOJO expensePOJO = yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth).get(position);
            if (expensePOJO.getEntryType() == ExpenseTypes.ENTRY_TYPE_EXPENSE) {
                return VIEW_EXPENSE;
            } else if (expensePOJO.getEntryType() == ExpenseTypes.ENTRY_TYPE_INCOME) {
                return VIEW_INCOME;
            }
            return VIEW_EXPENSE;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
            if (theme == BasicSettings.LIGHT_THEME) {
                if (viewType == VIEW_EXPENSE) {
                    View view = inflator.inflate(R.layout.recyclerview_personal_finance_row_layout_light, parent, false);
                    return new PersonalFinanceViewHolder(view);
                } else if (viewType == VIEW_INCOME) {
                    View view = inflator.inflate(R.layout.recyclerview_personal_finance_income_layout_light, parent, false);
                    return new PersonalFinanceIncomeViewHolder(view);
                } else {
                    View view = inflator.inflate(R.layout.recyclerview_personal_finance_row_layout_light, parent, false);
                    return new PersonalFinanceViewHolder(view);
                }
            } else {
                if (viewType == VIEW_EXPENSE) {
                    View view = inflator.inflate(R.layout.recyclerview_personal_finance_row_layout, parent, false);
                    return new PersonalFinanceViewHolder(view);
                } else if (viewType == VIEW_INCOME) {
                    View view = inflator.inflate(R.layout.recyclerview_personal_finance_income_layout, parent, false);
                    return new PersonalFinanceIncomeViewHolder(view);
                } else {
                    View view = inflator.inflate(R.layout.recyclerview_personal_finance_row_layout, parent, false);
                    return new PersonalFinanceViewHolder(view);
                }
            }


        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == VIEW_EXPENSE) {
                FinanceEntryPOJO expensePOJO = yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth).get(position);
                String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
                ((PersonalFinanceViewHolder) holder).mCurrency.setText(currency);
                ((PersonalFinanceViewHolder) holder).description.setText(expensePOJO.getDescription());
                ((PersonalFinanceViewHolder) holder).mAmount.setText(String.valueOf(expensePOJO.getAmount()));
                ((PersonalFinanceViewHolder) holder).mDate.setText(expensePOJO.getDate());
                setCatIconBackground(holder, expensePOJO.getCategories());
                showCatIcon(holder, expensePOJO.getCategories());
            } else if (getItemViewType(position) == VIEW_INCOME) {
                FinanceEntryPOJO expensePOJO = yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth).get(position);
                String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
                ((PersonalFinanceIncomeViewHolder) holder).mCurrency.setText(currency);
                ((PersonalFinanceIncomeViewHolder) holder).description.setText(expensePOJO.getDescription());
                ((PersonalFinanceIncomeViewHolder) holder).mAmount.setText(String.valueOf(expensePOJO.getAmount()));
                ((PersonalFinanceIncomeViewHolder) holder).mDate.setText(expensePOJO.getDate());
                ((PersonalFinanceIncomeViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_green);
                ((PersonalFinanceIncomeViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2048);
            }

        }

        void setCatIconBackground(@NonNull RecyclerView.ViewHolder holder, Categories categories) {
            switch (categories.getColorId()) {
                case 1:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_red);
                    break;
                case 2:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_yellow);
                    break;
                case 3:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_blue);
                    break;
                case 4:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_green);
                    break;
                case 5:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_green_blue);
                    break;
                case 6:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_pink);
                    break;
                default:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_blue);
                    break;
            }
        }


        void showCatIcon(@NonNull RecyclerView.ViewHolder holder, Categories categories) {
            switch (categories.getCategoryIconId()) {
                case 2000:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2000);
                    break;
                case 2001:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2001);
                    break;
                case 2002:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2002);
                    break;
                case 2003:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2003);
                    break;
                case 2004:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2004);
                    break;
                case 2005:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2005);
                    break;
                case 2006:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2006);
                    break;
                case 2007:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2007);
                    break;

                case 2008:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2008);
                    break;

                case 2009:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2009);
                    break;

                case 2010:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2010);
                    break;

                case 2011:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2011);
                    break;

                case 2012:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2012);
                    break;

                case 2013:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2013);
                    break;

                case 2014:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2014);
                    break;

                case 2015:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2015);
                    break;

                case 2016:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2016);
                    break;

                case 2017:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2017);
                    break;

                case 2018:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2018);
                    break;

                case 2019:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2019);
                    break;

                case 2020:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2020);
                    break;

                case 2021:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2021);
                    break;

                case 2022:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2022);
                    break;

                case 2023:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2023);
                    break;

                case 2024:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2024);
                    break;

                case 2025:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2025);
                    break;

                case 2026:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2026);
                    break;

                case 2027:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2027);
                    break;

                case 2028:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2028);
                    break;

                case 2029:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2029);
                    break;

                case 2030:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2030);
                    break;
                case 2031:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2031);
                    break;
                case 2032:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2032);
                    break;
                case 2033:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2033);
                    break;
                case 2034:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2034);
                    break;
                case 2035:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2035);
                    break;
                case 2036:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2036);
                    break;
                case 2037:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2037);
                    break;
                case 2038:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2038);
                    break;
                case 2039:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2039);
                    break;
                case 2040:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2040);
                    break;
                case 2041:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2041);
                    break;
                case 2042:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2042);
                    break;
                case 2043:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2043);
                    break;
                case 2044:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2044);
                    break;
                case 2045:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2045);
                    break;
                case 2046:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2046);
                    break;
                case 2047:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2047);
                    break;
                default:
                    ((PersonalFinanceAdapter.PersonalFinanceViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2000);
                    break;
            }
        }


        void deleteExpense(int position) {
            FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "expenses").child(yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth).get(position).getEntryKey()).removeValue();
        }

        EditText mDescription;
        EditText mExpenseAmount;
        EditText mTransactionId;
        EditText mNote;
        Button mDoneExpenseEntry;

        void showEditExpenseDialog(int position) {
            final FinanceEntryPOJO expensePOJO = yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth).get(position);
            mEditExpenseDialog = new BottomSheetDialog(getActivity());
            int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
            if (theme == BasicSettings.LIGHT_THEME) {
                mEditExpenseDialog.setContentView(R.layout.dialog_edit_expense_layout_light);
            } else {
                mEditExpenseDialog.setContentView(R.layout.dialog_edit_expense_layout);
            }

            ImageButton mClose = mEditExpenseDialog.findViewById(R.id.close_edit_expense_dialog);
            mDescription = mEditExpenseDialog.findViewById(R.id.expense_description_edittext);
            mExpenseAmount = mEditExpenseDialog.findViewById(R.id.amount_edittext);
            mTransactionId = mEditExpenseDialog.findViewById(R.id.transaction_id_edittext);
            mNote = mEditExpenseDialog.findViewById(R.id.expense_note_edittext);
            mDoneExpenseEntry = mEditExpenseDialog.findViewById(R.id.done_expense_button);
            mDescription.setText(expensePOJO.getDescription());
            mExpenseAmount.setText(String.format(Locale.getDefault(), "%.2f", expensePOJO.getAmount()));
            mTransactionId.setText(expensePOJO.getTransactionId().trim());
            mNote.setText(expensePOJO.getNote().trim());

            mDoneExpenseEntry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String description = mDescription.getText().toString().trim();
                    String amount = mExpenseAmount.getText().toString().trim();
                    String transactionId = mTransactionId.getText().toString().trim();
                    String notes = mNote.getText().toString().trim();
                    if (description.isEmpty()) {
                        Toast.makeText(getActivity(), "Write Description", Toast.LENGTH_SHORT).show();
                    } else {
                        if (amount.isEmpty()) {
                            Toast.makeText(getActivity(), "Enter Amount", Toast.LENGTH_SHORT).show();
                        } else {
                            if (transactionId.isEmpty()) {
                                transactionId = " ";
                            }
                            if (notes.isEmpty()) {
                                notes = " ";
                            }

                            FinanceEntryPOJO expensePOJO1 = new FinanceEntryPOJO(expensePOJO.getEntryKey(), Double.valueOf(amount), description, expensePOJO.getDate(), notes, transactionId, expensePOJO.getYear(), expensePOJO.getMonth(), expensePOJO.getDay(), expensePOJO.getEntryType(), expensePOJO.getCategories(), userInfoPOJO);
                            FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "expenses").child(expensePOJO.getEntryKey()).setValue(expensePOJO1);

                            mEditExpenseDialog.dismiss();
                        }
                    }
                }
            });

            mClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditExpenseDialog.dismiss();
                }
            });


            mEditExpenseDialog.setCanceledOnTouchOutside(true);
            mEditExpenseDialog.show();
        }


        @Override
        public int getItemCount() {
            if (yearMonthExpenseArrayListHashMap.containsKey(currentYear)) {
                if (yearMonthExpenseArrayListHashMap.get(currentYear).containsKey(currentMonth)) {
                    return yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth).size();
                }
            }
            return 0;
        }

        public class PersonalFinanceViewHolder extends RecyclerView.ViewHolder {
            public TextView description, mAmount, mDate;
            public TextView mCurrency;
            public ImageButton mDeleteExpense;
            public ImageButton mCatIcon;

            public PersonalFinanceViewHolder(View itemView) {
                super(itemView);
                description = itemView.findViewById(R.id.expense_description_text_view);
                mAmount = itemView.findViewById(R.id.amount_textview);
                mDate = itemView.findViewById(R.id.expense_entry_date_textview_row);
                mCurrency = itemView.findViewById(R.id.expense_currency_textview);
                mDeleteExpense = itemView.findViewById(R.id.delete_expense_button);
                mCatIcon = itemView.findViewById(R.id.expense_cat_icon_finance_fragment);

                mDeleteExpense.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteExpense(getPosition());
                    }
                });
                description.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditExpenseDialog(getPosition());
                    }
                });
                mAmount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEditExpenseDialog(getPosition());
                    }
                });
                mDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEditExpenseDialog(getPosition());
                    }
                });
                mCurrency.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEditExpenseDialog(getPosition());
                    }
                });
                mCatIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEditExpenseDialog(getPosition());
                    }
                });


            }
        }

        public class PersonalFinanceIncomeViewHolder extends RecyclerView.ViewHolder {
            public TextView description, mAmount, mDate;
            public TextView mCurrency;
            public ImageButton mDeleteExpense;
            public ImageButton mCatIcon;

            public PersonalFinanceIncomeViewHolder(View itemView) {
                super(itemView);
                description = itemView.findViewById(R.id.expense_description_text_view);
                mAmount = itemView.findViewById(R.id.amount_textview);
                mDate = itemView.findViewById(R.id.expense_entry_date_textview_row);
                mCurrency = itemView.findViewById(R.id.positive_currency_textview);
                mDeleteExpense = itemView.findViewById(R.id.delete_expense_button);
                mCatIcon = itemView.findViewById(R.id.expense_cat_icon_finance_fragment);

                mDeleteExpense.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteExpense(getPosition());
                    }
                });

                description.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditExpenseDialog(getPosition());
                    }
                });
                mAmount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEditExpenseDialog(getPosition());
                    }
                });
                mDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEditExpenseDialog(getPosition());
                    }
                });
                mCurrency.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEditExpenseDialog(getPosition());
                    }
                });
                mCatIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEditExpenseDialog(getPosition());
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