package com.josselinbuils.SMSManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract;

import com.josselinbuils.youbisms.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.lang.StringBuilder;

import org.json.JSONException;
import org.json.JSONObject;

public final class Contacts {

  /* Public functions */

  public static String getBase64Photo(ContentResolver cr, long contactId, boolean highRes) {
    String photo = null;

    try {
      Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
      InputStream photoStream = ContactsContract.Contacts.openContactPhotoInputStream(cr, contactUri, highRes);

      if (photoStream != null) {
        StringBuilder sb = new StringBuilder();
        sb.append("data:image/png;base64,");
        sb.append(new String(Base64.encodeBase64(IOUtils.toByteArray(photoStream), false), "UTF-8"));
        photo = sb.toString();
      }
    
    } catch (IOException e) {
      e.printStackTrace();
    }

    return photo;
  }

  public static JSONObject getContactbyPhoneNumber(ContentResolver cr, String phoneNumber) {
    JSONObject contact = new JSONObject();

    try {
      Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
      String[] projection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.NORMALIZED_NUMBER };
      Cursor cursor = cr.query(uri, projection, null, null, null);

      String name = phoneNumber;
      boolean hasPhoto = false;
      String nPhoneNumber = phoneNumber;

      try {
        
        if (cursor.moveToFirst()) {
          long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
          nPhoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.NORMALIZED_NUMBER));

          hasPhoto = checkPhoto(cr, id);
          name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));

          contact.put("id", id);
        }
      
      } finally {
        cursor.close();
      }

      contact.put("name", name);
      contact.put("hasPhoto", hasPhoto);
      contact.put("phoneNumber", nPhoneNumber);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return contact;
  }

  /* Private functions */

  private static boolean checkPhoto(ContentResolver cr, long contactId) {
    return getBase64Photo(cr, contactId, false) != null;
  }

  public static Bitmap getBitmap(Context context, long contactId) {
    Bitmap userIcon = getCroppedBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.user));

    if (contactId != -1) {
      Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
      InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri);

      if (input == null) {
        return userIcon;
      }

      return getCroppedBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), 192, 192, true));
    }

    return userIcon;
  }

  public static JSONObject getContactByRecipientId(ContentResolver cr, long id) {
    JSONObject contact = new JSONObject();

    Uri uri = ContentUris.withAppendedId(Uri.parse("content://mms-sms/canonical-address"), id);
    Cursor cursor = cr.query(uri, null, null, null, null);

    try {
      if (cursor.moveToFirst()) {
        contact = Contacts.getContactbyPhoneNumber(cr, cursor.getString(0));
      }
    } finally {
      cursor.close();
    }

    return contact;
  }

  private static Bitmap getCroppedBitmap(Bitmap bitmap) {
    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final int color = 0xff424242;
    final Paint paint = new Paint();
    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(color);
    canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    canvas.drawBitmap(bitmap, rect, rect, paint);

    return output;
  }
}