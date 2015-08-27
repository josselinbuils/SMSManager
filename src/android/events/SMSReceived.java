package com.josselinbuils.SMSManager;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import java.util.HashMap;
import java.util.Map.Entry;

public class SMSReceived extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)  {
      Log.i(SMSManager.TAG, "SMSReceived: sms received.");

      final Bundle bundle = intent.getExtras();

      String address = "", body = "";

      if (bundle != null) {
        final Object[] pdus = (Object[]) bundle.get("pdus");
        SmsMessage[] inMessages = new SmsMessage[pdus.length];
        HashMap<String, String> outMessages = new HashMap<String, String>(pdus.length);

        for (int i = 0; i < pdus.length; i++) {
          inMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

          address = inMessages[i].getOriginatingAddress();
          body = inMessages[i].getMessageBody();

          if (!outMessages.containsKey(address)) { 
            outMessages.put(address, body); 
          } else {    
            outMessages.put(address, outMessages.get(address) + body);
          }
        }

        for (Entry<String, String> message : outMessages.entrySet()) {
          
          ContentValues sms = new ContentValues();
          sms.put("address", message.getKey());
          sms.put("body", message.getValue());
          context.getContentResolver(). insert(Uri.parse("content://sms/inbox"), sms);

          Events.displayNotification(context, Contacts.getContactbyPhoneNumber(context.getContentResolver(), address), body);
        }
      }

      Events.sendEvent("messageReceived");
    }
  }