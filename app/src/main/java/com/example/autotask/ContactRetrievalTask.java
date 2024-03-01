package com.example.autotask;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

public class ContactRetrievalTask extends AsyncTask<Void, Void, ArrayList<ContactModel>> {
    private Context mContext;
    private OnContactsRetrievedListener mListener;

    public ContactRetrievalTask(Context context, OnContactsRetrievedListener listener) {
        mContext = context;
        mListener = listener;
    }

    ArrayList<ContactModel> contactsList = new ArrayList<>();
    ArrayList<String> phoneNumbers = new ArrayList<>();
    @Override
    protected ArrayList<ContactModel> doInBackground(Void... voids) {
        ContentResolver cr = mContext.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
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
                        while (pCur != null && pCur.moveToNext()) {
                            int numberIndex = pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            if (numberIndex != -1) {
                                String phoneNo = pCur.getString(numberIndex);
                                phoneNumbers.add(phoneNo);
                            }
                        }
                        if (pCur != null) {
                            pCur.close();
                        }
//                        Log.e(TAG, "doInBackground:$%$%$%$%$%$$%$%$%%$%$ "+name);
                        contactsList.add(new ContactModel(id, name, phoneNumbers));
                    }
                }
            }
            cur.close();
        }

        return contactsList;
    }

    @Override
    protected void onPostExecute(ArrayList<ContactModel> contactModels) {
        super.onPostExecute(contactModels);
        if (mListener != null) {
            mListener.onContactsRetrieved(contactModels);
        }
    }

    public interface OnContactsRetrievedListener {
        void onContactsRetrieved(ArrayList<ContactModel> contacts);
    }
}

