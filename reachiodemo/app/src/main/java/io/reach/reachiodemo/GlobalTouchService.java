package io.reach.reachiodemo;

import android.app.Service;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import io.reach.reachiodemo.bus.BusProvider;
import io.reach.reachiodemo.bus.RegionMotionEvent;

/**
 * Created by Jinhyun Kim, Muzi Li
 * https://github.com/jinkim608/reach.io
 * <p/>
 * Reference used: https://github.com/kpbird/android-global-touchevent
 * <p/>
 * Service that displays three indicators(anchor, thumb indicator, selector) and takes touch
 * gesture user input
 */
public class GlobalTouchService extends Service {

    private WindowManager mWindowManager;
    private Display display;

    // parent view that contains ImageViews
    private ViewGroup vgSelector;
    private ViewGroup vgThumbIndicator;
    private ViewGroup vgSelectorSwipe;
    private ViewGroup vgThumbIndicatorSwipe;

    private ImageView ivAnchor;
    private ImageView ivSelector;
    private ImageView ivThumbIndicator;

    // image views of swipe indicators
    private ImageView ivThumbIndicatorSwipe;
    private ImageView ivSelectorSwipe;

    private ImageView ivDropLeft;
    private ImageView ivDropRight;

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

    private float mX, mY; // x, y coord to determine if there is drag (movement)

    private App app;

    private Handler handler;
    private Timer timer;
    private TimerTask timerTask;

    private boolean clickDown = false;
    private boolean clickUp = false;

    private Animation animationActionDown;
    private Animation animationActionUp;
    private Animation animationFadeIn;
    private Animation animationFadeOut;
    private Animation animationSwipeBegin;
    private Animation animationSwipeEnd;
    private Animation animationCircleFadeOut;
    private Animation animationCircleFadeIn;

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
        vgSelector = new FrameLayout(this);
        vgThumbIndicator = new FrameLayout(this);
        vgThumbIndicatorSwipe = new FrameLayout(this);
        vgSelectorSwipe = new FrameLayout(this);

        initLocations(true);

        initAnimations();

        setupSelector();
        setupThumbIndicator();

        setupAnchorDropRegion();

