package com.example.contactscheduler;

import android.content.ContentProviderOperation;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.provider.ContactsContract.AUTHORITY;
import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.Data;
import static android.provider.ContactsContract.RawContacts;

public class MainActivity extends AppCompatActivity {

    public static final String CONTACT_SCHEDULER_API_URL = "https://contact-scheduler-api.herokuapp.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createContact();
    }

    private void createContact() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, CONTACT_SCHEDULER_API_URL, response -> {
            try {
                JSONObject json = new JSONObject(response);
                Contact contact = new Contact(json.getString("name"), json.getString("number"), json.getString("email"));
                saveContact(contact);
            } catch (JSONException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
        }, error -> Log.e("Error", error.getMessage()));
        requestQueue.add(stringRequest);
    }

    private void saveContact(Contact contact) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newInsert(
                RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .build());
        this.setName(contact.getName(), ops);
        this.setNumber(contact.getNumber(), ops);
        this.setEmail(contact.getEmail(), ops);

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

    private void setNumber(String number, ArrayList<ContentProviderOperation> ops) {
        if (null != number) {
            ops.add(ContentProviderOperation.
                    newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(Data.MIMETYPE,
                            Phone.CONTENT_ITEM_TYPE)
                    .withValue(Phone.NUMBER, number)
                    .withValue(Phone.TYPE,
                            Phone.TYPE_MOBILE)
                    .build());
        }
    }

    private void setName(String name, ArrayList<ContentProviderOperation> ops) {
        if (name != null) {
            ops.add(ContentProviderOperation.newInsert(
                    Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(Data.MIMETYPE,
                            StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            StructuredName.DISPLAY_NAME,
                            name).build());
        }
    }
}