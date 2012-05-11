package com.craftysoft.wordjar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

//singleton class
public class BluetoothHandler {

	private static BluetoothHandler _instance = null;

	private BluetoothAdapter mBluetoothAdapter = null;
	private static Context _context = null;
	
	private boolean _isBluetoothConnected = false;//only set when all BT components are connected and we should hear something 
	
	private boolean _isBluetoothSCOConnected = false;	
	private boolean _isBluetoothAdapterConnected = false;	
	private boolean _isBluetoothDeviceConnected = false;	
    
	private boolean _isRegistered = false;	
	private AudioManager _audioManager = null;
		
	//private to support singleton class pattern
	private BluetoothHandler()
	{
	}
	
	public static BluetoothHandler getInstance(Context context)
	{
		if(_instance == null)
		{
			_context = context;
			_instance = new BluetoothHandler();
		}

		return _instance;
	}
	
	private final BroadcastReceiver _btAudioManagerReciever = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(action.equalsIgnoreCase(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED))
			{
				 int l_state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
				 
				 if(l_state == AudioManager.SCO_AUDIO_STATE_CONNECTED)
				 {
					 _isBluetoothSCOConnected = true;
				 }
				 else if(l_state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED)
				 {
					 _isBluetoothSCOConnected = false;				 
				 }
			}
		}		
	};
	
	private final BroadcastReceiver _btAdapterReciever = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(action.equalsIgnoreCase(BluetoothAdapter.ACTION_STATE_CHANGED))
			{
				 int l_state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				 
				 if(l_state == BluetoothAdapter.STATE_ON)
				 {
					 _isBluetoothAdapterConnected = true;
				 }
				 else if(l_state == BluetoothAdapter.STATE_OFF)
				 {
					 _isBluetoothAdapterConnected = false;				 
				 }
			}
		}		
	};

			
	private final BroadcastReceiver _btDeviceReciever = new BroadcastReceiver() { 
	    @Override 
	    public void onReceive(Context context, Intent intent) { 
	        String action = intent.getAction(); 
//	        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); 
	 
//	        if (BluetoothDevice.ACTION_FOUND.equals(action)) { 
//	           //Device found 
//	        } 
//	        else 
	        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) { 
	            //Device is now connected 
	        	_isBluetoothDeviceConnected = true;
	        } 
//	        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { 
//	           //Done searching 
//
//	        } 
//	        else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) { 
//	           //Device is about to disconnect 
//	        } 
	        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) { 
	           //Device has disconnected 
	        	_isBluetoothDeviceConnected = false;
	        }            
	    } 
	}; 

	
	private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() { 
	    @Override 
	    public void onReceive(Context context, Intent intent) { 
	//        String action = intent.getAction(); 
	 //       BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); 
	 
//	        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) { 
//	           ... //Device has disconnected 
	        	
//	                int oldState = intent.getIntExtra(BluetoothHeadset.EXTRA_PREVIOUS_STATE, 0);
		        		        	
//	    	if(intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0) == BluetoothHeadset.STATE_CONNECTED) 
//		    {
//	    		Log.i("WordBT", "Connected");
//	    		//connect();
//		    }
//	    	else if(intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0) == BluetoothHeadset.STATE_DISCONNECTED) 
//		    {
//	    		Log.i("WordBT", "Disconnected");
//
//	    		//disconnect();
//		    }
	    		
	    } 
	}; 

	
	//make sure we call this before connect
	//handles checking for bluetooth availability and sets the broadcast recievers
	public void registerHeadset(AudioManager audioManager)
	{
    	IntentFilter bluetoothIntentFilter = new IntentFilter();
    	
    	bluetoothIntentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED);//SCO state
    	bluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//adapter state
    	bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//device state
    	bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//device state
	        
    	//bluetoothIntentFilter.addAction(BluetoothHeadset.ACTION_STATE_CHANGED);
    	//_context.registerReceiver(mBTReceiver, bluetoothIntentFilter); 

    	_context.registerReceiver(_btAudioManagerReciever , bluetoothIntentFilter);
		_context.registerReceiver(_btAdapterReciever, bluetoothIntentFilter);
		_context.registerReceiver(_btDeviceReciever, bluetoothIntentFilter);
		
    	_audioManager = audioManager;
    	
    	_isRegistered = true;
	}
	
	//make sure we call this when we are done with the bt 
	public void unregisterHeadset()
	{
		_context.unregisterReceiver(_btAudioManagerReciever);
		_context.unregisterReceiver(_btAdapterReciever);
		_context.unregisterReceiver(_btDeviceReciever);
		//_context.unregisterReceiver(mBTReceiver);

		_isRegistered = false;
	}
	
	public boolean isBluetoothAvailable() throws Exception
	{
		if(!isBluetoothCapable())
			throw new BluetoothNotAvailableException();
		
		if(!isBluetoothEnabled())
			throw new BluetoothNotEnabledException();//this exception is intended to prompt for more action if caught		
	
		int y = mBluetoothAdapter.getState();
		
		int g = 0;
		g = 9;
		
		return true;
	}	
	
	public void connect()
	{
		if(!_isBluetoothConnected)
		{
			if(!_audioManager.isBluetoothScoOn())
			{
				//am.setBluetoothA2dpOn(true);
				_audioManager.setBluetoothScoOn(true);		
				//am.setMicrophoneMute(true); 
				_audioManager.startBluetoothSco();
				
				_isBluetoothConnected = true;
			}
		}
	}
	
	public void disconnect()
	{
		if(_isBluetoothConnected)
		{		
			if(_audioManager.isBluetoothScoOn())
			{
				//am.setBluetoothA2dpOn(false);
				_audioManager.setBluetoothScoOn(false);
				//am.setMicrophoneMute(false);
				_audioManager.stopBluetoothSco();
				
				_isBluetoothConnected = false;
			}
		}
	}
		
	//return false is not available, true if available
	private boolean isBluetoothCapable()
    {
		if(mBluetoothAdapter == null)
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	    	
    	if(mBluetoothAdapter == null)
    		return false;//bluetooth not supported if we get null here
    	else
    		return true;
    }
    
    private boolean isBluetoothEnabled()
    {
		if(mBluetoothAdapter == null)
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    	return mBluetoothAdapter.isEnabled();
//		if (!mBluetoothAdapter.isEnabled()) 
    	 //   		{    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);    
    	  //  		startActivityForResult(enableBtIntent, 2);}
    }

    
    //***exception classes***
    
    public static class BluetoothNotAvailableException extends Exception
    {
    	private static final long serialVersionUID = 1L;

		public BluetoothNotAvailableException()
    	{
    		super("Bluetooth not available");
    	}
    }
    
    public static class BluetoothNotEnabledException extends Exception
    {
    	private static final long serialVersionUID = 1L;

		public BluetoothNotEnabledException()
    	{
    		super("Bluetooth not enabled");
    	}
    }
    
    public static class BluetoothNotRegisteredException extends Exception
    {
    	private static final long serialVersionUID = 1L;

		public BluetoothNotRegisteredException()
    	{
    		super("BluetoothHandler not registered.");
    	}
    }
}
