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


/*! \brief This Activity shows a list of all contacts and can be used
 *         to manage contacts.
 * 
 *  The menu enables the user to add new contacts. Further operations
 *  are supported if you click on the contact \ref BuddyActivity will
 *  be opened where the user can remove contacts.
 * */
public class BuddyListActivity extends ListActivity
{
  //! TAG to identify log messages from this class.
  protected static final String TAG = "loco.BuddyListActivity";
  //! ID used to identify the source of the result (#onActivityResult).
  protected static final int REQUEST_CONTACT = 1;
  
  //! Adapter used to show #m_cursor
  protected SimpleCursorAdapter m_adapter;
  //! Database of all persons that can be located...
  protected StalkerDatabase m_db;
  //! Current cursor of all persons (from #m_db). 
  protected Cursor m_cursor = null;
  
  
  /*! \brief Callback method (Activity), called if a instance
   *         of this activity has been created.
   * 
   *  Database object is created and adapter is created.
   * 
   *  \param savedInstanceState bundle to save extra state info.
   *         No extra fields have been added to this Bundle.
   * */
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
  
  /*! \brief Callback method (Activity), called if menu has to be created.
   * 
   *  \param menu The Menu that has to be created.
   *  \return \c true if menu has been created.
   * */
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.buddy_list_menu, menu);
    return true;
  }
  
  /*! \brief Callback method (Activity), called if user pressed a menu
   *         item
   * 
   *  \param item The item that has been pressed.
   *  \return \c true means that the event has been handled.
   * */
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.buddy_list_mnu_add:
        Intent pick_intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(pick_intent, REQUEST_CONTACT);
        return true;
      
      case R.id.buddy_list_mnu_back:
        this.finish(); 
        return true;
      
      case R.id.buddy_list_mnu_add_direct:
        Intent direct_intent = new Intent(this, DirectAddActivity.class);
        startActivity(direct_intent);
        return true;
      
      default:
        return super.onOptionsItemSelected(item);
    }
  }
  
  /*! \brief Callback method (Activity), called if an Activity returned
   *         a result (\c startActivityForResult).
   * 
   *  \param request The ID of the request (to distinguish different 
   *         requests).
   *  \param result The result of the operation.
   *  \param intent The original intent that has been used to start the
   *         Activity.
   * */
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
          
          String number = Utils.formatTelephoneNumber(cursor.getString(idx_number));
          String name = cursor.getString(idx_name);
          
          if (number.startsWith("+"))
          {
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
            Intent fix_intent = new Intent(this, FixNumberActivity.class);
            fix_intent.putExtra(FixNumberActivity.FIX_NUMBER, number);
            fix_intent.putExtra(FixNumberActivity.FIX_NAME, name);
            startActivity(fix_intent);
          }
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
  
  /*! \brief Callback method (ListActivity), called if an item has been
   *         clicked.
   * 
   *  \param listview The ListView that has been clicked.
   *  \param view The item that has been clicked.
   *  \param position The position in the ListView of the clicked icon.
   *  \param id The \c _id of the clicked item (\c rowid of \ref StalkerDatabase
   *         is set to \c _id, therefore it can be used to retrieve
   *         the clicked person through \ref StalkerDatabase.getPersonFromId).
   * */
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
  
  //! Helper method to release the current cursor.
  protected void releaseList()
  {
    Log.d(TAG, "Close cursor");
    if (m_cursor != null)
    {
      m_cursor.close();
      m_cursor = null;
    }
  }
  
  //! Helper method to load an up-to-date cursor and set adapter.
  protected void reloadList()
  {
    Cursor c = m_db.queryAllPersons();
    m_adapter.changeCursor(c);
    releaseList();
    m_cursor = c;
    Log.d(TAG, "Create cursor");
  }
  
  //! Callback method (Activity), called if Activity is about to close.
  @Override
  public void onStop()
  {
    releaseList();
    super.onStop();
  }
  
  //! Callback method (Activity), called if Activity has been started.
  @Override
  public void onStart()
  {
    super.onStart();
    reloadList();
  }
  
  //! Callback method (Activity), called if Activity is destroyed.
  @Override
  public void onDestroy()
  {
    m_db.close();
    super.onDestroy();
  }
}
