package com.everyday.skara.everyday;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.pojo.ActivityPOJO;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.LinkPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.everyday.skara.everyday.pojo.UserProfilePOJO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tapadoo.alerter.Alerter;

public class LinkActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    BoardPOJO boardPOJO;
    UserInfoPOJO userInfoPOJO;

    EditText mLink, mTitle;
    TextView mDate;
    Button mDone;
    WebView mLinkWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);
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


        mTitle = findViewById(R.id.link_title);
        mLink = findViewById(R.id.link);
        mDate = findViewById(R.id.link_date);
        mLinkWebView = findViewById(R.id.link_preview_webview);
        mLinkWebView.setVisibility(View.INVISIBLE);
        mDate.setText(DateTimeStamp.getDate());
        mDone = findViewById(R.id.link_done);

        mLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString();
                if (url.isEmpty()) {
                    mLinkWebView.setVisibility(View.INVISIBLE);
                } else {
                    if (!url.startsWith("http://") || !url.startsWith("https://")) {
                        url = "http://" + url;
                    }
                    mLinkWebView.loadUrl(String.valueOf(Uri.parse(url)));
                    mLinkWebView.setVisibility(View.VISIBLE);
                    mLinkWebView.setWebViewClient(new WebViewClient()
                    {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url)
                        {
                            return false;
                        }
                    });
                }
            }
        });
        mDone.setOnClickListener(this);


    }

    void toLoginActivity() {
        Intent intent = new Intent(LinkActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.link_done:
                saveLink();
                break;
        }
    }

    void saveLink() {
            mDone.setEnabled(false);
            String title = mTitle.getText().toString().trim();
            String link = mLink.getText().toString();
            if (!link.isEmpty()) {
                databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey() + "/links/");
                databaseReference.keepSynced(true);
                final DatabaseReference linkDatabaseReference = databaseReference.push();
                linkDatabaseReference.keepSynced(true);
                LinkPOJO linkPOJO = new LinkPOJO(link, title, DateTimeStamp.getDate(), linkDatabaseReference.getKey(), userInfoPOJO);
                linkDatabaseReference.setValue(linkPOJO);
                ActivityPOJO activityPOJO = new ActivityPOJO("link saved on "+boardPOJO.getDate() + "by" + userInfoPOJO.getName(), boardPOJO.getDate(), userInfoPOJO);
                firebaseDatabase.getReference(FirebaseReferences.FIREBASE_BOARDS + boardPOJO.getBoardKey()).child("activity").push().setValue(activityPOJO);
                toBoardsActivity();
            }else{
                // TODO: fields empty alert
            }
    }
    void toBoardsActivity(){
        Intent intent = new Intent(LinkActivity.this, BoardActivity.class);
        intent.putExtra("board_pojo", boardPOJO);
        intent.putExtra("user_profile",userInfoPOJO);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void showInternetAlerter() {
        Alerter.create(this)
                .setText("No internet connection. Will be saved to cloud later.")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Connectivity.openInternetSettings(getApplicationContext());
                    }
                })
                .setBackgroundColorRes(R.color.colorAccent)
                .show();
    }
}
