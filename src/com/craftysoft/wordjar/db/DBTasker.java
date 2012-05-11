package com.craftysoft.wordjar.db;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;

import com.craftysoft.wordjar.BaseWord;
import com.craftysoft.wordjar.R;
import com.craftysoft.wordjar.TaskListener;
import com.craftysoft.wordjar.WordJarActivity;
import com.craftysoft.wordjar.BaseWord.WordType;

/*
 * provides AsynTask methods for calls to the database 
 */
public final class DBTasker {
	
	private Context _context = null;
	
	//count of the active tasks so we can track when to close the progress dialog
	private static int _activeTaskCount = 0;
	
	
	public DBTasker(Context context)
	{
		_context = context;
	}

	////////////////////////////////////////////////////////////////////
	//PUBLIC TASK CLASSES
	public class GetAllWordsTask extends GetWordsTask
	{
		public GetAllWordsTask(ProgressDialog pd, TaskListener tl) {
			super(pd, tl, null);
		}	
	}

	public class GetAcceptedWordsTask extends GetWordsTask
	{

		public GetAcceptedWordsTask(ProgressDialog pd, TaskListener tl) {
			super(pd, tl, WordType.ACCEPTED);
		}	
	}
	
	public class GetRejectedWordsTask extends GetWordsTask
	{

		public GetRejectedWordsTask(ProgressDialog pd, TaskListener tl) {
			super(pd, tl, WordType.REJECTED);
		}	
	}

	
	///////////////////////////////////////////////////////////////////////////
	private abstract class GetWordsTask extends BaseAsyncTask
	{	
		protected BaseWord.WordType _type;
		
