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
    private ContentResolver cr;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.cr = cordova.getActivity().getContentResolver();
        
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
        Cursor cursor = cr.query(uri, new String[]{ PhoneLookup._ID, PhoneLookup.DISPLAY_NAME }, null, null, null);
        JSONObject contact = new JSONObject();
        String name = phoneNumber;

        try {
            if (cursor.moveToFirst()) {
                contact.put("id", cursor.getLong(cursor.getColumnIndex(PhoneLookup._ID)));
                name = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        contact.put("name", name);

        return contact;
    }

    private JSONArray getConvMessages(long id) throws JSONException {
        JSONArray messages = new JSONArray();
        Uri uri = Uri.parse("content://mms-sms/conversations/" + String.valueOf(id));
        Cursor cursor = cr.query(uri, new String[]{ "ct_t", "address", "body", "date", "date_sent", "read", "type" }, null, null, null);

        while (cursor.moveToNext()) {
            JSONObject message = new JSONObject();

            String ct_t = cursor.getString(cursor.getColumnIndex("ct_t"));
            if ("application/vnd.wap.multipart.related".equals(ct_t)) {
                // MMS
            } else {
                message.put("address", cursor.getString(cursor.getColumnIndex("address")));
                message.put("body", cursor.getString(cursor.getColumnIndex("body")));
                message.put("date", cursor.getLong(cursor.getColumnIndex("date")));
                message.put("dateSent", cursor.getLong(cursor.getColumnIndex("date_sent")));
                message.put("read", cursor.getInt(cursor.getColumnIndex("read")));
                message.put("type", cursor.getInt(cursor.getColumnIndex("type")));
            }

            messages.put(message);
        }

        return messages;
    }

    private JSONArray getConversations(JSONObject options) throws JSONException {
        boolean includeContactsInfos = options.has("includeContactsInfos") ? options.getBoolean("includeContactsInfos") : false;
        boolean includeMessages = options.has("includeMessages") ? options.getBoolean("includeMessages") : false;

        JSONArray conversations = new JSONArray();
        Uri uri = Uri.parse("content://mms-sms/conversations");
        Cursor cursor = cr.query(uri, new String[]{ "address", "body", "date", "thread_id" }, null, null, "date DESC");

        while (cursor.moveToNext()) {
            JSONObject conversation = new JSONObject();

            /* Conversation infos */

            String address = cursor.getString(cursor.getColumnIndex("address"));
            long convId = cursor.getLong(cursor.getColumnIndex("thread_id"));
            conversation.put("address", address);
            conversation.put("body", cursor.getString(cursor.getColumnIndex("body")));
            conversation.put("convId", convId);
            conversation.put("date", cursor.getLong(cursor.getColumnIndex("date")));

            /* Contact infos */

            if (includeContactsInfos) {
                JSONObject contact = getContact(address);
                conversation.put("contactName", contact.getString("name"));
                
                if (contact.has("id")) {
                    conversation.put("contactId", contact.getLong("id"));
                }
            }

            /* Messages */

            if (includeMessages) {
                JSONArray messages = getConvMessages(convId);
                conversation.put("messages", messages);
            }

            conversations.put(conversation);
        }

        return conversations;
    }
}