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


/*! \brief This Activity is used to directly enter contact details.
 * 
 *  I added this Activity because \ref FixNumberActivity maybe doesn't 
 *  let you enter a perfectly valid number... 
 * */
public class DirectAddActivity extends Activity
{
  //! TAG to identify log messages from this class.
  protected static final String TAG = "loco.DirectAddActivity";
  
  //! Database object used to add a person to the database.
  protected StalkerDatabase m_db;
  //! Reference to the EditText control to enter the name of the contact.
  protected EditText m_txt_name;
  //! Reference to the EditText control to enter the telephone number of the contact. 
  protected EditText m_txt_number;
  
  
  /*! \brief Callback method (Activity), called if a instance
   *         of this activity has been created.
   * 
   *  Sets up references to controls and database connection.
   * 
   *  \param savedInstanceState bundle to save extra state info.
   *         No extra fields have been added to this Bundle.
   * */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.direct_layout);
    
    m_txt_name = (EditText)findViewById(R.id.direct_txt_name);
    m_txt_number = (EditText)findViewById(R.id.direct_txt_number);
    
    m_db = new StalkerDatabase(getApplicationContext());
  }
  
  /*! Callback method, called if cancel button has been pressed.
   * 
   *  \param v The view that has been pressed.
   * */
  public void onCancel(View v)
  {
    finish();
  }
  
  /*! \brief Helper method to check if String is null or empty \c "".
   * 
   *  \param value String to test.
   *  \return \c true if \c value is null or empty, else \c false.
   * */
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
  
  /*! Callback method, called if the save button has been pressed.
   * 
   *  This method really tries to add a contact to the database.
   * 
   *  \note Note that it can fail to add a person to the database
   *        For example: it's not possible to have 2 contacts with same
   *        telephone number (which doesn't make sense anyway)!
   * 
   *  \param v The view that has been pressed.
   * 
   *  \see StalkerDatabase.addPerson for more information.
   * */
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
  
  /*! \brief Callback method (Activity), called if Activity is about to
   *         beeing destroyed.
   * 
   *  Closes the database connection.
   * */
  @Override
  public void onDestroy()
  {
    m_db.close();
    super.onDestroy();
  }
  
}
