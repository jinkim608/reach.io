package io.reach.reachiodemo;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.otto.Produce;

import io.reach.reachiodemo.bus.BusProvider;
import io.reach.reachiodemo.bus.RegionClickEvent;
import io.reach.reachiodemo.bus.RegionSwipeLeftEvent;
import io.reach.reachiodemo.bus.RegionSwipeRightEvent;
import io.reach.reachiodemo.interaction.OnFlingGestureListener;

public class GlobalTouchService extends Service {

    private WindowManager mWindowManager;
    private Display display;

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

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        app = App.getInstance();

        BusProvider.getInstance().register(this);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = mWindowManager.getDefaultDisplay();

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

                return true;
            }
        });

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

        mWindowManager.addView(ivSelector, eParams);

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
                BusProvider.getInstance().post(produceRegionClickEvent());
            }
        });

        ivThumbIndicator.setOnTouchListener(new OnFlingGestureListener() {
            @Override
            public void onTopToBottom() {
                //Your code here
                Log.d("####", "Top to bottom swipe detected on interaction region");
            }

            @Override
            public void onRightToLeft() {
                //Your code here
                Log.d("####", "Right to left swipe detected on interaction region");
                BusProvider.getInstance().post(produceRegionSwipeLeftEvent());
            }

            @Override
            public void onLeftToRight() {
                //Your code here
                Log.d("####", "Left to right swipe detected on interaction region");
                BusProvider.getInstance().post(produceRegionSwipeRightEvent());
            }

            @Override
            public void onBottomToTop() {
                //Your code here
                Log.d("####", "Bottom to top swipe detected on interaction region");
            }
        });

    }

    private void disableControlInteraction() {
        ivThumbIndicator.setOnClickListener(null);
        ivThumbIndicator.setOnTouchListener(null);
    }

    @Override
    public void onDestroy() {

        BusProvider.getInstance().unregister(this);

        if (mWindowManager != null) {
            if (ivSelector != null) mWindowManager.removeView(ivSelector);
            if (ivAnchor != null) mWindowManager.removeView(ivAnchor);
            if (ivAnchor != null) mWindowManager.removeView(ivThumbIndicator);
        }
        super.onDestroy();
    }


    private void updateIndicatorLocations() {

        /* update the selector point according to the current touch location */
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                selectorSize, selectorSize, sX - (selectorSize / 2), sY - (selectorSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.updateViewLayout(ivSelector, mParams);

        /* update thumb location indicator */
        WindowManager.LayoutParams tParams = new WindowManager.LayoutParams(
                thumbSize, thumbSize, tX - (thumbSize / 2), tY - (thumbSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        tParams.gravity = Gravity.LEFT | Gravity.TOP;

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

    /*
        Called when test button is clicked
     */
//    @Subscribe
//    public void onTestButtonClicked(TestButtonClickedEvent event) {
//        // post event to send x and y
//        BusProvider.getInstance().post(produceSelectorLocationEvent());
////        Log.d("####", "TestButtonClicked");
//    }

    /*
        Notify with the selector's current location
     */
//    @Produce
//    public SelectorLocationEvent produceSelectorLocationEvent() {
//        return new SelectorLocationEvent(sX, sY);
//    }

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
}