package com.craftysoft.wordjar;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;

import com.craftysoft.wordjar.db.DBConstants;

public class RejectedWordCursorAdapter extends BaseCursorAdapter {
	
	private final static int _layoutId  = R.layout.wordlist_rejected_row;

	public RejectedWordCursorAdapter(Context context, Cursor c) {
		super(context, c, _layoutId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) 
	{
		//super.bindingView(view, cursor, new String[]{DBConstants.WordTable.TODAYCOUNT_KEY}, new String[]{DBConstants.WordTable.TOTALCOUNT_KEY});
	
			if((_rowIndex % 2) == 1)
				view.setBackgroundResource(R.color.row_highlite);
			else
				view.setBackgroundResource(R.color.base_white);
			
			if(_onClickListener != null)
				view.setOnClickListener(_onClickListener);
			
			String column0Text = cursor.getString(DBConstants.WordTable.WORD_INDEX);    			
			String column1Text = Integer.toString(cursor.getInt(cursor.getColumnIndex(DBConstants.WordTable.TODAYCOUNT_KEY)));
			String column2Text = Integer.toString(cursor.getInt(cursor.getColumnIndex(DBConstants.WordTable.TOTALCOUNT_KEY)));

			ViewHolder holder = (ViewHolder)view.getTag();		
			holder.col0.setText(column0Text);
			holder.col1.setText(column1Text);
			holder.col2.setText(column2Text);
	}
}
