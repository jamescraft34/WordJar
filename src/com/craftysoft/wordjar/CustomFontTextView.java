package com.craftysoft.wordjar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomFontTextView extends TextView {

	//com.craftysoft.wordjar.CustomFontTextView
	public CustomFontTextView(Context context) {
		super(context);
	
		init();
	}

	public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	public CustomFontTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	//lets set this textview to have a more attractive font
	private void init()
	{
		try {
			if(!isInEditMode())//this will allow eclipse to display correctly in the layouts
				this.setTypeface(WordJarActivity.defaultFont);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

}
