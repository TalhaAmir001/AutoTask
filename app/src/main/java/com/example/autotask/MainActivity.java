package com.example.autotask;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.security.Provider;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;

    Button TimePickerButton;
    private static long unixTime;
    Intent serviceIntent;
    ArrayList<String> contactList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimePickerButton = findViewById(R.id.timePickerButton);

        TimePickerButton.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                getContactList();
            }

//            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
//                Toast.makeText(this, "no", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "yes", Toast.LENGTH_SHORT).show();
//                showTimePickerDialog();
//            }

        });

        BackgroundService service = new BackgroundService();
        serviceIntent = new Intent(getApplicationContext(), BackgroundService.class);

//        service.scheduleNextExecutionFromActivity();
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    private void getContactList() {
    boolean contactsRetrieved = false;
    ContentResolver cr = getContentResolver();
    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null);

    if ((cur != null ? cur.getCount() : 0) > 0) {
        while (cur != null && cur.moveToNext()) {
            int idIndex = cur.getColumnIndex(ContactsContract.Contacts._ID);
            int nameIndex = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int hasPhoneNumberIndex = cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

            if (idIndex != -1 && nameIndex != -1 && hasPhoneNumberIndex != -1) {
                String id = cur.getString(idIndex);
                String name = cur.getString(nameIndex);

                if (cur.getInt(hasPhoneNumberIndex) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        int numberIndex = pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (numberIndex != -1) {
                            String phoneNo = pCur.getString(numberIndex);
                            Log.i(TAG, "Name: " + name);
                            contactList.add(name);
                            Log.i(TAG, "Phone Number: " + phoneNo);
                            contactsRetrieved = true;
                        }
                    }
                    pCur.close();
                }
            }
        }
    }
    if (cur != null) {
        cur.close();
    }

    if (!contactsRetrieved) {
        Log.i(TAG, "No contacts retrieved");
    }
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




