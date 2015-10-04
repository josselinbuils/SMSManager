package com.josselinbuils.SMSManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.apache.cordova.CallbackContext;

public class SMSSent extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)  {

      /* Get data from intent */

      Bundle bundle = intent.getExtras();

      ContentValues sms = new ContentValues();
      sms.put("address", bundle.getString("address"));
      sms.put("date_sent", bundle.getLong("date_sent"));
      sms.put("body", bundle.getString("body"));

      Uri smsUri = Uri.parse(bundle.getString("smsUri"));

      // Delete message from outbox
      context.getContentResolver().delete(smsUri, null, null);

      int resultCode = getResultCode();

      if (resultCode == Activity.RESULT_OK) {
        Log.log("SMSSent: message sent.");

        // Add message to sent folder
        context.getContentResolver().insert(Uri.parse("content://sms/sent"), sms);

      } else { // Fail
        Log.error("SMSSent: fail to send message (code " + resultCode + ").");

        // Add error to message
        sms.put("error_code", resultCode);

        // Add message to outbox
        context.getContentResolver().insert(Uri.parse("content://sms/outbox"), sms);
      }

      Events.sendEvent("dataUpdated");
    }
  }