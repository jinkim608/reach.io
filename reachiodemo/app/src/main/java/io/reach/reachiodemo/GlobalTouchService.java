package io.reach.reachiodemo;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class GlobalTouchService extends Service implements OnTouchListener {

    private String TAG = this.getClass().getSimpleName();
    // window manager
    private WindowManager mWindowManager;
    // linear layout will use to detect touch event
    private LinearLayout touchLayout;

    private ImageView ivIndicator;

    Instrumentation m_Instrumentation;

    private LinearLayout touchLayout_extend;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("#### GlobalTouchService", "In onCreate()");

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
        if (mWindowManager != null) {
            if (touchLayout != null) mWindowManager.removeView(touchLayout);
            if (touchLayout_extend != null) mWindowManager.removeView(touchLayout_extend);

        }
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("#### onTouch event", "");
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            Log.d("DRAG EVENT: ", "X: " + event.getX() + ", Y: " + event.getY());
        }

        return true;
    }


}
