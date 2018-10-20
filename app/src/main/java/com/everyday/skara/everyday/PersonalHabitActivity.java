package com.everyday.skara.everyday;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.NewOptionTypes;
import com.everyday.skara.everyday.classes.NotificationTypes;
import com.everyday.skara.everyday.fragments.HabitsFragment;
import com.everyday.skara.everyday.fragments.PersonalGratitudeEntriesFragment;
import com.everyday.skara.everyday.pojo.GratitudePOJO;
import com.everyday.skara.everyday.pojo.HabitPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PersonalHabitActivity extends AppCompatActivity implements com.philliphsu.bottomsheetpickers.date.DatePickerDialog.OnDateSetListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    UserInfoPOJO userInfoPOJO;
    BottomSheetDialog mNewEntryDialog;
    public static int optionType;
    ImageButton mHabitEntriesButton;
    boolean isStartSelected = true;
    String mStartDateValue, mEndDateValue;
    int mStartDay, mStartMonth, mStartYear;
    int mEndDay, mEndMonth, mEndYear;
    TextView mStartDateTextView, mEndDateTextView;
    public static FragmentManager persoanlHabitFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_habit);
        Toolbar myToolbar = findViewById(R.id.habit_board_toolbar);
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
        optionType = NewOptionTypes.TYPE_PERSONAL_HABIT_ENTRIES;

        persoanlHabitFragmentManager = getSupportFragmentManager();
        mStartDateValue = new String("");
        mEndDateValue = new String("");
        mStartDateValue = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        mEndDateValue = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        mStartDay = mEndDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        mStartMonth = mEndMonth = Calendar.getInstance().get(Calendar.MONTH);
        mStartYear = mEndYear = Calendar.getInstance().get(Calendar.YEAR);

        mHabitEntriesButton = findViewById(R.id.habit_entry_option_icon);
        mHabitEntriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                persoanlHabitFragmentManager = getSupportFragmentManager();
                optionType = NewOptionTypes.TYPE_PERSONAL_HABIT_ENTRIES;
                HabitsFragment habitsFragment = new HabitsFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                habitsFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.habit_fragment_container, habitsFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });
        initFragment();

    }

    void initFragment() {
        persoanlHabitFragmentManager = getSupportFragmentManager();
        optionType = NewOptionTypes.TYPE_PERSONAL_HABIT_ENTRIES;
        HabitsFragment habitsFragment = new HabitsFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("user_profile", userInfoPOJO);
        habitsFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.habit_fragment_container, habitsFragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_personal_habit_board_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_new_habit_entry_item:
                showNewHabitDailog();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    void showNewHabitDailog() {
        mStartDateValue = "";
        mStartYear = 0;
        mStartMonth = 0;
        mStartDay = 0;

        mEndDateValue = "";
        mEndDay = 0;
        mEndMonth = 0;
        mEndYear = 0;

        mNewEntryDialog = new BottomSheetDialog(this);
        mNewEntryDialog.setContentView(R.layout.dialog_new_habit_layout);
        ImageButton mClose;
        final EditText mTitle, mDescription;
        final Button mStartDate, mEndDate;
        final CheckBox mForeverCheckbox;
        Button mDone;

        mTitle = mNewEntryDialog.findViewById(R.id.title_habit);
        mDescription = mNewEntryDialog.findViewById(R.id.desc_habit);
        mStartDateTextView = mNewEntryDialog.findViewById(R.id.habit_start_date_textview);
        mEndDateTextView = mNewEntryDialog.findViewById(R.id.habit_end_date_textview);
        mStartDate = mNewEntryDialog.findViewById(R.id.habit_start_date_button);
        mEndDate = mNewEntryDialog.findViewById(R.id.habit_end_date_button);
        mForeverCheckbox = mNewEntryDialog.findViewById(R.id.habit_forever_checkbox);
        mClose = mNewEntryDialog.findViewById(R.id.close_habit_entry_dialog);
        mDone = mNewEntryDialog.findViewById(R.id.done_habit_entry_button);

        mForeverCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mEndDateValue = "";
                    mEndDay = 0;
                    mEndMonth = 0;
                    mEndYear = 0;
                    mEndDateTextView.setText("00/00/0000 at 00:00");
                }else{
                    mEndDateValue = "";
                    mEndDay = 0;
                    mEndMonth = 0;
                    mEndYear = 0;
                    mEndDateTextView.setText(null);
                }
            }
        });
        mStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartSelected = true;
                DialogFragment dialog = createDialog();
                dialog.show(getSupportFragmentManager(), "date");
            }
        });
        mEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartSelected = false;
                DialogFragment dialog = createDialog();
                dialog.show(getSupportFragmentManager(), "date");
            }
        });

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewEntryDialog.dismiss();
            }
        });
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitle.getText().toString().trim();
                String desc = mDescription.getText().toString().trim();
                if (desc.isEmpty()) {
                    desc = "";
                }
                if (!(title.isEmpty() || mStartDateValue.isEmpty() || mStartDateValue.equals(""))) {
                    if (mForeverCheckbox.isChecked()) {
                        mEndDateValue = "";
                        mEndDay = 0;
                        mEndMonth = 0;
                        mEndYear = 0;
                        DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_HABITS + "/habits/");
                        databaseReference.keepSynced(true);
                        final DatabaseReference entryDatabaseReference = databaseReference.push();
                        entryDatabaseReference.keepSynced(true);
                        //String habitEntryKey, String title, String description, String startDate, String endDate, boolean isForever, int mStartDay, int mStartMonth, int mStartYear, int mEndDay, int mEndMonth, int mEndYear, int intervalType, String date, UserInfoPOJO userInfoPOJO
                        HabitPOJO habitPOJO = new HabitPOJO(entryDatabaseReference.getKey(), title, desc, mStartDateValue, mEndDateValue, mForeverCheckbox.isSelected(), mStartDay, mStartMonth, mStartYear, mEndDay, mEndMonth, mEndYear, NotificationTypes.INTERVAL_ONCE, DateTimeStamp.getDate(), userInfoPOJO);
                        entryDatabaseReference.setValue(habitPOJO);
                        mNewEntryDialog.dismiss();
                    } else {
                        if (!(mEndDateValue.isEmpty() || mEndDateValue.equals(""))) {
                            DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_HABITS + "/habits/");
                            databaseReference.keepSynced(true);
                            final DatabaseReference entryDatabaseReference = databaseReference.push();
                            entryDatabaseReference.keepSynced(true);
                            //String habitEntryKey, String title, String description, String startDate, String endDate, boolean isForever, int mStartDay, int mStartMonth, int mStartYear, int mEndDay, int mEndMonth, int mEndYear, int intervalType, String date, UserInfoPOJO userInfoPOJO
                            HabitPOJO habitPOJO = new HabitPOJO(entryDatabaseReference.getKey(), title, desc, mStartDateValue, mEndDateValue, mForeverCheckbox.isSelected(), mStartDay, mStartMonth, mStartYear, mEndDay, mEndMonth, mEndYear, NotificationTypes.INTERVAL_ONCE, DateTimeStamp.getDate(), userInfoPOJO);
                            entryDatabaseReference.setValue(habitPOJO);
                            mNewEntryDialog.dismiss();
                        } else {
                            Toast.makeText(PersonalHabitActivity.this, "End Date Missing", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(PersonalHabitActivity.this, "Cannot be blank", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mNewEntryDialog.setCanceledOnTouchOutside(false);
        mNewEntryDialog.show();
    }

    void toLoginActivity() {
        Intent intent = new Intent(PersonalHabitActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Bottom sheet picker for date and time
     * [STARTS HERE]
     */

    @Override
    public void onDateSet(com.philliphsu.bottomsheetpickers.date.DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        //   new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date())
        if (isStartSelected) {
            mStartDateValue = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
                this.mStartYear = year;
                this.mStartMonth = monthOfYear;
                this.mStartDay = dayOfMonth;
                mStartDateTextView.setText(mStartDateValue);
        } else {
            mEndDateValue = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
                this.mEndYear = year;
                this.mEndMonth = monthOfYear;
                this.mEndDay = dayOfMonth;
                mEndDateTextView.setText(mEndDateValue);
            }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private DialogFragment createDialog() {
        return createDialogWithSetters();
    }

    private DialogFragment createDialogWithSetters() {
        BottomSheetPickerDialog dialog = null;
        boolean themeDark = true;

        Calendar now = Calendar.getInstance();
        dialog = com.philliphsu.bottomsheetpickers.date.DatePickerDialog.newInstance(
                PersonalHabitActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));

        com.philliphsu.bottomsheetpickers.date.DatePickerDialog dateDialog = (com.philliphsu.bottomsheetpickers.date.DatePickerDialog) dialog;
        dateDialog.setYearRange(1900, 3000);
        dialog.setThemeDark(themeDark);

        return dialog;
    }
    //[ENDS HERE]
}
