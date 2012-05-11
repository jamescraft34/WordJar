package com.craftysoft.wordjar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import junit.framework.Assert;
import net.jeremybrooks.knicker.WordApi;
import net.jeremybrooks.knicker.WordsApi;
import net.jeremybrooks.knicker.dto.Definition;
import net.jeremybrooks.knicker.dto.Word;
import net.jeremybrooks.knicker.dto.WordOfTheDay;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.Toast;

import com.craftysoft.wordjar.BaseCursorAdapter.ViewHolder;
import com.craftysoft.wordjar.BaseWord.WordType;
import com.craftysoft.wordjar.BluetoothHandler.BluetoothNotAvailableException;
import com.craftysoft.wordjar.BluetoothHandler.BluetoothNotEnabledException;
import com.craftysoft.wordjar.db.DBConstants;
import com.craftysoft.wordjar.db.DBTasker;
import com.craftysoft.wordjar.db.WordDBAdapter;

public class WordJarActivity extends TabActivity implements OnClickListener, OnTabChangeListener  {
	
//	private Queue<ArrayList<String>> _listenedWordsQueue = (Queue<ArrayList<String>>) Collections.synchronizedList(new LinkedList<ArrayList<String>>());
	//private Queue<ArrayList<String>> _listenedWordsQueue = new LinkedList<ArrayList<String>>();
	private Queue<UserFeedback> _feedbackQueue = new LinkedList<UserFeedback>();//feedback to signal to user
	
	private SpeechRecognizer _sr = null;
	private Intent _speechIntent = null;
	
	private boolean _isListening = false;
	private Vibrator _vibe = null;
		
	public static WordJarActivity wordjarActivity = null;
	
	private int _currentTabIndex = 0;
	private static final int TABINDEX_ACCEPTED = 0;
	private static final int TABINDEX_REJECTED = 1;
	private static final int TABINDEX_DICTIONARYDOTCOM = 2;
	
	private boolean _vibsOn = true;
	private boolean _alertsOn = true;
		
	private TabHost _tabHost = null;
	
	private FrameLayout _frameLayoutFooter = null;
	
	//TODO: THESE WILL HOLD THE MEDIAPLAYER SOUNDS.  USING MEDIA PLAYER SO WE CAN  PLAY SOUNDS
	//SEQUENTIALLY. THIS IS NOT THE BEST WAY TO LOAD THE SOUNDS, LATER I SHOULD CREATE A LOOSE COUPLED 
	//WAY TO LOAD SOUNDS BASED ON AVAILABLE WORDTYPES....
	private SoundPlayer _spAccept = null; 
	private SoundPlayer _spReject = null;
	private SoundPlayer _spGoalReached = null;
	private final int GOALREACHEDSOUNDID = 999;
	
	private LinearLayout _linearLayoutHeaderRoot = null;
	
	//overriding font to use for all views
	public static Typeface defaultFont = null;
	private static final String FONT_TYPE = "fonts/arcena.ttf";

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;
	private static final int BLUETOOTH_REQUEST_CODE = 2;
	private static final int WORD_DETAIL_CODE = 3;
	
	public static final int WORD_ACTION_SAVED_CODE = 100;
	public static final int WORD_ACTION_DELETED_CODE = 101;
	public static final int PREFERENCE_CODE = 102;
	
	private int _errorListeningAttempts = 0;
	private final int MAX_ERROR_ATTEMPTS= 3;
	
	private SharedPreferences _pref = null;

	private ImageButton _wordOfDayButton = null;
	private ImageButton _randomWordButton = null;
	
	private ImageButton _speakButton = null;
	private ImageButton _listenButton = null;
	private ListView _acceptedListView = null;
	private ListView _rejectedListView = null;
	
	private AudioManager _audioManager = null;
		
	private BluetoothHandler _bluetoothHandler = null;
	private boolean _bluetoothAvailable = true;
	
	private MediaPlayer mPlayer = null;
	
	//private MasterSoundEffects _masterSoundEffects = null;
	
	public static ProgressDialog _progressDialog = null;
	
	/*
	 * Master word map to compare heard words to when Wordjar is listening
	 * Initially populate within onCreate() from database, then manually add new words during runtime
	 */
	private HashMap<String, BaseWord> _masterWordMap = null; 
	private AcceptedWordCursorAdapter _acceptedWordCursorAdapter = null;//accepted word adapter to bind to accepted listview
	private RejectedWordCursorAdapter _rejectedWordCursorAdapter = null;//rejected word adapter to bind to rejected listview
	private Cursor _acceptedWordsCursor = null;
	private Cursor _rejectedWordsCursor = null;		
	
	private enum WordnikApiCall
	{
		WordOfDay,
		RandomWord
	} 
	
	private class UserFeedback
	{
		private int _alertId;
		private long[] _vibePattern;
		public UserFeedback(int alertId, long[] vibePattern)
		{
			_alertId = alertId;
			_vibePattern = vibePattern;
		}
	}

	private class WordnikAsyncTask extends AsyncTask<WordnikApiCall, Exception, WordnikResult>
	{
		private boolean _errorOccured = false;
		private Context _context = null;
		
		public WordnikAsyncTask(Context context)
		{
			_context = context;
		}
		
