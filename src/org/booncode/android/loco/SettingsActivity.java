package org.booncode.android.loco;

import android.app.Activity;
import android.os.Bundle;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.content.SharedPreferences;

import android.widget.Toast;

public class SettingsActivity extends Activity
{
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.settings_layout);
  }
  
  
  public void pressedOk(View v)
  {
    Context context = getApplicationContext();
    int duration = Toast.LENGTH_LONG;
    Toast toast = Toast.makeText(context, "Button pressed!", duration);
    toast.show();
    SharedPreferences settings = this.getSharedPreferences(Stalker.SETTINGS_FILE, Context.MODE_PRIVATE);
    String text = settings.getString(Stalker.AUTH_LIST, "");
    Toast.makeText(this, text, duration).show();
  }
  
  
  public void addNumber(View v)
  {
    SharedPreferences settings = this.getSharedPreferences(Stalker.SETTINGS_FILE, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(Stalker.AUTH_LIST, "123456;987654321");
    editor.commit();
  }
  
  
}
