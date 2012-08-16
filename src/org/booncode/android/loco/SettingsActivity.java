package org.booncode.android.loco;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.TextView;
import android.view.View;
import android.widget.ListView;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, OnClickListener
{
  protected static final String TAG = "loco.SettingsActivity";
  
  protected String              m_line1_number;
  protected TextView            m_txt_number;
  protected Button              m_btn_setnumber;
  protected ApplicationSettings m_settings;
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    m_txt_number = new TextView(this);
    m_txt_number.setText("Bla bla bla");
    m_btn_setnumber = new Button(this);
    m_btn_setnumber.setText("Set current Line1 Number");
    m_btn_setnumber.setOnClickListener(this);
    ListView l = getListView();
    l.addFooterView(m_txt_number);
    l.addFooterView(m_btn_setnumber);
    m_settings = new ApplicationSettings(this);
    m_settings.registerChangeListener(this);
    updateContent();
  }
  
  private void updateContent()
  {
    boolean sim_protected = m_settings.isSimProtectEnabled();
    m_btn_setnumber.setEnabled(sim_protected);
    if (sim_protected)
    {
      String number = m_settings.getLine1Number();
      if (number != null)
      {
        m_txt_number.setText("Current number: " + number);
      }
      else
      {
        m_txt_number.setText("Phone-number not set! Press button to set to current!");
      }
    }
    else
    {
      m_txt_number.setText("");
    }
  }
  
  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
  {
    if (ApplicationSettings.KEY_SIM_PROTECT.equals(key))
    {
      updateContent();
    }
  }
  
  public void onClick(View v)
  {
    String number = Utils.getLine1Number(this);
    if (number != null)
    {
      m_settings.setLine1Number(number);
      Toast.makeText(this, "Successfully set number!", Toast.LENGTH_LONG).show();
    }
    else
    {
      Toast.makeText(this, "Error: Sim Card not ready!", Toast.LENGTH_LONG).show();
      //m_settings.setSimProtect(false);
      //finish();
    }
    updateContent();
  }
  
  @Override
  public void onDestroy()
  {
    m_settings.unregisterChangeListener(this);
    super.onDestroy();
  }
}
