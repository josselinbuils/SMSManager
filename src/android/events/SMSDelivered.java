package com.josselinbuils.SMSManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SMSDelivered extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)  {
      Log.i(SMSManager.TAG, "SMSDelivered: sms delivered.");
    }
  }