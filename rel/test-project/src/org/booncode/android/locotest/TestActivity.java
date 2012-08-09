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


public class TestActivity extends Activity
{
  protected static final String TAG = "TestActivity";
  
  
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
  
  @Override
  public void onStop()
  {
    Log.d(TAG, "onStop()");
    super.onStop();
  }
}
