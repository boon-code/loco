package org.booncode.android.loco;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteConstraintException;


/*! \brief This class handles an private sql database of all persons
 *         that are related to this application.
 * 
 *  Persons in the database have got a name, a number, caused sms count
 *  and can be authorised.
 * */
public class StalkerDatabase extends SQLiteOpenHelper
{
  //! The filename (private; this application only) of the database.
  public static final String DATABASE_FILE = "stalkerdb";
  /*! \brief Version of the database (can be used to identify and updated 
   *         outdated database from an older version of this software.
   * */
  public static final int DATABASE_VERSION = 1;
  //! Name of the sql table that holds all info about persons.
  public static final String TABLE_PERSONS = "persons";
  //! TAG used to identify log messages from this class.
  protected static final String TAG = "loco.StalkerDatabase";
  
  /*! \brief This class only holds all information of one person
   *         in this database.
   * */
  public static class Person
  {
    //! The Key of the name.
    public static final String NAME = "name";
    //! The Key of the telephone number.
    public static final String NUMBER = "number";
    //! The Key of the authorisation flag.
    public static final String AUTHORISED = "auth";
    //! The Key of the sms count field.
    public static final String SMS_COUNT = "smscount";
    
    //! The name of the person.
    public String name;
    //! The telephone number of the person.
    public String number;
    //! Flag whether this person is authorised to trigger sms-response (locating).
    public boolean authorised;
    //! Counter of how many sms have been caused by this person (on this device).
    public int smscount;
  }
  
  
  /*! \brief Constructor; Initializes database.
   * 
   *  \param context Context used to open the database.
   * */
  public StalkerDatabase(Context context)
  {
    // null means use default factory...
    super(context, DATABASE_FILE, null, DATABASE_VERSION);
  }
  
  /*! \brief Callback method (SQLiteOpenHelper), called if database 
   *         object has been created, but the database didn't exist yet.
   * 
   *  Creates the only table (#TABLE_PERSONS) of this database.
   * 
   *  \param db The newly created database.
   * */
  @Override
  public void onCreate(SQLiteDatabase db)
  {
    db.execSQL("create table " + TABLE_PERSONS + " ( " +
               Person.NUMBER + " TEXT PRIMARY KEY, " +
               Person.NAME + " TEXT, " +
               Person.AUTHORISED + " INTEGER, " +
               Person.SMS_COUNT + " INTEGER)");
  }
  
