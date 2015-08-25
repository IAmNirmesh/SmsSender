package com.smssender;

import android.animation.Animator;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.StringTokenizer;

public class ConversationActivity extends AppCompatActivity implements Animator.AnimatorListener {

    private TextView mNumber;
    private TextView mMessage;
    private TextView mDateTime;
    private TextView mEmptyText;
    private RelativeLayout mConversationContainer;
    private RelativeLayout mParentContainer;
    private boolean isknownNumber = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        mNumber = (TextView) findViewById(R.id.number);
        mMessage = (TextView) findViewById(R.id.message);
        mDateTime = (TextView) findViewById(R.id.dateTime);
        mEmptyText = (TextView) findViewById(R.id.emptyText);
        mConversationContainer = (RelativeLayout) findViewById(R.id.container);
        mParentContainer = (RelativeLayout) findViewById(R.id.parentContainer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSwipeListener();

        boolean isNewMessage = SmsSenderApplication.prefs.getBoolean("isNewMessage", false);
        Log.i("test", "" + isNewMessage);
        if(isNewMessage) {
            setTypeFace(Typeface.BOLD);
            SmsSenderApplication.prefs.edit().putBoolean("isNewMessage", false).commit();
        }
        else if(SmsSenderApplication.prefs.getBoolean("isRead",false))
            setTypeFace(Typeface.NORMAL);

        setUpValues();
    }

    private void setSwipeListener() {
        mConversationContainer.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeRight();
                mConversationContainer.animate().x((-getMovableXcords())).setListener(ConversationActivity.this);
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                mConversationContainer.animate().x(getMovableXcords()).setListener(ConversationActivity.this);
            }
        });
    }

    private void setUpValues() {
        String textMessage = FileUtils.readMessageFromFile(this);
        if(!TextUtils.isEmpty(textMessage)) {
            StringTokenizer st = new StringTokenizer(textMessage, FileUtils.MESSAGE_SEPERATOR);
            int i = 0;
            while (st.hasMoreTokens()) {
                switch (i) {
                    case 0:
                        String number = st.nextToken();
                        mNumber.setText(number);
                        isknownNumber = contactExists(this, number);
                        if (!isknownNumber) {
                            mConversationContainer.setBackgroundColor(getResources().getColor(R.color.unknown_bg_color));
                        }
                        else
                            mConversationContainer.setBackgroundColor(getResources().getColor(R.color.known_bg_color));
                        break;
                    case 1:
                        mMessage.setText(st.nextToken());
                        break;
                    case 2:
                        mDateTime.setText(st.nextToken());
                        break;
                }
                i++;
            }
        } else {
            mConversationContainer.setVisibility(View.GONE);
            mEmptyText.setVisibility(View.VISIBLE);
        }
    }

    private void setTypeFace(int type) {
        mNumber.setTypeface(null, type);
        mMessage.setTypeface(null, type);
        mDateTime.setTypeface(null, type);
    }


    private boolean contactExists(Context context, String number) {
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    private float getMovableXcords() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return displayMetrics.widthPixels * 0.75f;
    }

    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {
        if(mConversationContainer != null) {
            mConversationContainer.animate().x(20);
            if (isknownNumber) {
                setTypeFace(Typeface.NORMAL);
                SmsSenderApplication.prefs.edit().putBoolean("isRead", true).commit();
            }
            else {
                mParentContainer.removeView(mConversationContainer);
                mEmptyText.setVisibility(View.VISIBLE);
                FileUtils.writeMessageToFile(ConversationActivity.this, "");
                SmsSenderApplication.prefs.edit().putBoolean("isRead", false).commit();
            }
        }
    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }
}
