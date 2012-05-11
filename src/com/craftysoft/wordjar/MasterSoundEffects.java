package com.craftysoft.wordjar;

import java.util.HashMap;

import com.craftysoft.wordjar.BaseWord.WordType;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;


//singleton class

/*
 * manages the default sound effects that will be used
 */

public class MasterSoundEffects extends SoundPool{
	
	private static MasterSoundEffects _instance = null;
	
	private boolean _muteAlarm = false;
	
	
	private HashMap<Integer, Integer> _soundIdMap = new HashMap<Integer, Integer>();

	//default sound resources
	private static final int _goalReachedAlarm = R.raw.applause;	
	
	private Context _context = null;
	
	private MasterSoundEffects(Context context) throws Exception
	{		
		super(1, AudioManager.STREAM_VOICE_CALL, 0);
		
		_context = context;

		try
		{
			init();
		}
		catch(Exception ex)
		{
			throw ex;
		}
//		 mPlayer = new MediaPlayer();//.create(this, R.raw.boing);
//		 
//       try { 
//       	mPlayer.setDataSource(getResources().openRawResourceFd(R.raw.boing).getFileDescriptor());
////           mPlayer.setDataSource(new FileInputStream( 
////               "/sdcard/sample.mp3").getFD()); 
//
//           mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL); 
//
//           mPlayer.prepare(); 
//
//           //mPlayer.start(); 
//       } catch(Exception e) { 
//           Log.e("TAG", e.toString()); 
//       } 
		
		
	}
	
	private void init()
	{
		loadSound(_goalReachedAlarm); 
	}
	
	public void loadSound(int resourceId)
	{
		int soundId = load(_context, resourceId, 1); 
		_soundIdMap.put(resourceId, soundId);
	}
	
	public void delete()
	{
		release();//release the soundpool resources
		
		_instance = null;
	}
		
	public static MasterSoundEffects getInstance(Context context) throws Exception
	{ 
		 if(_instance == null)
			 _instance = new MasterSoundEffects(context);
		 
		 return _instance;
	}

	public void playWordSound(int resId)
	{
		if(!_muteAlarm)
			playSound(_soundIdMap.get(resId));				
	}
		
	public void playGoalReachedSound()
	{
		if(!_muteAlarm)
			playSound(_soundIdMap.get(_goalReachedAlarm));		
	}
	
	private void playSound(int id)
	{
		try 
		{
			play(id, 1.0f, 1.0f, 1, 0, 1.0f);
		} 
		catch (Exception e) 
		{
			//do nothing for now, just don't play the sound
			e.printStackTrace();
		}		
	}
	
	public void muteAllAlarms(boolean muteAll) {		
		_muteAlarm = muteAll;
	}
}
