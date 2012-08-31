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
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

import android.util.Log;
import android.widget.Toast;


/*! \brief Activity that is used to show cell information
 * 
 *  Added buttons to retrieve the position (if possible) and 
 *  open maps.
 * */
public class GsmCellActivity extends Activity
{
  //! Intent extra key for gsm data (String[]).
  public static final String EXTRA_KEY_GSM_DATA = "gsm";
  //! Intent extra key for the telephone number of the tracked person.
  public static final String EXTRA_SHOW_NUMBER = "number";
  //! Intent extra key for the name of the tracked person.
  public static final String EXTRA_SHOW_NAME = "name";
  
  //! TAG to identify log messages from this class.
  protected static final String TAG = "loco.GsmCellActivity";
  
  //! Shows raw gsm data that has been received.
  protected TextView m_txt_data;
  //! Shows the response message of maps.
  protected TextView m_txt_browser;
  //! Reference to button to open maps.
  protected Button   m_btn_maps;
  //! Reference to button to query server.
  protected Button   m_btn_findpos;
  //! Current cell-ID
  protected int m_cellid = 0;
  //! Current lac.
  protected int m_lac = 0;
  //! Last location result.
  protected Utils.LocationResult m_result = new Utils.LocationResult();
  
  
  /*! \brief Callback method (Activity), called if a instance
   *         of this activity has been created.
   * 
   *  Sets up references to controls.
   * 
   *  \param savedInstanceState bundle to save extra state info.
   *         No extra fields have been added to this Bundle.
   * */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.gsm_layout);
    
    m_txt_data = (TextView)findViewById(R.id.gsm_txt_data);
    m_txt_browser = (TextView)findViewById(R.id.gsm_txt_browser);
    m_btn_maps = (Button)findViewById(R.id.gsm_btn_maps);
    m_btn_findpos = (Button)findViewById(R.id.gsm_btn_findposition);
    
    m_btn_maps.setEnabled(false);
    m_btn_findpos.setEnabled(false);
  }
  
  /*! \brief Helper method to read raw gsm data.
   * 
   *  Shows raw gsm data in #m_txt_data (if raw data could be read).
   *  
   *  \param intent The intent to read data from.
   *  \return \c true if raw data could be read, else \c false.
   * */
  private boolean readData(Intent intent)
  {
    if (intent == null)
    {
      Log.e(TAG, "readData: got null intent...");
      return false;
    }
    
    String data[] = intent.getStringArrayExtra(EXTRA_KEY_GSM_DATA);
    
    if (data == null)
    {
      Log.e(TAG, "readData: gsm-data is null");
      return false;
    }
    
    if (data.length != 4)
    {
      Log.e(TAG, "readData: wrong format: expect 4 strings...");
      return false;
    }
    
    try
    {
      m_cellid = Integer.parseInt(data[2]);
      m_lac = Integer.parseInt(data[3]);
    }
    catch(NumberFormatException	ex)
    {
      Log.e(TAG, "Illegal format of cellid or lac", ex);
      return false;
    }
    
    StringBuilder b = new StringBuilder();
    
    b.append("mcc: ");
    b.append(data[0]);
    b.append("\nmnc: ");
    b.append(data[1]);
    b.append("\ncellid: ");
    b.append(data[2]);
    b.append("\nlac: ");
    b.append(data[3]);
    
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
    boolean success = readData(intent);
    m_btn_findpos.setEnabled(success);
    
    if (!success)
    {
      m_txt_data.setText("Couldn't retrieve data from intent");
    }
  }
  
  /*! \brief Callback method (Activity), called if activity has been
   *         started.
   * 
   *  Loads cell information and sets view accordingly.
   * */
  @Override
  public void onStart()
  {
    Log.d(TAG, "onStart");
    super.onStart();
    loadCellData(getIntent());
  }
  
  /*! \brief Callback method (Activity), called if activity resumes.
   * 
   *  Loads cell information and sets view accordingly.
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
   *  Loads cell information and sets view accordingly.
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
  
  /*! \brief Callback method, called if #m_btn_findpos has been pressed.
   * 
   *  Tries to find the position of the gsm cell by asking a server.
   *  
   *  \param v The View that has been pressed.
   * 
   *  \see Utils.locateGsmCell for details on retrieving the location
   *       of the gsm-cell.
   * */
  public void onFindPositionClicked(View v)
  {
    m_result = Utils.locateGsmCell(m_cellid, m_lac);
    m_btn_maps.setEnabled(m_result.success);
    if (m_result.success)
    {
      m_txt_browser.setText("Got data: latitude: " + String.valueOf(m_result.latitude) + ", longitude: " + String.valueOf(m_result.longitude));
    }
    else
    {
      m_txt_browser.setText("Couldn't retrieve position...");
    }
  }
  
  /*! \brief Callback method, called if #m_btn_maps has been pressed.
   * 
   *  Tries to open maps and show the position of the gsm-cell.
   * 
   *  \param v The View that has been pressed.
   * */
  public void onShowMaps(View v)
  {
    if (m_result.success)
    {
      Uri uri = Uri.parse(Utils.formatGeoData(m_result.latitude, m_result.longitude, "GSM-Cell"));
      Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
      startActivity(intent);
    }
  }
  
}
