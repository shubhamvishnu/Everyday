package com.everyday.skara.everyday;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.everyday.skara.everyday.classes.BasicSettings;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    Button mSetSymbolButton;
    TextView mSymbolTextView;
    Button mCategoriesButton;
    BottomSheetDialog mSymbolDialog;
    UserInfoPOJO userInfoPOJO;
    Switch mThemeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        mSetSymbolButton.setText(defaultSettingsPreference.getString("currency", getResources().getString(R.string.inr)));


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
                } else {
                    editor.putInt("theme", BasicSettings.LIGHT_THEME);
                    editor.apply();
                }
            }
        });
    }

    void setThemeState(int theme) {
        if (theme == 0) {
            mThemeSwitch.setChecked(true);
        } else if(theme == 1){
            mThemeSwitch.setChecked(false);
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
        }
    }

    void showSymbolsDialog() {
        Button mCur1, mCur2, mCur3, mCur4, mCur5, mCur6;
        mSymbolDialog = new BottomSheetDialog(SettingsActivity.this);
        mSymbolDialog.setContentView(R.layout.dialog_symbols_dialog);
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

        mSymbolDialog.setCanceledOnTouchOutside(false);
        mSymbolDialog.show();
    }

    void setSymbol(int currencyId) {
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (currencyId) {
            case 1:
                mSetSymbolButton.setText(getResources().getString(R.string.inr));
                editor.putString("currency", getResources().getString(R.string.inr));
                break;
            case 2:
                mSetSymbolButton.setText(getResources().getString(R.string.dollar));
                editor.putString("currency", getResources().getString(R.string.dollar));
                break;
            case 3:

                mSetSymbolButton.setText(getResources().getString(R.string.pound));
                editor.putString("currency", getResources().getString(R.string.pound));
                break;
            case 4:
                mSetSymbolButton.setText(getResources().getString(R.string.euro));
                editor.putString("currency", getResources().getString(R.string.euro));
                break;
            case 5:
                mSetSymbolButton.setText(getResources().getString(R.string.yen));
                editor.putString("currency", getResources().getString(R.string.yen));
                break;
            case 6:
                mSetSymbolButton.setText(getResources().getString(R.string.bitcoin));
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
    void toMainActivity(){
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