		@Override
		protected WordnikResult doInBackground(WordnikApiCall... params) 
		{
			try 
			{
				List<Definition> defs = null;
				Definition def = null;
				String definition = "";
				
				switch(params[0])
				{
					case WordOfDay:
						WordOfTheDay wod = WordsApi.wordOfTheDay();
						
						defs = wod.getDefinitions();
						if(defs.size() > 0)
						{
							def = defs.get(0);//just grab the first definition
							definition = def.getText();
						}
						
						return new WordnikResult(wod.getWord(), definition);

					case RandomWord:
						Word rw = WordsApi.randomWord();
						defs = WordApi.definitions(rw.getWord());			

						if(defs.size() > 0)
						{
							def = defs.get(0);//just grab the first definition
							definition = def.getText();
						}
						
						return new WordnikResult(rw.getWord(), definition);
				}
			} 
			catch (Exception e) 
			{
				_errorOccured = true;
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(WordnikResult result) 
		{
			if(_progressDialog.isShowing())
				_progressDialog.dismiss();
						
			if(_errorOccured)
				Toast.makeText(_context, _context.getResources().getString(R.string.wordnik_error), Toast.LENGTH_SHORT).show();
			else
				showWordnikResults(result);
		}

		@Override
		protected void onPreExecute() 
		{
			if(!_progressDialog.isShowing())
			{
				_progressDialog.setMessage("Talking to Wordnik.com...");
				_progressDialog.show();
			}
		}
	}

	/*
	 * Analyzes the listened speech and updates the database as appropriate
	 */
//	private class SpeechAnalyzer extends AsyncTask<Queue<ArrayList<String>>, BaseWord, Void> 
	private class SpeechAnalyzer extends AsyncTask<ArrayList<String>, BaseWord, Void>
	{
		HashMap<String, BaseWord> _masterWordMap = null;

		public SpeechAnalyzer(HashMap<String, BaseWord> masterWordMap)
		{
			_masterWordMap = masterWordMap;	
		}		

//		@Override
//		protected Void doInBackground(Queue<ArrayList<String>>... queues) 
//		{
//			Assert.assertTrue(queues.length == 1);
//			
//			Queue<ArrayList<String>> queue = (Queue<ArrayList<String>>)queues[0];
//			
//			analyzeSpeech(queue.poll());
//			
//			return null;
//		}
		
		@Override
		protected Void doInBackground(ArrayList<String>... queues) 
		{
			Assert.assertTrue(queues.length == 1);
			
			ArrayList<String> queue = (ArrayList<String>)queues[0];
			
			analyzeSpeech(queue);
			
			return null;
		}

				
	    @Override
		protected void onProgressUpdate(BaseWord... baseWords) {

	    	Assert.assertTrue(baseWords.length == 1);
	    	
//	    	signalFeedbackToUser(baseWords[0]);
	    	BaseWord bw = baseWords[0];
	    	signalFeedbackToUser(new UserFeedback(bw.get_typeId(), bw.get_vibePattern()));    	


	    	//display found word to user
	    	//_linearLayoutHeaderRoot.addView(new AnimatedTextview(getApplicationContext(), bw.get_word(), 20, 10));
	    	
	    	//add to database here...
			new DBTasker(getApplicationContext()).new UpdateIncrementWordTotalTask(null, bw, _updateIncrementWordTotalTaskListener).execute();

			//update the listview cursors to show any new data changes
//			if(bw.get_typeId() == WordType.REJECTED.TYPEID)
//				new DBTasker(getApplicationContext()).new GetRejectedWordsTask(null, _getRejectedWordsTaskListener).execute();
//			else//everything else is an accepted word
//				new DBTasker(getApplicationContext()).new GetAcceptedWordsTask(null, _getAcceptedWordsTaskListener).execute();
		}

		private void analyzeSpeech(ArrayList<String> speechList)
	    {
	    	if(speechList == null)
	    		return;
	    	
	 		String item = (String)speechList.get(0);//grab first interpretation speech comes up with and hope it was accurate!!
			String[] words = item.split(" ");//we must separate the individual words
			
			for(String word : words)
			{
				Log.i("WordJar", word);
				
	    		if(_masterWordMap.containsKey(word))
	    		{
	    			BaseWord bw = (BaseWord)_masterWordMap.get(word);
	    			
	    			publishProgress(bw);	    			
	    		}
			}
	    }
	}
	
	private void showWordnikResults(final WordnikResult result)
	{
		final Dialog wordnikDialog = new Dialog(this, R.style.Theme_FloatingActivity);
		wordnikDialog.setContentView(R.layout.wordnik_details);
		wordnikDialog.setCancelable(true);
		
		TextView tvDefinition = (TextView)wordnikDialog.findViewById(R.id.textViewDef);
		tvDefinition.setText(result.definition);
		tvDefinition.setTypeface(defaultFont);
		tvDefinition.setMovementMethod(new ScrollingMovementMethod());

		TextView tvWord = (TextView)wordnikDialog.findViewById(R.id.textViewWordNikWord);
		tvWord.setTypeface(defaultFont);
		tvWord.setText(result.word);
		
		Button save = (Button)wordnikDialog.findViewById(R.id.buttonSave);
		save.setTypeface(defaultFont);
		save.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new DBTasker(getApplicationContext()).new InsertNewWordTask(_progressDialog, new DictionaryWord(result.word, result.definition), _insertWordTaskListener).execute();
				wordnikDialog.dismiss();

			}});

