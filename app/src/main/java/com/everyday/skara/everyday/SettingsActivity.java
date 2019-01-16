package com.everyday.skara.everyday;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.classes.BasicSettings;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.classes.TimeDateStamp;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;
import com.philliphsu.bottomsheetpickers.time.grid.GridTimePickerDialog;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, com.philliphsu.bottomsheetpickers.date.DatePickerDialog.OnDateSetListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    Button mSetSymbolButton;
    TextView mSymbolTextView;
    Button mCategoriesButton;
    BottomSheetDialog mSymbolDialog;
    UserInfoPOJO userInfoPOJO;
    Switch mThemeSwitch;
    TextView mDobTextView;
    String date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        int theme = sp.getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            setContentView(R.layout.activity_setting_light);

        } else {
            setContentView(R.layout.activity_settings);
        }

        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void init() {
        Intent intent = getIntent();
        final SharedPreferences defaultSettingsPreference = getSharedPreferences(SPNames.DEFAULT_SETTINGS, MODE_PRIVATE);

//        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");
        mSetSymbolButton = findViewById(R.id.set_symbol_button);
        mSymbolTextView = findViewById(R.id.symbol_choice_textview);
        mCategoriesButton = findViewById(R.id.categories_setting);
        mThemeSwitch = findViewById(R.id.theme_switch);
        mDobTextView = findViewById(R.id.dob_textview);

        mSymbolTextView.setText(defaultSettingsPreference.getString("currency", getResources().getString(R.string.inr)));

        mDobTextView.setText(userInfoPOJO.getDay() + "/" + (userInfoPOJO.getMonth() + 1) + "/" + userInfoPOJO.getYear());
        mDobTextView.setOnClickListener(this);
        mSymbolTextView.setOnClickListener(this);
        mSetSymbolButton.setOnClickListener(this);
        mCategoriesButton.setOnClickListener(this);
        setThemeState(defaultSettingsPreference.getInt("theme", BasicSettings.DEFAULT_THEME));
        mThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = defaultSettingsPreference.edit();
                if (isChecked) {
                    editor.putInt("theme", BasicSettings.DEFAULT_THEME);
                    editor.apply();
                    toMainActivity();
                } else {
                    editor.putInt("theme", BasicSettings.LIGHT_THEME);
                    editor.apply();
                    toMainActivity();
                }
            }
        });
    }

    void setThemeState(int theme) {
        if (theme == BasicSettings.LIGHT_THEME) {
            mThemeSwitch.setChecked(false);
        } else if (theme == BasicSettings.DEFAULT_THEME) {
            mThemeSwitch.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_symbol_button:
                showSymbolsDialog();
                break;
            case R.id.categories_setting:
                toCategoriesActivity();
                break;
            case R.id.symbol_choice_textview:
                showSymbolsDialog();
                break;
            case R.id.dob_textview:
                DialogFragment dialog = createDialog();
                dialog.show(getSupportFragmentManager(), "Date");
                break;
        }
    }

    void showSymbolsDialog() {
        Button mCur1, mCur2, mCur3, mCur4, mCur5, mCur6;
        mSymbolDialog = new BottomSheetDialog(SettingsActivity.this);
        SharedPreferences sp = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        int theme = sp.getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            mSymbolDialog.setContentView(R.layout.dialog_symbols_layout_light);

        } else {
            mSymbolDialog.setContentView(R.layout.dialog_symbols_layout);

        }

        ImageButton mClose = mSymbolDialog.findViewById(R.id.close_symbols_dialog);

        mCur1 = mSymbolDialog.findViewById(R.id.cur1_button);
        mCur2 = mSymbolDialog.findViewById(R.id.cur2_button);
        mCur3 = mSymbolDialog.findViewById(R.id.cur3_button);
        mCur4 = mSymbolDialog.findViewById(R.id.cur4_button);
        mCur5 = mSymbolDialog.findViewById(R.id.cur5_button);
        mCur6 = mSymbolDialog.findViewById(R.id.cur6_button);

        mCur1.setText(getResources().getString(R.string.inr));
        mCur2.setText(getResources().getString(R.string.dollar));
        mCur3.setText(getResources().getString(R.string.pound));
        mCur4.setText(getResources().getString(R.string.euro));
        mCur5.setText(getResources().getString(R.string.yen));
        mCur6.setText(getResources().getString(R.string.bitcoin));


        mCur1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSymbol(1);
                mSymbolDialog.dismiss();
            }
        });


        mCur2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSymbol(2);
                mSymbolDialog.dismiss();
            }
        });


        mCur3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSymbol(3);
                mSymbolDialog.dismiss();
            }
        });


        mCur4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSymbol(4);
                mSymbolDialog.dismiss();
            }
        });

        mCur5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSymbol(5);
                mSymbolDialog.dismiss();
            }
        });

        mCur6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSymbol(6);
                mSymbolDialog.dismiss();
            }
        });


        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSymbolDialog.dismiss();
            }
        });

        mSymbolDialog.setCanceledOnTouchOutside(true);
        mSymbolDialog.show();
    }

    @Override
    public void onBackPressed() {
        toMainActivity();
    }

    void setSymbol(int currencyId) {
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (currencyId) {
            case 1:
                mSymbolTextView.setText(getResources().getString(R.string.inr));
                editor.putString("currency", getResources().getString(R.string.inr));
                break;
            case 2:
                mSymbolTextView.setText(getResources().getString(R.string.dollar));
                editor.putString("currency", getResources().getString(R.string.dollar));
                break;
            case 3:

                mSymbolTextView.setText(getResources().getString(R.string.pound));
                editor.putString("currency", getResources().getString(R.string.pound));
                break;
            case 4:
                mSymbolTextView.setText(getResources().getString(R.string.euro));
                editor.putString("currency", getResources().getString(R.string.euro));
                break;
            case 5:
                mSymbolTextView.setText(getResources().getString(R.string.yen));
                editor.putString("currency", getResources().getString(R.string.yen));
                break;
            case 6:
                mSymbolTextView.setText(getResources().getString(R.string.bitcoin));
                editor.putString("currency", getResources().getString(R.string.bitcoin));
                break;

        }
        editor.apply();
    }

    void toCategoriesActivity() {
        Intent intent = new Intent(SettingsActivity.this, CategoriesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toLoginActivity() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void toMainActivity() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
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

        date = DateFormat.getDateFormat(this).format(cal.getTime());

        if (!(date.isEmpty() || date.equals(""))) {
            SharedPreferences sharedPreferences = getSharedPreferences(SPNames.USER_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("dob_month", monthOfYear);
            editor.putInt("dob_year", year);
            editor.putInt("dob_day", dayOfMonth);
            editor.apply();

            userInfoPOJO.setDay(dayOfMonth);
            userInfoPOJO.setMonth(monthOfYear);
            userInfoPOJO.setYear(year);
            mDobTextView.setText(userInfoPOJO.getDay() + "/" + (userInfoPOJO.getMonth() + 1) + "/" + userInfoPOJO.getYear());


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
                SettingsActivity.this,
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
