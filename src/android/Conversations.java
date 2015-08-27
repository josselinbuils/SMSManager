package com.josselinbuils.SMSManager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony.Threads;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class Conversations {

  public static void deleteConversation(ContentResolver cr, long convId) {
    cr.delete(Uri.parse("content://mms-sms/conversations/" + convId), null, null);
  }

  public static JSONArray getConversations(ContentResolver cr) {
    List<JSONObject> conversations = new ArrayList<JSONObject>();

    try {
      Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
      String[] projection = { Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.READ, Threads.RECIPIENT_IDS, Threads.SNIPPET };
      Cursor cursor = cr.query(uri, projection, null, null, null);

      while (cursor.moveToNext()) {
        int messageCount = cursor.getInt(cursor.getColumnIndex(Threads.MESSAGE_COUNT));

        if (messageCount > 0) {
          JSONObject conversation = new JSONObject();

          long id = cursor.getLong(cursor.getColumnIndex(Threads._ID));
          long date = cursor.getLong(cursor.getColumnIndex(Threads.DATE));
          int read = cursor.getInt(cursor.getColumnIndex(Threads.READ));
          String recipientIds = cursor.getString(cursor.getColumnIndex(Threads.RECIPIENT_IDS));
          String snippet = cursor.getString(cursor.getColumnIndex(Threads.SNIPPET));

          conversation.put("id", id);
          conversation.put("date", date);
          conversation.put("read", read);
          conversation.put("recipientIds", recipientIds);
          conversation.put("snippet", snippet);

          JSONArray contacts = new JSONArray();
          List<String> recIds = Arrays.asList(recipientIds.split("\\S* \\S*"));

          for (String recId : recIds) {
            contacts.put(Contacts.getContactByRecipientId(cr, Long.parseLong(recId)));
          }

          conversation.put("contacts", contacts);
          conversations.add(conversation);
        }
      }

      if (cursor != null) {
        cursor.close();
      }

      /* Sort list */

      Collections.sort(conversations, new Comparator<JSONObject>() {
        public int compare(JSONObject a, JSONObject b) {
          String aVal = new String();
          String bVal = new String();

          try {
            aVal = a.getString("date");
            bVal = b.getString("date");
          } catch (JSONException e) {
            e.printStackTrace();
          }

          return bVal.compareTo(aVal);
        }
      });
    
    } catch (JSONException e) {
        e.printStackTrace();
      }

    return new JSONArray(conversations);
  }

  public static JSONArray getConvMessages(ContentResolver cr, long convId) {
    JSONArray messages = new JSONArray();

    try {
      Uri uri = Uri.parse("content://mms-sms/conversations/" + String.valueOf(convId));
      String[] projection = { "ct_t", "_id", "address", "body", "date", "date_sent", "error_code", "read", "type", "msg_box" };
      Cursor cursor = cr.query(uri, projection, null, null, null);

      while (cursor.moveToNext()) {
        JSONObject message = new JSONObject();

        String ct_t = cursor.getString(cursor.getColumnIndex("ct_t"));
        String address, type;

        if ("application/vnd.wap.multipart.related".equals(ct_t)) {
          // MMS

          long mmsId = cursor.getLong(cursor.getColumnIndex("_id"));

          message.put("address", MMS.getMmsAddress(cr, mmsId));

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

          message.put("content", MMS.getMmsContent(cr, mmsId));
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
          message.put("dateSent", cursor.getLong(cursor.getColumnIndex("date_sent")));
          message.put("read", cursor.getInt(cursor.getColumnIndex("read")));
          message.put("type", "sms");
        }

        message.put("errorCode", cursor.getInt(cursor.getColumnIndex("error_code")));
        messages.put(message);
      }

      if (cursor != null) {
        cursor.close();
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return messages;
  }
}