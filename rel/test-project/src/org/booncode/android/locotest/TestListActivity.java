package org.booncode.android.locotest;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.view.View;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.Toast;
import java.util.regex.*;
import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.SmsManager;

import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;

import java.util.regex.*;
import android.util.Log;

import android.database.Cursor;
import android.widget.Toast;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ListView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.Phones;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class TestListActivity extends ListActivity
{
  protected static final String TAG = "TestListActivity";
  
  protected SimpleCursorAdapter m_adapter;
  protected StalkerDatabase m_db;
  protected Cursor m_cursor = null;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    
    Log.d(TAG, "onCreate()");
    
    m_db = new StalkerDatabase(getApplicationContext());
    
    m_cursor = m_db.queryAllPersons();
    
    Log.d(TAG, "Create cursor");
    
    m_adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,
        m_cursor, new String[] {StalkerDatabase.Person.NAME, StalkerDatabase.Person.NUMBER},
        new int[] {android.R.id.text1, android.R.id.text2});
    
    setListAdapter(m_adapter);
  }
  
  @Override
  protected void onListItemClick(ListView listview, View view, int position, long id)
  {
    Log.d(TAG, String.format("Selected %d (id=%d)!", position, id));
    
    StalkerDatabase.Person person = m_db.getPersonFromId(id);
    if (person == null)
    {
      Log.w(TAG, String.format("Couldn't retrieve item %d", id));
    }
    else
    {
      String auth;
      
      if (person.authorised)
      {
        auth = "true";
      }
      else
      {
        auth = "false";
      }
      Log.d(TAG, String.format("Clicked on Person name=%s, number=%s, sms=%d, auth=%s",
          person.name, person.number, person.smscount, auth));
    }
    m_db.deletePerson(id);
    reloadList();
    
  }
  
  protected void releaseList()
  {
    Log.d(TAG, "Close cursor");
    if (m_cursor != null)
    {
      m_cursor.close();
      m_cursor = null;
    }
  }
  
  protected void reloadList()
  {
    Log.d(TAG, "Create cursor");
    Cursor c = m_db.queryAllPersons();
    m_adapter.changeCursor(c);
    releaseList();
    m_cursor = c;
  }
  
  @Override
  public void onStop()
  {
    releaseList();
    super.onStop();
  }
  
  @Override
  public void onStart()
  {
    super.onStart();
    reloadList();
  }
  
  @Override
  public void onDestroy()
  {
    m_db.close();
    super.onDestroy();
  }
}