		Button cancel = (Button)wordnikDialog.findViewById(R.id.buttonCancel);
		cancel.setTypeface(defaultFont);
		cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				wordnikDialog.dismiss();
			}});
		
		wordnikDialog.show();
	}

	public void signalGoalAchievedFeedback()
	{
		if(_isListening)//this will block playing the sound when the cursor is updating the listview when we are not listening...
			signalFeedbackToUser(new UserFeedback(GOALREACHEDSOUNDID, null));
	}
	
	private void signalFeedbackToUser(UserFeedback fb)
	{
		//feedback will be based on the sound signal since we can monitor when it has completed in order to play the next sound
		if(SoundPlayer.ISPLAYING)
		{
			putFeedbackInQueue(new UserFeedback(fb._alertId, fb._vibePattern));
		}
		else
		{
			//playSound(bw.get_alertId());
			playSoundEffect(fb._alertId);
			vibratePhonePattern(fb._vibePattern);
		}
	}
	
	private void vibratePhonePattern(long[] vibratePattern) {
		if((!_vibsOn) || (vibratePattern == null))
			return;
		
		try {
			if (_vibe == null)
				_vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

			_vibe.vibrate(vibratePattern, -1);
		} catch (Exception ex) {
			// do nothing
		}
	}
	
	//if a sound effect is already playing store next soundId in queue to play later
	private void putFeedbackInQueue(UserFeedback feedback)
	{
		_feedbackQueue.add(feedback);
	}
	
	private void playSoundEffect(int id)
	{	
		try 
		{
			if(id == WordType.REJECTED.TYPEID)
				_spReject.play(_alertsOn);
			else if(id == WordType.ACCEPTED.TYPEID)
				_spAccept.play(_alertsOn);
			else if(id == GOALREACHEDSOUNDID)
				_spGoalReached.play(_alertsOn);	
		} catch (Exception e) 
		{
			//do nothing
		}	
	}

	
