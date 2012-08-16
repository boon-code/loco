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

public class GsmCellActivity extends Activity
{
  public static final String EXTRA_KEY_GSM_DATA = "gsm";
  public static final String EXTRA_SHOW_NUMBER = "number";
  public static final String EXTRA_SHOW_NAME = "name";
  
  protected static final String TAG = "loco.GsmCellActivity";
  
  protected TextView m_txt_data;
  protected TextView m_txt_browser;
  protected Button   m_btn_maps;
  protected Button   m_btn_findpos;
  
  protected int m_cellid = 0;
  protected int m_lac = 0;
  protected Utils.LocationResult m_result = new Utils.LocationResult();
  
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
  
  private boolean readData()
  {
    Intent intent = getIntent();
    
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
  
  @Override
  public void onStart()
  {
    super.onStart();
    
    boolean success = readData();
    m_btn_findpos.setEnabled(success);
    
    if (!success)
    {
      m_txt_data.setText("Couldn't retrieve data from intent");
    }
  }
  
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
  
  public void onShowMaps(View v)
  {
    if (m_result.success)
    {
      Uri uri = Uri.parse(Utils.formatGeoData(m_result.latitude, m_result.longitude, "GSM-Cell"));
      Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
      startActivity(intent);
    }
  }
  
  @Override
  public void onStop()
  {
    super.onStop();
  }
  
  @Override
  public void onDestroy()
  {
    super.onDestroy();
  }
  
}