        setupAnchor();
    }

    private void setupThumbIndicatorOnSwipe() {
        removeThumbIndicatorSwipe();
        ivThumbIndicatorSwipe = new ImageView(this);

        ivThumbIndicatorSwipe.setImageDrawable(getResources().getDrawable(R.drawable.thumb_swipe));
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(app.thumbSize, app.thumbSize);
        ivThumbIndicatorSwipe.setLayoutParams(thumbParams);

        /* layout param for thumb indicator */
        WindowManager.LayoutParams tParams = new WindowManager.LayoutParams(
                app.thumbSize, app.thumbSize, tX - (thumbSize / 2), tY - (thumbSize / 2) - actionBarHeight / 2,
                // These are non-application windows providing user interaction with the phone (in particular, incoming calls).
                WindowManager.LayoutParams.TYPE_PHONE,
                // this window won't ever get key input focus
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        tParams.gravity = Gravity.LEFT | Gravity.TOP;

        vgThumbIndicatorSwipe.addView(ivThumbIndicatorSwipe);
        mWindowManager.addView(vgThumbIndicatorSwipe, tParams);
    }

    private void setupSelectorOnSwipe() {
        removeSelectorSwipe();
        ivSelectorSwipe = new ImageView(this);
        ivSelectorSwipe.setImageDrawable(getResources().getDrawable(R.drawable.selector_swipe));
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(app.selectorSize, app.selectorSize);
        ivSelectorSwipe.setLayoutParams(thumbParams);

        /* layout param for thumb indicator */
        WindowManager.LayoutParams tParams = new WindowManager.LayoutParams(
                app.selectorSize, app.selectorSize, sX - (selectorSize / 2), sY - (selectorSize / 2) - actionBarHeight / 2,
                // These are non-application windows providing user interaction with the phone (in particular, incoming calls).
                WindowManager.LayoutParams.TYPE_PHONE,
                // this window won't ever get key input focus
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        tParams.gravity = Gravity.LEFT | Gravity.TOP;

        vgSelectorSwipe.addView(ivSelectorSwipe);
        mWindowManager.addView(vgSelectorSwipe, tParams);
    }

    private void setupThumbIndicator() {
        removeThumbIndicator();
        ivThumbIndicator = new ImageView(this);

        ivThumbIndicator.setImageDrawable(getResources().getDrawable(R.drawable.thumb_01));
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(app.thumbSize, app.thumbSize);
        ivThumbIndicator.setLayoutParams(thumbParams);

        /* layout param for thumb indicator */
        WindowManager.LayoutParams tParams = new WindowManager.LayoutParams(
                app.thumbSize, app.thumbSize, tX - (thumbSize / 2), tY - (thumbSize / 2) - actionBarHeight / 2,
                // These are non-application windows providing user interaction with the phone (in particular, incoming calls).
                WindowManager.LayoutParams.TYPE_PHONE,
                // this window won't ever get key input focus
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        tParams.gravity = Gravity.LEFT | Gravity.TOP;

        vgThumbIndicator.addView(ivThumbIndicator);
        mWindowManager.addView(vgThumbIndicator, tParams);
    }

    private void setupSelector() {
        removeSelector();
        ivSelector = new ImageView(this);
        ivSelector.setImageDrawable(getResources().getDrawable(R.drawable.thumb_02));
        FrameLayout.LayoutParams earthParams = new FrameLayout.LayoutParams(app.selectorSize, app.selectorSize);
        ivSelector.setLayoutParams(earthParams);

        /* layout param for selector */
        WindowManager.LayoutParams eParams = new WindowManager.LayoutParams(
                selectorSize, selectorSize, sX - (selectorSize / 2), sY - (selectorSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        eParams.gravity = Gravity.LEFT | Gravity.TOP;

        //Add ImageView inside the parent ViewGroup
        vgSelector.addView(ivSelector);
        mWindowManager.addView(vgSelector, eParams);
    }

    private void setupAnchor() {
        // initialize image view for anchor and selector
        removeAnchor();
        ivAnchor = new ImageView(this);
        ivAnchor.setImageDrawable(getResources().getDrawable(R.drawable.thumb_03));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(anchorSize, anchorSize);
        ivAnchor.setLayoutParams(params);

        final Handler handler = new Handler();
        final Runnable mLongPressed = new Runnable() {
            public void run() {
                Log.i("", "Long press!");
                anchorOnLongClick(ivAnchor);
            }
        };


        ivAnchor.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                tX = (int) event.getRawX();
                tY = (int) event.getRawY();

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        mX = tX; // update last X
                        mY = tY; // update last Y
                        Log.d("####", "ACTION DOWN");
                        // trigger long press event after the delay
                        handler.postDelayed(mLongPressed, app.LONGCLICK_DELAY);

                        sX = (int) (tX + (tX - cX) * (movementRate - 1));
                        sY = (int) (tY + (tY - cY) * (movementRate - 1));

                        updateIndicatorLocations();
                        break;

                    case MotionEvent.ACTION_MOVE:

                        // Determine if moved more than threshold
                        if (tX != mX && tY != mY) {
//                            Log.d("####", "ACTION MOVE: " + tX + ", " + tY);

                            sX = (int) (tX + (tX - cX) * (movementRate - 1));
                            sY = (int) (tY + (tY - cY) * (movementRate - 1));

                            updateIndicatorLocations();
                            handler.removeCallbacks(mLongPressed);

                            if (timer != null) {
                                timer.cancel();
                            }
                        }
                        mX = tX; // update last X
                        mY = tY; // update last Y
                        break;

                    case MotionEvent.ACTION_UP:
                        enableControlInteraction();
//                        Log.d("####", "ACTION UP, long click: " + isLongClicked);
                        handler.removeCallbacks(mLongPressed);
                        resetTimer();

                    default:
                        return false;
                }
                return false;
            }
        });

        /* layout param for anchor */
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                anchorSize, anchorSize, cX - (anchorSize / 2), cY - (anchorSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(ivAnchor, mParams);
    }

    private void anchorOnLongClick(View view) {
        Log.d("####", "LONG CLICK");
        removeSelector();
        removeThumbIndicator();
        showAnchorDropRegion();

        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDrag(data, shadowBuilder, null, 0);
    }


    /* remove the views for left and right drop regions */
    private void hideDropRegion() {
        if (ivDropLeft != null) {
            try {
                mWindowManager.removeView(ivDropLeft);
            } catch (Exception e) {
            }
        }
        if (ivDropRight != null) {
            try {
                mWindowManager.removeView(ivDropRight);
            } catch (Exception e) {
            }
        }
    }

    /* display anchor drop region left and right */
    private void showAnchorDropRegion() {
        WindowManager.LayoutParams dropRegionParmas = new WindowManager.LayoutParams(
                app.dropRegionSize, app.dropRegionSize, 0, 0,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        dropRegionParmas.gravity = Gravity.BOTTOM | Gravity.LEFT;
        mWindowManager.addView(ivDropLeft, dropRegionParmas);

        dropRegionParmas.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        mWindowManager.addView(ivDropRight, dropRegionParmas);
    }

    /* set up the region where anchor can be drag-and-dropped */
    private void setupAnchorDropRegion() {
        ivDropLeft = new ImageView(this);
        ivDropLeft.setImageDrawable(getResources().getDrawable(R.drawable.dropregion_normal));
        ivDropLeft.setPadding(0, 100, 100, 0);
        ivDropLeft.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                Log.d("####", "onDrag");
                switch (event.getAction()) {

                    case DragEvent.ACTION_DRAG_STARTED:
                        // do nothing
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.d("####", "DRAG_ENTERED");
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.d("####", "DRAG_EXITED");
                        ivDropLeft.setImageDrawable(getResources().getDrawable(R.drawable.dropregion_normal));
                        break;

                    case DragEvent.ACTION_DROP:

                        Log.d("####", "DROP ON: " + event.getX() + ", " + event.getY());
                        hideDropRegion();
                        // initialize indicator locations to the left
                        initLocations(true);

                        setupSelector();
                        setupThumbIndicator();
                        setupAnchor();
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:

                    default:
                        break;
                }
                return true;
            }
        });

        ivDropRight = new ImageView(this);
        ivDropRight.setImageDrawable(getResources().getDrawable(R.drawable.dropregion_normal));
        ivDropRight.setPadding(100, 100, 0, 0);
        ivDropRight.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                Log.d("####", "onDrag");
                switch (event.getAction()) {

                    case DragEvent.ACTION_DRAG_STARTED:
                        // do nothing
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.d("####", "DRAG_ENTERED");
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.d("####", "DRAG_EXITED");
                        ivDropLeft.setImageDrawable(getResources().getDrawable(R.drawable.dropregion_normal));
                        break;

                    case DragEvent.ACTION_DROP:
                        Log.d("####", "DROP ON: " + event.getX() + ", " + event.getY());
                        hideDropRegion();
                        // initialize indicator locations to the right
                        initLocations(false);
                        setupSelector();
                        setupThumbIndicator();
                        setupAnchor();
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:

                    default:
                        break;
                }
                return true;
            }
        });
    }

    /* attach event listeners on the thumb indicator after moving out of the anchor */
    private void enableControlInteraction() {

        ivSelector.startAnimation(animationCircleFadeOut);
        ivThumbIndicator.startAnimation(animationCircleFadeOut);

        // Send detected event to MainActivity
        vgThumbIndicator.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                cancelTimer();

                // Calculate selector movement
                tX = (int) event.getRawX();
                tY = (int) event.getRawY();

                sX = (int) (tX + (tX - cX) * (movementRate - 1));
                sY = (int) (tY + (tY - cY) * (movementRate - 1));

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("Thumb", "Action Down");
//                        ivSelector.startAnimation(animationActionDown);
//                        setupThumbIndicatorOnSwipe();
                        setupSelectorOnSwipe();
//                        ivThumbIndicatorSwipe.startAnimation(animationSwipeBegin);
                        ivSelectorSwipe.startAnimation(animationSwipeBegin);

                        break;

                    case MotionEvent.ACTION_UP:
                        clickUp = true;
                        Log.d("Thumb", "Action Up");

                        if (clickDown == true) {
                            ivSelectorSwipe.startAnimation(animationSwipeEnd);
                        }

                        resetTimer();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        updateIndicatorLocationsOnSwipe();
                        break;

                }

                // Send selector location and event type (UP, DOWN and MOVE)
                if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
                    BusProvider.getInstance().post(new RegionMotionEvent(sX, sY, event.getAction()));
                }
                return false;
            }
        });
    }

    /* cancel timer if not null */
    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /* reset timer for resetting indicator locations */
    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timerTask = new MyTimerTask();

        timer.schedule(timerTask, app.RESET_DELAY);
    }

    /* remove event listeners on thumb indicator */
    private void disableControlInteraction() {
        vgThumbIndicator.setOnTouchListener(null);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        BusProvider.getInstance().unregister(this);

        if (mWindowManager != null) {
            removeSelector();
            removeThumbIndicator();
            removeAnchor();
            hideDropRegion();
        }

        if (timer != null) {
            timer.cancel();
        }
    }

    /* remove the image view for anchor from the window */
    private void removeAnchor() {
        if (ivAnchor != null) {
            try {
                mWindowManager.removeView(ivAnchor);
            } catch (Exception e) {
            }
        }
    }

    /* remove the image view for thumb indicator from the window */
    private void removeThumbIndicator() {
        if (ivThumbIndicator != null) {
            try {
                vgThumbIndicator.removeView(ivThumbIndicator);
            } catch (Exception e) {
            }
        }
        if (ivThumbIndicator != null) {
            try {
                mWindowManager.removeView(vgThumbIndicator);
            } catch (Exception e) {
            }
        }
    }

    /* remove the image view and view group for thumb swipe indicator from the window when swipe ends */
    private void removeThumbIndicatorSwipe() {
        if (ivThumbIndicatorSwipe != null) {
            try {
                vgThumbIndicatorSwipe.removeView(ivThumbIndicatorSwipe);
            } catch (Exception e) {
            }
        }
        if (vgThumbIndicatorSwipe != null) {
            try {
                mWindowManager.removeView(vgThumbIndicatorSwipe);
            } catch (Exception e) {
            }
        }
    }

    /* remove the image view and view group for selector swipe indicator from the window when swipe ends */
    private void removeSelectorSwipe() {
        if (ivSelectorSwipe != null) {
            try {
                vgSelectorSwipe.removeView(ivSelectorSwipe);
            } catch (Exception e) {

            }
        }
        if (vgSelectorSwipe != null) {
            try {
                mWindowManager.removeView(vgSelectorSwipe);
            } catch (Exception e) {
            }
        }
    }

    /* remove the image view and view group for selector from the window */
    private void removeSelector() {

        if (ivSelector != null) {
            try {
                vgSelector.removeView(ivSelector);
            } catch (Exception e) {
            }
        }
        if (vgSelector != null) {
            try {
                mWindowManager.removeView(vgSelector);
            } catch (Exception e) {
            }
        }
    }

    /* update the location params of swipe indicators and redraw */
    private void updateIndicatorLocationsOnSwipe() {
        /* update selector location for swipe*/
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                selectorSize, selectorSize, sX - (selectorSize / 2), sY - (selectorSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.windowAnimations = android.R.style.Animation_Translucent;

        try {
            mWindowManager.updateViewLayout(vgSelectorSwipe, mParams);
        } catch (Exception e) {
        }

        /* update thumb indicator location for swipe */
        WindowManager.LayoutParams tParams = new WindowManager.LayoutParams(
                thumbSize, thumbSize, tX - (thumbSize / 2), tY - (thumbSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        tParams.gravity = Gravity.LEFT | Gravity.TOP;
        tParams.windowAnimations = android.R.style.Animation_Translucent;
        try {
            mWindowManager.updateViewLayout(vgThumbIndicatorSwipe, tParams);
        } catch (Exception e) {
        }
    }

    /* update the location params for indicators and redraw */
    private void updateIndicatorLocations() {

        /* update the selector point according to the current touch location */
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                selectorSize, selectorSize, sX - (selectorSize / 2), sY - (selectorSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.windowAnimations = android.R.style.Animation_Translucent;

        //Update vgSelector
        try {
            mWindowManager.updateViewLayout(vgSelector, mParams);
        } catch (Exception e) {
        }

        /* update thumb location indicator */
        WindowManager.LayoutParams tParams = new WindowManager.LayoutParams(
                thumbSize, thumbSize, tX - (thumbSize / 2), tY - (thumbSize / 2) - actionBarHeight / 2,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        tParams.gravity = Gravity.LEFT | Gravity.TOP;
        tParams.windowAnimations = android.R.style.Animation_Translucent;
        try {
            mWindowManager.updateViewLayout(vgThumbIndicator, tParams);
        } catch (Exception e) {
        }
    }

    /* Place anchor, thumbIndicator, and selector at the initial component location */
    private void initLocations(boolean isLeft) {

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

        if (isLeft) {
            cX = anchorOuterMargin + anchorSize / 2;
        } else {
            cX = size.x - anchorOuterMargin - anchorSize / 2;
        }

        cY = size.y - anchorOuterMargin - anchorSize / 2;
        tX = cX;
        tY = cY;

        sX = cX;
        sY = cY;
    }

    /* reset the locations of the three indicators to the starting position  */
    private void resetLocations() {

        ivSelector.startAnimation(animationFadeIn);
        ivThumbIndicator.startAnimation(animationFadeIn);

        ivSelector.setImageDrawable(getResources().getDrawable(R.drawable.thumb_02));
        ivThumbIndicator.setImageDrawable(getResources().getDrawable(R.drawable.thumb_01));

        Log.d("####", "Resetting indicator locations");

        tX = cX;
        tY = cY;

        sX = cX;
        sY = cY;

        updateIndicatorLocations();
    }

    /* Initialize animations */
    private void initAnimations() {

        animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        animationFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ivSelector.startAnimation(animationFadeOut);
                ivThumbIndicator.startAnimation(animationFadeOut);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                resetLocations();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);

//        clickAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);
//        final Animation clickEndAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_end);
//        clickAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                ivSelector.startAnimation(clickEndAnimation);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });


        /* logic added to show full animations for both click and long tab */
        animationActionDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);
        animationActionUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_end);

        animationActionDown.setFillEnabled(true);
        animationActionDown.setFillAfter(true);
        animationActionDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (clickUp == true) {
                    ivSelector.startAnimation(animationActionUp);
                }
                clickDown = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationActionUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clickUp = false;
                clickDown = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        /* animations when swipe begins and ends */
        animationSwipeBegin = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.swipe_begin);
        animationSwipeEnd = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.swipe_end);
        animationSwipeBegin.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (clickUp == true) {
                    ivSelectorSwipe.startAnimation(animationSwipeEnd);
                }
                clickDown = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationSwipeEnd.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clickUp = false;
                clickDown = false;
                removeSelectorSwipe();
                removeThumbIndicatorSwipe();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animationCircleFadeOut = new AlphaAnimation(1, 0);
        animationCircleFadeOut.setDuration(300);
        animationCircleFadeIn = new AlphaAnimation(0, 1);
        animationCircleFadeIn.setDuration(300);

        animationCircleFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivSelector.setImageDrawable(getResources().getDrawable(R.drawable.selector_swipe));
                ivSelector.startAnimation(animationCircleFadeIn);
                ivThumbIndicator.setImageDrawable(getResources().getDrawable(R.drawable.thumb_swipe));
                ivThumbIndicator.startAnimation(animationCircleFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    /* timer task to reset indicator locations */
    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    resetLocations();
                    ivSelector.startAnimation(animationFadeOut);
                    ivThumbIndicator.startAnimation(animationFadeOut);
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