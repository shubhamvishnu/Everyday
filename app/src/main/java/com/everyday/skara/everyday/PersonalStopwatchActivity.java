package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PersonalStopwatchActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    UserInfoPOJO userInfoPOJO;
    BottomSheetDialog mNewLapSequenceDialog;
    ArrayList<String> mNewLsArrayList = new ArrayList<>();
    LsAdapter mNewLsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_stopwatch);
        Toolbar myToolbar = findViewById(R.id.stopwatch_toolbar);
        setSupportActionBar(myToolbar);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void init() {
        Intent intent = getIntent();
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_personal_prod_activity, menu);
        return true;
    }

    void showCreateLapSequenceDialog() {
        mNewLsArrayList = new ArrayList<>();
        ImageButton mClose;
        Button mDone, m5;
        EditText mTitle;
        RecyclerView mLsRecyclerView;

        mNewLapSequenceDialog = new BottomSheetDialog(this);
        mNewLapSequenceDialog.setContentView(R.layout.dialog_new_lap_sequence_layout);

        mTitle = mNewLapSequenceDialog.findViewById(R.id.new_ls_title);
        m5 = mNewLapSequenceDialog.findViewById(R.id.new_ls_5_button);
        mLsRecyclerView = mNewLapSequenceDialog.findViewById(R.id.new_ls_recyclerview);
        mDone = mNewLapSequenceDialog.findViewById(R.id.done_new_ls_dialog);
        mClose = mNewLapSequenceDialog.findViewById(R.id.close_new_ls_dialog);

        mLsRecyclerView.invalidate();
        mLsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mNewLapSequenceDialog.getContext());
        mLsRecyclerView.setLayoutManager(linearLayoutManager);
        mNewLsAdapter = new LsAdapter();
        mLsRecyclerView.setAdapter(mNewLsAdapter);

        m5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Add the number of seconds (or milliseconds);
                mNewLsArrayList.add("5");
                mNewLsAdapter.notifyDataSetChanged();
            }
        });
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewLapSequenceDialog.dismiss();
            }
        });

        mNewLapSequenceDialog.setCanceledOnTouchOutside(false);
        mNewLapSequenceDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_menu_item:
                showCreateLapSequenceDialog();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public class LsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;

        public LsAdapter() {
            try {
                this.inflator = LayoutInflater.from(mNewLapSequenceDialog.getContext());
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_new_ls_sequence_row_layout, parent, false);
            return new LsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((LsViewHolder) holder).mTimeInterval.setText(mNewLsArrayList.get(position));

        }

        @Override
        public int getItemCount() {
            return mNewLsArrayList.size();
        }


        public class LsViewHolder extends RecyclerView.ViewHolder {
            public TextView mTimeInterval;


            public LsViewHolder(View itemView) {
                super(itemView);
                mTimeInterval = itemView.findViewById(R.id.new_ls_interval_value_textview);
            }
        }

    }

    void toLoginActivity() {
        Intent intent = new Intent(PersonalStopwatchActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}



