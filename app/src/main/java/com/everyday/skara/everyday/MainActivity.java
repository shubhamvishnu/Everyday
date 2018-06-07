package com.everyday.skara.everyday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.ActivityPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.UserProfilePOJO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tapadoo.alerter.Alerter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    UserProfilePOJO userProfilePOJO;
    Button mNewButton;


    // Dialog
    BottomSheetDialog mNewBoardDialog;

    // Dialog Components
    EditText mTitle;
    TextView mDate;
    Button mDone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        mNewButton = findViewById(R.id.new_board_button);

        // initializing UserProfilePOJO
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.USER_DETAILS, MODE_PRIVATE);
        String name = sharedPreferences.getString("name", null);
        String email = sharedPreferences.getString("email", null);
        String profile_url = sharedPreferences.getString("url", null);
        String user_key = sharedPreferences.getString("user_key", null);
        String login_type = sharedPreferences.getString("login_type", null);
        int user_account_type = sharedPreferences.getInt("user_account_type", 0);

        userProfilePOJO = new UserProfilePOJO(name, email, profile_url, user_key, login_type, user_account_type);

        mNewButton.setOnClickListener(this);


    }

    void toLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_board_button:
                showNewBoardDialog();
                break;
        }
    }

    void showNewBoardDialog() {
        mNewBoardDialog = new BottomSheetDialog(this);
        mNewBoardDialog.setContentView(R.layout.dialog_new_baord_layout);

        mTitle = mNewBoardDialog.findViewById(R.id.board_title);
        mDate = mNewBoardDialog.findViewById(R.id.board_date);
        mDone = mNewBoardDialog.findViewById(R.id.board_done);

        mDate.setText(DateTimeStamp.getDate());

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitle.getText().toString().trim();
                if (!title.isEmpty()) {
                    createBoard(title);
                } else {
                    // TODO: Show empty field alert
                }
            }
        });

        mNewBoardDialog.setCanceledOnTouchOutside(true);
        mNewBoardDialog.show();
    }

    void createBoard(final String title) {
        if (Connectivity.checkInternetConnection(this)) {
            mTitle.setEnabled(false);
            mDone.setEnabled(false);
            databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS);
            final DatabaseReference boardReference = databaseReference.push();
            final String boardKey = boardReference.getKey();


            // initializing BoardPOJO class
            final BoardPOJO boardPOJO = new BoardPOJO(title, DateTimeStamp.getDate(), boardKey, userProfilePOJO);

            // TODO: add a progress bar
            boardReference.setValue(boardPOJO).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    ActivityPOJO activityPOJO = new ActivityPOJO(title + " created on " + boardPOJO.getDate() + "by" + userProfilePOJO.getName(), boardPOJO.getDate(), userProfilePOJO);
                    boardReference.child("activity").push().setValue(activityPOJO).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mNewBoardDialog.dismiss();
                            toBoardActivity(boardPOJO);
                        }
                    });

                }
            });
        }else{
            showInternetAlerter();
        }
    }
    void showInternetAlerter() {
        Alerter.create(this)
                .setText("Oops! no internet connection...")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Connectivity.openInternetSettings(getApplicationContext());
                    }
                })
                .setBackgroundColorRes(R.color.colorAccent)
                .show();
    }

    void toBoardActivity(BoardPOJO boardPOJO) {
        Intent intent = new Intent(MainActivity.this, BoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userProfilePOJO);
        startActivity(intent);
    }
}
