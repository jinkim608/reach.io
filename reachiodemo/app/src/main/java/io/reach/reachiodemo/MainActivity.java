package io.reach.reachiodemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import io.reach.reachiodemo.Bus.BusProvider;
import io.reach.reachiodemo.Bus.SelectorLocationEvent;
import io.reach.reachiodemo.Bus.TestButtonClickedEvent;

public class MainActivity extends Activity {

    Intent globalService;
    private TextView tvSelectorLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        globalService = new Intent(this, GlobalTouchService.class);
        tvSelectorLoc = (TextView) findViewById(R.id.tv_selector_loc);
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

    // test button on click
    public void testButtonClicked(View v) {
        // post bus event
        BusProvider.getInstance().post(produceTestButtonClickedEvent());
    }

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
    @Subscribe
    public void onSelectorLocationEvent(SelectorLocationEvent event) {
        tvSelectorLoc.setText("" + event.x + ", " + event.y + ")");
//        Log.d("####", "in Main X: " + event.x + ",  Y: " + event.y);
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
