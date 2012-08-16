package org.booncode.android.loco;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
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
import android.util.Log;

public class MainActivity extends Activity
{
  protected static final String TAG = "loco.MainActivity";
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.main_layout);
  }
  
  public void onLocateBuddy(View v)
  {
    Intent intent = new Intent(this, LocateBuddyListActivity.class);
    startActivity(intent);
  }
  
  public void onQuit(View v)
  {
    this.finish();
    Toast.makeText(this, "Bye!", Toast.LENGTH_LONG).show();
  }
  
  public void onShowSettings(View v)
  {
    Intent intent = new Intent(this, SettingsActivity.class);
    startActivity(intent);
  }
  
  public void onManageBuddyList(View v)
  {
    Intent intent = new Intent(this, BuddyListActivity.class);
    startActivity(intent);
  }
  
  public void onTestSim(View v)
  {
    Intent sim_check_intent = new Intent(this, SimCheckingService.class);
    startService(sim_check_intent);
  }
  
}
