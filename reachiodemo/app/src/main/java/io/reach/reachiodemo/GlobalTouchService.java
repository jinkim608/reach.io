package io.reach.reachiodemo;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.otto.Produce;

import java.util.Timer;
import java.util.TimerTask;

import io.reach.reachiodemo.bus.BusProvider;
import io.reach.reachiodemo.bus.RegionClickEvent;
import io.reach.reachiodemo.bus.RegionSwipeLeftEvent;
import io.reach.reachiodemo.bus.RegionSwipeRightEvent;
import io.reach.reachiodemo.interaction.OnFlingGestureListener;

public class GlobalTouchService extends Service {

    private WindowManager mWindowManager;
    private Display display;

    // parent view that contains ImageViews
    private ViewGroup mParentView;

    private ImageView ivAnchor;
    private ImageView ivSelector;
    private ImageView ivThumbIndicator;

    public Point size;

    private int selectorSize;
    private int anchorSize;
    private int thumbSize;
    private float movementRate;
    private int anchorOuterMargin;
    private int actionBarHeight;

    // controller location (fixed during the interaction)
    private int cX;
    private int cY;

    // touch location (rawX and rawY relative to screen)
    public static int tX;
    public static int tY;

    // selector location (calculated vector added to touch location)
    public static int sX;
    public static int sY;

    private App app;

    private Handler handler;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler();

        app = App.getInstance();

        BusProvider.getInstance().register(this);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = mWindowManager.getDefaultDisplay();

        // initialize parent ViewGroup
        mParentView = new FrameLayout(this);

        initLocations();

        // initialize image view for anchor and selector
        ivAnchor = new ImageView(this);
        ivAnchor.setImageDrawable(getResources().getDrawable(R.drawable.thumb_03));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(anchorSize, anchorSize);
        ivAnchor.setLayoutParams(params);
        ivAnchor.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Log.d("####", "rawY: " + event.getRawY() + ",\t controlY: " + cY);

                tX = (int) event.getRawX();
                tY = (int) event.getRawY();

                sX = (int) (tX + (tX - cX) * (movementRate - 1));
                sY = (int) (tY + (tY - cY) * (movementRate - 1));

                /* update selector location with 2 * the vector from anchor point to touch point */

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    // Log.d("####", "onTouch -- dx: " + dx + ",\t dy: " + dy);
                    // Log.d("####", "x: " + event.getX() + ",\t y: " + event.getY());
                }
                updateIndicatorLocations();

                //TODO: Determine when to enable and disable interaction
                enableControlInteraction();

                if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    resetTimer();
                    Log.d("TouchTest", "Touch up");
                }


                return true;
            }


        });

