package org.booncode.android.locotest;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog;
import android.app.AlarmManager;
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

import android.os.PowerManager;

import java.util.regex.*;
import android.util.Log;
import java.util.List;

import android.os.SystemClock;


public class TestService extends Service implements LocationListener
{
  public static final String COMMAND = "cmd";
  
  protected static final String TAG = "TestService";
  
  protected PendingIntent m_wake_intent;
  protected LocationManager m_manager;
  protected PowerManager.WakeLock m_lock;
  protected AlarmManager m_alarm_man;
  protected boolean m_updates_enabled = false;
  protected boolean m_wakeup_on = false;
  
  @Override
  public void onLocationChanged(Location loc)
  {
    String latitude = Double.toString(loc.getLatitude());
    String longitude = Double.toString(loc.getLongitude());
    
    String accuracy = "(None)";
    
    if (loc.hasAccuracy())
    {
      float acc = loc.getAccuracy();
      accuracy = "(" + acc + ")";
    }
    
    String geo = String.format("geo: %s, %s, %s", latitude, longitude, accuracy);
    Log.d(TAG, geo);
  }
    
  @Override
  public void onProviderDisabled(String provider)
  {
    Log.d(TAG, "onProviderDisabled(" + provider + ")");
  }
  
  @Override
  public void onProviderEnabled(String provider)
  {
    Log.d(TAG, "onProviderEnabled(" + provider + ")");
  }
  
  @Override
  public void onStatusChanged(String provider, int status, Bundle extras)
  {
    Log.d(TAG, "onStatusChanged(" + provider + ", " + status + ")");
  }
  
  
  @Override
  public void onCreate()
  {
    Log.d(TAG, "onCreate()");
    
    Intent intent = new Intent(this, TestService.class);
    intent.putExtra(TestService.COMMAND, 5);
    m_wake_intent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    
    m_alarm_man = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    
    PowerManager power_man = (PowerManager)getSystemService(Context.POWER_SERVICE);
    m_lock = power_man.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    this.m_manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
  }
  
  protected void releaseLock()
  {
    Log.d(TAG, "Request to release WakeLock...");
    if (m_lock.isHeld())
    {
      Log.d(TAG, "Releasing WakeLock...");
      m_lock.release();
    }
    else
    {
      Log.d(TAG, "WakeLock has not been acquired...");
    }
  }
  
  protected void acquireLock()
  {
    Log.d(TAG, "Request to acquire WakeLock...");
    if (!m_lock.isHeld())
    {
      Log.d(TAG, "Acquire WakeLock...");
      m_lock.acquire();
    }
    else
    {
      Log.d(TAG, "WakeLock has already been acquired...");
    }
  }
  
  protected void testLocating()
  {
    List<String> list = m_manager.getProviders(true);
    
    for (String name : list)
    {
      Log.d(TAG, "Found LocationProvider '" + name + "'");
      
    }
  }
  
  protected void startWakeUp()
  {
    if (!m_wakeup_on)
    {
      int period = 20000; // 20 seconds
      Log.d(TAG, "Turn on Alarm...");
      m_alarm_man.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
          SystemClock.elapsedRealtime() + period, period, m_wake_intent);
      
      m_wakeup_on = true;
    }
    else
    {
      Log.d(TAG, "Alarm already turned on...");
    }
  }
  
  protected void stopWakeUp()
  {
    if (m_wakeup_on)
    {
      Log.d(TAG, "Disabling Alarm...");
      m_alarm_man.cancel(m_wake_intent);
      m_wakeup_on = false;
    }
    else
    {
      Log.d(TAG, "Alarm has already been set up...");
    }
  }
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    Log.d(TAG, "onStart()");
    
    acquireLock();
    
    int cmd = intent.getIntExtra(COMMAND, -1);
    
    if (cmd == -1)
    {
      Log.d(TAG, "No command has been specified...");
    }
    else
    {
      Log.d(TAG, "Doing command " + cmd + "...");
    }
    
    switch(cmd)
    {
      case -1:
        break;
      
      case 1:
        
        startWakeUp();
        
        if (!m_updates_enabled)
        {
          Log.d(TAG, "Start locating service...");
          testLocating();
          this.m_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0.0f, this);
          this.m_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, this);
          m_updates_enabled = true;
        }
        else
        {
          Log.d(TAG, "Locating already activated...");
        }
        break;
      
      case 2:
        if (m_updates_enabled)
        {
          Log.d(TAG, "Stop locating...");
          m_manager.removeUpdates(this);
          releaseLock();
          m_updates_enabled = false;
        }
        else
        {
          Log.d(TAG, "Locating already stopped...");
        }
        break;
      
      case 3:
        Log.d(TAG, "Cancel Wakeup...");
        stopWakeUp();
        break;
      
      case 4:
        break;
      
      case 5:
        Log.d(TAG, "Wakeup...");
        break;
      
      case 0:
        Log.d(TAG, "Stop Service...");
        this.stopSelf();
        break;
      
      default:
        Log.d(TAG, "Unknown command " + cmd);
    }
    
    releaseLock();
    
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }
  
  @Override
  public void onDestroy()
  {
    Log.d(TAG, "onDestroy()");
    if (m_updates_enabled)
    {
      m_manager.removeUpdates(this);
      Log.d(TAG, "Stop locating at onDestroy()...");
    }
  }
}
