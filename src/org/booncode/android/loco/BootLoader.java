/* *******************************************************************************
 * LOCO - Localizes the position of you mobile.
 * Copyright (C) 2012  Manuel Huber
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * *******************************************************************************/
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
