package io.reach.reachiodemo.bus;

/**
 * Created by Jin on 11/7/14.
 */
public class RegionSwipeRightEvent {
    public int sX;
    public int sY;

    public RegionSwipeRightEvent(int sX, int sY) {
        this.sX = sX;
        this.sY = sY;
    }
}
