package io.reach.reachiodemo;

import android.app.ActivityManager;
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

import com.squareup.otto.Subscribe;

import io.reach.reachiodemo.bus.BusProvider;
import io.reach.reachiodemo.bus.RegionMotionEvent;
import io.reach.reachiodemo.ui.TabsPagerAdapter;

/**
 * Created by Jinhyun Kim, Muzi Li
 * https://github.com/jinkim608/reach.io
 * <p/>
 * MainActivity for this demo app. Displays swipeable tabs, a button, a list view and etc.,
 * as well as simulates user input touch events at the selector location
 */
public class MainActivity extends FragmentActivity {

    Intent globalService;
    private int counter = 0; // Count number of clicks on target button

    private ViewPager viewPager;
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

            case R.id.action_settings:
                Toast.makeText(this, "Selected Settings", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_refresh:
                Toast.makeText(this, "Selected Refresh", Toast.LENGTH_SHORT).show();
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

    /* listen for RegionMotionEvent (interaction region) */
    @Subscribe
    public void onRegionMotionEvent(RegionMotionEvent event) {
        simulateMotionEvent(event.sX, event.sY, event.action);
        Log.d("####", "ACTION MOVE: " + event.sX + ", " + event.sY + ", action: " + event.action);
    }

    /* simulate motion event (click and swipe) */
    private void simulateMotionEvent(int sX, int sY, int Action) {
        long downTime = SystemClock.uptimeMillis();
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                SystemClock.uptimeMillis(),
                Action,
                sX,
                sY,
                metaState
        );
        dispatchTouchEvent(motionEvent);
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