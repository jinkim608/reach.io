package io.reach.reachiodemo;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
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

    private ImageView ivThumbIndicator;

    public static int anchorX;
    public static int anchorY;
    public static int selectorX;
    public static int selectorY;

    public static int thumbX;
    public static int thumbY;

    public Point size;

    private App app;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void updateSelectorLoc() {

        /* update the selector point according to the current touch location */
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                app.selectorSize, app.selectorSize, selectorX, selectorY,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.updateViewLayout(ivSelector, mParams);

        /* update thumb location indicator */
        WindowManager.LayoutParams tParams = new WindowManager.LayoutParams(
                app.thumbSize, app.thumbSize, thumbX, thumbY,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        tParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.updateViewLayout(ivThumbIndicator, tParams);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        app = App.getInstance();

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = mWindowManager.getDefaultDisplay();

        /* initialize anchor and selector location */

        // get screen dimension in px
        size = new Point();
        display.getSize(size);

        anchorX = app.anchorOuterMargin;
        anchorY = size.y - app.anchorSize - app.anchorOuterMargin;

        selectorX = anchorX;
        selectorY = anchorY;

        // initialize image view for anchor and selector

        ivAnchor = new ImageView(this);
        ivAnchor.setImageDrawable(getResources().getDrawable(R.drawable.thumb_03));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(app.anchorSize, app.anchorSize);
        ivAnchor.setLayoutParams(params);
        ivAnchor.setOnTouchListener(this);

        ivSelector = new ImageView(this);
        ivSelector.setImageDrawable(getResources().getDrawable(R.drawable.thumb_02));
        FrameLayout.LayoutParams earthParams = new FrameLayout.LayoutParams(app.selectorSize, app.selectorSize);
        ivSelector.setLayoutParams(earthParams);

        ivThumbIndicator = new ImageView(this);
        ivThumbIndicator.setImageDrawable(getResources().getDrawable(R.drawable.thumb_01));
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(app.thumbSize, app.thumbSize);
        ivThumbIndicator.setLayoutParams(thumbParams);

        /* layout param for selector */
        WindowManager.LayoutParams eParams = new WindowManager.LayoutParams(
                app.selectorSize, app.selectorSize, selectorX, selectorY, WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        eParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(ivSelector, eParams);

        /* layout param for thumb indicator */
        WindowManager.LayoutParams tParams = new WindowManager.LayoutParams(
                app.thumbSize, app.thumbSize, anchorX, anchorY,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        tParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(ivThumbIndicator, tParams);

        /* layout param for anchor */
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                app.anchorSize, app.anchorSize, anchorX, anchorY,
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
            if (ivAnchor != null) mWindowManager.removeView(ivThumbIndicator);

        }
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        /* getRaw to get coordinates relative to screen, not from view itself
         * calculate difference from the anchor point */
        float dx = event.getRawX() - anchorX;
        float dy = event.getRawY() - anchorY;

        /* update selector location with 2 * the vector from anchor point to touch point */
        selectorX = (int) (anchorX + app.movementRate * dx);
        selectorY = (int) (anchorY + app.movementRate * dy);

        thumbX = (int)(event.getRawX());
        thumbY = (int)(event.getRawY());


        if (event.getAction() == MotionEvent.ACTION_MOVE) {
//            Log.d("####", "onTouch -- dx: " + dx + ",\t dy: " + dy);
            Log.d("####", "x: " + event.getX() + ",\t y: " + event.getY());
        }

        updateSelectorLoc();

        return true;
    }


}
