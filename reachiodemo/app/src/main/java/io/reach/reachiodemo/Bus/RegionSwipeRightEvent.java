package io.reach.reachiodemo.bus;

/**
 * Created by Jinhyun Kim, Muzi Li on 11/7/2014
 * https://github.com/jinkim608/reach.io
 *
 * Event fired when there is a swipe-right gesture in the interaction region
 */
public class RegionSwipeRightEvent {
    public int sX;
    public int sY;

    public RegionSwipeRightEvent(int sX, int sY) {
        this.sX = sX;
        this.sY = sY;
    }
}
