package com.everyday.skara.everyday.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.FinanceBoardExpense;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.BoardExpensePOJO;
import com.everyday.skara.everyday.pojo.BoardMembersPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.ExpenseMembersInfoPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class FinanceExpensesFragment extends Fragment implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    ValueEventListener valueEventListener;


    TextView mTotalAmountSpent, mTotalAmountOwed, mTotalAmountOwing, mCurencyTextView;
    double totalAmountSpent, totalAmountOwed, totalAmountOwing;
    ArrayList<BoardExpensePOJO> boardExpensePOJOArrayList;
    ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList;
    public static ArrayList<BoardExpensePOJO> mPersonalExpensesArrayList, mSharedExpensesArrayList, mOtherExpensesArrayList;

    Button mPersonalViewButton, mSharedViewButton, mOtherViewButton;
    View view;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.finance_board_expenses_layout, container, false);
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
        boardPOJO = (BoardPOJO) getArguments().getSerializable("board_pojo");
        userInfoPOJO = (UserInfoPOJO) getArguments().getSerializable("user_profile");

        mTotalAmountSpent = view.findViewById(R.id.total_amount_spent_finance_board_textview);
        mTotalAmountOwed = view.findViewById(R.id.total_amount_owed_finance_board_textview);
        mTotalAmountOwing = view.findViewById(R.id.total_amount_owing_finance_board_textview);

        mPersonalViewButton = view.findViewById(R.id.personal_expenses_view_button);
        mSharedViewButton = view.findViewById(R.id.shared_expenses_view_button);
        mOtherViewButton = view.findViewById(R.id.other_expenses_view_button);

        mCurencyTextView = view.findViewById(R.id.currency_fiance_board_textview);
        String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
        mCurencyTextView.setText(currency);


        mPersonalViewButton.setOnClickListener(this);
        mSharedViewButton.setOnClickListener(this);
        mOtherViewButton.setOnClickListener(this);

        mTotalAmountSpent.setText("-");
        mTotalAmountOwed.setText("-");
        mTotalAmountOwing.setText("-");

        initBoardMembers();
    }

    void initBoardMembers() {
        boardMembersPOJOArrayList = new ArrayList<>();

        mPersonalExpensesArrayList = new ArrayList<>();
        mSharedExpensesArrayList = new ArrayList<>();
        mOtherExpensesArrayList = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference memberDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/members/");
        memberDatabaseReference.keepSynced(true);
        memberDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        boardMembersPOJOArrayList.add(snapshot.getValue(BoardMembersPOJO.class));
                    }
                    initExpenses();
                } else {
                    initExpenses();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void initExpenses() {
        totalAmountSpent = 0.0;
        totalAmountOwed = 0.0;
        totalAmountOwing = 0.0;
        boardExpensePOJOArrayList = new ArrayList<>();
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/expenses");
        databaseReference.keepSynced(true);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        BoardExpensePOJO boardExpensePOJO = snapshot.getValue(BoardExpensePOJO.class);

                        // Created by the user
                        if (boardExpensePOJO.getUserInfoPOJO().getUser_key().equals(userInfoPOJO.getUser_key())) {
                            if (boardExpensePOJO.getSplitType() == FinanceBoardExpense.EXPENSE_TYPE_PERSONAL) {
                                boardExpensePOJOArrayList.add(boardExpensePOJO);
                                totalAmountSpent += boardExpensePOJO.getAmount();
                                mTotalAmountSpent.setText(String.format(Locale.getDefault(), "%.2f", totalAmountSpent));

                                mPersonalExpensesArrayList.add(boardExpensePOJO);

                            } else if ((boardExpensePOJO.getSplitType() == FinanceBoardExpense.EXPENSE_TYPE_SPECIFIC) || (boardExpensePOJO.getSplitType() == FinanceBoardExpense.EXPENSE_TYPE_EVERYONE)) {
                                double tempExpense = 0.0;
                                ArrayList<ExpenseMembersInfoPOJO> membersInfoPOJOS = boardExpensePOJO.getMemberInfoPojoList();
                                if (membersInfoPOJOS == null) {
                                    membersInfoPOJOS = new ArrayList<>();
                                }
                                tempExpense = boardExpensePOJO.getAmount() / (membersInfoPOJOS.size() + 1);
                                for (int i = 0; i < membersInfoPOJOS.size(); i++) {
                                    if (!membersInfoPOJOS.get(i).isHasPaid()) {
                                        totalAmountOwed += tempExpense;
                                        mTotalAmountOwed.setText(String.format(Locale.getDefault(), "%.2f", totalAmountOwed));
                                    }
                                }
                                totalAmountSpent += tempExpense;
                                mTotalAmountSpent.setText(String.format(Locale.getDefault(), "%.2f", totalAmountSpent));

                                mSharedExpensesArrayList.add(boardExpensePOJO);
                            }
                        } else {
                            ArrayList<ExpenseMembersInfoPOJO> expenseMembersInfoPOJOArrayList = boardExpensePOJO.getMemberInfoPojoList();
                            if (expenseMembersInfoPOJOArrayList == null) {
                                expenseMembersInfoPOJOArrayList = new ArrayList<>();
                            }
                            if (expenseMembersInfoPOJOArrayList.size() > 0) {

                                for (int i = 0; i < expenseMembersInfoPOJOArrayList.size(); i++) {
                                    if (expenseMembersInfoPOJOArrayList.get(i).getUserInfoPOJO().getUserInfoPOJO().getUser_key().equals(userInfoPOJO.getUser_key())) {
                                        if (!expenseMembersInfoPOJOArrayList.get(i).isHasPaid()) {
                                            double tempExpense = 0.0;
                                            tempExpense = boardExpensePOJO.getAmount() / (expenseMembersInfoPOJOArrayList.size() + 1);
                                            totalAmountOwing += tempExpense;
                                            mTotalAmountOwing.setText(String.format(Locale.getDefault(), "%.2f",totalAmountOwing));
                                            mOtherExpensesArrayList.add(boardExpensePOJO);
                                            break;
                                        }
                                    }
                                }
                            }

                        }

                    }
                }
                initChildExpensesFragment();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);

    }

    void initChildExpensesFragment() {
        ExpensesChildFragement expensesChildFragement = new ExpensesChildFragement();

        Bundle bundle = new Bundle();
        bundle.putSerializable("board_pojo", boardPOJO);
        bundle.putSerializable("user_profile", userInfoPOJO);
        expensesChildFragement.setArguments(bundle);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        transaction.replace(R.id.expenses_detail_fragment_container, expensesChildFragement);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }

    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.personal_expenses_view_button:
                initChildExpensesFragment();
                break;
            case R.id.shared_expenses_view_button:
                initSharedExpenses();
                break;
            case R.id.other_expenses_view_button:
                initOtherExpenses();
                break;

        }
    }

    void initSharedExpenses() {
        ExpensesSharedChildFragement expensesChildFragement = new ExpensesSharedChildFragement();

        Bundle bundle = new Bundle();
        bundle.putSerializable("board_pojo", boardPOJO);
        bundle.putSerializable("user_profile", userInfoPOJO);
        expensesChildFragement.setArguments(bundle);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        transaction.replace(R.id.expenses_detail_fragment_container, expensesChildFragement);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    void initOtherExpenses() {
        ExpensesOtherFragement expensesChildFragement = new ExpensesOtherFragement();

        Bundle bundle = new Bundle();
        bundle.putSerializable("board_pojo", boardPOJO);
        bundle.putSerializable("user_profile", userInfoPOJO);
        expensesChildFragement.setArguments(bundle);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        transaction.replace(R.id.expenses_detail_fragment_container, expensesChildFragement);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    /**
     * -----------------------------------------------------------------------
     * Personal expenses view
     * -----------------------------------------------------------------------
     */
    public static class ExpensesChildFragement extends Fragment {
        RecyclerView mExpensesChildRecyclerview;
        View expenseFragmentView;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            expenseFragmentView = inflater.inflate(R.layout.expenses_view_child_fragment_layout, container, false);
            return expenseFragmentView;
        }

        @Override
        public void onStart() {
            super.onStart();
            initExpensesChildFragment();
        }

        void initExpensesChildFragment() {
            mExpensesChildRecyclerview = expenseFragmentView.findViewById(R.id.expenses_view_recyclerview);
            mExpensesChildRecyclerview.invalidate();
            mExpensesChildRecyclerview.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            mExpensesChildRecyclerview.setLayoutManager(linearLayoutManager);
            ExpensesChildAdapter expensesChildAdapter = new ExpensesChildAdapter();
            mExpensesChildRecyclerview.setAdapter(expensesChildAdapter);
        }

        public class ExpensesChildAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
            private LayoutInflater inflator;

            public ExpensesChildAdapter() {
                try {
                    this.inflator = LayoutInflater.from(getActivity());
                } catch (NullPointerException e) {

                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = inflator.inflate(R.layout.recyclerview_expenses_view_child_fragment, parent, false);
                return new ExpensesChildViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                BoardExpensePOJO boardExpensePOJO = mPersonalExpensesArrayList.get(position);
                ((ExpensesChildViewHolder) holder).mDescription.setText(boardExpensePOJO.getDescription());
                ((ExpensesChildViewHolder) holder).mAmount.setText(String.format(Locale.getDefault(), "%.2f",boardExpensePOJO.getAmount()));
                ((ExpensesChildViewHolder) holder).mDate.setText(boardExpensePOJO.getDate());
            }

            @Override
            public int getItemCount() {
                return mPersonalExpensesArrayList.size();
            }


            public class ExpensesChildViewHolder extends RecyclerView.ViewHolder {
                public TextView mAmount, mDescription, mDate;

                public ExpensesChildViewHolder(View itemView) {
                    super(itemView);
                    mDescription = itemView.findViewById(R.id.expenses_view_desc);
                    mAmount = itemView.findViewById(R.id.expenses_view_amount);
                    mDate = itemView.findViewById(R.id.expense_view_date);
                }
            }
        }
    }


    /**
     * -----------------------------------------------------------------------
     * Shared expenses view
     * -----------------------------------------------------------------------
     */

    public static class ExpensesSharedChildFragement extends Fragment {
        RecyclerView mExpensesChildRecyclerview;
        View expenseFragmentView;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            expenseFragmentView = inflater.inflate(R.layout.expenses_shared_child_fragment_layout, container, false);
            return expenseFragmentView;
        }

        @Override
        public void onStart() {
            super.onStart();
            initExpensesChildFragment();
        }

        void initExpensesChildFragment() {
            mExpensesChildRecyclerview = expenseFragmentView.findViewById(R.id.expenses_shared_view_recyclerview);

            mExpensesChildRecyclerview.invalidate();
            mExpensesChildRecyclerview.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            mExpensesChildRecyclerview.setLayoutManager(linearLayoutManager);
            ExpensesSharedAdapter expensesChildAdapter = new ExpensesSharedAdapter();
            mExpensesChildRecyclerview.setAdapter(expensesChildAdapter);
        }

        public class ExpensesSharedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
            private LayoutInflater inflator;

            public ExpensesSharedAdapter() {
                try {
                    this.inflator = LayoutInflater.from(getActivity());
                } catch (NullPointerException e) {

                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = inflator.inflate(R.layout.recyclerview_expenses_shared_view_child_row_layout, parent, false);
                return new ExpensesSharedViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                BoardExpensePOJO boardExpensePOJO = mSharedExpensesArrayList.get(position);
                ((ExpensesSharedViewHolder) holder).mDescription.setText(boardExpensePOJO.getDescription());
                ((ExpensesSharedViewHolder) holder).mAmount.setText(String.format(Locale.getDefault(), "%.2f",boardExpensePOJO.getAmount()));
            }

            @Override
            public int getItemCount() {
                return mSharedExpensesArrayList.size();
            }


            public class ExpensesSharedViewHolder extends RecyclerView.ViewHolder {
                public TextView mAmount, mDescription;

                public ExpensesSharedViewHolder(View itemView) {
                    super(itemView);
                    mDescription = itemView.findViewById(R.id.expenses_shared_view_desc);
                    mAmount = itemView.findViewById(R.id.expenses_shared_view_amount);
                }
            }
        }
    }

    /**
     * -----------------------------------------------------------------------
     * Other expenses view
     * -----------------------------------------------------------------------
     */
    public static class ExpensesOtherFragement extends Fragment {
        RecyclerView mExpensesChildRecyclerview;
        View expenseFragmentView;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            expenseFragmentView = inflater.inflate(R.layout.expenses_other_view_child_fragment_layout, container, false);
            return expenseFragmentView;
        }

        @Override
        public void onStart() {
            super.onStart();
            initExpensesChildFragment();
        }

        void initExpensesChildFragment() {
            mExpensesChildRecyclerview = expenseFragmentView.findViewById(R.id.expenses_others_view_recyclerview);

            mExpensesChildRecyclerview.invalidate();
            mExpensesChildRecyclerview.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            mExpensesChildRecyclerview.setLayoutManager(linearLayoutManager);
            ExpensesOthersChildAdapter expensesChildAdapter = new ExpensesOthersChildAdapter();
            mExpensesChildRecyclerview.setAdapter(expensesChildAdapter);
        }

        public class ExpensesOthersChildAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
            private LayoutInflater inflator;

            public ExpensesOthersChildAdapter() {
                try {
                    this.inflator = LayoutInflater.from(getActivity());
                } catch (NullPointerException e) {

                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = inflator.inflate(R.layout.recyclerview_expenses_others_child_fragment, parent, false);
                return new ExpensesChildViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                BoardExpensePOJO boardExpensePOJO = mOtherExpensesArrayList.get(position);
                ((ExpensesChildViewHolder) holder).mDescription.setText(boardExpensePOJO.getDescription());
                ((ExpensesChildViewHolder) holder).mAmount.setText(String.format(Locale.getDefault(), "%.2f",boardExpensePOJO.getAmount()));
            }

            @Override
            public int getItemCount() {
                return mOtherExpensesArrayList.size();
            }


            public class ExpensesChildViewHolder extends RecyclerView.ViewHolder {
                public TextView mAmount, mDescription;

                public ExpensesChildViewHolder(View itemView) {
                    super(itemView);

                    mDescription = itemView.findViewById(R.id.expenses_others_view_desc);
                    mAmount = itemView.findViewById(R.id.expenses_others_view_amount);
                }
            }
        }
    }
}