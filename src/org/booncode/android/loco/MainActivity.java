package org.booncode.android.loco;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.view.View;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.SmsManager;

import android.widget.EditText;
import android.widget.Toast;
import java.util.regex.*;
import android.util.Log;


/*! \brief Main activity class that can be started by the user
 *         (main entry point for most actions).
 * 
 *  This activity will be shown if the user starts this application.
 *  All further actions (except some services of \ref Stalker class
 *  can be controlled from this activity.
 * */
public class MainActivity extends Activity
{
  //! TAG used to identify log messages from this activity.
  protected static final String TAG = "loco.MainActivity";
  
  
  /*! \brief Callback method (Activity), called if a instance
   *         of this activity has been created.
   * 
   *  \param savedInstanceState bundle to save extra state info.
   *         No extra fields have been added to this Bundle.
   * */
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.main_layout);
  }
  
  /*! \brief Callback method, called if the user pressed the locate
   *         Button.
   * 
   *  Opens an activity to choose the person that should be located.
   *  
   *  \param v The View that raised this event (should be the locate
   *         Button).
   * */
  public void onLocateBuddy(View v)
  {
    Intent intent = new Intent(this, LocateBuddyListActivity.class);
    startActivity(intent);
  }
  
  /*! \brief Callback method, called if the user pressed the quit
   *         Button.
   *  
   *  Quits the application.
   * 
   *  \param v The View that raised this event (should be the quit
   *         Button).
   * */
  public void onQuit(View v)
  {
    this.finish();
    Toast.makeText(this, "Bye!", Toast.LENGTH_LONG).show();
  }
  
  /*! \brief Callback method, called if the user pressed the Settings
   *         Button.
   * 
   *  A Settings activity will be created.
   *  
   *  \param v The View that raised this event (should be the Settings
   *         Button).
   * */
  public void onShowSettings(View v)
  {
    Intent intent = new Intent(this, SettingsActivity.class);
    startActivity(intent);
  }
  
  /*! \brief Callback method, called if the user pressed the manage
   *         Button.
   * 
   *  Opens an activity to add, or view contacts that are allowed to
   *  track the position of this device or should be notified if
   *  sim-protection detects a different sim card.
   *  
   *  \param v The View that raised this event (should be the manage
   *         Button).
   * */
  public void onManageBuddyList(View v)
  {
    Intent intent = new Intent(this, BuddyListActivity.class);
    startActivity(intent);
  }
  
  /*! \brief Callback method, called if the user pressed the test
   *         Button.
   * 
   *  Starts the \ref SimCheckingService, which tries to check the
   *  current sim-card (if sim-protection is enabled).
   * 
   *  \param v The View that raised this event (should be the test 
   *         Button).
   * */
  public void onTestSim(View v)
  {
    Intent sim_check_intent = new Intent(this, SimCheckingService.class);
    startService(sim_check_intent);
  }
  
}
