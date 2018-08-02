package com.everyday.skara.everyday.services;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

public class FirebaseInstanceIdService extends FirebaseMessagingService {
//    @Override
//    public void onNewToken(String s) {
//       // super.onNewToken(s);
//        Log.d("NEW_TOKEN",s);
//    }
//
//    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//         Log.d("loggggggggg", "From: " + remoteMessage.getFrom());
//
//        // Check if message contains a data payload.
//        if (remoteMessage.getData().size() > 0) {
//            Log.d("loggggggggg", "Message data payload: " + remoteMessage.getData());
//        }
//
//        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.d("loggggggggg", "Message Notification Body: " + remoteMessage.getNotification().getBody() + remoteMessage.getNotification().getTitle());
//        }
//    }
}
