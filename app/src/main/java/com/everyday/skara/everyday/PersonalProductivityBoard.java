package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.everyday.skara.everyday.classes.NewOptionTypes;
import com.everyday.skara.everyday.fragments.PersoanalLinksFragment;
import com.everyday.skara.everyday.fragments.PersonalNotesFragment;
import com.everyday.skara.everyday.fragments.PersonalTodoFragment;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class PersonalProductivityBoard extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    ImageButton mFilterButton;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;
    public static int optionType;
    ImageButton mTodo, mNotes, mLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_productivity_board);
        Toolbar myToolbar = findViewById(R.id.personal_prod_toolbar);
        setSupportActionBar(myToolbar);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        optionType = NewOptionTypes.TYPE_TODO;

        Intent intent = getIntent();

        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        mFilterButton = findViewById(R.id.filter_option_button);

        mTodo = findViewById(R.id.view_todo_image);
        mNotes = findViewById(R.id.view_notes_image);
        mLinks = findViewById(R.id.view_links_image);

        mTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionType = NewOptionTypes.TYPE_TODO;
                mFilterButton.setRotation(0);
                PersonalTodoFragment todoFragment = new PersonalTodoFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                todoFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.fragment_container, todoFragment);
                transaction.addToBackStack(null);

                transaction.commit();

            }
        });
        mNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionType = NewOptionTypes.TYPE_NOTE;
                mFilterButton.setRotation(0);
                PersonalNotesFragment notesFragment = new PersonalNotesFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                notesFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.fragment_container, notesFragment);
                transaction.addToBackStack(null);

                transaction.commit();


            }
        });
        mLinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionType = NewOptionTypes.TYPE_LINK;
                mFilterButton.setRotation(0);
                PersoanalLinksFragment linksFragment = new PersoanalLinksFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                linksFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.fragment_container, linksFragment);
                transaction.addToBackStack(null);

                transaction.commit();

            }
        });


        mFilterButton.setOnClickListener(this);
        initFragment();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_personal_prod_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_menu_item:
                switch (optionType) {
                    case NewOptionTypes.TYPE_TODO:
                        optionType = NewOptionTypes.TYPE_TODO;
                        toNewTodoActivity();
                        break;
                    case NewOptionTypes.TYPE_LINK:
                        optionType = NewOptionTypes.TYPE_LINK;
                        toNewLinkActivity();
                        break;
                    case NewOptionTypes.TYPE_NOTE:
                        optionType = NewOptionTypes.TYPE_NOTE;
                        toNewNoteActivity();
                        break;
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    void initFragment() {
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            if (optionType == NewOptionTypes.TYPE_NOTE) {
                PersonalNotesFragment notesFragment = new PersonalNotesFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                notesFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, notesFragment).commit();
            } else if (optionType == NewOptionTypes.TYPE_LINK) {
                PersoanalLinksFragment linksFragment = new PersoanalLinksFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                linksFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, linksFragment).commit();
            } else if (optionType == NewOptionTypes.TYPE_TODO) {
                PersonalTodoFragment todoFragment = new PersonalTodoFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                todoFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, todoFragment).commit();
            } else {
                PersonalTodoFragment todoFragment = new PersonalTodoFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("user_profile", userInfoPOJO);
                todoFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, todoFragment).commit();

            }

        }


    }

    void toLoginActivity() {
        Intent intent = new Intent(PersonalProductivityBoard.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }



    void toNewNoteActivity() {
        Intent intent = new Intent(PersonalProductivityBoard.this, PersonalNewNoteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toNewLinkActivity() {
        Intent intent = new Intent(PersonalProductivityBoard.this, PersonalNewLinkActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toNewTodoActivity() {
        Intent intent = new Intent(PersonalProductivityBoard.this, PersonalNewTodoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        toBoardsActivity();
    }

    void toBoardsActivity() {
        Intent intent = new Intent(PersonalProductivityBoard.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
