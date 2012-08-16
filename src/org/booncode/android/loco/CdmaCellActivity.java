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

public class CdmaCellActivity extends Activity
{
  public static final String EXTRA_KEY_CDMA_DATA = "cdma";
  public static final String EXTRA_SHOW_NUMBER = "number";
  public static final String EXTRA_SHOW_NAME = "name";
  
  protected static final String TAG = "loco.CdmaCellActivity";
  
  protected TextView m_txt_data;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.cdma_layout);
    
    m_txt_data = (TextView)findViewById(R.id.cdma_txt_data);
  }
  
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
  
  @Override
  public void onStart()
  {
    super.onStart();
    
    if (!readData())
    {
      m_txt_data.setText("Couldn't retrieve data from intent");
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
