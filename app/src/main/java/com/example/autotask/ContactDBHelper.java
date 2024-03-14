package com.example.autotask;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class ContactDBHelper extends SQLiteOpenHelper {

    // Database and table details
    private static final String DB_NAME = "contacts.db";
    private static final int DB_VERSION = 1;

    // Contacts table details
    private static final String CONTACTS_TABLE_NAME = "contacts";
    private static final String CONTACTS_COLUMN_ID = "id";
    private static final String CONTACTS_COLUMN_NAME = "name";
    private static final String CONTACTS_COLUMN_PHONE_COUNT = "pno_count";

    // Phone numbers table details
    private static final String PHONE_NUMBERS_TABLE_NAME = "phone_numbers";
    private static final String PHONE_NUMBERS_COLUMN_PHONE_NUMBER = "phone_number";
    private static final String PHONE_NUMBERS_COLUMN_CONTACT_ID = "contact_id";

    public ContactDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Contacts table
        String createContactsTable = "CREATE TABLE " + CONTACTS_TABLE_NAME + " (" +
                CONTACTS_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                CONTACTS_COLUMN_NAME + " TEXT, " +
                CONTACTS_COLUMN_PHONE_COUNT + " INTEGER)";
        db.execSQL(createContactsTable);

        // Create Phone Numbers table with foreign key constraint
        String createPhoneNumbersTable = "CREATE TABLE " + PHONE_NUMBERS_TABLE_NAME + " (" +
                PHONE_NUMBERS_COLUMN_PHONE_NUMBER + " TEXT, " +
                PHONE_NUMBERS_COLUMN_CONTACT_ID + " INTEGER, " +
                "FOREIGN KEY(" + PHONE_NUMBERS_COLUMN_CONTACT_ID + ") REFERENCES " +
                CONTACTS_TABLE_NAME + "(" + CONTACTS_COLUMN_ID + ")" + ")";
        db.execSQL(createPhoneNumbersTable);
    }

    // Method to add a new contact
    public void addContact(ContactModel contactModel) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction(); // Start transaction for reliable insertion

        try {
            // Insert contact details into the contacts table
            ContentValues contactValues = new ContentValues();
            contactValues.put(CONTACTS_COLUMN_ID, contactModel.getId());
            contactValues.put(CONTACTS_COLUMN_NAME, contactModel.getName());
            contactValues.put(CONTACTS_COLUMN_PHONE_COUNT, contactModel.getPhoneNumber().size());
            long newContactId = db.insert(CONTACTS_TABLE_NAME, null, contactValues);

            // Insert phone numbers into the phone_numbers table
            ArrayList<String> phoneNumbers = contactModel.getPhoneNumber();
            for (String phoneNumber : phoneNumbers) {
                ContentValues phoneValues = new ContentValues();
                phoneValues.put(PHONE_NUMBERS_COLUMN_PHONE_NUMBER, phoneNumber);
                phoneValues.put(PHONE_NUMBERS_COLUMN_CONTACT_ID, newContactId);
                db.insert(PHONE_NUMBERS_TABLE_NAME, null, phoneValues);
            }

            db.setTransactionSuccessful(); // Mark transaction successful
        } finally {
            db.endTransaction(); // End transaction regardless of success or failure
            db.close();
        }
    }

    @SuppressLint("Range")
    public ArrayList<ContactModel> getAllContacts() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ContactModel> contacts = new ArrayList<>();

        // Join query to fetch contacts and phone numbers in one go
        String query = "SELECT c.*, p.phone_number " +
                "FROM " + CONTACTS_TABLE_NAME + " c " +
                "LEFT JOIN " + PHONE_NUMBERS_TABLE_NAME + " p " +
                "ON c.id = p.contact_id";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                // Create a ContactModel object for each contact
                ContactModel contact = new ContactModel();
                contact.setId(cursor.getString(cursor.getColumnIndex(CONTACTS_COLUMN_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndex(CONTACTS_COLUMN_NAME)));
                contact.setPhoneNumber(new ArrayList<String>()); // Initialize phone number list

                // Loop through phone numbers for this contact
                String currentContactId = contact.getId();
                do {
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(PHONE_NUMBERS_COLUMN_PHONE_NUMBER));
                    if (phoneNumber != null) { // Add phone number only if it exists
                        contact.getPhoneNumber().add(phoneNumber);
                    }
                } while (cursor.moveToNext() && currentContactId.equals(cursor.getString(cursor.getColumnIndex(CONTACTS_COLUMN_ID))));

                // Move back one position to avoid skipping a contact
                cursor.moveToPrevious();

                contacts.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return contacts;
    }


//    // Method to fetch all contacts
//    @SuppressLint("Range")
//    public ArrayList<ContactModel> getAllContacts() {
//        SQLiteDatabase db = getReadableDatabase();
//        ArrayList<ContactModel> contacts = new ArrayList<>();
//
//        // Select all contacts from the contacts table
//        Cursor cursor = db.rawQuery("SELECT * FROM " + CONTACTS_TABLE_NAME, null);
//
//        if (cursor.moveToFirst()) {
//            do {
//                // Create a ContactModel object for each contact
//                ContactModel contact = new ContactModel();
//                contact.setId(cursor.getString(cursor.getColumnIndex(CONTACTS_COLUMN_ID)));
//                contact.setName(cursor.getString(cursor.getColumnIndex(CONTACTS_COLUMN_NAME)));
//                contact.setPhoneNumber(new ArrayList<>()); // Initialize phone number list
//
//                // Now fetch phone numbers for this contact (optional)
//                // You can use a separate query here to retrieve phone numbers based on contact ID
//
//                contacts.add(contact);
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        return contacts;
//    }


    // Implement methods for adding phone numbers, etc. (as needed)

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if necessary (e.g., drop and recreate tables)
        db.execSQL("DROP TABLE IF EXISTS " + PHONE_NUMBERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME);
        onCreate(db);
    }
}
