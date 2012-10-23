/* *******************************************************************************
 * LOCO - Localizes the position of you mobile.
 * Copyright (C) 2012  Manuel Huber
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * *******************************************************************************/
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


/*! \brief This service class is used to check current SIM card.
 * 
 *  This class is used to check Sim card (if sim-protection is enabled)
 * */
public class SimCheckingService extends Service
{
  //! TAG used to identify debug messages from this service.
  protected static final String TAG = "loco.SimCheckingService";
  //! If Sim Card is not available, service tries again after this time (ms).
  protected static final long WAKE_UP_TIMEOUT = 2 * 60 * 1000;
  //! Message that will be sent if Sim Card has been changed.
  protected static final String STOLEN_MESSAGE = "My phone (nr: %s) got stolen or I forgot about my anti-theft software (new-nr: %s)! Please contact me in person!";
  //! Short version of #STOLEN_MESSAGE.
  protected static final String STOLEN_MESSAGE_SHORT = "Phone (nr: %s) got maybe stolen! New number: %s; Please contact me in person!";
  //! Maximum number of characters of an sms.
  protected static final int MAX_SMS_SIZE = 160;
  
  protected AlarmManager          m_alarm_man;
  //! Database of a list of persons that will be notified if SIM card has been changed.
  protected StalkerDatabase       m_db;
  //! Object to check if this service is enabled.
  protected ApplicationSettings   m_settings;
  //! Wake-lock used to keep cpu running.
  protected PowerManager.WakeLock m_lock;
  //! Intent used to wake this service (if Sim card hasn't been available).
  protected PendingIntent         m_wake_intent;
  //! Line1 number that had been saved to #m_settings.
  protected String                m_number;
  
  
  /*! \brief Callback method (Service), called when this service is
   *         created.
   * 
   *  This method sets up a reference to AlarmManager and creates 
   *  instances to access database settings...
   * */
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
  
  /*! \brief Callback method (Service) used to bind this service to
   *         some other component.
   * 
   *  \param intent The intent of this binding.
   *  \return Returns \c null (binding disabled).
   * */
  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }
  
  /*! \brief Helper method to release the wake-lock of this service.
   * 
   *  The wake-lock (#m_lock) is used to prevent the cpu from going into
   *  idle state. After this method has been called, cpu is allowed to
   *  go into idle state again.
   * 
   *  \see acquireLock to enable wake-lock.
   * */
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
  
  /*! \brief Helper method to acquire the wake-lock of this service.
   * 
   *  The wake-lock (#m_lock) is used to prevent the cpu from going into
   *  idle state. After this method has been called, cpu is not allowed
   *  to go into idle state.
   * 
   *  \see releaseLock to release wake-lock.
   * */
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
  
  /*! \brief Helper method that sets up an alarm to wake the device.
   * 
   *  Wakes the device in #WAKE_UP_TIMEOUT ms.
   * */
  private void setNextAlarm()
  {
    long next_time = SystemClock.elapsedRealtime() + WAKE_UP_TIMEOUT;
    Log.d(TAG, "Setting up next alarm");
    m_alarm_man.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, next_time, m_wake_intent);
  }
  
  /*! \brief Helper method that sends a message to one buddy.
   * 
   *  First tries to send long message #STOLEN_MESSAGE. If this message
   *  exceeds the maximum sms size (#MAX_SMS_SIZE) a short version will
   *  be sent (#STOLEN_MESSAGE_SHORT). If this message also exceeds the
   *  maximum size, no message will be sent and the error will be 
   *  reported to \c Log.
   * */
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
  
  /*! \brief This method tries to check the sim card (line1 number).
   * 
   *  If the Sim-card is not ready yet, an alarm is set up to wake
   *  the device after a timeout of #WAKE_UP_TIMEOUT ms.
   * 
   *  \see notifyBuddies Will be called if Sim card has got a different
   *       line1 number as specified in #m_settings.
   * */
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
  
  /*! \brief Helper method that retrieves current settings and if
   *         Sim-protection is enabled, checks current Sim card.
   * 
   *  \see checkSimCard This method tries to check the sim card.
   * */
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
  
  /*! \brief Callback method (Service), entry point of this Service.
   * 
   *  This method gets called if this service has been started by
   *  an intent.
   *  \param intent The intent that started this service.
   *  \param flags Additional flags...
   *  \param startId Some id.
   *  \return A flag that determines whether the service should be
   *          restarted after it got killed by the os. \c START_STICKEY
   *          means, that this service will be restarted if the vm
   *          got killed (while the service is active...).
   * 
   *  \see process This method checks settings and decides whether the 
   *       service should be stopped, or kept running.
   * */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    acquireLock();
    Log.d(TAG, "Started SimCheckingService...");
    
    process();
    
    releaseLock();
    
    return START_STICKY;
  }
  
  /*! \brief Callback method (Service), called if service is about to 
   *         stop.
   * 
   *  \note Note that this method doesn't have to be called. It's possible 
   *        that the os kills this vm without calling this method.
   * */
  @Override
  public void onDestroy()
  {
    releaseLock();
    m_db.close();
    Log.d(TAG, "onDestroy()");
    super.onDestroy();
  }
  
}
