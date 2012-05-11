package com.craftysoft.wordjar;

import com.craftysoft.wordjar.db.DBConstants;

public class AcceptedWord extends BaseWord {
	
	public AcceptedWord(String word) {
		super(word, DBConstants.WORDTYPE_ACCEPTED);
	}
	
	public void setDailyGoal(int goal)
	{
		_dailyGoal = goal;
	}
	
	public int get_dailyGoal()
	{
		return _dailyGoal;
	}

}
