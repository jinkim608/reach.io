package io.reach.reachiodemo.bus;

import android.view.MotionEvent;

/**
 * Created by Jin on 11/7/14.
 */
public class RegionMotionEvent {

    public int sX;
    public int sY;
    public int action;

    public RegionMotionEvent(int sX, int sY, int action) {
        this.sX = sX;
        this.sY = sY;
        this.action = action;
    }
}
