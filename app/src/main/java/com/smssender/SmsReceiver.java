package com.smssender;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsMessage;
import android.util.Log;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SmsReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        SharedPreferences pref = context.getSharedPreferences("SmsSender", Context.MODE_PRIVATE);
        pref.edit().putBoolean("isNewMessage",true).commit();

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (Object aPdusObj : pdusObj) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                    String conversationMessage = senderNum + FileUtils.MESSAGE_SEPERATOR + message
                            + FileUtils.MESSAGE_SEPERATOR + getCurrentDateTime();

                    FileUtils.writeMessageToFile(context, conversationMessage);
                    showNotification(context, message, senderNum);
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);
        }
    }

    private void showNotification(Context context, String message, String number) {
        Intent resultIntent = new Intent(context, ConversationActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack
        stackBuilder.addParentStack(ConversationActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(number);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(getUniqueValue(), builder.build());
    }

    private int getUniqueValue() {
        int uniqueVal = (int) (System.currentTimeMillis() / 1000);
        return uniqueVal;
    }

    public String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
