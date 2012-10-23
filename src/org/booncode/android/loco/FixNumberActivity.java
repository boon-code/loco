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
import android.telephony.PhoneNumberUtils;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.EditText;
import android.widget.Toast;

import android.util.Log;
import android.widget.Toast;


/*! \brief This Activity is used to ensure that numbers in
 *         \ref StalkerDatabase are in international format.
 * 
 *  Since there is no method, to normalize a telephone number 
 *  I decided to bug the user to format the number.
 * 
 *  \todo Find a way to remove this activity and automatically format
 *        telephone numbers to international format.
 * */
public class FixNumberActivity extends Activity
{
  //! Intent extra key that holds the number to format.
  public static final String FIX_NUMBER = "number";
  //! Intent extra key that holds the name of the person to add.
  public static final String FIX_NAME = "name";
  
  //! TAG to identify log messages from this class.
  protected static final String TAG = "loco.FixNumberActivity";
  
  //! Database object to add person.
  protected StalkerDatabase m_db;
  //! Reference to the TextView to show the name of the person to add.
  protected TextView m_txt_name;
  //! Reference to the EditText control to fix the number.
  protected EditText m_txt_number;
  //! Name extracted from the Intent extras.
  protected String m_name;
  //! Original (not international) number extracted from the Intent extras.
  protected String m_number;
  
  
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
    this.setContentView(R.layout.fix_layout);
    
    m_txt_name = (TextView)findViewById(R.id.fix_txt_name);
    m_txt_number = (EditText)findViewById(R.id.fix_txt_number);
    
    m_db = new StalkerDatabase(getApplicationContext());
  }
  
  /*! Helper method to extract all Intent extras (#FIX_NAME, #FIX_NUMBER).
   * 
   *  \return Returns \c true if #m_name and #m_number could be extracted,
   *          else \c false.
   * */
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
  
  /*! Callback method, called if cancel button has been pressed.
   * 
   *  \param v The view that has been pressed.
   * */
  public void onCancel(View v)
  {
    finish();
  }
  
  /*! Callback method, called if the save button has been pressed.
   * 
   *  This method checks if number could be in international format,
   *  and if \c PhoneNumberUtils think the entered number is
   *  equal to the original number (#m_number).
   * 
   *  \note I'm not sure if \c PhoneNumberUtils.compare really works.
   *        I have to test it with some numbers...
   * 
   *  \param v The view that has been pressed.
   * */
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
