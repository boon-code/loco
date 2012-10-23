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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.ArrayList;


/*! \brief This class manages all (persistent) settings from this
 *         application.
 * 
 *  To create this object, a context of this application is necessary 
 *  to retrieve the application context, that is used to get an isntance
 *  of the default SharedPreferences. This instance is then used to 
 *  save and load simple data elements as strings and integers...
 * 
 *  \note Note that some keys of this class (#KEY_SIM_PROTECT,
 *        #KEY_THEFT_MODE) shouldn't be changed because they are related
 *        to res/xml/preferences.xml used by \ref SettingsActivity.
 * */
public class ApplicationSettings
{
  //! KEY of the current Notification ID
  public static final String KEY_NOTIFY_ID = "notifyid";
  //! KEY of the line-1 number
  public static final String KEY_LINE1_NUMBER = "line1nr";
  //! KEY of sim-protection (enabled, disabled)
  public static final String KEY_SIM_PROTECT = "simpro";
  //! KEY of theft-mode (enabled, disabled)
  public static final String KEY_THEFT_MODE = "theftmode";
  //! KEY of last line-1 number that was different to the line-1 number...
  public static final String KEY_THEFT_NUMBER = "theftnumber";
  
  //! Instance of SharedPreferences that is used to store and load settings.
  protected SharedPreferences m_settings;
  
  /*! \brief Creates an instance of this class
   * 
   *  \param context Some context of this application used to retrieve 
   *         the application-context which is used to retrieve the 
   *         default SharedPreferences instance (#m_settings).
   * */
  public ApplicationSettings(Context context)
  {
    Context app_context = context.getApplicationContext();
    this.m_settings = PreferenceManager.getDefaultSharedPreferences(app_context);
  }
  
  /*! \brief This method can be used to get notified if some preference
   *         changed.
   * 
   *  \param listener Object that implements the \c OnSharedPreferenceChangeListener
   *         interface an will get notifications if values have changed.
   * */
  public void registerChangeListener(OnSharedPreferenceChangeListener listener)
  {
    m_settings.registerOnSharedPreferenceChangeListener(listener);
  }
  
  /*! \brief Unregisteres objects that don't want to be notified if 
   *         values are changed.
   * 
   *  \param listener The object that wants to not be notified.
   * */
  public void unregisterChangeListener(OnSharedPreferenceChangeListener listener)
  {
    m_settings.unregisterOnSharedPreferenceChangeListener(listener);
  }
  
  /*! \brief Retrieves the last notification-ID
   * 
   *  \return Returns the last notification-ID.
   * */
  public int getNotifyID()
  {
    return m_settings.getInt(KEY_NOTIFY_ID, 0);
  }
  
  /*! \brief Sets the current notification-ID.
   * 
   *  \param id The new ID.
   * */
  public void setNotifyID(int id)
  {
    SharedPreferences.Editor editor = m_settings.edit();
    editor.putInt(KEY_NOTIFY_ID, id);
    editor.commit();
  }
  
  /*! \brief Stores the line-1 number.
   * 
   *  \param number New line-1 number to store.
   * */
  public void setLine1Number(String number)
  {
    SharedPreferences.Editor editor = m_settings.edit();
    editor.putString(KEY_LINE1_NUMBER, number);
    editor.commit();
  }
  
  /*! \brief Checks if line-1 number has been set.
   * 
   *  \return \c true if line-1 number has been set, else \c false.
   * */
  public boolean isLine1NumberSet()
  {
    return (getLine1Number() != null);
  }
  
  /*! \brief Loads line-1 number.
   * 
   *  \return Returns line-1 number or null if not set.
   * */
  public String getLine1Number()
  {
    return m_settings.getString(KEY_LINE1_NUMBER, null);
  }
  
  /*! \brief Enables or disables sim-protection.
   * 
   *  \param enabled If \c true, sim-protection will be enabled, else 
   *         \c false.
   * */
  public void setSimProtect(boolean enabled)
  {
    SharedPreferences.Editor editor = m_settings.edit();
    editor.putBoolean(KEY_SIM_PROTECT, enabled);
    editor.commit();
  }
  
  /*! \brief Checks whether sim-protection is enabled.
   * 
   *  \return \c true if sim-protection is enabled, else \c false.
   * */
  public boolean isSimProtectEnabled()
  {
    return m_settings.getBoolean(KEY_SIM_PROTECT, false);
  }
  
  /*! \brief Enables or disables theft-mode.
   * 
   *  \param enabled if \c true, theft-mode will be enabled, else disabled.
   * */
  public void setTheftMode(boolean enabled)
  {
    SharedPreferences.Editor editor = m_settings.edit();
    editor.putBoolean(KEY_THEFT_MODE, enabled);
    editor.commit();
  }
  
  /*! \brief Checks whether device is in theft-mode.
   * 
   *  \return Returns \c true if in theft-mode (enabled) else \c false.
   * */
  public boolean inTheftMode()
  {
    return m_settings.getBoolean(KEY_THEFT_MODE, false);
  }
  
  /*! \brief Stores the line-1 number of the replaced sim-card.
   * 
   *  \param number the line-1 number of the thief...
   * */
  public void setTheftNumber(String number)
  {
    SharedPreferences.Editor editor = m_settings.edit();
    editor.putString(KEY_THEFT_NUMBER, number);
    editor.commit();
  }
  
  /*! \brief Loads the last line-1 number that is not equal to the line-1
   *         number (#getLine1Number).
   * 
   *  \return Returns the last line-1 number of the thief, or null if not
   *          set.
   * */
  public String getTheftNumber()
  {
    return m_settings.getString(KEY_THEFT_NUMBER, null);
  }
}


