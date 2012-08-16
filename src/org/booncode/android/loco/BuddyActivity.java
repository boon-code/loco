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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;

import android.util.Log;
import android.widget.Toast;

public class BuddyActivity extends Activity
{
  public static final String SHOW_NUMBER = "number";
  
  protected static final String TAG = "loco.BuddyActivity";
  
  protected StalkerDatabase m_db;
  protected String m_number;
  
  protected TextView m_txt_name;
  protected TextView m_txt_number;
  protected TextView m_txt_smscount;
  protected CheckBox m_chk_auth;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.buddy_layout);
    
    m_txt_name = (TextView)findViewById(R.id.buddy_txt_name);
    m_txt_number = (TextView)findViewById(R.id.buddy_txt_number);
    m_txt_smscount = (TextView)findViewById(R.id.buddy_txt_smscount);
    m_chk_auth = (CheckBox)findViewById(R.id.buddy_chk_auth);
    
    m_db = new StalkerDatabase(getApplicationContext());
  }
  
  protected boolean extractData()
  {
    Bundle bundle = getIntent().getExtras();
    if (bundle != null)
    {
      m_number = bundle.getString(SHOW_NUMBER);
      
      if (m_number != null)
      {
        StalkerDatabase.Person person = m_db.getPersonFromNumber(m_number);
        if (person != null)
        {
          m_txt_name.setText(person.name);
          m_txt_number.setText(person.number);
          m_txt_smscount.setText(String.valueOf(person.smscount));
          m_chk_auth.setChecked(person.authorised);
          return true;
        }
        else
        {
          Log.w(TAG, String.format("Couldn't find person with number %s", m_number));
        }
      }
      else
      {
        Log.w(TAG, "Intent has no extra-key " + SHOW_NUMBER);
      }
    }
    
    return false;
  }
  
  public void onAuthorisationChanged(View v)
  {
    if (v.getId() == R.id.buddy_chk_auth)
    {
      CheckBox cbox = (CheckBox)v;
      StalkerDatabase.Person person = new StalkerDatabase.Person();
      person.number = m_number;
      person.authorised = cbox.isChecked();
      m_db.updatePerson(person, false, true, false);
    }
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.buddy_menu, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.buddy_mnu_back:
        this.finish();    
        return true;
      
      case R.id.buddy_mnu_delete:
        m_db.deletePerson(m_number);
        this.finish();
        return true;
      
      case R.id.buddy_mnu_locate:
        Utils.sendLocateSMS(m_number);
        Toast.makeText(this, "Locating...", Toast.LENGTH_LONG).show();
        return true;
      
      default:
        return super.onOptionsItemSelected(item);
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
