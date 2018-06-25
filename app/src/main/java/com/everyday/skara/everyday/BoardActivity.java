package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.everyday.skara.everyday.fragments.LinksFragment;
import com.everyday.skara.everyday.fragments.NotesFragment;
import com.everyday.skara.everyday.fragments.TodoFragment;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BoardActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Button mNewNoteButton, mNewLinkButton, mNewTodoButton;
    Button mNewOptionsButton;
    Button mViewTodo;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;

    // Dialog
    BottomSheetDialog mNewOptionsDialog;

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

    void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();

        Intent intent = getIntent();

        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        mNewOptionsButton = findViewById(R.id.new_options_button);

        mNewNoteButton = findViewById(R.id.new_note_button);
        mNewLinkButton = findViewById(R.id.new_link_button);
        mNewTodoButton = findViewById(R.id.new_todo_button);

        mViewTodo = findViewById(R.id.view_todo);


        mNewNoteButton.setOnClickListener(this);
        mNewLinkButton.setOnClickListener(this);
        mNewTodoButton.setOnClickListener(this);

        mViewTodo.setOnClickListener(this);
        mNewOptionsButton.setOnClickListener(this);
        initFragment();

    }

    void initFragment() {
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            TodoFragment todoFragment = new TodoFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable("board_pojo", boardPOJO);
            bundle.putSerializable("user_profile", userInfoPOJO);
            todoFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, todoFragment).commit();

        }


    }

    void toLoginActivity() {
        Intent intent = new Intent(BoardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_note_button:
                toNewNoteActivity();
                break;
            case R.id.new_link_button:
                toNewLinkActivity();
                break;
            case R.id.new_todo_button:
                toNewTodoActivity();
                break;
            case R.id.view_todo:
                toViewTodoActivity();
                break;
            case R.id.new_options_button:
                showOptionsDialog();
                break;

        }
    }

    void showOptionsDialog() {
        Button mTodo, mNotes, mLinks;
        mNewOptionsDialog = new BottomSheetDialog(this);
        mNewOptionsDialog.setContentView(R.layout.dialog_options_layout);

        mTodo = mNewOptionsDialog.findViewById(R.id.view_todo_image);
        mNotes = mNewOptionsDialog.findViewById(R.id.view_notes_image);
        mLinks = mNewOptionsDialog.findViewById(R.id.view_links_image);

        mTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TodoFragment todoFragment = new TodoFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("board_pojo", boardPOJO);
                bundle.putSerializable("user_profile", userInfoPOJO);
                todoFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.fragment_container, todoFragment);
                transaction.addToBackStack(null);

                transaction.commit();
                mNewOptionsDialog.dismiss();

            }
        });
        mNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotesFragment notesFragment = new NotesFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("board_pojo", boardPOJO);
                bundle.putSerializable("user_profile", userInfoPOJO);
                notesFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.fragment_container, notesFragment);
                transaction.addToBackStack(null);

                transaction.commit();

                mNewOptionsDialog.dismiss();

            }
        });
        mLinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinksFragment linksFragment = new LinksFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("board_pojo", boardPOJO);
                bundle.putSerializable("user_profile", userInfoPOJO);
                linksFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.fragment_container, linksFragment);
                transaction.addToBackStack(null);

                transaction.commit();

                mNewOptionsDialog.dismiss();
            }
        });

        mNewOptionsDialog.setCanceledOnTouchOutside(true);
        mNewOptionsDialog.show();
    }

    void toNewNoteActivity() {
        Intent intent = new Intent(BoardActivity.this, NewNoteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toNewLinkActivity() {
        Intent intent = new Intent(BoardActivity.this, LinkActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toNewTodoActivity() {
        Intent intent = new Intent(BoardActivity.this, TodoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toViewTodoActivity() {
        Intent intent = new Intent(BoardActivity.this, TodoViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

}
