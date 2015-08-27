package com.josselinbuils.SMSManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

import java.lang.System;
import java.util.ArrayList;

public final class SMS {
  private static final int MAXSMS_MESSAGE_LENGTH = 160;

  public static void sendSMS(Context context, String phoneNumber, String message) {
    Log.i(SMSManager.TAG, "SMS->sendSMS()");

    // Create message
    ContentValues sms = new ContentValues();
    sms.put("address", phoneNumber);
    sms.put("date_sent", System.currentTimeMillis());
    sms.put("body", message);

    // Add message to outbox and store new URI
    Uri smsUri = context.getContentResolver().insert(Uri.parse("content://sms/outbox"), sms);

    /* Sent event */

    Intent sentIntent = new Intent("SMS_SENT");
    sentIntent.putExtra("address", phoneNumber);
    sentIntent.putExtra("date_sent", System.currentTimeMillis());
    sentIntent.putExtra("body", message);
    sentIntent.putExtra("smsUri", smsUri.toString());

    final PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    
    /* Delivered event */

    final PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent("SMS_DELIVERED"), PendingIntent.FLAG_UPDATE_CURRENT);

    /* Send message */

    SmsManager smsManager = SmsManager.getDefault(); 

    if( message.length() > MAXSMS_MESSAGE_LENGTH) {
      ArrayList<String> messages = smsManager.divideMessage(message);     
      ArrayList<PendingIntent> sentPIArray = new ArrayList<PendingIntent>(){{ add(sentPI); }};
      ArrayList<PendingIntent> deliveredPIArray = new ArrayList<PendingIntent>(){{ add(deliveredPI); }};     
      
      smsManager.sendMultipartTextMessage(phoneNumber, null, messages, sentPIArray, deliveredPIArray);
    } else {
      smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    Events.sendEvent("dataUpdated");
  }
}