package com.lonian.android.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MaxWidthLinearLayout extends LinearLayout {

	public MaxWidthLinearLayout(Context context) {
		super(context);
	}
	
	public MaxWidthLinearLayout(Context context, AttributeSet attributes) {
		super(context, attributes);
	}

    @Override 
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) { 
    	super.onMeasure( widthMeasureSpec, heightMeasureSpec ); 

    	Context context = getContext(); 
    	WindowManager wm  = (WindowManager)context.getSystemService( Context.WINDOW_SERVICE ); 
    	Display display = wm.getDefaultDisplay(); 
            
    	setMeasuredDimension( display.getWidth(), getMeasuredHeight() );
    }
}
