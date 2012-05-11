package com.craftysoft.wordjar;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AboutActivity extends Activity implements OnClickListener{
	
    private TextView _link = null;
    private LinearLayout _ll = null;
    private TextView _version = null;
    
    //private Button _email = null;    
    //private String[] _emailList = {"craftySoft@gmail.com"};
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
 
        
        // Have the system blur any windows behind this one.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        setContentView(R.layout.about);
        
        _ll = (LinearLayout)findViewById(R.id.mainLayout);
        _ll.setOnClickListener(this);
        
        _version = (TextView)findViewById(R.id.textViewVersion);
        
        PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	        _version.setText("Version " + pInfo.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        
        initializeUI();        
    }
    
	private void initializeUI()
	{
        _link = (TextView)findViewById(R.id.TextViewLink);
        _link.setOnClickListener( new OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{
				Uri uri = Uri.parse("http://www.craftysoft.me" );
        		startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
        	}
        });
        
        
//        _email = (Button)findViewById(R.id.ButtonEmailLink);
//        _email.setOnClickListener( new OnClickListener()
//        {
//        	@Override
//        	public void onClick(View v)
//        	{
//        		callEmailIntent();
//        	}
//        });
	}

//	private void callEmailIntent()
//	{
//		try{
//			Intent i = new Intent(Intent.ACTION_SEND); 
//			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
//			i.setType("plain/text");
//			i.putExtra(Intent.EXTRA_EMAIL, _emailList);
//			i.putExtra(Intent.EXTRA_SUBJECT, "Re: WordJar");		
//			startActivity(Intent.createChooser(i, "Email Developer"));		
//		}
//		catch(Exception ex)
//		{
//			Toast.makeText(this, "Email client could not be launched on device.", Toast.LENGTH_SHORT).show();
//		}
//	}

	@Override
	public void onClick(View arg0) {
		AboutActivity.this.finish();
	}

}
