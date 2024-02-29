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
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BackgroundService extends Service {

    private Handler handler = new Handler();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the task when the service starts
//        scheduleNextExecution();
        startForeground(123, createNotification());
        handler.postDelayed(taskRunnable, 5000);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to bind to this service, so return null
        return null;
    }

    private Runnable taskRunnable = new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            Toast.makeText(getApplicationContext(), "Current time is: " + currentTime, Toast.LENGTH_SHORT).show();            handler.postDelayed(this, 5000);
        }
    };

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
