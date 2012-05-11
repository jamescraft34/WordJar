package com.craftysoft.wordjar;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class EmptyListImageView extends ImageView {

	private Context _context = null;
	public EmptyListImageView(Context context) {
		super(context);
		
		_context = context;
		
		init();
	}

	public EmptyListImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		_context = context;
		
		init();
	}

	public EmptyListImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		_context = context;
		
		init();
	}
	
	private void init()
	{
		//set this imageview's animation
		setImageDrawable(_context.getResources().getDrawable(R.anim.empty_list_animation));
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		// TODO Auto-generated method stub
		super.onWindowVisibilityChanged(visibility);
		
		AnimationDrawable anim = (AnimationDrawable) this.getDrawable(); 
		//anim.setVisible(false, true); //reset! see previous section 
		
		if(visibility == View.VISIBLE)
			anim.start(); 
		else
			anim.stop();
	}
	
	
	
	
	
	

}
