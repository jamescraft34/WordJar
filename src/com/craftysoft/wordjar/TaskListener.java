package com.craftysoft.wordjar;


public interface TaskListener {

	/*
	 * BaseWord = word we are performing the action on. NULL allowed
	 * Object = can be cursor, can be exception, etc...
	 */
	void performAction(BaseWord word, Object obj);
}
