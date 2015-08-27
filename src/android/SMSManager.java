package com.josselinbuils.SMSManager;

import android.content.ContentResolver;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class SMSManager extends CordovaPlugin {
  public static final String TAG = "YoubiSMS";

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    final ContentResolver contentResolver = cordova.getActivity().getContentResolver();
    final JSONObject infos = (args != null && args.length() > 0 && args.get(0) instanceof JSONObject) ? args.getJSONObject(0) : null;

    if (action.equals("deleteConversation")) {

      if (infos != null && infos.has("id")) {
        Conversations.deleteConversation(contentResolver, infos.getLong("id"));
        callbackContext.success();

      } else {
        callbackContext.error("id cannot be empty");
        return false;
      }
      
    } else if (action.equals("getConversations")) {
      
      // Create new thread to dont block Webview core
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          callbackContext.success(Conversations.getConversations(contentResolver));
        }
      });
      
    } else if (action.equals("getConvMessages")) {

      if (infos != null && infos.has("id")) {
        long id = infos.getLong("id");
        callbackContext.success(Conversations.getConvMessages(contentResolver, id));
      } else {
        callbackContext.error("missing conversation id");
      }
      
    } else if (action.equals("getContactPhoto")) {

      if (infos != null && infos.has("id")) {
        long id = infos.getLong("id");
        callbackContext.success(Contacts.getBase64Photo(contentResolver, id, true));
      } else {
        callbackContext.error("missing contact id");
      }
      
    } else if (action.equals("getContactThumbnail")) {

      if (infos != null && infos.has("id")) {
        final long id = infos.getLong("id");

        // Create new thread to dont block Webview core
        cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            callbackContext.success(Contacts.getBase64Photo(contentResolver, id, false));
          }
        });
      } else {
        callbackContext.error("missing contact id");
      }
      
    } else if (action.equals("listenEvents")) {

      Events.listenEvents(callbackContext);

      // Create an empty result
      PluginResult res = new PluginResult(PluginResult.Status.NO_RESULT);

      // Ask to keep listen for callbacks
      res.setKeepCallback(true);

      // Send result
      callbackContext.sendPluginResult(res);
      
    } else if (action.equals("sendSMS")) {

      if (infos != null && infos.has("phoneNumber") && infos.has("body")) {
        String phoneNumber = infos.getString("phoneNumber");
        String body = infos.getString("body");

        if (phoneNumber.length() == 0) {
          callbackContext.error("phoneNumber cannot be empty");
          return false;
          
        } else if (body.length() == 0) {
          callbackContext.error("body cannot be empty");
          return false;
          
        } else {
          SMS.sendSMS(cordova.getActivity().getApplicationContext(), infos.getString("phoneNumber"), infos.getString("body"));
          callbackContext.success();
        }
      } else {
        callbackContext.error("missing parameters");
      }
    } else {
      return false;
    }

    return true;
  }
}