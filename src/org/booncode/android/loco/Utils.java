package org.booncode.android.loco;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.SmsManager;
import android.util.Log;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;


/*! \brief This class has got some utility functions (static).
 * 
 * */
public class Utils
{
  //! TAG used to identify log messages from this class.
  protected static final String TAG = "loco.Utils";
  //! Magic header that has to be sent to maps.
  protected static final byte[] MAPS_HEADER = new byte[] {0x00, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                          0x00, 0x00, 0x00, 0x02, 0x65, 0x6E, 0x00, 0x07,
                                                          0x41, 0x6E, 0x64, 0x72, 0x6F, 0x69, 0x64, 0x00,
                                                          0x03, 0x31, 0x2E, 0x30, 0x00, 0x03, 0x57, 0x65,
                                                          0x62, 0x1B, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                          0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00};
  //! Magic trailer that has to be sent to maps.
  protected static final byte[] MAPS_TRAILER = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                           0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
  
  
  /*! \brief This class is used to return location and a flag that
   *         indicates successs or failure.
   * */
  public static class LocationResult
  {
    //! Latitude of position (only valid if #success is \c true).
    public final double latitude;
    //! Longitude of position (only valid if #sucess is \c true).
    public final double longitude;
    //! Flag that indicates if the position is valid.
    public final boolean success;
    
    /*! \brief Constructs a new instance.
     * 
     *  \param success Flag that indicates if position is valid.
     *  \param latitude Latitude of position.
     *  \param longitude Longitude of position.
     * */
    public LocationResult(boolean success, double latitude, double longitude)
    {
      this.success = success;
      this.latitude = latitude;
      this.longitude = longitude;
    }
    
    /*! \brief Constructs a new instance.
     * 
     *  #success is automatically set to \c true.
     * 
     *  \param latitude Latitude of position.
     *  \param longitude Longitude of position.
     * */
    public LocationResult(double latitude, double longitude)
    {
      this.success = true;
      this.latitude = latitude;
      this.longitude = longitude;
    }
    
    //! Constructs an invalid position result (#success is \c false).
    public LocationResult()
    {
      this.success = false;
      this.latitude = 0.0;
      this.longitude = 0.0;
    }
  }
  
  /*! \brief converts an integer to a byte array (big endian).
   * 
   *  \param value The value to convert.
   *  \return A byte array in big endian format representing the original
   *          value.
   * */
  public static byte[] toBigEndianByteArray(int value)
  {
    byte[] data = new byte[4];
    data[3] = (byte)((value >> 24) & 0xff);
    data[2] = (byte)((value >> 16) & 0xff);
    data[1] = (byte)((value >> 8 ) & 0xff);
    data[0] = (byte)(value & 0xff);
    return data;
  }
  
  public static void writeIntBigEndian(OutputStream stream, int value) throws IOException
  {
    stream.write((value >> 24) & 0xff);
    stream.write((value >> 16) & 0xff);
    stream.write((value >> 8 ) & 0xff);
    stream.write(value & 0xff);
  }
  
  public static LocationResult locateGsmCell(int cellid, int lac)
  {
    try
    {
      URL url = new URL("http://www.google.com/glm/mmap");
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.connect();
      OutputStream ostream = connection.getOutputStream();
      ostream.write(MAPS_HEADER);
      writeIntBigEndian(ostream, cellid);
      writeIntBigEndian(ostream, lac);
      ostream.write(MAPS_TRAILER);
      ostream.flush();
      
      DataInputStream istream = new DataInputStream(connection.getInputStream());
      istream.skipBytes(3);
      int error_code = istream.readInt();
      
      if (error_code == 0)
      {
        double latitude  = (double)istream.readInt() / 1000000.0;
        double longitude = (double)istream.readInt() / 1000000.0;
        return new LocationResult(latitude, longitude);
      }
      else
      {
        Log.d(TAG, String.format("locateGsmCell: Google returned error-code: %d", error_code));
      }
    }
    catch(IOException ex)
    {
      Log.e(TAG, "locateGsmCell: Couldn't retrieve position...", ex);
    }
    return new LocationResult();
  }
  
  public static String formatGeoData(String latitude, String longitude, String tag)
  {
    return String.format("geo:0,0?q=%s,%s (%s)", latitude, longitude, tag);
  }
  
  public static String formatGeoData(double latitude, double longitude, String tag)
  {
    return formatGeoData(String.valueOf(latitude), String.valueOf(longitude), tag);
  }
  
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
  
  public static void sendLocateSMS(String number)
  {
    Utils.sendSMS(number, MsgReceiver.LOCO_CMD_LOCATE);
  }
  
  public static String getLine1Number(Context context)
  {
    TelephonyManager tel_man = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    if (tel_man.getSimState() == TelephonyManager.SIM_STATE_READY)
    {
      String number = tel_man.getLine1Number();
      if (number != null)
      {
        return formatTelephoneNumber(number);
      }
    }
    
    return null;
  }
}
