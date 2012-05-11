package com.craftysoft.wordjar.db;

public class DBConstants 
{
	private DBConstants()
	{
		//dont allow this class to be created
	}
	
	public static final String DATABASE_NAME = "WordJarDB";
	public static final int DATABASE_VERSION = 1;
	
	public static final int WORDTYPE_REJECTED = 0;
	public static final int WORDTYPE_ACCEPTED = 1;
	public static final int WORDTYPE_DICTIONARY = 2;

	public static final String KEY_ID = "_id";//SQLite expects the unique ID field of each table to have this name
	public static final int KEY_ID_INDEX = 0;

	
	public static class WordTable
	{
		//table name
	    public static final String TABLENAME = "words";
	    
	    public static final String WORDID_KEY = KEY_ID;
	    public static final int WORDID_INDEX = KEY_ID_INDEX;
	    
	    public static final String TYPEID_KEY = "typeId";
	    public static final int TYPEID_INDEX = 1;
	    
	    public static final String WORD_KEY = "word";
	    public static final int WORD_INDEX = 2;
	    
	    public static final String DEFINITION_KEY = "definition";
	    public static final int DEFINITION_INDEX = 3;
	    
	    public static final String ISDICTIONARY_KEY = "isDictionaryDotCom";
	    public static final int ISDICTIONARY_INDEX = 4;
	    
	    public static final String DAILYGOALCOUNT_KEY = "dailyGoalCount";
	    public static final int DAILYGOALCOUNT_INDEX = 5;
	    	    
	    public static final String DATEADDED_KEY = "dateAdded";
	    public static final int DATEADDED_INDEX = 6;
	    
	    public static final String ALERTID_KEY = "alertId";
	    public static final int ALERTID_INDEX = 7;

	    //these fields are not in the database but are generated through queries and are always a part of selected data  from the word table
	    public static final String TOTALCOUNT_KEY = "totalCount";
	    public static final String TODAYCOUNT_KEY = "todayCount";

//	    public static final String TOTALCOUNT_KEY = "totalCount";
//	    public static final int TOTALCOUNT_INDEX = 6;
	    
//	    public static final String TODAYCOUNT_KEY = "todayCount";
//	    public static final int TODAYCOUNT_INDEX = 7;
	}
	
	public static class WordTypeTable
	{
	    public static final String TABLENAME = "wordType";
	    
	    public static final String TYPEID_KEY = "typeId";
	    public static final int TYPEID_INDEX = 1;	    
	}
	
	public static class AlertTypeTable
	{
	    public static final String TABLENAME = "alertType";
	    
	    public static final String TYPEID_KEY = "typeId";
	    public static final int TYPEID_INDEX = 1;	    
	}
	
	public static class StatsTable
	{
	    public static final String TABLENAME = "stats";
	    
	    public static final String ID_KEY = KEY_ID;
	    public static final int ID_INDEX = KEY_ID_INDEX;
	    
	    public static final String WORDID_KEY = "wordId";
	    public static final int WORDID_INDEX = 1;
	    
	    public static final String DATE_KEY = "date";
	    public static final int DATE_INDEX = 2;
	    
	    public static final String TOTAL_KEY = "total";
	    public static final int TOTAL_INDEX = 3;
	    
	    //public static final String GOAL_KEY = "goal";
	    //public static final int GOAL_INDEX = 4;
	}

}
