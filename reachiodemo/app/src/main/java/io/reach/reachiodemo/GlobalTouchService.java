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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import io.reach.reachiodemo.bus.BusProvider;
import io.reach.reachiodemo.bus.RegionClickEvent;
import io.reach.reachiodemo.bus.RegionMotionEvent;

public class GlobalTouchService extends Service {

    private WindowManager mWindowManager;
    private Display display;

    // parent view that contains ImageViews
    private ViewGroup mParentView;

    private ImageView ivAnchor;
    private ImageView ivSelector;
    private ImageView ivThumbIndicator;

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

    private App app;

    private Handler handler;
    private Timer timer;
    private TimerTask timerTask;

    private Animation clickAnimation;

    private static boolean isLongClicked = false;

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

        initLocations(true);

        initAnimations();

        // TODO: separate left and right anchor drop region
        // TODO: display them only while dragging the anchor

        setupSelector();
        setupThumbIndicator();
        setupAnchorDropRegion();

        setupAnchor();
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
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        tParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(ivThumbIndicator, tParams);
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

//        mWindowManager.addView(ivSelector, eParams);

        //Add ImageView inside the parent ViewGroup
        mWindowManager.addView(mParentView, eParams);
        mParentView.addView(ivSelector);
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
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        Log.d("####", "ACTION DOWN");
                        // trigger long press event after the delay
                        handler.postDelayed(mLongPressed, app.LONGCLICK_DELAY);

                        tX = (int) event.getRawX();
                        tY = (int) event.getRawY();

                        sX = (int) (tX + (tX - cX) * (movementRate - 1));
                        sY = (int) (tY + (tY - cY) * (movementRate - 1));

                        updateIndicatorLocations();

                        break;

                    case MotionEvent.ACTION_MOVE:

                        handler.removeCallbacks(mLongPressed);

                        Log.d("####", "ACTION MOVE");
                        tX = (int) event.getRawX();
                        tY = (int) event.getRawY();

                        sX = (int) (tX + (tX - cX) * (movementRate - 1));
                        sY = (int) (tY + (tY - cY) * (movementRate - 1));

                        updateIndicatorLocations();

                        //TODO: Determine when to enable and disable interaction
                        enableControlInteraction();

                        break;

                    case MotionEvent.ACTION_UP:
                        Log.d("####", "ACTION UP, long click: " + isLongClicked);
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
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(ivAnchor, mParams);
    }

    private void anchorOnLongClick(View view) {
        isLongClicked = true;

        Log.d("####", "LONG CLICK");
        removeSelector();
        removeThumbIndicator();

        showAnchorDropRegion();

        // TODO: display two drop anchor regions

        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDrag(data, shadowBuilder, null, 0);
//        view.setVisibility(View.INVISIBLE);
//        return true;
    }


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

    private void showAnchorDropRegion() {
         /* setup drop region images */
        WindowManager.LayoutParams dropRegionParmas = new WindowManager.LayoutParams(
                app.dropRegionSize, app.dropRegionSize, 0, 0,
                WindowManager.LayoutParams.TYPE_PHONE, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
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
                        // TODO: remove drop region
                        hideDropRegion();
                        // initialize indicator locations to the left
                        initLocations(true);

//                        mWindowManager.removeView(mParentView);
                        setupSelector();
//                        mWindowManager.removeView(ivThumbIndicator);
                        setupThumbIndicator();
//                        mWindowManager.removeView(ivAnchor);
                        setupAnchor();

                        // TODO: change tX, tY, re-render indicators

                        // Dropped, reassign View to ViewGroup
//                        View view = (View) event.getLocalState();
//                        ViewGroup owner = (ViewGroup) view.getParent();
//                        owner.removeView(view);
//                        LinearLayout container = (LinearLayout) v;
//                        container.addView(view);
//                        view.setVisibility(View.VISIBLE);
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
                        // TODO: remove drop region
                        hideDropRegion();
                        // initialize indicator locations to the right
                        initLocations(false);

                        setupSelector();
                        setupThumbIndicator();
                        setupAnchor();

                        // TODO: change tX, tY, re-render indicators

                        // Dropped, reassign View to ViewGroup
//                        View view = (View) event.getLocalState();
//                        ViewGroup owner = (ViewGroup) view.getParent();
//                        owner.removeView(view);
//                        LinearLayout container = (LinearLayout) v;
//                        container.addView(view);
//                        view.setVisibility(View.VISIBLE);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void enableControlInteraction() {

        // TODO: Send detected event to MainActivity

        ivThumbIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("####", "Click detected on interaction region");
                resetTimer();

//                BusProvider.getInstance().post(produceRegionClickEvent());
                BusProvider.getInstance().post(new RegionClickEvent(sX, sY));

                // trigger click animation

//                Animation clickAnimation = new ScaleAnimation(1.0f, 0.2f, 1.0f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//                clickAnimation.setDuration(200);
//                clickAnimation.setFillAfter(false);

                ivSelector.startAnimation(clickAnimation);
            }
        });

        ivThumbIndicator.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    BusProvider.getInstance().post(new RegionMotionEvent(sX, sY, event.getAction()));
                    resetTimer();
                }
                return false;
            }
        });

//        ivThumbIndicator.setOnTouchListener(new OnFlingGestureListener() {
//            @Override
//            public void onTopToBottom() {
//                Log.d("####", "Top to bottom swipe detected on interaction region");
//                BusProvider.getInstance().post(new RegionSwipeDownEvent(sX, sY));
//                resetTimer();
//            }
//
//            @Override
//            public void onRightToLeft() {
//                Log.d("####", "Right to left swipe detected on interaction region");
//                BusProvider.getInstance().post(new RegionSwipeLeftEvent(sX, sY));
//                resetTimer();
//            }
//
//            @Override
//            public void onLeftToRight() {
//                Log.d("####", "Left to right swipe detected on interaction region");
//                BusProvider.getInstance().post(new RegionSwipeRightEvent(sX, sY));
//                resetTimer();
//            }
//
//            @Override
//            public void onBottomToTop() {
//                Log.d("####", "Bottom to top swipe detected on interaction region");
//                BusProvider.getInstance().post(new RegionSwipeUpEvent(sX, sY));
//                resetTimer();
//            }
//        });

    }

    // reset timer for resetting indicator locations
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
            removeSelector();
            removeThumbIndicator();
            removeAnchor();
            hideDropRegion();
        }

        if (timer != null) {
            timer.cancel();
        }
    }

    private void removeAnchor() {
        if (ivAnchor != null) {
            try {
                mWindowManager.removeView(ivAnchor);
            } catch (Exception e) {
            }
        }
    }

    private void removeThumbIndicator() {
        if (ivThumbIndicator != null) {
            try {
                mWindowManager.removeView(ivThumbIndicator);
            } catch (Exception e) {
            }
        }
    }

    private void removeSelector() {

        if (ivSelector != null) {
            try {
                mParentView.removeView(ivSelector);
            } catch (Exception e) {
            }
        }
        if (mParentView != null) {
            try {
                mWindowManager.removeView(mParentView);
            } catch (Exception e) {
            }
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
        try {
            mWindowManager.updateViewLayout(mParentView, mParams);
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
            mWindowManager.updateViewLayout(ivThumbIndicator, tParams);
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

    // Initialize animations
    private void initAnimations() {
        clickAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);
        final Animation clickEndAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_end);
        clickAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivSelector.startAnimation(clickEndAnimation);
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