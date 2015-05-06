package org.josselinbuils.SMSManager;

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

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    final ContentResolver contentResolver = cordova.getActivity().getContentResolver();

    if (action.equals("getConversations")) {
      final JSONObject options = args.get(0) instanceof JSONObject ? args.getJSONObject(0) : null;

      // Create new thread to dont block Webview core
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          try {
            JSONArray res = _Conversations.getConversations(contentResolver, options);
            callbackContext.success(res);
          } catch (JSONException e) {
            // Ignored
          }
        }
      });
      
    } else if (action.equals("receiveMessages")) {

      _Events.listenMessages(cordova.getActivity(), callbackContext);

      // Create an empty result
      PluginResult res = new PluginResult(PluginResult.Status.NO_RESULT);

      // Ask to keep listen for callbacks
      res.setKeepCallback(true);
      
      // Send result
      callbackContext.sendPluginResult(res);
      
    } else if (action.equals("sendSMS")) {
      final JSONObject infos = args.get(0) instanceof JSONObject ? args.getJSONObject(0) : null;

      if (infos != null && infos.has("phoneNumber") && infos.has("body")) {
        String phoneNumber = infos.getString("phoneNumber");
        String body = infos.getString("body");

        if (phoneNumber.length() == 0) {
          callbackContext.error("Phone number cannot be empty.");
          return false;
          
        } else if (body.length() == 0) {
          callbackContext.error("Body cannot be empty.");
          return false;
          
        } else {
          _SMS.sendSMS(infos.getString("phoneNumber"), infos.getString("body"));
          callbackContext.success();
        }
      } else {
        callbackContext.error("Missing parameters.");
      }
    } else {
      return false;
    }

    return true;
  }
}