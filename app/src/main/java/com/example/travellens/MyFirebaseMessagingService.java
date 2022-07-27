package com.example.travellens;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.parse.ParseUser;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String SEND_NOTIFICATION_URL = "https://fcm.googleapis.com/fcm/send";
    public static final String TAG = "FIREBASE MESSAGING";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        Log.i(TAG, "Received message");

        // create notification channel for custom notification after sending message
        NotificationChannel channel = new NotificationChannel("1", getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(getString(R.string.channel_description));
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        // set managers channel
        NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        // set content view of notification and details
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_push);
        contentView.setImageViewResource(R.id.image, R.drawable.logo_no_background);
        contentView.setTextViewText(R.id.title, message.getNotification().getTitle());
        contentView.setTextViewText(R.id.text, message.getNotification().getBody());

        // build notification and notify
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(message.getNotification().getTitle())
                .setContentText(message.getNotification().getBody())
                .setSmallIcon(R.drawable.logo_no_background)
                .setContent(contentView).setChannelId("1");

        notificationManager.notify(1, mBuilder.build());
    }

    public static void checkUserTokenUpdate(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        String token = task.getResult();
                        if(!token.equals(ParseUser.getCurrentUser().getString(Post.KEY_DEVICE_TOKEN))){
                            ParseUser user = ParseUser.getCurrentUser();
                            user.put(Post.KEY_DEVICE_TOKEN, token);
                            user.saveInBackground();
                        }
                    }
                });

    }

    public static void sendNotification(String deviceToken, String title, String body) {
        checkUserTokenUpdate();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    String jsonString = new JSONObject()
                            .put("to", deviceToken)
                            .put("content_available", true)
                            .put("priority", "high")
                            .put("notification", new JSONObject().put("title", title).put("body", body))
                            .toString();
                    MediaType mediaType = MediaType.parse("application/json");

                    RequestBody body = RequestBody.create(mediaType, jsonString);
                    Request request = new Request.Builder()
                            .url(SEND_NOTIFICATION_URL)
                            .method("POST", body)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", "key=" + MessageActivity.API_KEY)
                            .build();
                    Response response = client.newCall(request).execute();
                } catch (Exception e) {
                    Log.e(TAG, "Problem with notifying", e);
                }
            }
        });

        thread.start();
    }
}

