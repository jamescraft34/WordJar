package com.craftysoft.wordjar;

public class DictionaryWord extends AcceptedWord {

	public DictionaryWord(String word, String definition) {
		super(word);
		
		_definition = definition;
		_isDictionaryDotCom = 1;
	}

}
