package com.example.autotask;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;

import java.security.Provider;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private ArrayList<ContactModel> contactsList = new ArrayList<>();

    private ContactDBHelper contactDBHelper;

    Button TimePickerButton;
    Button RetriveContactsButton;
    private static long unixTime;
    Intent serviceIntent;
    private int mSelectedSimSlot = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimePickerButton = findViewById(R.id.timePickerButton);
        RetriveContactsButton = findViewById(R.id.getContactsButton);

        contactDBHelper = new ContactDBHelper(this);

        RetriveContactsButton.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                // Show a ProgressDialog
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setCancelable(false); // Optional
                progressDialog.show();

                new ContactRetrievalTask(this, new ContactRetrievalTask.OnContactsRetrievedListener() {
                    @Override
                    public void onContactsRetrieved(ArrayList<ContactModel> contacts) {
                        for (int i = 0; i < contacts.size(); i++) {
                            contactDBHelper.addContact(contacts.get(i));
                        }

                        // Dismiss the ProgressDialog
                        progressDialog.dismiss();
                    }
                }).execute();
            }
        });

        TimePickerButton.setOnClickListener(v -> {

            String[] permissions = new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE};
            boolean allPermissionsGranted = true;

            // Check if all permissions are granted
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                // Request permissions if not granted
                ActivityCompat.requestPermissions(MainActivity.this, permissions, MY_PERMISSIONS_REQUEST_SEND_SMS);
            } else {
                // Permissions already granted, proceed
                contactsList = contactDBHelper.getAllContacts();
                showContactDialog();
            }
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

        final LinearLayout contactsLayout = dialogView.findViewById(R.id.contactsLayout);
        final RadioGroup simRadioGroup = dialogView.findViewById(R.id.simRadioGroup);
        final EditText messageEditText = dialogView.findViewById(R.id.messageEditText);

        // List to store selected contacts
        final List<String> selectedContacts = new ArrayList<>();

        // Populate contacts with checkboxes
        for (int i = 0; i < contactsList.size(); i++) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(contactsList.get(i).getName());
            checkBox.setId(i);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // Add selected contact to the list
                        selectedContacts.add(contactsList.get(buttonView.getId()).getPhoneNumber().get(0));
                    } else {
                        // Remove deselected contact from the list
                        selectedContacts.remove(contactsList.get(buttonView.getId()).getPhoneNumber().get(0));
                    }
                }
            });
            contactsLayout.addView(checkBox);
        }

        // Populate SIM slots with radio buttons
        SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
        if (checkReadPhoneStatePermission(this)) {
            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptionInfoList != null) {
                for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                    RadioButton radioButton = new RadioButton(this);
                    int simSlotIndex = subscriptionInfo.getSimSlotIndex();
                    radioButton.setText("SIM " + (simSlotIndex + 1));
                    radioButton.setId(simSlotIndex);
                    simRadioGroup.addView(radioButton);
                }
            }
        } else {
            requestReadPhoneStatePermission(MainActivity.this);
        }

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int selectedSimSlot = simRadioGroup.getCheckedRadioButtonId();
                String message = messageEditText.getText().toString();

                if (selectedContacts.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please select at least one contact", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedSimSlot == -1) {
                    Toast.makeText(MainActivity.this, "Please select a SIM slot", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Serialize the selectedContacts list
                Gson gson = new Gson();
                String selectedContactsJson = gson.toJson(selectedContacts);

                // Put the serialized list as an extra in the serviceIntent
                serviceIntent.putExtra("selectedContacts", selectedContactsJson);
                serviceIntent.putExtra("message", message);

                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 0);
                    Toast.makeText(MainActivity.this, "no", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "yes", Toast.LENGTH_SHORT).show();
                    showTimePickerDialog();
                }
                // Clear the list of selected contacts
                selectedContacts.clear();
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



