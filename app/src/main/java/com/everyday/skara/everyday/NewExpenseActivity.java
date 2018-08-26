package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.classes.BoardViewHolderClass;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.ExpenseType;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.TimeDateStamp;
import com.everyday.skara.everyday.pojo.BoardMembersPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.ExpensePOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;
import com.philliphsu.bottomsheetpickers.date.DatePickerDialog;
import com.philliphsu.bottomsheetpickers.time.grid.GridTimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewExpenseActivity extends AppCompatActivity implements View.OnClickListener, com.philliphsu.bottomsheetpickers.date.DatePickerDialog.OnDateSetListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;

    int expenseType = ExpenseType.TYPE_SHARED_EVERYONE;
    EditText mDescription;
    EditText mExpenseAmount;
    TextView mExpenseEntryDate;
    ImageButton mChooseDateImageButton;
    EditText mTransactionId;
    EditText mNote;
    Button mExpenseType;
    Button mDoneExpenseEntry;

    BottomSheetDialog mExpenseTypeDialog;

    String date;
    int day, month, year;

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
        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");

        mDescription = findViewById(R.id.expense_description_edittext);
        mExpenseAmount = findViewById(R.id.amount_edittext);
        mExpenseEntryDate = findViewById(R.id.expense_entry_date_textview);
        mChooseDateImageButton = findViewById(R.id.choose_expense_entry_date_image_button);
        mTransactionId = findViewById(R.id.transaction_id_edittext);
        mExpenseType = findViewById(R.id.expense_type_button);
        mNote = findViewById(R.id.expense_note_edittext);
        mExpenseType.setText("Share with everyone");
        mDoneExpenseEntry = findViewById(R.id.done_expense_button);

        mChooseDateImageButton.setOnClickListener(this);
        mExpenseType.setOnClickListener(this);
        mDoneExpenseEntry.setOnClickListener(this);

        date = new String("");
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        month = Calendar.getInstance().get(Calendar.MONTH);
        year = Calendar.getInstance().get(Calendar.YEAR);
        mExpenseEntryDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));


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
            case R.id.expense_type_button:
                showExpenseTypeDialog();
                break;

            case R.id.done_expense_button:
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

                DatabaseReference newExpenseDatabaseReference = firebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/expenses/").push();
                //String entryKey, Double amount, String description, String date, String expenseType, ArrayList<BoardMembersPOJO> sharedByArrayList, String note, String transactionId, int year, int month, int day
                ExpensePOJO expensePOJO = new ExpensePOJO(newExpenseDatabaseReference.getKey(), Double.valueOf(amount), description, date, expenseType, new ArrayList<BoardMembersPOJO>(), notes, transactionId, year, month, day, userInfoPOJO, new ArrayList<BoardMembersPOJO>());
                newExpenseDatabaseReference.setValue(expensePOJO);
                toFinancialActivity();
            }
        }


    }

    void toFinancialActivity() {
        Intent intent = new Intent(NewExpenseActivity.this, FinancialBoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile", userInfoPOJO);
        startActivity(intent);
    }

    void showExpenseTypeDialog() {
        mExpenseTypeDialog = new BottomSheetDialog(this);
        mExpenseTypeDialog.setContentView(R.layout.dialog_choose_board_type_layout);
        Button mPersonal, mEveryone;
        ImageButton mClose = mExpenseTypeDialog.findViewById(R.id.close_board_type_options_dialog);
        Button mDone = mExpenseTypeDialog.findViewById(R.id.done_choose_board_type);

        mEveryone = mExpenseTypeDialog.findViewById(R.id.share_everyone_expense_type);
        mPersonal = mExpenseTypeDialog.findViewById(R.id.personal_expense_type);

        mEveryone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expenseType = ExpenseType.TYPE_SHARED_EVERYONE;
                mExpenseType.setText("Share with everyone");
            }
        });
        mPersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expenseType = ExpenseType.TYPE_PERSONAL;
                mExpenseType.setText("Personal expense");
            }
        });
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpenseTypeDialog.dismiss();
            }
        });

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpenseTypeDialog.dismiss();
            }
        });

        mExpenseTypeDialog.setCanceledOnTouchOutside(false);
        mExpenseTypeDialog.show();
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

        date = DateFormat.getDateFormat(this).format(cal.getTime());

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
        Calendar minCalendar = TimeDateStamp.getCalendar("dd/MM/yyyy", "29/07/2018");
        Calendar maxCalendar = TimeDateStamp.getCalendar("dd/MM/yyyy", "29/07/2025");
        dateDialog.setMinDate(minCalendar);
        dateDialog.setMaxDate(maxCalendar);
        dateDialog.setYearRange(1920, 2050);
        dialog.setThemeDark(themeDark);

        return dialog;
    }
    //[ENDS HERE]
}

