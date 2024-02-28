package com.example.autotask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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

    Button TimePickerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimePickerButton = findViewById(R.id.timePickerButton);

        TimePickerButton.setOnClickListener(v -> showTimePickerDialog());

//        Service service = new Service() {
//            @Nullable
//            @Override
//            public IBinder onBind(Intent intent) {
//                return null;
//            }
//
//            @Override
//            public int onStartCommand(Intent intent, int flags, int startId) {
//                return super.onStartCommand(intent, flags, startId);
//            }
//        };
        BackgroundService service = new BackgroundService();
        Intent serviceIntent = new Intent(getApplicationContext(), BackgroundService.class);
        startService(serviceIntent);

//        service.scheduleNextExecutionFromActivity();
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Save the time and display it in a Toast message
        String time = String.format("%02d:%02d", hourOfDay, minute);
        Toast.makeText(this, "Time selected: " + time, Toast.LENGTH_SHORT).show();
    }
}