		public GetWordsTask(ProgressDialog pd, TaskListener tl, BaseWord.WordType type)
		{
			_pd = pd;
			_tl = tl;
			_type = type;
			
			_dialogPhrases = R.array.loading_tags;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected AsyncTaskResult doInBackground(Void... params) 
		{
			AsyncTaskResult result = null;
	    	try 
	    	{
				//WordDBAdapter wordDBAdapter = new WordDBAdapter(_context);	    	
				//wordDBAdapter.open();
	    		WordDBAdapter wordDBAdapter = WordDBAdapter.getInstance(_context).open();
				if(_type == null)
					result = new AsyncTaskResult(wordDBAdapter.getWords());
				else if(_type == BaseWord.WordType.ACCEPTED)
					result = new AsyncTaskResult(wordDBAdapter.getAcceptedWords());
				else if(_type == BaseWord.WordType.REJECTED)
					result = new AsyncTaskResult(wordDBAdapter.getRejectedWords());
			} 
	    	catch (Exception e) 
	    	{
	    		result = new AsyncTaskResult(new Exception(_context.getResources().getString(R.string.error_message_generic)));
	    	}
	    	return result;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	public class GetWordTask extends BaseAsyncTask
	{				
		private String _searchword;
		
		public GetWordTask(ProgressDialog pd, String word, TaskListener tl)
		{
			_pd = pd;
			_searchword = word;
			_tl= tl;
			
			_dialogPhrases = R.array.loading_tags;
		}
				
		@Override
		protected AsyncTaskResult<Object> doInBackground(Void... params) 
		{
			AsyncTaskResult result = null;
	    	try 
	    	{
//				WordDBAdapter wordDBAdapter = new WordDBAdapter(_context);	    	
//				wordDBAdapter.open();
	
	    		result = new AsyncTaskResult(WordDBAdapter.getInstance(_context).open().getWord(_searchword));

	    		
//				result = new AsyncTaskResult(wordDBAdapter.getWord(_searchword));
			} 
	    	catch (Exception e) 
	    	{
	    		result = new AsyncTaskResult(new Exception(_context.getResources().getString(R.string.error_message_generic)));
			}
	    	return result;
		}		
	}
	
	public class UpdateWordDetailsTask extends BaseAsyncTask
	{					
		public UpdateWordDetailsTask(ProgressDialog pd, BaseWord word, TaskListener tl)
		{
			_pd = pd;
			_word = word;
			_tl= tl;
			
			_dialogPhrases = R.array.updating_tags;
		}
				
		@Override
		protected AsyncTaskResult<Object> doInBackground(Void... params) 
		{
			AsyncTaskResult result = null;
	    	try 
	    	{
//				WordDBAdapter wordDBAdapter = new WordDBAdapter(_context);	    	
//				wordDBAdapter.open();
	    		
				WordDBAdapter.getInstance(_context).open().updateWordDetails(_word);
												
//				wordDBAdapter.updateWordDetails(_word);
			} 
	    	catch (Exception e) 
	    	{
	    		result = new AsyncTaskResult(new Exception(_context.getResources().getString(R.string.error_message_generic)));
			}
	    	return result;
		}		
	}
	
	public class UpdateIncrementWordTotalTask extends BaseAsyncTask
	{							
		public UpdateIncrementWordTotalTask(ProgressDialog pd, BaseWord word, TaskListener tl)
		{
			_pd = pd;
			_word = word;
			_tl= tl;
		}
				
		@Override
		protected AsyncTaskResult<Object> doInBackground(Void... params) 
		{
			AsyncTaskResult result = null;
	    	try 
	    	{
//				WordDBAdapter wordDBAdapter = new WordDBAdapter(_context);	    	
//				wordDBAdapter.open();
	
	    		WordDBAdapter wordDBAdapter = WordDBAdapter.getInstance(_context).open();

				wordDBAdapter.updateIncrementWordTotal(_word);
			} 
	    	catch (Exception e) 
	    	{
	    		result = new AsyncTaskResult(new Exception(_context.getResources().getString(R.string.error_message_generic)));
			}
	    	return result;
		}		
	}


	///////////////////////////////////////////////////////////////////////////
	public class GetWordDateStatsTask extends BaseAsyncTask
	{				
		private long _wordId;
		
		public GetWordDateStatsTask(ProgressDialog pd, long wordId, TaskListener tl)
		{
			_pd = pd;
			_tl= tl;
			_wordId = wordId;
			
			_dialogPhrases = R.array.loading_tags;
		}
				
		@Override
		protected AsyncTaskResult<Object> doInBackground(Void... params) 
		{
			AsyncTaskResult result = null;
	    	try 
	    	{
//				WordDBAdapter wordDBAdapter = new WordDBAdapter(_context);	    	
//				wordDBAdapter.open();
	    		WordDBAdapter wordDBAdapter = WordDBAdapter.getInstance(_context).open();
												
				result = new AsyncTaskResult(wordDBAdapter.getWordDateStats(_wordId));
			} 
	    	catch (Exception e) 
	    	{
	    		result = new AsyncTaskResult(new Exception(_context.getResources().getString(R.string.error_message_generic)));
			}
	    	return result;
		}		
	}

	
	///////////////////////////////////////////////////////////////////////////
	public class InsertNewWordTask extends BaseAsyncTask
	{				
		public InsertNewWordTask(ProgressDialog pd, BaseWord word, TaskListener tl)
		{
			_pd = pd;
			_word = word;
			_tl= tl;
			
			_dialogPhrases = R.array.inserting_tags;
		}
				
		@Override
		protected AsyncTaskResult<Object> doInBackground(Void... params) 
		{
			AsyncTaskResult result = null;
	    	try 
	    	{
//				WordDBAdapter wordDBAdapter = new WordDBAdapter(_context);	    	
//				wordDBAdapter.open();
	    		WordDBAdapter wordDBAdapter = WordDBAdapter.getInstance(_context).open();
				
				long wordId = wordDBAdapter.insertNewWord(_word);
		
				_word.set_id(wordId);
				
				if(_word.get_typeId() == DBConstants.WORDTYPE_ACCEPTED)
					result = new AsyncTaskResult(wordDBAdapter.getAcceptedWords());
				else if(_word.get_typeId() == DBConstants.WORDTYPE_REJECTED)
					result = new AsyncTaskResult(wordDBAdapter.getRejectedWords());
			} 
	    	catch(SQLiteConstraintException e)
	    	{
	    		//we will assume only a duplicate constraint error was thrown...
	    		result = new AsyncTaskResult(new Exception(_context.getResources().getString(R.string.error_message_duplicate)));
	    	}
	    	catch (Exception e) 
	    	{
	    		result = new AsyncTaskResult(new Exception(_context.getResources().getString(R.string.error_message_generic)));
			}
	    	
	    	
	    	return result;
		}		
	}
	
	public class DeleteWordTask extends BaseAsyncTask
	{				
		public DeleteWordTask(ProgressDialog pd, BaseWord word, TaskListener tl)
		{
			_pd = pd;
			_word = word;
			_tl= tl;
			
			_dialogPhrases = R.array.deleting_tags;
		}
				
		@Override
		protected AsyncTaskResult<Object> doInBackground(Void... params) 
		{
			AsyncTaskResult result = null;
	    	try 
	    	{
//				WordDBAdapter wordDBAdapter = new WordDBAdapter(_context);	    	
//				wordDBAdapter.open();
	    		WordDBAdapter wordDBAdapter = WordDBAdapter.getInstance(_context).open();
				
				wordDBAdapter.deleteWord(_word);								
			} 
	    	catch (Exception e) 
	    	{
	    		result = new AsyncTaskResult(new Exception(_context.getResources().getString(R.string.error_message_generic)));
			}
	    	return result;
		}		
	}

	public class DeleteAllWordsTask extends BaseAsyncTask
	{				
		public DeleteAllWordsTask(ProgressDialog pd, TaskListener tl)
		{
			_pd = pd;
			_tl= tl;
			
			_dialogPhrases = R.array.deleting_tags;
		}
				
		@Override
		protected AsyncTaskResult<Object> doInBackground(Void... params) 
		{
			AsyncTaskResult result = null;
	    	try 
	    	{
//				WordDBAdapter wordDBAdapter = new WordDBAdapter(_context);	    	
//				wordDBAdapter.open();
	    		WordDBAdapter wordDBAdapter = WordDBAdapter.getInstance(_context).open();
				
				wordDBAdapter.deleteAllWords();
//				result = new AsyncTaskResult(wordDBAdapter.deleteAllWords());								
			} 
	    	catch (Exception e) 
	    	{
	    		result = new AsyncTaskResult(new Exception(_context.getResources().getString(R.string.error_message_generic)));
			}
	    	return result;
		}		
	}

	public class ResetAllWordsTask extends BaseAsyncTask
	{				
		public ResetAllWordsTask(ProgressDialog pd, TaskListener tl)
		{
			_pd = pd;
			_tl= tl;
			
			_dialogPhrases = R.array.reseting_tags;
		}
				
		@Override
		protected AsyncTaskResult<Object> doInBackground(Void... params) 
		{
			AsyncTaskResult result = null;
	    	try 
	    	{
//				WordDBAdapter wordDBAdapter = new WordDBAdapter(_context);	    	
//				wordDBAdapter.open();
	    		WordDBAdapter wordDBAdapter = WordDBAdapter.getInstance(_context).open();
			
				wordDBAdapter.resetAllWords();
			} 
	    	catch (Exception e) 
	    	{
	    		result = new AsyncTaskResult(new Exception(_context.getResources().getString(R.string.error_message_generic)));
			}
	    	return result;
		}		
	}
	
	///////////////////////////////////////////////////////////////////////////
	/*
	 * BASE CLASS
	 */
	private abstract class BaseAsyncTask extends AsyncTask<Void, Void, AsyncTaskResult<Object>>
	{
		//progress dialog to use to signal loading, etc...
		protected ProgressDialog _pd = null;
		protected TaskListener _tl = null;
		protected BaseWord _word = null;
		protected int _dialogPhrases = R.array.loading_tags;//default loading tags can be changed for other dialogs
		
		@Override
		protected void onPreExecute() 
		{	
			_activeTaskCount++;
			
			showDialog();
		}
		
		@Override
		protected void onPostExecute(AsyncTaskResult<Object> result) 
		{
			_activeTaskCount--;
			
			if(result == null)
				_tl.performAction(_word, result);
			else if(!result.hasError())
				_tl.performAction(_word, (Cursor)result.getResult());
			else
				_tl.performAction(_word, (Exception)result.getError());
			
			if(_activeTaskCount == 0)
				hideDialog();
		}

		protected void showDialog()
		{
			if(_pd != null)
			{
				if(!_pd.isShowing())
				{
					_pd.setMessage(WordJarActivity.getRandomPhrase(_context, _dialogPhrases));
					_pd.show();
				}
			}
		}
		
		protected void hideDialog()
		{
			if(_pd != null)
			{
				if(_pd.isShowing())
				{
					//make sure we at least show the dialog for a second so user doesnt wonder
					//what the dialog was in case we finish quickly causing the dialog to just flash on the screen...
					try {
						Thread.sleep(300L);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					_pd.dismiss();
				}
			}
		}			
	}
	
	///////////////////////////////////////////////////////////////////////////
	private final class AsyncTaskResult<T>
	{
		private T _result = null;
		private Exception _ex = null;
		
		public AsyncTaskResult(T result, int... optionalInt)
		{
			super();
			_result = result;
		}
		
		public AsyncTaskResult(Exception ex)
		{
			super();
			_ex = ex;
		}
		
		public T getResult()
		{
			return _result;
		}
		
		public boolean hasError()
		{
			return (_ex != null);
		}
		
		public Exception getError()
		{
			return _ex;
		}
	}
}
