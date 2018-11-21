package com.everyday.skara.everyday.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.PersonalFinancialBoardActivity;
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

public class FinanceExpensesFragment extends Fragment implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    ValueEventListener valueEventListener;


    TextView mTotalAmountSpent, mTotalAmountOwed, mCurencyTextView;
    double totalAmountSpent, totalAmountOwed;
    ArrayList<BoardExpensePOJO> boardExpensePOJOArrayList;
    ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList;

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

        mCurencyTextView = view.findViewById(R.id.currency_fiance_board_textview);
        String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
        mCurencyTextView.setText(currency);

        mTotalAmountSpent.setText(null);
        mTotalAmountOwed.setText(null);

        initBoardMembers();
    }

    void initBoardMembers() {
        boardMembersPOJOArrayList = new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference memberDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/members/");
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
        boardExpensePOJOArrayList = new ArrayList<>();
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + boardPOJO.getBoardKey() + "/expenses");
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
                                mTotalAmountSpent.setText(totalAmountSpent + "");

                            } else if ((boardExpensePOJO.getSplitType() == FinanceBoardExpense.EXPENSE_TYPE_SPECIFIC) || (boardExpensePOJO.getSplitType() == FinanceBoardExpense.EXPENSE_TYPE_EVERYONE)) {
                                double tempExpense = 0.0;
                                ArrayList<ExpenseMembersInfoPOJO> membersInfoPOJOS = boardExpensePOJO.getMemberInfoPojoList();
                                tempExpense = boardExpensePOJO.getAmount() / (membersInfoPOJOS.size() + 1);
                                for (int i = 0; i < membersInfoPOJOS.size(); i++) {
                                    if (!membersInfoPOJOS.get(i).isHasPaid()) {
                                        totalAmountOwed += tempExpense;
                                        mTotalAmountOwed.setText(totalAmountOwed + "");
                                    }
                                }
                                totalAmountSpent += tempExpense;
                                mTotalAmountSpent.setText(totalAmountSpent + "");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);
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

        }
    }
}