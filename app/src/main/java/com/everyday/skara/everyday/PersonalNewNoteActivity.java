package com.everyday.skara.everyday;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.classes.ActionType;
import com.everyday.skara.everyday.classes.BasicSettings;
import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.ActivityPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.NotePOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tapadoo.alerter.Alerter;

public class PersonalNewNoteActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    EditText mTitle, mContent;
    TextView mDate;
    Button mDone;
    UserInfoPOJO userInfoPOJO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        int theme = sp.getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            setContentView(R.layout.activity_personal_new_note_light);

        } else {
            setContentView(R.layout.activity_personal_new_note);

        }
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    @Override
    public void onBackPressed() {
        saveNote();

    }

    void saveSelected() {
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("item_selected", 3);
        editor.apply();
        toMainActivity();
    }

    void toMainActivity() {
        Intent intent = new Intent(PersonalNewNoteActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        Intent intent = getIntent();

        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        mTitle = findViewById(R.id.note_title);
        mContent = findViewById(R.id.note_content);
        mDate = findViewById(R.id.note_date);
        mDone = findViewById(R.id.note_done);

        mDate.setText(DateTimeStamp.getDate());

        mDone.setOnClickListener(this);


    }

    void toLoginActivity() {
        Intent intent = new Intent(PersonalNewNoteActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.note_done:
                saveNote();
                break;
        }
    }

    void saveNote() {
        mDone.setEnabled(false);
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_PROD + "/notes/");
        databaseReference.keepSynced(true);
        DatabaseReference notesReference = databaseReference.push();
        notesReference.keepSynced(true);

        String title = mTitle.getText().toString().trim();
        String content = mContent.getText().toString();

        if (!content.isEmpty()) {
            NotePOJO notePOJO = new NotePOJO(notesReference.getKey(), title, content, DateTimeStamp.getDate(), userInfoPOJO);
            notesReference.setValue(notePOJO);
            ActivityPOJO activityPOJO = new ActivityPOJO("New Note Saved", DateTimeStamp.getDate(), ActionType.ACTION_TYPE_NEW_NOTE, userInfoPOJO);
            firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_PROD).child("activity").push().setValue(activityPOJO);
            saveSelected();
        } else {
            Toast.makeText(this, "Write Something", Toast.LENGTH_SHORT).show();
            saveSelected();
        }
    }

    void showInternetAlerter() {
        Alerter.create(this)
                .setText("No internet connection. Will be saved to cloud later.")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Connectivity.openInternetSettings(getApplicationContext());
                    }
                })
                .setBackgroundColorRes(R.color.colorAccent)
                .show();
    }


}
