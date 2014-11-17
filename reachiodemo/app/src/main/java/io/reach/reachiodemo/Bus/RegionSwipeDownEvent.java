package io.reach.reachiodemo.bus;

/**
 * Created by Jinhyun Kim, Muzi Li on 11/7/2014
 * https://github.com/jinkim608/reach.io
 * <p/>
 * Event fired when there is a swipe-down gesture in the interaction region
 */
public class RegionSwipeDownEvent {
    public int sX;
    public int sY;

    public RegionSwipeDownEvent(int sX, int sY) {
        this.sX = sX;
        this.sY = sY;
    }
}
