package com.example.travellens;


import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.util.Log;
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
    }

    public static void checkUserTokenUpdate(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        String token = task.getResult();
                        if(!token.equals(ParseUser.getCurrentUser().getString("deviceToken"))){
                            ParseUser user = ParseUser.getCurrentUser();
                            user.put("deviceToken", token);
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
                    MediaType mediaType = MediaType.parse("application/json");
                    String content = "{\n  \"to\":\"" + deviceToken + "\"," +
                            "\n  \"content_available\": true,\n  \"priority\": \"high\",\n  \"notification\": {\n      " +
                            "\"title\": \"" + title + "\",\n      " +
                            "\"body\": \"" + body + "\"\n   }\n}";
                    RequestBody body = RequestBody.create(mediaType, content);
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

