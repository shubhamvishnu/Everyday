package com.everyday.skara.everyday;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.everyday.skara.everyday.classes.NewOptionTypes;
import com.everyday.skara.everyday.fragments.PersonalFinanceAnalytics;
import com.everyday.skara.everyday.fragments.PersonalFinanceCategoriesFragment;
import com.everyday.skara.everyday.fragments.PersonalFinanceDayFragment;
import com.everyday.skara.everyday.fragments.PersonalFinanceFragment;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class PersonalFinancialBoardActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    UserInfoPOJO userInfoPOJO;
    public static int optionType;
    public static int mViewCurrentYear, mViewCurrentMonth;
    ImageButton mExpenses, mCatExpenses, mDayExpenses, mExpenseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_financial_board);
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
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");
        optionType = NewOptionTypes.TYPE_PERSONAL_EXPENSE;

        mViewCurrentYear = Calendar.getInstance().get(Calendar.YEAR);
        mViewCurrentMonth = Calendar.getInstance().get(Calendar.MONTH);

        mExpenses = findViewById(R.id.expenses_option_icon);
        mCatExpenses = findViewById(R.id.category_expenses_icon);
        mDayExpenses = findViewById(R.id.day_wise_expense_option);
        mExpenseAnalytics = findViewById(R.id.expense_analytics_item);
        mExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionType = NewOptionTypes.TYPE_PERSONAL_EXPENSE;
                clearBackStack();
                PersonalFinanceFragment personalFinanceFragment = new PersonalFinanceFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                personalFinanceFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.financial_fragment_container, personalFinanceFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });
        mCatExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBackStack();

                optionType = NewOptionTypes.TYPE_PERSONAL_CAT_EXPENSE;
                PersonalFinanceCategoriesFragment personalFinanceCategoriesFragment = new PersonalFinanceCategoriesFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                personalFinanceCategoriesFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.financial_fragment_container, personalFinanceCategoriesFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });
        mDayExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBackStack();

                optionType = NewOptionTypes.TYPE_PERSONAL_DAY_EXPENSE;
                PersonalFinanceDayFragment personalFinanceDayFragment = new PersonalFinanceDayFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                personalFinanceDayFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.financial_fragment_container, personalFinanceDayFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });
        mExpenseAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBackStack();

                optionType = NewOptionTypes.TYPE_PERSONAL_ANALYTICS_;
                PersonalFinanceAnalytics personalFinanceAnalytics = new PersonalFinanceAnalytics();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                personalFinanceAnalytics.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.financial_fragment_container, personalFinanceAnalytics);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });
        initFragment();
    }
    void clearBackStack(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    void initFragment() {
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.financial_fragment_container) != null) {
            if (optionType == NewOptionTypes.TYPE_PERSONAL_EXPENSE) {
                clearBackStack();
                PersonalFinanceFragment personalFinanceFragment = new PersonalFinanceFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                personalFinanceFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.financial_fragment_container, personalFinanceFragment).commit();
            } else if (optionType == NewOptionTypes.TYPE_PERSONAL_CAT_EXPENSE) {
                clearBackStack();
                PersonalFinanceCategoriesFragment personalFinanceCategoriesFragment = new PersonalFinanceCategoriesFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                personalFinanceCategoriesFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.financial_fragment_container, personalFinanceCategoriesFragment).commit();
            } else if (optionType == NewOptionTypes.TYPE_PERSONAL_DAY_EXPENSE) {
                clearBackStack();
                PersonalFinanceDayFragment personalFinanceDayFragment = new PersonalFinanceDayFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                personalFinanceDayFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.financial_fragment_container, personalFinanceDayFragment).commit();
            } else if (optionType == NewOptionTypes.TYPE_PERSONAL_ANALYTICS_) {
                clearBackStack();
                PersonalFinanceAnalytics personalFinanceAnalytics = new PersonalFinanceAnalytics();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                personalFinanceAnalytics.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.financial_fragment_container, personalFinanceAnalytics).commit();
            }

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
            case R.id.add_new_entry_menu_item:
                chooseEntryTypeBoard();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    void chooseEntryTypeBoard() {
        final Dialog mEntryTypeDialog = new BottomSheetDialog(this);
        mEntryTypeDialog.setContentView(R.layout.dialog_financial_entry_type_option_layout);
        Button mIncomeType, mExpenseType;
        mIncomeType = mEntryTypeDialog.findViewById(R.id.income_type_button);
        mExpenseType = mEntryTypeDialog.findViewById(R.id.expense_type_button);

        ImageButton mClose = mEntryTypeDialog.findViewById(R.id.close_entry_option_dialog);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEntryTypeDialog.dismiss();
            }
        });

        mIncomeType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEntryTypeDialog.dismiss();
                toNewIncomeActivity();
            }
        });
        mExpenseType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEntryTypeDialog.dismiss();
                toNewExpenseActivity();
            }
        });

        mEntryTypeDialog.setCanceledOnTouchOutside(false);
        mEntryTypeDialog.show();
    }

    void toNewIncomeActivity() {
        Intent intent = new Intent(PersonalFinancialBoardActivity.this, NewIncomeExpenseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toNewExpenseActivity() {
        Intent intent = new Intent(PersonalFinancialBoardActivity.this, NewExpenseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toLoginActivity() {
        Intent intent = new Intent(PersonalFinancialBoardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        toBoardActivity();
    }

    void toBoardActivity() {
        Intent intent = new Intent(PersonalFinancialBoardActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
