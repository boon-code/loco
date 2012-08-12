package org.booncode.android.locotest;

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
  public static final int DATABASE_VERSION = 4;
  
  public static final String TABLE_PERSONS = "persons";
  
  protected static final String TAG = "StalkerDatabase";
  
  
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
    if (authorised)
    {
      content.put(Person.AUTHORISED, 1);
    }
    else
    {
      content.put(Person.AUTHORISED, 0);
    }
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
  
  public void deletePerson(long rowid)
  {
    SQLiteDatabase db = this.getWritableDatabase();
    db.delete(TABLE_PERSONS, "rowid = ?", 
        new String[]{String.valueOf(rowid)});
    db.close();
  }
  
  public Person getPersonFromId(long rowid)
  {
    Person person = null;
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery("select rowid, * from " + TABLE_PERSONS +
        " WHERE rowid = ?", new String[]{String.valueOf(rowid)});
    
    if (cursor.moveToFirst())
    {
      person = new Person();
      person.name = cursor.getString(cursor.getColumnIndex(Person.NAME));
      person.number = cursor.getString(cursor.getColumnIndex(Person.NUMBER));
      int auth = cursor.getInt(cursor.getColumnIndex(Person.AUTHORISED));
      if (auth != 0)
      {
        person.authorised = true;
      }
      else
      {
        person.authorised = false;
      }
      person.smscount = cursor.getInt(cursor.getColumnIndex(Person.SMS_COUNT));
    }
    return person;
  }
  
  public boolean isAuthorisedNumber(String number)
  {
    /* TODO: Implement...
     * */
    return false;
  }
  
  public static Person toPerson(Cursor cursor)
  {
    Person person = new Person();
    person.name = cursor.getString(cursor.getColumnIndex(Person.NAME));
    person.number = cursor.getString(cursor.getColumnIndex(Person.NUMBER));
    int auth = cursor.getInt(cursor.getColumnIndex(Person.AUTHORISED));
    if (auth != 0)
    {
      person.authorised = true;
    }
    else
    {
      person.authorised = false;
    }
    person.smscount = cursor.getInt(cursor.getColumnIndex(Person.SMS_COUNT));
    return person;
  }
  
}
