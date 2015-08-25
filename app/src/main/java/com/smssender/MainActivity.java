package com.smssender;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mPhoneNumber;
    private final static int PICK_CONTACT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPhoneNumber = (EditText) findViewById(R.id.phoneNumber);
        Button pickContact = (Button) findViewById(R.id.pickContact);
        Button sendSms = (Button) findViewById(R.id.sendSms);
        Button viewConversation = (Button) findViewById(R.id.lastSms);

        pickContact.setOnClickListener(this);
        sendSms.setOnClickListener(this);
        viewConversation.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pickContact:
                pickContact();
                break;
            case R.id.sendSms:
                String phoneNumber = mPhoneNumber.getText().toString();
                if(!TextUtils.isEmpty(phoneNumber) && phoneNumber.length() > 10)
                    openSmsApp(phoneNumber);
                else
                    mPhoneNumber.setError("Invalid phone number");
                break;
            case R.id.lastSms:
                Intent intent = new Intent(this, ConversationActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void pickContact() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContactIntent, PICK_CONTACT);
    }
    private void openSmsApp(String phoneNumber) {
        Uri uri = Uri.parse("smsto:" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactUri = data.getData();
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(column);
                number = number.replaceAll("\\s+","");
                mPhoneNumber.setText(number);
                cursor.close();
            }
        }
    }
}
