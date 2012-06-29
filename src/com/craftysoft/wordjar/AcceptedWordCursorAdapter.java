package com.craftysoft.wordjar;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.craftysoft.wordjar.db.DBConstants;

public final class AcceptedWordCursorAdapter extends BaseCursorAdapter {

	private final static int _layoutId  = R.layout.wordlist_approved_row;
	
	
	public AcceptedWordCursorAdapter(Context context, Cursor c) {
		super(context, c, _layoutId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) 
	{
//		super.bindingView(view, cursor, new String[]{DBConstants.WordTable.TODAYCOUNT_KEY, DBConstants.WordTable.DAILYGOALCOUNT_KEY}, 
//				new String[]{DBConstants.WordTable.TOTALCOUNT_KEY});
	
		if((_rowIndex % 2) == 1)
			view.setBackgroundResource(R.color.row_highlite);
		else
			view.setBackgroundResource(R.color.base_white);
		
				
		if(_onClickListener != null)
			view.setOnClickListener(_onClickListener);
		
		if(_onLongClickListener != null)
			view.setOnLongClickListener(_onLongClickListener);
		
		String column0Text = cursor.getString(DBConstants.WordTable.WORD_INDEX);    
		int todayCount = cursor.getInt(cursor.getColumnIndex(DBConstants.WordTable.TODAYCOUNT_KEY));
		int  dailyGoal = cursor.getInt(DBConstants.WordTable.DAILYGOALCOUNT_INDEX);

		
		boolean goalAchieved = false;
		
		if((dailyGoal > 0) && (todayCount >= dailyGoal))
			goalAchieved = true;
		
		String sDailyGoal = "-";//default in case daily goal was never set or 0
		if(dailyGoal > 0)
			sDailyGoal = Integer.toString(dailyGoal);		
		
		String column1Text = Integer.toString(todayCount) +
							 _context.getResources().getString(R.string.col_separator) + 
							 sDailyGoal;					
				
		String column2Text = Integer.toString(cursor.getInt(cursor.getColumnIndex(DBConstants.WordTable.TOTALCOUNT_KEY)));
		
		ViewHolder holder = (ViewHolder)view.getTag();		
		holder.col0.setText(column0Text);

		if(goalAchieved)
		{			
			//only play achievement when word in questions is in focus
			if(column0Text.equals(WordJarActivity.currentWord))
			{
				if(todayCount == dailyGoal)
					WordJarActivity.wordjarActivity.signalGoalAchievedFeedback();
			}
			
			holder.col1.setTextColor(_context.getResources().getColor(R.color.base_red));			
		}
		else
			holder.col1.setTextColor(_context.getResources().getColor(R.color.wordlist_text));
		
		holder.col1.setText(column1Text);
		
		holder.col2.setText(column2Text);
		
		//animate the word that was caught so the user can see
		//if(column0Text.equals(WordJarActivity.currentWord))		
		//	view.startAnimation(AnimationUtils.loadAnimation(_context, R.anim.wave));
		//crafty
	}
}
