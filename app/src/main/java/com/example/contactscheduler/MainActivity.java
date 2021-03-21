package com.example.contactscheduler;

import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.provider.ContactsContract.AUTHORITY;
import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.Data;
import static android.provider.ContactsContract.RawContacts;

public class MainActivity extends AppCompatActivity {

    public static final String CONTACT_SCHEDULER_API_URL = "https://fp-woocommerce.herokuapp.com/orders";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createContact();
    }

    private void createContact() {
        Log.d("MORTADELA", "Create contact");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, CONTACT_SCHEDULER_API_URL, response -> {
            try {
                JSONObject jsonObj = new JSONObject(response);
                JSONArray jsonArray = jsonObj.getJSONArray("result");
                int limit = jsonArray.length();
                for (int i = 0; i < limit; i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    JSONObject billing = json.getJSONObject("billing");
                    if (!existContact(billing.getString("phone"))) {
                        Contact contact = new Contact("Footprints - " + billing.getString("first_name") + " " + billing.getString("last_name"), billing.getString("phone"), billing.getString("email"));
                        saveContact(contact);
                        setSaved(json.getString("reference"));
                    }
                }
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
        }, error -> Log.e("Error", "Errorrrrrr"));
        requestQueue.add(stringRequest);
    }

    private void setSaved(String reference) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(CONTACT_SCHEDULER_API_URL + reference);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("saved", true);

                Log.i("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    private boolean existContact(String number) {
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] projection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = this.getContentResolver().query(lookupUri, projection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return false;
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