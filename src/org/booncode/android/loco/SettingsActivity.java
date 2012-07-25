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

public class SettingsActivity extends Activity implements ServiceConnection
{
  private static final Pattern RE_PHONE_NUMBER = Pattern.compile("^[+]{1}\\d+$");
  
  protected boolean m_bound = false;
  protected Stalker m_stalker = null;
  protected AuthList m_auth;
  
  
  @Override
  public void onServiceConnected(ComponentName className, IBinder service)
  {
    Stalker.PrivateBinder binder = (Stalker.PrivateBinder) service;
    m_stalker = binder.getStalker();
    m_stalker.setCurrentActivity(this);
    m_bound = true;
  }
  
  protected boolean isBound()
  {
    return m_bound;
  }
  
  protected void disconnectEventually()
  {
    if (m_bound)
    {
      //Toast.makeText(this, "Disconnect...", Toast.LENGTH_LONG).show();
      m_stalker.setCurrentActivity(null);
      m_stalker = null;
      m_bound = false;
      unbindService(this);
    }
  }
  
  protected void connectEventually()
  {
    if (!m_bound)
    {
      //Toast.makeText(this, "Connect...", Toast.LENGTH_LONG).show();
      Intent intent = new Intent(this, Stalker.class);
      bindService(intent, this, Context.BIND_AUTO_CREATE);
    }
    else
    {
      //Toast.makeText(this, "Binding has already been done...", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onServiceDisconnected(ComponentName className)
  {
    m_bound = false;
    if (m_stalker != null)
    {
      m_stalker.setCurrentActivity(null);
      m_stalker = null;
    }
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.settings_layout);
    
    SharedPreferences settings = this.getSharedPreferences(AuthList.SETTINGS_FILE, Context.MODE_PRIVATE);
    
    m_auth = new AuthList(settings);
  }
  
  @Override
  protected void onStart()
  {
    super.onStart();
    this.connectEventually();
  }
  
  @Override
  protected void onRestart()
  {
    super.onRestart();
    this.connectEventually();
  }
  
  @Override
  protected void onStop()
  {
    super.onStop();
    this.disconnectEventually();
  }
  
  public void viewMap(String geodata)
  {
    Uri uri = Uri.parse(geodata);
    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
    startActivity(intent);
  }
  
  protected void sendSMS(String number, String text)
  {
    Intent intent = new Intent(this, SettingsActivity.class);
    PendingIntent pending_intent = PendingIntent.getActivity(this, 0, intent, 0);
    SmsManager man = SmsManager.getDefault();
    man.sendTextMessage(number, null, text, pending_intent, null);
  }
  
  public void locateCMD(View v)
  {
    final String auth_list[] = m_auth.toArray(true);
    
    if (m_auth.isEmpty())
    {
      Toast.makeText(SettingsActivity.this, "No Authorised Numbers", Toast.LENGTH_LONG).show();
    }
    else
    {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Pick a user!");
      builder.setItems(auth_list, new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface dialog, int item)
          {
            if (item >= 0 && item < auth_list.length)
            {
              SettingsActivity.this.sendSMS(auth_list[item], "at+loco-locate");
              Toast.makeText(SettingsActivity.this, "Sending SMS", Toast.LENGTH_LONG).show();
            }
          }
        });
      AlertDialog dialog = builder.create();
      dialog.show();
    }
  }
  
  public void addToSettings(String number)
  {
    Matcher m = RE_PHONE_NUMBER.matcher(number);
    if (m.matches())
    {
      m_auth.sharedList.add(number);
      m_auth.writeCurrent();
    }
  }
  
  public void addNumber(View v)
  {
    final EditText input = new EditText(this);
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Enter phone-number");
    builder.setView(input);
    builder.setPositiveButton("Add", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface dialog, int whichButton)
      {
        SettingsActivity.this.addToSettings(input.getText().toString());
      }
    });
    
    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface dialog, int whichButton)
      {}
    });

    builder.show();
  }
  
  public void quitApp(View v)
  {
    this.finish();
    Toast.makeText(this, "Bye!", Toast.LENGTH_LONG).show();
  }
  
  public void manageAuth(View v)
  {
    Intent intent = new Intent(this, AuthorisationActivity.class);
    startActivity(intent);
  }
  
  
}
