package org.booncode.android.loco;

import android.app.PendingIntent;
import android.app.Service;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.lang.*;

public class SimCheckingService extends Service
{
  protected static final String TAG = "loco.SimCheckingService";
  protected static final long WAKE_UP_TIMEOUT = 2 * 60 * 1000;
  
  protected static final String STOLEN_MESSAGE = "My phone (nr: %s) got stolen or I forgot about my anti-theft software (new-nr: %s)! Please contact me in person!";
  protected static final String STOLEN_MESSAGE_SHORT = "Phone (nr: %s) got maybe stolen! New number: %s; Please contact me in person!";
  protected static final int MAX_SMS_SIZE = 160;
  
  protected AlarmManager          m_alarm_man;
  protected StalkerDatabase       m_db;
  protected ApplicationSettings   m_settings;
  protected PowerManager.WakeLock m_lock;
  protected PendingIntent         m_wake_intent;
  protected String                m_number;
  
  @Override
  public void onCreate()
  {
    m_alarm_man = (AlarmManager)getSystemService(ALARM_SERVICE);
    m_db = new StalkerDatabase(getApplicationContext());
    
    PowerManager power_man = (PowerManager)getSystemService(POWER_SERVICE);
    m_lock = power_man.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    
    Intent intent = new Intent(this, SimCheckingService.class);
    m_wake_intent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    
    m_settings = new ApplicationSettings(this);
  }
  
  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }
  
  protected void releaseLock()
  {
    if (m_lock.isHeld())
    {
      Log.d(TAG, "Release WakeLock...");
      m_lock.release();
    }
    else
    {
      Log.d(TAG, "Request to release WakeLock (has not been acquired)...");
    }
  }
  
  protected void acquireLock()
  {
    if (!m_lock.isHeld())
    {
      Log.d(TAG, "Aquire WakeLock...");
      m_lock.acquire();
    }
    else
    {
      Log.d(TAG, "Request to acquire WakeLock (but has already been done) ...");
    }
  }
  
  private void setNextAlarm()
  {
    long next_time = SystemClock.elapsedRealtime() + WAKE_UP_TIMEOUT;
    Log.d(TAG, "Setting up next alarm");
    m_alarm_man.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, next_time, m_wake_intent);
  }
  
  private void notifyBuddies(String newnumber)
  {
    String message = String.format(STOLEN_MESSAGE, m_number, newnumber);
    if (message.length() > MAX_SMS_SIZE)
    {
      message = String.format(STOLEN_MESSAGE_SHORT, m_number, newnumber);
      if (message.length() > MAX_SMS_SIZE)
      {
        Log.e(TAG, "notifyBuddies: Can't format stolen-message, number too long");
        return;
      }
    }
    
    Cursor cursor = m_db.queryAllPersons();
    
    while(cursor.moveToNext())
    {
      StalkerDatabase.Person person = StalkerDatabase.toPerson(cursor);
      Log.w(TAG, String.format("Send SMS to %s (%s): My phone got stolen -> new number: %s, original: %s", person.name, person.number, newnumber, m_number));
      
      Utils.sendSMS(person.number, message);
    }
    cursor.close();
  }
  
  private void checkSimCard()
  {
    String current_number = Utils.getLine1Number(this);
    
    if (current_number != null)
    {
      String theft_number = m_settings.getTheftNumber();
      if (current_number.equals(m_number))
      {
        Log.d(TAG, "SimProtect: All fine =)");
        stopSelf();
      }
      else if(m_settings.inTheftMode() && (theft_number != null))
      {
        if (current_number.equals(theft_number))
        {
          Log.d(TAG, "SimProtect: Still: old number, bad person =(");
          // TODO start Stalker...
          stopSelf();
        }
        else
        {
          m_settings.setTheftNumber(current_number);
          notifyBuddies(current_number);
          Log.d(TAG, "SimProtect: You are a bad person =( =(");
          stopSelf();
        }
      }
      else
      {
        m_settings.setTheftMode(true);
        m_settings.setTheftNumber(current_number);
        notifyBuddies(current_number);
        // TODO: start Stalker...
        Log.d(TAG, "SimProtect: You are a bad person =(");
        stopSelf();
      }
    }
    else
    {
      setNextAlarm();
    }
  }
  
  protected void process()
  {
    m_number = m_settings.getLine1Number();
    
    if (m_settings.isSimProtectEnabled())
    {
      if (m_number != null)
      {
        checkSimCard();
      }
      else
      {
        Log.e(TAG, "SimProtect: No valid number has been set");
        stopSelf();
      }
    }
    else
    {
      Log.d(TAG, "SimProtect disabled...");
      stopSelf();
    }
  }
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    acquireLock();
    Log.d(TAG, "Started SimCheckingService...");
    
    process();
    
    releaseLock();
    
    return START_STICKY;
  }
  
  @Override
  public void onDestroy()
  {
    releaseLock();
    m_db.close();
    Log.d(TAG, "onDestroy()");
    super.onDestroy();
  }
  
}
