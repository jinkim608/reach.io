package io.reach.reachiodemo.bus;

/**
 * Created by Jinhyun Kim, Muzi Li on 11/7/2014
 * https://github.com/jinkim608/reach.io
 * <p/>
 * Event fired when there is a motion event in the interaction region
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
