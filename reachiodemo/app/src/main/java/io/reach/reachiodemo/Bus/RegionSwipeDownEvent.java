package io.reach.reachiodemo.bus;

/**
 * Created by Jin on 11/7/14.
 */
public class RegionSwipeDownEvent {
    public int sX;
    public int sY;

    public RegionSwipeDownEvent(int sX, int sY) {
        this.sX = sX;
        this.sY = sY;
    }
}
