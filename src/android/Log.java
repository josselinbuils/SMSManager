package com.josselinbuils.SMSManager;

import com.josselinbuils.youbisms.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

public final class Log {
  public static CallbackContext logCallbackContext;

  public static void error(String str) {
    if (Log.logCallbackContext != null) {
      PluginResult res = new PluginResult(PluginResult.Status.ERROR, str);
      res.setKeepCallback(true);
      Log.logCallbackContext.sendPluginResult(res);
    }
  }

  public static void listenLogs(CallbackContext callbackContext) {
    Log.logCallbackContext = callbackContext;
  }

  public static void log(String str) {
    if (Log.logCallbackContext != null) {
      PluginResult res = new PluginResult(PluginResult.Status.OK, str);
      res.setKeepCallback(true);
      Log.logCallbackContext.sendPluginResult(res);
    }
  }
}