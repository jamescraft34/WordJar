package com.craftysoft.wordjar.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * Creates or updates the WordJar database.
 * 
 * Should be called when WordJar is first created...
 */
public class DBAdapter {

	private WordJarDBHelper _wordJarDBHelper = null;
	private SQLiteDatabase _db = null;

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
	" (" + DBConstants.KEY_ID + " integer primary key autoincrement, " + 
	DBConstants.WordTable.TYPEID_KEY + " integer,  " + 
	DBConstants.WordTable.WORD_KEY + " text not null, " + 
	DBConstants.WordTable.DEFINITION_KEY + " text, " + 
	DBConstants.WordTable.ISDICTIONARY_KEY + " integer default 0, " + 
	DBConstants.WordTable.DAILYGOALCOUNT_KEY + " integer default 0, " + 
	//DBConstants.WordTable.TOTALCOUNT_KEY + " integer default 0, " +
	DBConstants.WordTable.DATEADDED_KEY + " text, " + 
	DBConstants.WordTable.ALERTID_KEY + " integer);";

//    private static final String TABLE_CREATE_DATESTATS = "create table if not exists " + DBConstants.DateStatsTable.TABLENAME +
//	" (" + DBConstants.KEY_ID + " integer primary key autoincrement, " +
//	DBConstants.DateStatsTable.WORDID_KEY + " integer not null, " +
//	DBConstants.DateStatsTable.DATE_KEY + " text not null, " + 
//	DBConstants.DateStatsTable.TOTAL_KEY + " integer default 0, " +
//	DBConstants.DateStatsTable.GOAL_KEY + " integer default 0);";
    
	public DBAdapter(Context context)
	{
    	_wordJarDBHelper = new WordJarDBHelper(context);
	}
	
    public void open() throws SQLException 
    {
        _db = _wordJarDBHelper.getWritableDatabase();
    }

    public void close() 
    {    
    	_wordJarDBHelper.close();
    }

    //nested helper class that is responsible for creating/opening or updating the database
	protected class WordJarDBHelper extends SQLiteOpenHelper
	{
		public WordJarDBHelper(Context context) {
			super(context, DBConstants.DATABASE_NAME, null, DBConstants.DATABASE_VERSION);
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