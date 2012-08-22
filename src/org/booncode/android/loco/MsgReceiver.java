package org.booncode.android.loco;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.telephony.SmsMessage;
import android.util.Log;


/*! \brief BroadcastReceiver that gets called if a sms has been received.
 * 
 *  This class has to be registered in the AndroidManifest and has to be
 *  given a proper priority to ensure that it's the first class that
 *  gets notified about a newly received sms. Since receiving an sms
 *  is an ordered broadcast, the BroadcastReceiver with the highest 
 *  priority can decide whether the notification will pass to the next
 *  Receiver or not.
 * */
public class MsgReceiver extends BroadcastReceiver
{
  //! Action that indicates that an sms has been received.
  public static final String RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
  //! This string indicates that this is a command.
  public static final String LOCO_CMD_START = "@+";
  //! Locate command.
  public static final String LOCO_CMD_LOCATE = LOCO_CMD_START + "locate";
  //! Command that indicates a response message with a geo location.
  public static final String LOCO_CMD_VIEW_POSITION = LOCO_CMD_START + "geo:";
  //! Command that indicates a response message with cell information.
  public static final String LOCO_CMD_VIEW_CELLS = LOCO_CMD_START + "cell";
  
  //! TAG used to identify log messages from this class.
  protected static final String TAG = "loco.MsgReceiver";
  //! The context of this BroadcastReceiver (see #onReceive).
  private Context m_context = null;
  
  /*! \brief This method really checks the actual message.
   * 
   *  This method does basic checks if this message could be a command
   *  and the telephone number the message is sent from can be found in
   *  Database.
   * 
   *  \note Note that this method doesn't check if a person is authorised
   *        to execute a command on the device (since this depends on 
   *        the actual command and should rather be decided by \ref Stalker).
   * 
   *  \param msg The content of the sms.
   *  \return \c true if this message should be filtered, else \c false.
   *          If #startStalker fails, the message will not be filtered
   *          (because it's likely that it's not a real command from
   *          this application...)
   * 
   *  \see startStalker starts the \ref Stalker service if command is 
   *       valid.
   * */
  protected boolean processMessage(SmsMessage msg)
  {
    String text = msg.getMessageBody();
    if (text != null)
    {
      Log.d(TAG, "Inspecting Message...");
      
      if (text.startsWith(LOCO_CMD_START))
      {
        String number = Utils.formatTelephoneNumber(msg.getOriginatingAddress());
        if (number != null)
        {
          StalkerDatabase db = new StalkerDatabase(m_context.getApplicationContext());
          StalkerDatabase.Person person = db.getPersonFromNumber(number);
          
          if (person != null)
          {
            return startStalker(text, number);
          }
          else
          {
            Log.d(TAG, "Person not permitted " + number);
          }
        }
        else
        {
          Log.w(TAG, "Couldn't retrieve number...");
        }
      }
    }
    
    return false;
  }
  
  /*! \brief This method extracts the actual message and checks if 
   *         it's a command.
   * 
   *  \param pdu The pdu object that should be scanned.
   *  \return \c true if this message should be filtered, else \c false.
   * 
   *  \see processMessage for further information on the detection of commands.
   * */
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
  
  /*! \brief Inspects the intent that has been received by this class.
   * 
   *  Checks if this intent contains an sms and does further checking.
   * 
   *  \param intent The intent to check.
   *  \return \c true if this message should be filtered, else \c false.
   * 
   *  \see scanPDU for further information about the detection of commands.
   * */
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
        // Ignore this error...
        Log.e(TAG, "inspectIntent: Couldn't retrieve PDU's", e);
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
  
  /*! brief Helper method that starts the \ref Stalker service.
   * 
   *  \param cmd The command that should be handled by the \ref Stalker
   *         class.
   *  \param number The telephone number of the sender of the sms that
   *         caused the \ref Stalker to start.
   *  \return \c true if \ref Stalker service has been started, \c false
   *          if an illegal command has been passed and the service has 
   *          therefore not been started.
   * */
  protected boolean startStalker(String cmd, String number)
  {
    Intent intent = new Intent(this.m_context, Stalker.class);
    intent.setFlags(Intent.FLAG_FROM_BACKGROUND);
    
    intent.putExtra(Stalker.EXTRA_KEY_NUMBER, number);
    
    if (cmd.startsWith(LOCO_CMD_LOCATE))
    {
       intent.putExtra(Stalker.EXTRA_KEY_CMD, Stalker.CMD_LOCATE);
    }
    else if(cmd.startsWith(LOCO_CMD_VIEW_POSITION))
    {
      intent.putExtra(Stalker.EXTRA_KEY_CMD, Stalker.CMD_RECEIVE_RESULT_POSITION);
      intent.putExtra(Stalker.EXTRA_KEY_MESSAGE, cmd.substring(LOCO_CMD_VIEW_POSITION.length()));
    }
    else if(cmd.startsWith(LOCO_CMD_VIEW_CELLS))
    {
      intent.putExtra(Stalker.EXTRA_KEY_CMD, Stalker.CMD_RECEIVE_RESULT_CELLS);
      intent.putExtra(Stalker.EXTRA_KEY_MESSAGE, cmd.substring(LOCO_CMD_VIEW_CELLS.length()));
    }
    else
    {
      Log.d(TAG, "Unknown command " + cmd);
      return false;
    }
    
    this.m_context.startService(intent);
    return true;
  }
  
  /*! \brief Callback method (BroadcastReceiver), called if an sms
   *         has been received.
   * 
   *  This is the main entry point of this class. Every time, the system
   *  receives an sms, this method will be invoked. It's essential, that
   *  this method decides whether the sms is a normal message or a command
   *  and if it's a command, abort the ordered broadcast. This prevents
   *  that the message passes to the next BroadcastReceiver.
   * 
   *  \param context The context that can be used to access application
   *         data (for example).
   *  \param intent The intent that holds information about the event.
   * 
   *  \see inspectIntent which checks the message.
   * */
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
