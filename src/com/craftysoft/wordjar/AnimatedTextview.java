package com.craftysoft.wordjar;

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

public class AnimatedTextview extends TextView {
	
	private final int _animationDuration = 2000;
	
	private final int _defaultTextColor = R.color.base_black;
	

	public AnimatedTextview(Context context, String text, float xAxis, float yAxis) 
	{
		super(context);
		
		init(context, text, xAxis, yAxis, _defaultTextColor);
	}

	
	public AnimatedTextview(Context context, String text, float xAxis, float yAxis, int color) {
		super(context);

		init(context, text, xAxis, yAxis, color);		
	}
	
	private void init(Context context, String text, float xAxis, float yAxis, int color)
	{
		this.setText(text);
		this.setTextSize(18f);
		this.setTextColor(color);		
		
		AnimationSet as = new AnimationSet(false);
		as.setFillAfter(true);
					
		float textWidth = (this.getPaint().measureText(text)) / 2;

		TranslateAnimation ta = new TranslateAnimation(xAxis - textWidth, xAxis - textWidth, yAxis, yAxis - 50f);
		ta.setDuration(_animationDuration);
						
		AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
		aa.setDuration(_animationDuration);
						
		as.addAnimation(ta);
		as.addAnimation(aa);
												
		this.setAnimation(as);
	}
}
