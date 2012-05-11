package com.craftysoft.wordjar;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;

public class GenericDialog extends Dialog {
	
	

	public GenericDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.generic_dialog);
	}


}
