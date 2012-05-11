package com.craftysoft.wordjar;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.craftysoft.wordjar.db.DBConstants;
import com.craftysoft.wordjar.db.DBTasker;
import com.craftysoft.wordjar.db.DBTasker.DeleteWordTask;

public class WordDetailActivity extends Activity {

	public static ProgressDialog _progressDialog = null;
	
	private BaseWord _word = null;
	
	private Button _bSave = null;
	private Button _bDelete = null;
	private Button _bCancel = null;
	private EditText _editTextDefinition = null;
	private EditText _editTextDailyGoal = null;
	private TextView _textViewWord = null;
	private ListView _listViewHistory = null;
	private TextView _textViewDailyGoal = null;
	private TextView _textViewSince = null;
	private ImageView _imageViewWordType = null;
	private TextView _textViewTodaysCount = null;
	private TextView _textViewTotalCount = null;
	
	//disabled text watcher
	private boolean _disableTextWatcher = true;
	
	private WordDateStatsArrayAdapter _wordDateStatsArrayAdapter = null;//adapter to bind to accepted listview

	
	//flag to signal data has changed so we know to save on exit
	private boolean _dataChanged = false;
	
	public WordDetailActivity() {
		// TODO Auto-generated constructor stub
	}
	
	//close the activity
	private void closeActivity()
	{
		finish();		
	}
	
