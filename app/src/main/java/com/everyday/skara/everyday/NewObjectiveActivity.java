package com.everyday.skara.everyday;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.everyday.skara.everyday.classes.ActionType;
import com.everyday.skara.everyday.classes.BasicSettings;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.ActivityPOJO;
import com.everyday.skara.everyday.pojo.TodoInfoPOJO;
import com.everyday.skara.everyday.pojo.TodoPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;
import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

public class NewObjectiveActivity extends AppCompatActivity implements View.OnClickListener, com.philliphsu.bottomsheetpickers.date.DatePickerDialog.OnDateSetListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    UserInfoPOJO userInfoPOJO;

    int mSDay, mSMonth, mSYear;
    int mEDay, mEMonth, mEYear;
    boolean startSelected = false;
    boolean endSelected = false;
    int dateTypeSelected = -1;

    Button mStartButton, mEndButton;

    @Override
    public void onBackPressed() {
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("item_selected", 5);
        editor.apply();
        toMainActivity();
    }

    void toMainActivity() {
        Intent intent = new Intent(NewObjectiveActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences(SPNames.DEFAULT_SETTINGS, MODE_PRIVATE);
        int theme = sp.getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            setContentView(R.layout.activity_new_objective);

        } else {
            setContentView(R.layout.activity_new_objective);
        }

        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void init() {
        Intent intent = getIntent();
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");
        firebaseDatabase = FirebaseDatabase.getInstance();

        mSDay = mSMonth = mSYear = -1;
        mEDay = mEMonth = mEYear = -1;


        mStartButton = findViewById(R.id.start_select_date);
        mEndButton = findViewById(R.id.end_select_date);


        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.USER_OBJECTIVE, Context.MODE_PRIVATE);
        if (sharedPreferences.contains("start_date_day")) {
            int d = sharedPreferences.getInt("start_date_day", 0);
            int m = sharedPreferences.getInt("start_date_month", 0);
            int y = sharedPreferences.getInt("start_date_year", 0);
            int ed = sharedPreferences.getInt("end_date_day", 0);
            int em = sharedPreferences.getInt("end_date_month", 0);
            int ey = sharedPreferences.getInt("end_date_year", 0);

            mStartButton.setText("" + d + "/" + m + "/" + y);
            mEndButton.setText("" + ed + "/" + em + "/" + ey);
        }

        mStartButton.setOnClickListener(this);
        mEndButton.setOnClickListener(this);

    }


    void toLoginActivity() {
        Intent intent = new Intent(NewObjectiveActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_select_date:
                dateTypeSelected = 1;
                DialogFragment dialog = createDialog();
                dialog.show(getSupportFragmentManager(), "Start Date");
                break;
            case R.id.end_select_date:
                dateTypeSelected = 2;
                DialogFragment dialog1 = createDialog();
                dialog1.show(getSupportFragmentManager(), "End Date");
                break;
        }
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

        if (dateTypeSelected == 1) {
            String date = DateFormat.getDateFormat(this).format(cal.getTime());

            if (!(date.isEmpty() || date.equals(""))) {
                SharedPreferences sharedPreferences = getSharedPreferences(SPNames.USER_OBJECTIVE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("start_date_day", dayOfMonth);
                editor.putInt("start_date_month", monthOfYear);
                editor.putInt("start_date_year", year);
//                editor.putInt("end_date_day", year);
//                editor.putInt("end_date_month", year);
//                editor.putInt("end_date_year", year);
                editor.apply();
                startSelected = true;
                mStartButton.setText(dayOfMonth + "/" + monthOfYear + "/" + year);
                dateTypeSelected = -1;
            }
        } else if (dateTypeSelected == 2) {
            String date = DateFormat.getDateFormat(this).format(cal.getTime());

            if (!(date.isEmpty() || date.equals(""))) {
                SharedPreferences sharedPreferences = getSharedPreferences(SPNames.USER_OBJECTIVE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putInt("start_date_day", dayOfMonth);
//                    editor.putInt("start_date_month", monthOfYear);
//                    editor.putInt("start_date_year", year);
                editor.putInt("end_date_day", dayOfMonth);
                editor.putInt("end_date_month", monthOfYear);
                editor.putInt("end_date_year", year);
                editor.apply();
                endSelected = true;
                mEndButton.setText(dayOfMonth + "/" + monthOfYear + "/" + year);
                dateTypeSelected = -1;
            }


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
                NewObjectiveActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));

        com.philliphsu.bottomsheetpickers.date.DatePickerDialog dateDialog = (com.philliphsu.bottomsheetpickers.date.DatePickerDialog) dialog;
        Calendar maxCalendar = Calendar.getInstance();
        Calendar minCalendar = Calendar.getInstance();
        minCalendar.add(Calendar.YEAR, -100);
        dateDialog.setMinDate(minCalendar);
        dateDialog.setMaxDate(maxCalendar);
        dateDialog.setYearRange(Calendar.YEAR, 2050);
        dialog.setThemeDark(themeDark);

        return dialog;
    }
    //[ENDS HERE]
}
