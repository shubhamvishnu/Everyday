package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.everyday.skara.everyday.classes.ActionType;
import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
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
        setContentView(R.layout.activity_new_note);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
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
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() +"/"+FirebaseReferences.FIREBASE_PERSONAL_BOARD_PROD+ "/notes/");
        databaseReference.keepSynced(true);
        DatabaseReference notesReference = databaseReference.push();
        notesReference.keepSynced(true);

        String title = mTitle.getText().toString().trim();
        String content = mContent.getText().toString();

        if (!content.isEmpty()) {
            NotePOJO notePOJO = new NotePOJO(notesReference.getKey(), title, content, DateTimeStamp.getDate(), userInfoPOJO);
            notesReference.setValue(notePOJO);
            ActivityPOJO activityPOJO = new ActivityPOJO("New Note Saved", DateTimeStamp.getDate(), ActionType.ACTION_TYPE_NEW_NOTE, userInfoPOJO);
            firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() +"/"+FirebaseReferences.FIREBASE_PERSONAL_BOARD_PROD).child("activity").push().setValue(activityPOJO);
            toPersoanalBoards();
        } else {
            // TODO: show field empty alert
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

    void toPersoanalBoards() {
        Intent intent = new Intent(PersonalNewNoteActivity.this, PersonalProductivityBoard.class);
        intent.putExtra("user_profile", userInfoPOJO);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}