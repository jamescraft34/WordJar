	package com.craftysoft.wordjar.db;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.craftysoft.wordjar.BaseWord;

public class WordDBAdapter {

    private DatabaseHelper _dbHelper; 
    private SQLiteDatabase _db; 
 
    private final Context _context; 

    private String _todaysCountQuery = "(SELECT " + DBConstants.StatsTable.TOTAL_KEY + " FROM " + DBConstants.StatsTable.TABLENAME + 	
	" WHERE " + DBConstants.StatsTable.DATE_KEY + " = '%s'  AND " + DBConstants.WordTable.TABLENAME + "." + DBConstants.WordTable.WORDID_KEY + 
	" = "  + DBConstants.StatsTable.WORDID_KEY + ") AS " + DBConstants.WordTable.TODAYCOUNT_KEY;
    
    private String _select = "SELECT "  + DBConstants.WordTable.TABLENAME + 
    	".*, sum(s." + DBConstants.StatsTable.TOTAL_KEY + ") AS " + DBConstants.WordTable.TOTALCOUNT_KEY +
    	", " + _todaysCountQuery +
    	" FROM " + DBConstants.WordTable.TABLENAME +
    	" LEFT OUTER JOIN " + DBConstants.StatsTable.TABLENAME + " AS s ON " + DBConstants.WordTable.TABLENAME + "." + 
    	DBConstants.WordTable.WORDID_KEY + " = s." + DBConstants.StatsTable.WORDID_KEY;    
    
    private String _havingDataContraint = " GROUP BY " + DBConstants.WordTable.WORD_KEY + " HAVING COUNT(" + DBConstants.WordTable.WORD_KEY + ") > 0";
    
    
    private static final String TABLE_CREATE_WORDS = "create table if not exists " + DBConstants.WordTable.TABLENAME + 
	" (" + DBConstants.KEY_ID + " integer primary key autoincrement, " + 
	DBConstants.WordTable.TYPEID_KEY + " integer,  " + 
	DBConstants.WordTable.WORD_KEY + " text not null unique, " + 
	DBConstants.WordTable.DEFINITION_KEY + " text, " + 
	DBConstants.WordTable.ISDICTIONARY_KEY + " integer default 0, " + 
	DBConstants.WordTable.DAILYGOALCOUNT_KEY + " integer default 0, " + 
	DBConstants.WordTable.DATEADDED_KEY + " text, " + 
	DBConstants.WordTable.ALERTID_KEY + " integer);";
	
//    private static final String TABLE_CREATE_STATS = "create table if not exists " + DBConstants.StatsTable.TABLENAME + 
//	" (" + DBConstants.KEY_ID + " integer primary key autoincrement, " + 
//	DBConstants.StatsTable.WORDID_KEY + " integer,  " + 
//	DBConstants.StatsTable.DATE_KEY + " text not null, " + 
//	DBConstants.StatsTable.TOTAL_KEY + " integer default 0);";
  
//    private static final String TABLE_CREATE_STATS = "create table if not exists " + DBConstants.StatsTable.TABLENAME + 
//	" (" + DBConstants.KEY_ID + " integer, " + 
//	DBConstants.StatsTable.WORDID_KEY + " integer,  " + 
//	DBConstants.StatsTable.DATE_KEY + " text not null, " + 
//	DBConstants.StatsTable.TOTAL_KEY + " integer default 0, PRIMARY KEY ("  + DBConstants.KEY_ID + "," + DBConstants.StatsTable.WORDID_KEY + "," + 
//	DBConstants.StatsTable.DATE_KEY + "));";
    
    private static final String TABLE_CREATE_STATS = "create table if not exists " + DBConstants.StatsTable.TABLENAME + 
	" (" + DBConstants.KEY_ID + " integer primary key autoincrement, " + 
	DBConstants.StatsTable.WORDID_KEY + " integer,  " + 
	DBConstants.StatsTable.DATE_KEY + " text not null, " + 
	DBConstants.StatsTable.TOTAL_KEY + " integer default 0, UNIQUE (" + DBConstants.StatsTable.WORDID_KEY + "," + 
	DBConstants.StatsTable.DATE_KEY + "));";

