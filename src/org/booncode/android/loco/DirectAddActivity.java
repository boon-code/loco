package org.booncode.android.loco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.net.Uri;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.telephony.PhoneNumberUtils;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.EditText;
import android.widget.Toast;

import android.util.Log;
import android.widget.Toast;

public class DirectAddActivity extends Activity
{
  protected static final String TAG = "loco.DirectAddActivity";
  
  protected StalkerDatabase m_db;
  
  protected EditText m_txt_name;
  protected EditText m_txt_number;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.direct_layout);
    
    m_txt_name = (EditText)findViewById(R.id.direct_txt_name);
    m_txt_number = (EditText)findViewById(R.id.direct_txt_number);
    
    m_db = new StalkerDatabase(getApplicationContext());
  }
  
  public void onCancel(View v)
  {
    finish();
  }
  
  private static boolean isNullOrEmpty(String value)
  {
    if (value != null)
    {
      return ("".equals(value));
    }
    else
    {
      return true;
    }
  }
  
  public void onSave(View v)
  {
    String number = m_txt_number.getText().toString();
    String name = m_txt_name.getText().toString();
    
    if (isNullOrEmpty(number) || isNullOrEmpty(name))
    {
      Toast.makeText(this, "You have to fill in all information", Toast.LENGTH_LONG).show();
    }
    else
    {
      boolean ret = m_db.addPerson(number, name, true);
      if (!ret)
      {
        Toast.makeText(this, "Couldn't add person...", Toast.LENGTH_LONG).show();
      }
      else
      {
        Toast.makeText(this, "Added " + name, Toast.LENGTH_LONG).show();
      }
      
      finish();
    }
  }
  
  @Override
  public void onDestroy()
  {
    m_db.close();
    super.onDestroy();
  }
  
}
