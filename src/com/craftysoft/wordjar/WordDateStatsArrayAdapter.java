package com.craftysoft.wordjar;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class WordDateStatsArrayAdapter extends ArrayAdapter<WordDateStat> {

	private static final int _layoutId = R.layout.word_stats_row;
	private ArrayList<WordDateStat> _wordStats = null;

	private Context _context = null;
	
	View[] viewRows = {null, null};
	
	public WordDateStatsArrayAdapter(Context context, ArrayList<WordDateStat> objects) {
		super(context, _layoutId, objects);

		_context = context;
		
		_wordStats = objects;
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
			vh.col0 = (CustomFontTextView)rowView.findViewById(R.id.textViewCol0);
			vh.col1 = (CustomFontTextView)rowView.findViewById(R.id.textViewCol1);
			vh.col2 = (CustomFontTextView)rowView.findViewById(R.id.textViewCol2);
	
			rowView.setTag(vh);
		}
		else
		{
			rowView = convertView;
		}
		
			
		WordDateStat stat = _wordStats.get(position);
		
		ViewHolder holder = (ViewHolder)rowView.getTag();		
		holder.col0.setText(stat.date);
//		holder.col1.setText("-");
		holder.col2.setText(Integer.toString(stat.total));
			
		return rowView;
	}

	//use the view holder pattern for performance to reduce "findViewById" calls
	private static class ViewHolder
	{
		protected TextView col0 = null;//stat
		protected TextView col1 = null;//stat
		protected TextView col2 = null;//stat
	}
}