    private static WordDBAdapter wdba = null;
    
	private WordDBAdapter(Context context) {
		_context = context;
	}
	
	//is there an instance of this
	public static boolean hasInstance()
	{
    	if(wdba == null)
    		return false;
    	else
    		return true;
	}
	
	public static WordDBAdapter getInstance(Context context)
	{
    	if(wdba == null)
    		wdba = new WordDBAdapter(context);
		
		return wdba;
	}
	
    public WordDBAdapter open() throws SQLException 
    { 
    	if(_dbHelper == null)
    	{
    		_dbHelper = new DatabaseHelper(_context); 
    		_db = _dbHelper.getWritableDatabase(); 
    	}
        return this; 
    } 
 
    public void close() 
    { 
    	//_db.close();
       if(_dbHelper != null)
       {
    	   _dbHelper.close(); 
    	   _dbHelper = null;
    	   
    	   _db.close();
    	   _db = null;
       }
    } 

	 
    private static class DatabaseHelper extends SQLiteOpenHelper { 
 
        DatabaseHelper(Context context) 
        { 
            super(context, DBConstants.DATABASE_NAME, null, DBConstants.DATABASE_VERSION); 
        } 
 
        @Override 
        public void onCreate(SQLiteDatabase db) 
        {
			db.execSQL(TABLE_CREATE_WORDS);
			try {
				db.execSQL(TABLE_CREATE_STATS);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } 
 
        @Override 
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
        
    		db.execSQL("DROP TABLE IF EXISTS" + DBConstants.WordTable.TABLENAME);
    		db.execSQL("DROP TABLE IF EXISTS" + DBConstants.StatsTable.TABLENAME);

    		onCreate(db);
        } 
    } 

    private String getDate()
    {
    	return new SimpleDateFormat("MM/dd/yy").format(new Date());
    }
    
    
    public void updateIncrementWordTotal(BaseWord word)
    {
     	String query  = "INSERT OR REPLACE INTO " + DBConstants.StatsTable.TABLENAME  
     	+ " (" + DBConstants.StatsTable.WORDID_KEY + "," + DBConstants.StatsTable.DATE_KEY + "," 
     	+ DBConstants.StatsTable.TOTAL_KEY + ") VALUES " 
     	+ " (" + word.get_id() + ",'" + getDate() + "', coalesce((select " + DBConstants.StatsTable.TOTAL_KEY +
     	" FROM " + DBConstants.StatsTable.TABLENAME + " WHERE " + DBConstants.StatsTable.WORDID_KEY + " = " + word.get_id()  + 
     	" AND " + DBConstants.StatsTable.DATE_KEY + " = '" + getDate() + "'),0) + 1)";
     	
    	_db.execSQL(query);
    }
    						
	//Get all words from Words table
	public Cursor getWords() 
	{
		String query = String.format(_select, getDate()) +
						_havingDataContraint +
						" ORDER BY " + DBConstants.WordTable.WORD_KEY;
		
		return _db.rawQuery(query, null);	
    }
    
	//get all Accepted words from Words table
	public Cursor getAcceptedWords()
	{
		return getWordsByType(DBConstants.WORDTYPE_ACCEPTED);
	}
	
	//get all Rejected Words from Words table
	public Cursor getRejectedWords()
	{
		return getWordsByType(DBConstants.WORDTYPE_REJECTED);
	}
	
	 //Get all words from Words table by the given type id
	private Cursor getWordsByType(int typeId) {
	
		String query = String.format(_select, getDate()) +							  
						" WHERE " + DBConstants.WordTable.TYPEID_KEY + " = " + typeId +
						_havingDataContraint + 
						" ORDER BY " + DBConstants.WordTable.WORD_KEY;
			
		return _db.rawQuery(query, null);
    }
	
