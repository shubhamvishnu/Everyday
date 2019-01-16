package com.everyday.skara.everyday;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.everyday.skara.everyday.classes.ActionType;
import com.everyday.skara.everyday.classes.BasicSettings;
import com.everyday.skara.everyday.classes.BoardTypes;
import com.everyday.skara.everyday.classes.BoardViewHolderClass;
import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.fragments.LifeBoardFragment;
import com.everyday.skara.everyday.fragments.PersoanalLinksFragment;
import com.everyday.skara.everyday.fragments.PersonalFinancialBoardFragment;
import com.everyday.skara.everyday.fragments.PersonalNotesFragment;
import com.everyday.skara.everyday.fragments.PersonalTodoFragment;
import com.everyday.skara.everyday.fragments.UserAccountFragment;
import com.everyday.skara.everyday.pojo.ActivityPOJO;
import com.everyday.skara.everyday.pojo.BoardMembersPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.Categories;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.everyday.skara.everyday.pojo.UserProfilePOJO;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference boardsReference;
    ChildEventListener childEventListener;
    UserProfilePOJO userProfilePOJO;
    UserInfoPOJO userInfoPOJO;
    // RecyclerView
    //BoardsAdapter boardsAdapter;
    //RecyclerView mBoardsRecyclerView;

    // Dialog
    BottomSheetDialog mNewBoardDialog;

    // Dialog Components
    EditText mTitle;
    TextView mDate;
    Button mDone;
    Toolbar myToolbar;
    BottomBar mBottomBar;
    ArrayList<BoardPOJO> boardPOJOArrayList;
    ArrayList<BoardViewHolderClass> boardViewHolderClassArrayList;

    // TextView mPersonalFinanceTitle, mPersoanlProdTitle, mPersonalGratitudeTitle, mPersonalHabitTitle, mPersonalLsTitle;
    ImageButton mTodo, mLink, mNotes, mFinance;
    public static int OPTION_TYPE = 1;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileAds.initialize(this, "ca-app-pub-2940427582515935~3482302447");
        SharedPreferences sp = getSharedPreferences(SPNames.DEFAULT_SETTINGS, MODE_PRIVATE);
        int theme = sp.getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            setContentView(R.layout.activity_main_layout_light);
        } else {
            setContentView(R.layout.activity_main_layout);
        }
        myToolbar = findViewById(R.id.boards_toolbar);
        myToolbar.setTitle("Finance");
        setSupportActionBar(myToolbar);
        sharedPreferences = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    void initBottomBar() {
        sharedPreferences = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        if (sharedPreferences.contains("item_selected")) {
            OPTION_TYPE = sharedPreferences.getInt("item_selected", 1);
        } else {
            OPTION_TYPE = 1;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("item_selected", 1);
            editor.apply();
        }


        mBottomBar = (BottomBar) findViewById(R.id.bottom_bar_main);
        --OPTION_TYPE;
        mBottomBar.selectTabAtPosition(OPTION_TYPE);
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.finance_item) {
                    OPTION_TYPE = 1;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("item_selected", 1);
                    editor.apply();
                    myToolbar.setTitle("Finance");

                    PersonalFinancialBoardFragment financialBoardActivity = new PersonalFinancialBoardFragment();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user_profile", userInfoPOJO);
                    financialBoardActivity.setArguments(bundle);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    transaction.replace(R.id.main_content_container, financialBoardActivity);
                    transaction.addToBackStack(null);

                    transaction.commit();
                } else if (tabId == R.id.todo_item_menu) {
                    OPTION_TYPE = 2;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("item_selected", 2);
                    editor.apply();
                    PersonalTodoFragment todoFragment = new PersonalTodoFragment();

                    myToolbar.setTitle("TODOs");


                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user_profile", userInfoPOJO);
                    todoFragment.setArguments(bundle);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    transaction.replace(R.id.main_content_container, todoFragment);
                    transaction.addToBackStack(null);

                    transaction.commit();
                } else if (tabId == R.id.note_item) {
                    OPTION_TYPE = 3;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("item_selected", 3);
                    editor.apply();

                    myToolbar.setTitle("Notes");

                    PersonalNotesFragment notesFragment = new PersonalNotesFragment();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user_profile", userInfoPOJO);
                    notesFragment.setArguments(bundle);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    transaction.replace(R.id.main_content_container, notesFragment);
                    transaction.addToBackStack(null);

                    transaction.commit();

                } else if (tabId == R.id.link_item) {
                    OPTION_TYPE = 4;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("item_selected", 4);
                    editor.apply();

                    myToolbar.setTitle("Links");

                    PersoanalLinksFragment linksFragment = new PersoanalLinksFragment();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user_profile", userInfoPOJO);
                    linksFragment.setArguments(bundle);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    transaction.replace(R.id.main_content_container, linksFragment);
                    transaction.addToBackStack(null);

                    transaction.commit();
                } else if (tabId == R.id.user_profile_item) {
                    OPTION_TYPE = 6;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("item_selected", 6);
                    editor.apply();
                    myToolbar.setTitle("Profile");
                    UserAccountFragment userAccountFragment = new UserAccountFragment();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user_profile", userInfoPOJO);
                    userAccountFragment.setArguments(bundle);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    transaction.replace(R.id.main_content_container, userAccountFragment);
                    transaction.addToBackStack(null);

                    transaction.commit();
                } else if (tabId == R.id.life_board_item) {


                    OPTION_TYPE = 5;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("item_selected", 5);
                    editor.apply();

                    myToolbar.setTitle("LifeBoard");
                    LifeBoardFragment lifeBoardFragment = new LifeBoardFragment();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user_profile", userInfoPOJO);
                    lifeBoardFragment.setArguments(bundle);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    transaction.replace(R.id.main_content_container, lifeBoardFragment);
                    transaction.addToBackStack(null);

                    transaction.commit();
                }
            }
        });
    }

    void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
