package com.josselinbuils.SMSManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.josselinbuils.youbisms.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONException;
import org.json.JSONObject;

public final class Events {
  public static CallbackContext receiveMessagesCallbackContext;

  public static void displayNotification(Context context, JSONObject contact, String body) {
    Notification.Builder notification = new Notification.Builder(context);
    
    notification.setSmallIcon(R.drawable.icon_small);
    notification.setContentText(body);
    notification.setLights(Color.GREEN, 200, 1000);
    notification.setPriority(Notification.PRIORITY_HIGH);
    notification.setDefaults(Notification.DEFAULT_SOUND);
    notification.setDefaults(Notification.DEFAULT_VIBRATE);
    notification.setAutoCancel(true);

    try {
      notification.setLargeIcon(Contacts.getBitmap(context, contact.has("id") ? contact.getLong("id") : -1));
      notification.setContentTitle(contact.getString("name"));
    } catch (JSONException e) {
        e.printStackTrace();
    }

    Intent notificationIntent = new Intent(context, com.josselinbuils.youbisms.CordovaApp.class);
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
    notification.setContentIntent(intent);

    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(1, notification.build());
  }

  public static void listenEvents(CallbackContext callbackContext) {
    Events.receiveMessagesCallbackContext = callbackContext;
  }

  public static void sendEvent(String event) {
    if (Events.receiveMessagesCallbackContext != null) {
      PluginResult res = new PluginResult(PluginResult.Status.OK, event);
      res.setKeepCallback(true);
      Events.receiveMessagesCallbackContext.sendPluginResult(res);
    }
  }
}