package org.booncode.android.loco;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.util.Log;


/*! \brief BroadcastReceiver that gets called if the phone just booted.
 * 
 *  This class is used to receive an intent if the boot procedure has
 *  completed. \ref SimCheckingService will be started if proper
 *  intent has been received.
 * */
public class BootLoader extends BroadcastReceiver
{
  //! Intent action this BroadcastReceiver is listening for.
  public static final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
  //! TAG to identify logging message from this class.
  protected static final String TAG = "loco.BootLoader";
  
  /*! \brief Callback method (BroadcastReceiver), called if some intent
   *         has been received.
   * 
   *  This Receiver has to be registered in the AndroidManifest to
   *  listen to specific events (in this case the #BOOT_ACTION).
   * */
  @Override
  public void onReceive(Context context, Intent intent)
  {
    if (intent != null)
    {
      String action = intent.getAction();
      if (BOOT_ACTION.equals(action))
      {
        Log.d(TAG, "BootLoader::onReceive()");
        Intent sim_check_intent = new Intent(context, SimCheckingService.class);
        context.startService(sim_check_intent);
      }
      else
      {
        Log.e(TAG, "Unexpected action: " + action);
      }
    }
    
  }
}
