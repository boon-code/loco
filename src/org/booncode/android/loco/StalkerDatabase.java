package org.booncode.android.loco;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteConstraintException;


public class StalkerDatabase extends SQLiteOpenHelper
{
  public static final String DATABASE_FILE = "stalkerdb";
  public static final int DATABASE_VERSION = 1;
  
  public static final String TABLE_PERSONS = "persons";
  
  protected static final String TAG = "loco.StalkerDatabase";
  
  
  public static class Person
  {
    public static final String NAME = "name";
    public static final String NUMBER = "number";
    public static final String AUTHORISED = "auth";
    public static final String SMS_COUNT = "smscount";
    
    public String name;
    public String number;
    public boolean authorised;
    public int smscount;
  }
  
  
  public StalkerDatabase(Context context)
  {
    // null means use default factory...
    super(context, DATABASE_FILE, null, DATABASE_VERSION);
  }
  
  @Override
  public void onCreate(SQLiteDatabase db)
  {
    db.execSQL("create table " + TABLE_PERSONS + " ( " +
               Person.NUMBER + " TEXT PRIMARY KEY, " +
               Person.NAME + " TEXT, " +
               Person.AUTHORISED + " INTEGER, " +
               Person.SMS_COUNT + " INTEGER)");
  }
  
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
  
  public Cursor queryAllPersons()
  {
    SQLiteDatabase db = this.getReadableDatabase();
    return db.rawQuery("select rowid _id, * from " + TABLE_PERSONS, null);
  }
  
  public Cursor queryAllAuthorisedPersons()
  {
    SQLiteDatabase db = this.getReadableDatabase();
    return db.rawQuery("select rowid _id, * from " + TABLE_PERSONS +
                       " where " + Person.AUTHORISED + " <> 0", null);
  }
  
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
  
  public void increaseSMSCount(String number)
  {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("update " + TABLE_PERSONS + 
          " set " + Person.SMS_COUNT + " = " + Person.SMS_COUNT + 
          " + 1 where " + Person.NUMBER + " = ?",
          new String[]{number});
    db.close();
  }
  
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
  
  protected static int getAuthorisedDBValue(boolean authorised)
  {
    if (authorised)
      return 1;
    else
      return 0;
  }
  
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
