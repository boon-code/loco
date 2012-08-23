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
   *  \return \c true if raw data could be read, else \c false.
   * */
  private boolean readData()
  {
    Intent intent = getIntent();
    
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
  
  /*! \brief Callback method (Activity), called if activity has been
   *         started.
   * 
   *  Tries to read raw cdma-data.
   * */
  @Override
  public void onStart()
  {
    super.onStart();
    
    if (!readData())
    {
      m_txt_data.setText("Couldn't retrieve data from intent");
    }
  }
  
}
