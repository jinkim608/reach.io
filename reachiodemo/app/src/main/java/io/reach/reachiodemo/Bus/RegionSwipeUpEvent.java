package io.reach.reachiodemo.bus;

/**
 * Created by Jin on 11/7/14.
 */
public class RegionSwipeUpEvent {
    public int sX;
    public int sY;

    public RegionSwipeUpEvent(int sX, int sY) {
        this.sX = sX;
        this.sY = sY;
    }
}
