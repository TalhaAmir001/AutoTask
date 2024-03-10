package com.example.autotask;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class ContactsDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_CONTACTS = "contacts";
    public static final String COLUMN_CONTACT_ID = "contact_id";
    public static final String COLUMN_CONTACT_NAME = "name";
    public static final String COLUMN_PHONE_NUMBER_COUNT = "phonenumbercount";

    public static final String TABLE_NAME_PHONE_NUMBERS = "phone_numbers";
    public static final String COLUMN_CONTACT_ID_FK = "contact_id";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";

    // SQL statement to create the contacts table
    private static final String SQL_CREATE_CONTACTS_TABLE =
            "CREATE TABLE " + TABLE_NAME_CONTACTS + " (" +
                    COLUMN_CONTACT_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_CONTACT_NAME + " TEXT, " +
                    COLUMN_PHONE_NUMBER_COUNT + " INTEGER DEFAULT 0)";

    // SQL statement to create the phone numbers table
    private static final String SQL_CREATE_PHONE_NUMBERS_TABLE =
            "CREATE TABLE " + TABLE_NAME_PHONE_NUMBERS + " (" +
                    COLUMN_CONTACT_ID_FK + " INTEGER, " +
                    COLUMN_PHONE_NUMBER + " TEXT PRIMARY KEY, " +
                    "FOREIGN KEY (" + COLUMN_CONTACT_ID_FK + ") REFERENCES " +
                    TABLE_NAME_CONTACTS + "(" + COLUMN_CONTACT_ID + "))";

    // SQL statement to drop the contacts table
    private static final String SQL_DELETE_CONTACTS_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME_CONTACTS;

    // SQL statement to drop the phone numbers table
    private static final String SQL_DELETE_PHONE_NUMBERS_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME_PHONE_NUMBERS;

    public ContactsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CONTACTS_TABLE);
        db.execSQL(SQL_CREATE_PHONE_NUMBERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PHONE_NUMBERS_TABLE);
        db.execSQL(SQL_DELETE_CONTACTS_TABLE);
        onCreate(db);
    }

    public void addContact(ContactModel contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contactValues = new ContentValues();
        contactValues.put(COLUMN_CONTACT_NAME, contact.getName());
        contactValues.put(COLUMN_PHONE_NUMBER_COUNT, contact.getPhoneNumber().size());

        long contactId = db.insert(TABLE_NAME_CONTACTS, null, contactValues);

        for (String phoneNumber : contact.getPhoneNumber()) {
            ContentValues phoneNumberValues = new ContentValues();
            phoneNumberValues.put(COLUMN_CONTACT_ID_FK, contactId);
            phoneNumberValues.put(COLUMN_PHONE_NUMBER, phoneNumber);

            db.insert(TABLE_NAME_PHONE_NUMBERS, null, phoneNumberValues);
        }

        db.close();
    }

//    public ArrayList<ContactModel> getAllContacts() {
//        ArrayList<ContactModel> contactList = new ArrayList<>();
//
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + TABLE_NAME_CONTACTS;
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        // Looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                String id = cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_ID));
//                String name = cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_NAME));
//                ArrayList<String> phoneNumbers = getPhoneNumbersForContact(id);
//
//                ContactModel contact = new ContactModel(id, name, phoneNumbers);
//                contactList.add(contact);
//            } while (cursor.moveToNext());
//        }
//
//        // Close the cursor and database
//        cursor.close();
//        db.close();
//
//        // Return the list of contacts
//        return contactList;
//    }

//    private ArrayList<String> getPhoneNumbersForContact(String contactId) {
//        ArrayList<String> phoneNumbers = new ArrayList<>();
//
//        String selectQuery = "SELECT  * FROM " + TABLE_NAME_PHONE_NUMBERS + " WHERE " + COLUMN_CONTACT_ID_FK + " = " + contactId;
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        if (cursor.moveToFirst()) {
//            do {
//                String phoneNumber = cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER));
//                phoneNumbers.add(phoneNumber);
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        return phoneNumbers;
//    }
}
