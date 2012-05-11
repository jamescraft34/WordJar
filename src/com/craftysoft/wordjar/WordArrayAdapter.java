package com.craftysoft.wordjar;

import android.content.Context;
import android.widget.ArrayAdapter;

public abstract class WordArrayAdapter extends ArrayAdapter<BaseWord> {

	protected Context _context = null;
	
	public WordArrayAdapter(Context context, int layoutId, BaseWord[] objects) {
		super(context, layoutId, objects);
		
		_context = context;
	}



}
