package org.josselinbuils.SMSManager;

import android.telephony.SmsManager;

import java.util.ArrayList;

public final class _SMS {
  private static final int MAX_SMS_MESSAGE_LENGTH = 160;

  public static void sendSMS(String phoneNumber, String message) {
    SmsManager smsManager = SmsManager.getDefault(); 

    if( message.length() > MAX_SMS_MESSAGE_LENGTH) {
      ArrayList<String> messages = smsManager.divideMessage(message);          
      smsManager.sendMultipartTextMessage(phoneNumber, null, messages, null, null);
    } else {
      smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }
  }
}