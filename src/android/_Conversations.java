package org.josselinbuils.SMSManager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class _Conversations {
  private static JSONObject getContact(ContentResolver cr, String phoneNumber) throws JSONException {
    Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
    Cursor cursor = cr.query(uri, new String[]{ PhoneLookup._ID, PhoneLookup.DISPLAY_NAME }, null, null, null);
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

  private static JSONArray getConvMessages(ContentResolver cr, long convId) throws JSONException {
    JSONArray messages = new JSONArray();
    Uri uri = Uri.parse("content://mms-sms/conversations/" + String.valueOf(convId));
    String[] projection = { "ct_t", "_id", "address", "body", "date", "date_sent", "read", "type", "msg_box" };
    Cursor cursor = cr.query(uri, projection, null, null, null);

    while (cursor.moveToNext()) {
      JSONObject message = new JSONObject();

      String ct_t = cursor.getString(cursor.getColumnIndex("ct_t"));
      String address, type;

      if ("application/vnd.wap.multipart.related".equals(ct_t)) {
        // MMS

        long mmsId = cursor.getLong(cursor.getColumnIndex("_id"));

        message.put("address", _MMS.getMmsAddress(cr, mmsId));

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

        message.put("content", _MMS.getMmsContent(cr, mmsId));
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

  public static JSONArray getConversations(ContentResolver cr, JSONObject options) throws JSONException {
    List<JSONObject> conversations = new ArrayList<JSONObject>();

    boolean includeContactsInfos = options.has("includeContactsInfos") ? options.getBoolean("includeContactsInfos") : false;
    boolean includeMessages = options.has("includeMessages") ? options.getBoolean("includeMessages") : false;

    Uri uri = Uri.parse("content://mms-sms/conversations");
    String[] projection = { "thread_id", "ct_t", "_id", "address", "body", "date", "date_sent" };
    Cursor cursor = cr.query(uri, projection, null, null, null);

    while (cursor.moveToNext()) {
      JSONObject conversation = new JSONObject();

      long thread_id = cursor.getLong(cursor.getColumnIndex("thread_id"));
      String ct_t = cursor.getString(cursor.getColumnIndex("ct_t"));
      String address;

      // Check if last message is MMS or SMS
      if ("application/vnd.wap.multipart.related".equals(ct_t)) {
        // MMS

        long id = cursor.getLong(cursor.getColumnIndex("_id"));

        address = _MMS.getMmsAddress(cr, id);
        conversation.put("address", address);

        /* Get MMS text part */

        JSONArray mmsContent = _MMS.getMmsContent(cr, id);
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
        JSONObject contact = getContact(cr, address);
        conversation.put("contactName", contact.getString("name"));

        if (contact.has("id")) {
          conversation.put("contactId", contact.getLong("id"));
        }
      }

            // Get all conversation messages if includeMessages is true
      if (includeMessages) {
        JSONArray messages = getConvMessages(cr, thread_id);
        conversation.put("messages", messages);
      }

      conversations.add(conversation);
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
          // Ignored
        }

        return bVal.compareTo(aVal);
      }
    });

    return new JSONArray(conversations);
  }
}