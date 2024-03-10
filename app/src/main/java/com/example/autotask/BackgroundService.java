package com.example.autotask;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackgroundService extends Service {

    private Handler handler = new Handler();
    long unixTime;
    String contactNumber = "", message = "";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the task when the service starts
//        scheduleNextExecution();
        startForeground(123, createNotification());
        unixTime = intent.getExtras().getLong("unixTime");

        // Retrieve the serialized selectedContacts list from the intent
        String selectedContactsJson = intent.getStringExtra("selectedContacts");

        // Deserialize the selectedContacts list
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> selectedContacts = gson.fromJson(selectedContactsJson, type);

        message = intent.getExtras().getString("message");
//        unixTime = intent.getLongExtra("unixTime");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                long unixTime1 = now.getTime() / 1000L;
                if (unixTime1 == unixTime) {

                    SmsManager smsManager = SmsManager.getDefault();
                    for (int i = 0; i < selectedContacts.size(); i++) {
                        contactNumber = selectedContacts.get(i);
                        smsManager.sendTextMessage(contactNumber, null, message, null, null);
                    }

                    stopSelf();
                    stopForeground(true);

                }
//            Toast.makeText(getApplicationContext(), "Unix time is: " + unixTime, Toast.LENGTH_SHORT).show();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to bind to this service, so return null
        return null;
    }

    //    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        handler.removeCallbacks(taskRunnable);
//    }
    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "Time Tracking Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        //missing mutability flag
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("Time Tracking Service")
                .setContentText("Running in the background")
//            .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .build();
    }
}