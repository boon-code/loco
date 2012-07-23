package org.booncode.android.loco;

import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.os.Bundle;
import android.os.IBinder;
import android.content.SharedPreferences;

import android.widget.Toast;

public class Stalker extends Service
{
  public static final String SETTINGS_FILE = "settings";
  public static final String AUTH_LIST = "auth_list";
  
  
  
  protected boolean isNumberAuthorised(String number)
  {
    SharedPreferences settings = this.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
    String auth_list[] = settings.getString(AUTH_LIST, "").split(";");
    for (String nr : auth_list)
    {
      if (number.equals(nr))
      {
        return true;
      }
    }
    return false;
  }
  
  
  protected void doCommand(String cmd, String addr)
  {
    Toast.makeText(this, "CMD: " + cmd, Toast.LENGTH_LONG).show();
    Toast.makeText(this, "ADDR: " + addr, Toast.LENGTH_LONG).show();
  }
  
  
  @Override
	public IBinder onBind(Intent intent)
  {
		return null;
	}
  
  
  @Override
  public void onStart(Intent intent, int startId)
  {
    Bundle bundle = intent.getExtras();
    
    if (bundle != null)
    {
      String cmd = bundle.getString("cmd");
      String addr = bundle.getString("phone-number");
      if (this.isNumberAuthorised(addr))
      {
        this.doCommand(cmd, addr);
      }
      else
      {
        Toast.makeText(this, "Unauthorised CMD:" + cmd, Toast.LENGTH_LONG).show();
      }
    }
    
    this.stopSelf();
  }
}
