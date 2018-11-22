package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.everyday.skara.everyday.classes.FinanceBoardExpense;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.fragments.PersonalNotesFragment;
import com.everyday.skara.everyday.pojo.BoardExpensePOJO;
import com.everyday.skara.everyday.pojo.BoardMembersPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.Categories;
import com.everyday.skara.everyday.pojo.ExpenseMembersInfoPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewBoardExpenseActivity extends AppCompatActivity implements View.OnClickListener, com.philliphsu.bottomsheetpickers.date.DatePickerDialog.OnDateSetListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabas = FirebaseDatabase.getInstance();
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;

    EditText mDescription;
    EditText mExpenseAmount;
    TextView mExpenseEntryDate;
    ImageButton mChooseDateImageButton;
    EditText mTransactionId;
    EditText mNote;
    Button expenseTypePersonal, expenseTypeEveryone, expenseTypeSpecific;
    Button mDoneExpenseEntry;
    BottomSheetDialog mSpecificMembersSelectionDialog;
    boolean isMemberSelected = false;
    String date;
    int day, month, year;
    int expenseType = FinanceBoardExpense.EXPENSE_TYPE_EVERYONE;

    Button mCategoryChoiceOption;
    BottomSheetDialog mCategoriesDialog;

    ArrayList<Categories> categoriesArrayList;
    ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList;

    Categories selectedCat;
    ArrayList<ExpenseMembersInfoPOJO> expenseMembersInfoPOJOS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_board_expense);
        Toolbar myToolbar = findViewById(R.id.new_expense_toolbar);
        setSupportActionBar(myToolbar);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void init() {
        Intent intent = getIntent();
//        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        mDescription = findViewById(R.id.expense_description_edittext);
        mExpenseAmount = findViewById(R.id.amount_edittext);
        mExpenseEntryDate = findViewById(R.id.expense_entry_date_textview);
        mChooseDateImageButton = findViewById(R.id.choose_expense_entry_date_image_button);
        mTransactionId = findViewById(R.id.transaction_id_edittext);
        mNote = findViewById(R.id.expense_note_edittext);
        mDoneExpenseEntry = findViewById(R.id.done_expense_button);

        mCategoryChoiceOption = findViewById(R.id.category_choose_option);

        expenseTypePersonal = findViewById(R.id.board_expense_personal);
        expenseTypeEveryone = findViewById(R.id.board_expense_everyone);
        expenseTypeSpecific = findViewById(R.id.board_expense_specific);

        mChooseDateImageButton.setOnClickListener(this);
        mDoneExpenseEntry.setOnClickListener(this);
        expenseTypePersonal.setOnClickListener(this);
        expenseTypeSpecific.setOnClickListener(this);
        expenseTypeEveryone.setOnClickListener(this);

        date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        month = Calendar.getInstance().get(Calendar.MONTH);
        year = Calendar.getInstance().get(Calendar.YEAR);
        mExpenseEntryDate.setText(date);

        mCategoryChoiceOption.setOnClickListener(this);
        initMembers();

    }

    void initMembers() {
        boardMembersPOJOArrayList = new ArrayList<>();
        DatabaseReference memberDatabaseReference = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/members/");
        memberDatabaseReference.keepSynced(true);
        memberDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        boardMembersPOJOArrayList.add(snapshot.getValue(BoardMembersPOJO.class));
                    }
                    initCategories();
                } else {
                    initCategories();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void initCategories() {
        categoriesArrayList = new ArrayList<>();
        DatabaseReference databaseReference = firebaseDatabas.getReference(FirebaseReferences.FIREBASE_BOARDS + "/" + boardPOJO.getBoardKey() + "/categories");
        databaseReference.keepSynced(true);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Categories categories = snapshot.getValue(Categories.class);
                    categoriesArrayList.add(categories);
                }
                selectedCat = categoriesArrayList.get(0);
                mCategoryChoiceOption.setText(selectedCat.getCategoryName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    void toLoginActivity() {
        Intent intent = new Intent(NewBoardExpenseActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.choose_expense_entry_date_image_button:
                DialogFragment dialog = createDialog();
                dialog.show(getSupportFragmentManager(), "date");
                break;

            case R.id.done_expense_button:
                expenseDoneClicked();
                break;

            case R.id.category_choose_option:
                showCategoryChoiceDialog();
                break;

            case R.id.board_expense_personal:
                expenseType = FinanceBoardExpense.EXPENSE_TYPE_PERSONAL;
                break;
            case R.id.board_expense_everyone:
                expenseType = FinanceBoardExpense.EXPENSE_TYPE_EVERYONE;
                break;
            case R.id.board_expense_specific:
                // expenseType = FinanceBoardExpense.EXPENSE_TYPE_SPECIFIC;
                showBoardMembers();
                break;
        }

    }

    void showBoardMembers() {
        expenseMembersInfoPOJOS = new ArrayList<>();
        mSpecificMembersSelectionDialog = new BottomSheetDialog(this);
        mSpecificMembersSelectionDialog.setContentView(R.layout.dialog_specific_members_selection_layout);
        ImageButton mClose = mSpecificMembersSelectionDialog.findViewById(R.id.close_member_selection_dialog);
        RecyclerView mSpecificMembersRecyclerview = mSpecificMembersSelectionDialog.findViewById(R.id.recyclerview_choose_specific_members);

        isMemberSelected = false;
        mSpecificMembersRecyclerview.invalidate();
        mSpecificMembersRecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mSpecificMembersRecyclerview.setLayoutManager(linearLayoutManager);
        SpecificMemberAdapter specificMemberAdapter = new SpecificMemberAdapter();
        mSpecificMembersRecyclerview.setAdapter(specificMemberAdapter);

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpecificMembersSelectionDialog.dismiss();
            }
        });


        mSpecificMembersSelectionDialog.setCanceledOnTouchOutside(false);
        mSpecificMembersSelectionDialog.show();

    }

    public class SpecificMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;

        public SpecificMemberAdapter() {
            try {
                this.inflator = LayoutInflater.from(mSpecificMembersSelectionDialog.getContext());
                isMemberSelected = false;
                expenseMembersInfoPOJOS = new ArrayList<>();
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_specific_member_row_layout, parent, false);
            return new SpecificMemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            BoardMembersPOJO boardMembersPOJO = boardMembersPOJOArrayList.get(position);
            UserInfoPOJO userInfoPOJO = boardMembersPOJO.getUserInfoPOJO();

            ((SpecificMemberViewHolder) holder).mName.setText(userInfoPOJO.getName());
            ((SpecificMemberViewHolder) holder).mEmail.setText(userInfoPOJO.getName());
            Glide.with(NewBoardExpenseActivity.this).load(userInfoPOJO.getProfile_url()).into(((SpecificMemberViewHolder) holder).mProfile);

        }

        @Override
        public int getItemCount() {
            return boardMembersPOJOArrayList.size();
        }

        public class SpecificMemberViewHolder extends RecyclerView.ViewHolder {
            public TextView mName, mEmail;
            public CircleImageView mProfile;
            public CheckBox mCheckbox;

            public SpecificMemberViewHolder(View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.name_board_members_view_textview);
                mEmail = itemView.findViewById(R.id.email_board_members_view_textview);
                mProfile = itemView.findViewById(R.id.profile_image_board_members_view);
                mCheckbox = itemView.findViewById(R.id.member_checkbox);
                mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (mCheckbox.isPressed()) {
                            selectMember(getPosition(), isChecked);
                        }
                    }
                });
            }
        }
    }

    void selectMember(int position, boolean state) {
        ExpenseMembersInfoPOJO expenseMembersInfoPOJO = new ExpenseMembersInfoPOJO(boardMembersPOJOArrayList.get(position), false);
        if (state) {
            if (!expenseMembersInfoPOJOS.contains(expenseMembersInfoPOJO)) {
                expenseMembersInfoPOJOS.add(expenseMembersInfoPOJO);
            }
        } else {
            if (expenseMembersInfoPOJOS.contains(expenseMembersInfoPOJO)) {
                expenseMembersInfoPOJOS.remove(position);
            }
        }
        if (expenseMembersInfoPOJOS.size() > 0) {
            isMemberSelected = true;
            expenseType = FinanceBoardExpense.EXPENSE_TYPE_SPECIFIC;
        } else {
            isMemberSelected = false;
            expenseType = FinanceBoardExpense.EXPENSE_TYPE_PERSONAL;

        }
    }

    void showCategoryChoiceDialog() {
        mCategoriesDialog = new BottomSheetDialog(this);
        mCategoriesDialog.setContentView(R.layout.dialog_choose_category_layout);
        ImageButton mClose = mCategoriesDialog.findViewById(R.id.close_cat_option_dialog);
        RecyclerView mCategoriesRecyclerView = mCategoriesDialog.findViewById(R.id.recyclerview_choose_category);

        mCategoriesRecyclerView.invalidate();
        mCategoriesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mCategoriesRecyclerView.setLayoutManager(linearLayoutManager);
        CatAdapter catAdapter = new CatAdapter();
        mCategoriesRecyclerView.setAdapter(catAdapter);

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategoriesDialog.dismiss();
            }
        });


        mCategoriesDialog.setCanceledOnTouchOutside(false);
        mCategoriesDialog.show();
    }

    void updateCategorySelected(int position) {
        selectedCat = categoriesArrayList.get(position);
        mCategoryChoiceOption.setText(selectedCat.getCategoryName());

    }

    public class CatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;
        //ArrayList<Categories> categoriesArrayList;

        public CatAdapter() {
            try {
                this.inflator = LayoutInflater.from(mCategoriesDialog.getContext());
                // this.categoriesArrayList = categoriesArrayList;
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_expense_catgories_row_layout, parent, false);
            return new CatAdapter.CatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Categories categories = categoriesArrayList.get(position);
            ((CatAdapter.CatViewHolder) holder).mCatName.setText(categories.getCategoryName());
            showCatIcon(holder, categories);
        }


        void showCatIcon(@NonNull RecyclerView.ViewHolder holder, Categories categories) {
            switch (categories.getCategoryIconId()) {
                case 2000:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2000);
                    break;
                case 2001:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2001);
                    break;
                case 2002:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2002);
                    break;
                case 2003:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2003);
                    break;
                case 2004:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2004);
                    break;
                case 2005:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2005);
                    break;
                case 2006:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2006);
                    break;
                case 2007:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2007);
                    break;

                case 2008:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2008);
                    break;

                case 2009:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2009);
                    break;

                case 2010:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2010);
                    break;

                case 2011:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2011);
                    break;

                case 2012:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2012);
                    break;

                case 2013:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2013);
                    break;

                case 2014:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2014);
                    break;

                case 2015:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2015);
                    break;

                case 2016:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2016);
                    break;

                case 2017:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2017);
                    break;

                case 2018:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2018);
                    break;

                case 2019:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2019);
                    break;

                case 2020:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2020);
                    break;

                case 2021:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2021);
                    break;

                case 2022:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2022);
                    break;

                case 2023:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2023);
                    break;

                case 2024:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2024);
                    break;

                case 2025:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2025);
                    break;

                case 2026:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2026);
                    break;

                case 2027:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2027);
                    break;

                case 2028:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2028);
                    break;

                case 2029:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2029);
                    break;

                case 2030:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2030);
                    break;
                case 2031:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2031);
                    break;
                case 2032:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2032);
                    break;
                case 2033:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2033);
                    break;
                case 2034:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2034);
                    break;
                case 2035:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2035);
                    break;
                case 2036:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2036);
                    break;
                case 2037:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2037);
                    break;
                case 2038:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2038);
                    break;
                case 2039:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2039);
                    break;
                case 2040:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2040);
                    break;
                case 2041:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2041);
                    break;
                case 2042:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2042);
                    break;
                case 2043:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2043);
                    break;
                case 2044:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2044);
                    break;
                case 2045:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2045);
                    break;
                case 2046:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2046);
                    break;
                case 2047:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2047);
                    break;
                default:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2000);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return categoriesArrayList.size();
        }

        public class CatViewHolder extends RecyclerView.ViewHolder {
            public Button mCatName;
            public ImageButton mCatIcon;

            public CatViewHolder(View itemView) {
                super(itemView);
                mCatName = itemView.findViewById(R.id.category_name_row_textview);
                mCatIcon = itemView.findViewById(R.id.expense_cat_icon_row);
                mCatName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateCategorySelected(getPosition());
                    }
                });
            }
        }

    }


    void expenseDoneClicked() {
        String description = mDescription.getText().toString().trim();
        String amount = mExpenseAmount.getText().toString().trim();
        String transactionId = mTransactionId.getText().toString().trim();
        String notes = mNote.getText().toString().trim();
        if (description.isEmpty()) {
            Toast.makeText(this, "description empty", Toast.LENGTH_SHORT).show();
        } else {
            if (amount.isEmpty()) {
                Toast.makeText(this, "amount empty", Toast.LENGTH_SHORT).show();
            } else {
                if (transactionId.isEmpty()) {
                    transactionId = " ";
                }
                if (notes.isEmpty()) {
                    notes = " ";
                }

                DatabaseReference newExpenseDatabaseReference = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_BOARDS + "/" + boardPOJO.getBoardKey() + "/expenses").push();
                newExpenseDatabaseReference.keepSynced(true);

                if (expenseType == FinanceBoardExpense.EXPENSE_TYPE_EVERYONE) {
                    expenseMembersInfoPOJOS = new ArrayList<>();
                    for (int i = 0; i < boardMembersPOJOArrayList.size(); i++) {
                        expenseMembersInfoPOJOS.add(new ExpenseMembersInfoPOJO(boardMembersPOJOArrayList.get(i), false));
                    }
                }
                //String entryKey, Double amount, String description, String date, String expenseType, ArrayList<BoardMembersPOJO> sharedByArrayList, String note, String transactionId, int year, int month, int day
                BoardExpensePOJO boardExpensePOJO = new BoardExpensePOJO(newExpenseDatabaseReference.getKey(), Double.valueOf(amount), description, date, notes, transactionId, year, month, day, selectedCat, userInfoPOJO, expenseMembersInfoPOJOS, expenseType);
                newExpenseDatabaseReference.setValue(boardExpensePOJO);
                toFinancialActivity();
            }
        }


    }

    void toFinancialActivity() {
        Intent intent = new Intent(NewBoardExpenseActivity.this, FinancialBoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    /**
     * Bottom sheet picker for date and time
     * [STARTS HERE]
     */
//    @Override
//    public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
//        Calendar cal = new java.util.GregorianCalendar();
//        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
//        cal.set(Calendar.MINUTE, minute);
//        time = DateFormat.getTimeFormat(this).format(cal.getTime());
//        if (!(time.isEmpty() || time.equals(""))) {
//
//            hours = hourOfDay;
//            minutes = minute;
//
//            //mReminder.setText("Reminder set at " + time + " on " + date);
//            setReminder();
//        }
//    }
    @Override
    public void onDateSet(com.philliphsu.bottomsheetpickers.date.DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        //   new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date())
        date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());

        if (!(date.isEmpty() || date.equals(""))) {
            this.year = year;
            this.month = monthOfYear;
            this.day = dayOfMonth;

            mExpenseEntryDate.setText(date);

//            // calling the time dialog
//            DialogFragment dialog1 = createTimeDialog();
//            dialog1.show(getSupportFragmentManager(), "time");
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private DialogFragment createDialog() {
        return createDialogWithSetters();
    }

//    private DialogFragment createTimeDialog() {
//        return createTimeDialogWithSetters();
//    }

//    private DialogFragment createTimeDialogWithSetters() {
//        BottomSheetPickerDialog dialog = null;
//        boolean custom = false;
//        boolean customDark = false;
//        boolean themeDark = true;
//
//        Calendar now = Calendar.getInstance();
//        dialog = GridTimePickerDialog.newInstance(
//                TodoActivity.this,
//                now.get(Calendar.HOUR_OF_DAY),
//                now.get(Calendar.MINUTE),
//                DateFormat.is24HourFormat(TodoActivity.this));
//        GridTimePickerDialog gridDialog = (GridTimePickerDialog) dialog;
//        dialog.setThemeDark(themeDark);
//
//        return dialog;
//    }

    private DialogFragment createDialogWithSetters() {
        BottomSheetPickerDialog dialog = null;
        boolean themeDark = true;

        Calendar now = Calendar.getInstance();
        dialog = com.philliphsu.bottomsheetpickers.date.DatePickerDialog.newInstance(
                NewBoardExpenseActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));

        com.philliphsu.bottomsheetpickers.date.DatePickerDialog dateDialog = (com.philliphsu.bottomsheetpickers.date.DatePickerDialog) dialog;
        dateDialog.setYearRange(1900, 3000);
        dialog.setThemeDark(themeDark);

        return dialog;
    }
    //[ENDS HERE]
}

