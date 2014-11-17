package io.reach.reachiodemo.bus;

/**
 * Created by Jinhyun Kim, Muzi Li on 11/7/2014
 * https://github.com/jinkim608/reach.io
 *
 * Event fired when there is a swipe-left gesture in the interaction region
 */
public class RegionSwipeLeftEvent {
    public int sX;
    public int sY;

    public RegionSwipeLeftEvent(int sX, int sY) {
        this.sX = sX;
        this.sY = sY;
    }
}
