package com.craftysoft.wordjar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CursorAdapter;
import android.widget.Toast;

import com.craftysoft.wordjar.db.DBConstants;
import com.craftysoft.wordjar.db.DBTasker;
import com.craftysoft.wordjar.db.DBTasker.InsertNewWordTask;


public abstract class BaseCursorAdapter extends CursorAdapter {

	private int _layoutId;

	protected Context _context = null;
	
	//we will use this to keep track of what row index is currently being created so we can determine if it needs a background color
	protected int _rowIndex = 0;
	
	protected OnClickListener _onClickListener = null;
	
	public void setRowOnClickListener(OnClickListener cl)
	{
		_onClickListener = cl;
	}
	
	//use the view holder pattern for performance to reduce "findViewById" calls
	protected static class ViewHolder
	{
		/*
		 * for now we will use a 3 column row of textviews
		 */
		protected CustomFontTextView col0 = null;
		protected CustomFontTextView col1 = null;
		protected CustomFontTextView col2 = null;
	}
	
	public BaseCursorAdapter(Context context, Cursor c, int layoutId) 
	{
		super(context, c);
		
		_context = context;
		
		_layoutId = layoutId;
	}
	
	
//	//used arrays so we can combine columns if needed
//	public void bindingView(View view, Cursor cursor, String[] column1, String[] column2) 
//	{
//		if((_rowIndex % 2) == 1)
//			view.setBackgroundResource(R.color.row_highlite);
//		else
//			view.setBackgroundResource(R.color.base_white);
//		
//		if(_onClickListener != null)
//			view.setOnClickListener(_onClickListener);
//		
//		String name = cursor.getString(cursor.getColumnIndex(DBConstants.WordTable.WORD_KEY));    
//
//		String column1Text = "";
//		for(int i = 0; i < column1.length;)
//		{
//			column1Text = column1Text + cursor.getString(cursor.getColumnIndex(column1[i]));
//			i++;
//			if(i < column1.length)//prevent adding separator to end of string....
//				column1Text = column1Text + _context.getResources().getString(R.string.col_separator);
//		}
//		
//		String column2Text = "";
//		for(int i = 0; i < column2.length;)
//		{
//			column2Text = column2Text + cursor.getString(cursor.getColumnIndex(column2[i]));
//			i++;
//			if(i < column2.length)//prevent adding separator to end of string....
//				column2Text = column2Text + _context.getResources().getString(R.string.col_separator);
//		}
//		
//		ViewHolder holder = (ViewHolder)view.getTag();		
//		holder.col0.setText(name);
//		holder.col1.setText(column1Text);
//		holder.col2.setText(column2Text);
//	}
	


	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup parent) 
	{	
		LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View rowView = inflater.inflate(_layoutId, parent, false);		
		
		final ViewHolder vh = new ViewHolder();
		vh.col0 = (CustomFontTextView)rowView.findViewById(R.id.textViewWord);
		vh.col1 = (CustomFontTextView)rowView.findViewById(R.id.textViewCol1);
		vh.col2 = (CustomFontTextView)rowView.findViewById(R.id.textViewCol2);

		rowView.setTag(vh);

		return rowView;
	}

	/*
	 * we are overriding this only to get at the position of the row so we can change it's background color
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		//save the current row index for later background coloring
		_rowIndex = position;

		return super.getView(position, convertView, parent);
	}
}
