package com.everyday.skara.everyday;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class Everyday extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
