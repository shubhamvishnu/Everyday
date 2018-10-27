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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.everyday.skara.everyday.classes.NotificationHolder;
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
import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.grid.GridTimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PersonalHabitActivity extends AppCompatActivity implements com.philliphsu.bottomsheetpickers.date.DatePickerDialog.OnDateSetListener, BottomSheetTimePickerDialog.OnTimeSetListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    UserInfoPOJO userInfoPOJO;
    BottomSheetDialog mNewEntryDialog;
    public static int optionType;
    ImageButton mHabitEntriesButton;
    String mDate;
    int mDay, mMonth, mYear;
    TextView mEndDateTextView;
    int mHours, mMinutes;
    String mTime;
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
        mDate = new String("");
        mTime = new String("");
        mHours = 0;
        mMinutes = 0;
        mDay = 0;
        mMonth = 0;
        mYear = 0;

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
        mDate = new String("");
        mDay = 0;
        mMonth = 0;
        mYear = 0;

        mNewEntryDialog = new BottomSheetDialog(this);
        mNewEntryDialog.setContentView(R.layout.dialog_new_habit_layout);
        ImageButton mClose;
        final EditText mTitle, mDescription;
        final Button mEndDate;
        Button mDone;

        mTitle = mNewEntryDialog.findViewById(R.id.title_habit);
        mDescription = mNewEntryDialog.findViewById(R.id.desc_habit);
        mEndDateTextView = mNewEntryDialog.findViewById(R.id.habit_end_date_textview);
        mEndDate = mNewEntryDialog.findViewById(R.id.habit_end_date_button);
        mClose = mNewEntryDialog.findViewById(R.id.close_habit_entry_dialog);
        mDone = mNewEntryDialog.findViewById(R.id.done_habit_entry_button);

        mEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                if (!(title.isEmpty() || mDate.isEmpty() || mDate.equals("") || mTime.isEmpty() || mTitle.equals(""))) {

                    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_HABITS + "/habits/");
                    databaseReference.keepSynced(true);
                    final DatabaseReference entryDatabaseReference = databaseReference.push();
                    entryDatabaseReference.keepSynced(true);
                    HabitPOJO habitPOJO = new HabitPOJO(entryDatabaseReference.getKey(), title, desc, mDate, mTime, mDay, mMonth, mYear, mHours, mMinutes, NotificationTypes.INTERVAL_ONCE, DateTimeStamp.getDate(), userInfoPOJO);
                    entryDatabaseReference.setValue(habitPOJO);

                    DatabaseReference reminderReference = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/reminders");
                    reminderReference.keepSynced(true);
                    NotificationHolder notificationHolder = new NotificationHolder(entryDatabaseReference.getKey(), "Reminder", "Habits Reminder", entryDatabaseReference.getKey(), mDate, mTime, mDay, mMonth, mYear, mHours, mMinutes, NotificationTypes.INTERVAL_ONCE, NotificationTypes.TYPE_TODO, true);
                    reminderReference.child(entryDatabaseReference.getKey()).setValue(notificationHolder);

                    mNewEntryDialog.dismiss();
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
    public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        mTime = DateFormat.getTimeFormat(this).format(cal.getTime());
        if (!(mTime.isEmpty() || mTime.equals(""))) {

            mHours = hourOfDay;
            mMinutes = minute;

            // mReminder.setText("Reminder set at " + time + " on " + date);

        }
    }

    private DialogFragment createTimeDialog() {
        return createTimeDialogWithSetters();
    }

    private DialogFragment createTimeDialogWithSetters() {
        BottomSheetPickerDialog dialog = null;
        boolean custom = false;
        boolean customDark = false;
        boolean themeDark = true;

        Calendar now = Calendar.getInstance();
        dialog = GridTimePickerDialog.newInstance(
                PersonalHabitActivity.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(PersonalHabitActivity.this));
        GridTimePickerDialog gridDialog = (GridTimePickerDialog) dialog;
        dialog.setThemeDark(themeDark);

        return dialog;
    }

    @Override
    public void onDateSet(com.philliphsu.bottomsheetpickers.date.DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        //   new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date())
        mDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
        this.mYear = year;
        this.mMonth = monthOfYear;
        this.mDay = dayOfMonth;
        mEndDateTextView.setText(mDate);

        DialogFragment dialog1 = createTimeDialog();
        dialog1.show(getSupportFragmentManager(), "time");

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
        dateDialog.setYearRange(Calendar.YEAR, 3000);
        dialog.setThemeDark(themeDark);

        return dialog;
    }
    //[ENDS HERE]
}
