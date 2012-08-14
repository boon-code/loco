package org.booncode.android.loco;

import android.app.Activity;
import android.app.ListActivity;
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
import android.widget.SimpleCursorAdapter;

import android.util.Log;
import android.widget.Toast;


public class LocateBuddyListActivity extends ListActivity
{
  protected static final String TAG = "loco.LocateBuddyListActivity";
  protected static final int REQUEST_CONTACT = 1;
  
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
      
      Log.d(TAG, "SEND SMS to " + person.number);
      this.finish();
    }
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
    Cursor c = m_db.queryAllPersons();
    m_adapter.changeCursor(c);
    releaseList();
    m_cursor = c;
    Log.d(TAG, "Create cursor");
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
