package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.everyday.skara.everyday.pojo.UserProfilePOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BoardActivity extends AppCompatActivity implements View.OnClickListener{
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Button mNewNoteButton, mNewLinkButton;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }
    void init(){
        firebaseDatabase = FirebaseDatabase.getInstance();

        Intent intent = getIntent();

        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        mNewNoteButton = findViewById(R.id.new_note_button);
        mNewLinkButton = findViewById(R.id.new_link_button);

        mNewNoteButton.setOnClickListener(this);
        mNewLinkButton.setOnClickListener(this);

    }
    void toLoginActivity(){
        Intent intent = new Intent(BoardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_note_button:
                toNewNoteActivity();
                break;
            case R.id.new_link_button:
                toNewLinkActivity();
                break;

        }
    }
    void toNewNoteActivity(){
        Intent intent = new Intent(BoardActivity.this, NewNoteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }
    void toNewLinkActivity(){
        Intent intent = new Intent(BoardActivity.this, LinkActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

}
