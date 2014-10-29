package com.kpbird.globaltouchevent;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class GlobalTouchService extends Service implements OnTouchListener{

	private String TAG = this.getClass().getSimpleName();
	// window manager 
	private WindowManager mWindowManager;
	// linear layout will use to detect touch event
	private LinearLayout touchLayout;

    Instrumentation m_Instrumentation;

    private LinearLayout touchLayout_extend;
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		// create linear layout
		touchLayout = new LinearLayout(this);

		// set layout width 30 px and height is equal to full screen
		LayoutParams lp = new LayoutParams(40, 40);
		touchLayout.setLayoutParams(lp);
		// set color if you want layout visible on screen
		touchLayout.setBackgroundColor(Color.CYAN);
		// set on touch listener
		touchLayout.setOnTouchListener(this);

		// fetch window manager object 
		 mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		 // set layout parameter of window manager
		 WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
	        		40, // width of layout 30 px
	        		40, //WindowManager.LayoutParams.MATCH_PARENT, // height is equal to full screen
	                0,
                    1500,
                    WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
	                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus  
	                PixelFormat.TRANSLUCENT);      
         mParams.gravity = Gravity.LEFT | Gravity.TOP;
        Log.i(TAG, "add View");

	     mWindowManager.addView(touchLayout, mParams);
		
	}
	

	@Override
	public void onDestroy() {
		 if(mWindowManager != null) {
	            if(touchLayout != null) mWindowManager.removeView(touchLayout);
             if(touchLayout_extend != null) mWindowManager.removeView(touchLayout_extend);

	        }
		super.onDestroy();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP)
            Log.i(TAG, "Anchor touched");
//			Log.i(TAG, "Action :" + event.getAction() + "\t X :" + event.getRawX() + "\t Y :"+ event.getRawY());

        touchLayout_extend = new LinearLayout(this);

        // set layout width 30 px and height is equal to full screen
        LayoutParams lp = new LayoutParams(400, 400);

        touchLayout_extend.setLayoutParams(lp);
        // set color if you want layout visible on screen
        touchLayout_extend.setBackgroundColor(Color.parseColor("#0FFFFFFF"));

        touchLayout_extend.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP)
                    Log.i(TAG, "X :" + event.getRawX() + "\t Y :"+ event.getRawY());

                m_Instrumentation = new Instrumentation();
                long downTime = event.getDownTime() + 100;
                long eventTime = event.getEventTime() + 100;
                float x = event.getRawX();
                float y = event.getRawY() - 500;
// List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
                int metaState = 0;
                final MotionEvent motionEvent = MotionEvent.obtain(
                        downTime,
                        eventTime,
                        MotionEvent.ACTION_DOWN,
                        x,
                        y,
                        metaState
                );

//                m_Instrumentation.sendPointerSync(motionEvent);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "new thread");
                        Log.i(TAG, String.valueOf(motionEvent.getRawX()));
                        Log.i(TAG, String.valueOf(motionEvent.getRawY()));

                        m_Instrumentation.sendPointerSync(motionEvent);

                    }
                });
//                t.start();
//// Dispatch touch event to view
//                touchLayout_extend.dispatchTouchEvent(motionEvent);

                return false;


            }
        });

        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, // width of layout 30 px
                300, //WindowManager.LayoutParams.MATCH_PARENT, // height is equal to full screen
                0,
                2000,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);

        mWindowManager.addView(touchLayout_extend, mParams);

		return true;
	}
	
	

}
