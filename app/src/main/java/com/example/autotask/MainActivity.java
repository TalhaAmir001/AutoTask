package com.example.autotask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.security.Provider;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    Button TimePickerButton;
    private static long unixTime;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimePickerButton = findViewById(R.id.timePickerButton);

        TimePickerButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
                Toast.makeText(this, "no", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "yes", Toast.LENGTH_SHORT).show();
                showTimePickerDialog();
            }
        });

        BackgroundService service = new BackgroundService();
        serviceIntent = new Intent(getApplicationContext(), BackgroundService.class);

//        service.scheduleNextExecutionFromActivity();
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Create a Calendar object with the selected time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Convert the selected time to Unix timestamp
        unixTime = calendar.getTimeInMillis() / 1000L;
        serviceIntent.putExtra("unixTime", unixTime);
        startService(serviceIntent);

        // Display the Unix time in a toast message
//        Toast.makeText(this, "Unix time is: " + unixTime, Toast.LENGTH_SHORT).show();
    }
}





