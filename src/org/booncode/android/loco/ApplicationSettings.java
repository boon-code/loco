package org.booncode.android.loco;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.ArrayList;

public class ApplicationSettings
{
  public static final String KEY_NOTIFY_ID = "notifyid";
  public static final String KEY_LINE1_NUMBER = "line1nr";
  public static final String KEY_SIM_PROTECT = "simpro";
  public static final String KEY_THEFT_MODE = "theftmode";
  public static final String KEY_THEFT_NUMBER = "theftnumber";
  
  protected SharedPreferences m_settings;
  protected Context           m_app_context;
  
  
  public ApplicationSettings(Context context)
  {
    this.m_app_context = context.getApplicationContext();
    this.m_settings = PreferenceManager.getDefaultSharedPreferences(this.m_app_context);
  }
  
  public void registerChangeListener(OnSharedPreferenceChangeListener listener)
  {
    m_settings.registerOnSharedPreferenceChangeListener(listener);
  }
  
  public void unregisterChangeListener(OnSharedPreferenceChangeListener listener)
  {
    m_settings.unregisterOnSharedPreferenceChangeListener(listener);
  }
  
  public int getNotifyID()
  {
    return m_settings.getInt(KEY_NOTIFY_ID, 0);
  }
  
  public void setNotifyID(int id)
  {
    SharedPreferences.Editor editor = m_settings.edit();
    editor.putInt(KEY_NOTIFY_ID, id);
    editor.commit();
  }
  
  public void setLine1Number(String number)
  {
    SharedPreferences.Editor editor = m_settings.edit();
    editor.putString(KEY_LINE1_NUMBER, number);
    editor.commit();
  }
  
  public boolean isLine1NumberSet()
  {
    return (getLine1Number() != null);
  }
  
  public String getLine1Number()
  {
    return m_settings.getString(KEY_LINE1_NUMBER, null);
  }
  
  public void setSimProtect(boolean enabled)
  {
    SharedPreferences.Editor editor = m_settings.edit();
    editor.putBoolean(KEY_SIM_PROTECT, enabled);
    editor.commit();
  }
  
  public boolean isSimProtectEnabled()
  {
    return m_settings.getBoolean(KEY_SIM_PROTECT, false);
  }
  
  public void setTheftMode(boolean enabled)
  {
    SharedPreferences.Editor editor = m_settings.edit();
    editor.putBoolean(KEY_THEFT_MODE, enabled);
    editor.commit();
  }
  
  public boolean inTheftMode()
  {
    return m_settings.getBoolean(KEY_THEFT_MODE, false);
  }
  
  public void setTheftNumber(String number)
  {
    SharedPreferences.Editor editor = m_settings.edit();
    editor.putString(KEY_THEFT_NUMBER, number);
    editor.commit();
  }
  
  public String getTheftNumber()
  {
    return m_settings.getString(KEY_THEFT_NUMBER, null);
  }
}


