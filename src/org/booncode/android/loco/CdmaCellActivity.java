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


//! Activity that is used to show cell information (cdma)
public class CdmaCellActivity extends Activity
{
  //! Intent extra key for raw cdma data (String[]).
  public static final String EXTRA_KEY_CDMA_DATA = "cdma";
  //! Intent extra key for the telephone number of the tracked person.
  public static final String EXTRA_SHOW_NUMBER = "number";
  //! Intent extra key for the name of the tracked person.
  public static final String EXTRA_SHOW_NAME = "name";
  
  //! TAG to identify log messages from this class.
  protected static final String TAG = "loco.CdmaCellActivity";
  
  //! Shows raw cdma data that has been received.
  protected TextView m_txt_data;
  
  
  /*! \brief Callback method (Activity), called if a instance
   *         of this activity has been created.
   * 
   *  Sets up a reference to the raw data view (#m_txt_data).
   * 
   *  \param savedInstanceState bundle to save extra state info.
   *         No extra fields have been added to this Bundle.
   * */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.cdma_layout);
    
    m_txt_data = (TextView)findViewById(R.id.cdma_txt_data);
  }
  
  /*! \brief Helper method to read raw cdma data.
   * 
   *  Shows raw cdma data in #m_txt_data (if raw data could be read).
   * 
   *  \param intent The Intent to read data from.
   *  \return \c true if raw data could be read, else \c false.
   * */
  private boolean readData(Intent intent)
  {
    if (intent == null)
    {
      Log.e(TAG, "readData: got null intent...");
      return false;
    }
    
    String data[] = intent.getStringArrayExtra(EXTRA_KEY_CDMA_DATA);
    
    if (data == null)
    {
      Log.e(TAG, "readData: cdma-data is null");
      return false;
    }
    
    if (data.length != 5)
    {
      Log.e(TAG, "readData: wrong format: expect 5 strings...");
      return false;
    }
    
    StringBuilder b = new StringBuilder();
    
    b.append("BaseStationID: ");
    b.append(data[0]);
    b.append("\nBaseStationLatitude: ");
    b.append(data[1]);
    b.append("\nBaseStationLongitude: ");
    b.append(data[2]);
    b.append("\nNetworkID: ");
    b.append(data[3]);
    b.append("\nSystemID: ");
    b.append(data[4]);
    
    m_txt_data.setText(b.toString());
    return true;
  }
  
  /*! Helper method that loads cell-information from intent.
   * 
   *  Trys to load cell information from intent and
   *  shows an error message if no valid data has been found in
   *  \c Intent object.
   * 
   *  \param intent The intent to load data from.
   * */
  protected void loadCellData(Intent intent)
  {
    if (!readData(intent))
    {
      m_txt_data.setText("Couldn't retrieve data from intent");
    }
  }
  
  /*! \brief Callback method (Activity), called if activity has been
   *         started.
   * 
   *  Tries to read raw cdma-data.
   * */
  @Override
  public void onStart()
  {
    super.onStart();
    loadCellData(getIntent());
  }
  
  /*! \brief Callback method (Activity), called if activity resumes.
   * 
   *  Tries to read raw cdma-data.
   * */
  @Override
  public void onResume()
  {
    Log.d(TAG, "onResume");
    super.onResume();
    loadCellData(getIntent());
  }
  
  /*! \brief Callback method (Activity), called if there is an instance
   *         of this activity, but it has been started with a new
   *         \c Intent.
   * 
   *  Tries to read raw cdma-data.
   * 
   *  \param intent New intent that has to be used by the activity.
   * */
  @Override
  public void onNewIntent(Intent intent)
  {
    Log.d(TAG, "onNewIntent");
    setIntent(intent);
    super.onNewIntent(intent);
  }
  
}