  /*! \brief Callback method (SQLiteOpenHelper), called if older version
   *         of this database has been detected (see #DATABASE_VERSION).
   * 
   *  This method can be used to extract old information and transfer it
   *  to the new database. (Currently, there are no other versions of this
   *  database; therefore the old database will be cleared and all tables
   *  will be recreated.)
   * 
   *  \param db The database object (used to access the database).
   *  \param oldversion old version number of the database that has been
   *         found.
   *  \param newversion new version number of this database.
   * */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion)
  {
    if (oldversion != newversion)
    {
      Log.d(TAG, String.format("db-version missmatch (old=%d, new=%d)", 
                               oldversion, newversion));
      
      db.execSQL("drop table if exists  " + TABLE_PERSONS);
      onCreate(db);
    }
  }
  
  /*! \brief Adds a person to the database.
   * 
   *  \param number The telephone number of the new person (primary key).
   *  \param name The name of the person.
   *  \param authorised Flag that indicates if this person is authorised
   *         to cause sending sms.
   *  \return Returns \c true if the insert has been successful, \c false
   *          on error.
   * */
  public boolean addPerson(String number, String name, boolean authorised)
  {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues content = new ContentValues();
    content.put(Person.NAME, name);
    content.put(Person.NUMBER, number);
    content.put(Person.AUTHORISED, getAuthorisedDBValue(authorised));
    content.put(Person.SMS_COUNT, 0);
    
    long ret = db.insert(TABLE_PERSONS, null, content);
    
    db.close();
    
    if (ret < 0)
    {
      Log.w(TAG, String.format("Couldn't insert data (number=%s, name=%s", number, name));
      return false;
    }
    else
    {
      Log.d(TAG, String.format("Successfully added name=%s, number=%s", name, number));
      return true;
    }
  }
  
  /*! \brief Queries all persons of this database.
   * 
   *  \note Note that you have to close the Cursor instance manually.
   *  
   *  \return Cursor object through which all persons can be read.
   * */
  public Cursor queryAllPersons()
  {
    SQLiteDatabase db = this.getReadableDatabase();
    return db.rawQuery("select rowid _id, * from " + TABLE_PERSONS, null);
  }
  
  /*! \brief Queries all authorised persons of this database.
   * 
   *  \return Cursor object through which all authorised persons can be
   *          read.
   * 
   *  \see queryAllPersons for more information on side effects.
   * */
  public Cursor queryAllAuthorisedPersons()
  {
    SQLiteDatabase db = this.getReadableDatabase();
    return db.rawQuery("select rowid _id, * from " + TABLE_PERSONS +
                       " where " + Person.AUTHORISED + " <> 0", null);
  }
  
  /*! \brief Removes a person from database.
   * 
   *  \param rowid id of the dataset that should be removed.
   * */
  public void deletePerson(long rowid)
  {
    SQLiteDatabase db = this.getWritableDatabase();
    int ret = db.delete(TABLE_PERSONS, "rowid = ?", 
                        new String[]{String.valueOf(rowid)});
    if (ret != 1)
    {
      Log.d(TAG, String.format("Deleting rowid %d returned %d", rowid, ret));
    }
    db.close();
  }
  
  /*! \brief Removes a person from database.
   * 
   *  \param number The number of the person that should be removed.
   * */
  public void deletePerson(String number)
  {
    SQLiteDatabase db = this.getWritableDatabase();
    int ret = db.delete(TABLE_PERSONS, Person.NUMBER + " = ?", 
                        new String[]{number});
    if (ret != 1)
    {
      Log.d(TAG, String.format("Deleting number %s returned %d", number, ret));
    }
    db.close();
  }
  
  /*! \brief Retrieves a person from the database.
   * 
   *  \param rowid The id of the dataset which has been requested.
   *  \return Returns the dataset or null if no dataset with \c rowid 
   *          exists.
   * */
  public Person getPersonFromId(long rowid)
  {
    Person person = null;
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery("select rowid, * from " + TABLE_PERSONS +
        " where rowid = ?", new String[]{String.valueOf(rowid)});
    
    if (cursor.moveToFirst())
    {
      person = toPerson(cursor);
    }
    
    cursor.close();
    db.close();
    return person;
  }
  
  /*! \brief Retrieves a person from the database.
   * 
   *  \param number The telephone number of the person which has been
   *         requested.
   *  \return Returns the dataset or null if not found.
   * */
  public Person getPersonFromNumber(String number)
  {
    Person person = null;
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery("select * from " + TABLE_PERSONS +
        " where " + Person.NUMBER + " = ?", new String[]{number});
    
    if (cursor.moveToFirst())
    {
      person = toPerson(cursor);
    }
    
    cursor.close();
    db.close();
    return person;
  }
  
  /*! \brief Checks, whether a person is authorised to cause sms
   *         to be sent.
   * 
   *  \param number The telephone number of the person to check.
   *  \return Returns \c true if the person could be found and is 
   *          marked \e authorised, else \c false.
   * */
  public boolean isAuthorisedNumber(String number)
  {
    boolean ret = false;
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery("select * from " + TABLE_PERSONS +
        " where " + Person.NUMBER + " = ?", new String[]{number});
    
    if (cursor.moveToFirst())
    {
      ret = isCurrentAuthorised(cursor);
    }
    
    cursor.close();
    db.close();
    return ret;
  }
  
  /*! \brief Increases (atom operation) the sms count of a person.
   * 
   *  \param number The telephone number of the person whos sms count
   *         will be increased.
   * */
  public void increaseSMSCount(String number)
  {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("update " + TABLE_PERSONS + 
          " set " + Person.SMS_COUNT + " = " + Person.SMS_COUNT + 
          " + 1 where " + Person.NUMBER + " = ?",
          new String[]{number});
    db.close();
  }
  
  /*! \brief Updates some properties of a person.
   * 
   *  \param person A person class that got all new properties that should
   *         be changed + the number to identify the person that
   *         should be changed...
   *  \param chname If \c true the name will be changed according to
   *         \c person.
   *  \param chauth If \c true the authorisation flag will be changed
   *         according to \c person.
   *  \param chsmscount If \c true the sms-count will be changed 
   *         according to \c person.
   * */
  public void updatePerson(Person person, boolean chname, boolean chauth, boolean chsmscount)
  {
    /* This method can't update the number...
     * chname = true      means update name
     * chauth = true      means update authorised state
     * chsmscount = true  means update sms-count
     * */
    
    if (chname || chauth || chsmscount)
    {
      ContentValues content = new ContentValues();
      
      if (chname)
        content.put(Person.NAME, person.name);
      
      if (chauth)
        content.put(Person.AUTHORISED, getAuthorisedDBValue(person.authorised));
      
      if (chsmscount)
        content.put(Person.SMS_COUNT, person.smscount);
      
      SQLiteDatabase db = this.getWritableDatabase();
      int ret = db.update(TABLE_PERSONS, content, Person.NUMBER + " = ?",
                          new String[]{person.number});
      
      if (ret != 1)
      {
        Log.d(TAG, String.format("updatePerson: number=%s; rows-affected=%d",
                                 person.number, ret));
      }
      
      db.close();
    }
    else
    {
      Log.d(TAG, "Update Person: nothing to update...");
    }
  }
  
  /*! \brief Helper method that checks if the cursor currently points to
   *         an authorised person.
   * 
   *  \note This method is intent only to be used internally. The \c cursor
   *        object will not be checked if it actually points to a 
   *        dataset.
   * 
   *  \param cursor The cursor to check.
   *  \return \c true if the first element of the cursor is authorised.
   * */
  protected static boolean isCurrentAuthorised(Cursor cursor)
  {
    int auth = cursor.getInt(cursor.getColumnIndex(Person.AUTHORISED));
    if (auth != 0)
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  /*! \brief Helper method that converts boolean to an integer.
   * 
   *  \note This method is only used internally.
   * 
   *  \param authorised Boolean value that should be converted to int.
   *  \return Returns an integer that represents \c authorised in the
   *          database.
   * */
  protected static int getAuthorisedDBValue(boolean authorised)
  {
    if (authorised)
      return 1;
    else
      return 0;
  }
  
  /*! \brief Converts the current dataset to an object of type \ref Person.
   * 
   *  \note Note that cursor has to point to a valid dataset.
   * 
   *  \param cursor The cursor that points to the dataset to convert.
   *  \return Returns an object (\ref Person) representing the dataset.
   * */
  public static Person toPerson(Cursor cursor)
  {
    Person person = new Person();
    person.name = cursor.getString(cursor.getColumnIndex(Person.NAME));
    person.number = cursor.getString(cursor.getColumnIndex(Person.NUMBER));
    person.authorised = isCurrentAuthorised(cursor);
    person.smscount = cursor.getInt(cursor.getColumnIndex(Person.SMS_COUNT));
    return person;
  }
  
}