	 //Get word from Words table
	public Cursor getWord(String word) {

		String query = String.format(_select, getDate()) +
						" WHERE " + DBConstants.WordTable.WORD_KEY + " = '" + word + "'";
		
		return _db.rawQuery(query, null);		
    }
	
	//get all date stats of a word by wordId
	public Cursor getWordDateStats(long wordId) {

		String query = "SELECT *" +
			" FROM " + DBConstants.StatsTable.TABLENAME +
			" WHERE " + DBConstants.StatsTable.WORDID_KEY + " = " + wordId +
			" AND " + DBConstants.StatsTable.DATE_KEY + " != '" + getDate() + "'" +
//			" AND " + DBConstants.StatsTable.TOTAL_KEY + " > 0" +
			" ORDER BY " + DBConstants.StatsTable.DATE_KEY + " DESC";

//		String query = "SELECT *" +
//		" FROM " + DBConstants.StatsTable.TABLENAME +
//		" WHERE " + DBConstants.StatsTable.WORDID_KEY + " = " + wordId +
//		" AND " + DBConstants.StatsTable.DATE_KEY + " != '03/06/12'" +
//		" ORDER BY " + DBConstants.StatsTable.DATE_KEY;

		return _db.rawQuery(query, null);		
    }

	public int deleteWord(BaseWord word)
	{
		//delete word stats
		_db.delete(DBConstants.StatsTable.TABLENAME, DBConstants.StatsTable.WORDID_KEY + " = " + word.get_id(), null);

		//delete word
		return _db.delete(DBConstants.WordTable.TABLENAME, DBConstants.WordTable.WORD_KEY + " = '" + word.get_word() + "'", null);
	}
	
	public int deleteAllWords()
	{
		//delete all word stats
		_db.delete(DBConstants.StatsTable.TABLENAME, null, null);

		//delete all words
		return _db.delete(DBConstants.WordTable.TABLENAME, null, null);
	}

	
	public int resetAllWords()
	{
		//delete all word stats
		_db.delete(DBConstants.StatsTable.TABLENAME, null, null);

		ContentValues values = new ContentValues();
		values.put(DBConstants.WordTable.DAILYGOALCOUNT_KEY, 0);
	
		//reset word data  //commented out to set the daily goal back to 0 below...
		return 0;//_db.update(DBConstants.WordTable.TABLENAME, values, null, null);		
	}

	//ONLY UPDATES NOTE AND DAILY GOAL FOR NOW....
	public int updateWordDetails(BaseWord word)
	{
		ContentValues values = new ContentValues();
		values.put(DBConstants.WordTable.DAILYGOALCOUNT_KEY, word._dailyGoal);
		values.put(DBConstants.WordTable.DEFINITION_KEY, word.get_definition());
		
		//update word data
		return _db.update(DBConstants.WordTable.TABLENAME, values, DBConstants.WordTable.WORDID_KEY + " = " + word.get_id(), null);		
	}

	
	public long insertNewWord(BaseWord word)
	{
		ContentValues values = new ContentValues();
		values.put(DBConstants.WordTable.TYPEID_KEY, word.get_typeId());
		values.put(DBConstants.WordTable.WORD_KEY, word.get_word());
		values.put(DBConstants.WordTable.DEFINITION_KEY, word.get_definition());
		values.put(DBConstants.WordTable.ISDICTIONARY_KEY, word.get_isDictionaryDotCom());
		values.put(DBConstants.WordTable.DAILYGOALCOUNT_KEY, 0);
		values.put(DBConstants.WordTable.DATEADDED_KEY, getDate());
		values.put(DBConstants.WordTable.ALERTID_KEY, word.get_alertId());//TODO: allow this to be set, default for now
		
		return _db.insertOrThrow(DBConstants.WordTable.TABLENAME, null, values);
	}
}
