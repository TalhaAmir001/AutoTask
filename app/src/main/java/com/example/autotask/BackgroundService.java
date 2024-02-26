package com.example.autotask;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BackgroundService extends Service {
    private Handler handler;
    private Runnable task;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        task = new Runnable() {
            @Override
            public void run() {
                // Execute your code here
                // For demonstration, let's show a toast message
                showToast("Task executed at: " + getCurrentDateTime());

                // Reschedule the task for the next execution
                scheduleNextExecution();
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the task when the service starts
        scheduleNextExecution();
        return START_STICKY;
    }

    private void scheduleNextExecution() {
        // Get the user-defined date and time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2024); // Example year
        calendar.set(Calendar.MONTH, Calendar.FEBRUARY); // Example month (0-11)
        calendar.set(Calendar.DAY_OF_MONTH, 27); // Example day
        calendar.set(Calendar.HOUR_OF_DAY, 12); // Example hour in 24-hour format
        calendar.set(Calendar.MINUTE, 0); // Example minute
        calendar.set(Calendar.SECOND, 0); // Example second

        // Schedule the task
        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();
        if (delay > 0) {
            handler.postDelayed(task, delay);
        }
    }

    private String getCurrentDateTime() {
        return dateFormat.format(new Date());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to bind to this service, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove any pending callbacks to avoid memory leaks
        handler.removeCallbacks(task);
    }
}