//	private void playSound(int alertId)
//	{
//		if(_alertsOff)
//			return;
//		
//		try {
////			_masterSoundEffects.playWordSound(alertId);
//			
//			SoundPlayer sp = new SoundPlayer(this, alertId, null);
//			sp.play();
//			
//			
//		} catch (Exception e) {
//			//do nothing
//		}		
//	}
	
	private OnCompletionListener _onSoundEffectCompletionListener = new OnCompletionListener(){

		@Override
		public void onCompletion(MediaPlayer mp) {
			//look in soundeffect queue to get anything waiting to be played
			SoundPlayer.ISPLAYING = false;
			
			if(!_feedbackQueue.isEmpty())
			{
				UserFeedback fb = (UserFeedback)_feedbackQueue.poll();
				
				signalFeedbackToUser(fb);
			}
			
		}};
	
		
    private RecognitionListener _speechListener = new RecognitionListener()
    {
		@Override
		public void onBeginningOfSpeech() {
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onEndOfSpeech() {
		}

		/*
			1 - ERROR_NETWORK_TIMEOUT Network operation timed out. 
			2 - ERROR_NETWORK Other network related errors. 
			3 - ERROR_AUDIO Audio recording error. 
			4 - ERROR_SERVER Server sends error status. 
			5 - ERROR_CLIENT Other client side errors. 
			6 - ERROR_SPEECH_TIMEOUT 
			7 - ERROR_NO_MATCH No recognition result matched. 
			8 - ERROR_RECOGNIZER_BUSY RecognitionService busy. 
			9 - ERROR_INSUFFICIENT_PERMISSIONS Insufficient permissions  
		*/
		@Override
		public void onError(int error) {
			
			String errorMsg = "";
			
			switch(error)
			{
				case 2:
					errorMsg = "A network error occured. Make sure your device is connected to a mobile network or WIFI connection.";
					_errorListeningAttempts++;
				break;
				case 3:
					errorMsg = "An audio recording error occurred, restart the device if problem persists.";
					_errorListeningAttempts++;
				break;
				case 6:
					errorMsg = "No speech input recorded.";
				break;
				case 7:
					errorMsg = "No matches found while analyzing speech.";
				break;
				case 8:
					errorMsg = "Speech recognizer is busy processing input.";
				break;
				case 1: case 4: case 5: case 9:
					errorMsg = "An unknown error has occured.";
					_errorListeningAttempts++;
				break;

			}
			
			Log.i("WordJar", "onError(): " + errorMsg + " " + Integer.toString(error));

			if(_errorListeningAttempts >= MAX_ERROR_ATTEMPTS)
			{
				//report error to user and stop recognition attempts
				new AlertDialog.Builder(WordJarActivity.this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setNegativeButton("Ok", null)
					.setTitle("Error")
					.setMessage(errorMsg)
					.show(); 
			
				_errorListeningAttempts = 0;
				//make sure we stop listening
				stopListening();
			}
			else
				startListening();
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
    		
		}

		@Override
		public void onReadyForSpeech(Bundle params) {
		}

		@Override
		public void onResults(Bundle results) {

			//reset errorattempts bc we are successful
			_errorListeningAttempts = 0;
			
			//save results to a queue for processing
			//_listenedWordsQueue.add(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
			
			//make separate thread call here to process results as to not slow down the UI thread
			SpeechAnalyzer sa = new SpeechAnalyzer(_masterWordMap);
			
//			if(_listenedWordsQueue.peek() != null)
//				sa.execute(_listenedWordsQueue.poll());
			//sa.execute(_listenedWordsQueue);
			sa.execute(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
			    		
    		startListening();
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			// TODO Auto-generated method stub	
		}    	
    };
	
	private TaskListener _getWordTaskListener = new TaskListener()
	{
		@Override
		public void performAction(BaseWord word, Object obj) 
		{
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
					showWordDetailScreen(WordJarActivity.wordCursorToBaseWord(cursor));
				}
			}
		}
	};
	
	//click listener for each row of the base cursor adapter
	private OnClickListener _rowClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {

			v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
									
			ViewHolder holder = (ViewHolder)v.getTag();		
			
			String word = holder.col0.getText().toString();
	
			new DBTasker(getApplicationContext()).new GetWordTask(_progressDialog, word, _getWordTaskListener).execute();
			
		}};

		
	private OnClickListener _wordOfDayClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			
			v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
			
			if(_isListening)
				Toast.makeText(WordJarActivity.this, WordJarActivity.this.getResources().getString(R.string.islistening), Toast.LENGTH_SHORT).show();
			else
			{
				WordnikAsyncTask wnAsynTask = new WordnikAsyncTask(WordJarActivity.this);
				wnAsynTask.execute(WordnikApiCall.WordOfDay);
			}
	}};
	
	private OnClickListener _randomWordClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {

			v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

			if(_isListening)
				Toast.makeText(WordJarActivity.this, WordJarActivity.this.getResources().getString(R.string.islistening), Toast.LENGTH_SHORT).show();
			else
			{
				WordnikAsyncTask wnAsynTask = new WordnikAsyncTask(WordJarActivity.this);
				wnAsynTask.execute(WordnikApiCall.RandomWord);
			}
	}};
	
		
		
	private TaskListener _getAllWordsTaskListener = new TaskListener()
	{
		@Override
		public void performAction(BaseWord word, Object obj) 
		{
			if(obj instanceof Exception)
			{
				Exception ex = (Exception)obj;
//				Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
				
				exitApplicationOnFatalError(getResources().getString(R.string.error_message_unknown_fatal));
			}
			else
			{
				Cursor cursor = (Cursor)obj;		
					
				if(cursor.moveToFirst())
				{
					while (!cursor.isAfterLast()) 
					{
						BaseWord bw = wordCursorToBaseWord(cursor);
						
						//save the word to the master list
						_masterWordMap.put(bw.get_word().toLowerCase(), bw);
						
						cursor.moveToNext();
					}
					
					setListenButtonEnabled(true);
				}
				else
					setListenButtonEnabled(false);
			}
		}
	};
	
	private TaskListener _insertWordTaskListener = new TaskListener()
	{		
		@Override
		public void performAction(BaseWord word, Object obj) 
		{
			if(obj instanceof Exception)
			{
				Exception ex = (Exception)obj;
				Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
			}
			else
			{
				if(word.get_typeId() == DBConstants.WORDTYPE_ACCEPTED)
					_acceptedWordCursorAdapter.changeCursor((Cursor)obj);
				else//else rejected
					_rejectedWordCursorAdapter.changeCursor((Cursor)obj);	
				
				//save the word to the master list
				if(!_masterWordMap.containsKey(word.get_word().toLowerCase()))
					_masterWordMap.put(word.get_word().toLowerCase(), word);
				
				//let user know word was added 
				//Toast.makeText(getApplicationContext(), "\"" + word.get_word() + "\" was added to your list.", Toast.LENGTH_SHORT).show();
				
				//enable listen button if it wasn't already enabled
				setListenButtonEnabled(true);
			}
		}
	};

	public static String currentWord;
	
	//we do nothing
	private TaskListener _updateIncrementWordTotalTaskListener = new TaskListener()
	{		
		@Override
		public void performAction(BaseWord word, Object obj) 
		{
			if(obj instanceof Exception)
			{
				Exception ex = (Exception)obj;
				Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
			}
			else
			{
				currentWord = word._word;
				
				//update the listviews!
				//update the listview cursors to show any new data changes
				new DBTasker(getApplicationContext()).new GetRejectedWordsTask(null, _getRejectedWordsTaskListener).execute();
				new DBTasker(getApplicationContext()).new GetAcceptedWordsTask(null, _getAcceptedWordsTaskListener).execute();

			}
		}
	};
		
	private TaskListener _getAcceptedWordsTaskListener = new TaskListener()
	{
			@Override
			public void performAction(BaseWord word, Object obj) 
			{
				if(obj instanceof Exception)
				{
					Exception ex = (Exception)obj;
			//		Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
					exitApplicationOnFatalError(getResources().getString(R.string.error_message_unknown_fatal));

				}
				else
				{
					_acceptedWordsCursor = (Cursor)obj;
					
					_acceptedWordCursorAdapter = new AcceptedWordCursorAdapter(WordJarActivity.this, _acceptedWordsCursor);	
					_acceptedWordCursorAdapter.setRowOnClickListener(_rowClickListener);
					_acceptedListView.setAdapter(_acceptedWordCursorAdapter);
									
					//let the activity determine when to close the cursor...
					WordJarActivity.this.startManagingCursor(_acceptedWordsCursor);
				
				}
			}
	};
	
	private TaskListener _getRejectedWordsTaskListener = new TaskListener()
	{
			@Override
			public void performAction(BaseWord word, Object obj)
			{
				if(obj instanceof Exception)
				{
					Exception ex = (Exception)obj;
//					Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
					exitApplicationOnFatalError(getResources().getString(R.string.error_message_unknown_fatal));

				}
				else
				{
					_rejectedWordsCursor = (Cursor)obj;
										
					_rejectedWordCursorAdapter = new RejectedWordCursorAdapter(WordJarActivity.this, _rejectedWordsCursor);
					_rejectedWordCursorAdapter.setRowOnClickListener(_rowClickListener);
					_rejectedListView.setAdapter(_rejectedWordCursorAdapter);
					
					//let the activity determine when to close the cursor...					
					WordJarActivity.this.startManagingCursor(_rejectedWordsCursor);
				}				
			}
	};
	
	@Override
	public void onStart()
	{
    	initializeBluetoothHeadset();
	
    	super.onStart();
	}
	
	@Override
	public void onStop()
	{
    	uninitializeBluetoothHeadset();
	
    	super.onStop();
	}
	

    @Override
    public void onPause()
    {
    	if(_isListening)
    		stopListening();
    
    	if(_bluetoothAvailable)
    		unregisterBluetoothHeadset();
    	
    	super.onPause();
    }
    
    @Override
    public void onResume()
    {
    	//get preference settings
    	_alertsOn = _pref.getBoolean("checkbox_alerts", true);
    	
    	//if(_masterSoundEffects != null)
    	//	_masterSoundEffects.muteAllAlarms(_alertsOff);
    	
    	_vibsOn = _pref.getBoolean("checkbox_vibration", true);

    	if(_bluetoothAvailable)
    		registerBluetoothHeadset();
    	
    	super.onResume();
    }
		
    @Override
    public void onDestroy()
    {
    	try 
    	{
			//release soundpool resources
//			if(_masterSoundEffects != null)
//				_masterSoundEffects.delete();
    		
    		//delete soundsplayers
			if(_spAccept != null)
				_spAccept.delete(); 
						
			if(_spReject != null)
				_spReject.delete();
			
			if(_spGoalReached != null)
				_spGoalReached.delete();

			_acceptedWordsCursor.close();
			_rejectedWordsCursor.close();

			//make sure we close the database!
    		if(WordDBAdapter.hasInstance())
        		WordDBAdapter.getInstance(this).close();
        	
		} 
    	catch (Exception e) 
    	{
			e.printStackTrace();
		}
     	
    	super.onDestroy();
    }
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();    
		inflater.inflate(R.menu.optionsmenu, menu);    
							
		return true;//(super.onCreateOptionsMenu(menu));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.itemPreferences:
				createPreferencesDialog();
			break;
			case R.id.itemAbout:
				showAboutScreen(null);
			break;
		}
		
		return true;
	}

	private void createPreferencesDialog() 
	{      
		startActivityForResult(new Intent(this, Preferences.class), PREFERENCE_CODE);
	}
 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
           
        //if there is no speech recognition we are done...
        if(!isSpeechPresent())
			exitApplicationOnFatalError(getResources().getString(R.string.error_message_no_speech_fatal));

        //open the database so its available when we need it... BUG FIX??
        WordDBAdapter.getInstance(this).open();
        
        _pref = PreferenceManager.getDefaultSharedPreferences(this);
        
    	//set default preference values if not done so yet
    	PreferenceManager.setDefaultValues(this, R.xml.preferences, false);  
    	
        //set static variable so we can call back to this activity...
        wordjarActivity = this;
        
        //prevent the screen from turning off
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 

        
        //set wordNik api key
		System.setProperty("WORDNIK_API_KEY", getResources().getString(R.string.wordnik_api_key));
        
        _speakButton = (ImageButton)findViewById(R.id.buttonSpeak);
        _speakButton.setOnClickListener(this);
        
        _wordOfDayButton = (ImageButton)findViewById(R.id.imageButtonWordOfTheDay);
        _wordOfDayButton.setOnClickListener(_wordOfDayClickListener);
        
        _randomWordButton = (ImageButton)findViewById(R.id.imageButtonRandomWord);
        _randomWordButton.setOnClickListener(_randomWordClickListener);

        _frameLayoutFooter = (FrameLayout)findViewById(R.id.frameLayoutFooter);
        
        View viewHeader = findViewById(R.id.includeHeader);
        _linearLayoutHeaderRoot = (LinearLayout)viewHeader.findViewById(R.id.linearLayoutHeaderRoot);
        
        _listenButton = (ImageButton)findViewById(R.id.buttonListen);
        _listenButton.setOnClickListener(this);
        
		_acceptedListView = (ListView) findViewById(R.id.acceptedlist);
		_rejectedListView = (ListView) findViewById(R.id.rejectedlist);
		
		_progressDialog = new ProgressDialog(this);
		
		_tabHost = getTabHost();
		_tabHost.setOnTabChangedListener(this);

		//load custom font type
        if(defaultFont == null)
        	defaultFont = Typeface.createFromAsset(WordJarActivity.this.getAssets(), FONT_TYPE);

        //if the following initialization steps fail the app will not function correctly so lets kill it
		try 
		{
			//TODO: DEF NOT THE BEST WAY TO SO THIS, SHOULD BE GENERIC LOADING INSTEAD...LATER...
			_spAccept = new SoundPlayer(this, WordType.ACCEPTED.ALERTRESOURCEID, _onSoundEffectCompletionListener);
			_spReject = new SoundPlayer(this, WordType.REJECTED.ALERTRESOURCEID, _onSoundEffectCompletionListener);
			_spGoalReached = new SoundPlayer(this, R.raw.applause, _onSoundEffectCompletionListener);
			

			//load master soundeffects
//			_masterSoundEffects = MasterSoundEffects.getInstance(this);
			
//			for(WordType wt : WordType.values())
//				_masterSoundEffects.loadSound(wt.ALERTRESOURCEID);
		} 
		catch (Exception e) 
		{
			exitApplicationOnFatalError(getResources().getString(R.string.error_message_unknown_fatal));
			e.printStackTrace();
		}
		  
		setupTabView();
        //initializeTabView();
              
        //set the audiomanager so the sound effects will be able to be played over bt and earphones...
        initializeAudioManager();
                	
        _masterWordMap = new HashMap<String, BaseWord>();
        //get all words and save to the global hashmap
        
		//set the listen button to disabled for now, let the asyntask getAllWords set it
		setListenButtonEnabled(false);
		
		new DBTasker(this).new GetAcceptedWordsTask(_progressDialog, _getAcceptedWordsTaskListener).execute();
		new DBTasker(this).new GetRejectedWordsTask(_progressDialog, _getRejectedWordsTaskListener).execute();
        
		initializeWordMap(true);		
        
		initializeSpeechIntent();		
    }
    
    
    private void initializeSpeechIntent()
    {
		_speechIntent = new Intent();
		//_speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		_speechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(100));			
		_speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		_speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);			
    }
    
    //lazy initialize
    private SpeechRecognizer getSpeechRecognizer() 
    { 
        if(_sr == null) 
        { 
			_sr = SpeechRecognizer.createSpeechRecognizer(this);
			_sr.setRecognitionListener(_speechListener);
        } 
        return _sr; 
    } 


    
    private void setListenButtonEnabled(boolean enabled)
    {
		if(_masterWordMap.size() > 0)	
		{
			_listenButton.setImageResource(R.drawable.ic_listen);
			_listenButton.setEnabled(true);
		}
		else
		{
			_listenButton.setImageResource(R.drawable.ic_listen_off);
			_listenButton.setEnabled(false);
		}
    }
    
	//initialize the tabview
	private void setupTabView()
	{
		_acceptedListView.setEmptyView(findViewById(R.id.emptyacceptedlist));
		_rejectedListView.setEmptyView(findViewById(R.id.emptyrejectedlist));
		
		View headerAccepted = getLayoutInflater().inflate(R.layout.wordlist_approved_header, null);
		//listView.setHeaderDividersEnabled(true);
		_acceptedListView.addHeaderView(headerAccepted);
		
		View headerRejected = getLayoutInflater().inflate(R.layout.wordlist_rejected_header, null);
		//listView.setHeaderDividersEnabled(true);
		_rejectedListView.addHeaderView(headerRejected);
			
		addTab(getResources().getString(R.string.tab_header_approved), R.drawable.tab_approved, (LinearLayout)findViewById(R.id.tabAccepted));
		addTab(getResources().getString(R.string.tab_header_rejected), R.drawable.tab_denied, (LinearLayout)findViewById(R.id.tabRejected));
		addTab(getResources().getString(R.string.tab_header_dictionary), R.drawable.wordnik_logo, (LinearLayout)findViewById(R.id.tabDictionary));
		
		//re-enable the bottom tabstrip because adding the custom view's as tab fill causes them to be set to false
		//(see documentation for "setStripEnabled")
		getTabWidget().setStripEnabled(true);

		_tabHost.setCurrentTab(_currentTabIndex);		
	}
	
	private void addTab(String label, int drawableId, final LinearLayout ll)
	{
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
				
		//TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		//title.setText(label);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(drawableId);

		_tabHost.addTab(_tabHost.newTabSpec(label).setIndicator(tabIndicator).setContent(new TabContentFactory(){
			@Override
			public View createTabContent(String tag) 
			{
				return ll;
			}
		}));
	}
	
	public static String getRandomPhrase(Context context, int arrayId)
	{
		String[] phrases = context.getResources().getStringArray(arrayId);
		
		int length = phrases.length;
		
		Random r = new Random();
		return phrases[r.nextInt(length)];
	}
	
	//call this if we catch an error that will not allow WordJar to function
	//correctly going forward
	private void exitApplicationOnFatalError(String errMessage)
	{
		new AlertDialog.Builder(this)
        .setTitle(errMessage) 
        .setCancelable(false)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() { 
            @Override 
            public void onClick(DialogInterface dialog, int which) { 
        		//kill the app
        		WordJarActivity.this.finish();
            } 
        }) 
        .show(); 
	}
    
    //parses a cursor containing results from the words table and creates a BaseWord
    public static BaseWord wordCursorToBaseWord(Cursor cursor)
    {
    	BaseWord bw = null;
    	    	
    	if(cursor.getInt(DBConstants.WordTable.TYPEID_INDEX) == DBConstants.WORDTYPE_ACCEPTED)
    	{
    		bw = new AcceptedWord(cursor.getString(DBConstants.WordTable.WORD_INDEX));
			bw._dailyGoal = cursor.getInt(DBConstants.WordTable.DAILYGOALCOUNT_INDEX);
    	}
    	else
    		bw = new RejectedWord(cursor.getString(DBConstants.WordTable.WORD_INDEX));
    	
    	bw._alertId = cursor.getInt(DBConstants.WordTable.ALERTID_INDEX);
    	bw._dateAdded = cursor.getString(DBConstants.WordTable.DATEADDED_INDEX);
    	bw._dailyGoal = cursor.getInt(DBConstants.WordTable.DAILYGOALCOUNT_INDEX);
    	bw._todayCount = cursor.getInt(cursor.getColumnIndex(DBConstants.WordTable.TODAYCOUNT_KEY));    	
    	bw._totalCount = cursor.getInt(cursor.getColumnIndex(DBConstants.WordTable.TOTALCOUNT_KEY));
    	bw._isDictionaryDotCom = cursor.getInt(DBConstants.WordTable.ISDICTIONARY_INDEX); 
    	bw._definition = cursor.getString(DBConstants.WordTable.DEFINITION_INDEX);
    	bw._id = cursor.getInt(DBConstants.KEY_ID_INDEX);

    	return bw;
    }
    
    private void initializeAudioManager()
    {
    	if(_audioManager == null)
    		_audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
    	//set the audiomanager to listen to the call stream, that way we can use the bluetooth headset when available
    	_audioManager.setMode(AudioManager.STREAM_VOICE_CALL);//.MODE_IN_CALL);
    }
    
    private void registerBluetoothHeadset()
    {
		_bluetoothHandler.registerHeadset(_audioManager);
    }
    
    private void unregisterBluetoothHeadset()
    {
		_bluetoothHandler.unregisterHeadset();
    }
    
    private void uninitializeBluetoothHeadset()
    {
    	if(_bluetoothHandler != null)
    	{
    		_bluetoothHandler.disconnect();//always make sure we close the connection!
    		    		
    		_bluetoothHandler = null;
    	}
    }    

    
    BluetoothDevice deviceBT = null;
    
    private void initializeBluetoothHeadset()
    {
    	if(_bluetoothHandler == null)
    		_bluetoothHandler = BluetoothHandler.getInstance(this);
    	
    	try 
    	{			
    		_bluetoothHandler.isBluetoothAvailable();
    		
    		
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 			
			
			// Loop through paired devices 
			for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) { 						
			
				deviceBT = device;
				
				BluetoothClass btc = device.getBluetoothClass();
				int y = btc.getDeviceClass();
				
				int g = device.getBondState();
				
				String d = device.getName();
			
				int yyt = 9;
				yyt = 8;
			} 

    		
    		
//			try 
//			{
				
//				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
//				
//				// Loop through paired devices 
//				for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) { 
//				
//				ddd = device;
//				
//				BluetoothClass btc = device.getBluetoothClass();
//				int y = btc.getDeviceClass();
//				
//				} 
//								
//				bt = new BluetoothHeadset(this, new BluetoothHeadset.ServiceListener(){
//
//					@Override
//					public void onServiceConnected() {
//						int y = 0;
//						y = 9;
//						// TODO Auto-generated method stub
//						
//
//						
//					}
//
//					@Override
//					public void onServiceDisconnected() {
//						// TODO Auto-generated method stub
//						int y = 0;
//						y = 9;
//					}});
//
//				
//				
////				if(!_bluetoothHandler.isBluetoothConnected())
////				{
////					_bluetoothHandler.connect(_audioManager);
////					_bluetoothConnected = true;
////				
////				}
////				boolean h = _audioManager.isBluetoothScoOn();
////				boolean gh = _audioManager.isBluetoothA2dpOn();
//////				this._masterSoundEffects.playAcceptedWordSound();
////				this._spGoalReached.play(_alertsOff);
//////				this._masterSoundEffects.playRejectedWordSound();
//				
//				//mPlayer.start(); 
//				
//				//am.stopBluetoothSco();
//				
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			
			

//			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//			
//			// If there are paired devices
//			if (pairedDevices.size() > 0) {    
//				// Loop through paired devices    
//				for (BluetoothDevice device : pairedDevices) {        
//					// Add the name and address to an array adapter to show in a ListView        
//					String h = device.getName() + "\n" + device.getAddress();    
//					
//					Toast.makeText(this, h, Toast.LENGTH_SHORT).show();
//				}
//			}
//			

		}
        catch(BluetoothNotAvailableException e)
        {
        	_bluetoothAvailable = false;
			e.printStackTrace();
        }
		catch(BluetoothNotEnabledException e)//this exception is intended to prompt for more action if caught		
		{
        	
//    		//prompt the user to turn on bluetooth
//    		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);    
//    		startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_CODE);

        	_bluetoothAvailable = false;//for now just make unavailable
			e.printStackTrace();			
		}
    	catch (Exception e) 
    	{
        	_bluetoothAvailable = false;//for now just make unavailable
			e.printStackTrace();
		}    	
    }
    
    //checks to see if there is a speech recognizer present
    private boolean isSpeechPresent()
    {
        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 
        																						PackageManager.MATCH_DEFAULT_ONLY);        
        
        if (activities.size() != 0) 
        	return true;
        else 
        	return false;
    }    
    
 	@Override
	public void onClick(View arg0) 
	{
		arg0.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

		if(arg0.getId() == R.id.buttonSpeak)
		{		
			
			/*			bt = new BluetoothHeadset(this, new BluetoothHeadset.ServiceListener(){

				@Override
				public void onServiceConnected() {
					int y = 0;
					y = 9;
					// TODO Auto-generated method stub
					

					
				}

				@Override
				public void onServiceDisconnected() {
					// TODO Auto-generated method stub
					int y = 0;
					y = 9;
				}});

				bt.connectHeadset(ddd);
							
				boolean g = bt.isConnected(ddd);			
				BluetoothDevice dj =			bt.getCurrentHeadset();\
			
			*/
			
			
			//make a call to handle the type of connectio!!! TODO TODO!!
						
//			if(_bluetoothAvailable)
//				_bluetoothHandler.connect();
//			this._spAccept.play(false);

			startVoiceRecognitionActivity();        
			
		}
		else if(arg0.getId() == R.id.buttonListen)
		{
			
//			if(_bluetoothAvailable)
//				_bluetoothHandler.disconnect();			
//			this._spAccept.play(false);


//craftytodo
			
			if(_isListening)
				stopListening();
			else
				startListening();
			
		}
	}
	
	private void startListening()
	{
		if(!_isListening)
		{
			_isListening = true;
	
			//set button to show its listening
			_listenButton.setImageResource(R.drawable.ic_listen_spk);
			_speakButton.setEnabled(false);
//			_tabHost.getTabWidget().getChildTabViewAt(TABINDEX_DICTIONARYDOTCOM).setEnabled(false);
		}
		
		getSpeechRecognizer().startListening(_speechIntent);
	}
	
	private void stopListening()
	{				
		_listenButton.setImageResource(R.drawable.ic_listen);
		_speakButton.setEnabled(true);
//		_tabHost.getTabWidget().getChildTabViewAt(TABINDEX_DICTIONARYDOTCOM).setEnabled(true); 

		 if(getSpeechRecognizer() != null) 
	     { 
			// if(_isListening)
			//	 _sr.stopListening();
			 
			 _sr.destroy();
			 _sr = null;
			 
	     }
		_isListening = false;
	}
	
	//Fire an intent to start the speech recognition activity.     
	private void startVoiceRecognitionActivity() 
	{       
		
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);        
		
		//Specify the calling package to identify your application        
		//intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());        
		
