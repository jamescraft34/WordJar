package com.craftysoft.wordjar;

import com.craftysoft.wordjar.db.DBConstants;

public class RejectedWord extends BaseWord {
 
	public RejectedWord(String word) {
		super(word, DBConstants.WORDTYPE_REJECTED);
	}

}
