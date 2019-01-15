package com.everyday.skara.everyday;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.everyday.skara.everyday.classes.BasicSettings;
import com.everyday.skara.everyday.classes.BoardTypes;
import com.everyday.skara.everyday.classes.Connectivity;
import com.everyday.skara.everyday.classes.DateTimeStamp;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.LoginTypes;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.classes.UserAccountType;
import com.everyday.skara.everyday.pojo.BoardPOJO;
import com.everyday.skara.everyday.pojo.Categories;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.everyday.skara.everyday.pojo.UserProfilePOJO;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tapadoo.alerter.Alerter;

public class LoginActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    // google sign in
    public static final int RC_SIGN_IN = 9001;
    Button signInButton;
    boolean alreadyExists;


    // firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    TextView mEverydayTextView, mTodo, mLinks, mNotes, mExpenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initGoogleSignIn();
        init();
    }
    void initView(){
        mEverydayTextView = findViewById(R.id.everyday_textview);
        mExpenses = findViewById(R.id.expenses_textview_login);
        mNotes= findViewById(R.id.notes_textview_login);
        mLinks = findViewById(R.id.links_textview_login);
        mTodo= findViewById(R.id.todo_textview_login);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "pacifico.ttf");
        mEverydayTextView.setTypeface(typeface);
        }

    void init() {
        // initializing auth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                updateUI(user);
            }
        };


    }

    void updateUI(FirebaseUser user) {
        if (user != null) {
            disableLoginOptions();
            userDetails(user);
        } else {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    /**
     * Google sign [STARTS HERE]
     */

    void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        signInButton = findViewById(R.id.sign_in_button);
        //signInButton.setSize(SignInButton.SIZE_ICON_ONLY);
        signInButton.setOnClickListener(this);

    }

    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(this, "Thank you for using Everyday.", Toast.LENGTH_SHORT).show();
            firebaseAuthWithGoogle(acct);
        } else {
            // Signed out, show unauthenticated UI.
        }
    }

    void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // [ENDS HERE]
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                if (Connectivity.checkInternetConnection(this)) {
                    googleSignIn();
                } else {
                    showInternetAlerter();
                }
                break;
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    void userDetails(final FirebaseUser user) {
        // Name, email address, and profile photo Url
        final String name = user.getDisplayName();
        final String email = user.getEmail();
        final Uri photoUrl = user.getPhotoUrl();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS);


        alreadyExists = false;
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("email").getValue().toString().equals(email)) {
                        alreadyExists = true;
                        UserProfilePOJO userProfilePOJO = snapshot.getValue(UserProfilePOJO.class);
                        setKey(userProfilePOJO.getUser_key(), userProfilePOJO.getEmail(), userProfilePOJO.getName(), userProfilePOJO.getProfile_url(), userProfilePOJO.getLogin_type(), userProfilePOJO.getUser_account_type(), userProfilePOJO.getDay(),userProfilePOJO.getMonth(), userProfilePOJO.getYear());
                        break;
                    }
                }

                if (!alreadyExists) {
                    alreadyExists = true;
                    final DatabaseReference referenceKey = databaseReference.push();

                    UserProfilePOJO userProfilePOJO = new UserProfilePOJO(name, email, photoUrl.toString(), referenceKey.getKey(), LoginTypes.LOGIN_TYPE_GOOGLE, UserAccountType.FREE_USER, 1, 0, 1990);
                    referenceKey.setValue(userProfilePOJO, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReferenceChild) {
                            setNewUserKey(referenceKey.getKey(), email, name, photoUrl.toString(), LoginTypes.LOGIN_TYPE_GOOGLE, UserAccountType.FREE_USER, 1, 0, 1990);
                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    void setNewUserKey(String userKey, String email, String name, String url, String loginType, int userAccountType, int day, int month, int year) {
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.USER_DETAILS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("url", url);
        editor.putString("user_key", userKey);
        editor.putString("login_type", loginType);
        editor.putInt("user_account_type", userAccountType);
        editor.putInt("dob_month", month);
        editor.putInt("dob_year", year);
        editor.putInt("dob_day", day);
        editor.apply();
        UserInfoPOJO userInfoPOJO = new UserInfoPOJO(name, email, url, userKey, day, month, year);
        createPersonalBoard(userInfoPOJO);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    void setKey(String userKey, String email, String name, String url, String loginType, int userAccountType, int day, int month, int year) {
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.USER_DETAILS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("url", url);
        editor.putString("user_key", userKey);
        editor.putString("login_type", loginType);
        editor.putInt("user_account_type", userAccountType);
        editor.putInt("dob_month", month);
        editor.putInt("dob_year", year);
        editor.putInt("dob_day", day);
        editor.apply();
        UserInfoPOJO userInfoPOJO = new UserInfoPOJO(name, email, url, userKey, day, month, year);
        initBasicSettings();
    }

    void createPersonalBoard(UserInfoPOJO userInfoPOJO) {
        final BoardPOJO boardPOJO = new BoardPOJO("Financial Board", DateTimeStamp.getDate(), "-financial_board", BoardTypes.BOARD_TYPE_FINANCIAL, userInfoPOJO);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL);
        databaseReference.setValue(boardPOJO);

        DatabaseReference catReference = databaseReference.child("categories").push();
        catReference.setValue(new Categories("Others", catReference.getKey(), 2005, 1, 0.0));

        DatabaseReference catReference2 = databaseReference.child("categories").push();
        catReference2.setValue(new Categories("Food and Drinks", catReference2.getKey(), 2002, 2, 0.0));

        DatabaseReference catReference3 = databaseReference.child("categories").push();
        catReference3.setValue(new Categories("Transport", catReference3.getKey(), 2000, 3, 0.0));

        DatabaseReference catReference4 = databaseReference.child("categories").push();
        catReference4.setValue(new Categories("Shopping", catReference4.getKey(), 2001, 4, 0.0));

        DatabaseReference catReference5 = databaseReference.child("categories").push();
        catReference5.setValue(new Categories("Leisure", catReference5.getKey(), 2006, 5, 0.0));

        DatabaseReference catReference6 = databaseReference.child("categories").push();
        catReference6.setValue(new Categories("Income", catReference6.getKey(), 2048, 1, 0.0));

        final BoardPOJO boardPOJO3 = new BoardPOJO("Gratitude Board", DateTimeStamp.getDate(), "-gratitude_board", BoardTypes.BOARD_TYPE_PERSONAL_GRATITUDE, userInfoPOJO);
        DatabaseReference databaseReference3 = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_GRATITUDE);
        databaseReference3.setValue(boardPOJO3);


        final BoardPOJO boardPOJO4 = new BoardPOJO("Habit Board", DateTimeStamp.getDate(), "-habit_board", BoardTypes.BOARD_TYPE_PERSONAL_HABIT, userInfoPOJO);
        DatabaseReference databaseReference4 = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_HABITS);
        databaseReference4.setValue(boardPOJO4);


        final BoardPOJO boardPOJO1 = new BoardPOJO("Productivity Board", DateTimeStamp.getDate(), "-productivity_board", BoardTypes.BOARD_TYPE_PERSONAL_PRODUCTIVITY, userInfoPOJO);
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_PROD);
        databaseReference1.setValue(boardPOJO1);

        initBasicSettings();
    }

    void initBasicSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currency", getResources().getString(R.string.inr));
        editor.putInt("theme", BasicSettings.DEFAULT_THEME);
        editor.apply();
        toMainActivity();
    }

    void toMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void disableLoginOptions() {
        signInButton.setEnabled(false);
    }

}