package com.craftysoft.wordjar.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DBAdapterBase {

	protected static final String DATABASE_NAME = "WordJarDB";
    protected static final int DATABASE_VERSION = 1;

    protected final Context _context;
    
    protected WordJarDBHelper _wordJarDBHelper = null;
    protected SQLiteDatabase _db = null;
    
    //set when db has been opened
    protected boolean _isOpen = false;
    
    protected static final String KEY_ID = "_id";//SQLite expects the unique ID field of each table to have this name

 /*
    private static final String TABLE_CREATE_WORDS = "create table if not exists " + DBConstants.WordTable.TABLENAME + 
    	" (" + KEY_ID + " integer primary key autoincrement, " + 
    	DBConstants.WordTable.TYPEID_KEY + " integer references " + DBConstants.WordTypeTable.TABLENAME + 
    		" (" + DBConstants.WordTypeTable.TYPEID_KEY + "), " +
    	DBConstants.WordTable.WORD_KEY + " text not null, " + 
    	DBConstants.WordTable.DEFINITION_KEY + " text, " + 
    	DBConstants.WordTable.ISDICTIONARY_KEY + " integer default 0, " + 
    	DBConstants.WordTable.DAILYGOALCOUNT_KEY + " integer default 0, " + 
    	DBConstants.WordTable.TOTALCOUNT_KEY + " integer default 0, " +
    	DBConstants.WordTable.DATEADDED_KEY + " text, " + 
    	DBConstants.WordTable.ALERTID_KEY + " integer references " + DBConstants.AlertTypeTable.TABLENAME + 
    		" (" + DBConstants.AlertTypeTable.TYPEID_KEY + "));";
    	
    private static final String TABLE_CREATE_WORDTYPES = "create table if not exists " + DBConstants.WordTypeTable.TABLENAME +
    	" (" + KEY_ID + " integer primary key autoincrement, " +
    	DBConstants.WordTypeTable.TYPEID_KEY + " integer not null);";
    
    private static final String TABLE_CREATE_ALERTTYPES = "create table if not exists " + DBConstants.AlertTypeTable.TABLENAME +
	" (" + KEY_ID + " integer primary key autoincrement, " +
	DBConstants.AlertTypeTable.TYPEID_KEY + " integer not null);";
*/
    
    private static final String TABLE_CREATE_WORDS = "create table if not exists " + DBConstants.WordTable.TABLENAME + 
	" (" + KEY_ID + " integer primary key autoincrement, " + 
	DBConstants.WordTable.TYPEID_KEY + " integer,  " + 
	DBConstants.WordTable.WORD_KEY + " text not null, " + 
	DBConstants.WordTable.DEFINITION_KEY + " text, " + 
	DBConstants.WordTable.ISDICTIONARY_KEY + " integer default 0, " + 
	DBConstants.WordTable.DAILYGOALCOUNT_KEY + " integer default 0, " + 
//	DBConstants.WordTable.TOTALCOUNT_KEY + " integer default 0, " +
	DBConstants.WordTable.DATEADDED_KEY + " text, " + 
	DBConstants.WordTable.ALERTID_KEY + " integer);";

//    private static final String TABLE_CREATE_DATESTATS = "create table if not exists " + DBConstants.StatsTable.TABLENAME +
//	" (" + KEY_ID + " integer primary key autoincrement, " +
//	DBConstants.StatsTable.WORDID_KEY + " integer not null, " +
//	DBConstants.StatsTable.DATE_KEY + " text not null, " + 
//	DBConstants.StatsTable.TOTAL_KEY + " integer default 0, " +
//	DBConstants.StatsTable.GOAL_KEY + " integer default 0);";
    
  
    
	
	public DBAdapterBase(Context context)
	{
		_context = context;
	}
	
    public DBAdapterBase open() throws SQLException 
    {
    	_wordJarDBHelper = new WordJarDBHelper(_context);
        _db = _wordJarDBHelper.getWritableDatabase();
        
        _isOpen = true;
        
        return this;
    }

    public void close() {
        
    	if(_db != null)
    	{
    		_db.close();
    		_isOpen = false;
    	}
    }

    //nested helper class that is responsible for creating/opening or updating the database
	protected class WordJarDBHelper extends SQLiteOpenHelper
	{

		public WordJarDBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			
			//create all the databases
//			db.execSQL(TABLE_CREATE_WORDTYPES);
//			db.execSQL(TABLE_CREATE_ALERTTYPES);
		//	db.execSQL(TABLE_CREATE_DATESTATS);
			db.execSQL(TABLE_CREATE_WORDS);
			
			//TODO: FILL TABLES WITH SAMPLE DATA
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}	
	}
}
