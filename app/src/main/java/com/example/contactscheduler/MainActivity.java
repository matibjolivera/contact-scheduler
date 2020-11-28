package com.example.contactscheduler;

import android.content.ContentProviderOperation;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import static android.provider.ContactsContract.AUTHORITY;
import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.Data;
import static android.provider.ContactsContract.RawContacts;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.createContact();
    }

    private void createContact() {
        String name = "Test";
        String number = "5491134160701";
        String email = "matibjolivera@gmail.com";

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(
                RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .build());

        this.setName(name, ops);
        this.setNumber(number, ops);
        this.setEmail(email, ops);

        try {
            getContentResolver().applyBatch(AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setEmail(String email, ArrayList<ContentProviderOperation> ops) {
        if (null != email) {
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(Data.MIMETYPE,
                            Email.CONTENT_ITEM_TYPE)
                    .withValue(Email.DATA, email)
                    .withValue(Email.TYPE, Email.TYPE_WORK)
                    .build());
        }
    }

    private void setNumber(String mobileNumber, ArrayList<ContentProviderOperation> ops) {
        if (null != mobileNumber) {
            ops.add(ContentProviderOperation.
                    newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(Data.MIMETYPE,
                            Phone.CONTENT_ITEM_TYPE)
                    .withValue(Phone.NUMBER, mobileNumber)
                    .withValue(Phone.TYPE,
                            Phone.TYPE_MOBILE)
                    .build());
        }
    }

    private void setName(String displayName, ArrayList<ContentProviderOperation> ops) {
        if (displayName != null) {
            ops.add(ContentProviderOperation.newInsert(
                    Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(Data.MIMETYPE,
                            StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            StructuredName.DISPLAY_NAME,
                            displayName).build());
        }
    }
}