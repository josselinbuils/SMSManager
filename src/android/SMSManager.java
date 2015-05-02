package org.josselinbuils.SMSManager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class SMSManager extends CordovaPlugin {
  private ContentResolver contentResolver;

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    this.contentResolver = cordova.getActivity().getContentResolver();

    if (action.equals("getConversations")) {
      final JSONObject options = args.get(0) instanceof JSONObject ? args.getJSONObject(0) : null;

      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          try {
            JSONArray res = getConversations(options);
            callbackContext.success(res);
          } catch (JSONException e) {
            // Ignored
          }
        }
      });
    } else {
      return false;
    }

    return true;
  }

  private JSONObject getContact(String phoneNumber) throws JSONException {
    Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
    Cursor cursor = contentResolver.query(uri, new String[]{ PhoneLookup._ID, PhoneLookup.DISPLAY_NAME }, null, null, null);
    JSONObject contact = new JSONObject();
    String name = phoneNumber;

    if (cursor.moveToFirst()) {
      contact.put("id", cursor.getLong(cursor.getColumnIndex(PhoneLookup._ID)));
      name = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
      cursor.close();
    }

    contact.put("name", name);

    return contact;
  }

  private JSONArray getConvMessages(long convId) throws JSONException {
    JSONArray messages = new JSONArray();
    Uri uri = Uri.parse("content://mms-sms/conversations/" + String.valueOf(convId));
    String[] projection = { "ct_t", "_id", "address", "body", "date", "date_sent", "read", "type", "msg_box" };
    Cursor cursor = contentResolver.query(uri, projection, null, null, null);

    while (cursor.moveToNext()) {
      JSONObject message = new JSONObject();

      String ct_t = cursor.getString(cursor.getColumnIndex("ct_t"));
      String address, type;

      if ("application/vnd.wap.multipart.related".equals(ct_t)) {
        // MMS

        long mmsId = cursor.getLong(cursor.getColumnIndex("_id"));

        message.put("address", getMmsAddress(mmsId));

        // Get and format box type
        switch (cursor.getInt(cursor.getColumnIndex("msg_box"))) {
          case 1:
            message.put("box", "inbox");
            break;
          case 2:
            message.put("box", "sent");
            break;
          case 3:
            message.put("box", "draft");
            break;
          case 4:
            message.put("box", "outbox");
            break;
          case 5:
            message.put("box", "failed");
            break;
        }

        message.put("content", getMmsContent(mmsId));
        message.put("date", cursor.getLong(cursor.getColumnIndex("date")) * 1000);
        message.put("type", "mms");

      } else {
        // SMS

        message.put("address", cursor.getString(cursor.getColumnIndex("address")));

        // Get and format box type
        switch (cursor.getInt(cursor.getColumnIndex("type"))) {
          case 1:
            message.put("box", "inbox");
            break;
          case 2:
            message.put("box", "sent");
            break;
          case 3:
            message.put("box", "draft");
            break;
          case 4:
            message.put("box", "outbox");
            break;
          case 5:
            message.put("box", "failed");
            break;
          case 6:
            message.put("box", "queued");
            break;
        }

        message.put("body", cursor.getString(cursor.getColumnIndex("body")));
        message.put("date", cursor.getLong(cursor.getColumnIndex("date")));
        message.put("date_sent", cursor.getLong(cursor.getColumnIndex("date_sent")));
        message.put("read", cursor.getInt(cursor.getColumnIndex("read")));
        message.put("type", "sms");
      }

      messages.put(message);
    }

    if (cursor != null) {
      cursor.close();
    }

    return messages;
  }

  private JSONArray getConversations(JSONObject options) throws JSONException {
    boolean includeContactsInfos = options.has("includeContactsInfos") ? options.getBoolean("includeContactsInfos") : false;
    boolean includeMessages = options.has("includeMessages") ? options.getBoolean("includeMessages") : false;

    JSONArray conversations = new JSONArray();
    Uri uri = Uri.parse("content://mms-sms/conversations");
    String[] projection = { "thread_id", "ct_t", "_id", "address", "body", "date", "date_sent" };
    String sortOrder = "date DESC";
    Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);

    while (cursor.moveToNext()) {
      JSONObject conversation = new JSONObject();

      long thread_id = cursor.getLong(cursor.getColumnIndex("thread_id"));
      String ct_t = cursor.getString(cursor.getColumnIndex("ct_t"));
      String address;

            // Check if last message is MMS or SMS
      if ("application/vnd.wap.multipart.related".equals(ct_t)) {
                // MMS

        long id = cursor.getLong(cursor.getColumnIndex("_id"));

        address = getMmsAddress(id);
        conversation.put("address", address);

        /* Get MMS text part */

        JSONArray mmsContent = getMmsContent(id);
        String body = "";

        for (int i = 0; i < mmsContent.length(); i++) {
          JSONObject part = mmsContent.getJSONObject(i);

          if ("text/plain".equals(part.getString("type"))) {
            body += part.getString("body") + " ";
          }
        }

        conversation.put("body", body);
        conversation.put("convId", thread_id);
        conversation.put("date", cursor.getLong(cursor.getColumnIndex("date")) * 1000);

      } else {
                // SMS

        address = cursor.getString(cursor.getColumnIndex("address"));
        conversation.put("address", address);
        conversation.put("body", cursor.getString(cursor.getColumnIndex("body")));
        conversation.put("convId", thread_id);
        conversation.put("date", cursor.getLong(cursor.getColumnIndex("date")));
        conversation.put("dateSent", cursor.getLong(cursor.getColumnIndex("date_sent")));
      }

            // Get contact infos if includeContactsInfos is true
      if (includeContactsInfos) {
        JSONObject contact = getContact(address);
        conversation.put("contactName", contact.getString("name"));

        if (contact.has("id")) {
          conversation.put("contactId", contact.getLong("id"));
        }
      }

            // Get all conversation messages if includeMessages is true
      if (includeMessages) {
        JSONArray messages = getConvMessages(thread_id);
        conversation.put("messages", messages);
      }

      conversations.put(conversation);
    }

    if (cursor != null) {
      cursor.close();
    }

    return conversations;
  }

  private String getMmsAddress(long id) {
    String address = null;

    Uri uri = Uri.parse("content://mms/" + String.valueOf(id) + "/addr");
    Cursor cursor = contentResolver.query(uri, null, null, null, null);

    if (cursor.moveToFirst()) {
      address = cursor.getString(cursor.getColumnIndex("address"));
    }

    if (cursor != null) {
      cursor.close();
    }

    return address;
  }

  private JSONArray getMmsContent(long mmsId) throws JSONException {
    JSONArray parts = new JSONArray();

    Uri uri = Uri.parse("content://mms/part");
    String selection = "mid=" + String.valueOf(mmsId);
    Cursor cursor = contentResolver.query(uri, null, selection, null, null);

    while (cursor.moveToNext()) {
      JSONObject part = new JSONObject();

      String type = cursor.getString(cursor.getColumnIndex("ct"));
      part.put("type", type);

      if ("text/plain".equals(type)) {
        String body = cursor.getString(cursor.getColumnIndex("text"));
        part.put("body", body);
      } else if ("image/jpeg".equals(type) || "image/bmp".equals(type) || "image/gif".equals(type) || "image/jpg".equals(type) || "image/png".equals(type)) {
        String src = "content://mms/part/" + cursor.getString(cursor.getColumnIndex("_id"));
        part.put("src", src);
      }

      parts.put(part);
    }

    if (cursor != null) {
      cursor.close();
    }

    return parts;
  }
}