/*		// Display an hint to the user about what he should say.        
		int arrayId = R.array.speak_accepted_tags;
		if(_currentTabIndex == 1)
			arrayId = R.array.speak_rejected_tags;
		
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getRandomPhrase(this, arrayId));        
*/
		if(_currentTabIndex == TABINDEX_ACCEPTED)
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.speak_accepted_prompt));        
		else if(_currentTabIndex == TABINDEX_REJECTED)
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.speak_rejected_prompt));        
		
		// Given an hint to the recognizer about what the user is going to say        
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);        
		
		// Specify how many results you want to receive. The results will be sorted        
		// where the first result is the one with higher confidence.        
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);        
				
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);    
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch(requestCode)
		{
			case VOICE_RECOGNITION_REQUEST_CODE:
				if(resultCode == Activity.RESULT_OK)
				{					
					verifySpeechMatch(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS), _currentTabIndex);
				}
			break;
			case PREFERENCE_CODE:
				//do nothing for now...
			break;
			case WORD_DETAIL_CODE:
				if(resultCode != Activity.RESULT_CANCELED)
				{
					BaseWord bw = (BaseWord)data.getSerializableExtra(this.getResources().getString(R.string.namespace) + ".baseword"); 					
	
					if(resultCode == WORD_ACTION_SAVED_CODE)
					{				
						//replace the current valuoe in the map
						_masterWordMap.put(bw.get_word().toLowerCase(), bw);
					}
					if(resultCode == WORD_ACTION_DELETED_CODE)
					{				
						//delete the map value
						_masterWordMap.remove(bw.get_word().toLowerCase());
					}

	//				if(bw.get_typeId() == BaseWord.WordType.REJECTED.TYPEID)
	//					new DBTasker(this).new GetRejectedWordsTask(_progressDialog, _getRejectedWordsTaskListener).execute();
	//				else
	//					new DBTasker(this).new GetAcceptedWordsTask(_progressDialog, _getAcceptedWordsTaskListener).execute();

					
					if(_masterWordMap.size() > 0)
						setListenButtonEnabled(true);
					else
						setListenButtonEnabled(false);
				}
			break;

			
		}
	
		super.onActivityResult(requestCode, resultCode, data);

	}
	
	//removes all items from the master word map
	public void clearWordMap()
	{
		_masterWordMap.clear();
	}
	
	//resets all words in the hashmap (usually called when reseting all stats)
	public void initializeWordMap(boolean showDialogs)
	{
		ProgressDialog pd = null;
		if(showDialogs)
			pd = _progressDialog;

		clearWordMap();//clear the map first...
		
        //get all the words from db...
		new DBTasker(this).new GetAllWordsTask(pd, _getAllWordsTaskListener).execute();

	}
	
	//refreshes the UI (lists, buttons, etc...) to a state after some db action has been performed
	public void refreshUI(boolean showDialogs)
	{
		ProgressDialog pd = null;
		if(showDialogs)
			pd = _progressDialog;
		
		new DBTasker(this).new GetRejectedWordsTask(pd, _getRejectedWordsTaskListener).execute();
		new DBTasker(this).new GetAcceptedWordsTask(pd, _getAcceptedWordsTaskListener).execute();
		
		if(_masterWordMap.size() > 0)
			setListenButtonEnabled(true);
		else
			setListenButtonEnabled(false);

	}
	
	private void verifySpeechMatch(ArrayList<String> matchList, final int tabIndex)
	{
		//always grab the first match from the list then
		//make sure to split bc the recognizerintent will jam everything heard into one item of the array
		final String word = matchList.get(0).split(" ")[0].toLowerCase();
		
		//verify spoken word is what we want...	
		AlertDialog.Builder adb = new AlertDialog.Builder(this);		
		adb.setIcon(getResources().getDrawable(R.drawable.action_droid));
		adb.setTitle(getResources().getString(R.string.speak_verify_prompt))
			.setMessage("\"" + word + "\"")
			.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							switch(tabIndex)
							{
								case TABINDEX_ACCEPTED:
									new DBTasker(getApplicationContext()).new InsertNewWordTask(_progressDialog, new AcceptedWord(word), _insertWordTaskListener).execute();
									break;
								case TABINDEX_REJECTED:
									new DBTasker(getApplicationContext()).new InsertNewWordTask(_progressDialog, new RejectedWord(word), _insertWordTaskListener).execute();
									break;
							}							
						}
			}).setNegativeButton("No", null).show();
	}

	@Override
	public void onTabChanged(String tabLabel) 
	{
		Resources r = getResources();
		if(tabLabel == r.getString(R.string.tab_header_approved))
		{
			_frameLayoutFooter.setVisibility(View.VISIBLE);
			_currentTabIndex =  TABINDEX_ACCEPTED;
		}
		else if(tabLabel == r.getString(R.string.tab_header_rejected))
		{
			_frameLayoutFooter.setVisibility(View.VISIBLE);
			_currentTabIndex =  TABINDEX_REJECTED;
		}
		else
		{
			_currentTabIndex = TABINDEX_DICTIONARYDOTCOM;
			_frameLayoutFooter.setVisibility(View.INVISIBLE);
		}
	}
	
	//made this public because we call it directly from XML layout file
	public void showAboutScreen(View v) 
	{ 		
		if(v != null)
			v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

		startActivity(new Intent(this, AboutActivity.class)); 
	}
	
	private void showWordDetailScreen(BaseWord bw) 
	{ 		
		Intent intent = new Intent(this, WordDetailActivity.class);
		intent.putExtra(this.getResources().getString(R.string.namespace) + ".baseword", bw);
	
		startActivityForResult(intent, WORD_DETAIL_CODE); 
	}
}