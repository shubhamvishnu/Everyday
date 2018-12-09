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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.classes.ExpenseTypes;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.fragments.PersonalFinanceCategoriesFragment;
import com.everyday.skara.everyday.pojo.Categories;
import com.everyday.skara.everyday.pojo.FinanceEntryPOJO;
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

public class NewExpenseActivity extends AppCompatActivity implements View.OnClickListener, com.philliphsu.bottomsheetpickers.date.DatePickerDialog.OnDateSetListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabas = FirebaseDatabase.getInstance();
    //    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;

    EditText mDescription;
    EditText mExpenseAmount;
    TextView mExpenseEntryDate;
    ImageButton mChooseDateImageButton;
    EditText mTransactionId;
    EditText mNote;
    Button mDoneExpenseEntry;
    ImageButton mCategoryImage;
    Button mCatNameButton;

    String date;
    int day, month, year;

    BottomSheetDialog mCategoriesDialog;

    ArrayList<Categories> categoriesArrayList;

    Categories selectedCat;


    void initCategories() {
        categoriesArrayList = new ArrayList<>();
        final DatabaseReference databaseReference = firebaseDatabas.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/categories");
        databaseReference.keepSynced(true);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Categories categories = snapshot.getValue(Categories.class);
                    if (categories.getCategoryIconId() != 2048) {
                        categoriesArrayList.add(categories);
                    }
                }
                selectedCat = categoriesArrayList.get(0);
                updateCategoryView(selectedCat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_expense);
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
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        mDescription = findViewById(R.id.expense_description_edittext);
        mExpenseAmount = findViewById(R.id.amount_edittext);
        mExpenseEntryDate = findViewById(R.id.expense_entry_date_textview);
        mChooseDateImageButton = findViewById(R.id.choose_expense_entry_date_image_button);
        mTransactionId = findViewById(R.id.transaction_id_edittext);
        mNote = findViewById(R.id.expense_note_edittext);
        mDoneExpenseEntry = findViewById(R.id.done_expense_button);

        mCategoryImage = findViewById(R.id.expense_cat_icon_new_expense);
        mCatNameButton = findViewById(R.id.cat_name_text_view_new_expense);

        mChooseDateImageButton.setOnClickListener(this);
        mDoneExpenseEntry.setOnClickListener(this);

        date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        month = Calendar.getInstance().get(Calendar.MONTH);
        year = Calendar.getInstance().get(Calendar.YEAR);
        mExpenseEntryDate.setText(date);

        mCatNameButton.setOnClickListener(this);
        initCategories();

    }

    void toLoginActivity() {
        Intent intent = new Intent(NewExpenseActivity.this, LoginActivity.class);
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

            case R.id.cat_name_text_view_new_expense:
                showCategoryChoiceDialog();
                break;
        }

    }

    void updateCategoryView(Categories categories) {
        mCatNameButton.setText(categories.getCategoryName());
        switch (categories.getColorId()) {
            case 1:
                mCategoryImage.setBackgroundResource(R.drawable.circle_background_red);
                break;
            case 2:
                mCategoryImage.setBackgroundResource(R.drawable.circle_background_yellow);
                break;
            case 3:
                mCategoryImage.setBackgroundResource(R.drawable.circle_background_blue);
                break;
            case 4:
                mCategoryImage.setBackgroundResource(R.drawable.circle_background_green);
                break;
            case 5:
                mCategoryImage.setBackgroundResource(R.drawable.circle_background_green_blue);
                break;
            case 6:
                mCategoryImage.setBackgroundResource(R.drawable.circle_background_pink);
                break;
            default:
                mCategoryImage.setBackgroundResource(R.drawable.circle_background_blue);
                break;
        }

        switch (categories.getCategoryIconId()) {
            case 2000:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2000);
                break;
            case 2001:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2001);
                break;
            case 2002:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2002);
                break;
            case 2003:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2003);
                break;
            case 2004:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2004);
                break;
            case 2005:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2005);
                break;
            case 2006:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2006);
                break;
            case 2007:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2007);
                break;

            case 2008:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2008);
                break;

            case 2009:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2009);
                break;

            case 2010:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2010);
                break;

            case 2011:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2011);
                break;

            case 2012:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2012);
                break;

            case 2013:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2013);
                break;

            case 2014:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2014);
                break;

            case 2015:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2015);
                break;

            case 2016:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2016);
                break;

            case 2017:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2017);
                break;

            case 2018:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2018);
                break;

            case 2019:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2019);
                break;

            case 2020:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2020);
                break;

            case 2021:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2021);
                break;

            case 2022:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2022);
                break;

            case 2023:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2023);
                break;

            case 2024:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2024);
                break;

            case 2025:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2025);
                break;

            case 2026:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2026);
                break;

            case 2027:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2027);
                break;

            case 2028:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2028);
                break;

            case 2029:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2029);
                break;

            case 2030:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2030);
                break;
            case 2031:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2031);
                break;
            case 2032:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2032);
                break;
            case 2033:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2033);
                break;
            case 2034:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2034);
                break;
            case 2035:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2035);
                break;
            case 2036:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2036);
                break;
            case 2037:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2037);
                break;
            case 2038:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2038);
                break;
            case 2039:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2039);
                break;
            case 2040:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2040);
                break;
            case 2041:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2041);
                break;
            case 2042:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2042);
                break;
            case 2043:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2043);
                break;
            case 2044:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2044);
                break;
            case 2045:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2045);
                break;
            case 2046:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2046);
                break;
            case 2047:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2047);
                break;
            default:
                mCategoryImage.setImageResource(R.drawable.ic_cat_2000);
                break;
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

        mCategoriesDialog.setCanceledOnTouchOutside(true);
        mCategoriesDialog.show();
    }

    void updateCategorySelected(int position) {
        selectedCat = categoriesArrayList.get(position);
        updateCategoryView(selectedCat);

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
            return new CatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Categories categories = categoriesArrayList.get(position);
            ((CatViewHolder) holder).mCatName.setText(categories.getCategoryName());
            showCatIcon(holder, categories);
        }


        void showCatIcon(@NonNull RecyclerView.ViewHolder holder, Categories categories) {
            switch (categories.getCategoryIconId()) {
                case 2000:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2000);
                    break;
                case 2001:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2001);
                    break;
                case 2002:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2002);
                    break;
                case 2003:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2003);
                    break;
                case 2004:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2004);
                    break;
                case 2005:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2005);
                    break;
                case 2006:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2006);
                    break;
                case 2007:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2007);
                    break;

                case 2008:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2008);
                    break;

                case 2009:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2009);
                    break;

                case 2010:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2010);
                    break;

                case 2011:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2011);
                    break;

                case 2012:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2012);
                    break;

                case 2013:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2013);
                    break;

                case 2014:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2014);
                    break;

                case 2015:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2015);
                    break;

                case 2016:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2016);
                    break;

                case 2017:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2017);
                    break;

                case 2018:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2018);
                    break;

                case 2019:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2019);
                    break;

                case 2020:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2020);
                    break;

                case 2021:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2021);
                    break;

                case 2022:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2022);
                    break;

                case 2023:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2023);
                    break;

                case 2024:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2024);
                    break;

                case 2025:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2025);
                    break;

                case 2026:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2026);
                    break;

                case 2027:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2027);
                    break;

                case 2028:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2028);
                    break;

                case 2029:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2029);
                    break;

                case 2030:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2030);
                    break;
                case 2031:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2031);
                    break;
                case 2032:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2032);
                    break;
                case 2033:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2033);
                    break;
                case 2034:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2034);
                    break;
                case 2035:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2035);
                    break;
                case 2036:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2036);
                    break;
                case 2037:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2037);
                    break;
                case 2038:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2038);
                    break;
                case 2039:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2039);
                    break;
                case 2040:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2040);
                    break;
                case 2041:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2041);
                    break;
                case 2042:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2042);
                    break;
                case 2043:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2043);
                    break;
                case 2044:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2044);
                    break;
                case 2045:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2045);
                    break;
                case 2046:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2046);
                    break;
                case 2047:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2047);
                    break;
                default:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2000);
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
                        if(mCategoriesDialog != null && mCategoriesDialog.isShowing()){
                            mCategoriesDialog.dismiss();
                        }
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

                DatabaseReference newExpenseDatabaseReference = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "expenses").push();
                //String entryKey, Double amount, String description, String date, String expenseType, ArrayList<BoardMembersPOJO> sharedByArrayList, String note, String transactionId, int year, int month, int day
                FinanceEntryPOJO expensePOJO = new FinanceEntryPOJO(newExpenseDatabaseReference.getKey(), Double.valueOf(amount), description, date, notes, transactionId, year, month, day, ExpenseTypes.ENTRY_TYPE_EXPENSE, selectedCat, userInfoPOJO);
                newExpenseDatabaseReference.setValue(expensePOJO);
                toFinancialActivity();
            }
        }


    }

    void toFinancialActivity() {
        Intent intent = new Intent(NewExpenseActivity.this, PersonalFinancialBoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra("board_pojo", boardPOJO);
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
                NewExpenseActivity.this,
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

