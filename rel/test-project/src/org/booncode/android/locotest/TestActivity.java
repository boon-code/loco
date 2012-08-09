package org.booncode.android.locotest;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.view.View;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.Toast;
import java.util.regex.*;
import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.SmsManager;

import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;

import java.util.regex.*;
import android.util.Log;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.widget.Toast;


public class TestActivity extends Activity
{
  protected static final String TAG = "TestActivity";
  protected static final int REQUEST_CONTACT = 1;
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.test_layout);
  }
  
  @Override
  public void onStart()
  {
    super.onStart();
    Log.d(TAG, "onStart()");
  }
  
  public void startService1(View v)
  {
    Intent intent = new Intent(this, TestService.class);
    intent.putExtra(TestService.COMMAND, 1);
    startService(intent);
  }
  
  public void startService2(View v)
  {
    Intent intent = new Intent(this, TestService.class);
    intent.putExtra(TestService.COMMAND, 2);
    startService(intent);
  }
  
  public void stopMyService(View v)
  {
    Intent intent = new Intent(this, TestService.class);
    intent.putExtra(TestService.COMMAND, 0);
    startService(intent);
  }
  
  public void startService3(View v)
  {
    Intent intent = new Intent(this, TestService.class);
    intent.putExtra(TestService.COMMAND, 3);
    startService(intent);
  }
  
  public void startService4(View v)
  {
    Intent intent = new Intent(this, TestService.class);
    intent.putExtra(TestService.COMMAND, 4);
    startService(intent);
  }
  
  public void noCmdAction(View v)
  {
    Intent intent = new Intent(this, TestService.class);
    startService(intent);
  }
  
  public void pickContacts(View v)
  {
    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
    startActivityForResult(intent, REQUEST_CONTACT);
  }
  
  @Override
  public void onActivityResult (int request, int result, Intent intent)
  {
    if (request == REQUEST_CONTACT)
    {
      if (result == Activity.RESULT_OK)
      {
        Log.d(TAG, "Picking contact number went fine...");
        Uri uri = intent.getData();
        Log.d(TAG, "Uri: " + uri.toString());
        Cursor cursor =  managedQuery(uri, null, null, null, null);
        if (cursor.moveToFirst())
        {
          int idx_number, idx_name;
          try
          {
            idx_number = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
            idx_name = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
          }
          catch(IllegalArgumentException ex)
          {
            Log.d(TAG, "Couldn't retrieve name and number...", ex);
            return;
          }
          
          String number = cursor.getString(idx_number);
          String name = cursor.getString(idx_name);
          Toast.makeText(this, name + ": " + number, Toast.LENGTH_LONG).show();
        }
        else
        {
          Log.d(TAG, "No number...");
        }
      }
      else
      {
        Log.d(TAG, "Picking contact numberfailed...");
      }
    }
  }
  
  @Override
  public void onStop()
  {
    Log.d(TAG, "onStop()");
    super.onStop();
  }
}
