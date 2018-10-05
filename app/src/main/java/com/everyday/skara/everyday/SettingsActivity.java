package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    Button mSetSymbolButton;
    TextView mSymbolTextView;
    Button mCategoriesButton;
    BottomSheetDialog mSymbolDialog;
    UserInfoPOJO userInfoPOJO;
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
//        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");
        mSetSymbolButton = findViewById(R.id.set_symbol_button);
        mSymbolTextView = findViewById(R.id.symbol_choice_textview);
        mCategoriesButton = findViewById(R.id.categories_setting);

        mSetSymbolButton.setOnClickListener(this);
        mCategoriesButton.setOnClickListener(this);
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
        mSymbolDialog = new BottomSheetDialog(SettingsActivity.this);
        mSymbolDialog.setContentView(R.layout.dialog_symbols_dialog);
        ImageButton mClose = mSymbolDialog.findViewById(R.id.close_symbols_dialog);
        // todo: complete the dialog
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSymbolDialog.dismiss();
            }
        });

        mSymbolDialog.setCanceledOnTouchOutside(false);
        mSymbolDialog.show();
    }
    void toCategoriesActivity(){
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
}
