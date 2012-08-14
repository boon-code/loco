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

public class MainActivity extends Activity
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.main_layout);
  }
  
  /*
  protected void sendSMS(String number, String text)
  {
    Intent intent = new Intent(this, SettingsActivity.class);
    PendingIntent pending_intent = PendingIntent.getActivity(this, 0, intent, 0);
    SmsManager man = SmsManager.getDefault();
    man.sendTextMessage(number, null, text, pending_intent, null);
  }
  * */
  
  public void locateCMD(View v)
  {
    Intent intent = new Intent(this, LocateBuddyListActivity.class);
    startActivity(intent);
  }
  
  public void quitApp(View v)
  {
    this.finish();
    Toast.makeText(this, "Bye!", Toast.LENGTH_LONG).show();
  }
  
  public void manageAuth(View v)
  {
    Intent intent = new Intent(this, BuddyListActivity.class);
    startActivity(intent);
  }
  
  public void test(View v)
  {
    Intent intent = new Intent(this, Stalker.class);
    intent.putExtra(Stalker.EXTRA_KEY_CMD, Stalker.CMD_TEST);
    startService(intent);
  }
}
