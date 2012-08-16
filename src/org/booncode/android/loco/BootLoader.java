package org.booncode.android.loco;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.util.Log;

public class BootLoader extends BroadcastReceiver
{
  public static final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
  protected static final String TAG = "loco.BootLoader";
  
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
