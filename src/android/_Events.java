package org.josselinbuils.SMSManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

public final class _Events {

  public static void listenMessages(Activity activity, CallbackContext callbackContext) {

    // Create filter for MessagesListener BroadcastReceiver
    IntentFilter filter = new IntentFilter();

    // Receive SMS
    filter.addAction("android.provider.Telephony.SMS_RECEIVED");

    // Receive MMS
    filter.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");

    // Start listening for new MMS/SMS
    activity.registerReceiver(new MessagesListener(callbackContext), filter);
  }

  private static class MessagesListener extends BroadcastReceiver {
    // Receive MMS and SMS

    private CallbackContext callbackContext;

    public MessagesListener(CallbackContext callbackContext) {
      this.callbackContext = callbackContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      PluginResult res = new PluginResult(PluginResult.Status.OK, "Message received");
      res.setKeepCallback(true);
      callbackContext.sendPluginResult(res);
    }
  }
}