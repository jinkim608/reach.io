package io.reach.reachiodemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import io.reach.reachiodemo.App.SwipeDirection;
import io.reach.reachiodemo.bus.BusProvider;
import io.reach.reachiodemo.bus.RegionClickEvent;
import io.reach.reachiodemo.bus.RegionSwipeLeftEvent;
import io.reach.reachiodemo.bus.RegionSwipeRightEvent;
import io.reach.reachiodemo.bus.TestButtonClickedEvent;

public class MainActivity extends Activity {

    Intent globalService;
    private TextView tvSelectorLoc;
    private int counter = 0;    // Count number of clicks on target button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        globalService = new Intent(this, GlobalTouchService.class);
        tvSelectorLoc = (TextView) findViewById(R.id.tv_selector_loc);
    }

    public void targetClicked(View v) {
        counter++;
        ((Button) v).setText(String.valueOf(counter));
    }

    public void buttonClicked(View v) {

        if (v.getTag() == null) {
            startService(globalService);
            v.setTag("on");
            ((Button) v).setText("Stop Service");
            Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
        } else {
            stopService(globalService);
            v.setTag(null);
            ((Button) v).setText("Start Service");
            Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
        }
    }
//
//    // test button on click
//    public void testButtonClicked(View v) {
//        // post bus event
//        BusProvider.getInstance().post(produceTestButtonClickedEvent());
//
//    }

    /*
        Notify that the test button is clicked: MainAct --> Service
     */
    @Produce
    public TestButtonClickedEvent produceTestButtonClickedEvent() {
        return new TestButtonClickedEvent();
    }

    /*
        Called when X and Y values are returned: Service --> MainAct
     */
//    @Subscribe
//    public void onSelectorLocationEvent(SelectorLocationEvent event) {
//        tvSelectorLoc.setText("" + event.x + ", " + event.y + ")");
////      Log.d("####", "in Main X: " + event.x + ",  Y: " + event.y);
//
////        simulateClick(event);
//    }

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
}