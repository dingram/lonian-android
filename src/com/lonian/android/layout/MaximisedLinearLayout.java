package com.lonian.android.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MaximisedLinearLayout extends LinearLayout {

	public MaximisedLinearLayout(Context context) {
		super(context);
	}
	
	public MaximisedLinearLayout(Context context, AttributeSet attributes) {
		super(context, attributes);
	}

    @Override 
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) { 
    	super.onMeasure( widthMeasureSpec, heightMeasureSpec ); 

    	Context context = getContext(); 
    	WindowManager wm  = (WindowManager)context.getSystemService( Context.WINDOW_SERVICE ); 
    	Display display = wm.getDefaultDisplay(); 
            
    	setMeasuredDimension( display.getWidth(), display.getHeight() ); 
    }
}
