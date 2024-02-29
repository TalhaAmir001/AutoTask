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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BackgroundService extends Service {

    private Handler handler = new Handler();
    long unixTime;
    Date now = new Date();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the task when the service starts
//        scheduleNextExecution();
        startForeground(123, createNotification());
        unixTime = intent.getExtras().getLong("unixTime");
//        unixTime = intent.getLongExtra("unixTime");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Date now = new Date();
                long unixTime1 = now.getTime() / 1000L;
                if (unixTime1 == unixTime){
                    Toast.makeText(BackgroundService.this, "wow", Toast.LENGTH_SHORT).show();
                    String phoneNumber = "923010617687"; // Replace with the phone number you want to send a message to
                    String message = "Hello, World!"; // Replace with the message you want to send

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
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

//    private Runnable taskRunnable = new Runnable() {
//        @Override
//        public void run() {
//
//            Date now = new Date();
//            long unixTime1 = now.getTime() / 1000L;
//            if (unixTime1 == unixTime){
//                Toast.makeText(BackgroundService.this, "wow", Toast.LENGTH_SHORT).show();
//                String phoneNumber = "923010617687"; // Replace with the phone number you want to send a message to
//                String message = "Hello, World!"; // Replace with the message you want to send
//
//                SmsManager smsManager = SmsManager.getDefault();
//                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
//
//            }
////            Toast.makeText(getApplicationContext(), "Unix time is: " + unixTime, Toast.LENGTH_SHORT).show();
//            handler.postDelayed(this, 1000);
//        }
//    };

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