/*
        mBoardsRecyclerView = findViewById(R.id.recyclerview_boards);

        mTodo = findViewById(R.id.todo_board_image);
        mLink = findViewById(R.id.link_board_image);
        mNotes = findViewById(R.id.notes_board_image);
        mFinance = findViewById(R.id.finance_board_image);

        mPersonalFinanceTitle = findViewById(R.id.personal_financial_cardview_title);
        mPersoanlProdTitle = findViewById(R.id.personal_prod_cardview_title);
        mPersonalGratitudeTitle = findViewById(R.id.personal_gratitude_cardview_title);
        mPersonalHabitTitle = findViewById(R.id.personal_habits_cardview_title);
        mPersonalLsTitle = findViewById(R.id.personal_ls_cardview_title);
*/
        // initializing UserProfilePOJO
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.USER_DETAILS, MODE_PRIVATE);
        String name = sharedPreferences.getString("name", null);
        String email = sharedPreferences.getString("email", null);
        String profile_url = sharedPreferences.getString("url", null);
        String user_key = sharedPreferences.getString("user_key", null);
        String login_type = sharedPreferences.getString("login_type", null);
        int user_account_type = sharedPreferences.getInt("user_account_type", 0);
        int day = sharedPreferences.getInt("dob_day", 1);
        int month = sharedPreferences.getInt("dob_month", 0);
        int year = sharedPreferences.getInt("dob_year", 1990);


        userProfilePOJO = new UserProfilePOJO(name, email, profile_url, user_key, login_type, user_account_type, day, month, year);
        userInfoPOJO = new UserInfoPOJO(name, email, profile_url, user_key, day, month, year);

        /*
        mPersonalFinanceTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPersonalFinanceActivity();
            }
        });
        mPersoanlProdTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPersonalProducitivityActivity();
            }
        });
        mPersonalGratitudeTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPersonalGratitudeActivity();
            }
        });

        mPersonalHabitTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPersonalHabitActivity();
            }
        });
        mPersonalLsTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPersonalLsActivity();
            }
        });


        mFinance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPersonalFinanceActivity();
            }
        });
        mTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPersonalProducitivityActivity();
            }
        });
        mLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPersonalProducitivityActivity();
            }
        });
        mNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPersonalProducitivityActivity();
            }
        });
       initRecyclerView();
        */

        initBottomBar();
    }

    void toPersonalFinanceActivity() {
        Intent intent = new Intent(MainActivity.this, PersonalFinancialBoardFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toPersonalProducitivityActivity() {
        Intent intent = new Intent(MainActivity.this, PersonalProductivityBoard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toPersonalGratitudeActivity() {
        Intent intent = new Intent(MainActivity.this, PersonalGratitudeBoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toPersonalHabitActivity() {
        Intent intent = new Intent(MainActivity.this, PersonalHabitActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toPersonalLsActivity() {
        Intent intent = new Intent(MainActivity.this, PersonalStopwatchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_user_account:
//                return true;

            case R.id.action_new_board:
                //showBoardTypesDialog();
                newItemClicked();
                return true;
//
//            case R.id.action_other_board:
//                toOtherBoardsActivity();
//                return true;

            case R.id.action_Settings:
                toSettingsActivity();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    void chooseEntryTypeBoard() {
        final Dialog mEntryTypeDialog = new BottomSheetDialog(MainActivity.this);
        SharedPreferences sp = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        int theme = sp.getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            mEntryTypeDialog.setContentView(R.layout.dialog_financial_entry_type_option_layout_light);
        } else {
            mEntryTypeDialog.setContentView(R.layout.dialog_financial_entry_type_option_layout);
        }
        Button mIncomeType, mExpenseType;
        mIncomeType = mEntryTypeDialog.findViewById(R.id.income_type_button);
        mExpenseType = mEntryTypeDialog.findViewById(R.id.expense_type_button);

        ImageButton mClose = mEntryTypeDialog.findViewById(R.id.close_entry_option_dialog);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEntryTypeDialog.dismiss();
            }
        });

        mIncomeType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEntryTypeDialog.dismiss();
                toNewIncomeActivity();
            }
        });
        mExpenseType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEntryTypeDialog.dismiss();
                toNewExpenseActivity();
            }
        });

        mEntryTypeDialog.setCanceledOnTouchOutside(true);
        mEntryTypeDialog.show();
    }

    void toNewIncomeActivity() {
        Intent intent = new Intent(MainActivity.this, NewIncomeExpenseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toNewExpenseActivity() {
        Intent intent = new Intent(MainActivity.this, NewExpenseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toNewObjectiveActivity(){
        Intent intent = new Intent(MainActivity.this, NewObjectiveActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }
    void newItemClicked() {
        if (OPTION_TYPE == 1) {
            chooseEntryTypeBoard();
        } else if (OPTION_TYPE == 2) {
            toNewTodoActivity();
        } else if (OPTION_TYPE == 3) {
            toNewNoteActivity();
        } else if (OPTION_TYPE == 4) {
            toNewLinkActivity();
        } else if (OPTION_TYPE == 5) {
            toNewObjectiveActivity();
        }else if(OPTION_TYPE == 6){

        }
    }

    void toNewNoteActivity() {
        Intent intent = new Intent(MainActivity.this, PersonalNewNoteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toNewLinkActivity() {
        Intent intent = new Intent(MainActivity.this, PersonalNewLinkActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toNewTodoActivity() {
        Intent intent = new Intent(MainActivity.this, PersonalNewTodoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    int boardType = 0;

    void showBoardTypesDialog() {
        final BottomSheetDialog mBoardTypesDialog = new BottomSheetDialog(this);
        mBoardTypesDialog.setContentView(R.layout.dialog_board_types_layout);
        boardType = 0;
        Button mProdType, mFinancialType;
        ImageButton mClose;
        mClose = mBoardTypesDialog.findViewById(R.id.close_board_types_dialog);

        mProdType = mBoardTypesDialog.findViewById(R.id.prod_type);
        mFinancialType = mBoardTypesDialog.findViewById(R.id.financial_type);

        mProdType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boardType = BoardTypes.BOARD_TYPE_PRODUCTIVITY;
                mBoardTypesDialog.dismiss();
                showNewBoardDialog(boardType);
            }
        });
        mFinancialType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boardType = BoardTypes.BOARD_TYPE_FINANCIAL;
                mBoardTypesDialog.dismiss();
                showNewBoardDialog(boardType);
            }
        });

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBoardTypesDialog.dismiss();
            }
        });

        mBoardTypesDialog.setCanceledOnTouchOutside(false);
        mBoardTypesDialog.show();
    }

    void initBoards() {
        boardViewHolderClassArrayList = new ArrayList<>();
        boardPOJOArrayList = new ArrayList<>();
        boardsReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS_INFO + userInfoPOJO.getUser_key());
        boardsReference.keepSynced(true);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                BoardPOJO boardPOJO = dataSnapshot.getValue(BoardPOJO.class);
                boardPOJOArrayList.add(boardPOJO);
                // int position, MainActivity.BoardsAdapter boardsAdapter, BoardMembersActivity.MembersAdapter membersAdapter, ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList
                //  BoardViewHolderClass boardViewHolderClass = new BoardViewHolderClass((boardPOJOArrayList.size() - 1), boardsAdapter, new MembersViewAdapter((boardPOJOArrayList.size() - 1)), new ArrayList<BoardMembersPOJO>());
                // boardViewHolderClassArrayList.add(boardViewHolderClass);
                //boardsAdapter.notifyItemInserted(boardPOJOArrayList.size() - 1);
                fetchBoardMembers(boardPOJO, (boardPOJOArrayList.size() - 1));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        boardsReference.addChildEventListener(childEventListener);
    }

    void fetchBoardMembers(final BoardPOJO boardPOJO, final int position) {
        final ArrayList<BoardMembersPOJO> membersPOJOS = new ArrayList<>();
        final DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/members");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        BoardMembersPOJO membersPOJO = snapshot.getValue(BoardMembersPOJO.class);
                        membersPOJOS.add(membersPOJO);
                    }
                    BoardViewHolderClass boardViewHolderClass = boardViewHolderClassArrayList.get(position);
                    boardViewHolderClass.setBoardMembersPOJOArrayList(membersPOJOS);
                    boardViewHolderClassArrayList.set(position, boardViewHolderClass);
                    boardViewHolderClass.getMembersAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void initRecyclerView() {
        boardPOJOArrayList = new ArrayList<>();
//        mBoardsRecyclerView.setHasFixedSize(true);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        mBoardsRecyclerView.setLayoutManager(linearLayoutManager);
//        boardsAdapter = new BoardsAdapter();
//        mBoardsRecyclerView.setAdapter(boardsAdapter);


        initBoards();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (childEventListener != null) {
            boardsReference.removeEventListener(childEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (childEventListener != null) {
            boardsReference.removeEventListener(childEventListener);
        }
    }

    void toLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }


    void showNewBoardDialog(final int boardType) {
        mNewBoardDialog = new BottomSheetDialog(this);
        mNewBoardDialog.setContentView(R.layout.dialog_new_baord_layout);
        ImageButton mClose = mNewBoardDialog.findViewById(R.id.close_new_board_dialog);

        mTitle = mNewBoardDialog.findViewById(R.id.board_title);
        mDate = mNewBoardDialog.findViewById(R.id.board_date);
        mDone = mNewBoardDialog.findViewById(R.id.board_done);

        mDate.setText(DateTimeStamp.getDate());
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewBoardDialog.dismiss();
            }
        });

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitle.getText().toString().trim();
                if (!title.isEmpty()) {
                    createBoard(title, boardType);
                } else {
                    // TODO: Show empty field alert
                }
            }
        });

        mNewBoardDialog.setCanceledOnTouchOutside(false);
        mNewBoardDialog.show();
    }

    void createBoard(final String title, final int boardType) {
        mTitle.setEnabled(false);
        mDone.setEnabled(false);
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS);
        databaseReference.keepSynced(true);
        final DatabaseReference boardReference = databaseReference.push();
        boardReference.keepSynced(true);
        final String boardKey = boardReference.getKey();


        // initializing BoardPOJO class
        final BoardPOJO boardPOJO = new BoardPOJO(title, DateTimeStamp.getDate(), boardKey, boardType, userInfoPOJO);

        // TODO: add a progress bar
        boardReference.setValue(boardPOJO);
        // updating the user group information
        databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS_INFO + userInfoPOJO.getUser_key() + "/" + boardKey);
        databaseReference.keepSynced(true);
        databaseReference.setValue(boardPOJO);
        DatabaseReference databaseReferenceCat = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_BOARDS + userInfoPOJO.getUser_key() + "/" + boardKey);
        databaseReferenceCat.keepSynced(true);
        Log.d("kkkkkkkkkkkkkkkkkk", boardType + "");
        if (boardType == BoardTypes.BOARD_TYPE_FINANCIAL) {
            Log.d("kkkkkkkkkkkkkkkkkk", boardType + "========");

            DatabaseReference catReference = boardReference.child("categories").push();
            catReference.keepSynced(true);
            catReference.setValue(new Categories("Others", catReference.getKey(), 2005, 1, 0.0));

            DatabaseReference catReference2 = boardReference.child("categories").push();
            catReference2.keepSynced(true);
            catReference2.setValue(new Categories("Food and Drinks", catReference2.getKey(), 2002, 2, 0.0));

            DatabaseReference catReference3 = boardReference.child("categories").push();
            catReference3.keepSynced(true);
            catReference3.setValue(new Categories("Transport", catReference3.getKey(), 2000, 3, 0.0));

            DatabaseReference catReference4 = boardReference.child("categories").push();
            catReference4.keepSynced(true);
            catReference4.setValue(new Categories("Shopping", catReference4.getKey(), 2001, 4, 0.0));

            DatabaseReference catReference5 = boardReference.child("categories").push();
            catReference5.keepSynced(true);
            catReference5.setValue(new Categories("Leisure", catReference5.getKey(), 2006, 5, 0.0));

        }
        // initializing ActivityPOJO class
        ActivityPOJO activityPOJO = new ActivityPOJO("New Board Created", DateTimeStamp.getDate(), ActionType.ACTION_TYPE_CREATE_BOARD, userInfoPOJO);

        // pushing ActivityPOJO
        boardReference.child("activity").push().setValue(activityPOJO);
        mNewBoardDialog.dismiss();
        toBoardActivity(boardPOJO);
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
        Intent intent = null;
        if (boardPOJO.getBoardType() == BoardTypes.BOARD_TYPE_PRODUCTIVITY) {
            intent = new Intent(MainActivity.this, BoardActivity.class);
        } else if (boardPOJO.getBoardType() == BoardTypes.BOARD_TYPE_FINANCIAL) {
            intent = new Intent(MainActivity.this, FinancialBoardActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toAddMembers(BoardPOJO boardPOJO) {
        Intent intent = new Intent(MainActivity.this, AddBoardMembersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void toOtherBoardsActivity() {
        Intent intent = new Intent(MainActivity.this, OtherBoardsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public class BoardsAdapter extends RecyclerView.Adapter<BoardsAdapter.BoardsViewHolder> {

        private LayoutInflater inflator;

        public BoardsAdapter() {
            try {
                this.inflator = LayoutInflater.from(MainActivity.this);
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public BoardsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_boards_row_layout, parent, false);
            BoardsViewHolder viewHolder = new BoardsViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull BoardsViewHolder holder, int position) {
            BoardPOJO boardPOJO = boardPOJOArrayList.get(position);
            holder.boardTitle.setText(boardPOJO.getTitle());
            initBoardMembersRecyclerview(holder, position);
        }

        void initBoardMembersRecyclerview(BoardsViewHolder holder, int position) {
            holder.mMemberRecyclerview.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(holder.mMemberRecyclerview.getContext());
            holder.mMemberRecyclerview.setLayoutManager(linearLayoutManager);
            MembersViewAdapter membersAdapter = boardViewHolderClassArrayList.get(position).getMembersAdapter();
            holder.mMemberRecyclerview.setAdapter(membersAdapter);
        }

        @Override
        public int getItemCount() {
            return boardPOJOArrayList.size();
        }

        public class BoardsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public Button boardTitle;
            public ImageButton addMembers;

            public RecyclerView mMemberRecyclerview;

            public BoardsViewHolder(View itemView) {
                super(itemView);
                boardTitle = itemView.findViewById(R.id.boards_title_button);
                addMembers = itemView.findViewById(R.id.boards_add_member_button);
                mMemberRecyclerview = itemView.findViewById(R.id.boards_view_members_recyclerview);

                boardTitle.setOnClickListener(this);
                addMembers.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.boards_title_button:
                        toBoardActivity(boardPOJOArrayList.get(getPosition()));
                        break;
                    case R.id.boards_add_member_button:
                        toAddMembers(boardPOJOArrayList.get(getPosition()));
                        break;

                }
            }
        }
    }


    public class MembersViewAdapter extends RecyclerView.Adapter<MembersViewAdapter.MembersViewHolder> {

        private LayoutInflater inflator;
        int position;

        public MembersViewAdapter(int position) {
            try {
                this.inflator = LayoutInflater.from(MainActivity.this);
                this.position = position;
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_boards_view_members_row_layout, parent, false);
            MembersViewHolder viewHolder = new MembersViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MembersViewHolder holder, int position) {
            holder.mName.setText(boardViewHolderClassArrayList.get(this.position).getBoardMembersPOJOArrayList().get(position).getUserInfoPOJO().getName());
            holder.mEmail.setText(boardViewHolderClassArrayList.get(this.position).getBoardMembersPOJOArrayList().get(position).getUserInfoPOJO().getEmail());
            Glide.with(MainActivity.this).load(boardViewHolderClassArrayList.get(this.position).getBoardMembersPOJOArrayList().get(position).getUserInfoPOJO().getProfile_url()).into(holder.mProfile);
        }

        @Override
        public int getItemCount() {
            return boardViewHolderClassArrayList.get(position).getBoardMembersPOJOArrayList().size();
        }

        class MembersViewHolder extends RecyclerView.ViewHolder {
            public TextView mName, mEmail;
            public CircleImageView mProfile;

            public MembersViewHolder(View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.name_board_members_view_textview);
                mEmail = itemView.findViewById(R.id.email_board_members_view_textview);
                mProfile = itemView.findViewById(R.id.profile_image_board_members_view);
            }
        }
    }

}
