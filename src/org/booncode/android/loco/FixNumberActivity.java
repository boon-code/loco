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

public class FixNumberActivity extends Activity
{
  public static final String FIX_NUMBER = "number";
  public static final String FIX_NAME = "name";
  
  protected static final String TAG = "loco.FixNumberActivity";
  
  protected StalkerDatabase m_db;
  
  protected TextView m_txt_name;
  protected EditText m_txt_number;
  
  protected String m_name;
  protected String m_number;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.fix_layout);
    
    m_txt_name = (TextView)findViewById(R.id.fix_txt_name);
    m_txt_number = (EditText)findViewById(R.id.fix_txt_number);
    
    m_db = new StalkerDatabase(getApplicationContext());
  }
  
  protected boolean extractData()
  {
    Bundle bundle = getIntent().getExtras();
    if (bundle != null)
    {
      m_number = bundle.getString(FIX_NUMBER);
      m_name = bundle.getString(FIX_NAME);
      
      if ((m_name != null) && (m_number != null))
      {
        m_txt_name.setText(m_name);
        m_txt_number.setText(m_number);
        return true;
      }
    }
    
    Toast.makeText(this, "Illegal intent started this Activity...", Toast.LENGTH_LONG).show();
    return false;
  }
  
  public void onCancel(View v)
  {
    finish();
  }
  
  public void onSave(View v)
  {
    String number = m_txt_number.getText().toString();
    if (!number.startsWith("+"))
    {
      Toast.makeText(this, "Wrong format...", Toast.LENGTH_LONG).show();
    }
    else if(PhoneNumberUtils.compare(number, m_number))
    {
      boolean ret = m_db.addPerson(number, m_name, true);
      if (!ret)
      {
        Toast.makeText(this, "Couldn't add person...", Toast.LENGTH_LONG).show();
      }
      else
      {
        Toast.makeText(this, "Added " + m_name, Toast.LENGTH_LONG).show();
      }
      
      finish();
    }
    else
    {
      Toast.makeText(this, "Number has been altered", Toast.LENGTH_LONG).show();
    }
  }
  
  @Override
  public void onStart()
  {
    super.onStart();
    if (!extractData())
    {
      this.finish();
    }
  }
  
  @Override
  public void onStop()
  {
    super.onStop();
  }
  
  @Override
  public void onDestroy()
  {
    m_db.close();
    super.onDestroy();
  }
  
}
