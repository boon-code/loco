package org.booncode.android.loco;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import org.json.*;


public class StalkerSettings
{
  public static final String SETTINGS_FILE = "settings";
  public static final String STALKER_DATA = "stalker";
  
  protected SharedPreferences m_settings;
  
  public ArrayList<DataSet> sharedList;
  
  public class DataSet
  {
    public static final String NAME;
    
    public String name;
    
    public void fromJSON(JSONObject obj)
    {
      
    }
    
    public JSONObject toJSON()
    {
      return new JSONObject();
    }
  }
  
  
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
    String json = this.m_settings.getString(STALKER_DATA, "");
    JSONArray ar = 
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
