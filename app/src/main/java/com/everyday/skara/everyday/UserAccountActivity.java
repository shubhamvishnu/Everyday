package com.everyday.skara.everyday;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.LoginTypes;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.FeedbackPOJO;
import com.everyday.skara.everyday.pojo.UserProfilePOJO;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAccountActivity extends FragmentActivity implements View.OnClickListener {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    private GoogleApiClient mGoogleApiClient;
    Button mFeedbackButton;
    Dialog mFeedbackDialog;
    SharedPreferences userSharedPreferences;
    UserProfilePOJO userProfilePOJO;
    CircleImageView mUserProfile;
    TextView mName, mEmail;
    Button mLogout;

    String userKey;
    int selectedIcon;
    ImageButton mShareAppPlaystore, mShareAppLink;


    @Override
    protected void onStart() {
        initGoogleSignout();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
        if (user != null) {
            init();
        } else {
            toLogin();
        }
    }

    void init() {

        mUserProfile = (CircleImageView) findViewById(R.id.user_profile_circle_image_view);
        mName = (TextView) findViewById(R.id.user_profile_name_text_view);
        mEmail = (TextView) findViewById(R.id.user_profile_email_text_view);

        mFeedbackButton = findViewById(R.id.feedback_button);

        mShareAppPlaystore = findViewById(R.id.share_app_playstore);
        mShareAppLink = findViewById(R.id.share_app_via_link);

        mLogout = (Button) findViewById(R.id.user_profile_logout_button);
        mLogout.setOnClickListener(this);
        mFeedbackButton.setOnClickListener(this);
        mShareAppLink.setOnClickListener(this);
        mShareAppPlaystore.setOnClickListener(this);
        setViewComponents(false);
        initUserProfileDetails();
    }

    void setViewComponents(boolean action) {
        if (action) {
           Glide.with(this).load(userProfilePOJO.getProfile_url()).into(mUserProfile);
            mName.setText(userProfilePOJO.getName());
            mEmail.setText(userProfilePOJO.getEmail());
            mLogout.setEnabled(true);
        } else {
            mUserProfile.setImageDrawable(null);
            mName.setText(null);
            mEmail.setText(null);
            mLogout.setEnabled(false);
        }
    }

    void initUserProfileDetails() {
        // initializing UserProfilePOJO
        userSharedPreferences = getSharedPreferences(SPNames.USER_DETAILS, MODE_PRIVATE);
        String name = userSharedPreferences.getString("name", null);
        String email = userSharedPreferences.getString("email", null);
        String profile_url = userSharedPreferences.getString("url", null);
        String user_key = userSharedPreferences.getString("user_key", null);
        String login_type = userSharedPreferences.getString("login_type", null);
        int user_account_type = userSharedPreferences.getInt("user_account_type", 0);

        userProfilePOJO = new UserProfilePOJO(name, email, profile_url, user_key, login_type, user_account_type);
        setViewComponents(true);

    }

    void toLogin() {
        Intent intent = new Intent(UserAccountActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        toMainActivity();
    }

    void toMainActivity() {
        Intent intent = new Intent(UserAccountActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void showLogoutAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle("Logout");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you want to logout?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        logoutUser();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_profile_logout_button:
                showLogoutAlert();
                break;
            case R.id.feedback_button:
                showFeedbackDialog();
                break;
            case R.id.share_app_playstore:
                openLink();
                break;
            case R.id.share_app_via_link:
                shareApp();
                break;
        }
    }

    void shareApp() {
        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Snapshot!");
        i.putExtra(android.content.Intent.EXTRA_TEXT, "A simple app to always stay in sync. Click on the link below to download\n" + "http://play.google.com/store/apps/details?id=com.snapshot.skra.snapshot");
        startActivity(Intent.createChooser(i, "Share via"));
    }

    void openLink() {
        Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=com.snapshot.skra.snapshot");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }



    void showFeedbackDialog() {
        mFeedbackDialog = new Dialog(this);
        mFeedbackDialog.setContentView(R.layout.dialog_feedback_layout);
        selectedIcon = -1;
        final ImageButton sad, happy, inlove;
        final EditText feedbackNote;
        final Button doneFeedback;

        sad = mFeedbackDialog.findViewById(R.id.sad_button);
        happy = mFeedbackDialog.findViewById(R.id.happy_button);
        inlove = mFeedbackDialog.findViewById(R.id.in_love_button);

        feedbackNote = mFeedbackDialog.findViewById(R.id.feedback_note_edittext);
        doneFeedback = mFeedbackDialog.findViewById(R.id.done_feedback_button);
        sad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedIcon = 1;
                sad.setBackgroundColor(getResources().getColor(R.color.grey));
                happy.setBackgroundColor(getResources().getColor(R.color.transparent));
                inlove.setBackgroundColor(getResources().getColor(R.color.transparent));

            }
        });
        happy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedIcon = 2;
                happy.setBackgroundColor(getResources().getColor(R.color.grey));
                sad.setBackgroundColor(getResources().getColor(R.color.transparent));
                inlove.setBackgroundColor(getResources().getColor(R.color.transparent));
            }
        });
        inlove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedIcon = 3;
                inlove.setBackgroundColor(getResources().getColor(R.color.grey));
                happy.setBackgroundColor(getResources().getColor(R.color.transparent));
                sad.setBackgroundColor(getResources().getColor(R.color.transparent));
            }
        });

        doneFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String note = feedbackNote.getText().toString();
                if ((selectedIcon == -1) && (note.isEmpty())) {
                    Toast.makeText(UserAccountActivity.this, "You haven't selected anything", Toast.LENGTH_SHORT).show();
                } else if ((selectedIcon != -1)) {
                    doneFeedback.setEnabled(false);
                    FeedbackPOJO feedbackPOJO = new FeedbackPOJO(userProfilePOJO, "NULL", selectedIcon);
                    if (!note.isEmpty()) {
                        feedbackPOJO.setNote(note);
                    }
                    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_FEEDBACK);
                    databaseReference.push().setValue(feedbackPOJO).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFeedbackDialog.dismiss();
                        }
                    });

                }
            }
        });

        Window window = mFeedbackDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        mFeedbackDialog.setCanceledOnTouchOutside(true);
        mFeedbackDialog.show();
    }

    void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences.Editor editor = userSharedPreferences.edit();
        editor.clear();
        editor.apply();

        if (userProfilePOJO.getLogin_type().equals(LoginTypes.LOGIN_TYPE_GOOGLE)) {
            googleSignout();
        }
    }

    /**
     * Google sign [STARTS HERE]
     */

    void initGoogleSignout() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }

    void googleSignout() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        toLogin();
                    }
                });
    }
}