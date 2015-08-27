package com.josselinbuils.SMSManager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class MMS {

	public static String getMmsAddress(ContentResolver cr, long id) {
		String address = null;

		Uri uri = Uri.parse("content://mms/" + String.valueOf(id) + "/addr");
		Cursor cursor = cr.query(uri, null, null, null, null);

		if (cursor.moveToFirst()) {
			address = cursor.getString(cursor.getColumnIndex("address"));
		}

		if (cursor != null) {
			cursor.close();
		}

		return address;
	}

	public static JSONArray getMmsContent(ContentResolver cr, long mmsId) throws JSONException {
		JSONArray parts = new JSONArray();

		Uri uri = Uri.parse("content://mms/part");
		String selection = "mid=" + String.valueOf(mmsId);
		Cursor cursor = cr.query(uri, null, selection, null, null);

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