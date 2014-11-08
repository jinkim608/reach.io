package io.reach.reachiodemo;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import io.reach.reachiodemo.App.SwipeDirection;
import io.reach.reachiodemo.bus.BusProvider;
import io.reach.reachiodemo.bus.RegionClickEvent;
import io.reach.reachiodemo.bus.RegionSwipeLeftEvent;
import io.reach.reachiodemo.bus.RegionSwipeRightEvent;
import io.reach.reachiodemo.bus.TestButtonClickedEvent;
import io.reach.reachiodemo.ui.TabsPagerAdapter;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    Intent globalService;
    private int counter = 0;    // Count number of clicks on target button

    private ViewPager viewPager;
    private ActionBar actionBar;
    private TabsPagerAdapter mAdapter;
    private static boolean serviceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        globalService = new Intent(this, GlobalTouchService.class);


        viewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);

        actionBar = getActionBar();

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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

        // Add 3 tabs, specifying the tab's text and TabListener
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
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_service:
                //TODO
                if (!isServiceRunning(GlobalTouchService.class)) {
                    Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
                    startService(globalService);
//                    serviceRunning = true;
                    item.setIcon(R.drawable.stop);


                } else {
                    Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
                    stopService(globalService);
//                    serviceRunning = false;
                    item.setIcon(R.drawable.start);
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void targetClicked(View v) {
        counter++;
        ((Button) v).setText(String.valueOf(counter));
    }

    /*
        Notify that the test button is clicked: MainAct --> Service
     */
    @Produce
    public TestButtonClickedEvent produceTestButtonClickedEvent() {
        return new TestButtonClickedEvent();
    }

    @Subscribe
    public void onRegionClickEvent(RegionClickEvent event) {
        simulateClick(event.sX, event.sY);
    }

    // simulate touch event
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

        Log.d("####", "Simulating Touch at: " + x + ", " + y + " @ " + downTime);
//        View v = findViewById(android.R.id.content).getRootView();
//        v.dispatchTouchEvent(motionEvent);
//        boolean dispatched = mCurrentActivity.dispatchTouchEvent(motionEvent);
//        Log.d("####", String.valueOf(dispatched));

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

    @Subscribe
    public void onRegionSwipeLeftEvent(RegionSwipeLeftEvent event) {
        simulateSwipe(event.sX, event.sY, SwipeDirection.Left);
    }

    @Subscribe
    public void onRegionSwipeRightEvent(RegionSwipeRightEvent event) {
        simulateSwipe(event.sX, event.sY, SwipeDirection.Right);
    }

    /* simulate swipe gesture at the x, y location to the direction passed in */
    private void simulateSwipe(int eX, int eY, SwipeDirection direction) {

        if (direction == SwipeDirection.Left) {
            // swipe to the left
            Log.d("####", "swipe at (" + eX + ", " + eY + ") to: LEFT");
        } else {
            // swipe to the right
            Log.d("####", "swipe at (" + eX + ", " + eY + ") to: RIGHT");
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