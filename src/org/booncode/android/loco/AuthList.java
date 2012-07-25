package org.booncode.android.loco;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;


public class AuthList
{
  public static final String SETTINGS_FILE = "settings";
  public static final String AUTH_LIST = "auth_list";
  public static final String SEP = ";";
  
  protected SharedPreferences m_settings;
  
  public ArrayList<String> sharedList;
  
  
  public AuthList(SharedPreferences settings)
  {
    this.m_settings = settings;
    this.sharedList = new ArrayList<String>();
    this.reloadList();
  }
  
  public boolean isEmpty()
  {
    return (this.sharedList.size() == 0);
  }
  
  public String[] toArray(boolean reload)
  {
    if (reload)
    {
      this.reloadList();
    }
    return this.sharedList.toArray(new String[this.sharedList.size()]);
  }
  
  public void writeCurrent()
  {
    StringBuilder builder = new StringBuilder();
    SharedPreferences.Editor editor = m_settings.edit();
    for (String number : sharedList)
    {
      builder.append(number);
      builder.append(";");
    }
    editor.putString(AUTH_LIST, builder.toString());
    editor.commit();
  }
  
  public void reloadList()
  {
    String[] items = this.m_settings.getString(AUTH_LIST, "").split(";");
    sharedList.clear();
    for (String item : items)
    {
      if (!item.equals(""))
      {
        sharedList.add(item);
      }
    }
  }
}