//        FrameLayout fl = new FrameLayout(this);
//        WindowManager.LayoutParams fParams = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_PHONE,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT);
//        fl.setLayoutParams(fParams);

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
                selectorSize, selectorSize, sX - (selectorSize / 2), sY - (selectorSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        eParams.gravity = Gravity.LEFT | Gravity.TOP;

//        mWindowManager.addView(ivSelector, eParams);

        //Add ImageView inside the parent ViewGroup
        mWindowManager.addView(mParentView, eParams);
        mParentView.addView(ivSelector);

        /* layout param for thumb indicator */
        WindowManager.LayoutParams tParams = new WindowManager.LayoutParams(
                app.thumbSize, app.thumbSize, tX - (thumbSize / 2), tY - (thumbSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        tParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(ivThumbIndicator, tParams);

        /* layout param for anchor */
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                anchorSize, anchorSize, cX - (anchorSize / 2), cY - (anchorSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(ivAnchor, mParams);
    }

    private void enableControlInteraction() {

        // TODO: Send detected event to MainActivity

        ivThumbIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("####", "Click detected on interaction region");
                resetTimer();

                BusProvider.getInstance().post(produceRegionClickEvent());

                // trigger click animation

                Animation clickAnimation = new ScaleAnimation(1.0f, 0.2f, 1.0f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                clickAnimation.setDuration(200);
                clickAnimation.setFillAfter(false);
                ivSelector.startAnimation(clickAnimation);
            }
        });

        ivThumbIndicator.setOnTouchListener(new OnFlingGestureListener() {
            @Override
            public void onTopToBottom() {
                Log.d("####", "Top to bottom swipe detected on interaction region");
            }

            @Override
            public void onRightToLeft() {
                Log.d("####", "Right to left swipe detected on interaction region");
                BusProvider.getInstance().post(produceRegionSwipeLeftEvent());
                resetTimer();
            }

            @Override
            public void onLeftToRight() {
                Log.d("####", "Left to right swipe detected on interaction region");
                BusProvider.getInstance().post(produceRegionSwipeRightEvent());
                resetTimer();
            }

            @Override
            public void onBottomToTop() {
                Log.d("####", "Bottom to top swipe detected on interaction region");
            }
        });

    }

    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timerTask = new MyTimerTask();

        timer.schedule(timerTask, app.RESET_DELAY);
    }

    private void disableControlInteraction() {
        ivThumbIndicator.setOnClickListener(null);
        ivThumbIndicator.setOnTouchListener(null);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        BusProvider.getInstance().unregister(this);

        if (mWindowManager != null) {
//            if (ivSelector != null) mWindowManager.removeView(ivSelector);
            if (ivAnchor != null) mWindowManager.removeView(ivAnchor);
            if (ivAnchor != null) mWindowManager.removeView(ivThumbIndicator);

            // destroy mParentView

            if (mParentView != null) mWindowManager.removeView(mParentView);

        }

        if (timer != null) {
            timer.cancel();
        }

    }


    private void updateIndicatorLocations() {

        /* update the selector point according to the current touch location */
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                selectorSize, selectorSize, sX - (selectorSize / 2), sY - (selectorSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.windowAnimations = android.R.style.Animation_Translucent;
//        mWindowManager.updateViewLayout(ivSelector, mParams);

        //Update mParentView
        mWindowManager.updateViewLayout(mParentView, mParams);


        /* update thumb location indicator */
        WindowManager.LayoutParams tParams = new WindowManager.LayoutParams(
                thumbSize, thumbSize, tX - (thumbSize / 2), tY - (thumbSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        tParams.gravity = Gravity.LEFT | Gravity.TOP;
        tParams.windowAnimations = android.R.style.Animation_Translucent;

        mWindowManager.updateViewLayout(ivThumbIndicator, tParams);
    }

    /* Place anchor, thumbIndicator, and selector at the initial component location */
    private void initLocations() {

        // get screen dimension in px
        size = new Point();
        display.getSize(size);

        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        anchorSize = app.anchorSize;
        selectorSize = app.selectorSize;
        thumbSize = app.thumbSize;
        movementRate = app.movementRate;
        anchorOuterMargin = app.anchorOuterMargin;

        cX = anchorOuterMargin + anchorSize / 2;
        cY = size.y - anchorOuterMargin - anchorSize / 2;

        tX = cX;
        tY = cY;

        sX = cX;
        sY = cY;
    }

    private void resetLocations() {

        Log.d("####", "Resetting indicator locations");
//        Animation moveRighttoLeft = new TranslateAnimation(tX, cX, tY, cY);
//        moveRighttoLeft.setDuration(1000);
//
//        AnimationSet animation = new AnimationSet(false);
//        animation.addAnimation(moveRighttoLeft);

//        ivThumbIndicator.setAnimation(animation);
//        ivThumbIndicator.startAnimation(animation);

        tX = cX;
        tY = cY;

        sX = cX;
        sY = cY;


        updateIndicatorLocations();
    }

    @Produce
    public RegionClickEvent produceRegionClickEvent() {
        return new RegionClickEvent(sX, sY);
    }

    @Produce
    public RegionSwipeLeftEvent produceRegionSwipeLeftEvent() {
        return new RegionSwipeLeftEvent(sX, sY);
    }

    @Produce
    public RegionSwipeRightEvent produceRegionSwipeRightEvent() {
        return new RegionSwipeRightEvent(sX, sY);
    }

    /* timer task to reset indicator locations */
    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resetLocations();
                    disableControlInteraction();
                }
            });
        }

        // use handler to call the method for UI thread
        private void runOnUiThread(Runnable runnable) {
            handler.post(runnable);
        }
    }
}