package com.everyday.skara.everyday.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.everyday.skara.everyday.pojo.ExpensePOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class PersonalFinanceFragment extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    UserInfoPOJO userInfoPOJO;
    ArrayList<ExpensePOJO> expensePOJOArrayList;
    RecyclerView mPersonalFinanceRecyclerView;
    PersonalFinanceAdapter mPersonalFinanceAdapter;
    ChildEventListener mExpenseChildEventListener;
    DatabaseReference mExpensesDatabaseReference;

    BottomSheetDialog mMonthBottomSheetDialog;

    HashMap<String, ArrayList<ExpensePOJO>> dateExpenseArrayListHashMap;
    HashMap<Integer, HashMap<Integer, ArrayList<ExpensePOJO>>> yearMonthExpenseArrayListHashMap;

    Button mMonthSelectionButton;
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

        dateExpenseArrayListHashMap = new HashMap<>();
        yearMonthExpenseArrayListHashMap = new HashMap<>();

        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        currentMonth = Calendar.getInstance().get(Calendar.MONTH);

        mTotalExpenseTextView.setText("0.00");

        mMonthSelectionButton.setText(String.valueOf(currentMonth));

        mMonthSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthSelectionDialog();
            }
        });

        initFinanceRecyclerView();

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
        mPersonalFinanceAdapter = new PersonalFinanceAdapter(new ArrayList<ExpensePOJO>());
        mPersonalFinanceRecyclerView.setAdapter(mPersonalFinanceAdapter);

        initExpenses();
    }

    void initExpenses() {
        expensePOJOArrayList = new ArrayList<>();

        dateExpenseArrayListHashMap = new HashMap<>();
        yearMonthExpenseArrayListHashMap = new HashMap<>();


        mExpensesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/expenses");
        mExpensesDatabaseReference.keepSynced(true);
        mExpenseChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ExpensePOJO expensePOJO = dataSnapshot.getValue(ExpensePOJO.class);
                expensePOJOArrayList.add(expensePOJO);

                // expense year and month wise
                if (yearMonthExpenseArrayListHashMap.containsKey(expensePOJO.getYear())) {
                    if (yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                        ArrayList<ExpensePOJO> expensePOJOArrayList2 = yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth());
                        expensePOJOArrayList2.add(expensePOJO);
                        yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList2);
                    } else {
                        ArrayList<ExpensePOJO> expensePOJOArrayList2 = new ArrayList<>();
                        expensePOJOArrayList2.add(expensePOJO);
                        yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList2);
                    }
                } else {
                    HashMap<Integer, ArrayList<ExpensePOJO>> monthHashMap = new HashMap<>();
                    ArrayList<ExpensePOJO> expensePOJOArrayListTemp = new ArrayList<>();
                    expensePOJOArrayListTemp.add(expensePOJO);
                    monthHashMap.put(expensePOJO.getMonth(), expensePOJOArrayListTemp);
                    yearMonthExpenseArrayListHashMap.put(expensePOJO.getYear(), monthHashMap);
                }

                // expenses date wise
                if (dateExpenseArrayListHashMap.containsKey(expensePOJO.getDate())) {
                    ArrayList<ExpensePOJO> expensePOJOArrayList1 = dateExpenseArrayListHashMap.get(expensePOJO.getDate());
                    expensePOJOArrayList1.add(expensePOJO);
                    dateExpenseArrayListHashMap.put(expensePOJO.getDate(), expensePOJOArrayList1);
                } else {
                    ArrayList<ExpensePOJO> expensePOJOArrayList1 = new ArrayList<>();
                    expensePOJOArrayList1.add(expensePOJO);
                    dateExpenseArrayListHashMap.put(expensePOJO.getDate(), expensePOJOArrayList1);
                }

                // reflect updated data
                updateExpenses();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                ExpensePOJO expensePOJO = dataSnapshot.getValue(ExpensePOJO.class);

                expensePOJOArrayList.remove(expensePOJO);

                // expense year and month wise
                if (yearMonthExpenseArrayListHashMap.containsKey(expensePOJO.getYear())) {
                    if (yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                        ArrayList<ExpensePOJO> expensePOJOArrayList2 = yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth());
                        expensePOJOArrayList2.remove(expensePOJO);
                        if(expensePOJOArrayList2.size() == 0){
                            yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).remove(expensePOJO.getMonth());
                        }else {
                            yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList2);
                        }
                    }
                }

                // expenses date wise
                if (dateExpenseArrayListHashMap.containsKey(expensePOJO.getDate())) {
                    ArrayList<ExpensePOJO> expensePOJOArrayList1 = dateExpenseArrayListHashMap.get(expensePOJO.getDate());
                    expensePOJOArrayList1.remove(expensePOJO);
                    if(expensePOJOArrayList1.size() == 0){
                        dateExpenseArrayListHashMap.remove(expensePOJO.getDate());
                    }else {
                        dateExpenseArrayListHashMap.put(expensePOJO.getDate(), expensePOJOArrayList1);
                    }
                }
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

        mMonthSelectionButton.setText(String.valueOf(currentMonth));

        // check if the year is present in hashmap
        if (yearMonthExpenseArrayListHashMap.containsKey(currentYear)) {
            // check if the month from the year is present
            if (yearMonthExpenseArrayListHashMap.get(currentYear).containsKey(currentMonth)) {
                // update the recyclerview with the updated expenses array HashMap
                mPersonalFinanceRecyclerView.invalidate();
                mPersonalFinanceAdapter = new PersonalFinanceAdapter(yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth));
                mPersonalFinanceRecyclerView.setAdapter(mPersonalFinanceAdapter);
                mPersonalFinanceAdapter.notifyDataSetChanged();
            } else {
                mPersonalFinanceRecyclerView.invalidate();
                mPersonalFinanceAdapter = new PersonalFinanceAdapter(new ArrayList<ExpensePOJO>());
                mPersonalFinanceRecyclerView.setAdapter(mPersonalFinanceAdapter);
                mPersonalFinanceAdapter.notifyDataSetChanged();
            }
        } else {
            mPersonalFinanceRecyclerView.invalidate();
            mPersonalFinanceAdapter = new PersonalFinanceAdapter(new ArrayList<ExpensePOJO>());
            mPersonalFinanceRecyclerView.setAdapter(mPersonalFinanceAdapter);
            mPersonalFinanceAdapter.notifyDataSetChanged();
        }

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
        ArrayList<ExpensePOJO> expensePOJOArrayList;

        public PersonalFinanceAdapter(ArrayList<ExpensePOJO> expensePOJOSArrayList) {
            try {
                this.inflator = LayoutInflater.from(getActivity());
                totalExpense = 0.0;
                mTotalExpenseTextView.setText(String.format(Locale.getDefault(), "%.2f", totalExpense));
                this.expensePOJOArrayList = expensePOJOSArrayList;
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_personal_finance_row_layout, parent, false);
            return new PersonalFinanceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ExpensePOJO expensePOJO = this.expensePOJOArrayList.get(position);
            totalExpense += expensePOJO.getAmount();
            mTotalExpenseTextView.setText(String.format(Locale.getDefault(), "%.2f", totalExpense));
            ((PersonalFinanceViewHolder) holder).description.setText(expensePOJO.getDescription());
            ((PersonalFinanceViewHolder) holder).mAmount.setText(String.valueOf(expensePOJO.getAmount()));
            ((PersonalFinanceViewHolder) holder).mDate.setText(expensePOJO.getDate());
            ((PersonalFinanceViewHolder) holder).mTransactionId.setText(expensePOJO.getTransactionId());
            ((PersonalFinanceViewHolder) holder).mNote.setText(expensePOJO.getNote());
        }

        void deleteExpense(int position) {
            totalExpense -= this.expensePOJOArrayList.get(position).getAmount();
            firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/expenses/" + this.expensePOJOArrayList.get(position).getEntryKey()).removeValue();
            this.expensePOJOArrayList.remove(position);
            mTotalExpenseTextView.setText(String.format(Locale.getDefault(),"%.2f", totalExpense));
            notifyItemRemoved(position);
        }


        @Override
        public int getItemCount() {
            return this.expensePOJOArrayList.size();
        }

        public class PersonalFinanceViewHolder extends RecyclerView.ViewHolder {
            public TextView description, mAmount, mDate, mTransactionId, mNote;
            public Button mDeleteExpense;

            public PersonalFinanceViewHolder(View itemView) {
                super(itemView);
                description = itemView.findViewById(R.id.expense_description_text_view);
                mAmount = itemView.findViewById(R.id.amount_textview);
                mDate = itemView.findViewById(R.id.expense_entry_date_textview_row);
                mTransactionId = itemView.findViewById(R.id.transaction_id_textview);
                mNote = itemView.findViewById(R.id.expense_note_textview);

                mDeleteExpense = itemView.findViewById(R.id.delete_expense_button);
                mDeleteExpense.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteExpense(getPosition());
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
