package io.reach.reachiodemo.bus;

/**
 * Created by Jin on 11/7/14.
 */
public class RegionSwipeLeftEvent {
    public int sX;
    public int sY;

    public RegionSwipeLeftEvent(int sX, int sY) {
        this.sX = sX;
        this.sY = sY;
    }
}
