package com.example.autotask;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
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
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import java.security.Provider;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private ArrayList<ContactModel> contactsList = new ArrayList<>();

    Button TimePickerButton;
    private static long unixTime;
    Intent serviceIntent;
    private int mSelectedSimSlot = -1;


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
//                getContactList();
                new ContactRetrievalTask(this, new ContactRetrievalTask.OnContactsRetrievedListener() {
                    @Override
                    public void onContactsRetrieved(ArrayList<ContactModel> contacts) {
                        contactsList = contacts;
//                        Log.e(TAG, "onContactsRetrieved:12121212121212 "+contactsList.get(0).getName());
                        showContactDialog();
                    }
                }).execute();
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

        serviceIntent = new Intent(getApplicationContext(), BackgroundService.class);

//        service.scheduleNextExecutionFromActivity();
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    private void showContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_messaging, null);
        builder.setView(dialogView);

        final RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        final EditText messageEditText = dialogView.findViewById(R.id.messageEditText);

        // Populate radio group with contacts
        for (int i = 0; i < contactsList.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(contactsList.get(i).getName());
            radioButton.setId(i);
            radioGroup.addView(radioButton);
        }

        // Get SIM slots
        SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
        if (checkReadPhoneStatePermission(MainActivity.this)){
            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptionInfoList != null) {
                for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                    RadioButton radioButton = new RadioButton(this);
                    mSelectedSimSlot = subscriptionInfo.getSimSlotIndex();
                    radioButton.setText("SIM " + (subscriptionInfo.getSimSlotIndex() + 1));
                    radioButton.setId(subscriptionInfo.getSimSlotIndex());
                    radioGroup.addView(radioButton);
                }
            }
        } else {
            requestReadPhoneStatePermission(MainActivity.this);
        }

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int selectedContactId = radioGroup.getCheckedRadioButtonId();
                if (selectedContactId == -1) {
                    Toast.makeText(MainActivity.this, "Please select a contact", Toast.LENGTH_SHORT).show();
                    return;
                }
                String selectedContact = contactsList.get(selectedContactId).getPhoneNumber().get(0);
                String message = messageEditText.getText().toString();

                if (mSelectedSimSlot == -1) {
                    Toast.makeText(MainActivity.this, "Please select a SIM slot", Toast.LENGTH_SHORT).show();
                    return;
                }

                serviceIntent.putExtra("contactNumber", selectedContact);
                serviceIntent.putExtra("message", message);
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 0);
                Toast.makeText(MainActivity.this, "no", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "yes", Toast.LENGTH_SHORT).show();
                showTimePickerDialog();
            }
                // Perform action with selected contact, sim slot, and message
                // For example: sendSMS(selectedContact, mSelectedSimSlot, message)
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }
    public static boolean checkReadPhoneStatePermission(Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    private static final int REQUEST_READ_PHONE_STATE = 123;
    public static void requestReadPhoneStatePermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                REQUEST_READ_PHONE_STATE);
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



