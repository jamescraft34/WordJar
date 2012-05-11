package com.craftysoft.wordjar;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AcceptedWordArrayAdapter extends WordArrayAdapter {

	private static final int _layoutId = R.layout.wordlist_approved_row;
	private AcceptedWord[] _acceptedWords = null;
	
	public AcceptedWordArrayAdapter(Context context, AcceptedWord[] objects) {
		super(context, _layoutId, objects);	
		
		_acceptedWords = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	
		View rowView = null;//convertView will hold a recycled view if we are lucky...
		
		//for performance - check to see if this view is being recycled so we dont need to recreate
		if(convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(_layoutId, parent, false);
	
			final ViewHolder vh = new ViewHolder();
			vh.col0 = (CustomFontTextView)rowView.findViewById(R.id.textViewWord);
			vh.col1 = (CustomFontTextView)rowView.findViewById(R.id.textViewCol1);
			vh.col2 = (CustomFontTextView)rowView.findViewById(R.id.textViewCol2);
	
			rowView.setTag(vh);
		}
		else
		{
			rowView = convertView;
		}
	
		rowView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				//CustomFontTextView tv = (CustomFontTextView) v.findViewById(R.id.textViewWord);
	
				//vibrate the phone
				v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
//				String h = r.getText().toString();
				
			}});
		
		AcceptedWord word = _acceptedWords[position];
		
		ViewHolder holder = (ViewHolder)rowView.getTag();		
		holder.col0.setText(word._word);
		holder.col1.setText(Integer.toString(word._todayCount));
		holder.col2.setText(Integer.toString(word.get_dailyGoal()));
		
		return rowView;
	}
	
	//use the view holder pattern for performance to reduce "findViewById" calls
	private static class ViewHolder
	{
		protected TextView col0 = null;//word
		protected TextView col1 = null;//today count
		protected TextView col2 = null;//goal count
	}
}
