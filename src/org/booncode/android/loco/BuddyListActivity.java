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
import android.provider.ContactsContract;

import android.util.Log;
import android.widget.Toast;


public class BuddyListActivity extends ListActivity
{
  protected static final String TAG = "loco.BuddyListActivity";
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
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.buddy_list_menu, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.bla_btn_add:
          Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
          startActivityForResult(intent, REQUEST_CONTACT);
        return true;
      
      case R.id.bla_btn_back:
        this.finish(); 
        return true;
      
      default:
        return super.onOptionsItemSelected(item);
    }
  }
  
  @Override
  public void onActivityResult (int request, int result, Intent intent)
  {
    if (request == REQUEST_CONTACT)
    {
      if (result == Activity.RESULT_OK)
      {
        Log.d(TAG, "Picking contact number went fine...");
        Uri uri = intent.getData();
        Log.d(TAG, "Uri: " + uri.toString());
        Cursor cursor =  managedQuery(uri, null, null, null, null);
        if (cursor.moveToFirst())
        {
          int idx_number, idx_name;
          try
          {
            idx_number = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
            idx_name = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
          }
          catch(IllegalArgumentException ex)
          {
            Log.d(TAG, "Couldn't retrieve name and number...", ex);
            return;
          }
          
          String number = TelephonyUtils.formatTelephoneNumber(cursor.getString(idx_number));
          String name = cursor.getString(idx_name);
          
          Toast.makeText(this, name + ": " + number, Toast.LENGTH_LONG).show();
          
          StalkerDatabase db = new StalkerDatabase(getApplicationContext());
          boolean ret = db.addPerson(number, name, true);
          
          if (!ret)
          {
            Toast.makeText(this, "Couldn't add person...", Toast.LENGTH_LONG).show();
          }
          
          db.close();
        }
        else
        {
          Log.d(TAG, "No number...");
        }
      }
      else
      {
        Log.d(TAG, "Picking contact number failed...");
      }
    }
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
      Intent intent = new Intent(this, BuddyActivity.class);
      intent.putExtra(BuddyActivity.SHOW_NUMBER, person.number);
      startActivity(intent);
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
