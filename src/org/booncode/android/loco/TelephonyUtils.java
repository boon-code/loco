package org.booncode.android.loco;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.telephony.SmsMessage;
import android.telephony.SmsManager;
import android.util.Log;
import android.telephony.PhoneNumberUtils;

public class TelephonyUtils
{
  protected static final String TAG = "loco.TelephonyUtils";
  
  public static String formatTelephoneNumber(String number)
  {
    return PhoneNumberUtils.stripSeparators(number);
  }
  
  public static void sendSMS(String number, String message)
  {
    SmsManager man = SmsManager.getDefault();
    man.sendTextMessage(number, null, message, null, null);
    Log.d(TAG, String.format("SEND SMS (number=%s): '%s'", number, message));
  }
}
