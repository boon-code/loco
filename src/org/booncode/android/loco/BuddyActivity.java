/* *******************************************************************************
 * LOCO - Localizes the position of you mobile.
 * Copyright (C) 2012  Manuel Huber
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * *******************************************************************************/
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


/*! \brief This Activity presents information about one contact in the
 *         \ref StalkerDatabase.
 * */
public class BuddyActivity extends Activity
{
  //! Intent extra key of the number of the person whos info will be shown.
  public static final String SHOW_NUMBER = "number";
  
  //! TAG to identify log messages from this class.
  protected static final String TAG = "loco.BuddyActivity";
  
  //! Database to retrieve information about the person (and change certain settings).
  protected StalkerDatabase m_db;
  //! Extracted number of the person that is currently shown.
  protected String m_number;
  //! Reference to the TextView that contains the name of the person.
  protected TextView m_txt_name;
  //! Reference to the TextView that contains the number of the person.
  protected TextView m_txt_number;
  //! Reference to the TextView that contains the number of sms this person caused.
  protected TextView m_txt_smscount;
  //! Reference to the CheckBox that indicates whether the person is authorised to cause sms to be sent.
  protected CheckBox m_chk_auth;
  
  
  /*! \brief Callback method (Activity), called if a instance
   *         of this activity has been created.
   * 
   *  Database object is created references to controls are set up.
   * 
   *  \param savedInstanceState bundle to save extra state info.
   *         No extra fields have been added to this Bundle.
   * */
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
  
  /*! \brief Helper method that extracts information from Intent extras.
   * 
   *  Extracts number from intent and queries all information of this 
   *  person by using #m_db.
   * 
   *  \return Returns \c true if data could be extracted, else \c false.
   * */
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
  
  /*! \brief Callback method, called if #m_chk_auth CheckBox has been 
   *         clicked.
   * 
   *  \param v The view that has been clicked.
   * */
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
  
  /*! \brief Callback method (Activity), called if menu has to be created.
   * 
   *  \param menu The Menu that has to be created.
   *  \return \c true if menu has been created.
   * */
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.buddy_menu, menu);
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
  
  /*! \brief Callback method (Activity), called if Activity got started.
   * 
   *  If #extractData return \c false, the Activity will be closed
   *  immediately.
   * */
  @Override
  public void onStart()
  {
    super.onStart();
    if (!extractData())
    {
      this.finish();
    }
  }
  
  //! Callback method (Activity), called if Activity is destroyed.
  @Override
  public void onDestroy()
  {
    m_db.close();
    super.onDestroy();
  }
}
