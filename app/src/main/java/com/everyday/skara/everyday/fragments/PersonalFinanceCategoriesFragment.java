package com.everyday.skara.everyday.fragments;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.Categories;
import com.everyday.skara.everyday.pojo.ExpensePOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class PersonalFinanceCategoriesFragment extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    UserInfoPOJO userInfoPOJO;
    ArrayList<ExpensePOJO> expensePOJOArrayList;
    RecyclerView mPersonalFinanceRecyclerView;
    PersonalFinanceAdapter mPersonalFinanceAdapter;
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

    Double totalExpense = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_personal_finance_layout, container, false);
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
        mCurencyTextView = view.findViewById(R.id.currency_textview);
        String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
        mCurencyTextView.setText(currency);
        expensePOJOArrayList = new ArrayList<>();

        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        currentMonth = Calendar.getInstance().get(Calendar.MONTH);

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
        mMonthBottomSheetDialog.setCanceledOnTouchOutside(false);
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

    void initCategories(){
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

        mMonthSelectionButton.setTitle(String.valueOf(currentMonth));
        mPersonalFinanceRecyclerView.invalidate();
        mPersonalFinanceAdapter = new PersonalFinanceAdapter();
        mPersonalFinanceRecyclerView.setAdapter(mPersonalFinanceAdapter);
        mPersonalFinanceAdapter.notifyDataSetChanged();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mExpenseChildEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseChildEventListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mExpenseChildEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseChildEventListener);
        }
    }


    public class PersonalFinanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;

        public PersonalFinanceAdapter() {
            try {
                this.inflator = LayoutInflater.from(getActivity());
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_categories_row_layout, parent, false);
            return new PersonalFinanceAdapter.PersonalFinanceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ArrayList<ExpensePOJO> expensePOJOArrayList = new ArrayList<>();
            double total = 0.0;
            Categories categories = categoriesArrayList.get(position);
            if(catYearMonthExpenseArrayListHashMap.containsKey(categories.getCategoryKey())){
                if(catYearMonthExpenseArrayListHashMap.get(categories.getCategoryKey()).containsKey(currentYear)){
                    if(catYearMonthExpenseArrayListHashMap.get(categories.getCategoryKey()).get(currentYear).containsKey(currentMonth)){
                        expensePOJOArrayList = catYearMonthExpenseArrayListHashMap.get(categories.getCategoryKey()).get(currentYear).get(currentMonth);
                        for(int i = 0; i < expensePOJOArrayList.size(); i++){
                            total += expensePOJOArrayList.get(i).getAmount();
                        }

                    }
                }
            }
            ((PersonalFinanceViewHolder)holder).mCatName.setText(categories.getCategoryName());
            ((PersonalFinanceViewHolder)holder).mTotal.setText(String.format(Locale.getDefault(), "%.2f", total));
            totalExpense += total;
            mTotalExpenseTextView.setText(String.format(Locale.getDefault(), "%.2f", totalExpense));
        }

        @Override
        public int getItemCount() {
            return categoriesArrayList.size();
        }

        public class PersonalFinanceViewHolder extends RecyclerView.ViewHolder {
            public TextView mCatName, mTotal;

            public PersonalFinanceViewHolder(View itemView) {
                super(itemView);
                mCatName = itemView.findViewById(R.id.cat_name_text_view_recyclerview);
                mTotal = itemView.findViewById(R.id.total_amount_cat_recyclerview);
            }
        }

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