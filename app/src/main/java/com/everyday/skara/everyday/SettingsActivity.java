package com.everyday.skara.everyday;

import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.everyday.skara.everyday.classes.DateTimeStamp;

import org.w3c.dom.Text;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    Button mSetSymbolButton;
    TextView mSymbolTextView;
    BottomSheetDialog mSymbolDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
    }

    void init() {
        mSetSymbolButton = findViewById(R.id.set_symbol_button);
        mSymbolTextView = findViewById(R.id.symbol_choice_textview);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_symbol_button:
                showSymbolsDialog();
                break;
        }
    }

    void showSymbolsDialog() {
        mSymbolDialog = new BottomSheetDialog(SettingsActivity.this);
        mSymbolDialog.setContentView(R.layout.dialog_symbols_dialog);
        ImageButton mClose = mSymbolDialog.findViewById(R.id.close_symbols_dialog);


        mSymbolDialog.setCanceledOnTouchOutside(false);
        mSymbolDialog.show();
    }

}
