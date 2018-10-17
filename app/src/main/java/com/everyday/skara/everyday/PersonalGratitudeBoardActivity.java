package com.everyday.skara.everyday;

import android.content.Intent;
import android.media.Image;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.NewOptionTypes;
import com.everyday.skara.everyday.fragments.PersonalFinanceFragment;
import com.everyday.skara.everyday.fragments.PersonalGratitudeEntriesFragment;
import com.everyday.skara.everyday.pojo.GratitudePOJO;
import com.everyday.skara.everyday.pojo.LinkPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PersonalGratitudeBoardActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    UserInfoPOJO userInfoPOJO;
    BottomSheetDialog mNewEntryDialog;
    int moodChoice = -1;
    public static int optionType;
    ImageButton mGratitudeEntriesIcon;
    ImageButton mMoodsViewIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_gratitude);
        Toolbar myToolbar = findViewById(R.id.gratitude_board_toolbar);
        setSupportActionBar(myToolbar);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }
    void init(){
        Intent intent = getIntent();
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");
        optionType = NewOptionTypes.TYPE_PERSONAL_GRATITUDE_ENTRIES;

        mGratitudeEntriesIcon = findViewById(R.id.gratitude_entry_option_icon);
        mMoodsViewIcon= findViewById(R.id.gratitude_moods_view_icon);

        mGratitudeEntriesIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionType = NewOptionTypes.TYPE_PERSONAL_GRATITUDE_ENTRIES;
                PersonalGratitudeEntriesFragment personalGratitudeEntriesFragment= new PersonalGratitudeEntriesFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                personalGratitudeEntriesFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.gratitude_fragment_container, personalGratitudeEntriesFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_personal_gratitude_board_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_new_gratitude_entry_item:
                showNewGratitudeEntryDialog();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    void showNewGratitudeEntryDialog(){
        mNewEntryDialog = new BottomSheetDialog(this);
        mNewEntryDialog.setContentView(R.layout.new_gratitude_entry_layout);
        ImageButton mClose;
        final EditText entry1, entry2, entry3, entryNote;
        Button mDone;

        mClose = mNewEntryDialog.findViewById(R.id.close_gratitude_entry_dialog);
        entry1 = mNewEntryDialog.findViewById(R.id.entry_1_edittext);
        entry2 = mNewEntryDialog.findViewById(R.id.entry_2_edittext);
        entry3 = mNewEntryDialog.findViewById(R.id.entry_3_edittext);
        entryNote = mNewEntryDialog.findViewById(R.id.entry_note_edittext);

        mDone = mNewEntryDialog.findViewById(R.id.done_gratitude_entry_button);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewEntryDialog.dismiss();
            }
        });

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String entry1Value = entry1.getText().toString().trim();
                String entry2Value = entry2.getText().toString().trim();
                String entry3Value = entry3.getText().toString().trim();
                String entryNotevalue = entryNote.getText().toString().trim();
                if(entryNotevalue.isEmpty()){
                    entryNotevalue = "";
                }
                if (!(entry1Value.isEmpty() || entry2Value.isEmpty() || entry3Value.isEmpty())) {
                    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() +"/"+FirebaseReferences.FIREBASE_PERSONAL_BOARD_GRATITUDE+ "/gratitude/");
                    databaseReference.keepSynced(true);
                    final DatabaseReference entryDatabaseReference = databaseReference.push();
                    entryDatabaseReference.keepSynced(true);
                    GratitudePOJO gratitudePOJO = new GratitudePOJO(entryDatabaseReference.getKey(), entry1Value, entry2Value,entry3Value, moodChoice, entryNotevalue, DateTimeStamp.getDate(), userInfoPOJO);
                    entryDatabaseReference.setValue(gratitudePOJO);
                    mNewEntryDialog.dismiss();
                } else {
                    Toast.makeText(PersonalGratitudeBoardActivity.this, "Cannot be blank", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mNewEntryDialog.setCanceledOnTouchOutside(false);
        mNewEntryDialog.show();
    }
    void toLoginActivity() {
        Intent intent = new Intent(PersonalGratitudeBoardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
