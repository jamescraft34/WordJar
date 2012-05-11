package com.craftysoft.wordjar;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;

public class SoundPlayer {

	private MediaPlayer _mediaPlayer = null;
	
	public static boolean ISPLAYING = false;
			
	public SoundPlayer(Context context, int soundId, OnCompletionListener onCompletionListener)
	{
		try 
		{
			String namespace = context.getResources().getString(R.string.namespace);
			Uri path = Uri.parse("android.resource://" + namespace + "/" + soundId);

			_mediaPlayer = new MediaPlayer();
			_mediaPlayer.setDataSource(context, path);
			_mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);			
			_mediaPlayer.prepare();
			_mediaPlayer.setVolume(1.0f, 1.0f);
			_mediaPlayer.setOnCompletionListener(onCompletionListener);
		}
		catch(Exception ex)
		{
			//do nothing
		}
	}
	
	public void play(boolean alertsOn)
	{
		try 
		{
			ISPLAYING = true;
			
			if(alertsOn)//mimic a mute so we can still get the oncompletion event to fire
				_mediaPlayer.setVolume(1.0f, 1.0f);
			else
				_mediaPlayer.setVolume(0.0f, 0.0f);

			
			_mediaPlayer.start();
		} 
		catch (IllegalStateException e) {
			//do nothing
			ISPLAYING = false;
		}
	}
	
	public void delete()
	{		
		if(_mediaPlayer != null)
		{
			try
			{
				if(_mediaPlayer.isPlaying())
					_mediaPlayer.stop();
				
				_mediaPlayer.release();
				_mediaPlayer = null;
			}
			catch(Exception ex)
			{
				//do nothing
			}
		}		
	}
}
