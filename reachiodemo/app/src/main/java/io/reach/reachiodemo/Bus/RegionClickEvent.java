package io.reach.reachiodemo.bus;

/**
 * Created by Jinhyun Kim, Muzi Li on 11/7/2014
 * https://github.com/jinkim608/reach.io
 * <p/>
 * Event fired when there is a click in the interaction region
 */
public class RegionClickEvent {
    public int sX;
    public int sY;

    public RegionClickEvent(int sX, int sY) {
        this.sX = sX;
        this.sY = sY;
    }
}
