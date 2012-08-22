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


/*! \brief Settings Activity, this activity automatically saves changes
 *         to the default SharedPreference.
 * 
 *  Currently only 2 Options are included:
 *  \li Theft-Mode: Currently just a flag that will be set to \c true if
 *      SimCheckingService detects a different sim card.
 *  \li Sim-Protection: If this flag is set to \c true, sim-protection is
 *      enabled every time the device is booted.
 * 
 *  \see res/xml/preferences.xml for the preference options.
 * */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, OnClickListener
{
  //! TAG that is used to identify log messages from this class.
  protected static final String TAG = "loco.SettingsActivity";
  
  //! Reference to extra TextView to show line1 number that has been configured.
  protected TextView            m_txt_number;
  //! Reference to the button to set line1 number to the line1 number from the current sim card.
  protected Button              m_btn_setnumber;
  //! Object to set/retrieve settings for this application.
  protected ApplicationSettings m_settings;
  
  
  /*! \brief Callback method (Activity), called if activity is created.
   * 
   *  \param savedInstanceState bundle to save extra state info.
   *         No extra fields have been added to this Bundle.
   * */
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    m_txt_number = new TextView(this);
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
  
  /*! \brief Helper method to update #m_txt_number View's text.
   * 
   *  Shows state information of the current sim-protection settings
   *  and enable/disable #m_btn_setnumber accordingly.
   * */
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
  
  /*! \brief Callback method (OnSharedPreferenceChangeListener), called
   *         if a Preference has been changed by the user.
   * 
   *  This is useful to update the extra TextView (#m_txt_number) and 
   *  the Button (#m_btn_setnumber) accordingly.
   * 
   *  \param sharedPreferences An instance of the default SharedPreference
   *         that can be used to access settings in this method
   *  \param key The key that has been changed.
   * 
   *  \see updateContent This method is used to update the Views.
   * */
  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
  {
    if (ApplicationSettings.KEY_SIM_PROTECT.equals(key))
    {
      updateContent();
    }
  }
  
  /*! \brief Callback method (OnClickListener), called if #m_btn_setnumber 
   *         has been pressed.
   * 
   *  \param v The view that raised this event.
   * 
   *  \see updateContent This method updates the Views.
   * */
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
    }
    updateContent();
  }
  
  /*! \brief Callback method (Activity), called if the activity is about
   *         to beeing stopped.
   * */
  @Override
  public void onDestroy()
  {
    m_settings.unregisterChangeListener(this);
    super.onDestroy();
  }
}
