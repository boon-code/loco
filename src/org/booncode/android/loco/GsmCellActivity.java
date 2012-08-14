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

public class GsmCellActivity extends Activity
{
  public static final String EXTRA_KEY_GSM_DATA = "gsm";
  public static final String EXTRA_SHOW_NUMBER = "number";
  public static final String EXTRA_SHOW_NAME = "name";
  
  protected static final String TAG = "loco.BuddyActivity";
  
  protected TextView m_txt_data;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.gsm_layout);
    
    m_txt_data = (TextView)findViewById(R.id.txt_gsm_data);
  }
  
  @Override
  public void onStart()
  {
    super.onStart();
    
    Intent intent = getIntent();
    
    String[] data = intent.getStringArrayExtra(EXTRA_KEY_GSM_DATA);
    StringBuilder b = new StringBuilder();
    
    for (String i : data)
    {
      b.append(i);
      b.append("\n");
    }
    
    m_txt_data.setText(b.toString());
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
