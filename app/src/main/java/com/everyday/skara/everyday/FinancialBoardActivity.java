package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.ExpenseType;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.NewOptionTypes;
import com.everyday.skara.everyday.pojo.BoardMembersPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
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

public class FinancialBoardActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    ArrayList<ExpensePOJO> expensePOJOArrayList;
    ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList;
    int memberCount = -1;
    ChildEventListener mExpenseChildEventListener;
    DatabaseReference mExpensesDatabaseReference;

    ArrayList<ExpensePOJO> personalExpenses;
    ArrayList<ExpensePOJO> equalExpense;
    ArrayList<ExpensePOJO> specificExpense;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_board);
        Toolbar myToolbar = findViewById(R.id.financial_board_toolbar);
        setSupportActionBar(myToolbar);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }

    }

    void init() {
        Intent intent = getIntent();
        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        personalExpenses = new ArrayList<>();
        equalExpense = new ArrayList<>();
        specificExpense = new ArrayList<>();

        initMembers();

    }
    void initMembers(){
        boardMembersPOJOArrayList = new ArrayList<>();
        DatabaseReference membersDatabaseReference = firebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/members");
        membersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        boardMembersPOJOArrayList.add(snapshot.getValue(BoardMembersPOJO.class));
                    }
                    memberCount = boardMembersPOJOArrayList.size();
                }else{
                    memberCount = -1;
                }
                initExpenses();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    void initExpenses(){
        mExpensesDatabaseReference = firebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/expenses");
        mExpenseChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ExpensePOJO expensePOJO = dataSnapshot.getValue(ExpensePOJO.class);
                expensePOJOArrayList.add(expensePOJO);

                if(expensePOJO.getUserInfoPOJO().getUser_key().equals(userInfoPOJO.getUser_key()) && expensePOJO.getExpenseType() == ExpenseType.TYPE_PERSONAL){
                    personalExpenses.add(expensePOJO);
                }
                else if(expensePOJO.getExpenseType() == ExpenseType.TYPE_SHARED_EVERYONE){
                    equalExpense.add(expensePOJO);
                }
                else if(expensePOJO.getExpenseType() == ExpenseType.TYPE_SPECIFIC_PEOPLE){
                    specificExpense.add(expensePOJO);
                }
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
    protected void onStop() {
        super.onStop();
        if (mExpenseChildEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseChildEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExpenseChildEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseChildEventListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_financial_board_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_new_expense_menu_item:
                toNewExpenseActivity();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    void toNewExpenseActivity(){
        Intent intent = new Intent(FinancialBoardActivity.this, NewExpenseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toLoginActivity() {
        Intent intent = new Intent(FinancialBoardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
