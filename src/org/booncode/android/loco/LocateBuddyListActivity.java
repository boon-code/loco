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


/*! \brief Activity that shows a list of all contacts that can be located.
 * 
 *  This Activity just shows all contacts that have been added and can
 *  be located. If the user clicks on the contact, he/she will be located.
 * */
public class LocateBuddyListActivity extends ListActivity
{
  //! TAG to identify log messages from this class.
  protected static final String TAG = "loco.LocateBuddyListActivity";
  
  //! Adapter used to show #m_cursor
  protected SimpleCursorAdapter m_adapter;
  //! Database of all persons that can be located.
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
  
  /*! \brief Callback method (ListActivity), called if a contact has been
   *         clicked.
   * 
   *  \param listview The ListView that has been clicked.
   *  \param view The Item that has been clicked.
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
      
      Utils.sendLocateSMS(person.number);
      this.finish();
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
