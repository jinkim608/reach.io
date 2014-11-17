package io.reach.reachiodemo;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import io.reach.reachiodemo.App.SwipeDirection;
import io.reach.reachiodemo.bus.BusProvider;
import io.reach.reachiodemo.bus.RegionClickEvent;
import io.reach.reachiodemo.bus.RegionMotionEvent;
import io.reach.reachiodemo.bus.RegionSwipeDownEvent;
import io.reach.reachiodemo.bus.RegionSwipeLeftEvent;
import io.reach.reachiodemo.bus.RegionSwipeRightEvent;
import io.reach.reachiodemo.bus.RegionSwipeUpEvent;
import io.reach.reachiodemo.ui.TabsPagerAdapter;

/**
 * Created by Jinhyun Kim, Muzi Li
 * https://github.com/jinkim608/reach.io
 * <p/>
 * MainActivity for this demo app. Displays swipeable tabs, a button, a list view and etc.,
 * as well as simulates user input touch events at the selector location
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    Intent globalService;
    private int counter = 0; // Count number of clicks on target button

    private ViewPager viewPager;
    private ActionBar actionBar;
    private TabsPagerAdapter mAdapter;

    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = App.getInstance();
        globalService = new Intent(this, GlobalTouchService.class);

        viewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);

        actionBar = getActionBar();

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#243342")));

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                viewPager.setCurrentItem(tab.getPosition());
                // show the given tab
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

        // Add 2 tabs, specifying the tab's text and TabListener
        for (int i = 0; i < 2; i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText("Tab " + (i + 1))
                            .setTabListener(tabListener));
        }

        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

        // automatically start service in the beginning if not already running
        if (!isServiceRunning(GlobalTouchService.class)) {
            startService(globalService);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        if (isServiceRunning(GlobalTouchService.class)) {
            menu.getItem(0).setIcon(R.drawable.stop);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // action bar item
        switch (item.getItemId()) {
            case R.id.action_service:
                if (!isServiceRunning(GlobalTouchService.class)) {
                    Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
                    startService(globalService);
                    item.setIcon(R.drawable.stop);

                } else {
                    Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
                    stopService(globalService);
                    item.setIcon(R.drawable.start);
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // increment count on button click
    public void targetClicked(View v) {
        counter++;
        ((Button) v).setText(String.valueOf(counter));
    }

    /* listen for RegionClickEvent */
    @Subscribe
    public void onRegionClickEvent(RegionClickEvent event) {
        simulateClick(event.sX, event.sY);
    }

    /* simulate touch event at the location passed in */
    private void simulateClick(int eX, int eY) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = eX * 1.0f;
        float y = eY * 1.0f;

        // List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_DOWN,
                x,
                y,
                metaState
        );

        //click
        dispatchTouchEvent(motionEvent);

        MotionEvent motionEvent_UP = MotionEvent.obtain(
                downTime + 100,
                eventTime + 100,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );

        //click released
        dispatchTouchEvent(motionEvent_UP);
    }

    /* listen for RegionMotionEvent */
    @Subscribe
    public void onRegionMotionEvent(RegionMotionEvent event) {
//        simulateSwipe(event.tX, event.tY, SwipeDirection.Left);
        Log.d("####", "ACTION MOVE: " + event.sX + ", " + event.sY);
    }

    /* listen for RegionSwipeLeftEvent */
    @Subscribe
    public void onRegionSwipeLeftEvent(RegionSwipeLeftEvent event) {
        simulateSwipe(event.sX, event.sY, SwipeDirection.Left);
    }

    /* listen for RegionSwipeRightEvent */
    @Subscribe
    public void onRegionSwipeRightEvent(RegionSwipeRightEvent event) {
        simulateSwipe(event.sX, event.sY, SwipeDirection.Right);
    }

    /* listen for RegionSwipeUpEvent */
    @Subscribe
    public void onRegionSwipeUpEvent(RegionSwipeUpEvent event) {
        simulateSwipe(event.sX, event.sY, SwipeDirection.Up);
    }

    /* listen for RegionSwipeDownEvent */
    @Subscribe
    public void onRegionSwipeDownEvent(RegionSwipeDownEvent event) {
        simulateSwipe(event.sX, event.sY, SwipeDirection.Down);
    }

    /* simulate swipe gesture at the x, y location to the direction passed in */
    private void simulateSwipe(int eX, int eY, SwipeDirection direction) {

        // #### SWIPE LEFT
        if (direction == SwipeDirection.Left) {
            Log.d("####", "Swipe LEFT");

            long downTime = SystemClock.uptimeMillis();
//            long eventTime = SystemClock.uptimeMillis() + 100;

            int metaState = 0;
            MotionEvent motionEvent_Down = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN,
                    eX,
                    eY,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Down);

            MotionEvent motionEvent_Move = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_MOVE,
                    eX - app.swipeLengHorizontal,   //Swipe left
                    eY,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Move);

            MotionEvent motionEvent_Up = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis() + 1000,
                    MotionEvent.ACTION_UP,
                    eX - app.swipeLengHorizontal,
                    eY,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Up);


            // #### SWIPE RIGHT

        } else if (direction == SwipeDirection.Right) {
            Log.d("####", "Swipe RIGHT");

            long downTime = SystemClock.uptimeMillis();
//            long eventTime = SystemClock.uptimeMillis() + 100;

            int metaState = 0;
            MotionEvent motionEvent_Down = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN,
                    eX,
                    eY,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Down);

            MotionEvent motionEvent_Move = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_MOVE,
                    eX + app.swipeLengHorizontal,   //Swipe left
                    eY,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Move);

            MotionEvent motionEvent_Up = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis() + 1000,
                    MotionEvent.ACTION_UP,
                    eX + app.swipeLengHorizontal,
                    eY,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Up);

            // #### SWIPE UP

        } else if (direction == SwipeDirection.Up) {
            Log.d("####", "Swipe UP");

            long downTime = SystemClock.uptimeMillis();
//            long eventTime = SystemClock.uptimeMillis() + 100;

            int metaState = 0;
            MotionEvent motionEvent_Down = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN,
                    eX,
                    eY,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Down);

            MotionEvent motionEvent_Move = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_MOVE,
                    eX,   //Swipe up
                    eY - app.swipeLengVertical,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Move);

            MotionEvent motionEvent_Up = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis() + 3000,
                    MotionEvent.ACTION_UP,
                    eX,
                    eY - app.swipeLengVertical,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Up);

            // #### SWIPE DOWN

        } else if (direction == SwipeDirection.Down) {
            // swipe to the top
            Log.d("####", "Swipe DOWN");

            long downTime = SystemClock.uptimeMillis();
//            long eventTime = SystemClock.uptimeMillis() + 100;

            int metaState = 0;
            MotionEvent motionEvent_Down = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN,
                    eX,
                    eY,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Down);

            MotionEvent motionEvent_Move = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_MOVE,
                    eX,   //Swipe down
                    eY + app.swipeLengVertical,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Move);

            MotionEvent motionEvent_Up = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis() + 3000,
                    MotionEvent.ACTION_UP,
                    eX,
                    eY + app.swipeLengVertical,
                    metaState
            );
            dispatchTouchEvent(motionEvent_Up);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register ourselves so that we can provide the initial value.
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Always unregister when an object no longer should be on the bus.
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    /* check if service is running */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}