	private TextWatcher textViewWatcher = new TextWatcher()
	{
		@Override
		public void afterTextChanged(Editable s) 
		{
			if(!_disableTextWatcher)
			{
				if((_editTextDefinition.getText().toString().trim().equals("")) &&
						(_editTextDailyGoal.getText().toString().trim().equals("")))
				{
					_bSave.setEnabled(false);
				}	
				else
					_bSave.setEnabled(true);				
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
		}
	};
	
	private TaskListener _deleteWordTaskListener = new TaskListener()
	{		
		@Override
		public void performAction(BaseWord word, Object obj) 
		{
			if(obj == null)
			{
				//let user know word was deleted
				//Toast.makeText(getApplicationContext(), "\"" + word.get_word() + "\" was deleted from your list.", Toast.LENGTH_SHORT).show();
				
				WordJarActivity.wordjarActivity.refreshUI(false);
				
				Intent intent = new Intent();
				intent.putExtra(WordDetailActivity.this.getResources().getString(R.string.namespace) + ".baseword", word);

				WordDetailActivity.this.setResult(WordJarActivity.WORD_ACTION_DELETED_CODE, intent);

				try {
					Thread.sleep(300L);//sleep just for a little so we don't pop back to activity while its refreshing lists
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				WordDetailActivity.this.finish();
			}
			else if(obj instanceof Exception)
			{
				Exception ex = (Exception)obj;
				Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	};

	private TaskListener _updateWordTaskListener = new TaskListener()
	{		
		@Override
		public void performAction(BaseWord word, Object obj) 
		{
			if(obj == null)
			{
				WordJarActivity.wordjarActivity.refreshUI(false);
				
				Intent intent = new Intent();
				intent.putExtra(WordDetailActivity.this.getResources().getString(R.string.namespace) + ".baseword", word);

				WordDetailActivity.this.setResult(WordJarActivity.WORD_ACTION_SAVED_CODE, intent);

				try {
					Thread.sleep(300L);//sleep just for a little so we don't pop back to activity while its refreshing lists
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				WordDetailActivity.this.finish();
			}
			else if(obj instanceof Exception)
			{
				Exception ex = (Exception)obj;
				Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	private TaskListener _getWordStatsTaskListener = new TaskListener()
	{		
		@Override
		public void performAction(BaseWord word, Object obj) 
		{
			ArrayList<WordDateStat> values = new ArrayList<WordDateStat>();
			
			//always generate the header
			View headerAccepted = getLayoutInflater().inflate(R.layout.word_stats_header, null);
			_listViewHistory.addHeaderView(headerAccepted);


			if(obj instanceof Exception)
			{
				Exception ex = (Exception)obj;
				Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
			}
			else
			{
				Cursor cursor = (Cursor)obj;									
				
				if(cursor.moveToFirst())
				{
					while (!cursor.isAfterLast()) 
					{
						WordDateStat ws = cursorToWordDateStat(cursor);
						
						values.add(ws);
						
						cursor.moveToNext();
					}				
				}
			}
			_wordDateStatsArrayAdapter = new WordDateStatsArrayAdapter(WordDetailActivity.this, values);
			
			// Assign adapter to ListView
			_listViewHistory.setAdapter(_wordDateStatsArrayAdapter);
		}
	};

	private WordDateStat cursorToWordDateStat(Cursor cursor)
	{
		return new WordDateStat(cursor.getString(DBConstants.StatsTable.DATE_INDEX),
											cursor.getInt(DBConstants.StatsTable.TOTAL_INDEX));
	}
		
	//sets the form data to the word displayed
	private void setFields()
	{
		_textViewWord.setText(_word.get_word());
		_editTextDefinition.setText(_word.get_definition());
		
		if(_word instanceof RejectedWord)
		{
			_editTextDailyGoal.setVisibility(View.GONE);
			_textViewDailyGoal.setVisibility(View.GONE);
		
			_imageViewWordType.setImageResource(BaseWord.WordType.REJECTED.ICONID);
		}
		else
		{
			if(_word._isDictionaryDotCom == 1)
				_imageViewWordType.setImageResource(BaseWord.WordType.DICTIONARY.ICONID);
			else
				_imageViewWordType.setImageResource(BaseWord.WordType.ACCEPTED.ICONID);

			_editTextDailyGoal.setVisibility(View.VISIBLE);
			_textViewDailyGoal.setVisibility(View.VISIBLE);
			
			int dg = _word._dailyGoal;
			if(dg > 0)
				_editTextDailyGoal.setText(Integer.toString(dg));
		}
		
    	_textViewTodaysCount.setText(Integer.toString(_word.get_todayCount()));
    	_textViewTotalCount.setText(Integer.toString(_word.get_totalCount()));

		
		_textViewSince.setText(_word.get_dateAdded());

		getWordStats(_word.get_id());
		
	}
	
	private void getWordStats(long id)
	{
		new DBTasker(getApplicationContext()).new GetWordDateStatsTask(_progressDialog, id, _getWordStatsTaskListener).execute();

	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Have the system blur any windows behind this one.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        setContentView(R.layout.word_details);
        
		//_progressDialog = new ProgressDialog(this);
        
        _word = (BaseWord)getIntent().getSerializableExtra(this.getResources().getString(R.string.namespace) + ".baseword"); 
        	
        _listViewHistory = (ListView)findViewById(R.id.listViewHistory);
        //_listViewHistory.setEmptyView(findViewById(R.id.emptyHistorylist));
        
        //set the button typefaces to a fun typeface
        _bSave = (Button)this.findViewById(R.id.buttonSave);
        _bSave.setTypeface(WordJarActivity.defaultFont);
        _bSave.setEnabled(false);
        _bSave.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) 
			{
				int dg = 0;
				String sdg = _editTextDailyGoal.getText().toString();
				if(!sdg.equals(""))
					dg = Integer.parseInt(sdg);
				
				_word._dailyGoal = dg;
				_word.set_definition(_editTextDefinition.getText().toString().trim());
				
            	//update word
				new DBTasker(getApplicationContext()).new UpdateWordDetailsTask(_progressDialog, _word, _updateWordTaskListener).execute();				
			}
		});
        
        _bDelete = (Button)this.findViewById(R.id.buttonDelete);
        _bDelete.setTypeface(WordJarActivity.defaultFont);
        _bDelete.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				 new AlertDialog.Builder(WordDetailActivity.this)
					.setIcon(getResources().getDrawable(R.drawable.action_droid))
	            	.setTitle("Are you sure you want to delete \"" + _word._word + "\"?") 
	            	.setPositiveButton("Ok", new DialogInterface.OnClickListener() { 
		                @Override 
		                public void onClick(DialogInterface dialog, int which) 
		                {
		                	//delete word
							new DBTasker(getApplicationContext()).new DeleteWordTask(_progressDialog, _word, _deleteWordTaskListener).execute();
		                } 
	            	}) 
	            	.setNegativeButton("Cancel", null) 
	            	.show(); 
				 }});
        
        _bCancel = (Button)this.findViewById(R.id.buttonCancel);
        _bCancel.setTypeface(WordJarActivity.defaultFont);
        _bCancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				closeActivity();
			}});
        
        _textViewWord = (TextView)findViewById(R.id.textViewWord);
        _textViewWord.setTypeface(WordJarActivity.defaultFont);
        
        _editTextDefinition = (EditText)findViewById(R.id.editTextDefinition);
        _editTextDefinition.setTypeface(WordJarActivity.defaultFont);
        _editTextDefinition.addTextChangedListener(textViewWatcher);        
        
        _editTextDailyGoal = (EditText)findViewById(R.id.editTextDailyGoal);
        _editTextDailyGoal.setTypeface(WordJarActivity.defaultFont);
        _editTextDailyGoal.addTextChangedListener(textViewWatcher);

        _textViewDailyGoal = (TextView)findViewById(R.id.textViewDailyGoal);
        
    	_textViewTodaysCount = (TextView)findViewById(R.id.textViewTodaysCount);
    	_textViewTotalCount = (TextView)findViewById(R.id.textViewTotalCount);

        _textViewSince = (TextView)findViewById(R.id.textViewSince);
        
        _imageViewWordType = (ImageView)findViewById(R.id.imageViewWordType);
        
        setFields();        
        
        _disableTextWatcher = false;
    }
}
