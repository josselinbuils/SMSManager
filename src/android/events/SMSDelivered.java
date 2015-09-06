package com.josselinbuils.SMSManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SMSDelivered extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)  {
      Log.log("SMSDelivered: sms delivered.");
    }
  }