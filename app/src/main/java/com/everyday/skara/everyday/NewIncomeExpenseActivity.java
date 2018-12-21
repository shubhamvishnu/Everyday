package com.everyday.skara.everyday;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.everyday.skara.everyday.classes.SPNames;
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

public class NewIncomeExpenseActivity extends AppCompatActivity implements View.OnClickListener, com.philliphsu.bottomsheetpickers.date.DatePickerDialog.OnDateSetListener {
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
    String date;
    int day, month, year;
    ArrayList<Categories> categoriesArrayList;
    ImageButton mCatIcon;
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
                    if (categories.getCategoryIconId() == 2048) {
                        categoriesArrayList.add(categories);
                    }
                }
                selectedCat = categoriesArrayList.get(0);
                updateIncomeCat();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    void updateIncomeCat(){
        mCatIcon.setBackgroundResource(R.drawable.circle_background_red);
        mCatIcon.setImageResource(R.drawable.ic_cat_2048);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_income_expense);
        Toolbar myToolbar = findViewById(R.id.new_income_toolbar);
        setSupportActionBar(myToolbar);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }
    @Override
    public void onBackPressed() {
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("item_selected", 1);
        editor.apply();
        toMainActivity();
    }

    void toMainActivity(){
        Intent intent = new Intent(NewIncomeExpenseActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    void init() {
        Intent intent = getIntent();
//        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        mDescription = findViewById(R.id.income_description_edittext);
        mExpenseAmount = findViewById(R.id.income_amount_edittext);
        mExpenseEntryDate = findViewById(R.id.income_entry_date_textview);
        mChooseDateImageButton = findViewById(R.id.choose_income_entry_date_image_button);
        mTransactionId = findViewById(R.id.income_transaction_id_edittext);
        mNote = findViewById(R.id.income_note_edittext);
        mDoneExpenseEntry = findViewById(R.id.done_income_button);
        mCatIcon = findViewById(R.id.expense_cat_icon_new_income);


        mChooseDateImageButton.setOnClickListener(this);
        mDoneExpenseEntry.setOnClickListener(this);

        date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        month = Calendar.getInstance().get(Calendar.MONTH);
        year = Calendar.getInstance().get(Calendar.YEAR);
        mExpenseEntryDate.setText(date);

        initCategories();

    }

    void toLoginActivity() {
        Intent intent = new Intent(NewIncomeExpenseActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.choose_income_entry_date_image_button:
                DialogFragment dialog = createDialog();
                dialog.show(getSupportFragmentManager(), "date");
                break;

            case R.id.done_income_button:
                expenseDoneClicked();
                break;
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
                FinanceEntryPOJO expensePOJO = new FinanceEntryPOJO(newExpenseDatabaseReference.getKey(), Double.valueOf(amount), description, date, notes, transactionId, year, month, day, ExpenseTypes.ENTRY_TYPE_INCOME, selectedCat, userInfoPOJO);
                newExpenseDatabaseReference.setValue(expensePOJO);
                toMainActivity();
            }
        }


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
                this,
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

