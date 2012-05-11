package com.craftysoft.wordjar;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.craftysoft.wordjar.db.DBConstants;

/*
 * Base class for "word" objects
 */
public abstract class BaseWord  implements Serializable {

	private static final long[] _vibeRejected = {100,100,100,100,100,100,100,100};
	private static final long[] _vibeAccepted = {100,100};
	
	public enum WordType
	{
		REJECTED (DBConstants.WORDTYPE_REJECTED, 0, R.raw.buzzer, R.drawable.denied, _vibeRejected),
		ACCEPTED (DBConstants.WORDTYPE_ACCEPTED, 1, R.raw.shinyding, R.drawable.approved, _vibeAccepted),
		DICTIONARY (DBConstants.WORDTYPE_DICTIONARY, 2, R.raw.shinyding, R.drawable.wordnik_logo,_vibeAccepted);
		
		public long[] VIBEPATTERN;
		public int TYPEID;//id used for the type of word **these should not change as the database will contain these
		public int ALERTID;//Id for the alert that will sound for the type, this will be saved to the database
		public int ALERTRESOURCEID;//Id for the resource identifier, we can easily change these here later
		public int ICONID;//default icon used for the word type
				
		WordType(int id, int alertId, int rawId, int iconId, long[] vibepattern)
		{
			TYPEID = id;
			ALERTID = alertId;
			ALERTRESOURCEID = rawId;
			ICONID = iconId;
			VIBEPATTERN = vibepattern; 
		}		
	}
	
	protected String _word = null;
	protected int _todayCount = 0;//running total of times today word has been caught
	protected int _totalCount = 0;	//running total of times word has been caught
	protected long _id;//unique id given to each word in the database
	protected String _definition = "";//word definition (only populated when word is from Dictionary.com
	protected int _isDictionaryDotCom = 0;//is word taken from Dictionary.com
	public int _dailyGoal = 0;//daily goal for accepted words only
	protected int _alertId = WordType.ACCEPTED.ALERTRESOURCEID;//alert id to use for audible tone user will  hear when this word is caught (TODO: defaults for now)
	protected String _dateAdded = new SimpleDateFormat("MM/dd/yy").format(new Date());;//date the word was added to the list, used to reference stats
	protected int _typeId;//ACCEPTED OR REJECTED OR ETC... type
	protected long[] _vibePattern = WordType.ACCEPTED.VIBEPATTERN;

	
	public BaseWord(String word, int typeId)
	{
		_word = word;
		_typeId = typeId;
		
		//TODO:	set the alertid for now to be default
		if(_typeId == DBConstants.WORDTYPE_REJECTED){
			_alertId = WordType.REJECTED.ALERTRESOURCEID;
			_vibePattern = WordType.REJECTED.VIBEPATTERN;
		}
	}

	public long[] get_vibePattern() {
		return _vibePattern;
	}

	public void set_vibePattern(long[] vibePattern) {
		_vibePattern = vibePattern;
	}

	public int get_typeId() {
		return _typeId;
	}

	public String get_word() {
		return _word;
	}

	public int get_todayCount() {
		return _todayCount;
	}

	public void set_todayCount(int todayCount) {
		_todayCount = todayCount;
	}

	public int get_totalCount() {
		return _totalCount;
	}

	public void set_totalCount(int totalCount) {
		_totalCount = totalCount;
	}

	public long get_id() {
		return _id;
	}

	public void set_id(long id) {
		_id = id;
	}

	public String get_definition() {
		return _definition;
	}

	public void set_definition(String definition) {
		_definition = definition;
	}

	public void set_isDictionaryDotCom(int isDictionaryDotCom) {
		_isDictionaryDotCom = isDictionaryDotCom;
	}
	
	public int get_isDictionaryDotCom() {
		return _isDictionaryDotCom;
	}

	public int get_alertId() {
		return _alertId;
	}

	public void set_alertId(int alertId) {
		_alertId = alertId;
	}

	public String get_dateAdded() {
		return _dateAdded;
	}

	public void set_dateAdded(String dateAdded) {
		_dateAdded = dateAdded;
	}
}
