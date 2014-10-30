package io.reach.reachiodemo;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class GlobalTouchService extends Service implements OnTouchListener {

    private WindowManager mWindowManager;
    private Display display;

    private ImageView ivAnchor;
    private ImageView ivSelector;

    public static int anchorX;
    public static int anchorY;
    public static int selectorX;
    public static int selectorY;

    public Point size;

    private int selectorSize;
    private int anchorSize;
    private float movementRate;
    private int anchorOuterMargin;

    private App app;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        app = App.getInstance();

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = mWindowManager.getDefaultDisplay();

        /* initialize anchor and selector sizes and locations */
        anchorSize = app.anchorSize;
        selectorSize = app.selectorSize;
        movementRate = app.movementRate;
        anchorOuterMargin = app.anchorOuterMargin;

        // get screen dimension in px
        size = new Point();
        display.getSize(size);

        anchorX = anchorOuterMargin;
        anchorY = size.y - anchorSize - anchorOuterMargin - 40;

        selectorX = anchorX;
        selectorY = anchorY;

        // initialize image view for anchor and selector

        ivAnchor = new ImageView(this);
        ivAnchor.setImageDrawable(getResources().getDrawable(R.drawable.dot));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(anchorSize, anchorSize);
        ivAnchor.setLayoutParams(params);
        ivAnchor.setOnTouchListener(this);

        ivSelector = new ImageView(this);
        ivSelector.setImageDrawable(getResources().getDrawable(R.drawable.earth));
        FrameLayout.LayoutParams selectorParams = new FrameLayout.LayoutParams(selectorSize, selectorSize);
        ivSelector.setLayoutParams(selectorParams);

        /* layout param for selector */
        WindowManager.LayoutParams eParams = new WindowManager.LayoutParams(
                selectorSize, selectorSize, selectorX, selectorY, WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        eParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(ivSelector, eParams);

        /* layout param for anchor */
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                anchorSize, anchorSize, anchorX, anchorY,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(ivAnchor, mParams);

    }

    @Override
    public void onDestroy() {
        if (mWindowManager != null) {
            if (ivSelector != null) mWindowManager.removeView(ivSelector);
            if (ivAnchor != null) mWindowManager.removeView(ivAnchor);

        }
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.d("====", "rawY: " + event.getRawY() + ",\t anchorY: " + anchorY);

        /* getRaw to get coordinates relative to screen, not from view itself
         * calculate difference from the anchor point */
        float dx = event.getRawX() - anchorX;
        float dy = event.getRawY() - anchorSize - anchorOuterMargin - anchorY;

        /* update selector location with 2 * the vector from anchor point to touch point */
        selectorX = (int) (anchorX - (selectorSize) + movementRate * dx);
        selectorY = (int) (anchorY + movementRate * dy);

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
//            Log.d("####", "onTouch -- dx: " + dx + ",\t dy: " + dy);
//            Log.d("####", "x: " + event.getX() + ",\t y: " + event.getY());
        }

        updateSelectorLoc();

        return true;
    }


    private void updateSelectorLoc() {

        /* update the selector point according to the current touch location */
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                selectorSize, selectorSize, selectorX, selectorY,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.updateViewLayout(ivSelector, mParams);
    }


}
